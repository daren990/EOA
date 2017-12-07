package cn.oa.web.action.wxTI;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.model.ShopMember;
import cn.oa.model.ShopWechatrelation;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.PathUtil;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.view.FreemarkerView;
import cn.oa.web.wx.Config;


@Filters
@IocBean(name = "wx.member")
@At(value="/wx/member")
public class WeiXinMemberAction extends Action{

	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View index(HttpServletRequest req,String org_id) throws Exception {
		StringBuilder QR_code = new StringBuilder("");
		Config c = wxClientService.initOrgConfig(org_id,QR_code);
		req.setAttribute("QR_code", QR_code.toString());
		req.setAttribute("org_id", org_id);
		return new FreemarkerView("wxTI/member_index");
	}
	
	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View join_view(HttpServletRequest req,String org_id) throws Exception {
		ShopWechatrelation sw = wxClientService.getByOrgIdAndOpenId(org_id, Webs.getOpenidInSession(req, org_id));
		ShopMember sm = wxClientService.getByClientId(sw.getClientId().toString());
		StringBuilder QR_code = new StringBuilder();
		Integer level = 3;
		if(sm == null){
			wxClientService.insertMember(sw.getClientId(),req,QR_code);
			req.setAttribute("type", 1);
		}else{
			level = sm.getLevel();
			QR_code.append(sm.getQrCode());
			req.setAttribute("type", 0);
		}
		req.setAttribute("level", level);
		req.setAttribute("QR_code", QR_code);
		return new FreemarkerView("wxTI/member_join");
	}
	
	@GET
	@At
	@Ok("json")
	public Object getMember(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		try {
			Integer clientId = Https.getInt(req, "clientId");
			ShopMember sm = wxClientService.getByClientId(clientId.toString());
			mb.put("memberID", sm.getId());
			Code.ok(mb, "");
		} catch (Exception e) {
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
}
