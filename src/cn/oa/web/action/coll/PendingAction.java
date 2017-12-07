package cn.oa.web.action.coll;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.WorkItem;

import cn.oa.consts.Status;
import cn.oa.model.Att_record;
import cn.oa.model.Borrow;
import cn.oa.model.Change;
import cn.oa.model.Errand;
import cn.oa.model.JobReport;
import cn.oa.model.Leave;
import cn.oa.model.Notice;
import cn.oa.model.Outwork;
import cn.oa.model.Overtime;
import cn.oa.model.Perform;
import cn.oa.model.RedressRecord;
import cn.oa.model.Regular;
import cn.oa.model.Reimburse;
import cn.oa.model.Resign;
import cn.oa.model.Ticket;
import cn.oa.model.User;
import cn.oa.model.Warn;
import cn.oa.model.WorkAttendance;
import cn.oa.service.OvertimeService;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
import cn.oa.web.view.FreemarkerView;

@IocBean(name = "coll.pending")
@At(value = "/coll/pending")
public class PendingAction extends Action {

	public static Log log = Logs.getLog(PendingAction.class);

	private static final int pageSize = 20;
	@Inject
	private OvertimeService overtimeService;
	
	public Object pageUtil(HttpServletRequest req){
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/examine/perform/apply/nodes", token);
		CSRF.generate(req, "/adm/examine/perform/approve/actors", token);
		CSRF.generate(req, "/adm/expense/reimburse/approve/actors", token);
		CSRF.generate(req, "/adm/expense/borrow/approve/actors", token);
		CSRF.generate(req, "/adm/salary/leave/approve/actors", token);
		CSRF.generate(req, "/res/ticket/approve/actors", token);
		CSRF.generate(req, "/flow/task/actors", token);
		CSRF.generate(req, "/hrm/resign/approve/actors", token);
		CSRF.generate(req, "/hrm/regular/approve/actors", token);
		CSRF.generate(req, "/res/change/approve/actors", token);
		CSRF.generate(req, "/adm/salary/overtime/approve/actors", token);
		String subject = Https.getStr(req, "subject", R.CLEAN);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		
		MapBean mb = new MapBean();
		Webs.put(mb, "subject", subject);
		Webs.put(mb, "startTime", startStr);
		Webs.put(mb, "endTime", endStr);
		
		//leave请假     l==0  待审批
		Page<Leave> leaves =  Webs.page(req);
		leaves = mapper.page(Leave.class, leaves, "LeaveApprove.count", "LeaveApprove.index", cnd(req, "l", mb, true));
		
		Page<Errand> errands = new Page<Errand>(pageSize, false);
		errands = mapper.page(Errand.class, errands, "Errand.count", "Errand.index", cnd(req, "e", mb, false));
		
		Page<Outwork> outworks = new Page<Outwork>(pageSize, false);
		outworks = mapper.page(Outwork.class, outworks, "Outwork.count", "Outwork.index", cnd(req, "w", mb, false));

		Page<Overtime> overtimes = Webs.page(req);
		overtimes = mapper.page(Overtime.class, overtimes, "Overtime.count", "Overtime.index", cnd(req, "ot", mb,true));
		
		Page<Perform> performs = Webs.page(req);
		performs = mapper.page(Perform.class, performs, "PerformApprove.count", "PerformApprove.index", cnd(req, "p", mb, true));
		
		Page<Reimburse> reimburses = Webs.page(req);
		reimburses = mapper.page(Reimburse.class, reimburses, "ReimburseApprove.count", "ReimburseApprove.index", cnd(req, "r", mb, true));
		
		Page<Borrow> borrows = Webs.page(req);
		borrows = mapper.page(Borrow.class, borrows, "BorrowApprove.count", "BorrowApprove.index", cnd(req, "b", mb, true));
		
		Page<Ticket> tickets = Webs.page(req);
		tickets = mapper.page(Ticket.class, tickets, "TicketApprove.count", "TicketApprove.index", cnd(req, "t", mb, true));
		
		Page<Resign> resign = Webs.page(req);
		resign = mapper.page(Resign.class, resign, "ResignApprove.count", "ResignApprove.index", cnd(req, "r", mb,true));
		
		Page<Change> change = Webs.page(req);
		change = mapper.page(Change.class, change, "ChangeApprove.count", "ChangeApprove.index", cnd(req, "c", mb,true));
		
		Page<Warn> warn = Webs.page(req);
		warn = mapper.page(Warn.class, warn, "WarnApprove.count", "WarnApprove.index", cnd(req, "w", mb,true));
		
		Page<Regular> regular = Webs.page(req);
		regular = mapper.page(Regular.class, regular, "RegularApprove.count", "RegularApprove.index", cnd(req, "r", mb,true));
		
		Page<Att_record> record = Webs.page(req);
		record = mapper.page(Att_record.class, record, "Att_record.count", "Attr_record.index", cnd2(req,"a",mb,false));
		
		List<JobReport> jobReport = mapper.query(JobReport.class, "Report.all", Cnd.where("r.approve_actor", "=", Context.getUserId()).and("r.approve","=",0));
		
		Criteria cri = Cnd.cri();
		cri.where().and("a.approver", "=", Context.getUserId());
		Cnds.eq(cri, mb, "a.status", "status", Status.UNAPPROVED);
		cri.getOrderBy().desc("a.creator_time");
		// 批量补录申请
		Page<RedressRecord> redressRecord = Webs.page(req);
		redressRecord = mapper.page(RedressRecord.class, redressRecord, "redress_record.count", "redress_record.index", cri);
		
		Date nowDate = new Date();
		List<Notice> notices = mapper.query(Notice.class, "Notice.look", Cnd.where("n.receive_id", "=", Context.getUserId()).and("a.end_time", ">=", nowDate).and("a.start_time", "<=", nowDate));
		QueryFilter filter = new QueryFilter();
		filter.setOperator(String.valueOf(Context.getUserId()));
		if (Strings.isNotBlank(startStr))
			filter.setCreateTimeStart(startStr);
		if (Strings.isNotBlank(endStr))
			filter.setCreateTimeStart(endStr);
		org.snaker.engine.access.Page<WorkItem> page = new org.snaker.engine.access.Page<WorkItem>(6);
		List<WorkItem> works = snaker.query().getWorkItems(page, filter);
		
		StringBuilder buff = new StringBuilder();
		for (WorkItem work : works) {
			buff.append(work.getCreator()).append(",");
		}

		Map<String, String> userMap = new ConcurrentHashMap<String, String>();
		Integer[] userIds = Converts.array(buff.toString(), ",");
		if (!Asserts.isEmpty(userIds)) {
			List<User> users = dao.query(User.class, Cnd.where("userId", "in", userIds));
			for (User user : users) {
				userMap.put(String.valueOf(user.getUserId()), user.getTrueName());
			}
		}
		req.setAttribute("regular", regular.getResult());
		req.setAttribute("leaves", leaves.getResult());
		req.setAttribute("errands", errands.getResult());
		req.setAttribute("outworks", outworks.getResult());
		req.setAttribute("overtimes", overtimes.getResult());
		req.setAttribute("performs", performs.getResult());
		req.setAttribute("reimburses", reimburses.getResult());
		req.setAttribute("borrows", borrows.getResult());
		req.setAttribute("tickets", tickets.getResult());
		req.setAttribute("resign", resign.getResult());
		req.setAttribute("change", change.getResult());
		req.setAttribute("works", works);
		req.setAttribute("userMap", userMap);
		req.setAttribute("warn", warn.getResult());
		req.setAttribute("record", record.getResult());
		req.setAttribute("jobReport", jobReport);
		req.setAttribute("notices", notices);
		req.setAttribute("redressRecord", redressRecord.getResult());
		req.setAttribute("mb", mb);
		
		// 存休时长
		int minute = overtimeService.lastDeferredMinute(Context.getUserId());
		float m = new Float(minute)/60;
		String ms = new String(m+"");
		int index = ms.indexOf(".");
		ms = ms.substring(0,index+3>ms.length()?ms.length():index+3);
		req.setAttribute("overtimeRest", ms);
		WorkAttendance att = dao.fetch(WorkAttendance.class, Context.getCorpId());
		req.setAttribute("att", att);
		if(req.getSession().getAttribute("mark") != null) {
			return new FreemarkerView("wx.approval");
		}
		return null;
	}
	
	@GET
	@At
	@Ok("ftl:coll/pending_page_wx")
	public Object wxpage(HttpServletRequest req) {		
		return pageUtil(req);
	}
	@GET
	@At
	@Ok("ftl:coll/pending_page")
	public Object page(HttpServletRequest req) {
		return pageUtil(req);
	}
	
	private Criteria cnd(HttpServletRequest req, String field, MapBean mb, boolean isActor) {
		String subject = mb.getString("subject");
		String startStr = mb.getString("startTime");
		String endStr = mb.getString("endTime");
		
		Criteria cri = Cnd.where(field + ".status", "=", Status.ENABLED);
		if (isActor)
		
			cri.where().and("a.actor_id", "=", Context.getUserId())
					.and("a.approve", "=", Status.PROOFING);
		else

			cri.where()
					.and(field + ".operator_id", "=", Context.getUserId())
					.and(field + ".approve", "=", Status.PROOFING);
		
		if (Strings.isNotBlank(subject)) cri.where().and(field + ".subject", "like", "%" + subject + "%");
		if (Strings.isNotBlank(startStr)) cri.where().and(field + ".create_time", ">=", startStr + " 00:00:00");
		if (Strings.isNotBlank(endStr)) cri.where().and(field + ".create_time", "<=", endStr + " 23:59:59");
		cri.getOrderBy().desc(field + ".create_time");
		
		return cri;
	}
	
	private Criteria cnd2(HttpServletRequest req, String field, MapBean mb, boolean isActor) {
		String subject = mb.getString("subject");
		String startStr = mb.getString("startTime");
		String endStr = mb.getString("endTime");
		
		Criteria cri = Cnd.where(field + ".status", "=", Status.ENABLED);
		if (isActor)
		
			cri.where().and("a.approveId", "=", Context.getUserId())
					.and("a.approve", "=", Status.PROOFING);
		else

			cri.where()
					.and(field + ".approveId", "=", Context.getUserId())
					.and(field + ".approve", "=", Status.PROOFING);
		
		if (Strings.isNotBlank(subject)) cri.where().and(field + ".subject", "like", "%" + subject + "%");
		if (Strings.isNotBlank(startStr)) cri.where().and(field + ".date", ">=", startStr + " 00:00:00");
		if (Strings.isNotBlank(endStr)) cri.where().and(field + ".date", "<=", endStr + " 23:59:59");
		cri.getOrderBy().desc(field + ".date");
		
		return cri;
	}
	
}
