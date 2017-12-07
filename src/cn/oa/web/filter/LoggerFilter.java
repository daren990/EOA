package cn.oa.web.filter;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.nutz.dao.Dao;
import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;

import cn.oa.consts.Sessions;
import cn.oa.model.Logger;
import cn.oa.model.Resource;
import cn.oa.model.User;
import cn.oa.repository.ResourceRepository;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;

public class LoggerFilter implements ActionFilter {

	private static final Log log = Logs.getLog(LoggerFilter.class);
	
	@Override
	public View match(ActionContext ctx) {
		Ioc ioc = ctx.getIoc();
		ResourceRepository resourceRepository = ioc.get(ResourceRepository.class);
		Dao dao = ioc.get(Dao.class);
		
		HttpServletRequest req = ctx.getRequest();
		if (req.getMethod().equalsIgnoreCase("GET")) {
			return null;
		}

		User user = (User) req.getSession().getAttribute(Sessions.USER);
		
		if (user == null) {
			return null;
		}
		
		DateTime now = new DateTime();
		
		List<Resource> resources = resourceRepository.find(user.getUserId());
		Map<String, Resource> urlMap = Context.urlMap(resources);
		String path = ctx.getPath();
		
		Logger logger = new Logger();
		logger.setUserId(user.getUserId());
		logger.setIp(Webs.ip(req));
		logger.setClient(Webs.browser(req));
		logger.setUrl(path);
		
		if (urlMap.containsKey(path)) {
			logger.setContent(urlMap.get(path).getResourceName());
		}
		logger.setModifyTime(now.toDate());
		
		try {
			dao.insert(logger);
		} catch (Exception e) {
			log.error("(Logger:match) error: ", e);
		}
		
		return null;
	}
}
