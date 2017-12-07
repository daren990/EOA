package cn.oa.web.action.flow.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.view.ServerRedirectView;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;

import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Code;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
import cn.oa.web.view.FreemarkerView;

@IocBean(name = "flow.form.custom")
@At(value = "/flow/form/custom")
public class CustomAction extends Action {
	
	public static Log log = Logs.getLog(CustomAction.class);
	
	@GET
	@At
	public View start(HttpServletRequest req, HttpServletResponse res) {
		try {
			String processId = Https.getStr(req, "processId", R.CLEAN, R.REQUIRED, "流程定义ID");
			String orderId = Https.getStr(req, "orderId", R.CLEAN, "流程实例ID");
			String taskId = Https.getStr(req, "taskId", R.CLEAN, "流程任务ID");
			
			if (Strings.isBlank(orderId)) {
				snaker.startInstanceById(processId, String.valueOf(Context.getUserId()), null);
				return new ServerRedirectView("/coll/pending/page");
			} else {
				Process process = snaker.process().getProcessById(processId);
				Asserts.isNull(process, "流程定义");
				Order order = snaker.query().getOrder(orderId);
				Asserts.isNull(order, "流程");
				Task task = snaker.query().getTask(taskId);
				Asserts.isNull(task, "流程任务");

				req.setAttribute("process", process);
				req.setAttribute("order", order);
				req.setAttribute("task", task);
			}
		} catch (Exception e) {
			log.error("(Custom:start) error: ", e);
		}
		
		return new FreemarkerView("flow/form/custom/1");
	}
	
	@POST
	@At("/1")
	@Ok("json")
	public Object _1(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String taskId = Https.getStr(req, "taskId", R.CLEAN, R.REQUIRED, "流程任务ID");
			snaker.executeTask(taskId, String.valueOf(Context.getUserId()), null);
			Code.ok(mb, "自定义节点成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			Code.error(mb, "自定义节点失败");
			log.error("(Custom:1) error: ", e);
		}
		
		return mb;
	}
	
}
