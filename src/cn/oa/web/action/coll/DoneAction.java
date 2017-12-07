package cn.oa.web.action.coll;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.WorkItem;

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
import cn.oa.model.User;
import cn.oa.model.Warn;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "coll.done")
@At(value = "/coll/done")
public class DoneAction extends Action {

	private static final int pageSize = 20;
	
	@GET
	@At
	@Ok("ftl:coll/done_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:coll/done_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	public void pageUtil(HttpServletRequest req){
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/examine/perform/apply/nodes", token);
		CSRF.generate(req, "/adm/examine/perform/approve/actors", token);
		CSRF.generate(req, "/adm/expense/reimburse/approve/actors", token);
		CSRF.generate(req, "/adm/expense/borrow/approve/actors", token);
		CSRF.generate(req, "/res/ticket/approve/actors", token);
		CSRF.generate(req, "/adm/salary/leave/approve/actors", token);
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
		errands = mapper.page(Errand.class, errands, "Errand.count", "Errand.index", cnd(req, "e", mb, false));
		
		Page<Outwork> outworks = new Page<Outwork>(pageSize, false);
		outworks = mapper.page(Outwork.class, outworks, "Outwork.count", "Outwork.index", cnd(req, "w", mb, false));

		/*Page<Overtime> overtimes = new Page<Overtime>(pageSize, false);
		overtimes = mapper.page(Overtime.class, overtimes, "Overtime.count", "Overtime.index", cnd(req, "ot", mb, false));*/

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
		
		Page<Regular> regular = Webs.page(req);
		regular = mapper.page(Regular.class, regular, "RegularApprove.count", "RegularApprove.index", cnd(req, "r", mb,true));
		
		Page<Change> change = Webs.page(req);
		change = mapper.page(Change.class, change, "ChangeApprove.count", "ChangeApprove.index", cnd(req, "c", mb,true));
		
		Page<Leave> leaves =  Webs.page(req);
		leaves = mapper.page(Leave.class, leaves, "LeaveApprove.count", "LeaveApprove.index", cnd(req, "l", mb, true));
		
		Page<Warn> warn = Webs.page(req);
		warn = mapper.page(Warn.class, warn, "WarnApprove.count", "WarnApprove.index", cnd(req, "w", mb , true));
		
		QueryFilter filter = new QueryFilter();
		filter.setOperator(String.valueOf(Context.getUserId()));
		if (Strings.isNotBlank(startStr))
			filter.setCreateTimeStart(startStr);
		if (Strings.isNotBlank(endStr))
			filter.setCreateTimeStart(endStr);
		org.snaker.engine.access.Page<WorkItem> page = new org.snaker.engine.access.Page<WorkItem>(6);
		List<WorkItem> works = snaker.query().getHistoryWorkItems(page, filter);
		
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
		
		req.setAttribute("leaves", leaves.getResult());
		req.setAttribute("errands", errands.getResult());
		req.setAttribute("outworks", outworks.getResult());
		req.setAttribute("overtimes", overtimes.getResult());
		req.setAttribute("performs", performs.getResult());
		req.setAttribute("reimburses", reimburses.getResult());
		req.setAttribute("borrows", borrows.getResult());
		req.setAttribute("tickets", tickets.getResult());
		req.setAttribute("resign", resign.getResult());
		req.setAttribute("regular", regular.getResult());
		req.setAttribute("change", change.getResult());
		req.setAttribute("warn", warn.getResult());
		
		req.setAttribute("works", works);
		req.setAttribute("userMap", userMap);
		req.setAttribute("mb", mb);
	}
	
	private Criteria cnd(HttpServletRequest req, String field, MapBean mb, boolean isActor) {
		String subject = mb.getString("subject");
		String startStr = mb.getString("startTime");
		String endStr = mb.getString("endTime");
		
		Criteria cri = Cnd.where(field + ".status", "=", Status.ENABLED);
		if (isActor)
			cri.where().and("a.actor_id", "=", Context.getUserId())
					.and("a.approve", "in", new Integer[] { Status.APPROVED, Status.UNAPPROVED });
		else
			cri.where()
					.and(field + ".operator_id", "=", Context.getUserId())
					.and(field + ".approve", "in", new Integer[] { Status.APPROVED, Status.UNAPPROVED });
		if (Strings.isNotBlank(subject)) cri.where().and(field + ".subject", "like", "%" + subject + "%");
		if (Strings.isNotBlank(startStr)) cri.where().and(field + ".create_time", ">=", startStr + " 00:00:00");
		if (Strings.isNotBlank(endStr)) cri.where().and(field + ".create_time", "<=", endStr + " 23:59:59");
		cri.getOrderBy().desc(field + ".modify_time");
		
		return cri;
	}
}
