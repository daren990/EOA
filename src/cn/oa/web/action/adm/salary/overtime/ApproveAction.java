package cn.oa.web.action.adm.salary.overtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
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
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Dict;
import cn.oa.model.Overtime;
import cn.oa.model.OvertimeActor;
import cn.oa.model.OvertimeRest;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.model.WorkAttendance;
import cn.oa.service.OvertimeService;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
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

/**
 * 加班审批
 * 
 * @author SimonTang
 */
@IocBean(name = "adm.salary.overtime.approve")
@At(value = "/adm/salary/overtime/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);
	
	@Inject
	private OvertimeService overtimeService;

	@GET
	@At
	@Ok("ftl:adm/salary/overtime/approve_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}

	@GET
	@At
	@Ok("ftl:adm/salary/overtime/approve_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/salary/overtime/approve/able", token);
		CSRF.generate(req, "/adm/salary/overtime/approve/actors", token);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where().and("ot.status", "=", Status.ENABLED).and("a.actor_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "ot.approve", "approve", approve);
		Cnds.gte(cri, mb, "ot.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "ot.create_time", "endTime", endStr);
		cri.getOrderBy().desc("ot.modify_time");

		Page<Overtime> page = Webs.page(req);
		page = mapper.page(Overtime.class, page, "Overtime.count", "Overtime.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	/**
	 * 加班审批页
	 * 
	 * @param req
	 */
	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer overtimeId = Https.getInt(req, "overtimeId", R.REQUIRED, R.I, "申请ID");

		Overtime overtime = mapper.fetch(Overtime.class, "Overtime.query",
				Cnd.where("ot.status", "=", Status.ENABLED).and("a.actor_id", "=", Context.getUserId()).and("ot.overtime_id", "=", overtimeId));
		Asserts.isNull(overtime, "申请不存在");
		req.setAttribute("overtimeMap", dictService.map(Dict.OVERTIME));

		// 获取审批人
		List<OvertimeActor> actors = mapper.query(OvertimeActor.class, "OvertimeActor.query", Cnd.where("a.overtime_id", "=", overtimeId).asc("a.modify_time"));
		OvertimeActor actor = null; // 当前审批人员
		OvertimeActor next = null; // 指向下一审批人员

		Integer bindId = null;
		for (OvertimeActor e : actors) {
			if (e.getActorId().equals(Context.getUserId()))
				actor = e; // last
			if (e.getVariable().equals(Roles.MGR.getName()))
				bindId = e.getActorId();
		}
		List<User> operators = null;
		boolean toWho = false;
		// 如果申请人属于职能部门,135行政人事部,136财务部,121和德集团
		User applyUser = dao.fetch(User.class, overtime.getUserId());
		if (121 == applyUser.getCorpId()) {
			toWho = true;
		}
		/*
		 * if(((135==user.getOrgId())||(136==user.getOrgId()))&&(121==user.getCorpId
		 * ())){ toWho = true; }
		 */
		// 获取当前审批人角色名称
		Criteria cri = Cnd.cri();
		cri.where().and("u.status", "=", Status.ENABLED).and("u.user_id", "=", Context.getUserId());
		List<Role> curRoles = mapper.query(Role.class, "Role.query", cri);
		for (Role roleName : curRoles) {
			if (Roles.GM.getName().equals(roleName.getRoleName()) || Roles.BOSS.getName().equals(roleName.getRoleName())) {
				toWho = true;
				break;
			}
		}
		if (!toWho) {
			operators = userService.operators(Roles.GM.getName());
		}

		// 如果不属于职能部门
		req.setAttribute("operators", operators);
		req.setAttribute("overtime", overtime);
		req.setAttribute("actors", actors);
		req.setAttribute("actor", actor);
		req.setAttribute("next", next);
		req.setAttribute("bindId", bindId);
	}

	@GET
	@At
	@Ok("ftl:adm/salary/overtime/approve_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}

	@GET
	@At
	@Ok("ftl:adm/salary/overtime/approve_add_wx")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}

	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer overtimeId = Https.getInt(req, "overtimeId", R.REQUIRED, R.I, "申请ID");
			List<OvertimeActor> actors = mapper.query(OvertimeActor.class, "OvertimeActor.query", Cnd.where("a.overtime_id", "=", overtimeId).asc("a.modify_time"));
			mb.put("actors", actors);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:actors) error: ", e);
		}
		return mb;
	}

	/**
	 * 审批提交
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer overtimeId = Https.getInt(req, "overtimeId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");

			Overtime overtime = mapper.fetch(Overtime.class, "Overtime.query",
					Cnd.where("ot.status", "=", Status.ENABLED).and("a.actor_id", "=", Context.getUserId()).and("ot.overtime_id", "=", overtimeId));
			Asserts.isNull(overtime, "申请不存在");

			/*WorkAttendance att = dao.fetch(WorkAttendance.class, overtime.getCorpId());
			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());

			DateTime start = new DateTime(overtime.getStartTime());

			if (start.isBefore(pos))
				throw new Errors("考勤结束日期禁止审批");*/

			DateTime now = new DateTime();

			boolean toWho = false;
			// 如果申请人属于职能部门,135行政人事部,136财务部,121和德集团
			User applyUser = dao.fetch(User.class, overtime.getUserId());
			if (121 == applyUser.getCorpId()) {
				toWho = true;
			}
			// 获取当前审批人角色名称
			Criteria cri = Cnd.cri();
			cri.where().and("u.status", "=", Status.ENABLED).and("u.user_id", "=", Context.getUserId());
			List<Role> curRoles = mapper.query(Role.class, "Role.query", cri);
			for (Role roleName : curRoles) {
				if ("g.manager".equals(roleName.getRoleName()) || "boss".equals(roleName.getRoleName())) {
					toWho = true;
					break;
				}
			}
			
			Integer referId = 0;// 下一级审批人
			if (!toWho && (approve == Status.APPROVED)) {
				referId = Https.getInt(req, "refererId", R.REQUIRED, "下级审批");
			}
			Map<String, String[]> remarkMap = new HashMap<String, String[]>();
			overtimeService.isLegalApprove(overtime, remarkMap, referId, approve);
			
			/*if (referId == 0 && (overtime.getApprove().equals(Status.OK) || //
					overtime.getApprove().equals(Status.UNAPPROVED) || //
					overtime.getApprove().equals(Status.APPROVED))) {
				Code.error(mb, "审批已通过，不能再修改");
				return mb;
			}*/
			overtime.setApprove(approve);
			overtime.setModifyTime(now.toDate());
			overtime.setOpinion(opinion);
			transSave(overtime, approve, Context.getUserId(), referId, remarkMap);
			Code.ok(mb, "加班申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "加班申请审批失败");
		}

		return mb;
	}

	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");

			if (arr != null && arr.length > 0) {
				DateTime now = new DateTime();

				List<WorkAttendance> atts = dao.query(WorkAttendance.class, null);
				Map<Integer, WorkAttendance> attMap = new ConcurrentHashMap<Integer, WorkAttendance>();
				for (WorkAttendance att : atts)
					attMap.put(att.getOrgId(), att);

				List<Overtime> overtimes = mapper.query(Overtime.class, "Overtime.query",
						Cnd.where("ot.status", "=", Status.ENABLED).and("ot.operator_id", "=", Context.getUserId()).and("ot.overtime_id", "in", arr));
				for (Overtime overtime : overtimes) {
					WorkAttendance att = attMap.get(overtime.getCorpId());
					Asserts.isNull(att, "最近考勤周期未配置");
					DateTime pos = new DateTime(att.getEndDate());

					DateTime start = new DateTime(overtime.getStartTime());
					if (start.isBefore(pos))
						continue;
					save(overtime, approve, Context.getUserId(), now, Status.value);
				}
			}
			Code.ok(mb, "加班申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:able) error: ", e);
			Code.error(mb, "加班申请审批失败");
		}

		return mb;
	}

	private void save(Overtime overtime, Integer approve, Integer modifyId, DateTime now, String opinion) {
		if (overtime.getApprove().equals(approve) && overtime.getOpinion().equals(opinion))
			return;
		overtime.setApprove(approve);
		overtime.setModifyTime(now.toDate());
		overtime.setApprove(approve);
		overtime.setModifyTime(now.toDate());
		overtime.setOpinion(opinion);
		dao.update(overtime);
	}

	/**
	 * 
	 * @param overtime
	 * @param approve
	 * @param modifyId 当前审批人
	 * @param referId 下一级审批人
	 */
	private void transSave(final Overtime overtime, final Integer approve, final Integer modifyId, final Integer referId, final Map<String, String[]> remarkMap) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.update(OvertimeActor.class,
						Chain.make("approve", approve).add("opinion", overtime.getOpinion()).add("referer_id", referId).add("modifyTime", overtime.getModifyTime()),
						Cnd.where("overtime_id", "=", overtime.getOvertimeId()).and("actor_id", "=", modifyId));
				dao.clear(OvertimeActor.class, Cnd.where("overtime_id", "=", overtime.getOvertimeId()).and("approve", "=", Status.PROOFING));
				if (referId == 0) {
						
					dao.update(overtime);
					if (overtime.getApprove() == Status.APPROVED) {
						OvertimeRest overtimeRest = new OvertimeRest();
						overtimeRest.setCreteDateTime(new Date());
						overtimeRest.setType(Status.TYPE_OVERTIME);
						overtimeRest.setUserid(overtime.getUserId());
						overtimeRest.setWorkMinute(overtime.getWorkMinute());
						overtimeRest.setStartTime(overtime.getStartTime());
						dao.insert(overtimeRest);
					} else if (overtime.getApprove() == Status.UNAPPROVED) {
						// 不批准，判断是否是二次审批，如果是删除OvertimeRest
						List<OvertimeRest> ovs = dao.query(OvertimeRest.class, Cnd.where("userid", "=", overtime.getUserId())//
								.and("start_time", "=", overtime.getStartTime())//
								.and("work_minute", "=", overtime.getWorkMinute()));
						if (ovs != null && ovs.size() > 0) {
							dao.delete(ovs.get(0));
						}
					}
					checkedRecordService.update3(overtime.getUserId(), modifyId, remarkMap);
					
				} else {
					User u = dao.fetch(User.class, Cnd.where("user_id", "=", referId));
					List<String> maiList = new ArrayList<String>();
					if (StringUtils.isNotBlank(u.getEmail())) {
						maiList.add(u.getEmail());
					}
					MailStart mail = new MailStart();
					mail.mail(maiList, overtime.getSubject(), overtime.getContent());
					DateTime motifyTime = Calendars.parse(overtime.getModifyTime(), Calendars.DATE_TIMES).plusSeconds(1);
					dao.insert(new OvertimeActor(overtime.getOvertimeId(), referId, Value.I, Status.PROOFING, Roles.GM.getName(), motifyTime.toDate(), "-"));
				}
			}
		});
	}
}
