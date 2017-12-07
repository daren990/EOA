package cn.oa.web.action.flow.task;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.Process;

import cn.oa.consts.Status;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "flow.task.send")
@At(value = "/flow/task/send")
public class SendAction extends Action {

	@GET
	@At
	@Ok("ftl:flow/task/send_page")
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
		
		Page<HistoryOrder> page = Webs.page(req);

		org.snaker.engine.access.Page<HistoryOrder> pages = Webs.fetch(page);
		QueryFilter filter = new QueryFilter();
		filter.setOperator(String.valueOf(Context.getUserId()));
		if (Strings.isNotBlank(startStr))
			filter.setCreateTimeStart(startStr);
		if (Strings.isNotBlank(endStr))
			filter.setCreateTimeStart(endStr);
		snaker.query().getHistoryOrders(pages, filter);
		Webs.copy(pages, page);
		
		List<Process> processes = snaker.process().getProcesss(new QueryFilter().setState(Status.ENABLED));
		
		req.setAttribute("page", page);
		req.setAttribute("processes", processes);
		req.setAttribute("mb", mb);
	}
}