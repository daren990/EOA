package cn.oa.web.filter;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;

import cn.oa.web.Context;
import cn.oa.web.Views;

public class CtxFilter implements ActionFilter {

	@Override
	public View match(ActionContext ctx) {
		Ioc ioc = ctx.getIoc();
		HttpServletRequest req = ctx.getRequest();
		req.setAttribute("ctx", "");
		req.setAttribute("views", ioc.get(Views.class));
		req.setAttribute("now", new Date());
		req.setAttribute("currentUser", Context.getUser());
		req.setAttribute("roleNames", Context.getRoles());
		return null;
	}
}
