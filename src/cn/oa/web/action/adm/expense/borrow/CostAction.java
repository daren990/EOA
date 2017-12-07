package cn.oa.web.action.adm.expense.borrow;

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

import cn.oa.consts.Status;
import cn.oa.model.Borrow;
import cn.oa.model.Org;
import cn.oa.model.User;
import cn.oa.utils.MapBean;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean(name = "adm.expense.borrow.cost")
@At(value = "/adm/expense/borrow/cost")
public class CostAction extends Action {

	public static Log log = Logs.getLog(CostAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/expense/borrow/approve/actors", token);
		
		Integer corpId = Https.getInt(req, "corpId", R.CLEAN, R.I);
		Integer userId = Https.getInt(req, "userId", R.CLEAN, R.I);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		String number = Https.getStr(req, "number", R.CLEAN,"{1,100}");

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("b.status", "=", Status.ENABLED)
				.and("b.approved", "=", Status.OK);
		Cnds.eq(cri, mb, "u.corp_id", "corpId", corpId);
		Cnds.eq(cri, mb, "b.number", "number", number);
		Cnds.eq(cri, mb, "b.user_id", "userId", userId);
		Cnds.gte(cri, mb, "b.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "b.create_time", "endTime", endStr);
		cri.getOrderBy().desc("b.modify_time");

		Page<Borrow> page = Webs.page(req);
		page = mapper.page(Borrow.class, page, "Borrow.count", "Borrow.index", cri);
		
		Long sumMoney = mapper.count("Borrow.sum", cri);
		
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		List<User> users = dao.query(User.class, Cnd.where("status", "=", Status.ENABLED));
		
		req.setAttribute("page", page);
		req.setAttribute("sumMoney", sumMoney.intValue());
		req.setAttribute("corps", corps);
		req.setAttribute("users", users);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/cost_page_wx")
	public void wxpage(HttpServletRequest req) {pageUtil(req);}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/cost_page")
	public void page(HttpServletRequest req) {pageUtil(req);}
}
