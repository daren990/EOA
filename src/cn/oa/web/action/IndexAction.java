package cn.oa.web.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.model.Resource;
import cn.oa.model.User;
import cn.oa.utils.Values;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;
import cn.oa.web.filter.CtxFilter;
import cn.oa.web.filter.SessionFilter;
import cn.oa.web.view.FreemarkerView;

@IocBean
@Filters
public class IndexAction extends Action {
	
	private static final Log log = Logs.getLog(IndexAction.class);

	@Filters( { @By(type = SessionFilter.class), @By(type = CtxFilter.class) })
	@At("/")
	public View index(HttpServletRequest req) {
		log.debug("index(HttpServletRequest req)");
		String view = Https.getStr(req, "view", R.CLEAN, R.REGEX, "regex:^index|path|content$");
		view = Values.getStr(view, "index");
		
		User user = Context.getUser();
		if (user == null){
			return new FreemarkerView("404");
		}
		
		if (view.equals("path")) {
			Integer userId = user.getUserId();
			List<Resource> resources = resourceRepository.find(userId);
			req.setAttribute("menuNodes", Context.menuNodes(resources));
			System.out.println("\n\n\n\n\nsize : " + resources.size() + "\n" + resources + "\n\n\n\n\n");

		} else if (view.equals("content")) {
			String redirect = null;

			List<Resource> resources = resourceRepository.find(user.getUserId());
			Map<String, Resource> urlMap = Context.urlMap(resources);
			if (urlMap.containsKey("/edu/course/lesson/lesson/page")) redirect = "/edu/course/lesson/lesson/page";
			if (urlMap.containsKey("/coll/send/page")) redirect = "/coll/send/page";
			if (urlMap.containsKey("/coll/pending/page")) redirect = "/coll/pending/page";

			return new ServerRedirectView(redirect);
		}
		
		return new FreemarkerView(view);
	}
}
