package cn.oa.web.filter;

import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.web.Context;

public class SessionFilter implements ActionFilter {

	@Override
	public View match(ActionContext ctx) {
		if (Context.getUser() == null) {
			ctx.getResponse().setStatus(403);
			return new ServerRedirectView("/login");
		}
		return null;
	}
}
