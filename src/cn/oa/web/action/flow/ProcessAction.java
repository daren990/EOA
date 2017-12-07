package cn.oa.web.action.flow;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadAdaptor;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.helper.StreamHelper;
import org.snaker.engine.model.ProcessModel;

import cn.oa.consts.Status;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.helper.Jsons;
import cn.oa.utils.lang.Files;
import cn.oa.utils.lang.Uploads;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/flow/process")
public class ProcessAction extends Action {

	public static Log log = Logs.getLog(ProcessAction.class);
	
	@GET
	@At
	@Ok("ftl:flow/process_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/flow/precess/delete", token);
		
		Page<Process> page = Webs.page(req);

		org.snaker.engine.access.Page<Process> pages = Webs.fetch(page);

		QueryFilter filter = new QueryFilter();
		filter.setState(Status.ENABLED);
		filter.orderBy("create_Time").order("desc");
		snaker.process().getProcesss(pages, filter);
		Webs.copy(pages, page);

		req.setAttribute("page", page);
	}
	
	@GET
	@At
	@Ok("ftl:flow/process_edit")
	public void edit(HttpServletRequest req) {
		CSRF.generate(req);
		
		String processId = Https.getStr(req, "processId", R.CLEAN);
		Process process = null;
		String content = null;
		
		if (Strings.isNotBlank(processId))
			process = snaker.process().getProcessById(processId);
		if (process == null)
			process = new Process();
		if (process.getDBContent() != null)
			try {
				content = new String(process.getDBContent(), "UTF-8");
			} catch (UnsupportedEncodingException e) {}
		
		req.setAttribute("process", process);
		req.setAttribute("content", content);
	}

	@POST
	@At
	@Ok("json")
	@AdaptBy(type = UploadAdaptor.class, args = { "${app.root}/res/tmp" })
	public Object edit(HttpServletRequest req, HttpServletResponse res, @Param("..") NutMap map) {
		MapBean mb = new MapBean();
		String processId = null;
		InputStream stream = null;
		try {
			String token = Validator.getStr(map.get("token"), R.CLEAN, R.REQUIRED, R.RANGE, "{1,32}");
			CSRF.validate(req, token);
			
			processId = Validator.getStr(map.get("processId"), R.CLEAN, "流程定义ID");
			Uploads.required(map.get("file"));
			Uploads.max(map.get("file"), 1);
			Uploads.suff(map.get("file"), "txt,snaker");
			
			TempFile tmp = (TempFile) map.get("file");
			File file = tmp.getFile();
			
			stream = StreamHelper.getStreamFromFile(file);
			if (Strings.isNotBlank(processId)) {
				snaker.process().redeploy(processId, stream);
			} else {
				snaker.process().deploy(stream);
			}
			
			Code.ok(mb, "流程定义部署成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Process:edit) error: ", e);
			Code.error(mb, "流程定义部署失败");
		} finally {
			Files.close(stream);
		}
		
		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:flow/process_design")
	public void design(HttpServletRequest req) {
		CSRF.generate(req);
		
		String processId = Https.getStr(req, "processId", R.CLEAN);
		Process process = null;
		
		if (Strings.isNotBlank(processId))
			process = snaker.process().getProcessById(processId);
		if (process == null)
			process = new Process();
		
		if (process.getModel() != null) {
			String content = Jsons.getModel(process.getModel());
			req.setAttribute("content", content);
		}
		
		req.setAttribute("process", process);
	}

	@POST
	@At
	@Ok("json")
	public Object design(HttpServletRequest req, HttpServletResponse res, @Param("..") NutMap map) {
		MapBean mb = new MapBean();
		String processId = null;
		InputStream stream = null;
		try {
			processId = Https.getStr(req, "processId", R.CLEAN);
			String model = Https.getStr(req, "model", R.REQUIRED, "流程定义");
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + Jsons.convertXml(model);
			stream = StreamHelper.getStreamFromString(xml);
			if (Strings.isNotBlank(processId)) {
				snaker.process().redeploy(processId, stream);
			} else {
				snaker.process().deploy(stream);
			}
			
			Code.ok(mb, (Strings.isNotBlank(processId) ? "编辑" : "新建") + "流程定义设计成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Process:design) error: ", e);
			Code.error(mb, (Strings.isNotBlank(processId) ? "编辑" : "新建") + "流程定义设计失败");
		} finally {
			Files.close(stream);
		}
		
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object delete(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[a-zA-Z0-9,]+$:不是合法值", "checkedIds");
			String[] arr = Strings.splitIgnoreBlank(checkedIds, ",");

			if (arr != null && arr.length == 1) {
				snaker.process().undeploy(arr[0]);
			}
			
			Code.ok(mb, "删除流程定义成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Process:delete) error: ", e);
			Code.error(mb, "删除流程定义失败");
		}
		
		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:flow/process_view")
	public void view(HttpServletRequest req) {
		String orderId = Https.getStr(req, "orderId", R.CLEAN, R.REQUIRED, "流程实例ID");
		HistoryOrder order = snaker.query().getHistOrder(orderId);
		
		req.setAttribute("order", order);
	}
	
	@POST
	@At
	@Ok("json")
	public Object view(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String orderId = Https.getStr(req, "orderId", R.CLEAN, R.REQUIRED, "流程实例ID");
			HistoryOrder order = snaker.query().getHistOrder(orderId);
			List<Task> tasks = snaker.query().getActiveTasks(new QueryFilter().setOrderId(orderId));
			Process process = snaker.process().getProcessById(order.getProcessId());
			Asserts.isNull(process, "流程定义不存在");
			ProcessModel model = process.getModel();
			if (model != null)
				mb.put("process", Jsons.getModel(model));
			if (!Asserts.isEmpty(tasks))
				mb.put("active", Jsons.getActive(tasks));
		} catch (Exception e) {
			log.error("(Process:view) error: ", e);
		}
		
		return mb;
	}
}
