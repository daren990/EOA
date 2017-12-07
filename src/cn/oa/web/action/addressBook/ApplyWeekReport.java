package cn.oa.web.action.addressBook;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import cn.oa.model.JobReply;
import cn.oa.model.JobReport;
import cn.oa.model.JobReportShare;
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
@IocBean(name = "addressBook.ApplyWeekReport")
@At(value = "addressBook/ApplyWeekReport")
public class ApplyWeekReport extends Action{
	
	public static Log log = Logs.getLog(ApplyWeekReport.class);
	public static final int PAGE_SIZE = 100000;
	private Integer reId = null;
	@GET
	@At
	@Ok("ftl:addressBook/applyReport_week_page")
	public void page(HttpServletRequest req){
			pageUtil(req);
	}	
	@GET
	@At
	@Ok("ftl:addressBook/applyReport_week_wxpage")
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
				.and("w.type", "=", 1)
				.and("w.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "w.approve", "approve", approve);
		Cnds.gte(cri, mb, "w.start_date", "start_date", startStr);
		Cnds.lte(cri, mb, "w.end_date", "start_date", endStr);
		cri.getOrderBy().desc("w.create_time");

		
		Page<JobReport> page = Webs.page(req);
		page = mapper.page(JobReport.class, page, "JobReprot.count", "JobReprot.index", cri);		
		req.setAttribute("page", page);
		/*List<JobReport> jReport = page.getResult();*/
		/*req.setAttribute("corps", corps);*/
		req.setAttribute("mb", mb);	
	}
	
	public ReportBean getTimeAxis(HttpServletRequest req,ReportBean reportBean){
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);		
		String startStr = Https.getStr(req, "startTime");
		req.setAttribute("startStr", startStr);	
		String endStr = null;
		if(startStr!=null){
		DateTime start = Calendars.parse(startStr + "-01", Calendars.DATE);
		endStr = start.plusMonths(1).plusDays(-1).toString("yyyy-MM-dd");
		startStr += "-01"; 
		}
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("w.type", "=", 1)
				.and("w.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "w.approve", "approve", approve);
		Cnds.gte(cri, mb, "w.start_date", "start_date", startStr);
		Cnds.lte(cri, mb, "w.end_date", "end_date", endStr);
		cri.getOrderBy().desc("w.start_date");

		
		Page<JobReport> page = Webs.page(req);
		page.setPageSize(31);
		page = mapper.page(JobReport.class, page, "JobReprot.count", "JobReprot.index", cri);
		List<String> yearMonthList = new ArrayList<String>();
		String i = "";
		for(JobReport j:page.getResult()){
			long count = mapper.count("Reply.count", Cnd.where("report_id", "=", j.getReportId()));
			j.setCount(count);
			DateTime dateTime = Calendars.parse(j.getStartDate(), Calendars.DATE);
			String yearMonth = dateTime.toString("yyyy-MM");
			j.setYearMonth(yearMonth);
			if(!i.equals(yearMonth)){
				yearMonthList.add(yearMonth);
				i=yearMonth;
			}
		}
		req.setAttribute("yearMonthList", yearMonthList);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);	
		reportBean.setYearMonthList(yearMonthList);
		reportBean.setPage(page);
		return reportBean;
	}
	@GET
	@At
	@Ok("ftl:addressBook/applyReport_week_add")
	public void add(HttpServletRequest req){
		ReportBean reportBean = new ReportBean();
		reportBean = getTimeAxis(req,reportBean);
		reportBean = addUtil(req,reportBean);
	}
	
	
	@POST
	@At
	@Ok("json")
	public Object search(HttpServletRequest req, HttpServletResponse res) {
		ReportBean reportBean = new ReportBean();
		getTimeAxis(req,reportBean);
		addUtil(req,reportBean);
		return reportBean;
	}
	
	@GET
	@At
	@Ok("ftl:addressBook/applyReport_week_wxadd")
	public void wxadd(HttpServletRequest req){
		addUtil(req);
	}
	
	public ReportBean addUtil(HttpServletRequest req,ReportBean reportBean){
		CSRF.generate(req);
		Integer reportId = Https.getInt(req, "reportId", R.I);
		Integer type = Https.getInt(req, "type", R.I);
		JobReport report = null;
		List<JobReply> jobReply = null;
		if(reportId != null){
			report = mapper.fetch(JobReport.class, "JobReprot.query", Cnd.where("w.report_id", "=", reportId).and("w.user_id", "=", Context.getUserId()));
			/*jobReply = mapper.query(JobReply.class, "Share.reply", Cnd.where("r.report_id", "=", reportId));*/
			if(report==null)throw new Errors("你无法打开其他人的日报");
			Criteria rcri = Cnd.cri();
			rcri.where()
					.and("r.report_id", "=", reportId);
			rcri.getOrderBy().desc("r.reply_id");
			jobReply = mapper.query(JobReply.class, "Share.reply", rcri);
			for(JobReply j:jobReply){
				j.setReportTime(j.getReportTime().substring(0, 19));
			}
		}
		if(report == null){
			report = new JobReport();
		}
		User user =  mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", Context.getUserId()));
		if(user.getManagerId()!=0){
			req.setAttribute("managerId", user.getManagerId());
			req.setAttribute("manager", user.getManagerName());
		}else{
			req.setAttribute("managerId", "");
			req.setAttribute("manager", "你没有上级审批人员");
		}
		List<User> sharePeople = null;
		sharePeople = dao.query(User.class,Cnd.where("status","=",1).and("user_id", "<>", Context.getUserId()).and("user_id", "<>", 45));
		
		List<JobReportShare> sharePage = null;
		if(reportId!=null){
		sharePage = mapper.query(JobReportShare.class, "Share.join", Cnd.where("sreport_id", "=", reportId));
		}
		req.setAttribute("markId", reportId);
		req.setAttribute("sharePage", sharePage);
		req.setAttribute("jobReply", jobReply);
		req.setAttribute("sharePeople", sharePeople);
		req.setAttribute("report", report);
		req.setAttribute("type", type);
		reportBean.setReportId(reportId);
		reportBean.setSharePage(sharePage);
		reportBean.setJobReply(jobReply);
		reportBean.setSharePeople(sharePeople);
		reportBean.setReport(report);
		reportBean.setType(type);
		return reportBean;
	}
	
	public void addUtil(HttpServletRequest req){
		CSRF.generate(req);
		Integer reportId = Https.getInt(req, "reportId", R.I);
		Integer type = Https.getInt(req, "type", R.I);
		JobReport report = null;
		List<JobReply> jobReply = null;
		if(reportId != null){
			report = mapper.fetch(JobReport.class, "JobReprot.query", Cnd.where("w.report_id", "=", reportId).and("w.user_id", "=", Context.getUserId()));
			/*jobReply = mapper.query(JobReply.class, "Share.reply", Cnd.where("r.report_id", "=", reportId));*/
			if(report==null)throw new Errors("你无法打开其他人的日报");
			Criteria rcri = Cnd.cri();
			rcri.where()
					.and("r.report_id", "=", reportId);
			rcri.getOrderBy().desc("r.reply_id");
			jobReply = mapper.query(JobReply.class, "Share.reply", rcri);
		}
		if(report == null){
			report = new JobReport();
		}
		User user =  mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", Context.getUserId()));
		if(user.getManagerId()!=0){
			req.setAttribute("managerId", user.getManagerId());
			req.setAttribute("manager", user.getManagerName());
		}else{
			req.setAttribute("managerId", "");
			req.setAttribute("manager", "你没有上级审批人员");
		}
		List<User> sharePeople = null;
		sharePeople = dao.query(User.class,Cnd.where("status","=",1).and("user_id", "<>", Context.getUserId()).and("user_id", "<>", 45));
		
		List<JobReportShare> sharePage = null;
		if(reportId!=null){
		sharePage = mapper.query(JobReportShare.class, "Share.join", Cnd.where("sreport_id", "=", reportId));
		}
		req.setAttribute("markId", reportId);
		req.setAttribute("sharePage", sharePage);
		req.setAttribute("jobReply", jobReply);
		req.setAttribute("sharePeople", sharePeople);
		req.setAttribute("report", report);
		req.setAttribute("type", type);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		return postAdd(req, res);
		}
	@POST
	@At
	@Ok("json")
	public Object wxadd(HttpServletRequest req, HttpServletResponse res) {
		return postAdd(req, res);
	}
	public Object postAdd(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer reportId = null;
		JobReport report = null;
		String judgeTime = null;
		JobTime jobTime= null;
		try{
			
			CSRF.validate(req);
			reportId = Https.getInt(req, "reportId", R.I);
			Integer approveActor = Https.getInt(req, "approveActor", R.REQUIRED, "上级审批");
			String start_yyyyMMdd = Https.getStr(req, "start_yyyyMMdd", R.REQUIRED, R.D, "开始时间");
			String end_yyyyMMdd = Https.getStr(req, "end_yyyyMMdd", R.REQUIRED, R.D, "结束时间");			
			String content1 = Https.getStr(req, "content1", R.REQUIRED, "内容");
			String content2 = Https.getStr(req, "content2");
			String summary = Https.getStr(req, "summary");
			String[] shareto = req.getParameterValues("shareto");
			DateTime now = new DateTime();
			if(reportId != null){
				/*report = mapper.fetch(JobReport.class, "Report.query", Cnd.where("r.report_id", "=", reportId));*/
				report = dao.fetch(JobReport.class, reportId);
				/*if(report.getApprove() == 1)
					throw new Errors("周报已审阅,不能再做更改");	*/												
			}
			DateTime start = Calendars.parse(start_yyyyMMdd+" 00:00", Calendars.DATE_TIME);
			DateTime end = Calendars.parse(end_yyyyMMdd+" 23:59", Calendars.DATE_TIME);
			
			
			//查询上次提交周报的结束日期
			jobTime= dao.fetch(JobTime.class, Cnd.where("user_id", "=", Context.getUserId()));
			if(report == null){
			if(jobTime != null)
				judgeTime = jobTime.getEndTime();
			
			/*判断周报开始时间是否小于上次提交周报的时间*/
			if(judgeTime != null){
				String splitTime = judgeTime.split("\\ ")[0];
				DateTime jTime = Calendars.parse(splitTime+" 23:59", Calendars.DATE_TIME);
				if(jTime.isAfter(start)){
					throw new Errors("周报开始时间不能小于或等于上次提交周报的时间");
				}
			}
			//如果查询出这次的周报不存在,则新添周报			
			report = new JobReport();
			report.setCreateTime(now.toDate());
			report.setType(1);
			/*report.setApprove(0);*/
			report.setUserId(Context.getUserId());
			report.setTitle("周报");
			/*report.setSubmit(shareto.length);*/
			}
			report.setApprove(0);
			report.setApproveActor(approveActor);
			report.setContent1(content1);	
			report.setContent2(content2);
			report.setSummary(summary);
			if(shareto!=null){
				report.setShareNumber(shareto.length);
			}else{
				report.setShareNumber(null);	
			}
			/*判断开始日期不能大于结束日期*/
			if(start.isAfter(end)){
				throw new Errors("开始日期不能大于结束日期");		
			}
			report.setStartDate(start.toString("yyyy-MM-dd"));
			report.setEndDate(end.toString("yyyy-MM-dd"));
			if(jobTime == null)
				jobTime = new JobTime();
			jobTime.setEndTime(report.getEndDate());
			jobTime.setType(report.getType());
			jobTime.setUserId(report.getUserId());
			Integer repotyId = null;
			if(reportId != null){
				report.setModifyTime(now.toDate());
				/*dao.update(report);*/
				transSave(report,jobTime,true,shareto,reportId);
			}else{
				/*dao.insert(report);*/
				repotyId = transSave(report,jobTime,false,shareto,reportId);
			}
			mb.put("reportId",(reportId==null)?repotyId:reportId);
			Code.ok(mb, (reportId == null ? "新建" : "编辑") + "周报成功");
			
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(ApplyWeekReport:add) error: ", e);
			Code.error(mb, (reportId == null ? "新建" : "编辑") + "周报失败");
		}
		return mb;
			
	}

	@POST
	@At
	@Ok("json")
	public Object share(HttpServletRequest req, HttpServletResponse res) {
		return shareUtil(req, res);			
		}
	@POST
	@At
	@Ok("json")
	public Object wxshare(HttpServletRequest req, HttpServletResponse res) {
		return shareUtil(req, res);			
		}
	public Object shareUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		JobReply jobReply = new JobReply();
		JobReply jobReplyto = new JobReply();
		DateTime now = new DateTime();
		User user = new User();
		try{
			Integer replyId = Https.getInt(req, "replyId", R.REQUIRED,R.I);
			String contenttoo = Https.getStr(req, "contenttoo", R.REQUIRED, "内容");
			jobReply = dao.fetch(JobReply.class,replyId);
			if(jobReply.getReplyCount()!=null)
				jobReply.setReplyCount(jobReply.getReplyCount()+1);
			else
				jobReply.setReplyCount(1);
			user = dao.fetch(User.class,jobReply.getUserId());
			/*jobReplyto = jobReply;*/
			//插入一条回复数据
			jobReplyto.setReportId(jobReply.getReportId());
			jobReplyto.setReplyContent(contenttoo);
			jobReplyto.setReplyToo(jobReply.getReplyContent());
			jobReplyto.setUserId(Context.getUserId());
			jobReplyto.setReportTime(now.toString("yyyy-MM-dd hh:mm:ss"));
			jobReplyto.setReplyTooUser(user.getTrueName());
			
			transApproveTo(jobReply,jobReplyto);
			Code.ok(mb, "回复成功");
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(ReportShare:add) error: ", e);
			Code.error(mb, "回复失败");
		}
		return mb;	
	}
	private void transApproveTo(final JobReply jobReply,final JobReply jobReplyTo) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.update(jobReply);
				dao.insert(jobReplyTo);
			}
		});
	}
	
	private Integer transSave(final JobReport report,final JobTime jobTime,final boolean isUpdate,final String[] shareto,final Integer reportId) {
			Trans.exec(new Atom() {
				@Override
				public void run() {
					JobReport jReport = null;
					if(isUpdate == true)
						dao.update(report);
					else{
						jReport = dao.insert(report);
						reId = jReport.getReportId();
					}
					if(jobTime.getTimeId()==null)
						dao.insert(jobTime);
					else
						dao.update(jobTime);
				//分享
				if(shareto!=null){
					if(reportId!=null){
						/*List<JobReportShare> listShares = dao.query(JobReportShare.class, Cnd.where("sreport_id", "=", reportId));
						if(listShares!=null){*/
							dao.clear(JobReportShare.class, Cnd.where("sreport_id","=",reportId));
						for(int i=0;i<shareto.length;i++){
							JobReportShare jobS = new JobReportShare();
							jobS.setSreportId(reportId);
							jobS.setTouserId(Integer.parseInt(shareto[i]));
							dao.insert(jobS);
						}
					}else{
						for(int i=0;i<shareto.length;i++){
							JobReportShare jobS = new JobReportShare();
							jobS.setSreportId(jReport.getReportId());
							jobS.setTouserId(Integer.parseInt(shareto[i]));
							dao.insert(jobS);
						}
					}
				}else{
					if(reportId!=null){dao.clear(JobReportShare.class, Cnd.where("sreport_id","=",reportId));}
				}
			}
			});return reId;
		}
}

