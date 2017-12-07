package cn.oa.web.action.res.change;

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
import cn.oa.model.Asset;
import cn.oa.model.Change;
import cn.oa.model.ChangeActor;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
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

@IocBean(name = "res.change.approve")
@At(value = "/res/change/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);
	
	@GET
	@At
	@Ok("ftl:res/change/approve_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/change/approve/actors", token);
		CSRF.generate(req, "/res/change/approve/returned", token);
		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("c.status", "=", Status.ENABLED)
			.and("a.actor_id", "=", Context.getUserId());
		Cnds.gte(cri, mb, "c.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "c.create_time", "endTime", endStr);
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		cri.getOrderBy().desc("c.modify_time");
		
		Page<Change> page = Webs.page(req);
		page = mapper.page(Change.class, page, "ChangeApprove.count", "ChangeApprove.index", cri);

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
			Integer changeId = Https.getInt(req, "changeId", R.REQUIRED, R.I, "申请ID");
			List<ChangeActor> actors = mapper.query(ChangeActor.class, "ChangeActor.query", Cnd
					.where("a.change_id", "=", changeId)
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
	@Ok("ftl:res/change/approve_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer changeId = Https.getInt(req, "changeId", R.REQUIRED, R.I, "申请ID");
		Change change = mapper.fetch(Change.class, "ChangeApprove.query", Cnd
				.where("a.actor_id", "=", Context.getUserId())
				.and("c.change_id", "=", changeId));
		Asserts.isNull(change, "申请不存在");

		List<ChangeActor> actors = mapper.query(ChangeActor.class, "ChangeActor.query", Cnd
				.where("a.change_id", "=", changeId)
				.asc("a.modify_time"));
		ChangeActor actor = null; // 当前审批人员
		ChangeActor next = null; // 指向下一审批人员
		
		Integer bindId = null;
		for (ChangeActor e : actors) {
			if (e.getActorId().equals(Context.getUserId())) actor = e; // last
			if (e.getVariable().equals(Roles.ASM.getName())) bindId = e.getActorId();
		}
		if (actor.getRefererId() != 0) {
			for (ChangeActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId())) next = e;
			}
		}
		List<User> operators = null;
		if (actor.getVariable().equals(Roles.MGR.getName())) {
			User user = dao.fetch(User.class, change.getUserId());
			operators = userService.operators(user.getCorpId(), Roles.ASM.getName());
		}

		req.setAttribute("operators", operators);
		req.setAttribute("change", change);
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
		Integer changeId = null;

		String error = null;
		try {
			CSRF.validate(req);
			
			changeId = Https.getInt(req, "changeId", R.REQUIRED, R.I, "申请ID");
			
			Change change = mapper.fetch(Change.class, "ChangeApprove.query", Cnd
					.where("a.actor_id", "=", Context.getUserId())
					.and("c.change_id", "=", changeId));
			Asserts.isNull(change, "申请不存在");
			
			if (Asserts.hasAny(change.getApproved(), new Integer[] { 3, Status.OK })) {
				returned(req, mb, changeId);
				error = "回收资产失败";
			} else {
				approve(req, mb, changeId, change);
				error = "申请审批失败";
			}
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, error);
		}

		return mb;
	}
	
	private void approve(HttpServletRequest req, MapBean mb, Integer changeId, Change change) {
		DateTime now = new DateTime();
		
		Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1,2]", "审批状态");
		String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
		
		ChangeActor actor = dao.fetch(ChangeActor.class, Cnd
				.where("changeId", "=", changeId)
				.and("actorId", "=", Context.getUserId()));
		Asserts.isNull(actor, "禁止审批资产申请");
		
		Integer required = R.CLEAN;
		if (!actor.getVariable().equals(Roles.ASM.getName()) && approve.equals(Status.APPROVED)) {
			required = R.REQUIRED;
		}
		Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
		refererId = Values.getInt(refererId);
		
		Asset asset = dao.fetch(Asset.class, change.getAssetId());
		Asserts.isNull(asset, "资产不存在");
		
		Criteria cri = Cnd
				.where("assetId", "=", asset.getAssetId());
				
		if (changeId != null)
			cri.where().and("changeId", "!=", changeId);
		
		
		if (actor.getRefererId() != 0) {
			ChangeActor next = dao.fetch(ChangeActor.class, Cnd
					.where("changeId", "=", changeId)
					.and("actorId", "=", actor.getRefererId()));
			if (next != null)
				Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的资产申请");
		}
		
		String nextVariable = null;
		if (approve.equals(Status.APPROVED)) {
			if (actor.getVariable().equals(Roles.MGR.getName())) {
				nextVariable = Roles.ASM.getName();
			}
		}
		
		transApprove(changeId, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, opinion);
		Code.ok(mb, "申请审批成功");
	}
	
	private void returned(HttpServletRequest req, MapBean mb, Integer changeId) {
		Integer returned = Https.getInt(req, "returned", R.REQUIRED, R.I, R.IN, "in [0,1]", "回收状态");
		if (returned.equals(Value.T)) {
			dao.update(Change.class, Chain
					.make("returned", returned)
					.add("deduct", Value.F)
					.add("approved", Status.OK), Cnd
					.where("status", "=", Status.ENABLED)
					.and("approved", "=", 3)
					.and("changeId", "=", changeId));
		} else {
			dao.update(Change.class, Chain
					.make("returned", returned)
					.add("deduct", Value.T)
					.add("approved", 3), Cnd
					.where("status", "=", Status.ENABLED)
					.and("approved", "=", Status.OK)
					.and("changeId", "=", changeId));
		}
		
		Code.ok(mb, (returned.equals(Value.T) ? "同意" : "拒绝") + "回收资产成功");
	}
	
	private void transApprove(final Integer changeId, final Integer approve, final Integer actorId, final String variable,
			final Integer refererId, final String nextVariable, final DateTime now, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				dao.update(ChangeActor.class, Chain
						.make("approve", approve)
						.add("refererId", refererId)
						.add("modifyTime", now.toDate())
						.add("opinion", opinion), Cnd
						.where("changeId", "=", changeId)
						.and("actorId", "=", actorId)
						.and("variable", "=", variable));
				dao.clear(ChangeActor.class, Cnd.where("changeId", "=", changeId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						dao.insert(new ChangeActor(changeId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Status.value));
					self = refererId.equals(Value.I) ? 3 : Status.APPROVED;
				}
				
				Chain chain = Chain
						.make("approved", self)
						.add("modifyTime", now.toDate());
				
				/*// 是否扣除数量
				if (variable.equals(Roles.ASM.getName())) {
					chain.add("deduct", approve.equals(Status.APPROVED) ? Value.T : Value.F);
				}*/
				
				dao.update(Change.class, chain, Cnd
						.where("changeId", "=", changeId));
			}
		});
	}
	
}