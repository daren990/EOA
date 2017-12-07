package cn.oa.web.filter;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.consts.Roles;
import cn.oa.consts.Sessions;
import cn.oa.model.Resource;
import cn.oa.model.User;
import cn.oa.repository.ResourceRepository;
import cn.oa.utils.Asserts;
import cn.oa.utils.Strings;
import cn.oa.web.Context;

public class AuthorityFilter implements ActionFilter {

	private static final Log log = Logs.getLog(AuthorityFilter.class);

	@Override
	public View match(ActionContext ctx) {
		Ioc ioc = ctx.getIoc();
		ResourceRepository resourceRepository = ioc.get(ResourceRepository.class);

		HttpServletRequest req = ctx.getRequest();
		
		User user = (User) req.getSession().getAttribute(Sessions.USER);
		
		if (user == null) {
			ctx.getResponse().setStatus(403);
			return new ServerRedirectView("/login");
		}
		
		String path = ctx.getPath();
		if (Strings.isBlank(path)) {
			return null;
		}

		List<Resource> resources = resourceRepository.find(user.getUserId());
		Map<String, Resource> urlMap = Context.urlMap(resources);

		if (Asserts.hasAny(Roles.ADM.getName(), Context.getRoles())) {
			return null;
		}

		if (Strings.startsWith(path, "/flow/form")) {
			return null;
		}
		
		if (urlMap.containsKey(path)) {
			if (log.isInfoEnabled()) {
				log.infof("(%s:%s) 操作: [%s]", req.getRemoteHost(), user.getUsername(), path);
			}
			return null;
		} else {
			if (log.isWarnEnabled()) {
				log.warnf("(%s:%s) 没有权限操作: [%s]", req.getRemoteHost(), user.getUsername(), path);
			}
			ctx.getResponse().setStatus(403);
			return new ServerRedirectView("/403");
		}
	}
}
