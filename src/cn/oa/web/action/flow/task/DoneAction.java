package cn.oa.web.action.flow.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.WorkItem;

import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;


@IocBean(name = "flow.task.done")
@At(value = "/flow/task/done")
public class DoneAction extends Action {

	@GET
	@At
	@Ok("ftl:flow/task/done_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/flow/task/actors", token);
		
		String subject = Https.getStr(req, "subject", R.CLEAN);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		
		MapBean mb = new MapBean();
		Webs.put(mb, "subject", subject);
		Webs.put(mb, "startTime", startStr);
		Webs.put(mb, "endTime", endStr);
		
		Page<WorkItem> page = Webs.page(req);

		org.snaker.engine.access.Page<WorkItem> pages = Webs.fetch(page);
		QueryFilter filter = new QueryFilter();
		filter.setOperator(String.valueOf(Context.getUserId()));
		if (Strings.isNotBlank(startStr))
			filter.setCreateTimeStart(startStr);
		if (Strings.isNotBlank(endStr))
			filter.setCreateTimeStart(endStr);
		
		snaker.query().getHistoryWorkItems(pages, filter);
		Webs.copy(pages, page);
		
		StringBuilder buff = new StringBuilder();
		for (WorkItem work : page.getResult()) {
			buff.append(work.getCreator()).append(",");
		}

		Map<String, String> userMap = new ConcurrentHashMap<String, String>();
		Integer[] userIds = Converts.array(buff.toString(), ",");
		if (!Asserts.isEmpty(userIds)) {
			List<User> users = dao.query(User.class, Cnd.where("userId", "in", userIds));
			for (User user : users) {
				userMap.put(String.valueOf(user.getUserId()), user.getTrueName());
			}
		}
		
		req.setAttribute("page", page);
		req.setAttribute("userMap", userMap);
		req.setAttribute("mb", mb);
	}
}