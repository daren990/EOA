package cn.oa.web.action.sys.cons;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
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
import cn.oa.consts.Suffix;
import cn.oa.consts.WeekDay;
import cn.oa.model.Org;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.ShiftCorp;
import cn.oa.model.ShiftHoliday;
import cn.oa.model.User;
import cn.oa.service.ShiftService;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.ShiftExcelUtil;
import cn.oa.utils.Strings;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.except.Errors;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;
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
 * 新的排班
 * @qiumingxie 
 */
@IocBean(name = "sys.cons.shift")
@At(value = "/sys/cons/shift")
public class ShiftAction extends Action {

	public static Log log = Logs.getLog(ShiftAction.class);
	
	@Inject
	private ShiftService shiftService;
	
	@GET
	@At
	@Ok("ftl:sys/cons/day_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/cons/shift/add", token);
		CSRF.generate(req, "/sys/cons/shift/getShiftStatus", token);
		String endStr;
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);		
		Integer shortcut = Https.getInt(req, "shortcut", R.I, R.IN, "in [0,1]");
		Integer corpId = Https.getInt(req, "corpId", R.I);
		if(corpId == null) corpId = Context.getCorpId();
		String strDate = startStr;	
		//获取结束日期号数
		String endDay = null;
		if(strDate!=null){
			//字符串转换为日期,再获取6天前的日期
			DateTime start = null;
			if(shortcut!=null){
				if(shortcut == 0){
					start = Calendars.parse(strDate, Calendars.DATE).plusWeeks(-1);
				}else if(shortcut == 1){
					start = Calendars.parse(strDate, Calendars.DATE).plusWeeks(1);
				}
			}else{
				start = Calendars.parse(strDate, Calendars.DATE);
			}
			DateTime end = start.plusDays(6);
			startStr = start.toString("yyyy-MM-dd");
			endStr = end.toString("yyyy-MM-dd");
			endDay = end.toString("dd");
		}else{
			//获取本周周一和周日的日期
			DateTime now = new DateTime();				
			int i = Integer.parseInt(now.toString("e"));
			now = now.plusDays(7-i);
			endStr = now.toString("yyyy-MM-dd");
			startStr = now.plusDays(-6).toString("yyyy-MM-dd");
			endDay = now.toString("dd");
			System.out.println(endStr+"|"+startStr+"|"+endDay);
		}
		Criteria holidayCri = Cnd.cri();
		holidayCri.where().and("holiday",">=",startStr).and("holiday","<=",endStr);
		holidayCri.getOrderBy().asc("holiday");	
		List<ShiftHoliday> shiftHolidays = dao.query(ShiftHoliday.class, holidayCri);
		//如果这一周存在法定节假日，才会进入下面的循环
		for(ShiftHoliday sh:shiftHolidays){
			System.out.println(Calendars.parse(sh.getHoliday(), Calendars.DATE).toString("e"));
			sh.setDay(Calendars.parse(sh.getHoliday(), Calendars.DATE).toString("e"));
		}
		
		String[] roles = Context.getRoles();
		List<String> roleList = Arrays.asList(roles);
//		for(String role : roles){
			// 管理员、排班管理员角色可以给所有公司排班
//			if(Roles.ADM.getName().equals(role) || Roles.SS.getName().equals(role)){
			//判断用户是否有权限可以进行排班
			List<Org> orgs = null;
			if(roleList.contains(Roles.ADM.getName()) || roleList.contains(Roles.SS.getName())){
				
				orgs = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", 1));
				
//				Criteria criUser = Cnd.cri();
//				criUser.where().and("u.status", "=", Status.ENABLED);
//				criUser.getOrderBy().desc("u.corp_id");
//				//查询所有的启用的用户
//				List<User> users = mapper.query(User.class, "ShiftUser.query", criUser);
//				
//				Criteria criShift = Cnd.cri();
//				criShift.where().and("shift_date",">=",startStr).and("shift_date","<=",endStr);
//				criShift.getOrderBy().asc("s.shift_date");
//				//查询这一周的需要上班的shift对象
//				List<Shift> shifts = mapper.query(Shift.class, "Shift.query", criShift);
//				System.out.println(shifts+":"+shifts.size());
//				
//				
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
//				for (Shift s : shifts) {
//					System.out.println(Calendars.parse(sdf.format(s.getShiftDate()), Calendars.DATE).toString("yyyy-MM-dd"));
//					//获得星期几
//					String weekDay = Calendars.parse(sdf.format(s.getShiftDate()), Calendars.DATE).toString("e");
//	
//					s.setSort(Integer.parseInt(weekDay));
//					if(s.getClasses() == null)continue;
//					ShiftClass shiftClass = dao.fetch(ShiftClass.class, s.getClasses());
//					s.setShiftClass(shiftClass);
//				}
//				//获取公司名称
//				Org org = dao.fetch(Org.class,corpId);
//				List<ShiftClass> classes = dao.query(ShiftClass.class, Cnd.where("status", "=", 1));
//				ShiftCorp shiftCorp = dao.fetch(ShiftCorp.class, Cnd.where("corp_id", "in", corpId));
//				req.setAttribute("shifts", shifts);
//				req.setAttribute("page", users);
//				req.setAttribute("shiftCorp", shiftCorp);
//				req.setAttribute("classes", classes);
//				req.setAttribute("corp", org.getOrgName());
//				req.setAttribute("startStr", startStr);
//				req.setAttribute("endStr", endStr);
//				req.setAttribute("endDay", endDay);
//				req.setAttribute("shiftHolidays", shiftHolidays);
//				
//				return ;
			}else{
				orgs = dao.query(Org.class, Cnd.where("orgId", "=", Context.getCorpId()));
			}
//		}
		
		Criteria cri = Cnd.cri();
		cri.where().and("u.corp_id", "=", corpId)
				   .and("shift_date",">=",startStr)
				   .and("shift_date","<=",endStr);
		cri.getOrderBy().asc("s.shift_date");	
		
		Criteria criUser = Cnd.cri();
		criUser.where()
			.and("u.corp_id", "=", corpId)
			.and("u.status", "=", Status.ENABLED);
		criUser.getOrderBy().desc("u.user_id");
		//不分页查询
		List<User> page = mapper.query(User.class, "ShiftUser.query", criUser);
		
		//获取本公司所有员工的排班
		List<Shift> shifts = mapper.query(Shift.class, "Shift.query", cri);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
		for (Shift s : shifts) {
			String weekDay = Calendars.parse(sdf.format(s.getShiftDate()), Calendars.DATE).toString("e");
			s.setSort(Integer.parseInt(weekDay));
			if(s.getClasses() == null)continue;
			ShiftClass shiftClass = dao.fetch(ShiftClass.class, s.getClasses());
			s.setShiftClass(shiftClass);
		}
		//获取公司名称
		Org org = dao.fetch(Org.class, corpId);
		List<ShiftClass> classes = dao.query(ShiftClass.class, Cnd.where("corp_id", "in", corpId).and("status", "=", 1));
//		if (121 != org.getOrgId()) { // 121为集团代码
//			Criteria cri1 = Cnd.cri();
//			cri1.where().and("corp_id", "=", 121);
//			cri1.getOrderBy().desc("modify_time");
//			classes.addAll(dao.query(ShiftClass.class, cri1));
//		}
		ShiftCorp shiftCorp = dao.fetch(ShiftCorp.class, Cnd.where("corp_id", "in", corpId));
		
		req.setAttribute("shiftCorp", shiftCorp);
		req.setAttribute("page", page);
		req.setAttribute("classes", classes);
		req.setAttribute("corp", org.getOrgName());
		req.setAttribute("shifts", shifts);
		req.setAttribute("startStr", startStr);
		req.setAttribute("endStr", endStr);
		req.setAttribute("endDay", endDay);
		req.setAttribute("shiftHolidays", shiftHolidays);
		req.setAttribute("corps", orgs);
	}
	
	private void monthPageUtil(HttpServletRequest req ,Integer corpId) {
		MapBean mb = new MapBean();
		String trueName = Https.getStr(req, "trueName", R.CLEAN, R.RANGE, "{1,20}");
		
		String endStr;
		String startStr = Https.getStr(req, "startTime");
		Integer shortcut = Https.getInt(req, "shortcut", R.I, R.IN, "in [0,1]");
		String strDate = startStr;	
		String nextMonth = null;
		if(strDate!=null){
			//字符串转换为日期,再获取6天前的日期
			startStr = startStr + "-01";
			if(shortcut!=null){
				if(shortcut == 0){
					DateTime stime = Calendars.parse(startStr, Calendars.DATE).plusMonths(-1);
					startStr = Calendars.str(stime, Calendars.DATE);
					strDate = stime.toString("yyyy-MM");
				}else if(shortcut == 1){
					DateTime stime = Calendars.parse(startStr, Calendars.DATE).plusMonths(1);
					startStr = Calendars.str(stime, Calendars.DATE);
					strDate = stime.toString("yyyy-MM");
				}
			}
			
			DateTime now = Calendars.parse(startStr, Calendars.DATE);
			nextMonth = now.plusMonths(1).toString("yyyy-MM-")+"01";
			now = Calendars.parse(nextMonth, Calendars.DATE);
			endStr = now.plusDays(-1).toString("yyyy-MM-dd");
		}else{
			//获取本月1号到月尾的日期
			DateTime now = new DateTime();
			strDate = now.toString("yyyy-MM");
			startStr = strDate+"-01";
			nextMonth = now.plusMonths(1).toString("yyyy-MM-")+"01";
			now = Calendars.parse(nextMonth, Calendars.DATE);
			endStr = now.plusDays(-1).toString("yyyy-MM-dd");
		}
		Criteria cri = Cnd.cri();
		cri.where()
			.and("u.status", "=", Status.ENABLED)
			.and("u.userName", "<>", "admin");
		Cnds.eq(cri, mb, "u.corp_id", "corpId", corpId);
		Cnds.like(cri, mb, "u.true_name", "trueName", trueName);
		cri.getOrderBy().desc("u.user_id");
		
		Page<User> page = Webs.page(req);
		page = mapper.page(User.class, page, "User.count", "User.index", cri);
		
		Criteria scri = Cnd.cri();
		scri.where().and("s.shift_date",">=",startStr)
				   .and("s.shift_date","<=",endStr);
		Cnds.eq(scri, mb, "u.corp_id", "corpId", corpId);
		Cnds.like(scri, mb, "u.true_name", "trueName", trueName);
		scri.getOrderBy().asc("s.shift_date");
		List<Shift> shifts = mapper.query(Shift.class, "Shiftinner.query", scri);
//		Map<String, Shift> shiftMap = new HashMap<String, Shift>();
		for(Shift s:shifts){
			//获取date日期的号数dd
			s.setDay(Integer.parseInt(Calendars.parse(s.getShiftDate(), Calendars.DATE).toString("dd")));
//			shiftMap.put(s.getUserId().toString() + s.getDay(), s);
		}
		//获取本月天数
		int dayCount = Integer.parseInt(Calendars.parse(endStr, Calendars.DATE).toString("dd"));
		List<Integer> dayList = new ArrayList<Integer>();
		List<WeekDay> weekDays = new ArrayList<WeekDay>();				
		
		for(int i=1;i<=dayCount;i++){
			WeekDay weekDay = new WeekDay();
			DateTime d = null;
			if(i<10){
				d = Calendars.parse(strDate+"-0"+String.valueOf(i), Calendars.DATE);
			}else{
				d = Calendars.parse(strDate+"-"+String.valueOf(i), Calendars.DATE);
			}
			weekDay.setDay(i);
			
			weekDay.setWeekStr(weekUtil(Integer.parseInt(d.toString("e"))));
			weekDays.add(weekDay);
		}
		
		for(int i=1;i<=dayCount;i++){
			dayList.add(i);
		}
		Map<Integer, Shift> map = new HashMap<Integer, Shift>();
		for (Shift s : shifts) {
			if(map.get(s.getClasses())==null){
				map.put(s.getClasses(), s);
			}
		}
		List<Shift> classes = new ArrayList<Shift>();
		for(Integer key : map.keySet()){
			classes.add(map.get(key));
		}
		
		req.setAttribute("page", page);
		req.setAttribute("dayList", dayList);
		req.setAttribute("weekDays", weekDays);
		req.setAttribute("shifts", shifts);
		req.setAttribute("classes", classes);
		req.setAttribute("strDate", strDate);
		req.setAttribute("mb", mb);
		req.setAttribute("monthStr", strDate);
		req.setAttribute("startStr", startStr);
		req.setAttribute("endStr", endStr);
//		req.setAttribute("shiftMap", shiftMap);
	}
	
	/**
	 * 查看排班
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:sys/cons/all_page")
	public void monthPage(HttpServletRequest req) {
		Integer corpId = Https.getInt(req, "corpId",R.CLEAN, R.I);
		monthPageUtil(req,corpId);
		List<Org> corps = dao.query(Org.class, Cnd.where("type", "=", Status.ENABLED));
		req.setAttribute("corps", corps);
	}
	
	@GET
	@At
	@Ok("ftl:sys/cons/path_page")
	public void pathMonthPage(HttpServletRequest req) {
		monthPageUtil(req,Context.getCorpId());
		Org org = dao.fetch(Org.class,Context.getCorpId());
		req.setAttribute("corps", org);
	}
	
	@GET
	@At
	@Ok("ftl:sys/cons/personal_page")
	public void personalpage(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/cons/shift/add", token);
		String endStr;
		String startStr = Https.getStr(req, "startTime");
		Integer shortcut = Https.getInt(req, "shortcut", R.I, R.IN, "in [0,1]");
		String strDate = startStr;	
		String nextMonth = null;
		//获取结束日期号数
		String endDay = null;
		if(strDate!=null){
			//字符串转换为日期,再获取6天前的日期
			startStr = startStr + "-01";
			if(shortcut!=null){
				if(shortcut == 0){
					DateTime stime = Calendars.parse(startStr, Calendars.DATE).plusMonths(-1);
					startStr = Calendars.str(stime, Calendars.DATE);
					strDate = stime.toString("yyyy-MM");
				}else if(shortcut == 1){
					DateTime stime = Calendars.parse(startStr, Calendars.DATE).plusMonths(1);
					startStr = Calendars.str(stime, Calendars.DATE);
					strDate = stime.toString("yyyy-MM");
				}
			}
			
			DateTime now = Calendars.parse(startStr, Calendars.DATE);
			nextMonth = now.plusMonths(1).toString("yyyy-MM-")+"01";
			now = Calendars.parse(nextMonth, Calendars.DATE);
			endStr = now.plusDays(-1).toString("yyyy-MM-dd");
		}else{
			//获取本月1号到月尾的日期
			DateTime now = new DateTime();
			strDate = now.toString("yyyy-MM");
			startStr = strDate+"-01";
			nextMonth = now.plusMonths(1).toString("yyyy-MM-")+"01";
			now = Calendars.parse(nextMonth, Calendars.DATE);
			endStr = now.plusDays(-1).toString("yyyy-MM-dd");
		}

		//获取本月跨越的周数
		int countWeek = 0;
		int i = 0;
		DateTime stareTime = Calendars.parse(startStr,Calendars.DATE);
		DateTime endTime = Calendars.parse(endStr,Calendars.DATE);
		List<WeekDay> weekDays = new ArrayList<WeekDay>();
		List<Integer> weeks = new ArrayList<Integer>();
		Map<Date, Integer> weekDayMap = new ConcurrentHashMap<Date, Integer>();
		List<ShiftHoliday> shiftHoliday = dao.query(ShiftHoliday.class, Cnd.where("holiday", ">=", startStr).and("holiday", "<=", endStr));
		Map<Date, String> smap = new HashMap<Date, String>();
		for(ShiftHoliday sh:shiftHoliday){
			smap.put(sh.getHoliday(), sh.getRemarks());
		}
		while(true){
			WeekDay wDay = new WeekDay();
			wDay.setDay(Integer.parseInt(stareTime.toString("dd")));
			wDay.setSunday(Integer.parseInt(stareTime.toString("e")));
			wDay.setWeek(countWeek+1);
			if(smap.containsKey(stareTime.toDate())){
				wDay.setHoliday(smap.get(stareTime.toDate()));
			}
			weekDays.add(wDay);
			weekDayMap.put(stareTime.toDate(), countWeek+1);
			if(Integer.parseInt(stareTime.toString("e"))==6){				
				countWeek++;
				weeks.add(countWeek);
			}
			if(stareTime.equals(endTime)){
				if(Integer.parseInt(stareTime.toString("e"))!=6){
					countWeek++;
					weeks.add(countWeek);
				}
				break;
			}
			stareTime = stareTime.plusDays(1);			
			i++;
			if(i==31)
				break;
		}
		
		Criteria cri = Cnd.cri();
		cri.where().and("u.user_id", "=", Context.getUserId())
				   .and("s.shift_date",">=",startStr)
				   .and("s.shift_date","<=",endStr);
		cri.getOrderBy().asc("s.shift_date");			
		
		//获取该员工的排班
		List<Shift> shifts = mapper.query(Shift.class, "Shiftinner.query", cri);
		List<ShiftClass> classes = new ArrayList<ShiftClass>();
		
		for (Shift s : shifts) {
			DateTime weekDay = Calendars.parse(s.getShiftDate(), Calendars.DATE);
			s.setDay(Integer.parseInt(weekDay.toString("dd")));
			s.setSort(Integer.parseInt(weekDay.toString("e")));
			s.setWeek(weekDayMap.get(s.getShiftDate()));
			ShiftClass shiftClass = dao.fetch(ShiftClass.class,s.getClasses() );
			if(shiftClass == null)continue;
			s.setShiftClass(shiftClass);
			classes.add(shiftClass);
		}
		Map<Integer, ShiftClass> map = new HashMap<Integer, ShiftClass>();
		for (ShiftClass c : classes) {
			if(map.get(c.getClassId())==null){
				map.put(c.getClassId(), c);
			}
		}
		List<ShiftClass> sclasses = new ArrayList<ShiftClass>();
		for(Integer key : map.keySet()){
			sclasses.add(map.get(key));
		}
		//获取公司名称
		Org org = dao.fetch(Org.class,Context.getCorpId());
		ShiftCorp shiftCorp = dao.fetch(ShiftCorp.class, Cnd.where("corp_id", "in", Context.getCorpId()));
		req.setAttribute("shiftCorp", shiftCorp);
		req.setAttribute("corp", org.getOrgName());
		req.setAttribute("shifts", shifts);
		req.setAttribute("startStr", startStr);
		req.setAttribute("endStr", endStr);
		req.setAttribute("endDay", endDay);
		req.setAttribute("weeks", weeks);
		req.setAttribute("weekDays", weekDays);
		req.setAttribute("sclasses", sclasses);
		req.setAttribute("monthStr", strDate);
	}
	
	private void monthExcelUtil(HttpServletRequest req, HttpServletResponse res,Integer corpId) {
		try {
			MapBean mb = new MapBean();
			String trueName = Https.getStr(req, "trueName", R.CLEAN, R.RANGE, "{1,20}");
			
			String endStr;
			String startStr = Https.getStr(req, "startTime",R.REQUIRED,"年月");
			String strDate = startStr;	
			String nextMonth = null;
		
				startStr = startStr + "-01";
				DateTime now = Calendars.parse(startStr, Calendars.DATE);
				nextMonth = now.plusMonths(1).toString("yyyy-MM-")+"01";
				now = Calendars.parse(nextMonth, Calendars.DATE);
				endStr = now.plusDays(-1).toString("yyyy-MM-dd");

			Criteria cri = Cnd.cri();
			cri.where()
				.and("u.status", "=", Status.ENABLED)
				.and("u.userName", "<>", "admin");
			Cnds.like(cri, mb, "u.true_name", "trueName", trueName);
			Cnds.eq(cri, mb, "u.corp_id", "corpId", corpId);
			cri.getOrderBy().desc("u.user_id");
			
			List<User> page = mapper.query(User.class, "User.query", cri);
			
			Criteria scri = Cnd.cri();
			scri.where().and("s.shift_date",">=",startStr)
					   .and("s.shift_date","<=",endStr);
			Cnds.like(scri, mb, "u.true_name", "trueName", trueName);
			Cnds.eq(scri, mb, "u.corp_id", "corpId", corpId);
			scri.getOrderBy().asc("s.shift_date");
			List<Shift> shifts = mapper.query(Shift.class, "Shiftinner.query", scri);
			for(Shift s:shifts){
				//获取date日期的号数dd
				s.setDay(Integer.parseInt(Calendars.parse(s.getShiftDate(),Calendars.DATE).toString("dd")));
			}
			//获取本月天数
			int dayCount = Integer.parseInt(Calendars.parse(endStr, Calendars.DATE).toString("dd"));
			List<Integer> dayList = new ArrayList<Integer>();
			List<WeekDay> weekDays = new ArrayList<WeekDay>();				
			
			for(int i=1;i<=dayCount;i++){
				WeekDay weekDay = new WeekDay();
				DateTime d = null;
				if(i<10){
					d = Calendars.parse(strDate+"-0"+String.valueOf(i), Calendars.DATE);
				}else{
					d = Calendars.parse(strDate+"-"+String.valueOf(i), Calendars.DATE);
				}
				weekDay.setDay(i);
				
				weekDay.setWeekStr(weekUtil(Integer.parseInt(d.toString("e"))));
				weekDays.add(weekDay);
			}
			
			for(int i=1;i<=dayCount;i++){
				dayList.add(i);
			}
			Map<Integer, Shift> map = new HashMap<Integer, Shift>();
			for (Shift s : shifts) {
				if(map.get(s.getClasses())==null){
					map.put(s.getClasses(), s);
				}
			}
			List<Shift> classes = new ArrayList<Shift>();
			for(Integer key : map.keySet()){
				classes.add(map.get(key));
			}
			Org org = dao.fetch(Org.class,corpId);

			String excelName = org.getOrgName() + " " +strDate+" 排班表";
			ServletOutputStream output = Webs.output(req, res, Encodes.urlEncode(excelName+"." + Suffix.XLSX));
			Excels excels = new Excels(Suffix.XLSX);
			List<List<Data>> rowList = new ArrayList<List<Data>>();
			rowList.add(ShiftExcelUtil.subjects(dayList));
			rowList.add(ShiftExcelUtil.subjects2(weekDays));
			int count=0;
			for(User u:page){
				count++;
				rowList.add(ShiftExcelUtil.cells(u.getUserId(),u.getTrueName(),u.getJobNumber(),shifts,count,dayList));
			}
			Map<Integer, Shift> classMap = new HashMap<Integer, Shift>();
			//if()
			for (Shift s : shifts) {
				if(classMap.get(s.getClasses())==null){
					classMap.put(s.getClasses(), s);
				}
			}
			List<Shift> shiftClass = new ArrayList<Shift>();
			for(Integer key : classMap.keySet()){
				shiftClass.add(classMap.get(key));
			}
			excels.writeNoXss(output, rowList, excelName,shiftClass);
			} catch (Exception e) {
				log.error("(ShiftAction:download) error: ", e);
			}
	}
	
	@GET
	@At
	public void monthExcel(HttpServletRequest req, HttpServletResponse res) {
		Integer corpId = Https.getInt(req, "corpId",R.REQUIRED,R.I);
		monthExcelUtil(req,res,corpId);
	}
	
	@GET
	@At
	public void pathMonthExcel(HttpServletRequest req, HttpServletResponse res) {
		monthExcelUtil(req,res,Context.getCorpId());
	}
	
	/**
	 * 保存排班
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
			Date nowDate = new Date();
			String strDate = Https.getStr(req, "workDate", R.D,R.REQUIRED, "考勤日期");
			Integer corpId = Https.getInt(req, "corpId", R.I, R.REQUIRED, "公司的ID");
			//字符串转换为日期,再获取6天前的日期
			DateTime end = Calendars.parse(strDate, Calendars.DATE);
			DateTime start = end.plusDays(-6);
			String startStr = start.toString("yyyy-MM-dd");
			String endStr = end.toString("yyyy-MM-dd");
			
//			String[] roles = Context.getRoles();
//			for(String role : roles){
				// 管理员、排班管理员角色可以给所有公司排班
//				if(Roles.ADM.getName().equals(role) || Roles.SS.getName().equals(role)){
//					List<User> users = dao.query(User.class, Cnd.where("status", "=", Status.ENABLED));
//					transSave(users, req, startStr, endStr, nowDate, start);
//					Code.ok(mb, "排班编辑成功");
//					return mb;
//				}
//			}
			
			//为登录用户所在的公司排班
			Criteria criUser = Cnd.cri();
			criUser.where()
				.and("corp_id", "=", corpId)
				.and("status", "=", Status.ENABLED);
			criUser.getOrderBy().desc("user_id");	
			
			//获取本公司所有非禁用的员工,遍历表单
			List<User> users = dao.query(User.class, criUser);
			transSave(users, req, startStr, endStr, nowDate, start);
			Code.ok(mb, "排班编辑成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(ShiftAction:add) error: ", e);
			Code.error(mb, "排班编辑失败");
		}
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object importp(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			Date nowDate = new Date();
			String strDate = Https.getStr(req, "import", R.D,R.REQUIRED, "考勤日期");
			Integer corpId = Https.getInt(req, "corpId", R.I);
			if(corpId == null) corpId = Context.getCorpId();
			//字符串转换为上周的日期,再获取6天前的日期
			DateTime end = Calendars.parse(strDate, Calendars.DATE).plusDays(-1);
			DateTime start = end.plusDays(-6);
			String startStr = start.toString("yyyy-MM-dd");
			String endStr = end.toString("yyyy-MM-dd");		
			Criteria cri = Cnd.cri();
			cri.where()
				.and("u.corp_id", "=", corpId)
				.and("s.shift_date", ">=", startStr)
				.and("s.shift_date", "<=", endStr);
			cri.getOrderBy().asc("s.shift_date");

			List<Shift> shifts = mapper.query(Shift.class, "Shift.query", cri);
			System.out.println(shifts.size());
			for(Shift s:shifts){
				DateTime shiftDate = Calendars.parse(s.getShiftDate(), Calendars.DATE).plusDays(7);
				s.setShiftDate(shiftDate.toDate());
				s.setModifyId(Context.getUserId());
				s.setModifyTime(null);
				s.setCreateTime(nowDate);
			}
			if(shifts.size()>0){
				transImport(shifts);
			}
			Code.ok(mb, "排班导入成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(ShiftAction:add) error: ", e);
			Code.error(mb, "排班导入失败");
		}

		return mb;
	}
	
	/**
	 * 选出当前导入的班次
	 * @param i
	 * @param mon
	 * @param tue
	 * @param wen
	 * @param thu
	 * @param fri
	 * @param sat
	 * @param sun
	 * @return
	 */
	private Integer chooseDay(int i,Integer mon,Integer tue,Integer wen,Integer thu,Integer fri,Integer sat,Integer sun){
		Integer classes = null;
		switch (i) {
		case 0:classes=mon;break;
		case 1:classes=tue;break;
		case 2:classes=wen;break;
		case 3:classes=thu;break;
		case 4:classes=fri;break;
		case 5:classes=sat;break;
		case 6:classes=sun;break;
		default:break;
		}
		return classes;
	}
	
	private void transSave(final List<User> users,final HttpServletRequest req,final String startStr,final String endStr,final Date nowDate,final DateTime start) {
		Trans.exec(new Atom() {
			@Override
			public void run() {				
				Integer mon = null;
				Integer tue = null;
				Integer wen = null;
				Integer thu = null;
				Integer fri = null;
				Integer sat = null;
				Integer sun = null;
				Integer classes = null;
				for(User u :users){
					String s = Integer.toString(u.getUserId());
					mon = Https.getInt(req, "mon"+s,R.CLEAN);
					tue = Https.getInt(req, "tue"+s,R.CLEAN);
					wen = Https.getInt(req, "wen"+s,R.CLEAN);
					thu = Https.getInt(req, "thu"+s,R.CLEAN);
					fri = Https.getInt(req, "fri"+s,R.CLEAN);
					sat = Https.getInt(req, "sat"+s,R.CLEAN);
					sun = Https.getInt(req, "sun"+s,R.CLEAN);
					dao.clear(Shift.class,Cnd.where("shift_date", ">=", startStr).and("shift_date", "<=", endStr).and("user_id", "=", u.getUserId()));
					for(int i=0;i<7;i++){
						Shift shift = new Shift();							
						classes = chooseDay(i,mon,tue,wen,thu,fri,sat,sun);
						if(classes==null)
							continue;
						shift.setClasses(classes);
						shift.setShiftDate(start.plusDays(i).toDate());
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
	
	private void transImport(final List<Shift> shifts) {
		Trans.exec(new Atom() {
			@Override
			public void run() {				
				for(Shift s:shifts){
					dao.clear(Shift.class,Cnd.where("user_id", "=", s.getUserId()).and("shift_date", "=", s.getShiftDate()));
					dao.insert(s);
				}
			}
		});
	}
	
	private String weekUtil(int key){
		String week = null;
		switch (key) {
		case 1:week = "一";
		
			break;
		case 2:week = "二";

			break;
		case 3:week = "三";

			break;
		case 4:week = "四";

			break;
		case 5:week = "五";

			break;
		case 6:week = "六";

			break;
		case 7:week = "日";

			break;
		default:
			break;
		}
		return week;
	}
	
	
	
	/**
	 * 查询排班状态锁
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:sys/cons/shiftstatus_page")
	public void shiftstatus(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/cons/shift/able", token);
		String year = Https.getStr(req, "year", R.yyyy);
		String month = Https.getStr(req, "month", R.MM);
		String resultMonth = null;
		if (Strings.isNotBlank(year) && Strings.isNotBlank(month)){
			resultMonth = year + "-" + month;
		}
		MapBean mb = new MapBean();
		
		List<Shift> shifts = null;
		if(StringUtils.isNotBlank(resultMonth)){
			shifts = mapper.query(Shift.class,"ShiftStatus.query", Cnd.where("u.status","=",Status.ENABLED).and("date_format(s.shift_date, '%Y-%m')", "=", resultMonth));
			if(shifts != null && shifts.size() > 0){
				for(Shift sh : shifts){
					sh.setShiftMonth(resultMonth);
					sh.setStatus(sh.getStatus() == null ? Status.DISABLED : sh.getStatus());
				}
			}
		}
		Webs.put(mb, "year", year);
		Webs.put(mb, "month", month);
		DateTime now = new DateTime();
		mb.put("years", now.getYear());
		mb.put("months", now.toString("MM"));
		
		req.setAttribute("shifts", shifts);
		req.setAttribute("mb", mb);
	}
	
	/**
	 * 设置排班状态：放开、锁住
	 * @param req
	 * @param res
	 * @return
	 */
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
			String shiftMount = Https.getStr(req, "shiftMount", R.REQUIRED, "排班月份");
			if (arr != null && arr.length > 0) {
				for(Integer corpId : arr){
					shiftService.changeStatus(corpId, shiftMount, status);
				}
			}
			Code.ok(mb, "设置排班状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(User:able) error: ", e);
			Code.error(mb, "设置排班状态失败");
		}

		return mb;
	}
	
	
	@POST
	@At
	@Ok("json")
	public Object getShiftStatus(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String strDate = Https.getStr(req, "startTime", R.D,R.REQUIRED, "排班开始时间");
			Integer index = Https.getInt(req, "index");
			Integer userId = Https.getInt(req, "userId");
			//距离排班中当前选中的起始日期相隔天数，0~6
			DateTime startTime = Calendars.parse(strDate, "yyyy-MM-dd");
			if(index != null){
				startTime = startTime.plusDays(index);
			}
			Integer corpId = Context.getCorpId();
			if(userId != null){
				User user = dao.fetch(User.class, userId);
				corpId = user.getCorpId();
			}
			List<Shift> shifts = mapper.query(Shift.class,"ShiftStatus.query", Cnd.where("u.status","=",Status.ENABLED)
					.and("date_format(s.shift_date, '%Y-%m')", "=", startTime.toString("yyyy-MM"))
					.and("u.corp_id", "=", corpId));
			// 还未添加过考勤
			if(shifts == null || shifts.size() == 0){
				mb.put("code", 1);
				return mb;
			}
			// 添加过考勤,且可编辑，如果有考勤记录
			//正常情况是，shifts的size为1，而且里面的status为1，因为此时代表被排班的员工所在的公司，所有的排班信息都没有曾经用来生成考勤和工资，因此可以被编辑
			if(shifts.size() == 1 && (shifts.get(0).getStatus() == null || Status.ENABLED == shifts.get(0).getStatus())){
				mb.put("code", 1);
				return mb;
			}
			throw new Errors("禁止修改已定版的考勤记录");
		} catch (Errors e) {

			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Shift:getShiftStatus) error: ", e);
			Code.error(mb, "获取排班状态失败");
		}
		return mb;
	}
}
