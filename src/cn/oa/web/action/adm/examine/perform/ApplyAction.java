package cn.oa.web.action.adm.examine.perform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

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
import cn.oa.model.Perform;
import cn.oa.model.PerformActor;
import cn.oa.model.PerformModel;
import cn.oa.model.Release;
import cn.oa.model.Target;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Servlets;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.examine.perform.apply")
@At(value = "/adm/examine/perform/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);
	
	@GET
	@At
	@Ok("ftl:adm/examine/perform/apply_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/examine/perform/apply/del", token);
		CSRF.generate(req, "/adm/examine/perform/apply/nodes", token);
		CSRF.generate(req, "/adm/examine/perform/approve/actors", token);
		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("p.status", "=", Status.ENABLED)
			.and("p.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "p.approved", "approve", approve);
		Cnds.gte(cri, mb, "p.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "p.create_time", "endTime", endStr);
		cri.getOrderBy().desc("p.modify_time");
		
		Page<Perform> page = Webs.page(req);
		page = mapper.page(Perform.class, page, "Perform.count", "Perform.index", cri);

		Criteria cri2 = Cnd.cri();
		DateTime now = new DateTime();
		
		cri2.where().and("r.status", "=", Status.ENABLED)
					.and("r.corp_id", "=", Context.getCorpId());
		Cnds.lte(cri2, mb, "r.start_date", "startDate", now.toString(Calendars.DATE));
		Cnds.gte(cri2, mb, "r.end_date", "endDate", now.toString(Calendars.DATE));
		Release release = mapper.fetch(Release.class, "Release.query", cri2);		
	//	111111111111111111111
		/*Release release = dao.fetch(Release.class, Cnd
				.where("status", "=", Status.ENABLED)
				.and("corpId", "=", Context.getCorpId()));*/
		
		req.setAttribute("page", page);
		req.setAttribute("release", release);
		req.setAttribute("mb", mb);
	}

	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer performId = Https.getInt(req, "performId", R.REQUIRED, R.I, "绩效ID");
			List<Target> nodes = mapper.query(Target.class, "Target.query", Cnd
					.where("t.perform_id", "=", performId));
			mb.put("nodes", nodes);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:nodes) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:adm/examine/perform/apply_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		String token = CSRF.generate(req);
		CSRF.generate(req, "/coll/send/del", token);
		Integer performId = Https.getInt(req, "performId", R.REQUIRED, R.I);
		
		Perform perform = null;
		Release release = null;
		List<Target> targets = null;
		if (performId != null) {
			perform = mapper.fetch(Perform.class, "Perform.query", Cnd
					.where("p.user_id", "=", Context.getUserId())
					.and("p.perform_id", "=", performId));
			if (perform != null) {
				targets = dao.query(Target.class, Cnd.where("performId", "=", performId));
				Map<String, Object> map = new ConcurrentHashMap<String, Object>();
				map.put("perform_id", performId);
				
				release = dao.fetch(Release.class, perform.getReleaseId());
				Asserts.isNull(release, "发布绩效不存在");

				
				// 绩效审批
				PerformActor actor = mapper.fetch(PerformActor.class, "PerformActor.query", Cnd
						.where("performId", "=", performId)
						.and("variable", "=", Roles.FAP.getName())
						.limit(1)
						.asc("modifyTime"));
				
				if (actor != null) {
					perform.setActorId(actor.getActorId());
					perform.setActorName(actor.getActorName());
				}
			}
		}
		if (perform == null){
			perform = new Perform();
			release = dao.fetch(Release.class,Cnd.where("status", "=", Status.ENABLED).and("corp_id", "=", Context.getCorpId()));
		}
		Asserts.isNull(release, "没有发布绩效");
		
		if (Asserts.isEmpty(targets)) {
			targets = new ArrayList<Target>();
			targets.add(new Target());
		}
		Integer bindId = null;
		List<User> operators = new ArrayList<User>();
		List<PerformModel> list = mapper.query(PerformModel.class, "PerformModel.query", Cnd.where("m.corp_id", "=", Context.getCorpId()));
		PerformModel performmodel = null;
		for(PerformModel model : list){
			Integer[] arr = Converts.array(model.getModelUsers(), ",");
			Boolean boo = false;
			for(Integer userId : arr){
				if(userId.equals(Context.getUserId())){
					boo = true;
					performmodel = model;
					break;
				}
			}
			if(boo == true)
				break;
		}
		if(performmodel != null&&performmodel.getFirstReferer()!=null){
			operators.add(dao.fetch(User.class, performmodel.getFirstReferer()));
			bindId = performmodel.getFirstReferer();
		}
		
		req.setAttribute("operators", operators);
		req.setAttribute("perform", perform);
		req.setAttribute("targets", targets);
		req.setAttribute("release", release);
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
			
			performId = Https.getInt(req, "performId", R.I);

			Integer releaseId = Https.getInt(req, "releaseId", R.I, "绩效发布");
			Integer actorId = Https.getInt(req, "actorId", R.REQUIRED, R.I, "审批人员");

			Release release = dao.fetch(Release.class, releaseId);
			Asserts.isNull(release, "发布绩效不存在");
			
			Criteria cri = Cnd
					.where("releaseId", "=", releaseId)
					.and("userId", "=", Context.getUserId())
					.and("status", "=", Status.ENABLED);
			if (performId != null)
				cri.where().and("performId", "=", performId);
			
			if (dao.count(Perform.class, cri) > (performId != null ? 1 : 0)) throw new Errors("禁止重复申请绩效考核");
			
			DateTime now = new DateTime();
			Perform perform = null;
			if (performId != null) {
				/*perform = dao.fetch(Perform.class, Cnd
						.where("userId", "=", Context.getUserId())
						.and("performId", "=", performId));*/
				perform = mapper.fetch(Perform.class, "Perform.query", Cnd.where("u.user_id", "=", Context.getUserId()).and("p.perform_id", "=", performId));
				
				Asserts.isNull(perform, "绩效考核不存在");
				if ((!Asserts.hasAny(perform.getApproved(), new Integer[] {Status.PROOFING, Status.UNAPPROVED,3}))&&perform.getVersion().equals(Release.SCORE)) {
					throw new Errors("禁止编辑已审批的绩效考核");
				}
				if ((!Asserts.hasAny(perform.getApproved(), new Integer[] {Status.PROOFING, Status.UNAPPROVED}))&&perform.getVersion().equals(Release.WEIGHT)) {
					throw new Errors("禁止编辑已审批的绩效考核");
				}
			} else {
				perform = new Perform();
				perform.setCreateTime(now.toDate());
			}
			if(release.getStatus().equals(Status.DISABLED))
				throw new Errors("禁止编辑禁用的绩效考核");

			Map<String, Object> contentMap = Servlets.startsWith(req, "content_");
			Map<String, Object> gradeMap = Servlets.startsWith(req, "grade_");
			Map<String, Object> weightMap = Servlets.startsWith(req, "weight_");
			Map<String, Object> my_scoreMap = Servlets.startsWith(req, "my_score_");
			Map<String, Object> my_score_gradeMap = Servlets.startsWith(req, "my_score_grade_");
			Map<String, Object> scoreMap = Servlets.startsWith(req, "score_");
			Map<String, Object> score_gradeMap = Servlets.startsWith(req, "score_grade_");
			Map<String, Object> manscoreMap = Servlets.startsWith(req, "manscore_");
			Map<String, Object> manscore_gradeMap = Servlets.startsWith(req, "manscore_grade_");
			
			int weightSum = 0;
			
			for (Entry<String, Object> entry : contentMap.entrySet()) {
				String index = entry.getKey();
				Validator.validate(entry.getValue(), R.REQUIRED, R.RANGE, "{1,6000}", "考核目标");
				Validator.validate(gradeMap.get(index),  "评分标准");
				Validator.validate(weightMap.get(index), R.REQUIRED, R.I, "权重");
				Validator.validate(my_scoreMap.get(index), R.F, "本人评分");
				Validator.validate(my_score_gradeMap.get(index),  "评分标准");
				Validator.validate(scoreMap.get(index), R.F, "主管评分");
				Validator.validate(score_gradeMap.get(index),  "主管评价");
				Validator.validate(manscoreMap.get(index), R.F, "经理评分");
				Validator.validate(manscore_gradeMap.get(index), R.F, "经理评价");
				weightSum += Values.getInt(weightMap.get(index));
			}

			Asserts.notEq(weightSum, 100, "权重合计只能等于100");
			
			List<PerformModel> list = mapper.query(PerformModel.class, "PerformModel.query", Cnd.where("m.corp_id", "=", Context.getCorpId()));
			Integer modelId = 0;
			for(PerformModel model : list){
				Integer[] arr = Converts.array(model.getModelUsers(), ",");
				Boolean boo = false;
				for(Integer userId : arr){
					if(userId.equals(Context.getUserId())){
						boo = true;
						modelId = model.getModelId();
						break;
					}
				}
				if(boo == true)
					break;
			}
			Asserts.Eq(modelId,0,"用户绩效模板不能为空");
			
			StringBuilder buff = new StringBuilder();
			buff.append("绩效考核_")
				.append(Context.getUsername())
				.append("于")
				.append(new DateTime(release.getReleaseStartDate()).toString(Calendars.DATE))
				.append("至")
				.append(new DateTime(release.getReleaseEndDate()).toString(Calendars.DATE))
				.append("考核");
			
			perform.setReleaseId(releaseId);
			perform.setUserId(Context.getUserId());
			perform.setSubject(buff.toString());
			perform.setApproved(Status.PROOFING);
			perform.setStatus(Status.ENABLED);
			perform.setModifyTime(now.toDate());
			perform.setModelId(modelId);

			transSave(release.getVersion(), performId, perform, actorId, contentMap, gradeMap,  weightMap, my_scoreMap, my_score_gradeMap, scoreMap,score_gradeMap, manscoreMap,manscore_gradeMap);
			
			Code.ok(mb, (performId == null ? "新建" : "编辑") + "绩效考核成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (performId == null ? "新建" : "编辑") + "绩效考核失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object del(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				dao.update(Perform.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("approved", "=", Status.PROOFING)
						.and("performId", "in", arr));
			}
			Code.ok(mb, "删除绩效考核成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除绩效考核失败");
		}

		return mb;
	}
	
	private void transSave(final Integer version,
			final Integer performId, final Perform perform, final Integer actorId,
			final Map<String, Object> contentMap, 
			final Map<String, Object> gradeMap, 
			final Map<String, Object> weightMap, 
			final Map<String, Object> my_scoreMap, 
			final Map<String, Object> my_score_gradeMap, 
			final Map<String, Object> scoreMap,
			final Map<String, Object> score_gradeMap,
			final Map<String, Object> manscoreMap,
			final Map<String, Object> manscore_gradeMap
			) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = performId;
				if (performId != null) {
					dao.clear(PerformActor.class, Cnd.where("performId", "=", performId));	
					/*if (version.equals(Release.WEIGHT)) {
						dao.clear(PerformActor.class, Cnd.where("performId", "=", performId));						
					} else {
						dao.update(PerformActor.class, Chain.make("approve", Status.OK), Cnd.where("performId", "=", performId));
					}*/
					dao.update(perform);
				} else {
					id = dao.insert(perform).getPerformId();
				}
				
				// 绩效目标
				dao.clear(Target.class, Cnd.where("performId", "=", id));
				List<Target> targets = new ArrayList<Target>();
				for (Entry<String, Object> entry : contentMap.entrySet()) {
					String index = entry.getKey();
					String content = (String) entry.getValue();
					String grade = (String) gradeMap.get(index);		
					Integer weight = Values.getInt(weightMap.get(index));
					Double my_score = Values.getDouble(my_scoreMap.get(index));
					String my_score_grade = (String) my_score_gradeMap.get(index);
					Double score = Values.getDouble(scoreMap.get(index));
					String score_grade = (String) score_gradeMap.get(index);
					Double manscore = Values.getDouble(manscoreMap.get(index));
					String manscore_grade = (String) manscore_gradeMap.get(index);
					targets.add(new Target(id, content,grade, weight, my_score,my_score_grade, score,score_grade, manscore,manscore_grade));
				}
				dao.fastInsert(targets);
				
				// 指定绩效审批
				dao.clear(PerformActor.class, Cnd.where("performId", "=", performId).and("actorId", "=", actorId));
				dao.insert(new PerformActor(id, actorId, Value.I, Status.PROOFING, Roles.FAP.getName(), perform.getModifyTime(), Status.value));
			}
		});
	}
}