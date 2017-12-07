package cn.oa.web.action.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryTask;

import cn.oa.model.Actor;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Code;
import cn.oa.web.action.Action;


@IocBean(name = "flow.task")
@At(value = "/flow/task")
public class TaskAction extends Action {

	public static Log log = Logs.getLog(TaskAction.class);
	
	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String orderId = Https.getStr(req, "orderId", R.CLEAN, R.REQUIRED, "流程实例ID");
			List<HistoryTask> tasks = snaker.query().getHistoryTasks(new QueryFilter().setOrderId(orderId).orderBy("finish_Time").order("asc"));
			
			StringBuilder buff = new StringBuilder();
			for (HistoryTask task : tasks) {
				buff.append(task.getOperator()).append(",");
			}
			
			List<Actor> actors = new ArrayList<Actor>();
			Integer[] userIds = Converts.array(buff.toString(), ",");
			if (!Asserts.isEmpty(userIds)) {
				List<User> users = dao.query(User.class, Cnd.where("userId", "in", userIds));
				Map<String, String> userMap = new ConcurrentHashMap<String, String>();
				for (User user : users) {
					userMap.put(String.valueOf(user.getUserId()), user.getTrueName());
				}
				for (HistoryTask task : tasks) {
					actors.add(new Actor(userMap.get(task.getOperator()), task.getDisplayName(), task.getFinishTime()));
				}
			}
			
			mb.put("actors", actors);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Task:actors) error: ", e);
		}

		return mb;
	}
}
