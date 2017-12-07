package cn.oa.web.action.hrm.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
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

import cn.oa.consts.Status;
import cn.oa.consts.Suffix;
import cn.oa.model.Org;
import cn.oa.model.User;
import cn.oa.model.WageResult;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.RMB;
import cn.oa.utils.Strings;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.except.Errors;
import cn.oa.utils.helper.Wages;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

/**
 * 工资条
 * @author SimonTang
 */
@IocBean(name = "hrm.result.wage")
@At(value = "/hrm/result/wage")
public class WageAction extends Action {

	public static Log log = Logs.getLog(WageAction.class);

	@GET
	@At
	@Ok("ftl:hrm/result/wage_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);
		
		String year = Https.getStr(req, "year", R.yyyy);
		String month = Https.getStr(req, "month", R.MM);
		Integer corpId = Https.getInt(req, "corpId", R.I);
		Integer userId = Https.getInt(req, "userId", R.I);
		
		DateTime now = new DateTime();
		MapBean mb = new MapBean();
		
		String resultMonth = null;
		if (Strings.isNotBlank(year) && Strings.isNotBlank(month)) resultMonth = year + month;
		
		Criteria cri = Cnd.cri();
		Cnds.eq(cri, mb, "u.corp_id", "corpId", corpId);
		Cnds.eq(cri, mb, "w.user_id", "userId", userId);
		Cnds.like(cri, mb, "w.result_month", "resultMonth", resultMonth);
		cri.getOrderBy().desc("w.result_month");
		
		Page<WageResult> page = Webs.page(req);
		page = mapper.page(WageResult.class, page, "WageResult.count", "WageResult.index", cri);
		
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		Webs.put(mb, "year", year);
		Webs.put(mb, "month", month);

		mb.put("years", now.getYear());
		mb.put("months", now.plusMonths(-1).toString("MM"));
		req.setAttribute("page", page);
		req.setAttribute("corps", corps);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:hrm/result/wage_edit")
	public void edit(HttpServletRequest req){
		MapBean mb = new MapBean();
		Integer userId = Https.getInt(req, "userId", R.I);
		String month = req.getParameter("month");
		
		Criteria cri = Cnd.cri();
		cri.where()
			.and("w.user_id", "=", userId)
			.and("w.result_month", "=", month);
		
		Page<WageResult> page = Webs.page(req);
		page = mapper.page(WageResult.class, page, "WageResult.count", "WageResult.index", cri);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@POST
	@At
	@Ok("json")
	public Object update(HttpServletRequest request){
		Integer userId = Https.getInt(request, "userId", R.I);
		Double subsidies = Https.getDouble(request, "subsidies", R.F);
		Double commission = Https.getDouble(request, "commission", R.F);
		String month = request.getParameter("month");
		dao.update(WageResult.class,Chain.make("commission", commission*100).add("subsidies", subsidies*100),//
				Cnd.where("user_id", "=", userId).and("result_month", "=", month));
		Map<String,Object> map = new HashMap<String, Object>(0);
		map.put("userId", userId);
		map.put("month", month);
		return map;
	}
	
	@GET
	@At
	@Ok("ftl:hrm/result/wage_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String year = Https.getStr(req, "year", R.REQUIRED, R.yyyy, "年份");
			String month = Https.getStr(req, "month", R.REQUIRED, R.MM, "月份");
			Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I, "所属公司");
			Integer userId = Https.getInt(req, "userId", R.I, "用户姓名");
			Float otherIncrease = Https.getFloat(req, "otherIncrease", R.REQUIRED, R.F, "其他增加");
			Float otherDeduction = Https.getFloat(req, "otherDeduction", R.REQUIRED, R.F, "其他扣除");
			
			List<User> users = null;
			if (userId != null) {
				User user = mapper.fetch(User.class, "User.query", Cnd
						.where("u.user_id", "=", userId)
						.and("u.status", "=", Status.ENABLED));
				Asserts.isNull(user, "用户不存在");
				users = new ArrayList<User>();
				users.add(user);
			} else {
				users =	mapper.query(User.class, "User.query", Cnd
						.where("u.corp_id", "=", corpId)
						.and("u.status", "=", Status.ENABLED));
			}
			Asserts.isEmpty(users, "用户集合不能为空值");
			
			Integer[] userIds = Converts.array(User.class, Integer.class, users, "userId");
			
			dao.update(WageResult.class, Chain
					.make("otherIncrease", RMB.on(otherIncrease))
					.add("otherDeduction", RMB.on(otherDeduction)), Cnd
					.where("resultMonth", "=", year + month)
					.and("userId", "in", userIds));
			
			Code.ok(mb, "编辑工资条成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Wage:add) error: ", e);
			Code.error(mb, "编辑工资条失败");
		}

		return mb;
	}
	
	/**
	 * 导出工资条
	 * @param req
	 * @param res
	 */
	@GET
	@At
	public void download(HttpServletRequest req, HttpServletResponse res) {
		try {
			String year = Https.getStr(req, "year", R.yyyy);
			String month = Https.getStr(req, "month", R.MM);
			Integer corpId = Https.getInt(req, "corpId", R.I);
			Integer userId = Https.getInt(req, "userId", R.I);
			
			MapBean mb = new MapBean();
			
			String resultMonth = null;
			if (Strings.isNotBlank(year) && Strings.isNotBlank(month)) resultMonth = year + month;
			
			Criteria cri = Cnd.cri();
			Cnds.eq(cri, mb, "u.corp_id", "corpId", corpId);
			Cnds.eq(cri, mb, "w.user_id", "userId", userId);
			Cnds.eq(cri, mb, "w.result_month", "resultMonth", resultMonth);
			cri.getOrderBy().desc("w.result_month");
			List<WageResult> results = mapper.query(WageResult.class, "WageResult.query", cri);
			
			ServletOutputStream output = Webs.output(req, res, Encodes.urlEncode("工资条." + Suffix.XLSX));
			Excels excels = new Excels(Suffix.XLSX);
			List<List<Data>> rowList = new ArrayList<List<Data>>();
			rowList.add(Wages.subjects());
			for (WageResult e : results) {
				rowList.add(Wages.cells(views, e));
			}
			
			excels.write(output, rowList, "工资条");
		} catch (Exception e) {
			log.error("(Wage:download) error: ", e);
		}
	}
}
