
package cn.oa.web.action.wxTI;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Sessions;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopCompanyWechatImg;
import cn.oa.utils.SignUtil;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.view.FreemarkerView;

@Filters
@IocBean(name = "wx.index")
@At(value = "/wx/index")
public class WeiXinIndexAction extends Action {
	
	private static final Log log = Logs.getLog(WeiXinIndexAction.class);
	
	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View indexPage(HttpServletRequest req, String org_id,String code,String openid) throws Exception {
		
		//轮播图
		List<ShopCompanyWechatImg> nodes = dao.query(ShopCompanyWechatImg.class, Cnd.where("org_id", "=",org_id).and("state","=","1").asc("id"));
		ShopCompanyWechatImg first = new ShopCompanyWechatImg();
		ShopCompanyWechatImg last = new ShopCompanyWechatImg();

		if(nodes != null && nodes.size() != 0)
		{
			first = nodes.get(0);
			last = nodes.get(nodes.size()-1);
		}
		req.setAttribute("nodes", nodes);
		req.setAttribute("first", first);
		req.setAttribute("last", last);
		ShopClient sc = Webs.getSCInSession(req);
		String divMark = Webs.getDivMark(req);
		req.setAttribute("org_id", org_id);
		req.setAttribute("sc", sc);
		req.setAttribute("divMark", divMark);
		return new FreemarkerView("wxTI/wechat_indexPage");
	}

	
	@GET
	@At
	public void divMark(HttpServletRequest req, String div){
		req.getSession().setAttribute(Sessions.divMark, div);
	}

	
	
	
	/**
	 * 接口验证,总入口
	 * 保留此方法，不一定用到
	 * @param out
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@GET
	@POST
	@At
	public void index(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 微信加密签名  
	        String signature = request.getParameter("signature");  
	        // 时间戳  
	        String timestamp = request.getParameter("timestamp");  
	        // 随机数  
	        String nonce = request.getParameter("nonce");  
	        // 随机字符串  
	        String echostr = request.getParameter("echostr");  
			PrintWriter out = response.getWriter();
			if (null != signature && null != timestamp && null != nonce && null != echostr) {/* 接口验证 */
				
				if(SignUtil.checkSignature(signature, timestamp, nonce)) {  
					out.print(echostr);  
			    }
				out.flush();
				out.close();
			} else {/* 消息处理 */
				response.reset();
			}
		} catch (Exception e) {
		}
	}

}
