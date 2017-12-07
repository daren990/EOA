package cn.oa.web.action.adm.expense;

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
import cn.oa.model.Project;
import cn.oa.model.ProjectActor;
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
@IocBean(name="adm.expense.approve")
@At(value = "/adm/expense/approve")
public class ApproveAction extends Action{
	public static Log log = Logs.getLog(ApproveAction.class);
	
	@GET
	@At
	@Ok("ftl:adm/expense/approve_page")
	public void page(HttpServletRequest req){
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/expense/approve/actors", token);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("a.actor_id", "=", Context.getUserId());
		Cnds.gte(cri, mb, "p.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "p.create_time", "endTime", endStr);
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		cri.getOrderBy().desc("p.modify_time");
		
		Page<Project> page = Webs.page(req);
		page = mapper.page(Project.class, page, "ProjectApprove.count", "ProjectApprove.index", cri);

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
			Integer projectId = Https.getInt(req, "projectId", R.REQUIRED, R.I, "申请ID");
			List<ProjectActor> actors = mapper.query(ProjectActor.class, "ProjectActor.query", Cnd
					.where("a.project_id", "=", projectId)
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
	@Ok("ftl:adm/expense/approve_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer projectId = Https.getInt(req, "projectId", R.REQUIRED, R.I, "申请ID");
		Project project = mapper.fetch(Project.class, "ProjectApprove.query", Cnd
				.where("a.actor_id", "=", Context.getUserId())
				.and("p.project_id", "=", projectId));
		Asserts.isNull(project, "申请不存在");

		List<ProjectActor> actors = mapper.query(ProjectActor.class, "ProjectActor.query", Cnd
				.where("a.project_id", "=", projectId)
				.asc("a.modify_time"));
		ProjectActor actor = null; // 当前审批人员
		ProjectActor next = null; // 指向下一审批人员
		
		Integer bindId = null;
		for (ProjectActor e : actors) {
			if (e.getActorId().equals(Context.getUserId())) actor = e;
			if (e.getVariable().equals(Roles.GM.getName())) bindId = e.getActorId();
		}
		if (actor.getRefererId() != 0) {
			for (ProjectActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId())) next = e;
			}
		}
		List<User> operators = null;
			operators =userService.operators(Roles.BOSS.getName());
		if(operators.size()==0)
			operators = new ArrayList<User>();
		
		req.setAttribute("operators", operators);
		req.setAttribute("project", project);
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
		Integer projectId = null;
		try {
			CSRF.validate(req);
			
			projectId = Https.getInt(req, "projectId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1,2]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			
			DateTime now = new DateTime();
			Project project = mapper.fetch(Project.class, "ProjectApprove.query", Cnd
					.where("a.actor_id", "=", Context.getUserId())
					.and("p.project_id", "=", projectId));
			Asserts.isNull(project, "申请不存在");

			ProjectActor actor = dao.fetch(ProjectActor.class, Cnd
					.where("projectId", "=", projectId)
					.and("actorId", "=", Context.getUserId()));
			Asserts.isNull(actor, "禁止审批预算项目申请");
			
			Integer required = R.CLEAN;
			if (!actor.getVariable().equals(Roles.BOSS.getName())) {
				required = R.REQUIRED;
			}
			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			refererId = Values.getInt(refererId);
			
			if (actor.getRefererId() != 0) {
				ProjectActor next = dao.fetch(ProjectActor.class, Cnd
						.where("projectId", "=", projectId)
						.and("actorId", "=", actor.getRefererId()));
				if (next != null)
					Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的预算申请");
			}
			String nextVariable = null;
			if (approve.equals(Status.APPROVED)) {
				if (actor.getVariable().equals(Roles.GM.getName())) {
					nextVariable = Roles.BOSS.getName();
			}
		}
			transApprove(projectId, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, opinion);
			Code.ok(mb, "申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "申请审批失败");
		}

		return mb;
	}

	private void transApprove(final Integer projectId, final Integer approve,
			final Integer actorId,final String variable,final Integer refererId,
			final String nextVariable,final DateTime now, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				dao.update(ProjectActor.class, Chain
						.make("approve", approve)
						.add("refererId", refererId)
						.add("modifyTime", now.toDate())
						.add("opinion", opinion), Cnd
						.where("projectId", "=", projectId)
						.and("actorId", "=", actorId));
				dao.clear(ProjectActor.class, Cnd.where("projectId", "=", projectId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						dao.insert(new ProjectActor(projectId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Status.value));
					self = refererId.equals(Value.I) ? Status.OK : Status.APPROVED;
				}
				
				dao.update(Project.class, Chain
						.make("approved", self)
						.add("modifyTime", now.toDate()), Cnd
						.where("projectId", "=", projectId));
			}
		});
		
	}
		
}
