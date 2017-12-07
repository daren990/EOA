package cn.oa.web.action.addressBook;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.model.ReportModel;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
@IocBean(name = "addressBook.applyReport")
@At(value = "addressBook/applyReport")
public class ApplyReport extends Action{
	
	public static Log log = Logs.getLog(AddressBook.class);
	
	@GET
	@At
	@Ok("ftl:addressBook/applyReport")
	public void wxadd(HttpServletRequest req){
		CSRF.generate(req);
		Integer reportId = Https.getInt(req, "reportId", R.I);
		Integer type = Https.getInt(req, "type", R.I);
		ReportModel report = null;
		if(reportId != null){
			report = mapper.fetch(ReportModel.class, "Report.query", Cnd.where("r.report_id", "=", reportId));
		}
		if(report == null){
			report = new ReportModel();
		}
		req.setAttribute("report", report);
		req.setAttribute("type", type);
	}
		
	@POST
	@At
	@Ok("json")
	public Object wxadd(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer reportId = null;
		Integer type = null;
		Integer share = null;
		ReportModel report = null;
		try{
			
			CSRF.validate(req);
			reportId = Https.getInt(req, "reportId", R.I);
			type = Https.getInt(req, "type", R.I);
			share = Https.getInt(req, "share", R.I);
			String title = Https.getStr(req, "title", R.REQUIRED, R.CLEAN, "标题");
			String content = Https.getStr(req, "content", R.REQUIRED, "内容");
			
			DateTime now = new DateTime();
			if(reportId != null){
				report = mapper.fetch(ReportModel.class, "Report.query", Cnd.where("r.report_id", "=", reportId));
			}
			if(report == null)
				report = new ReportModel();
			
			report.setContent(content);
			report.setCreateTime(now.toDate());
			report.setShare(share);
			report.setTitle(title);
			report.setShare(share);
			report.setType(type);
			report.setUserId(Context.getUserId());
			
			if(reportId != null)
				dao.update(report);
			else
				dao.insert(report);
			Code.ok(mb, (report == null ? "新建" : "编辑") + "报告成功");

		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (report == null ? "新建" : "编辑") + "报告失败");
		}
		return mb;
			
		}
}
