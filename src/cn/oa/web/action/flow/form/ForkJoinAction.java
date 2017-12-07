package cn.oa.web.action.flow.form;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Code;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "flow.form.forkjoin")
@At(value = "/flow/form/forkjoin")
public class ForkJoinAction extends Action {
	
	public static Log log = Logs.getLog(ForkJoinAction.class);
	
	@POST
	@At("/1")
	@Ok("json")
	public Object _1(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String processId = Https.getStr(req, "processId", R.CLEAN, R.REQUIRED, "流程定义ID");
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("task1.operator", Context.getUserId());
			args.put("task2.operator", Context.getUserId());
			args.put("task3.operator", Context.getUserId());
			args.put("task4.operator", Context.getUserId());
			
			snaker.startAndExecute(processId, String.valueOf(Context.getUserId()), args);
			
			Code.ok(mb, "forkjoin成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			Code.error(mb, "forkjoin失败");
			log.error("(ForkJoin:1) error: ", e);
		}
		
		return mb;
	}
	
	@POST
	@At("/2")
	@Ok("json")
	public Object _2(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String taskId = Https.getStr(req, "taskId", R.CLEAN, R.REQUIRED, "流程任务ID");
			snaker.executeTask(taskId, String.valueOf(Context.getUserId()), null);
			Code.ok(mb, "forkjoin成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			Code.error(mb, "forkjoin失败");
			log.error("(ForkJoin:2) error: ", e);
		}
		
		return mb;
	}
	
	@POST
	@At("/3")
	@Ok("json")
	public Object _3(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String taskId = Https.getStr(req, "taskId", R.CLEAN, R.REQUIRED, "流程任务ID");
			snaker.executeTask(taskId, String.valueOf(Context.getUserId()), null);
			Code.ok(mb, "forkjoin成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			Code.error(mb, "forkjoin失败");
			log.error("(ForkJoin:3) error: ", e);
		}
		
		return mb;
	}
	
	@POST
	@At("/4")
	@Ok("json")
	public Object _4(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String taskId = Https.getStr(req, "taskId", R.CLEAN, R.REQUIRED, "流程任务ID");
			snaker.executeTask(taskId, String.valueOf(Context.getUserId()), null);
			Code.ok(mb, "forkjoin成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			Code.error(mb, "forkjoin失败");
			log.error("(ForkJoin:4) error: ", e);
		}
		
		return mb;
	}
	
}
