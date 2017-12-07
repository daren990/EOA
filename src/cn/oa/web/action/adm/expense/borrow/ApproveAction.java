package cn.oa.web.action.adm.expense.borrow;

import java.util.ArrayList;
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
import cn.oa.model.Accountant;
import cn.oa.model.Borrow;
import cn.oa.model.BorrowActor;
import cn.oa.model.Org;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
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

@IocBean(name = "adm.expense.borrow.approve")
@At(value = "/adm/expense/borrow/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);
	
	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/expense/borrow/approve/actors", token);
		CSRF.generate(req, "/adm/expense/borrow/approve/returned", token);
		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		String number = Https.getStr(req, "number", R.CLEAN,"{1,100}");
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("b.status", "=", Status.ENABLED)
			.and("a.actor_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "b.number", "number", number);
		Cnds.gte(cri, mb, "b.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "b.create_time", "endTime", endStr);
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		cri.getOrderBy().desc("b.modify_time");
		
		Page<Borrow> page = Webs.page(req);
		page = mapper.page(Borrow.class, page, "BorrowApprove.count", "BorrowApprove.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/approve_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/approve_page_wx")
	public void wxpage(HttpServletRequest req){
		pageUtil(req);
	}

	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer borrowId = Https.getInt(req, "borrowId", R.REQUIRED, R.I, "申请ID");
			List<BorrowActor> actors = mapper.query(BorrowActor.class, "BorrowActor.query", Cnd
					.where("a.borrow_id", "=", borrowId)
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
	@Ok("ftl:adm/expense/borrow/approve_add")
	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer borrowId = Https.getInt(req, "borrowId", R.REQUIRED, R.I, "申请ID");
		Borrow borrow = mapper.fetch(Borrow.class, "BorrowApprove.query", Cnd
				.where("a.actor_id", "=", Context.getUserId())
				.and("b.borrow_id", "=", borrowId));
		Asserts.isNull(borrow, "申请不存在");

		List<BorrowActor> actors = mapper.query(BorrowActor.class, "BorrowActor.query", Cnd
				.where("a.borrow_id", "=", borrowId)
				.asc("a.modify_time"));
		BorrowActor actor = null; // 当前审批人员
		BorrowActor next = null; // 指向下一审批人员
		
		Integer bindId = null;
		for (BorrowActor e : actors) {
			if (e.getActorId().equals(Context.getUserId())) actor = e;
			if (e.getVariable().equals(Roles.ASS.getName())) bindId = e.getActorId();
		}
		if (actor.getRefererId() != 0) {
			for (BorrowActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId())) next = e;
			}
		}
		
		Integer orgId =dao.fetch(User.class, borrow.getUserId()).getCorpId();
		Integer accountant = dao.fetch(Org.class, orgId).getAccountantId();
		Accountant userIds = dao.fetch(Accountant.class, accountant);
		Asserts.isNull(accountant, "财务人员配置不能为空");
		Asserts.isNull(userIds, "财务人员配置不能为空");
		String [] str = Strings.splitIgnoreBlank(userIds.getUserIds(), ",");
		
		List<User> operators = null;
		if (actor.getVariable().equals(Roles.ASS.getName())) {
			User user = dao.fetch(User.class, borrow.getUserId());
			List<Role> roles = roleRepository.find(user.getUserId());
			String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
			operators = new ArrayList<User>();
			
			if(Asserts.hasAny(Roles.GM.getName(), roleNames)){
				for(String userId : str){
					String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
					if(Asserts.hasAny(Roles.ACC.getName(), roleName)||Asserts.hasAny(Roles.ASVI.getName(),roleName)){
						operators = new ArrayList<User>();
						operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
					}
				}
			}
			else	
			operators = userService.operators(user.getCorpId(), Roles.GM.getName());
			
		} else if (Asserts.hasAny(actor.getVariable(), new String[] { Roles.GM.getName() })) {
			
			for(String userId : str){
				String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
				if(Asserts.hasAny(Roles.ACC.getName(), roleName)||Asserts.hasAny(Roles.ASVI.getName(),roleName)){
					operators = new ArrayList<User>();
					operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
				}
			}
		} else if (actor.getVariable().equals(Roles.ACC.getName())) {
			for(String userId : str){
				String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
				if(Asserts.hasAny(Roles.ASVI.getName(), roleName)){
					operators = new ArrayList<User>();
					operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
				}
			}
		}
		else if (actor.getVariable().equals(Roles.ASVI.getName())) {
			for(String userId : str){
				String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
				if(Asserts.hasAny(Roles.CAS.getName(), roleName)||Asserts.hasAny(Roles.CSVI.getName(), roleName)){
					operators = new ArrayList<User>();
					operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
				}
			}
		}
		else if (actor.getVariable().equals(Roles.CAS.getName())) {
			for(String userId : str){
				String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
				if(Asserts.hasAny(Roles.CSVI.getName(), roleName)){
					operators = new ArrayList<User>();
					operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
				}
			}
		}
		else
			operators = new ArrayList<User>();

		req.setAttribute("operators", operators);
		req.setAttribute("borrow", borrow);
		req.setAttribute("actors", actors);
		req.setAttribute("actor", actor);
		req.setAttribute("next", next);
		req.setAttribute("bindId", bindId);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/approve_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/approve_add")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer borrowId = null;
		try {
			CSRF.validate(req);
			
			borrowId = Https.getInt(req, "borrowId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1,2]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			
			DateTime now = new DateTime();
			Borrow borrow = mapper.fetch(Borrow.class, "BorrowApprove.query", Cnd
					.where("a.actor_id", "=", Context.getUserId())
					.and("b.borrow_id", "=", borrowId));
			Asserts.isNull(borrow, "申请不存在");

			BorrowActor actor = dao.fetch(BorrowActor.class, Cnd
					.where("borrowId", "=", borrowId)
					.and("actorId", "=", Context.getUserId()));
			Asserts.isNull(actor, "禁止审批借支申请");
			
			Integer required = R.CLEAN;
			if (!actor.getVariable().equals(Roles.CSVI.getName()) && approve.equals(Status.APPROVED)) {
				required = R.REQUIRED;
			}
			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			refererId = Values.getInt(refererId);
			
			if (actor.getRefererId() != 0) {
				BorrowActor next = dao.fetch(BorrowActor.class, Cnd
						.where("borrowId", "=", borrowId)
						.and("actorId", "=", actor.getRefererId()));
				if (next != null)
					Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的借支申请");
			}
			
			String nextVariable = null;
			if (approve.equals(Status.APPROVED)) {
				if (actor.getVariable().equals(Roles.ASS.getName())) {
					nextVariable = Roles.GM.getName();
				} else if (Asserts.hasAny(actor.getVariable(), new String[] { Roles.GM.getName() })) {
					nextVariable = Roles.ACC.getName();
				} else if (actor.getVariable().equals(Roles.ACC.getName())) {
					nextVariable = Roles.ASVI.getName();
				}
				else if (actor.getVariable().equals(Roles.ASVI.getName())) {
					nextVariable = Roles.CAS.getName();
				}
				else if (actor.getVariable().equals(Roles.CAS.getName())) {
					nextVariable = Roles.CSVI.getName();
				}
			}
			
			transApprove(borrowId, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, opinion);
			Code.ok(mb, "申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "申请审批失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object returned(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				dao.update(Borrow.class, Chain
						.make("returned", Status.ENABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("returned", "=", Status.DISABLED)
						.and("approve", "=", Status.OK)
						.and("borrowId", "in", arr));
			}
			Code.ok(mb, "归还借支申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:returned) error: ", e);
			Code.error(mb, "归还借支申请失败");
		}

		return mb;
	}
	
	private void transApprove(final Integer borrowId, final Integer approve, final Integer actorId, final String variable,
			final Integer refererId, final String nextVariable, final DateTime now, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				dao.update(BorrowActor.class, Chain
						.make("approve", approve)
						.add("refererId", refererId)
						.add("modifyTime", now.toDate())
						.add("opinion", opinion), Cnd
						.where("borrowId", "=", borrowId)
						.and("actorId", "=", actorId));
				dao.clear(BorrowActor.class, Cnd.where("borrowId", "=", borrowId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						dao.insert(new BorrowActor(borrowId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Status.value));
					self = refererId.equals(Value.I) ? Status.OK : Status.APPROVED;
				}
				
				dao.update(Borrow.class, Chain
						.make("approved", self)
						.add("modifyTime", now.toDate()), Cnd
						.where("borrowId", "=", borrowId));
			}
		});
	}
	
}