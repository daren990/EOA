package cn.oa.app.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.nutz.ioc.loader.annotation.IocBean;
import org.snaker.engine.IManagerService;
import org.snaker.engine.IOrderService;
import org.snaker.engine.IProcessService;
import org.snaker.engine.IQueryService;
import org.snaker.engine.ITaskService;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.cfg.Configuration;
import org.snaker.engine.core.Execution;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Task;
import org.snaker.engine.model.TaskModel;
import org.snaker.engine.model.TaskModel.TaskType;

@IocBean
public class Snaker implements SnakerEngine {

	private SnakerEngine engine;

	public Snaker(DataSource dataSource) {
		engine = new Configuration().initAccessDBObject(dataSource).buildSnakerEngine();
	}

	@Override
	public SnakerEngine configure(Configuration config) {
		return engine.configure(config);
	}

	@Override
	public IManagerService manager() {
		return engine.manager();
	}

	@Override
	public IProcessService process() {
		return engine.process();
	}

	@Override
	public IQueryService query() {
		return engine.query();
	}

	@Override
	public IOrderService order() {
		return engine.order();
	}

	@Override
	public ITaskService task() {
		return engine.task();
	}

	@Override
	public Order startInstanceById(String id) {
		return engine.startInstanceById(id);
	}

	@Override
	public Order startInstanceById(String id, String operator) {
		return engine.startInstanceById(id, operator);
	}

	@Override
	public Order startInstanceById(String id, String operator, Map<String, Object> args) {
		return engine.startInstanceById(id, operator, args);
	}

	@Override
	public Order startInstanceByName(String name) {
		return engine.startInstanceByName(name);
	}

	@Override
	public Order startInstanceByName(String name, Integer version) {
		return engine.startInstanceByName(name, version);
	}

	@Override
	public Order startInstanceByName(String name, Integer version, String operator) {
		return engine.startInstanceByName(name, version, operator);
	}

	@Override
	public Order startInstanceByName(String name, Integer version, String operator, Map<String, Object> args) {
		return engine.startInstanceByName(name, version, operator, args);
	}

	@Override
	public Order startInstanceByExecution(Execution execution) {
		return engine.startInstanceByExecution(execution);
	}
	
	@Override
	public List<Task> createFreeTask(String orderId, String operator, Map<String, Object> args, TaskModel model) {
		return engine.createFreeTask(orderId, operator, args, model);
	}

	@Override
	public List<Task> executeTask(String taskId) {
		return engine.executeTask(taskId);
	}

	@Override
	public List<Task> executeTask(String taskId, String operator) {
		return engine.executeTask(taskId, operator);
	}

	@Override
	public List<Task> executeTask(String taskId, String operator, Map<String, Object> args) {
		return engine.executeTask(taskId, operator, args);
	}

	@Override
	public List<Task> executeAndJumpTask(String taskId, String operator, Map<String, Object> args, String nodeName) {
		return engine.executeAndJumpTask(taskId, operator, args, nodeName);
	}
	
	public List<Task> startAndExecute(String processId, String operator, Map<String, Object> args) {
		Order order = engine.startInstanceById(processId, operator, args);
		List<Task> tasks = engine.query().getActiveTasks(new QueryFilter().setOrderId(order.getId()));
		List<Task> newTasks = new ArrayList<Task>();
		if (tasks != null && tasks.size() > 0) {
			Task task = tasks.get(0);
			newTasks.addAll(engine.executeTask(task.getId(), operator, args));
		}
		return newTasks;
	}
	
	public List<Task> transfer(String taskId, String operator, String... actors) {
		List<Task> tasks = engine.task().createNewTask(taskId, TaskType.Major.ordinal(), actors);
		engine.task().complete(taskId, operator);
		return tasks;
	}
}
