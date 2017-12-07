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
import cn.oa.model.Borrow;
import cn.oa.model.BorrowActor;
import cn.oa.model.Project;
import cn.oa.model.ProjectActor;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.RMB;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.expense.project")
@At(value = "/adm/expense/project")
public class ProjectAction extends Action {

	public static Log log = Logs.getLog(ProjectAction.class);
	
	@GET
	@At
	@Ok("ftl:adm/expense/project_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/expense/project/able", token);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.getOrderBy().desc("p.modify_time");
		
		Page<Project> page = Webs.page(req);
		page = mapper.page(Project.class, page, "Project.count", "Project.index", cri);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@GET
	@At
	@Ok("ftl:adm/expense/project_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer projectId = Https.getInt(req, "projectId", R.REQUIRED, R.I);
		Project project = null;
		List<User> operators = null;
		if (projectId != null) {
			project = mapper.fetch(Project.class, "Project.query", Cnd.where("p.project_id", "=", projectId));
		}
		if (project == null)
			project = new Project();
		
		if(Asserts.hasAny(Roles.GM.getName(), Context.getRoles())){
			operators = userService.operators(Roles.BOSS.getName());
		}
			else{
				operators = userService.operators(Context.getCorpId(), Roles.GM.getName());
			}
		if(operators.size()==0)
			operators = new ArrayList<User>();
		

		req.setAttribute("project", project);
		req.setAttribute("operators", operators);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer projectId = null;
		try {
			CSRF.validate(req);
			
			projectId = Https.getInt(req, "projectId", R.I);
			String projectName = Https.getStr(req, "projectName", R.CLEAN, R.RANGE, "{1,20}", "项目名称");
			Float money = Https.getFloat(req, "money", R.REQUIRED, R.F, "预算金额");
			String startStr = Https.getStr(req, "start_yyyyMMdd", R.REQUIRED, R.D, "开始时间");
			String endStr = Https.getStr(req, "end_yyyyMMdd", R.REQUIRED, R.D, "结束时间");
		//	Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			Integer actorId = Https.getInt(req, "actorId", R.REQUIRED, R.I, "审批人员");
			DateTime now = new DateTime();
			Project project = null;
			
			if (projectId != null) {
				project = mapper.fetch(Project.class, "Project.query", Cnd.where("p.project_id", "=", projectId));
				Asserts.isNull(project, "项目不存在");
			} else {
				project = new Project();
				project.setCreateTime(now.toDate());
			}
			project.setProjectName(projectName);
			project.setMoney(RMB.toMinute(money));
			project.setStartDate(Calendars.parse(startStr, Calendars.DATE).toDate());
			project.setEndDate(Calendars.parse(endStr, Calendars.DATE).toDate());
			project.setStatus(Status.DISABLED);
			project.setModifyTime(now.toDate());
			project.setOrgId(Context.getCorpId());
			project.setApproved(Status.PROOFING);
			project.setOperatorId(Context.getUserId());
			transSave(projectId, project, actorId);
			
			Code.ok(mb, (projectId == null ? "新建" : "编辑") + "项目成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Project:add) error: ", e);
			Code.error(mb, (projectId == null ? "新建" : "编辑") + "项目失败");
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
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			if (arr != null && arr.length == 1) {
				dao.update(Project.class, Chain.make("status", status), Cnd.where("projectId", "in", arr));
			}
			Code.ok(mb, "设置项目状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Project:able) error: ", e);
			Code.error(mb, "设置项目状态失败");
		}

		return mb;
	}
	
	private void transSave(final Integer projectId, final Project project, final Integer actorId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = projectId;
				if (projectId != null) {
					dao.clear(ProjectActor.class, Cnd.where("projectId", "=", projectId));
					dao.update(project);
				} else {
					id = dao.insert(project).getProjectId();
				}
				
				// 指定助理审批
				dao.insert(new ProjectActor(id, actorId, Value.I, Status.PROOFING, Roles.GM.getName(), project.getModifyTime(),Status.value));
			}
		});
	}
}
