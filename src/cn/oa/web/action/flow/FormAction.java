package cn.oa.web.action.flow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.view.ServerRedirectView;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.model.WorkModel;

import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.Strings;
import cn.oa.utils.Values;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
import cn.oa.web.view.FreemarkerView;

@IocBean(name = "flow.form")
@At(value = "/flow/form")
public class FormAction extends Action {

	public static Log log = Logs.getLog(FormAction.class);

	@GET
	@At
	public View all(HttpServletRequest req) {
		try {
			String processId = Https.getStr(req, "processId", R.CLEAN, R.REQUIRED, "流程定义ID");
			String orderId = Https.getStr(req, "orderId", R.CLEAN, "流程实例ID");
			String taskId = Https.getStr(req, "taskId", R.CLEAN, "流程任务ID");
			String type = Https.getStr(req, "type", R.CLEAN);
			type = Values.getStr(type, "");
			
			Process process = snaker.process().getProcessById(processId);
			Asserts.isNull(process, "流程定义");
			
			if (Strings.isNotBlank(process.getInstanceUrl())) {
				StringBuilder buff = new StringBuilder();
				buff.append(process.getInstanceUrl())
					.append("?processId=").append(processId)
					.append("&processName=").append(process.getName());
				if (Strings.isNotBlank(orderId))
					buff.append("&orderId=").append(orderId);
				if (Strings.isNotBlank(taskId))
					buff.append("&taskId=").append(taskId);
				return new ServerRedirectView(buff.toString());
			}
			
			Map<String, String> operators = new ConcurrentHashMap<String, String>();
			
			List<WorkModel> models = process.getModel().getWorkModels();
			String current = "";
			
			if (Strings.isNotBlank(orderId)) {
				Order order = snaker.query().getOrder(orderId);
				
				List<HistoryTask> tasks = snaker.query().getHistoryTasks(new QueryFilter().setOrderId(orderId));
				req.setAttribute("order", order);

				StringBuilder buff = new StringBuilder();
				for (HistoryTask task : tasks) {
					buff.append(task.getOperator()).append(",");
					req.setAttribute("variable_" + task.getTaskName(), task.getVariableMap());
				}
				
				// name mapping
				Integer[] userIds = Converts.array(buff.toString(), ",");
				if (!Asserts.isEmpty(userIds)) {
					List<User> users = dao.query(User.class, Cnd.where("userId", "in", userIds));
					for (User user : users) {
						operators.put(String.valueOf(user.getUserId()), user.getTrueName());
					}
				}
			}
			
			if (Strings.isNotBlank(taskId)) {
				Task task = snaker.query().getTask(taskId);
				req.setAttribute("task", task);
				current = task.getTaskName();
			} else {
				current = models.isEmpty() ? "" : models.get(0).getName();
			}
			
			if (!type.equalsIgnoreCase("send"))
				req.setAttribute("current", current);
			
			req.setAttribute("works", models);
			req.setAttribute("process", process);
			req.setAttribute("operators", operators);
			req.setAttribute("operator", Context.getTrueName());
		} catch (Exception e) {
			log.error("(Form:all) error: ", e);
		}
		
		
		return new FreemarkerView("flow/form/all");
	}
}
