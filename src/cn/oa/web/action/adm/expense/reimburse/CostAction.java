package cn.oa.web.action.adm.expense.reimburse;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.model.Dict;
import cn.oa.model.Org;
import cn.oa.model.Project;
import cn.oa.model.Reimburse;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.expense.reimburse.cost")
@At(value = "/adm/expense/reimburse/cost")
public class CostAction extends Action {

	public static Log log = Logs.getLog(CostAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/expense/reimburse/approve/actors", token);
		
		Integer corpId = Https.getInt(req, "corpId", R.CLEAN, R.I);
		Integer userId = Https.getInt(req, "userId", R.CLEAN, R.I);
		Integer projectId = Https.getInt(req, "projectId", R.CLEAN, R.I);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		String number = Https.getStr(req, "number", R.CLEAN,"{1,100}");

		boolean isBudget = false;
		if (!(Asserts.hasAny(Roles.ACC.getName(), Context.getRoles())
				|| (Asserts.hasAny(Roles.ASVI.getName(), Context.getRoles()))
				||(Asserts.hasAny(Roles.CSVI.getName(), Context.getRoles()))
				|| Asserts.hasAny(Roles.CAS.getName(), Context.getRoles())))
			isBudget = true;
		
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("r.status", "=", Status.ENABLED)
				.and("r.approved", "=", Status.OK);
		
		Integer[] projectIds = null;
		if (isBudget) {
			List<Project> projects = dao.query(Project.class, Cnd
					.where("operatorId", "=", Context.getUserId())
					.and("status", "=", Status.ENABLED).and("approved", "=", Status.OK));
			projectIds = Converts.array(Project.class, Integer.class, projects, "projectId");
			if (!Asserts.isEmpty(projectIds))
				cri.where().and("p.project_id", "in", projectIds);
		}
		
		Cnds.eq(cri, mb, "u.corp_id", "corpId", corpId);
		Cnds.eq(cri, mb, "r.user_id", "userId", userId);
		//Cnds.eq(cri, mb, "r.type_id", "typeId", typeId);
		Cnds.eq(cri, mb, "r.number", "number", number);
		Cnds.eq(cri, mb, "r.project_id", "projectId", projectId);
		Cnds.gte(cri, mb, "r.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "r.create_time", "endTime", endStr);
		cri.getOrderBy().desc("r.modify_time");

		Page<Reimburse> page = Webs.page(req);
		page = mapper.page(Reimburse.class, page, "Reimburse.count", "Reimburse.index", cri);

		Long sumMoney = mapper.count("Reimburse.sum", cri);
		
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		List<User> users = dao.query(User.class, Cnd.where("status", "=", Status.ENABLED));

		Criteria cri2 = Cnd.cri();
		cri2.where().and("status", "=", Status.ENABLED).and("approved", "=", Status.OK);
		
		if (isBudget && !Asserts.isEmpty(projectIds)) cri2.where().and("projectId", "in", projectIds);
		List<Project> projects = dao.query(Project.class, cri2);
		
		req.setAttribute("page", page);
		req.setAttribute("sumMoney", sumMoney.intValue());
		req.setAttribute("corps", corps);
		req.setAttribute("users", users);
		req.setAttribute("projects", projects);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/cost_page")
	public void page(HttpServletRequest req) {pageUtil(req);}
	
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/cost_page_wx")
	public void wxpage(HttpServletRequest req) {pageUtil(req);}
}
