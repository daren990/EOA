package cn.oa.web.action.addressBook;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;


import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.JobReport;
import cn.oa.model.JobTime;
import cn.oa.model.Leave;
import cn.oa.model.LeaveActor;
import cn.oa.model.Org;
import cn.oa.model.ReportModel;
import cn.oa.model.User;
import cn.oa.model.WorkDay;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
@IocBean(name = "addressBook.approveDayReport")
@At(value = "addressBook/approveDayReport")
public class ApproveDayReport extends Action{
	
	public static Log log = Logs.getLog(ApproveDayReport.class);
	public static final int PAGE_SIZE = 100000;
	@GET
	@At
	@Ok("ftl:addressBook/approveReport_day_page")
	public void page(HttpServletRequest req){
		pageUtil(req);		
	}
	@GET
	@At
	@Ok("ftl:addressBook/approveReport_day_wxpage")
	public void wxpage(HttpServletRequest req){
		pageUtil(req);		
	}
	public void pageUtil(HttpServletRequest req){

		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("w.type", "=", 2)
				.and("w.approve_actor", "=", Context.getUserId());
		Cnds.eq(cri, mb, "w.approve", "approve", approve);
		Cnds.gte(cri, mb, "w.start_date", "start_date", startStr);
		Cnds.lte(cri, mb, "w.end_date", "end_date", endStr);
		cri.getOrderBy().asc("w.approve");

		
		Page<JobReport> page = Webs.page(req);
		page = mapper.page(JobReport.class, page, "JobReprot.count", "JobReprot.index", cri);		
		req.setAttribute("page", page);
		/*List<JobReport> jReport = page.getResult();*/
		/*req.setAttribute("corps", corps);*/
		req.setAttribute("mb", mb);	
	}
	@GET
	@At
	@Ok("ftl:addressBook/approveReport_day_add")
	public void add(HttpServletRequest req){
		addUtil(req);
	}
	@GET
	@At
	@Ok("ftl:addressBook/approveReport_day_wxadd")
	public void wxadd(HttpServletRequest req){
		addUtil(req);
	}
	public void addUtil(HttpServletRequest req){
		CSRF.generate(req);
		Integer reportId = Https.getInt(req, "reportId", R.I);
		Integer type = Https.getInt(req, "type", R.I);
		JobReport report = null;
		if(reportId != null){
			report = mapper.fetch(JobReport.class, "JobReprot.query", Cnd.where("w.report_id", "=", reportId));
			report.setApprove(1);
			dao.update(report);
		}
		if(report == null){
			report = new JobReport();
		}
		req.setAttribute("report", report);
		req.setAttribute("type", type);
	}
		
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer reportId = null;
		JobReport report = null;
		try{
			
			CSRF.validate(req);
			reportId = Https.getInt(req, "reportId", R.I);			
			if(reportId != null){
				/*report = mapper.fetch(JobReport.class, "Report.query", Cnd.where("r.report_id", "=", reportId));*/
				report = dao.fetch(JobReport.class, reportId);
				report.setApprove(2);
				int istrue = dao.update(report);
				if(istrue==1){
					Code.ok(mb, "退回日报成功!");
				}else{
					Code.error(mb, "日报日报失败");
				}
			}else{														
				Code.error(mb, "没有找到该报告");
			}
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(ApproveDayReport:add) error: ", e);
		}
		return mb;
			
		}

}
