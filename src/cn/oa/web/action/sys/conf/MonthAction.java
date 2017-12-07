package cn.oa.web.action.sys.conf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import cn.oa.consts.Cache;
import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.model.Org;
import cn.oa.model.SalaryRuleItem;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.User;
import cn.oa.model.WorkMonth;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/sys/conf/month")
public class MonthAction extends Action {

	public static Log log = Logs.getLog(MonthAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/month_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/month/days", token);
		CSRF.generate(req, "/sys/conf/month/able", token);
		
		String workName = Https.getStr(req, "workName", R.CLEAN, R.RANGE, "{1,20}");
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "m.month_name", "workName", workName);
		cri.getOrderBy().desc("m.year").desc("m.month");
		
		Page<WorkMonth> page = Webs.page(req);
		page = mapper.page(WorkMonth.class, page, "WorkMonth.count", "WorkMonth.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@At
	@Ok("ftl:sys/conf/month_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer monthId = Https.getInt(req, "monthId", R.REQUIRED, R.I);
		
		WorkMonth month = null;
		if (monthId != null) {
			month = mapper.fetch(WorkMonth.class, "WorkMonth.query", Cnd.where("m.month_id", "=", monthId));
		}
		if (month == null) month = new WorkMonth();
		
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		List<ShiftClass> shiftClasses = null;
		String[] roles = Context.getRoles();
		List<String> roleList = Arrays.asList(roles);
		//超级管理员和排班管理员可以获取所有班次和公司，并进行排班
		if(roleList.contains(Roles.ADM.getName()) || roleList.contains(Roles.SS.getName())){
			shiftClasses = dao.query(ShiftClass.class,Cnd.where("status", "=", Status.ENABLED));
			if(shiftClasses == null){
				shiftClasses = new ArrayList<ShiftClass>();
			}
		}else{
			shiftClasses = dao.query(ShiftClass.class,Cnd.where("corpId", "=", Context.getCorpId()).and("status", "=", Status.ENABLED));
			if(shiftClasses == null){
				shiftClasses = new ArrayList<ShiftClass>();
			}
			corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP).and("org_id", "=", Context.getCorpId()));
		}
		

		
		DateTime now = new DateTime();
		MapBean mb = new MapBean();
		mb.put("years", now.getYear());
		mb.put("months", now.plusMonths(-1).toString("MM"));
		
		
		req.setAttribute("month", month);
		req.setAttribute("corps", corps);
		req.setAttribute("shiftClasses", shiftClasses);
		req.setAttribute("mb", mb);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer monthId = null;
		try {
			CSRF.validate(req);
			monthId = Https.getInt(req, "monthId", R.I);
			
			String workName = Https.getStr(req, "workName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "月排班名称");
			String workDays = Https.getStr(req, "workDays", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "工作日");
			String holidays = Https.getStr(req, "holidays", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "节假日");
			String orgYear = Https.getStr(req, "orgYear", R.yyyy, "月排班年份");
			String yearStr = Https.getStr(req, "year", R.REQUIRED, R.yyyy, "月排班年份");
			String orgMonth = Https.getStr(req, "orgMonth", R.MM, "月排班月份");
			String monthStr = Https.getStr(req, "month", R.REQUIRED, R.MM, "月排班月份");
			Integer orgOrgId = Https.getInt(req, "orgOrgId", R.I, "公司绑定");
		//	Integer paidDay = Https.getInt(req, "paidDay", R.REQUIRED, R.I, "本月带薪天数");
			Integer orgId = Https.getInt(req, "orgId", R.REQUIRED, R.I, "公司绑定");
			Integer shiftClassId = Https.getInt(req, "shiftClassId", R.REQUIRED, R.I, "班次绑定");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			int count = dao.count(WorkMonth.class, Cnd
					.where("orgId", "=", orgId)
					.and("year", "=", yearStr)
					.and("month", "=", monthStr)
					.and("status", "=", Status.ENABLED));
			
			boolean blank = Strings.isBlank(yearStr) && Strings.isBlank(monthStr) && orgId == null;
			boolean equals = Strings.equals(orgYear, yearStr) && Strings.equals(orgMonth, monthStr) && orgOrgId.equals(orgId);
			
			if (!( blank || equals) && count > 0) throw new Errors("月排班已存在");
			
			WorkMonth month = null;
			if (monthId != null) {
				month = mapper.fetch(WorkMonth.class, "WorkMonth.query", Cnd.where("m.month_id", "=", monthId));
				Asserts.isNull(month, "月排班不存在");
			} else {
				month = new WorkMonth();
			}
			
			month.setWorkName(workName);
			month.setWorkDays(workDays);
			month.setYear(yearStr);
			month.setMonth(monthStr);
			month.setOrgId(orgId);
			month.setShiftClassId(shiftClassId);
			month.setStatus(status);
			month.setHolidays(holidays);
			if (monthId != null) {
				dao.update(month);
			} else {
				dao.insert(month);
			}
			
			System.out.println(shiftClassId);
			shift(month, shiftClassId);
			
			
			Code.ok(mb, (monthId == null ? "新建" : "编辑") + "月排班成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Month:add) error: ", e);
			Code.error(mb, (monthId == null ? "新建" : "编辑") + "月排班失败");
		}

		return mb;
	}
	
	private void shift(WorkMonth workMonth, Integer ClassesId){
		Date nowDate = new Date();
	

		String startStr = workMonth.getYear()+"-"+workMonth.getMonth();
		DateTime startDate = Calendars.parse(startStr, "yyyy-MM");
		DateTime endDate =startDate.plusMonths(1).plusDays(-1);
		String endStr = endDate.toString("yyyy-MM-dd");
		startStr = startDate.toString("yyyy-MM-dd");
		
		System.out.println(workMonth);
		
		Criteria criUser = Cnd.cri();
		criUser.where()
			.and("corp_id", "=", workMonth.getOrgId())
			.and("status", "=", Status.ENABLED);
		criUser.getOrderBy().desc("user_id");
		//获取本公司所有非禁用的员工,遍历表单
		List<User> users = dao.query(User.class, criUser);
		System.out.println(users);
		System.out.println(endDate.getDayOfMonth());
		
		//获取排班的日子的集合
		String[] days = workMonth.getWorkDays().split(",");
		System.out.println(days.length);

		transSave(users, ClassesId, startStr, endStr, nowDate, endDate, days);
	
	}
	
	private void transSave(final List<User> users, final Integer ClassesId, final String startStr,final String endStr,final Date nowDate,final DateTime endDate, final String[] days) {
		Trans.exec(new Atom() {
			@Override
			public void run() {		
				for(User u :users){

					dao.clear(Shift.class,Cnd.where("shift_date", ">=", startStr).and("shift_date", "<=", endStr).and("user_id", "=", u.getUserId()));
					for(int i=0; i < endDate.getDayOfMonth(); i++){
						boolean flag = true;
						for(String day : days){
							if(new Integer(day) == endDate.plusDays(-i).getDayOfMonth()){
								flag = false;
								break;
							}
						}
						if(flag){
							continue;
						}
						Shift shift = new Shift();							
						shift.setClasses(ClassesId);
						shift.setShiftDate(endDate.plusDays(-i).toDate());
						shift.setUserId(u.getUserId());
						shift.setCreateTime(nowDate);
						shift.setIsUsed(Status.ENABLED);
						shift.setModifyId(Context.getUserId());
						shift.setStatus(Status.ENABLED);
						dao.insert(shift);
					}
				}
			}
		});
	}
	
	@POST
	@At
	@Ok("json")
	public Object days(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String yearStr = Https.getStr(req, "year", R.REQUIRED, R.yyyy, "年份");
			String monthStr = Https.getStr(req, "month", R.REQUIRED, R.MM, "月份");
			
			DateTime month = Calendars.parse(yearStr + monthStr, "yyyyMM");
			
			int maxDay = month.dayOfMonth().withMaximumValue().getDayOfMonth();
			int start = Integer.valueOf(month.dayOfMonth().withMinimumValue().toString("e"));
			int end = Integer.valueOf(month.dayOfMonth().withMaximumValue().toString("e"));
			
			StringBuilder buff = new StringBuilder();
			if (start > 1) {
				for (int i = 0; i < start - 1; i++) buff.append("0").append(",");
			}
			for (int i = 1; i < maxDay + 1; i++) {
				if (i < 10) buff.append("0");
				buff.append(i).append(",");
			}
			
			if (end < 7) {
				for (int i = 0; i < 7 - end; i++) buff.append("0").append(",");
			}
			
			String[] days = Strings.splitIgnoreBlank(buff.toString());
			mb.put("days", days);
			
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Month:days) error: ", e);
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
			if (arr != null && arr.length > 0) {
				cache.removeAll(Cache.FQN_RESOURCES);
				dao.update(WorkMonth.class, Chain.make("status", status), Cnd.where("monthId", "in", arr));
			}
			Code.ok(mb, "设置月排班状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Month:able) error: ", e);
			Code.error(mb, "设置月排班状态失败");
		}

		return mb;
	}
}
