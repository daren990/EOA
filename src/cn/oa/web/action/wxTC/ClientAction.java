package cn.oa.web.action.wxTC;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.consts.Sessions;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopWechatrelation;
import cn.oa.utils.Calendars;
import cn.oa.utils.MapBean;
import cn.oa.utils.SmsUtil;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.PathUtil;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;
import cn.oa.web.action.hrm.ArchiveAction;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.view.FreemarkerView;
import cn.oa.web.wx.Config;

@Filters
@IocBean(name = "wx.client")
@At(value = "/wx/client")
public class ClientAction extends Action {

	public static Log log = Logs.getLog(ClientAction.class);
	
	@GET
	@At
	public View join_view(HttpServletRequest req, String org_id,String code,String openid) throws Exception {
		String redirect = null;
		if(openid == null || openid.equals("")){
			Config c = wxClientService.initOrgConfig(org_id);
			openid = wxClientService.getOpenid(code, c);
			if(openid.equals("reset")){
				//重新获取到code
				redirect = "/wx/redirect/redirectWechat?redirectUrl="+PathUtil.getBasePath(req)+"/wx/client/join_view&org_id="+org_id;
				return new ServerRedirectView(redirect);
			}
		}	
		req.setAttribute("openid", openid);
		req.setAttribute("org_id", org_id);
		return new FreemarkerView("wxTC/wechat_join");
	}
	
	@GET
	@At
	@Ok("Json")
	public Object smsValidate(HttpServletRequest req, HttpSession session) throws Exception {
		MapBean mb = new MapBean();
		try {
			String clientPhone = Https.getStr(req, "clientPhone");
			Integer smsCode = null;
	
			//随着生成四位数
			//smsCode = (int) (Math.random() * 10000);
			
			String str="0123456789";
			StringBuilder sb=new StringBuilder(4);
			for(int i=0;i<4;i++){
				char ch=str.charAt(new Random().nextInt(str.length()));
				sb.append(ch);
			}
			smsCode = Integer.parseInt(sb.toString());
			session.setAttribute("smsCode", smsCode);
			int returnCode = toolsService.sendSmsForIHuYi(clientPhone, sb.toString());
			Code.ok(mb, "验证成功");
			mb.put("returnCode",returnCode);
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "内部系统错误");
		}
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object join(HttpServletRequest req, HttpSession session ) throws Exception {
		MapBean mb = new MapBean();
		try {
			Integer smsCode = Https.getInt(req, "smsCode");
			String clientName = Https.getStr(req, "clientName");
			String clientPhone = Https.getStr(req, "clientPhone");
			String openid = Https.getStr(req, "openid");
			Integer org_id = Https.getInt(req, "org_id");
			if(session.getAttribute("smsCode") == null || !((Integer)session.getAttribute("smsCode")).equals(smsCode)){
				System.out.println(session.getAttribute("smsCode"));
				Code.error(mb, "验证码输入错误");
				return mb;
			}
			wxClientService.wx_InsertShopClient(clientName, clientPhone, org_id, openid);
			MapBean wxShopClientMap = (MapBean) req.getSession().getAttribute(Sessions.wxShopClientMap);
			//存入缓存中
			if(wxShopClientMap != null){
				wxShopClientMap.put(org_id, openid);
			}
			Code.ok(mb, "验证成功");
		} catch (Exception e) {
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
	
	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View editPage(HttpServletRequest req, String org_id) throws Exception {
		ShopClient sc = Webs.getSCInSession(req);
		String openid = Webs.getOpenidInSession(req, org_id);
//		System.out.println(openid);
		req.setAttribute("org_id", org_id);
		req.setAttribute("sc", sc);
		return new FreemarkerView("wxTI/wechat_clientEdit");
	}
	
	@POST
	@At
	@Ok("json")
	@Filters({@By(type = ClientAutoLogin.class)})
	public MapBean edit(HttpServletRequest req, HttpSession session){
		MapBean mb = new MapBean();
		try {
			//获取缓存中的客户信息
			ShopClient sc = Webs.getSCInSession(req);
			Integer gender = Https.getInt(req, "sex", "性别");
			String phone = Https.getStr(req, "telephone", R.CLEAN,R.RANGE, "{1,20}", "联系电话");
			String address = Https.getStr(req, "address", R.CLEAN,R.RANGE, "{1,200}", "联系地址");
			String birthday_yyyyMMdd = Https.getStr(req, "birthday_yyyyMMdd", R.D, "出生年月");
			DateTime birthday = null;
			if(birthday_yyyyMMdd != null && birthday_yyyyMMdd != ""){birthday = Calendars.parse(birthday_yyyyMMdd, Calendars.DATE);}
			String truename = Https.getStr(req, "truename", R.REQUIRED, "客户姓名");
			
			ShopClient client = new ShopClient();
			client.setId(sc.getId());
			client.setSex(gender);
			client.setTelephone(phone);
			client.setAddress(address);
			if(birthday != null){
				client.setBirthday(birthday.toDate());	
			}
			client.setTruename(truename);
			clientService.addOrUpdate(client);
			
			//清除缓存中的客户信息
			session.setAttribute(Sessions.wxShopClientMap, null);
			session.setAttribute(Sessions.wxShopClient, null);
			
			mb.put("status", 1);
		} catch (Exception e) {
			mb.put("status", 0);
		}
		return mb;
	}
	
}
