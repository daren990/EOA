package cn.oa.web.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.consts.Sessions;
import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentWechatrelation;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopWechatrelation;
import cn.oa.service.WxClientService;
import cn.oa.service.client.ClientService;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.PathUtil;
import cn.oa.utils.web.Webs;
/**
 * 负责对用来拦截没有登录的客户的访问，而且如果没有登录，会自动根据微信回调的code来实现自动登录，如果自动登录失败，才会被拦截
 * @author Administrator
 *
 */
public class ClientAutoLogin implements ActionFilter {

	public static Log log = Logs.getLog(ClientAutoLogin.class);


	@Override
	public View match(ActionContext ctx) {
		Ioc ioc = ctx.getIoc();
		ClientService clientService = ioc.get(ClientService.class, "clientService");
//		WxClientService wxClientService = ioc.get(WxClientService.class, "wxClientService");
		Dao dao = ioc.get(Dao.class, "dao");
		
		HttpServletRequest req = ctx.getRequest();
		String org_id = req.getParameter("org_id");
		String code = req.getParameter("code");
		Asserts.isNull("org_id", "请指定公司的id");
		
		//从session中获取客户个人信息
		ShopClient sc = (ShopClient) req.getSession().getAttribute(Sessions.wxShopClient);
		MapBean wxShopClientMap = (MapBean) req.getSession().getAttribute(Sessions.wxShopClientMap);
		
		if(code == null || code.equals("")){
			//进入本流程，说明方法并非是微信回调的
			if(sc !=null && wxShopClientMap != null){
				if(wxShopClientMap.containsKey(org_id)){
					return null;
				}
			}
			//转发微信重新获取code，以此实现自动登录
			String redirect = "/wx/redirect/redirectWechat?redirectUrl="+PathUtil.getRequestURL(req)+"&org_id="+org_id;
			return new ServerRedirectView(redirect);
			
		}else{
			try {
				if(sc !=null && wxShopClientMap != null){
					if(wxShopClientMap.containsKey(org_id)){
						return null;
					}else{
						//装载了拦截后所要跳转到的路径
						String redirect = null;
						MapBean mb = clientService.getShopClient(org_id, code);
						if(mb.get("type").toString().equals("-1") || mb.get("type").toString().equals("1")){
							//转发微信重新获取code
							redirect = "/wx/redirect/redirectWechat?redirectUrl="+PathUtil.getRequestURL(req)+"&org_id="+org_id;
							return new ServerRedirectView(redirect);
						
						}else if(mb.get("type").toString().equals("2")){
							//当客户的手机（微信）换了，此时需要将新的手机（微信）自动绑定到该客户中覆盖之前的
							ShopWechatrelation scr = dao.fetch(ShopWechatrelation.class, Cnd.where("clientId", "=", sc.getId()).and("orgId", "=", org_id));
							String newOpenId = (String)mb.get("openid");
							if(scr == null){
								scr = new ShopWechatrelation();
								scr.setClientId(sc.getId());
								scr.setCreateTime(new Date());
								scr.setOpenId(newOpenId);
								scr.setOrgId(Integer.parseInt(org_id));
								dao.insert(scr);
							}else{
								//该OPENID不存在的时候才更新，正常来说应当不相等
								if(!newOpenId.equals(scr.getOpenId())){
									scr.setClientId(sc.getId());
									scr.setCreateTime(new Date());
									scr.setOpenId(newOpenId);
									scr.setOrgId(Integer.parseInt(org_id));
									dao.update(scr);
								}
							}
							//更新缓存
							wxShopClientMap.put(org_id, newOpenId);
							return null;
						}else if(mb.get("type").toString().equals("3")){
							//进入该流程，说明是带着缓存访问新的公众号，但这个公众号(公司)之前已经绑定了这个openId
							ShopClient dbShopClient = (ShopClient)mb.get("shopClient");
							if(!dbShopClient.getId().equals(sc.getId())){
								//覆盖之前缓存的客户的对象,即相当于换账号登录
								req.getSession().setAttribute(Sessions.wxEduStudent, dbShopClient);
								MapBean newMb = new MapBean();
								newMb.put(org_id , (String)mb.get("openid"));
								req.getSession().setAttribute(Sessions.wxEduStudentMap, newMb);
							}else{
								wxShopClientMap.put(org_id, (String)mb.get("openid"));
							}
							return null;
						}
					}
				}else{
					//装载了拦截后所要跳转到的路径
					String redirect = null;
					MapBean mb = clientService.getShopClient(org_id, code);
					
					if(mb.get("type").toString().equals("-1") || mb.get("type").toString().equals("1")){
						//转发微信重新获取code
						redirect = "/wx/redirect/redirectWechat?redirectUrl="+PathUtil.getRequestURL(req)+"&org_id="+org_id;
						return new ServerRedirectView(redirect);
					
					}else if(mb.get("type").toString().equals("2")){
						//转跳登记信息的页面,此时已经获取到openid
						redirect = "/wx/client/join_view?org_id="+org_id+"&code="+code+"&openid="+mb.getString("openid");
						return new ServerRedirectView(redirect);
					
					}else if(mb.get("type").toString().equals("3")){
						//shopClient存入session，还有客户所有的openid和orgid以键值对的方式存入了map中
						sc = (ShopClient) mb.get("shopClient");
//						List<ShopWechatrelation> shopWechatrelations = wxClientService.getOpenIdAndOrgId(sc.getId());
//						wxShopClientMap = toMapBean(shopWechatrelations);
						wxShopClientMap = new MapBean();
						wxShopClientMap.put(org_id, (String)mb.get("openid"));
						Webs.getReq().getSession().setAttribute(Sessions.wxShopClientMap, wxShopClientMap);
						Webs.getReq().getSession().setAttribute(Sessions.wxShopClient, sc);
						return null;
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("客户自动登录失败");
			}
		}
		return null;
	}
	
	private MapBean toMapBean(List<ShopWechatrelation> shopWechatrelations){
		MapBean mapBean = new MapBean();
		if(shopWechatrelations == null){
			return mapBean; 
		}
		for(ShopWechatrelation shopWechatrelation : shopWechatrelations){
			mapBean.put(shopWechatrelation.getOrgId(), shopWechatrelation.getOpenId());
		}
		return mapBean;
	}
}
