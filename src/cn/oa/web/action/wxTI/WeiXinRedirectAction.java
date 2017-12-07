package cn.oa.web.action.wxTI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.utils.web.PathUtil;
import cn.oa.web.action.Action;
import cn.oa.web.wx.Config;
import cn.oa.web.wx.InterfacePath;

@Filters
@IocBean(name = "wx.redirect")
@At(value = "/wx/redirect")
public class WeiXinRedirectAction extends Action {

	@GET
	@At
	public void redirectWechat(HttpServletRequest req,HttpServletResponse response,String redirectUrl,String org_id)
			throws Exception {
		Config c = wxClientService.initOrgConfig(org_id);
		if(redirectUrl.indexOf("?") > 0){
			redirectUrl = redirectUrl+"&org_id="+org_id;
		}else{
			redirectUrl = redirectUrl+"?org_id="+org_id;
		}
		String url = String.format(InterfacePath.AUTH_URL, c.getAPPID(), redirectUrl,"snsapi_base");
		response.sendRedirect(url);
	}
}
