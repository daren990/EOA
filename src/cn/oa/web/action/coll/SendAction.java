package cn.oa.web.action.coll;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.Process;

import cn.oa.consts.Status;
import cn.oa.model.Borrow;
import cn.oa.model.Change;
import cn.oa.model.Errand;
import cn.oa.model.Leave;
import cn.oa.model.Outwork;
import cn.oa.model.Overtime;
import cn.oa.model.Perform;
import cn.oa.model.Regular;
import cn.oa.model.Reimburse;
import cn.oa.model.Resign;
import cn.oa.model.Ticket;
import cn.oa.model.Warn;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validates;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/coll/send")
public class SendAction extends Action {

	public static Log log = Logs.getLog(SendAction.class);
	
	private static final int pageSize = 20;
	
	@GET
	@At
	@Ok("ftl:coll/send_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/coll/send/del", token);
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
		CSRF.generate(req, "/res/warn/approve/actors", token);
		CSRF.generate(req, "/adm/salary/overtime/approve/actors", token);
		String subject = Https.getStr(req, "subject", R.CLEAN);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		
		MapBean mb = new MapBean();
		Webs.put(mb, "subject", subject);
		Webs.put(mb, "startTime", startStr);
		Webs.put(mb, "endTime", endStr);
		
		
		Page<Errand> errands = new Page<Errand>(pageSize, false);
		errands = mapper.page(Errand.class, errands, "Errand.count", "Errand.index", cnd(req, "e", mb));
		
		Page<Outwork> outworks = new Page<Outwork>(pageSize, false);
		outworks = mapper.page(Outwork.class, outworks, "Outwork.count", "Outwork.index", cnd(req, "w", mb));

		Page<Overtime> overtimes = Webs.page(req);
		overtimes = mapper.page(Overtime.class, overtimes, "OvertimeApply.count", "OvertimeApply.index", cnd(req, "ot", mb));
		
		Page<Perform> performs = Webs.page(req);
		performs = mapper.page(Perform.class, performs, "Perform.count", "Perform.index", cnd(req, "p", mb));
		
		Page<Reimburse> reimburses = Webs.page(req);
		reimburses = mapper.page(Reimburse.class, reimburses, "Reimburse.count", "Reimburse.index", cnd(req, "r", mb));
		
		Page<Borrow> borrows = Webs.page(req);
		borrows = mapper.page(Borrow.class, borrows, "Borrow.count", "Borrow.index", cnd(req, "b", mb));
		
		Page<Ticket> tickets = Webs.page(req);
		tickets = mapper.page(Ticket.class, tickets, "Ticket.count", "Ticket.index", cnd(req, "t", mb));
		
		Page<Change> change = Webs.page(req);
		change = mapper.page(Change.class, change, "Change.count", "Change.index", cnd(req, "c", mb));
		
		Page<Leave> leaves =  Webs.page(req);
		leaves = mapper.page(Leave.class, leaves, "Leave.count", "Leave.index", cnd(req, "l", mb));
		
		Page<Warn> warn = Webs.page(req);
		warn = mapper.page(Warn.class, warn, "Warn.count", "Warn.index", cnd(req, "w", mb));
		
		Page<Resign> resign = Webs.page(req);
		resign = mapper.page(Resign.class, resign, "Resign.count", "Resign.index", cnd(req, "r", mb));
		
		Page<Regular> regular = Webs.page(req);
		regular = mapper.page(Regular.class, regular, "Regular.count", "Regular.index", cnd(req, "r", mb));
		
		
		QueryFilter filter = new QueryFilter();
		filter.setOperator(String.valueOf(Context.getUserId()));
		if (Strings.isNotBlank(startStr))
			filter.setCreateTimeStart(startStr);
		if (Strings.isNotBlank(endStr))
			filter.setCreateTimeStart(endStr);
		List<HistoryOrder> orders = snaker.query().getHistoryOrders(filter);
		
		List<Process> processes = snaker.process().getProcesss(new QueryFilter().setState(Status.ENABLED));
		req.setAttribute("regular", regular.getResult());
		req.setAttribute("leaves", leaves.getResult());
		req.setAttribute("errands", errands.getResult());
		req.setAttribute("outworks", outworks.getResult());
		req.setAttribute("overtimes", overtimes.getResult());
		req.setAttribute("performs", performs.getResult());
		req.setAttribute("reimburses", reimburses.getResult());
		req.setAttribute("borrows", borrows.getResult());
		req.setAttribute("tickets", tickets.getResult());
		req.setAttribute("resign",resign.getResult());
		req.setAttribute("orders", orders);
		req.setAttribute("processes", processes);
		req.setAttribute("change", change.getResult());
		req.setAttribute("warn", warn.getResult());
		
		req.setAttribute("mb", mb);
	}
	
	private Criteria cnd(HttpServletRequest req, String field, MapBean mb) {
		String subject = mb.getString("subject");
		String startStr = mb.getString("startTime");
		String endStr = mb.getString("endTime");
		
		Criteria cri = Cnd
				.where(field + ".status", "=", Status.ENABLED)
				.and(field + ".user_id", "=", Context.getUserId());
		if (Strings.isNotBlank(subject)) cri.where().and(field + ".subject", "like", "%" + subject + "%");
		if (Strings.isNotBlank(startStr)) cri.where().and(field + ".create_time", ">=", startStr + " 00:00:00");
		if (Strings.isNotBlank(endStr)) cri.where().and(field + ".create_time", "<=", endStr + " 23:59:59");
		cri.getOrderBy().desc(field + ".modify_time");
		
		return cri;
	}
	
	@POST
	@At
	@Ok("json")
	public Object del(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, "checkedIds");
			String[] arr = Strings.splitIgnoreBlank(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				List<Integer> leaveIds = new ArrayList<Integer>();
				List<Integer> errandIds = new ArrayList<Integer>();
				List<Integer> outworkIds = new ArrayList<Integer>();
				List<Integer> overtimeIds = new ArrayList<Integer>();
				
				for (String str : arr) {
					String pref = Strings.before(str, "_");
					String suff = Strings.after(str, "_");
					if (!Validates.integer(suff)) continue;
					if (pref.equals("leave")) leaveIds.add(Integer.valueOf(suff));
					if (pref.equals("errand")) errandIds.add(Integer.valueOf(suff));
					if (pref.equals("outwork")) outworkIds.add(Integer.valueOf(suff));
					if (pref.equals("overtime")) overtimeIds.add(Integer.valueOf(suff));
				}
				
//				DateTime now = new DateTime();
//				Map<Integer, WorkDay> dayMap = workRepository.dayMap();
//				Map<Integer, String[]> weekMap = workRepository.weekMap();
//				Map<String, Integer[]> monthMap = workRepository.monthMap(Context.getCorpId());

				/*WorkDay day = dayMap.get(Context.getDayId());
				Asserts.isNull(day, "日排班不能为空值");
				String[] weeks = weekMap.get(Context.getWeekId());
				Asserts.isEmpty(weeks, "周排班不能为空值");*/

				// 请假
				if (!Asserts.isEmpty(leaveIds)) {
					List<Leave> leaves = mapper.query(Leave.class, "Leave.query", cnd("l", "leave_id", leaveIds));
					for (Leave leave : leaves) {
//						leave.setStatus(Status.DISABLED);
//						leaveService.delete(leave, day, weeks, monthMap, Context.getUserId(), now);
						leaveService.delete(leave);
					}
				}
				
				// 出差
				if (!Asserts.isEmpty(errandIds)) {
					List<Errand> errands = mapper.query(Errand.class, "Errand.query", cnd("e", "errand_id", errandIds));
					for (Errand errand : errands) {
//						errand.setStatus(Status.DISABLED);
//						errandService.delete(errand, day, weeks, monthMap, Context.getUserId(), now);
						errandService.delete(errand);
					}
				}
				
				// 外勤
				if (!Asserts.isEmpty(outworkIds)) {
					List<Outwork> outworks = mapper.query(Outwork.class, "Outwork.query", cnd("w", "outwork_id", outworkIds));
					for (Outwork outwork : outworks) {
//						outwork.setStatus(Status.DISABLED);
//						Map<String, String[]> remarkMap = new ConcurrentHashMap<String, String[]>();
//						outworkService.shift(Calendars.str(outwork.getStartTime(),Calendars.DATE), remarkMap,  null,Context.getUserId(),outwork.getType());
//						outworkService.transDelete(outwork, remarkMap);
						outworkService.delete(outwork);
					}
				}
				
				// 加班
				if (!Asserts.isEmpty(overtimeIds)) {
					List<Overtime> overtimes = mapper.query(Overtime.class, "Overtime.query", cnd("ot", "overtime_id", overtimeIds));
					for (Overtime overtime : overtimes) {
//						overtime.setStatus(Status.DISABLED);
//						dao.update(overtime);
						overtimeService.delete(overtime);
					}
				}
			}
			Code.ok(mb, "删除待发事项成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Send:del) error: ", e);
			Code.error(mb, "删除待发事项失败");
		}

		return mb;
	}

	private Criteria cnd(String field, String id, List<Integer> values) {
		Criteria cri = Cnd
			.where(field + ".status", "=", Status.ENABLED)
			.and(field + ".user_id", "=", Context.getUserId())
			.and(field + ".approve", "=", Status.PROOFING)
			.and(field + "." + id, "in", values);
		return cri;
	}
}
