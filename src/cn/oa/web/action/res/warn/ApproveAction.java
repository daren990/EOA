package cn.oa.web.action.res.warn;

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
import cn.oa.model.Warn;
import cn.oa.model.WarnActor;
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

@IocBean(name = "res.warn.approve")
@At(value = "/res/warn/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);
	
	@GET
	@At
	@Ok("ftl:res/warn/approve_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/warn/approve/actors", token);
		
		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);
		Integer approve = Https.getInt(req, "approve", R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("w.status", "=", Status.ENABLED)
			.and("a.actor_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		Cnds.gte(cri, mb, "w.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "w.create_time", "endTime", endStr);
		cri.getOrderBy().desc("w.modify_time");
		
		Page<Warn> page = Webs.page(req);
		page = mapper.page(Warn.class, page, "WarnApprove.count", "WarnApprove.index", cri);

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
			Integer warnId = Https.getInt(req, "warnId", R.REQUIRED, R.I, "申请ID");
			List<WarnActor> actors = mapper.query(WarnActor.class, "WarnActor.query", Cnd
					.where("a.warn_id", "=", warnId)
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
	@Ok("ftl:res/warn/approve_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer warnId = Https.getInt(req, "warnId", R.REQUIRED, R.I, "申请ID");
		
		Warn warn = mapper.fetch(Warn.class, "WarnApprove.query", Cnd
				.where("a.actor_id", "=", Context.getUserId())
				.and("w.warn_id", "=", warnId));
		Asserts.isNull(warn, "报障申请不存在");
		
		List<WarnActor> actors = mapper.query(WarnActor.class, "WarnActor.query", Cnd.where("a.warn_id", "=", warnId).asc("a.modify_time"));
		WarnActor actor = null; // 当前审批人员
		WarnActor next = null; // 指向下一审批人员
		
		Integer bindId = null;
		for (WarnActor e : actors) {
			if (e.getActorId().equals(Context.getUserId())) actor = e;
		}
		if (actor.getRefererId() != 0) {
			for (WarnActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId())) next = e;
			}
		}
		Asserts.isNull(actor, "当前审批人员不存在");
		
		req.setAttribute("warn", warn);
		req.setAttribute("actors", actors);
		req.setAttribute("actor", actor);
		req.setAttribute("next", next);
		req.setAttribute("bindId", bindId);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer warnId = null;
		try {
			CSRF.validate(req);
			
			warnId = Https.getInt(req, "warnId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			
			DateTime now = new DateTime();
			Warn warn = mapper.fetch(Warn.class, "WarnApprove.query", Cnd
					.where("a.actor_id", "=", Context.getUserId())
					.and("w.warn_id", "=", warnId));
			Asserts.isNull(warn, "报障申请不存在");

			WarnActor actor = dao.fetch(WarnActor.class, Cnd.where("warnId", "=", warnId).and("actorId", "=", Context.getUserId()));
			Asserts.isNull(actor, "禁止审批报障申请");
			
			Integer required = R.CLEAN;
			if (actor.getVariable().equals(Roles.WRR.getName()) && approve.equals(Status.APPROVED)) {
				required = R.REQUIRED;
			}
			String result = Https.getStr(req, "result", R.CLEAN, required, R.RANGE, "{1,200}", "处理结果");
			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			refererId = Values.getInt(refererId);
			
			if (actor.getRefererId() != 0) {
				WarnActor next = dao.fetch(WarnActor.class, Cnd.where("warnId", "=", warnId).and("actorId", "=", actor.getRefererId()));
				if (next != null)
					Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的报障申请");
			}
			
			String nextVariable = null;
			if (actor.getVariable().equals(Roles.WRR.getName()))
				nextVariable = Roles.Me.getName();
			
			transApprove(warnId, result, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, opinion);
			
			Code.ok(mb, "报障申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "报障申请审批失败");
		}

		return mb;
	}
	
	private void transApprove(final Integer warnId, final String result, final Integer approve, final Integer actorId, final String variable,
			final Integer refererId, final String nextVariable, final DateTime now, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				dao.update(WarnActor.class, Chain
						.make("approve", approve)
						.add("refererId", refererId)
						.add("modifyTime", now.toDate())
						.add("opinion", opinion), Cnd
						.where("warnId", "=", warnId)
						.and("actorId", "=", actorId));
				dao.clear(WarnActor.class, Cnd.where("warnId", "=", warnId).and("approve", "=", Status.PROOFING));
				
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I)) {
						dao.insert(new WarnActor(warnId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Status.value));
						self = variable.equals(Roles.WRR.getName()) ? Status.CHECKING : Status.APPROVED;
					} else
						self = Status.OK;
				}

				Chain chain = Chain
						.make("approved", self)
						.add("modifyTime", now.toDate());
				if (variable.equals(Roles.WRR.getName()))
					chain.add("result", result);
				
				dao.update(Warn.class, chain, Cnd
						.where("warnId", "=", warnId));
			}
		});
	}
}