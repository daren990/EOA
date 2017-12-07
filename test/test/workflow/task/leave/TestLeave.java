package test.workflow.task.leave;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Task;
import org.snaker.engine.helper.StreamHelper;

import test.Setup;
import cn.oa.utils.Strings;

public class TestLeave extends Setup {
	
	public void apply() {
		String path = Strings.after(getClass().getResource("").getPath(), "/WEB-INF/classes/");
		String processId = snaker.process().deploy(StreamHelper.getStreamFromClasspath(path + "leave.snaker"));
		
		Integer day = 3;
		String reason = "请假";
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("day", day);
		args.put("reason", reason);
		args.put("apply.operator", "员工");
		args.put("approveManager.operator", "部门经理");
		args.put("approveBoss.operator", "总经理");
		
		Order order = snaker.startInstanceById(processId, "员工", args);
		
		List<Task> tasks = snaker.query().getActiveTasks(new QueryFilter().setOrderId(order.getId()));
		for (Task task : tasks) {
			snaker.executeTask(task.getId(), "员工", args);
		}
	}
	
	public void manager() {
		String taskId = "27489efcaf8b43b7a4130bbb6e4625b9";
		String result = "1";
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("approveManager.suggest", "同意");
		args.put("approveManager.result", result);
		if (result.equals("-1"))
			snaker.executeAndJumpTask(taskId, "部门经理", args, null);
		else
			snaker.executeTask(taskId, "部门经理", args);
	}
	
	@Test
	public void boss() {
		String taskId = "8628282d93d34235998389713fcf778d";
		String result = "1";
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("approveBoss.suggest", "同意");
		args.put("approveBoss.result", result);
		if (result.equals("-1"))
			snaker.executeAndJumpTask(taskId, "总经理", args, null);
		else
			snaker.executeTask(taskId, "总经理", args);
	}
	
}
