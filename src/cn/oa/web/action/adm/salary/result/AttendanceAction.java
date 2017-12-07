package cn.oa.web.action.adm.salary.result;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;

import cn.oa.model.AttendanceResult;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.salary.result.attendance")
@At(value = "/adm/salary/result/attendance")
public class AttendanceAction extends Action {
	
	public static Log log = Logs.getLog(AttendanceAction.class);

	
	public void pageUtil(HttpServletRequest req) {
		DateTime now = new DateTime();
		MapBean mb = new MapBean();
		
		String year = Values.getStr(Https.getStr(req, "year", R.yyyy), String.valueOf(now.getYear()));
		String month = Values.getStr(Https.getStr(req, "month", R.MM), now.plusMonths(-1).toString("MM"));
		
		Criteria cri = Cnd.cri();
		cri.where()
			.and("a.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "a.result_month", "resultMonth", year + month);
		cri.getOrderBy().desc("a.result_month");
		
		Page<AttendanceResult> page = Webs.page(req);
		page = mapper.page(AttendanceResult.class, page, "AttendanceResult.count", "AttendanceResult.index", cri);

		mb.put("year", year);
		mb.put("month", month);
		
		mb.put("years", now.getYear());
		mb.put("months", now.plusMonths(-1).toString("MM"));
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/result/attendance_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/result/attendance_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}
	
}
