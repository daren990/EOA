package cn.oa.web.action.sys.cons;

import java.util.List;

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

import cn.oa.consts.Status;
import cn.oa.model.Org;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.User;
import cn.oa.utils.Calendars;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

/**
 * 统计排班表
 * @author jiawei.lu
 *
 */
@IocBean(name = "sys.cons.schedulstatistics")
@At(value = "/sys/cons/schedulstatistics")
public class SchedulStatistics extends Action {

	public static Log log = Logs.getLog(SchedulStatistics.class);
	@GET
	@At
	@Ok("ftl:sys/cons/SchedulStatistics")
	public void page(HttpServletRequest req){
		CSRF.generate(req);
		String monthFirst = null;
		String nextMonth = null;
		String monthFinally = null;
		String monthStr = Https.getStr(req, "month");
		Integer shortcut = Https.getInt(req, "shortcut", R.I, R.IN, "in [0,1]");
		DateTime now = new DateTime();	

		if(monthStr!=null&&monthStr!=""){
			if(shortcut!=null){
				if(shortcut == 0){
					DateTime stime = Calendars.parse(monthStr+"-01", Calendars.DATE).plusMonths(-1);
					monthStr = stime.toString("yyyy-MM");
				}else if(shortcut == 1){
					DateTime stime = Calendars.parse(monthStr+"-01", Calendars.DATE).plusMonths(1);
					monthStr = stime.toString("yyyy-MM");
				}
			}
			monthFirst = monthStr+"-01";
			now = Calendars.parse(monthFirst, Calendars.DATE);
			nextMonth = now.plusMonths(1).toString("yyyy-MM-")+"01";
			now = Calendars.parse(nextMonth, Calendars.DATE);
			monthFinally = now.plusDays(-1).toString("yyyy-MM-dd");
		}else{
		//获取本月第一天到最后一天的日期
		monthFirst = now.toString("yyyy-MM-")+"01";
		monthStr = now.toString("yyyy-MM");
		nextMonth = now.plusMonths(1).toString("yyyy-MM-")+"01";
		now = Calendars.parse(nextMonth, Calendars.DATE);
		monthFinally = now.plusDays(-1).toString("yyyy-MM-dd");
		}

		//获取本公司所有非禁用的员工
		Criteria criUser = Cnd.cri();
		criUser.where()
			.and("u.corp_id", "=", Context.getCorpId())
			.and("u.status", "=", Status.ENABLED);
		criUser.getOrderBy().desc("u.user_id");
		List<User> page = mapper.query(User.class, "ShiftUser.query", criUser);
		for(User u:page){
			float sumTime = 0;
			Criteria cri = Cnd.cri();
			cri.where().and("u.user_id", "=", u.getUserId())
					   .and("shift_date",">=",monthFirst)
					   .and("shift_date","<=",monthFinally);
			List<Shift> shifts = mapper.query(Shift.class, "Shift.query", cri);
			for (Shift s : shifts) {
				if(s.getClasses()==null)continue;
				List<ShiftClass> list = dao.query(ShiftClass.class, Cnd.where("class_id", "=", s.getClasses()));
				for(ShiftClass shiftClass:list){
					sumTime += shiftClass.getSumTime();
				}
			}
			u.setSumTime(sumTime);
		}
		Org org = dao.fetch(Org.class,Context.getCorpId());
		req.setAttribute("page", page);
		req.setAttribute("org", org.getOrgName());
		req.setAttribute("monthFirst", monthFirst);
		req.setAttribute("monthFinally", monthFinally);
		req.setAttribute("monthStr", monthStr);
	}
}
