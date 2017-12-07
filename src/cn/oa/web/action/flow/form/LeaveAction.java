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

import cn.oa.consts.Id;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Code;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "flow.form.leave")
@At(value = "/flow/form/leave")
public class LeaveAction extends Action {
	
	public static Log log = Logs.getLog(LeaveAction.class);
	
	@POST
	@At("/1")
	@Ok("json")
	public Object _1(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String processId = Https.getStr(req, "processId", R.CLEAN, R.REQUIRED, "流程定义ID");
			Integer day = Https.getInt(req, "day", R.REQUIRED, R.I, "请假天数");
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, "请假原因");
			
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("day", day);
			args.put("reason", reason);
			args.put("apply.operator", Context.getUserId());
			args.put("approveManager.operator", Context.getUser().getManagerId());
			args.put("approveBoss.operator", Id.BOSS);
			
			snaker.startAndExecute(processId, String.valueOf(Context.getUserId()), args);
			
			Code.ok(mb, "请假申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			Code.error(mb, "请假申请失败");
			log.error("(Leave:1) error: ", e);
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
			String result = Https.getStr(req, "approveManager.result", R.CLEAN, R.REQUIRED, "上级经理审批结果");
			String suggest = Https.getStr(req, "approveManager.suggest", R.CLEAN, "上级经理审批意见");
			
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("approveManager.result", result);
			args.put("approveManager.suggest", suggest);
			
			if (result.equals("-1")) {
				snaker.executeAndJumpTask(taskId, String.valueOf(Context.getUserId()), args, null);
			} else {
				snaker.executeTask(taskId, String.valueOf(Context.getUserId()), args);
			}
			
			Code.ok(mb, "请假审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			Code.error(mb, "请假审批失败");
			log.error("(Leave:2) error: ", e);
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
			String result = Https.getStr(req, "approveBoss.result", R.CLEAN, R.REQUIRED, "总经理审批结果");
			String suggest = Https.getStr(req, "approveBoss.suggest", R.CLEAN, "总经理审批意见");
			
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("approveBoss.result", result);
			args.put("approveBoss.suggest", suggest);
			
			if (result.equals("-1")) {
				snaker.executeAndJumpTask(taskId, String.valueOf(Context.getUserId()), args, null);
			} else {
				snaker.executeTask(taskId, String.valueOf(Context.getUserId()), args);
			}
			
			Code.ok(mb, "请假审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			Code.error(mb, "请假审批失败");
			log.error("(Leave:3) error: ", e);
		}
		
		return mb;
	} 
}
