package cn.oa.web.action.hrm.regular;

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
import cn.oa.model.Archive;
import cn.oa.model.Regular;
import cn.oa.model.RegularActor;
import cn.oa.model.Role;
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

@IocBean(name = "hrm.regular.approve")
@At(value = "/hrm/regular/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);
	
	@GET
	@At
	@Ok("ftl:hrm/regular/approve_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/hrm/regular/approve/actors", token);
		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("r.status", "=", Status.ENABLED)
			.and("a.actor_id", "=", Context.getUserId());
		Cnds.gte(cri, mb, "r.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "r.create_time", "endTime", endStr);
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		cri.getOrderBy().desc("r.modify_time");
		
		Page<Regular> page = Webs.page(req);
		page = mapper.page(Regular.class, page, "RegularApprove.count", "RegularApprove.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			/*CSRF.validate(req);*/
			Integer resignId = Https.getInt(req, "resignId", R.REQUIRED, R.I, "申请ID");
			List<RegularActor> actors = mapper.query(RegularActor.class, "RegularActor.query", Cnd
					.where("a.resign_id", "=", resignId)
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
	@Ok("ftl:hrm/regular/approve_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer resignId = Https.getInt(req, "resignId", R.REQUIRED, R.I, "申请ID");
		Regular resign = mapper.fetch(Regular.class, "RegularApprove.query", Cnd
				.where("a.actor_id", "=", Context.getUserId())
				.and("r.resign_id", "=", resignId));
		Asserts.isNull(resign, "申请不存在");
		//获取审批人 
		List<RegularActor> actors = mapper.query(RegularActor.class, "RegularActor.query", Cnd
				.where("a.resign_id", "=", resignId)
				.asc("a.modify_time"));
		RegularActor actor = null; // 当前审批人员
		RegularActor next = null; // 指向下一审批人员
		
		Integer bindId = null;
		for (RegularActor e : actors) {
			if (e.getActorId().equals(Context.getUserId())) actor = e; // last
			if (e.getVariable().equals(Roles.MGR.getName())) bindId = e.getActorId();
		}
		if (actor.getRefererId() != 0) {
			for (RegularActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId())) next = e;
			}
		}
		List<User> operators = null;
		/*Integer[] actorIds = Converts.array(RegularActor.class, Integer.class, actors, "actorId");*/
		/*--------------++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++---------------*/
		//获取当前审批人角色名称
		Criteria cri = Cnd.cri();
		cri.where()
			.and("u.status", "=", Status.ENABLED)
			.and("u.user_id", "=", Context.getUserId());
		List<Role> page =  mapper.query(Role.class,"Role.query", cri);

		User user = new User();
		user = dao.fetch(User.class,resign.getUserId());		
		//如果申请人属于职能部门,135行政人事部,136市场部,121和德集团
		if(((135==user.getOrgId())||(136==user.getOrgId()))&&(121==user.getCorpId())){
			operators = userService.operators(null, Roles.PSVI.getName());
			for(Role roleName : page){
				if(Roles.PSVI.getName().equals(roleName.getRoleName())){
					operators = userService.operators(null, Roles.BOSS.getName());
				}
				/*if(Roles.FIS.getName().equals(roleName.getRoleName())){
					operators = userService.operators(null, Roles.BOSS.getName());
				}*/
				if(Roles.BOSS.getName().equals(roleName.getRoleName())){
					operators = null;
				}	
			}
		}else{
			for(String cRole : Context.getRoles()){
				if(cRole!=Roles.GM.getName())
					operators = userService.operators(null, Roles.GM.getName());
			}
			for(Role roleName : page){
				if(Roles.GM.getName().equals(roleName.getRoleName())){
					operators = userService.operators(null, Roles.PSVI.getName());
				}
				if(Roles.PSVI.getName().equals(roleName.getRoleName())){
					operators = userService.operators(null, Roles.BOSS.getName());
				}
				/*if(Roles.FIS.getName().equals(roleName.getRoleName())){
					operators = userService.operators(null, Roles.BOSS.getName());
				}*/
				if(Roles.BOSS.getName().equals(roleName.getRoleName())){
					operators = null;
				}	
			}
		}
		req.setAttribute("operators", operators);
		req.setAttribute("resign", resign);
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
		Integer resignId = null;
		try {
			CSRF.validate(req);
			
			resignId = Https.getInt(req, "resignId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1,2]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			
			DateTime now = new DateTime();
			Regular resign = mapper.fetch(Regular.class, "RegularApprove.query", Cnd
					.where("a.actor_id", "=", Context.getUserId())
					.and("r.resign_id", "=", resignId));
			Asserts.isNull(resign, "申请不存在");
			if(resign.getApproved().equals(Status.OK)||resign.getApproved().equals(Status.UNAPPROVED)){
				Code.error(mb, "审批已通过，不能再修改");
				return mb;
			}
			RegularActor actor = dao.fetch(RegularActor.class, Cnd
					.where("resignId", "=", resignId)
					.and("actorId", "=", Context.getUserId()));
			Asserts.isNull(actor, "禁止审批离职申请");
			
			//}
			Integer required = R.REQUIRED;	
			//如果当前审批人是超级管理员
			if (actor.getVariable().equals(Roles.ADR.getName())) {
				required = R.REQUIRED;
			}
			//
			if (actor.getRefererId() != 0) {
				RegularActor next = dao.fetch(RegularActor.class, Cnd
						.where("resignId", "=", resignId)
						.and("actorId", "=", actor.getRefererId()));
				if (next != null)
					Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的离职申请");
			}
			String nextVariable = null;
			/*--------------++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++---------------*/
			//获取上级审批人的数量(防止 财务主管审批 与 上级审批 职位重复)
			/*Integer count = dao.func(RegularActor.class, "count", "resignId", Cnd
					.where("resignId", "=", resignId));*/
			//决定流程是否结束的标记
			boolean endOrNor = false;
			nextVariable = null;	
			for(String cRole : Context.getRoles()){	
//				if(Roles.GM.getName().equals(cRole)){
//					nextVariable = Roles.PSVI.getName();
//				}
				if(Roles.PSVI.getName().equals(cRole)){
//					nextVariable = Roles.BOSS.getName();
					nextVariable = Roles.GM.getName();
				}
				/*if(Roles.FIS.getName().equals(cRole)){
					if(count<1)
						nextVariable = Roles.PSVI.getName();
					else
						nextVariable = Roles.BOSS.getName();	
				}*/
				if(Roles.BOSS.getName().equals(cRole) || Roles.GM.getName().equals(cRole)){					
					nextVariable = null;	
					required = R.CLEAN;
					endOrNor = true;
				}else{
					String[] nextRoles = userService.findRoleNames(Context.getUser().getManagerId());
					for(String nRole : nextRoles){
						if(Roles.BOSS.getName().equals(nRole)){
							nextVariable = Roles.BOSS.getName();
						}else if(Roles.GM.getName().equals(nRole)){
							nextVariable = Roles.GM.getName();
						}
					}
					if(nextVariable == null){
						nextVariable = Roles.MGR.getName();
					}
				}
			}
			if(approve==-1)
				required = R.CLEAN;
			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			refererId = Values.getInt(refererId);
			if(endOrNor){
				refererId = 0;
			}
			transApprove(resignId, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now,opinion);
			Code.ok(mb, "申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "申请审批失败");
		}

		return mb;
	}
	
	private void transApprove(final Integer resignId, final Integer approve, final Integer actorId, final String variable,
			final Integer refererId, final String nextVariable, final DateTime now, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				dao.update(RegularActor.class, Chain
						.make("approve", approve)
						.add("refererId", refererId)
						.add("opinion", opinion)
						.add("modifyTime", now.toDate()), Cnd
						.where("resignId", "=", resignId)
						.and("actorId", "=", actorId)
						.and("variable", "=", variable));
				dao.clear(RegularActor.class, Cnd.where("resignId", "=", resignId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						dao.insert(new RegularActor(resignId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(),"-"));
					self = refererId.equals(Value.I) ? Status.OK : Status.APPROVED;
				}
				
				dao.update(Regular.class, Chain
						.make("approved", self)
						.add("modifyTime", now.toDate()), Cnd
						.where("resignId", "=", resignId));
				//如果离职审批结束,则更改人事档案转正日期
				if(self.equals(Status.OK)){
					Regular resign = dao.fetch(Regular.class,resignId);
				/*Archive archive = dao.fetch(Archive.class,resign.getUserId());
				dao.update(archive);*/
				dao.update(Archive.class, Chain
						.make("full_date", resign.getResignDate()), Cnd
						.where("user_id", "=", resign.getUserId()));
				}
			}
		});
	}
	
}