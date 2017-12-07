package cn.oa.web.action.addressBook;


import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
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
import cn.oa.model.Archive;
import cn.oa.model.JobReply;
import cn.oa.model.JobReport;
import cn.oa.model.JobReportShare;
import cn.oa.model.JobTime;
import cn.oa.model.Leave;
import cn.oa.model.LeaveActor;
import cn.oa.model.Org;
import cn.oa.model.ReportModel;
import cn.oa.model.Resign;
import cn.oa.model.ResignActor;
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
@IocBean(name = "addressBook.reportShare")
@At(value = "addressBook/reportShare")
public class ReportShare extends Action{
	
	public static Log log = Logs.getLog(ApplyDayReport.class);
	public static final int PAGE_SIZE = 100000;
	@GET
	@At
	@Ok("ftl:addressBook/Report_share")
	public void page(HttpServletRequest req){
		pageUtil(req);
	}
	@GET
	@At
	@Ok("ftl:addressBook/Report_share")
	public void wxpage(HttpServletRequest req){
		pageUtil(req);
	}
	public void pageUtil(HttpServletRequest req){

		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);	
		Integer daySize = 15;
		Integer dayNum = Https.getInt(req, "dayNum", R.I);
		Integer weekSize = 15;
		Integer weekNum = Https.getInt(req, "weekNum", R.I);
		MapBean mb = new MapBean();
		Criteria daycri = Cnd.cri();
		daycri.where()
				.and("s.touser_id", "=", Context.getUserId())
				.and("w.type", "=", 2);
		daycri.getOrderBy().desc("w.create_time");
		

		Criteria weekcri = Cnd.cri();
		weekcri.where()
				.and("s.touser_id", "=", Context.getUserId())
				.and("w.type", "=", 1);
		weekcri.getOrderBy().desc("w.create_time");
		
		/*Page<JobReport> daypage= Webs.page(req);
		daypage = mapper.page(JobReport.class, daypage, "JobReprotShare.count", "JobReprotShare.index", daycri);	*/	
		List<JobReport> daypage =null;
		List<JobReport> weekpage =null;
		Pager dayPager=new Pager();
		if(daySize!=null&&dayNum!=null){
			dayPager.setPageNumber(dayNum);//第几页
			dayPager.setPageSize(daySize);//有多少条
		}else{
			dayPager.setPageNumber(1);//第几页
			dayPager.setPageSize(15);//有多少条
		}
		Pager weekPager=new Pager();
		if(weekSize!=null&&weekNum!=null){
			weekPager.setPageNumber(weekNum);//第几页
			weekPager.setPageSize(weekSize);//有多少条
		}else{
			weekPager.setPageNumber(1);//第几页
			weekPager.setPageSize(15);//有多少条
		}
		daypage = query(JobReport.class, "JobReprotShare.unindex", daycri,dayPager);
		long dayCount = mapper.count("JobReprotShare.count", daycri);
		weekpage = query(JobReport.class, "JobReprotShare.unindex", weekcri,weekPager);
		long weekCount = mapper.count("JobReprotShare.count", weekcri);
		/*Page<JobReport> weekpage= Webs.page(req);
		weekpage = mapper.page(JobReport.class, weekpage, "JobReprotShare.count", "JobReprotShare.index", weekcri);		*/
		//获取内容和评论
		Integer reportId = Https.getInt(req, "reportId", R.I);
		Integer type = Https.getInt(req, "type", R.I);
		JobReport report = null;
		List<JobReply> jobReply = null;
		if(reportId != null){
			report = mapper.fetch(JobReport.class, "JobReprot.query", Cnd.where("w.report_id", "=", reportId));
			/*jobReply = dao.query(JobReply.class, Cnd.where("", op, value))*/
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
			Criteria stareCri = Cnd.cri();
			stareCri.where()
					.and("touser_id", "=", Context.getUserId());
			stareCri.getOrderBy().desc("share_id");
			/*report = mapper.fetch(JobReport.class, "JobReprot.query", stareCri);*/
			JobReportShare jrs = dao.fetch(JobReportShare.class,stareCri);
			if(jrs!=null){
			report = mapper.fetch(JobReport.class, "JobReprot.query", Cnd.where("w.report_id", "=", jrs.getSreportId()));
			Criteria rcri = Cnd.cri();
			rcri.where()
					.and("r.report_id", "=", jrs.getSreportId());
			rcri.getOrderBy().desc("r.reply_id");
			jobReply = mapper.query(JobReply.class, "Share.reply", rcri);
			for(JobReply j:jobReply){
				j.setReportTime(j.getReportTime().substring(0, 19));
			}
			}
			if(report == null){
				report = new JobReport();
			}
			
		}
		long dayPageCount = 15;
		long weekPageCount = 15;
		dayPageCount = dayCount%dayPageCount == 0?dayCount/dayPageCount:dayCount/dayPageCount+1;
		weekPageCount = weekCount%weekPageCount == 0?weekCount/weekPageCount:weekCount/weekPageCount+1;
		req.setAttribute("jobReply", jobReply);
		req.setAttribute("report", report);
		req.setAttribute("type", type);
		req.setAttribute("weekpage", weekpage);
		req.setAttribute("daypage", daypage);	
		req.setAttribute("dayCount", dayCount);
		req.setAttribute("dayNum", dayNum);
		req.setAttribute("weekCount", weekCount);
		req.setAttribute("weekNum", weekNum);
		req.setAttribute("dayPageCount", dayPageCount);
		req.setAttribute("weekPageCount", weekPageCount);
		/*List<JobReport> jReport = page.getResult();*/
		/*req.setAttribute("corps", corps);*/
		req.setAttribute("mb", mb);		
	}

	@POST
	@At
	@Ok("json")
	public Object page(HttpServletRequest req, HttpServletResponse res) {
		return postPage(req, res);			
		}
	@POST
	@At
	@Ok("json")
	public Object wxpage(HttpServletRequest req, HttpServletResponse res) {
		return postPage(req, res);
	}
	public Object postPage(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer reportId = null;
		JobReply jobReply = new JobReply();
		try{
			//String[] shareto = req.getParameterValues("shareto");
			CSRF.validate(req);
			reportId = Https.getInt(req, "reportId", R.REQUIRED,R.I);
			String replyContent = Https.getStr(req, "reply", R.REQUIRED, "内容");
			DateTime now = new DateTime();
			
			jobReply.setReportId(reportId);
			jobReply.setUserId(Context.getUserId());
			jobReply.setReplyContent(replyContent);
			jobReply.setReportTime(now.toString("yyyy-MM-dd hh:mm:ss"));
			transApprove(jobReply,reportId);
			Code.ok(mb, "评论成功");
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(ReportShare:add) error: ", e);
			Code.error(mb, "日报失败");
		}
		return mb;			
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		return addUtil(req, res);			
		}
	public Object addUtil(HttpServletRequest req, HttpServletResponse res) {
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
	
	private void transApprove(final JobReply jobReply,final Integer reportId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				JobReport jobReport = dao.fetch(JobReport.class,reportId);
				if(jobReport!=null){
					jobReport.setSubmit(jobReply.getReplyContent());
					dao.update(jobReport);
				}
				dao.insert(jobReply);
			}
		});
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
	//分页查询
	private <T> List<T> query(Class<T> clazz, String query, Condition cnd,Pager pager) {
		Sql sql = null;

		sql = Sqls.queryEntity(dao.sqls().get(query));
		sql.setEntity(dao.getEntity(clazz));
		if (cnd != null) {
			sql.setCondition(cnd);
		}
		if (pager != null) {
			sql.setPager(pager);
		}
		dao.execute(sql);
		return sql.getList(clazz);
	}

}
