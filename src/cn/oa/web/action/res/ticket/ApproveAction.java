package cn.oa.web.action.res.ticket;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Chain;
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

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Dict;
import cn.oa.model.Ticket;
import cn.oa.model.TicketActor;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "res.ticket.approve")
@At(value = "/res/ticket/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);
	
	@GET
	@At
	@Ok("ftl:res/ticket/approve_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/ticket/approve/actors", token);
		CSRF.generate(req, "/res/ticket/approve/returned", token);
		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("t.status", "=", Status.ENABLED)
			.and("a.actor_id", "=", Context.getUserId());
		Cnds.gte(cri, mb, "t.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "t.create_time", "endTime", endStr);
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		cri.getOrderBy().desc("t.modify_time");
		
		Page<Ticket> page = Webs.page(req);
		page = mapper.page(Ticket.class, page, "TicketApprove.count", "TicketApprove.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer ticketId = Https.getInt(req, "ticketId", R.REQUIRED, R.I, "申请ID");
			List<TicketActor> actors = mapper.query(TicketActor.class, "TicketActor.query", Cnd
					.where("a.ticket_id", "=", ticketId)
					.asc("a.modify_time"));
			mb.put("actors", actors);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:actors) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:res/ticket/approve_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer ticketId = Https.getInt(req, "ticketId", R.REQUIRED, R.I, "申请ID");
		Ticket ticket = mapper.fetch(Ticket.class, "TicketApprove.query", Cnd
				.where("a.actor_id", "=", Context.getUserId())
				.and("t.ticket_id", "=", ticketId));
		Asserts.isNull(ticket, "申请不存在");

		List<TicketActor> actors = mapper.query(TicketActor.class, "TicketActor.query", Cnd
				.where("a.ticket_id", "=", ticketId)
				.asc("a.modify_time"));
		TicketActor actor = null; // 当前审批人员
		TicketActor next = null; // 指向下一审批人员
		
		Integer bindId = null;
		for (TicketActor e : actors) {
			if (e.getActorId().equals(Context.getUserId())) actor = e; // last
			if (e.getVariable().equals(Roles.ASS.getName())) bindId = e.getActorId();
		}
		if (actor.getRefererId() != 0) {
			for (TicketActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId())) next = e;
			}
		}
		List<User> operators = null;
		if (actor.getVariable().equals(Roles.ASS.getName())) {
			User user = dao.fetch(User.class, ticket.getUserId());
			operators = userService.operators(user.getCorpId(), user.getLevel());
			bindId = user.getManagerId();
		} else if (actor.getVariable().equals(Roles.MGR.getName())) {
			User user = dao.fetch(User.class, ticket.getUserId());
			operators = userService.operators(user.getCorpId(), Roles.ADR.getName());
		}

		req.setAttribute("operators", operators);
		req.setAttribute("ticket", ticket);
		req.setAttribute("actors", actors);
		req.setAttribute("actor", actor);
		req.setAttribute("next", next);
		req.setAttribute("bindId", bindId);
		req.setAttribute("ticketMap", dictService.map(Dict.TICKET));
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer ticketId = null;
		try {
			CSRF.validate(req);
			
			ticketId = Https.getInt(req, "ticketId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1,2]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			
			DateTime now = new DateTime();
			Ticket ticket = mapper.fetch(Ticket.class, "TicketApprove.query", Cnd
					.where("a.actor_id", "=", Context.getUserId())
					.and("t.ticket_id", "=", ticketId));
			Asserts.isNull(ticket, "申请不存在");

			TicketActor actor = dao.fetch(TicketActor.class, Cnd
					.where("ticketId", "=", ticketId)
					.and("actorId", "=", Context.getUserId()));
			Asserts.isNull(actor, "禁止审批票务申请");
			
			Integer required = R.CLEAN;
			if (!actor.getVariable().equals(Roles.ADR.getName()) && approve.equals(Status.APPROVED)) {
				required = R.REQUIRED;
			}
			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			refererId = Values.getInt(refererId);
			
			required = R.CLEAN;
			if (actor.getVariable().equals(Roles.ADR.getName())) {
				required = R.REQUIRED;
			}
			String result = Https.getStr(req, "result", R.CLEAN, required, R.RANGE, "{1,60}", "处理结果");
			
			if (actor.getRefererId() != 0) {
				TicketActor next = dao.fetch(TicketActor.class, Cnd
						.where("ticketId", "=", ticketId)
						.and("actorId", "=", actor.getRefererId()));
				if (next != null)
					Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的票务申请");
			}
			
			String nextVariable = null;
			if (approve.equals(Status.APPROVED)) {
				if (actor.getVariable().equals(Roles.ASS.getName())) {
					nextVariable = Roles.MGR.getName();
				} else if (actor.getVariable().equals(Roles.MGR.getName())) {
					nextVariable = Roles.ADR.getName();
				}
			}
			
			transApprove(ticketId, result, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, opinion);
			Code.ok(mb, "申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "申请审批失败");
		}

		return mb;
	}
	
	private void transApprove(final Integer ticketId, final String result, final Integer approve, final Integer actorId, final String variable,
			final Integer refererId, final String nextVariable, final DateTime now, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				dao.update(TicketActor.class, Chain
						.make("approve", approve)
						.add("refererId", refererId)
						.add("modifyTime", now.toDate())
						.add("opinion", opinion), Cnd
						.where("ticketId", "=", ticketId)
						.and("actorId", "=", actorId)
						.and("variable", "=", variable));
				dao.clear(TicketActor.class, Cnd.where("ticketId", "=", ticketId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						dao.insert(new TicketActor(ticketId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Status.value));
					self = refererId.equals(Value.I) ? Status.OK : Status.APPROVED;
				}
				
				Chain chain = Chain
						.make("approved", self)
						.add("modifyTime", now.toDate());
				if (variable.equals(Roles.ADR.getName())) chain.add("result", result);
					
				dao.update(Ticket.class, chain, Cnd
						.where("ticketId", "=", ticketId));
			}
		});
	}
	
}