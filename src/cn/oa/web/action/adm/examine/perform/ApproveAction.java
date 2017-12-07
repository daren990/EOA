package cn.oa.web.action.adm.examine.perform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import cn.oa.model.LeaveActor;
import cn.oa.model.Perform;
import cn.oa.model.PerformActor;
import cn.oa.model.PerformModel;
import cn.oa.model.Release;
import cn.oa.model.Target;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Servlets;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.examine.perform.approve")
@At(value = "/adm/examine/perform/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);

	@GET
	@At
	@Ok("ftl:adm/examine/perform/approve_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/examine/perform/apply/nodes", token);
		CSRF.generate(req, "/adm/examine/perform/approve/actors", token);

		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);
		Integer approve = Https.getInt(req, "approve", R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where().and("p.status", "=", Status.ENABLED).and("a.actor_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		Cnds.gte(cri, mb, "p.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "p.create_time", "endTime", endStr);
		cri.getOrderBy().desc("p.modify_time");

		Page<Perform> page = Webs.page(req);
		page = mapper.page(Perform.class, page, "PerformApprove.count", "PerformApprove.index", cri);

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
			Integer performId = Https.getInt(req, "performId", R.REQUIRED, R.I, "绩效ID");
			List<PerformActor> actors = mapper.query(PerformActor.class, "PerformActor.query",
					//11.22改
					Cnd.where("a.perform_id", "=", performId).and("a.approve", "!=", Status.OK).asc("a.modify_time"));
		  //      	Cnd.where("a.perform_id", "=", performId).and("a.approve", "!=", Status.PROOFING).asc("a.modify_time"));
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
	@Ok("ftl:adm/examine/perform/approve_add")
	public void add(HttpServletRequest req, Object approve) {
		CSRF.generate(req);
		// REQUIRED = 1
		Integer performId = Https.getInt(req, "performId", R.REQUIRED, R.I, "绩效ID");

		Perform perform = mapper.fetch(Perform.class, "PerformApprove.query", Cnd.where("a.actor_id", "=", Context.getUserId()).and("p.perform_id", "=", performId));
		Asserts.isNull(perform, "绩效不存在");

		Release release = dao.fetch(Release.class, perform.getReleaseId());
		Asserts.isNull(release, "发布绩效不存在");

		List<Target> targets = dao.query(Target.class, Cnd.where("performId", "=", performId));
		Asserts.isEmpty(targets, "绩效目标不存在");

		PerformModel model = dao.fetch(PerformModel.class, perform.getModelId());
		Asserts.isNull(model, "该考核用户绩效模板不存在");

		List<PerformActor> actors = mapper.query(PerformActor.class, "PerformActor.query",
				Cnd.where("a.perform_id", "=", performId).and("a.approve", "!=", Status.OK).asc("a.modify_time"));
		

		
		PerformActor actor = null; // 当前审批人员
		PerformActor next = null; // 指向下一审批人员
	

		Integer bindId = null;
		for (PerformActor e : actors) {
			if (e.getActorId().equals(Context.getUserId()))
				actor = e; // last
			if (e.getVariable().equals(Roles.SAP.getName()))
				bindId = e.getActorId();
		}
		if (actor.getRefererId() != 0) {
			for (PerformActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId()))
					next = e;
			}
		}
	
		Asserts.isNull(actor, "当前审批人员不存在");

		List<User> operators = new ArrayList<User>();
		
		Integer approve1 = R.REQUIRED;
		if (actor.getVariable().equals(Roles.Me.getName()))
			for (PerformActor e : actors) {
				if (actor.getVariable().equals(Roles.Me.getName()) && (approve1==Status.UNAPPROVED))
					dao.update(PerformActor.class, Chain.make("approve", Status.PROOFING), Cnd.where("performId", "=", performId));
			}

		/*
		 * if (actor.getVariable().equals(Roles.PFM.getName())) { if
		 * (release.getVersion().equals(Release.WEIGHT)) { User user =
		 * dao.fetch(User.class, perform.getUserId()); operators =
		 * userService.operators(Context.getCorpId(), user.getLevel()); bindId =
		 * user.getManagerId(); } else { PerformActor operator =
		 * mapper.fetch(PerformActor.class, "PerformActor.query", Cnd
		 * .where("performId", "=", performId) .and("variable", "=",
		 * Roles.MGR.getName()) .and("approve", "=", Status.OK) .limit(1)
		 * .asc("modifyTime")); User manager = new User();
		 * manager.setUserId(operator.getActorId());
		 * manager.setTrueName(operator.getActorName()); operators = new
		 * ArrayList<User>(); operators.add(manager); bindId =
		 * manager.getUserId(); } }
		 */

		/*
		 * if (actor.getVariable().equals(Roles.MGR.getName())) { if
		 * (release.getVersion().equals(Release.WEIGHT)) {
		 * if(model.getSecondReferer()!=null && model.getSecondStep()!=null){
		 * operators.add(dao.fetch(User.class,model.getSecondReferer())); } //
		 * operators = userService.operators(Context.getCorpId(),
		 * Roles.PFM.getName()); bindId = model.getSecondReferer(); } else {
		 * PerformActor operator = mapper.fetch(PerformActor.class,
		 * "PerformActor.query", Cnd .where("performId", "=", performId)
		 * .and("variable", "=", Roles.PFM.getName()) .and("approve", "=",
		 * Status.OK) .limit(1) .asc("modifyTime")); if (operator != null) {
		 * User manager = new User(); manager.setUserId(operator.getActorId());
		 * manager.setTrueName(operator.getActorName()); operators.add(manager);
		 * bindId = manager.getUserId(); } } }
		 */
		User applyUser = dao.fetch(User.class, perform.getUserId());
		User firstUser = dao.fetch(User.class, model.getFirstReferer());
		User secondUser = null;
		if (model.getSecondReferer() != null) {
			secondUser = dao.fetch(User.class, model.getSecondReferer());
		}

		if (actor.getVariable().equals(Roles.Me.getName())) {
			if (release.getVersion().equals(Release.SCORE)) {
				operators.add(firstUser);
				bindId = model.getFirstReferer();
			}
		}
		if (actor.getVariable().equals(Roles.FAP.getName())) {
			if (release.getVersion().equals(Release.WEIGHT)) {
				if (model.getSecondReferer() != null && model.getSecondStep() != null) {
					operators.add(secondUser);
					bindId = model.getSecondReferer();
				} else {
					operators.add(applyUser);
					bindId = perform.getUserId();
				}
			} else {
				if (model.getSecondReferer() != null && model.getSecondStep() != null) {
					operators.add(secondUser);
					bindId = model.getSecondReferer();
				} else {
					operators.add(applyUser);
					bindId = perform.getUserId();
				}
			}
		}
		if (actor.getVariable().equals(Roles.SAP.getName())) {

			operators.add(applyUser);
			bindId = perform.getUserId();

		}
		Boolean required = true;
		if (perform.getVersion().equals(Release.WEIGHT)) {
			// if(actor.getVariable().equals(Roles.SAP.getName()))required =
			// false;
			// if(actor.getVariable().equals(Roles.FAP.getName())&&model.getSecondReferer()==null)
			// required = false;
			if (actor.getVariable().equals(Roles.Me.getName()))
				required = false;
		} else if (actor.getVariable().equals(Roles.Me.getName()))
			required = false;

		req.setAttribute("required", required);
		req.setAttribute("release", release);
		req.setAttribute("operators", operators);
		req.setAttribute("perform", perform);
		req.setAttribute("targets", targets);
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
		Integer performId = null;
		try {
			CSRF.validate(req);
            //REQUIRED==1
			performId = Https.getInt(req, "performId", R.REQUIRED, R.I, "绩效ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");

			DateTime now = new DateTime();
			Perform perform = mapper.fetch(Perform.class, "PerformApprove.query", Cnd.where("a.actor_id", "=", Context.getUserId()).and("p.perform_id", "=", performId));
			Asserts.isNull(perform, "绩效不存在");
			
			Release release = dao.fetch(Release.class, perform.getReleaseId());
			Asserts.isNull(release, "发布绩效不存在");

			PerformActor actor = dao.fetch(PerformActor.class, Cnd.where("performId", "=", performId).and("actorId", "=", Context.getUserId()));
			System.out.println(actor);
			Asserts.isNull(actor, "禁止审批绩效考核");

			PerformModel model = dao.fetch(PerformModel.class, perform.getModelId());
			Asserts.isNull(model, "该考核用户绩效模板不存在");

			Integer required = R.REQUIRED;
			if (actor.getVariable().equals(Roles.Me.getName()))
				required = R.CLEAN;
			
			
	
			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			refererId = Values.getInt(refererId);
			if (perform.getApproved() != null&&refererId==0)
				if (perform.getApproved().equals(Status.OK)) {
					throw new Errors("审批已通过，不能再修改");
				}
			/*
			 * if(perform.getVersion().equals(Release.WEIGHT)&&required.equals(R.
			 * CLEAN)){ refererId = perform.getUserId(); }
			 */
		

			Map<String, Object> contentMap = Servlets.startsWith(req, "content_");
			Map<String, Object> gradeMap = Servlets.startsWith(req, "grade_");
			Map<String, Object> weightMap = Servlets.startsWith(req, "weight_");
			Map<String, Object> my_scoreMap = Servlets.startsWith(req, "my_score_");
			Map<String, Object> my_score_gradeMap = Servlets.startsWith(req, "my_score_grade_");
			Map<String, Object> scoreMap = Servlets.startsWith(req, "score_");
			Map<String, Object> score_gradeMap = Servlets.startsWith(req, "score_grade_");
			Map<String, Object> manscoreMap = Servlets.startsWith(req, "manscore_");
			Map<String, Object> manscore_gradeMap = Servlets.startsWith(req, "manscore_grade_");

			if (perform.getVersion().equals(Release.SCORE))
				for (Entry<String, Object> entry : scoreMap.entrySet()) {
					String index = entry.getKey();
					Validator.validate(scoreMap.get(index), R.CLEAN, R.F, "主管评分");
					Validator.validate(manscoreMap.get(index), R.CLEAN, R.F, "经理评分");
					Validator.validate(my_scoreMap.get(index), R.CLEAN, R.F, "本人评分");
				}

			if (actor.getRefererId() != 0) {
				PerformActor next = dao.fetch(PerformActor.class, Cnd.where("performId", "=", performId).and("actorId", "=", actor.getRefererId()));
				if (next != null)
					Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的绩效考核");
			}
			if (release.getStatus().equals(Status.DISABLED))
				throw new Errors("禁止编辑禁用的绩效考核");

			String nextVariable = null;
			Boolean end = false;
			/*
			 * if (actor.getVariable().equals(Roles.PFM.getName())) nextVariable
			 * = Roles.MGR.getName(); if
			 * (actor.getVariable().equals(Roles.MGR.getName())) nextVariable =
			 * Roles.Me.getName();
			 */
			if (actor.getVariable().equals(Roles.FAP.getName())) {
				if (!(model.getSecondReferer() == null && model.getSecondStep() == null)) {
					nextVariable = Roles.SAP.getName();
				} else {
					nextVariable = Roles.Me.getName();
				}
			}
			if (actor.getVariable().equals(Roles.SAP.getName()))
				nextVariable = Roles.Me.getName();

			if (actor.getVariable().equals(Roles.Me.getName()))
				nextVariable = Roles.Me.getName();

			if (actor.getVariable().equals(Roles.SAP.getName()) || (model.getSecondReferer() == null && model.getSecondStep() == null))
				end = true;

			transApprove(release.getVersion(), performId, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, contentMap, gradeMap,weightMap, my_scoreMap,my_score_gradeMap,
					scoreMap,score_gradeMap, manscoreMap,manscore_gradeMap, opinion, end);

			Code.ok(mb, "绩效审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "绩效审批失败");
		}

		return mb;
	}

	private void transApprove(final Integer version, final Integer performId, final Integer approve, final Integer actorId, final String variable, final Integer refererId,
			final String nextVariable, final DateTime now, final Map<String, Object> contentMap, final Map<String, Object> gradeMap,final Map<String, Object> weightMap, final Map<String, Object> my_scoreMap,
			final Map<String, Object> my_score_gradeMap,final Map<String, Object> scoreMap, final Map<String, Object> score_gradeMap,final Map<String, Object> manscoreMap,final Map<String, Object> manscore_gradeMap, final String opinion, final Boolean end) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				if (approve.equals(Status.UNAPPROVED)){
					dao.update(PerformActor.class, Chain.make("approve", -1).add("refererId",actorId).add("modifyTime", now.toDate()).add("opinion", opinion),
							Cnd.where("performId", "=", performId).and("actorId", "=", actorId));
					dao.update(PerformActor.class, Chain.make("approve", 0).add("refererId", 0).add("modifyTime", now.toDate()).add("opinion", opinion),
							Cnd.where("performId", "=", performId).and("refererId","!=",Value.I));
					
					
				}else {
					dao.update(PerformActor.class, Chain.make("approve", approve).add("refererId", refererId).add("modifyTime", now.toDate()).add("opinion", opinion),
							Cnd.where("performId", "=", performId).and("actorId", "=", actorId));
				}
			//	dao.clear(PerformActor.class, Cnd.where("performId", "=", performId).and("approve", "=", Status.PROOFING));
				
				
				
				
				if (approve.equals(Status.APPROVED)) {           
					//判断指定下一审批人的id     I==0；   i不为0  到指定的审批人
					if (!refererId.equals(Value.I)) {
						dao.clear(PerformActor.class, Cnd.where("performId", "=", performId).and("actorId", "=", refererId));
						// 下一级审批人是申请人,默认已批准.weight==0    ME是本人
						if (Roles.Me.getName().equals(nextVariable) && version.equals(Release.WEIGHT)) {
						//	approved==1   已批准        i==0;
							dao.insert(new PerformActor(performId, refererId, Value.I, Status.APPROVED, nextVariable, now.plusSeconds(1).toDate(), Status.value));
						//weight==0   Release  发布的id=0(有发布的情况， rease id为0 没有下级审核人 )
							if (version.equals(Release.WEIGHT)) {
								self = 3; // 流程完成1/2
							} else {
								self = Status.OK;
							}
						} else {
							dao.insert(new PerformActor(performId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Status.value));
						//CHECKING = 2;		// 核对            CHECKING = 2(状态只有0和1);// 核对   if  true 2    flash  1；1(启动状态)   
							self = end.equals(true) ? Status.CHECKING : Status.APPROVED;
						}
					} else {// 没有下级审批人
						if (version.equals(Release.WEIGHT)) {
							self = 3; // 流程完成1/2
						} else {
							self = Status.OK;
						}
					}
				}

				dao.update(Perform.class, Chain.make("approved", self).add("modifyTime", now.toDate()), Cnd.where("performId", "=", performId));

				// 上级评分

				dao.clear(Target.class, Cnd.where("performId", "=", performId));
				List<Target> targets = new ArrayList<Target>();
				for (Entry<String, Object> entry : contentMap.entrySet()) {
					String index = entry.getKey();
					String content = (String) entry.getValue();
					String grade = (String) entry.getValue();
					Integer weight = Values.getInt(weightMap.get(index));
					Double my_score = Values.getDouble(my_scoreMap.get(index));
					String my_score_grade = (String) entry.getValue();
					Double score = Values.getDouble(scoreMap.get(index));
					String score_grade = (String) entry.getValue();
					Double manscore = Values.getDouble(manscoreMap.get(index));
					String manscore_grade = (String) entry.getValue();
					targets.add(new Target(performId, content,grade, weight, my_score,my_score_grade, score,score_grade, manscore,manscore_grade));
				}
				dao.fastInsert(targets);

			}
		});
	}
}