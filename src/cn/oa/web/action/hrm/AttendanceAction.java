package cn.oa.web.action.hrm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.nutz.dao.Cnd;
import org.nutz.dao.TableName;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.OrderBy;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Roles;
import cn.oa.consts.ShiftC;
import cn.oa.consts.Status;
import cn.oa.consts.Suffix;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.AttendanceThreshold;
import cn.oa.model.AttendanceThresholdItem;
import cn.oa.model.CheckedRecord;
import cn.oa.model.ConfLeave;
import cn.oa.model.Errand;
import cn.oa.model.Leave;
import cn.oa.model.Org;
import cn.oa.model.Outwork;
import cn.oa.model.Overtime;
import cn.oa.model.RedressRecord;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.ShiftCorp;
import cn.oa.model.User;
import cn.oa.model.Wage;
import cn.oa.model.WorkAttendance;
import cn.oa.model.WorkDay;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MailC;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.except.Errors;
import cn.oa.utils.helper.CheckedRecords;
import cn.oa.utils.helper.Works;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

/**
 * 人力资源-考勤核对
 * 
 * @author SimonTang
 */
@IocBean(name = "hrm.attendance")
@At(value = "/hrm/attendance")
public class AttendanceAction extends Action {

	public static Log log = Logs.getLog(AttendanceAction.class);

	@GET
	@At
	@Ok("ftl:hrm/attendance_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);
		CSRF.generate(req, "/hrm/attendance/coll", token);

		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);

		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);// 公司id
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);// 用户id,对于系统是唯一
		String remarkIn = Https.getStr(req, "remarkIn", R.CLEAN);// 上午
		String remarkOut = Https.getStr(req, "remarkOut", R.CLEAN);// 下午

		DateTime now = new DateTime();
		MapBean mb = new MapBean();

		List<CheckedRecord> attendances = new ArrayList<CheckedRecord>();

		/*
		 * 查询一个人(整个if语句)
		 */
		if (userId != null && Strings.isNotBlank(startStr) && Strings.isNotBlank(endStr)) {
			User user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId).and("u.status", "=", Status.ENABLED));

			Map<Integer, WorkDay> dayMap = workRepository.dayMap();
			Map<Integer, String[]> weekMap = workRepository.weekMap();
			Map<String, Integer[]> monthMap = workRepository.monthMap(user.getCorpId());

			WorkDay day = dayMap.get(user.getDayId());
			Asserts.isNull(day, "日排班不能为空值");
			String[] weeks = weekMap.get(user.getWeekId());
			Asserts.isEmpty(weeks, "周排班不能为空值");

			int works = Works.workMinute(day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut());

			Map<String, String[]> leaveDayMap = new ConcurrentHashMap<String, String[]>();
			Map<String, String[]> outworkDayMap = new ConcurrentHashMap<String, String[]>();
			Map<String, String[]> errandDayMap = new ConcurrentHashMap<String, String[]>();
			workService.dayMaps(userId, startStr, endStr, leaveDayMap, outworkDayMap, errandDayMap);

			Criteria cri = Cnd.cri();
			cri.where().and("c.work_date", "<=", now.toString(Calendars.DATE)).and("c.user_id", "=", userId);

			if (Strings.isNotBlank(remarkIn) && Strings.isNotBlank(remarkOut)) {
				cri.where().and(Cnd.exps("c.remark_in", "=", remarkIn).or(Cnd.exp("c.remark_out", "=", remarkOut)));
				Webs.put(mb, "remarkIn", remarkIn);
				Webs.put(mb, "remarkOut", remarkOut);
			} else {
				Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
				Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
			}

			Cnds.gte(cri, mb, "c.work_date", "startTime", startStr);
			Cnds.lte(cri, mb, "c.work_date", "endTime", endStr);

			DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
			DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");

			int months = Months.monthsBetween(start, end).getMonths();

			for (int i = 0; i < months + 1; i++) {
				DateTime plus = start.plusMonths(i);

				if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM")))
					continue;

				Map<String, String> vars = new ConcurrentHashMap<String, String>();
				vars.put("month", plus.toString("yyyyMM"));
				List<CheckedRecord> checkedRecords = mapper.query(CheckedRecord.class, "CheckedRecord.query", cri, null, vars);

				for (CheckedRecord e : checkedRecords) {
					workService.hours(e, day, weeks, monthMap, works, leaveDayMap, outworkDayMap, errandDayMap);
					attendances.add(e);
				}
			}
			req.setAttribute("works", works);
		}

		Webs.put(mb, "startTime", startStr);
		Webs.put(mb, "endTime", endStr);
		Webs.put(mb, "corpId", corpId);
		Webs.put(mb, "userId", userId);

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		req.setAttribute("attendances", attendances);
		req.setAttribute("corps", corps);
		req.setAttribute("mb", mb);
	}

	@GET
	@At
	@Ok("ftl:hrm/attendance_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);
		String workDate = Https.getStr(req, "workDate", R.REQUIRED, R.D);

		Map<String, String> vars = new ConcurrentHashMap<String, String>();
		vars.put("month", Calendars.parse(workDate, Calendars.DATE).toString("yyyyMM"));

		CheckedRecord attendance = mapper.fetch(CheckedRecord.class, "CheckedRecord.query", Cnd.where("c.user_id", "=", userId).and("c.work_date", "=", workDate), null, vars);

		req.setAttribute("attendance", attendance);
	}

	@GET
	@At
	@Ok("ftl:hrm/attendance_page")
	public void pageBack(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);
		CSRF.generate(req, "/hrm/attendance/coll", token);

		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);

		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);// 公司id
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);// 用户id,对于系统是唯一
		String remarkIn = Https.getStr(req, "remarkIn", R.CLEAN);// 上午
		String remarkOut = Https.getStr(req, "remarkOut", R.CLEAN);// 下午

		DateTime now = new DateTime();
		MapBean mb = new MapBean();

		Criteria cri = Cnd.cri();
		cri.where().and("c.work_date", "<=", now.toString(Calendars.DATE));

		List<CheckedRecord> attendances = new ArrayList<CheckedRecord>();

		if (userId != null) {
			cri.where().and("c.user_id", "=", userId);
			Webs.put(mb, "userId", userId);
		} else {
			cri.where().and("o.org_id", "=", corpId);
		}

		/*
		 * 查询
		 */
		if (Strings.isNotBlank(remarkIn) && Strings.isNotBlank(remarkOut)) {
			if (remarkIn.equals("异常") && !remarkOut.equals("异常")) {
				// 非正常和非公休
				cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
				// 非请假批准
				cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));

				Webs.put(mb, "remarkIn", remarkIn);
				Webs.put(mb, "remarkOut", remarkOut);
			} else if (!remarkIn.equals("异常") && remarkOut.equals("异常")) {
				// 非正常和非公休
				cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
				// 非请假批准
				cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));

				Webs.put(mb, "remarkIn", remarkIn);
				Webs.put(mb, "remarkOut", remarkOut);
			} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
				cri.where().and(Cnd.exps("c.remark_in", "=", remarkIn).or(Cnd.exp("c.remark_out", "=", remarkOut)));
				Webs.put(mb, "remarkIn", remarkIn);
				Webs.put(mb, "remarkOut", remarkOut);
			} else if (remarkIn.equals("异常") && remarkOut.equals("异常")) {
				// 非正常和非公休
				cri.where().and(
						Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休"))
								.or(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休"))));
				// 非请假批准
				cri.where().and(
						Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null))
								.or(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null))));
				Webs.put(mb, "remarkIn", remarkIn);
				Webs.put(mb, "remarkOut", remarkOut);
			}
		} else {
			if (remarkIn.equals("异常")) {
				// 非正常和非公休
				cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
				// 非请假批准
				cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));
				Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
				Webs.put(mb, "remarkIn", remarkIn);
			} else if (remarkOut.equals("异常")) {
				// 非正常和非公休
				cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
				// 非请假批准
				cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));
				Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
				Webs.put(mb, "remarkOut", remarkOut);
			} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
				Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
				Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
			}
		}

		Cnds.gte(cri, mb, "c.work_date", "startTime", startStr);
		Cnds.lte(cri, mb, "c.work_date", "endTime", endStr);

		DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
		DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");

		int months = Months.monthsBetween(start, end).getMonths();

		for (int i = 0; i < months + 1; i++) {
			DateTime plus = start.plusMonths(i);

			if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM")))
				continue;

			Map<String, String> vars = new ConcurrentHashMap<String, String>();
			vars.put("month", plus.toString("yyyyMM"));
			List<CheckedRecord> checkedRecords = mapper.query(CheckedRecord.class, "CheckedRecord2.query", cri, null, vars);

			for (CheckedRecord e : checkedRecords) {
				attendances.add(e);
			}
		}

		Webs.put(mb, "startTime", startStr);
		Webs.put(mb, "endTime", endStr);
		Webs.put(mb, "corpId", corpId);

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		req.setAttribute("attendances", attendances);
		req.setAttribute("corps", corps);
		req.setAttribute("mb", mb);
	}

	private Object addUtil(HttpServletRequest req, HttpServletResponse res, User user) {
		MapBean mb = new MapBean();
		try {
			Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I, "用户");
			String workDate = Https.getStr(req, "workDate", R.D, "考勤日期");
			String remarkedIn = Https.getStr(req, "remarkedIn", R.CLEAN);
			String remarkedOut = Https.getStr(req, "remarkedOut", R.CLEAN);

			Map<Integer, WorkDay> dayMap = workRepository.dayMap();
			WorkDay day = dayMap.get(user.getDayId());
			Asserts.isNull(day, "日排班不能为空值");

			String month = Calendars.parse(workDate, Calendars.DATE).toString("yyyyMM");
			Map<String, String> vars = new ConcurrentHashMap<String, String>();
			vars.put("month", month);

			CheckedRecord attendance = mapper.fetch(CheckedRecord.class, "CheckedRecord.query", Cnd.where("c.user_id", "=", userId).and("c.work_date", "=", workDate), null, vars);
			Asserts.isNull(attendance, "考勤记录不存在");

			if (attendance.getVersion().equals(Status.ENABLED))
				throw new Errors("禁止修改已定版的考勤记录");

			if (Strings.isNotBlank(remarkedIn)) {
				attendance.setCheckedIn(day.getCheckIn());
				attendance.setRemarkIn("正常");
				attendance.setRemarkedIn(remarkedIn);
			}
			if (Strings.isNotBlank(remarkedOut)) {
				attendance.setCheckedOut(day.getCheckOut());
				attendance.setRemarkOut("正常");
				attendance.setRemarkedOut(remarkedOut);
			}

			update(month, attendance);

			Code.ok(mb, "考勤补录成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Attendance:add) error: ", e);
			Code.error(mb, "考勤补录失败");
		}

		return mb;
	}

	/**
	 * 考勤补录
	 * 
	 * @param req
	 * @param res
	 * @param user
	 * @return
	 */
	private Object newAddUtil(HttpServletRequest req, HttpServletResponse res, User user) {
		MapBean mb = new MapBean();
		try {
			Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I, "用户");
			String workDate = Https.getStr(req, "workDate", R.D, "考勤日期");
			String remarkedIn = Https.getStr(req, "remarkedIn", R.CLEAN);
			String remarkedOut = Https.getStr(req, "remarkedOut", R.CLEAN);
			Shift shift = mapper.fetch(Shift.class, "Shiftinner.query", Cnd.where("s.user_id", "=", userId).and("s.shift_date", "=", workDate));
			Asserts.isNull(shift, "当日没有排班");
			String checkIn = null;
			String checkOut = null;
			if (shift.getNight() == ShiftC.NIGHT_IN) {
				Shift yesterday = mapper.fetch(Shift.class, "Shiftinner.query",
						Cnd.where("s.user_id", "=", userId).and("s.shift_date", "=", Calendars.parse(workDate, Calendars.DATE).plusDays(-1).toString(Calendars.DATE)));
				checkIn = yesterday.getFirstNight();
				checkOut = shift.getFirstMorning();
			} else {
				checkIn = shift.getFirstMorning();
				if (shift.getSecond() == 1) {
					checkOut = shift.getSecondNight();
				} else {
					checkOut = shift.getFirstNight();
				}
			}
			String month = Calendars.parse(workDate, Calendars.DATE).toString("yyyyMM");
			Map<String, String> vars = new ConcurrentHashMap<String, String>();
			vars.put("month", month);

			CheckedRecord attendance = mapper.fetch(CheckedRecord.class, "CheckedRecord.query", Cnd.where("c.user_id", "=", userId).and("c.work_date", "=", workDate), null, vars);
			Asserts.isNull(attendance, "考勤记录不存在");

			if (attendance.getVersion().equals(Status.ENABLED))
				throw new Errors("禁止修改已定版的考勤记录");
			
			System.out.println(checkIn+":"+remarkedIn);
			if (Strings.isNotBlank(remarkedIn)) {
				if(remarkedIn.equals("旷工")){
					attendance.setRemarkIn("旷工");
				}else{
					attendance.setCheckedIn(checkIn);
					attendance.setRemarkIn("已补录");
				}

			}
			if (Strings.isNotBlank(remarkedOut)) {
				if(remarkedOut.equals("旷工")){
					attendance.setRemarkOut("旷工");
				}else{
					attendance.setCheckedOut(checkOut);
					attendance.setRemarkOut("已补录");
				}
			}

			update(month, attendance);

			Code.ok(mb, "考勤补录成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Attendance:add) error: ", e);
			Code.error(mb, "考勤补录失败");
		}

		return mb;
	}

	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		CSRF.validate(req);
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I, "用户");
		User user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId));
		Asserts.isNull(user, "用户不存在");
		// 获取公司使用的排班类型
		ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class, "ShiftCorp.query",
				Cnd.where("o.status", "=", Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", user.getCorpId()));
		// 新排班
		if (shiftCorp != null) {
			return newAddUtil(req, res, user);
		} else {
			return addUtil(req, res, user);
		}
	}

	private void update(String month, final CheckedRecord attendance) {
		TableName.run(month, new Runnable() {
			@Override
			public void run() {
				dao.update(attendance);
			}
		});
	}

	private void newPage2Util(HttpServletRequest req) {
		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);

		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);// 公司id
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);// 用户id,对于系统是唯一
		String remarkIn = Https.getStr(req, "remarkIn", R.CLEAN);// 上午
		String remarkOut = Https.getStr(req, "remarkOut", R.CLEAN);// 下午
		if (remarkIn == null)
			remarkIn = "";
		if (remarkOut == null)
			remarkOut = "";
		DateTime now = new DateTime();
		MapBean mb = new MapBean();

		List<CheckedRecord> attendances = new ArrayList<CheckedRecord>();

		if (corpId != null && Strings.isNotBlank(startStr) && Strings.isNotBlank(endStr)) {
			List<User> users = null;
			if (userId == null) {
				users = mapper.query(User.class, "User.query", Cnd.where("u.corp_id", "=", corpId).and("u.status", "=", Status.ENABLED));
				System.out.println(users.size());
			} else {
				User user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId).and("u.status", "=", Status.ENABLED));
				Webs.put(mb, "userId", userId);
				users = new ArrayList<User>();
				users.add(user);
			}

			for (User user : users) {
				Map<String, String[]> leaveDayMap = new ConcurrentHashMap<String, String[]>();
				Map<String, String[]> outworkDayMap = new ConcurrentHashMap<String, String[]>();
				Map<String, String[]> errandDayMap = new ConcurrentHashMap<String, String[]>();
				// 设置请假日期,一天分为上午和下午,{String,String}数组显示
				workService.dayMaps(user.getUserId(), startStr, endStr, leaveDayMap, outworkDayMap, errandDayMap);

				Criteria cri = Cnd.cri();
				cri.where().and("c.work_date", "<=", now.toString(Calendars.DATE)).and("c.user_id", "=", user.getUserId());

				if (Strings.isNotBlank(remarkIn) && Strings.isNotBlank(remarkOut)) {
					if (remarkIn.equals("异常") && !remarkOut.equals("异常")) {
						// 非正常和非公休
						cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
						// 非请假批准
						cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));

						Webs.put(mb, "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					} else if (!remarkIn.equals("异常") && remarkOut.equals("异常")) {
						// 非正常和非公休
						cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
						// 非请假批准
						cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));

						Webs.put(mb, "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
						cri.where().and(Cnd.exps("c.remark_in", "=", remarkIn).or(Cnd.exp("c.remark_out", "=", remarkOut)));
						Webs.put(mb, "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					} else if (remarkIn.equals("异常") && remarkOut.equals("异常")) {
						// 非正常和非公休
						cri.where().and(
								Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休"))
										.or(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休"))));
						// 非请假批准
						cri.where().and(
								Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null))
										.or(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null))));
						Webs.put(mb, "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					}
				} else {
					if (remarkIn.equals("异常")) {
						// 非正常和非公休
						cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
						// 非请假批准
						cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));
						Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
						Webs.put(mb, "remarkIn", remarkIn);
					} else if (remarkOut.equals("异常")) {
						// 非正常和非公休
						cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
						// 非请假批准
						cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));
						Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
						Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
						Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
					}
				}

				Cnds.gte(cri, mb, "c.work_date", "startTime", startStr);
				Cnds.lte(cri, mb, "c.work_date", "endTime", endStr);

				DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
				DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");

				int months = Months.monthsBetween(start, end).getMonths();

				for (int i = 0; i < months + 1; i++) {
					DateTime plus = start.plusMonths(i);

					if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM")))
						continue;

					Map<String, String> vars = new ConcurrentHashMap<String, String>();
					vars.put("month", plus.toString("yyyyMM"));
					List<CheckedRecord> checkedRecords = mapper.query(CheckedRecord.class, "CheckedRecord.query", cri, null, vars);

					for (CheckedRecord e : checkedRecords) {
						Shift shift = dao.fetch(Shift.class, Cnd.where("user_id", "=", user.getUserId()).and("shift_date", "=", e.getWorkDate()));
			
						if (shift != null) {
							if (shift.getClasses() == null) {
								attendances.add(e);
								continue;
							}
							ShiftClass shiftClass = dao.fetch(ShiftClass.class, shift.getClasses());
							if (shiftClass == null) {
								attendances.add(e);
								continue;
							}
							String checkIn = null;
							String checkOut = null;
							String restIn = null;
							String restOut = null;
							// 白班
							if (shiftClass.getNight() == ShiftC.DAY_IN) {
								// 二头班
								if (shiftClass.getSecond() == 1) {
									checkIn = shiftClass.getFirstMorning();
									restIn = shiftClass.getFirstNight();
									restOut = shiftClass.getSecondMorning();
									checkOut = shiftClass.getSecondNight();

								} else {
									checkIn = shiftClass.getFirstMorning();
									checkOut = shiftClass.getFirstNight();
									// 获取两个时间的中间值,2015-2-6 为任意时间
									DateTime fm = Calendars.parse("2015-2-6 " + checkIn, Calendars.DATE_TIME);
									DateTime fn = Calendars.parse("2015-2-6 " + checkOut, Calendars.DATE_TIME);
									long beginDate = (fn.toDate().getTime() + fm.toDate().getTime()) / 2;
									SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
									String middle = sdf.format(new Date(beginDate));
									restIn = middle;
									restOut = middle;
								}
								// 夜班
							} else if (shiftClass.getNight() == ShiftC.NIGHT_IN) {
								DateTime nowN = Calendars.parse(e.getWorkDate(), Calendars.DATE).plus(-1);
								Shift shiftN = dao.fetch(Shift.class, Cnd.where("user_id", "=", user.getUserId()).and("shift_date", "=", nowN.toString("yyyy-MM-dd")));
								if (shiftN == null)
									continue;
								ShiftClass shiftClassN = dao.fetch(ShiftClass.class, shiftN.getClasses());
								if (shiftClassN == null)
									continue;
								checkIn = shiftClassN.getFirstNight();
								checkOut = shiftClass.getFirstMorning();
								// 获取两个时间的中间值,2015-2-6 为任意时间
								DateTime fm = Calendars.parse("2015-2-6 " + checkIn, Calendars.DATE_TIME);
								DateTime fn = Calendars.parse("2015-2-6 " + checkOut, Calendars.DATE_TIME);
								long beginDate = (fn.toDate().getTime() + fm.toDate().getTime()) / 2;
								SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
								String middle = sdf.format(new Date(beginDate));
								restIn = middle;
								restOut = middle;
							} else {
								continue;
							}
							int works = Works.workMinute(checkIn, checkOut, restIn, restOut);
							// 当日日排班

							WorkDay day = new WorkDay();
							day.setCheckIn(checkIn);
							day.setCheckOut(checkOut);
							day.setRestIn(restIn);
							day.setRestOut(restOut);
							System.out.println(e.getWorkDate().toLocaleString());
							workService.hours(e, day, works, leaveDayMap, outworkDayMap, errandDayMap);
						}else{
							//周末加班情况的处理
							DateTime workDate = Calendars.parse(e.getWorkDate(), Calendars.DATE);
							Overtime overtime = dao.fetch(Overtime.class, Cnd.where("user_id", "=", user.getUserId()).and("startTime", ">", Calendars.str(workDate, "yyyy-MM-dd 00:00:00")).and("startTime", "<", Calendars.str(workDate, "yyyy-MM-dd 23:59:59")));
							if(overtime != null){
								//显示加班的时长
								e.setMinute(overtime.getWorkMinute());
							}
						}
						if (e.getRemarkedIn() != null || e.getRemarkedOut() != null) {
							editCheckRecord(e);
						}
						attendances.add(e);
					}
				}
			}
		}
		Webs.put(mb, "startTime", startStr);
		Webs.put(mb, "endTime", endStr);
		Webs.put(mb, "corpId", corpId);
		Webs.put(mb, "userId", userId);

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		req.setAttribute("attendances", attendances);
		req.setAttribute("corps", corps);
		req.setAttribute("mb", mb);
	}

	private void editCheckRecord(CheckedRecord e) {
		OrderBy cnd = Cnd.where("status", "=", Status.ENABLED).and("userId", "=", e.getUserId())
				.and("startTime", ">=", Calendars.str(e.getWorkDate(), Calendars.DATE) + " 00:00:00")
				.and("startTime", "<=", Calendars.str(e.getWorkDate(), Calendars.DATE) + " 23:59:59").desc("modify_time");
		List<Leave> leaves = dao.query(Leave.class, cnd);
		List<Outwork> outworks = dao.query(Outwork.class, cnd);
		List<Errand> errands = dao.query(Errand.class, cnd);
		Date l = null;
		Date o = null;
		Date er = null;
		Set<Date> set = new HashSet<Date>();
		set.add(l = leaves.size() > 0 ? leaves.get(0).getModifyTime() : null);
		set.add(o = outworks.size() > 0 ? outworks.get(0).getModifyTime() : null);
		set.add(er = errands.size() > 0 ? errands.get(0).getModifyTime() : null);
		Integer lh = l != null ? l.hashCode() : 0;
		Integer oh = o != null ? o.hashCode() : 0;
		Integer erh = er != null ? er.hashCode() : 0;
		set.remove(null);
		Iterator<Date> iterator = set.iterator();
		if (iterator.hasNext()) {

			Date first = iterator.next();
			while (iterator.hasNext()) {
				Date temp = iterator.next();
				if (first.getTime() < temp.getTime()) {
					first = temp;
				}
			}
			String out = StringUtils.isNotBlank(e.getRemarkedOut()) ? e.getRemarkedOut() : "";
			String in = StringUtils.isNotBlank(e.getRemarkedIn()) ? e.getRemarkedIn() : "";
			if (first.hashCode() == lh) {
				Leave leave = leaves.get(0);
				if (leave.getStartType().equals(Leave.AM) && leave.getEndType().equals(Leave.PM)) {
					e.setRemarkedOut(out + "[请假原因：" + leave.getReason() + "]");
				} else if (leave.getStartType().equals(Leave.AM) && leave.getEndType().equals(Leave.AM)) {
					e.setRemarkedIn(in + "[请假原因：" + leave.getReason() + "]");
				} else {
					e.setRemarkedOut(out + "[请假原因：" + leave.getReason() + "]");
				}
			} else if (first.hashCode() == oh) {
				Outwork outwork = outworks.get(0);
				if (outwork.getType().equals(Outwork.AM)) {
					e.setRemarkedIn(in + "[外勤原因：" + outwork.getReason() + "]");
				} else {
					e.setRemarkedOut(out + "[外勤原因：" + outwork.getReason() + "]");
				}
			} else if (first.hashCode() == erh) {
				Errand errand = errands.get(0);
				e.setRemarkedOut(e.getRemarkedIn() + "[出差描述：" + errand.getContent() + "]");
			}
		}
	}

	@GET
	@At
	@Ok("ftl:hrm/attendance_page")
	public void page2(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);
		CSRF.generate(req, "/hrm/attendance/coll", token);
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);// 公司id
		// 获取公司使用的排班类型
		ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class, "ShiftCorp.query", Cnd.where("o.status", "=", Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", corpId));
		// 新排班
		if (shiftCorp != null) {
			newPage2Util(req);
		} else {// 旧排班
			page2Util(req);
		}
	}

	private void page2Util(HttpServletRequest req) {

		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);

		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);// 公司id
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);// 用户id,对于系统是唯一
		String remarkIn = Https.getStr(req, "remarkIn", R.CLEAN);// 上午
		String remarkOut = Https.getStr(req, "remarkOut", R.CLEAN);// 下午

		DateTime now = new DateTime();
		MapBean mb = new MapBean();

		List<CheckedRecord> attendances = new ArrayList<CheckedRecord>();

		if (corpId != null && Strings.isNotBlank(startStr) && Strings.isNotBlank(endStr)) {
			List<User> users = null;
			if (userId == null) {
				users = mapper.query(User.class, "User.query", Cnd.where("u.corp_id", "=", corpId).and("u.status", "=", Status.ENABLED));
			} else {
				User user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId).and("u.status", "=", Status.ENABLED));
				Webs.put(mb, "userId", userId);
				users = new ArrayList<User>();
				users.add(user);
			}

			for (User user : users) {
				Map<Integer, WorkDay> dayMap = workRepository.dayMap();
				Map<Integer, String[]> weekMap = workRepository.weekMap();
				Map<String, Integer[]> monthMap = workRepository.monthMap(user.getCorpId());
				WorkDay day = dayMap.get(user.getDayId());
				Asserts.isNull(day, "日排班不能为空值");
				String[] weeks = weekMap.get(user.getWeekId());
				Asserts.isEmpty(weeks, "周排班不能为空值");
				int works = Works.workMinute(day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut());

				Map<String, String[]> leaveDayMap = new ConcurrentHashMap<String, String[]>();
				Map<String, String[]> outworkDayMap = new ConcurrentHashMap<String, String[]>();
				Map<String, String[]> errandDayMap = new ConcurrentHashMap<String, String[]>();
				// 设置请假日期
				workService.dayMaps(user.getUserId(), startStr, endStr, leaveDayMap, outworkDayMap, errandDayMap);

				Criteria cri = Cnd.cri();
				cri.where().and("c.work_date", "<=", now.toString(Calendars.DATE)).and("c.user_id", "=", user.getUserId());

				if (Strings.isNotBlank(remarkIn) && Strings.isNotBlank(remarkOut)) {
					if (remarkIn.equals("异常") && !remarkOut.equals("异常")) {
						// 非正常和非公休
						cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
						// 非请假批准
						cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));

						Webs.put(mb, "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					} else if (!remarkIn.equals("异常") && remarkOut.equals("异常")) {
						// 非正常和非公休
						cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
						// 非请假批准
						cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));

						Webs.put(mb, "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
						cri.where().and(Cnd.exps("c.remark_in", "=", remarkIn).or(Cnd.exp("c.remark_out", "=", remarkOut)));
						Webs.put(mb, "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					} else if (remarkIn.equals("异常") && remarkOut.equals("异常")) {
						// 非正常和非公休
						cri.where().and(
								Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休"))
										.or(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休"))));
						// 非请假批准
						cri.where().and(
								Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null))
										.or(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null))));
						Webs.put(mb, "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					}
				} else {
					if (remarkIn.equals("异常")) {
						// 非正常和非公休
						cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
						// 非请假批准
						cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));
						Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
						Webs.put(mb, "remarkIn", remarkIn);
					} else if (remarkOut.equals("异常")) {
						// 非正常和非公休
						cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
						// 非请假批准
						cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));
						Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
						Webs.put(mb, "remarkOut", remarkOut);
					} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
						Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
						Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
					}
				}

				Cnds.gte(cri, mb, "c.work_date", "startTime", startStr);
				Cnds.lte(cri, mb, "c.work_date", "endTime", endStr);

				DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
				DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");

				int months = Months.monthsBetween(start, end).getMonths();

				for (int i = 0; i < months + 1; i++) {
					DateTime plus = start.plusMonths(i);

					if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM")))
						continue;

					Map<String, String> vars = new ConcurrentHashMap<String, String>();
					vars.put("month", plus.toString("yyyyMM"));
					List<CheckedRecord> checkedRecords = mapper.query(CheckedRecord.class, "CheckedRecord.query", cri, null, vars);

					for (CheckedRecord e : checkedRecords) {
						workService.hours(e, day, weeks, monthMap, works, leaveDayMap, outworkDayMap, errandDayMap);
						attendances.add(e);
					}
				}
				req.setAttribute("works", works);
			}
		}
		Webs.put(mb, "startTime", startStr);
		Webs.put(mb, "endTime", endStr);
		Webs.put(mb, "corpId", corpId);
		Webs.put(mb, "userId", userId);

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		req.setAttribute("attendances", attendances);
		req.setAttribute("corps", corps);
		req.setAttribute("mb", mb);
	}

	@GET
	@At
	@Ok("ftl:hrm/attendance_page_wx")
	public void wxpage2(HttpServletRequest req) {
		MapBean mb = new MapBean();

		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);// 用户id,对于系统是唯一
		DateTime now = new DateTime();
		List<User> users = null;
		Integer corpId = Context.getCorpId();
		if (Asserts.hasAny(Roles.BOSS.getName(), Context.getRoles())) {
			users = mapper.query(User.class, "User.query", Cnd.where("u.status", "=", Status.ENABLED));
		} else {
			users = mapper.query(User.class, "User.query", Cnd.where("u.corp_id", "=", corpId).and("u.status", "=", Status.ENABLED));
		}

		List<CheckedRecord> attendances = new ArrayList<CheckedRecord>();

		if (userId != null && Strings.isNotBlank(startStr) && Strings.isNotBlank(endStr)) {

			Criteria cri = Cnd.cri();
			cri.where().and("c.work_date", "<=", now.toString(Calendars.DATE));
			Cnds.gte(cri, mb, "c.work_date", "startTime", startStr);
			Cnds.lte(cri, mb, "c.work_date", "endTime", endStr);
			Cnds.eq(cri, mb, "c.user_id", "userId", userId);

			DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
			DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");

			int months = Months.monthsBetween(start, end).getMonths();

			for (int i = 0; i < months + 1; i++) {
				DateTime plus = start.plusMonths(i);

				if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM")))
					continue;

				Map<String, String> vars = new ConcurrentHashMap<String, String>();
				vars.put("month", plus.toString("yyyyMM"));
				List<CheckedRecord> checkedRecords = mapper.query(CheckedRecord.class, "CheckedRecord.query", cri, null, vars);

				for (CheckedRecord e : checkedRecords) {
					attendances.add(e);
				}
			}
		}

		req.setAttribute("mb", mb);
		req.setAttribute("users", users);
		req.setAttribute("attendances", attendances);
	}

	/**
	 * 生成考勤汇总
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public Object collUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I, "所属公司");
			Integer userId = Https.getInt(req, "userId", R.I);
			String startStr = Https.getStr(req, "startTime", R.REQUIRED, R.D, "考勤开始日期");
			String endStr = Https.getStr(req, "endTime", R.REQUIRED, R.D, "考勤结束日期");

			WorkAttendance att = dao.fetch(WorkAttendance.class, corpId);
			boolean exist = true;
			if (att == null) {
				att = new WorkAttendance();
				att.setOrgId(corpId);
				exist = false;
			}
			att.setStartDate(Calendars.parse(startStr, Calendars.DATE).toDate());
			att.setEndDate(Calendars.parse(endStr, Calendars.DATE).toDate());
			if (exist)
				dao.update(att);
			else
				dao.insert(att);

			List<User> users = null;
			if (userId != null) {
				User user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId).and("u.status", "=", Status.ENABLED));
				Asserts.isNull(user, "用户不存在");
				users = new ArrayList<User>();
				users.add(user);
			} else {
				users = mapper.query(User.class, "User.query", Cnd.where("u.corp_id", "=", corpId).and("u.status", "=", Status.ENABLED));
			}

			Asserts.isEmpty(users, "用户集合不能为空值");

			List<AttendanceThreshold> thresholds = dao.query(AttendanceThreshold.class, Cnd.where("status", "=", Status.ENABLED));
			Asserts.isEmpty(thresholds, "考勤阈值集合不能为空");
			List<ConfLeave> leaves = dao.query(ConfLeave.class, Cnd.where("status", "=", Status.ENABLED));
			Asserts.isEmpty(leaves, "请假薪酬规则不能为空");
			// 修改过,添加.and("o.org_id","=",corpId),防止考勤汇总出错
			List<Org> corps = mapper.query(Org.class, "Org.query", Cnd.where("o.type", "=", Status.ENABLED).and("o.status", "=", Status.ENABLED).and("o.org_id", "=", corpId));

			Map<Integer, AttendanceThreshold> thresholdMap = new ConcurrentHashMap<Integer, AttendanceThreshold>();

			for (AttendanceThreshold threshold : thresholds) {
				Integer arr[] = Converts.array(threshold.getOrgIds(), ",");
				for (Integer a : arr) {
					thresholdMap.put(a, threshold);
				}
			}

			Map<Integer, ConfLeave> leavesMap = new ConcurrentHashMap<Integer, ConfLeave>();

			for (Org org : corps) {
				for (ConfLeave leave : leaves) {
					if (org.getConfLeaveId().equals(leave.getConfLeaveId()))
						leavesMap.put(org.getOrgId(), leave);
				}
			}
			DateTime startStr_yyyyMM = Calendars.parse(startStr, Calendars.DATE);
			DateTime endStr_yyyyMM = Calendars.parse(endStr, Calendars.DATE);
			List<User> interim = new ArrayList<User>();

			// 如果某子公司考勤阀值配置为空，则不汇总此公司的人
			for (User user : users) {
				AttendanceThreshold threshold = thresholdMap.get(user.getCorpId());
				if (threshold == null)
					continue;

				List<AttendanceThresholdItem> thresholdItem = dao.query(AttendanceThresholdItem.class,
						Cnd.where("thresholdId", "=", threshold.getThresholdId()).and("status", "=", Status.ENABLED));
				ConfLeave leave = leavesMap.get(user.getCorpId());
				if (leave == null)
					continue;

				Wage wage = dao.fetch(Wage.class, user.getUserId());
				if (wage == null)
					continue;

				DateTime effectTime_yyyyMM = Calendars.parse(wage.getEffectTime(), Calendars.DATE);
				DateTime entry_yyyyMM = Calendars.parse(user.getEntryDate(), Calendars.DATE);
				if (effectTime_yyyyMM.isAfter(startStr_yyyyMM) && effectTime_yyyyMM.isBefore(endStr_yyyyMM) && !entry_yyyyMM.isEqual(effectTime_yyyyMM)) {
					interim.add(user);
					continue;
				}
				user.setInterim("");
				resultService.coll(user, thresholdItem, startStr, endStr, Context.getUserId(), startStr, endStr);
			}
			for (User user : interim) {

				AttendanceThreshold threshold = thresholdMap.get(user.getCorpId());
				if (threshold == null)
					continue;

				List<AttendanceThresholdItem> thresholdItem = dao.query(AttendanceThresholdItem.class, Cnd.where("thresholdId", "=", threshold.getThresholdId()));
				ConfLeave leave = leavesMap.get(user.getCorpId());
				if (leave == null)
					continue;

				Wage wage = dao.fetch(Wage.class, user.getUserId());
				if (wage == null)
					continue;

				String effectTime = Calendars.str(wage.getEffectTime(), Calendars.DATE);
				DateTime start_yyyyMMNext = Calendars.parse(wage.getEffectTime(), Calendars.DATE).plusDays(-1);
				String effectTimeNext = Calendars.str(start_yyyyMMNext, Calendars.DATE);
				user.setInterim("interim1");
				resultService.coll(user, thresholdItem, effectTime, endStr, Context.getUserId(), startStr, endStr);
				user.setInterim("interim2");
				resultService.coll(user, thresholdItem, startStr, effectTimeNext, Context.getUserId(), startStr, endStr);
			}

			Code.ok(mb, "生成汇总成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Attendance:coll) error: ", e);
			Code.error(mb, "生成汇总失败");
		}

		return mb;
	}

	/**
	 * 生成考勤汇总
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	public Object newCollUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I, "所属公司");
			Integer userId = Https.getInt(req, "userId", R.I);
			String startStr = Https.getStr(req, "startTime", R.REQUIRED, R.D, "考勤开始日期");
			String endStr = Https.getStr(req, "endTime", R.REQUIRED, R.D, "考勤结束日期");

			List<AttendanceThreshold> thresholds = dao.query(AttendanceThreshold.class, Cnd.where("status", "=", Status.ENABLED));
			Asserts.isEmpty(thresholds, "考勤阈值集合不能为空");
			AttendanceThreshold threshold = null;
			for (AttendanceThreshold tmpAt : thresholds) {
				Integer arr[] = Converts.array(tmpAt.getOrgIds(), ",");
				for (Integer a : arr) {
					if (corpId.equals(a)) {
						threshold = tmpAt;
					}
				}
			}
			// 如果某子公司**考勤阀值配置**为空，则不汇总此公司的人
			Asserts.isNull(threshold, "该公司未配置相应的的考勤阈值！");
			List<AttendanceThresholdItem> thresholdItem = dao.query(AttendanceThresholdItem.class,
					Cnd.where("thresholdId", "=", threshold.getThresholdId()).and("status", "=", Status.ENABLED));

			// 获取公司使用的排班类型
			ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class, "ShiftCorp.query",
					Cnd.where("o.status", "=", Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", corpId));
			boolean isNew = false;
			// 新排班
			if (shiftCorp != null) {
				isNew = true;
			}

			List<String> maiList = new ArrayList<String>();
			/* 获取用户集合-- */
			List<User> users = null;
			if (userId != null) {
				User user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId).and("u.status", "=", Status.ENABLED));
				Asserts.isNull(user, "用户不存在");
				users = new ArrayList<User>();
				users.add(user);
			} else {
				users = mapper.query(User.class, "User.query", Cnd.where("u.corp_id", "=", corpId).and("u.status", "=", Status.ENABLED));
			}
			Asserts.isEmpty(users, "用户集合不能为空值");

			DateTime startStr_yyyyMM = Calendars.parse(startStr, Calendars.DATE);// 汇总起始时间
			DateTime endStr_yyyyMM = Calendars.parse(endStr, Calendars.DATE);// 汇总截止时间
			for (User user : users) {
				// 如果此人的 **工资配置**为空，则不汇总此人
				Wage wage = dao.fetch(Wage.class, user.getUserId());
				if (wage == null) {
					log.error("汇总放弃：工资配置为空！工号:" + user.getJobNumber());
					continue;
				}

				String email = user.getEmail();
				if (StringUtils.isNotEmpty(email) && !MailC.checkEmail(email)) {
					maiList.add(email);
				}

				// 工资生效时间大于入职时间,并且小于汇总时间
				DateTime effectTime_yyyyMM = Calendars.parse(wage.getEffectTime(), Calendars.DATE);
				DateTime entry_yyyyMM = Calendars.parse(user.getEntryDate(), Calendars.DATE);

				// 月中调薪的员工出2条汇总
				if (effectTime_yyyyMM.isAfter(startStr_yyyyMM) && effectTime_yyyyMM.isBefore(endStr_yyyyMM) && !entry_yyyyMM.isEqual(effectTime_yyyyMM)) {
					String effectTime = Calendars.str(wage.getEffectTime(), Calendars.DATE);
					DateTime start_yyyyMMNext = Calendars.parse(wage.getEffectTime(), Calendars.DATE).plusDays(-1);
					String effectTimeNext = Calendars.str(start_yyyyMMNext, Calendars.DATE);
					user.setInterim("interim1");
					if (isNew) {
						resultService.newColl(user, thresholdItem, effectTime, endStr, Context.getUserId(), startStr, endStr);
					} else {
						resultService.coll(user, thresholdItem, effectTime, endStr, Context.getUserId(), startStr, endStr);
					}
					user.setInterim("interim2");
					if (isNew) {
						resultService.newColl(user, thresholdItem, startStr, effectTimeNext, Context.getUserId(), startStr, endStr);
					} else {
						resultService.coll(user, thresholdItem, startStr, effectTimeNext, Context.getUserId(), startStr, endStr);
					}
					continue;
				}
				user.setInterim("");
				if (isNew) {
					resultService.newColl(user, thresholdItem, startStr, endStr, Context.getUserId(), startStr, endStr);
				} else {
					resultService.coll(user, thresholdItem, startStr, endStr, Context.getUserId(), startStr, endStr);
				}
			}

			/* 插入考勤日期-- */
			WorkAttendance att = dao.fetch(WorkAttendance.class, corpId);
			boolean exist = true;
			if (att == null) {
				att = new WorkAttendance();
				att.setOrgId(corpId);
				exist = false;
			}
			att.setStartDate(Calendars.parse(startStr, Calendars.DATE).toDate());
			att.setEndDate(Calendars.parse(endStr, Calendars.DATE).toDate());
			if (exist)
				dao.update(att);
			else
				dao.insert(att);

			String subject = dao.fetch(Org.class, Cnd.where("org_id", "=", corpId)).getOrgName() + " 已考勤定版完成!";
			String content = "请" + "各位员工" + "登录OA进行考勤汇总核对";
			MailStart mail = new MailStart();
			String mailR = mail.mail(maiList, subject, content);
			Code.ok(mb, "生成汇总完成，" + " , " + mailR);
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Attendance:coll) error: ", e);
			Code.error(mb, "生成汇总失败");
		}

		return mb;
	}

	/**
	 * 生成汇总
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@POST
	@At
	@Ok("json")
	public Object coll(HttpServletRequest req, HttpServletResponse res) {
		return newCollUtil(req, res);
	}

	@POST
	@At
	@Ok("json")
	public Object change(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String corpIds = Https.getStr(req, "modelOrgs", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			Integer[] arrCorp = Converts.array(corpIds, ",");
			List<User> users = null;
			Criteria cri = Cnd.cri();
			if (arrCorp != null) {
				cri.where().and("u.corp_id", "in", arrCorp).and("u.status", "=", Status.ENABLED).and("u.true_name", "<>", "系统管理员");
				users = mapper.query(User.class, "User.role", cri);
			} else {
				users = new ArrayList<User>();
			}
			mb.put("users", users);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Attendance:able) error: ", e);
			Code.error(mb, "查找用户");
		}
		return mb;
	}
	
	/**
	 * 批量补录页面
	 * @param req
	 * @param res
	 */
	@GET
	@At
	@Ok("ftl:hrm/batchRecord")
	public void batchRecord(HttpServletRequest req, HttpServletResponse res) {
		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);
		Integer corpId = Https.getInt(req, "corpId", R.I);
		Integer userId = Https.getInt(req, "userId", R.I);
		String remarkIn = Https.getStr(req, "remarkIn", R.CLEAN);
		String remarkOut = Https.getStr(req, "remarkOut", R.CLEAN);
		List<Org> orgs = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("parent_id", "=", 0));
		
		List<User> operators = null;
		
		User manager = dao.fetch(User.class, Context.getUser().getManagerId());
		if(manager != null){
			operators = new ArrayList<User>();
			operators.add(manager);
		} else{
			operators = userService.operators(Context.getCorpId(), Context.getLevel());
		}
		req.setAttribute("operators", operators);
		
		req.setAttribute("orgs", orgs);
		req.setAttribute("startTime", startStr);
		req.setAttribute("endTime", endStr);
		req.setAttribute("corpId", corpId);
		req.setAttribute("userId", userId);
		req.setAttribute("remarkIn", remarkIn);
		req.setAttribute("remarkOut", remarkOut);
	}

	/**
	 * 批量补录提交
	 * 
	 * @param req
	 * @param res
	 * @return
	 */
	@POST
	@At
	@Ok("json")
	public Object record(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String corpIds = Https.getStr(req, "modelOrgs", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司");
			String userIds = Https.getStr(req, "modelUsers", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "员工");
			String startStr = Https.getStr(req, "start_yyyyMMdd", R.D, R.REQUIRED, "补录日期");
			Integer startType = Https.getInt(req, "startType", R.I, R.REQUIRED, "补录时间段");
			String redressDesc = Https.getStr(req, "redressDesc", R.REQUIRED, "补录原因");
			Integer operatorId = Https.getInt(req, "operatorId", R.I, R.REQUIRED, "审批人");
			Integer[] arrCorp = Converts.array(corpIds, ",");
			Integer[] arrUser = Converts.array(userIds, ",");
			Criteria cri = Cnd.cri();
			if (arrCorp == null) {
				throw new Errors("公司不存在");
			}
			cri.where().and("u.corp_id", "in", arrCorp);
			if (arrUser != null) {
				cri.where().and("u.user_id", "in", arrUser);
			}
			cri.where().and("u.status", "=", Status.ENABLED);
			List<User> users = mapper.query(User.class, "User.role", cri);
			transSave(users, startStr, startType, redressDesc, operatorId);
			Code.ok(mb, "批量补录已提交，等待审批");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Attendance:able) error: ", e);
			Code.error(mb, "批量补录提交失败");
		}

		return mb;
	}

	/**
	 * 导出考勤记录
	 * 
	 * @param req
	 * @param res
	 */
	@GET
	@At
	public void download(HttpServletRequest req, HttpServletResponse res) {
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);// 公司id
		// 获取公司使用的排班类型
		ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class, "ShiftCorp.query", Cnd.where("o.status", "=", Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", corpId));
		// 新排班
		if (shiftCorp != null) {
			newDownloadUtil(req, res);
		} else {
			downloadUtil(req, res);
		}
	}

	public void newDownloadUtil(HttpServletRequest req, HttpServletResponse res) {
		try {
			String startStr = Https.getStr(req, "startTime", R.D);
			String endStr = Https.getStr(req, "endTime", R.D);

			Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);
			Integer userId = Https.getInt(req, "userId", R.I);
			String remarkIn = Https.getStr(req, "remarkIn", R.CLEAN);
			String remarkOut = Https.getStr(req, "remarkOut", R.CLEAN);

			DateTime now = new DateTime();
			MapBean mb = new MapBean();

			ServletOutputStream output = Webs.output(req, res, Encodes.urlEncode("考勤记录." + Suffix.XLSX));
			Excels excels = new Excels(Suffix.XLSX);
			List<List<Data>> rowList = new ArrayList<List<Data>>();
			rowList.add(CheckedRecords.subjects(true));

			if (corpId != null && Strings.isNotBlank(startStr) && Strings.isNotBlank(endStr)) {
				List<User> users = null;
				if (userId == null) {
					users = mapper.query(User.class, "User.query", Cnd.where("u.corp_id", "=", corpId).and("u.status", "=", Status.ENABLED));
				} else {
					User user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId).and("u.status", "=", Status.ENABLED));
					users = new ArrayList<User>();
					users.add(user);
				}

				for (User user : users) {

					Map<String, String[]> leaveDayMap = new ConcurrentHashMap<String, String[]>();
					Map<String, String[]> outworkDayMap = new ConcurrentHashMap<String, String[]>();
					Map<String, String[]> errandDayMap = new ConcurrentHashMap<String, String[]>();
					workService.dayMaps(user.getUserId(), startStr, endStr, leaveDayMap, outworkDayMap, errandDayMap);

					Criteria cri = Cnd.cri();
					cri.where().and("c.work_date", "<=", now.toString(Calendars.DATE)).and("c.user_id", "=", user.getUserId());

					if (Strings.isNotBlank(remarkIn) && Strings.isNotBlank(remarkOut)) {

						if (remarkIn.equals("异常") && !remarkOut.equals("异常")) {
							// 非正常和非公休
							cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
							// 非请假批准
							cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));

							Webs.put(mb, "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						} else if (!remarkIn.equals("异常") && remarkOut.equals("异常")) {
							// 非正常和非公休
							cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
							// 非请假批准
							cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));

							Webs.put(mb, "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
							cri.where().and(Cnd.exps("c.remark_in", "=", remarkIn).or(Cnd.exp("c.remark_out", "=", remarkOut)));
							Webs.put(mb, "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						} else if (remarkIn.equals("异常") && remarkOut.equals("异常")) {
							// 非正常和非公休
							cri.where().and(
									Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休"))
											.or(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休"))));
							// 非请假批准
							cri.where().and(
									Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null))
											.or(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null))));
							Webs.put(mb, "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						}
					} else {
						if (remarkIn.equals("异常")) {
							// 非正常和非公休
							cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
							// 非请假批准
							cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));
							Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
							Webs.put(mb, "remarkIn", remarkIn);
						} else if (remarkOut.equals("异常")) {
							// 非正常和非公休
							cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
							// 非请假批准
							cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));
							Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
							Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
							Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
						}
					}

					Cnds.gte(cri, mb, "c.work_date", "startTime", startStr);
					Cnds.lte(cri, mb, "c.work_date", "endTime", endStr);

					DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
					DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");

					int months = Months.monthsBetween(start, end).getMonths();

					for (int i = 0; i < months + 1; i++) {
						DateTime plus = start.plusMonths(i);

						if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM")))
							continue;

						Map<String, String> vars = new ConcurrentHashMap<String, String>();
						vars.put("month", plus.toString("yyyyMM"));
						List<CheckedRecord> checkedRecords = mapper.query(CheckedRecord.class, "CheckedRecord.query", cri, null, vars);

						for (CheckedRecord e : checkedRecords) {
							Shift shift = dao.fetch(Shift.class, Cnd.where("user_id", "=", user.getUserId()).and("shift_date", "=", e.getWorkDate()));
							if (shift != null) {
								if (shift.getClasses() == null) {
									e.setMinute(0);
									rowList.add(CheckedRecords.cells(views, e, true));
									continue;
								}
								ShiftClass shiftClass = dao.fetch(ShiftClass.class, shift.getClasses());
								if (shiftClass == null) {
									e.setMinute(0);
									rowList.add(CheckedRecords.cells(views, e, true));
									continue;
								}
								String checkIn = null;
								String checkOut = null;
								String restIn = null;
								String restOut = null;
								boolean isNight = false;
								// 白班
								if (shiftClass.getNight() == ShiftC.DAY_IN) {
									// 二头班
									if (shiftClass.getSecond() == 1) {
										checkIn = shiftClass.getFirstMorning();
										restIn = shiftClass.getFirstNight();
										restOut = shiftClass.getSecondMorning();
										checkOut = shiftClass.getSecondNight();

									} else {
										checkIn = shiftClass.getFirstMorning();
										checkOut = shiftClass.getFirstNight();
										// 获取两个时间的中间值,2015-2-6 为任意时间
										DateTime fm = Calendars.parse("2015-2-6 " + checkIn, Calendars.DATE_TIME);
										DateTime fn = Calendars.parse("2015-2-6 " + checkOut, Calendars.DATE_TIME);
										long beginDate = (fn.toDate().getTime() + fm.toDate().getTime()) / 2;
										SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
										String middle = sdf.format(new Date(beginDate));
										restIn = middle;
										restOut = middle;
									}
									// 夜班
								} else if (shiftClass.getNight() == ShiftC.NIGHT_IN) {
									DateTime nowN = Calendars.parse(shift.getShiftDate(), Calendars.DATE).plus(-1);
									Shift shiftN = dao.fetch(Shift.class, Cnd.where("user_id", "=", user.getUserId()).and("shift_date", "=", nowN.toString("yyyy-MM-dd")));
									if (shiftN == null)
										continue;
									ShiftClass shiftClassN = dao.fetch(ShiftClass.class, shiftN.getClasses());
									if (shiftClassN == null)
										continue;
									checkIn = shiftClassN.getFirstNight();
									checkOut = shiftClass.getFirstMorning();
									// 获取两个时间的中间值,2015-2-6 为任意时间
									DateTime fm = Calendars.parse("2015-2-6 " + checkIn, Calendars.DATE_TIME);
									DateTime fn = Calendars.parse("2015-2-6 " + checkOut, Calendars.DATE_TIME);
									long beginDate = (fn.toDate().getTime() + fm.toDate().getTime()) / 2;
									SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
									String middle = sdf.format(new Date(beginDate));
									restIn = middle;
									restOut = middle;
									isNight = true;
								} else {
									continue;
								}
								int works = Works.workMinute(checkIn, checkOut, restIn, restOut);
								if (isNight) {
									works = 1440 - works;
								}
								// 当日日排班
								WorkDay day = new WorkDay();
								day.setCheckIn(checkIn);
								day.setCheckOut(checkOut);
								day.setRestIn(restIn);
								day.setRestOut(restOut);
								workService.hours(e, day, works, leaveDayMap, outworkDayMap, errandDayMap);
							} else {
								e.setMinute(0);
							}
							rowList.add(CheckedRecords.cells(views, e, true));
						}
					}
				}
			}
			excels.write(output, rowList, "考勤记录");
		} catch (Exception e) {
			log.error("(Attendance:download) error: ", e);
		}
	}

	public void downloadUtil(HttpServletRequest req, HttpServletResponse res) {
		try {
			String startStr = Https.getStr(req, "startTime", R.D);
			String endStr = Https.getStr(req, "endTime", R.D);

			Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);
			Integer userId = Https.getInt(req, "userId", R.I);
			String remarkIn = Https.getStr(req, "remarkIn", R.CLEAN);
			String remarkOut = Https.getStr(req, "remarkOut", R.CLEAN);

			DateTime now = new DateTime();
			MapBean mb = new MapBean();

			ServletOutputStream output = Webs.output(req, res, Encodes.urlEncode("考勤记录." + Suffix.XLSX));
			Excels excels = new Excels(Suffix.XLSX);
			List<List<Data>> rowList = new ArrayList<List<Data>>();
			rowList.add(CheckedRecords.subjects(true));

			if (corpId != null && Strings.isNotBlank(startStr) && Strings.isNotBlank(endStr)) {
				List<User> users = null;
				if (userId == null) {
					users = mapper.query(User.class, "User.query", Cnd.where("u.corp_id", "=", corpId).and("u.status", "=", Status.ENABLED));
				} else {
					User user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId).and("u.status", "=", Status.ENABLED));
					users = new ArrayList<User>();
					users.add(user);
				}

				for (User user : users) {
					Map<Integer, WorkDay> dayMap = workRepository.dayMap();
					Map<Integer, String[]> weekMap = workRepository.weekMap();
					Map<String, Integer[]> monthMap = workRepository.monthMap(user.getCorpId());

					WorkDay day = dayMap.get(user.getDayId());
					Asserts.isNull(day, "日排班不能为空值");
					String[] weeks = weekMap.get(user.getWeekId());
					Asserts.isEmpty(weeks, "周排班不能为空值");

					int works = Works.workMinute(day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut());

					Map<String, String[]> leaveDayMap = new ConcurrentHashMap<String, String[]>();
					Map<String, String[]> outworkDayMap = new ConcurrentHashMap<String, String[]>();
					Map<String, String[]> errandDayMap = new ConcurrentHashMap<String, String[]>();
					workService.dayMaps(user.getUserId(), startStr, endStr, leaveDayMap, outworkDayMap, errandDayMap);

					Criteria cri = Cnd.cri();
					cri.where().and("c.work_date", "<=", now.toString(Calendars.DATE)).and("c.user_id", "=", user.getUserId());

					if (Strings.isNotBlank(remarkIn) && Strings.isNotBlank(remarkOut)) {

						if (remarkIn.equals("异常") && !remarkOut.equals("异常")) {
							// 非正常和非公休
							cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
							// 非请假批准
							cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));

							Webs.put(mb, "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						} else if (!remarkIn.equals("异常") && remarkOut.equals("异常")) {
							// 非正常和非公休
							cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
							// 非请假批准
							cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));

							Webs.put(mb, "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
							cri.where().and(Cnd.exps("c.remark_in", "=", remarkIn).or(Cnd.exp("c.remark_out", "=", remarkOut)));
							Webs.put(mb, "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						} else if (remarkIn.equals("异常") && remarkOut.equals("异常")) {
							// 非正常和非公休
							cri.where().and(
									Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休"))
											.or(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休"))));
							// 非请假批准
							cri.where().and(
									Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null))
											.or(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null))));
							Webs.put(mb, "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						}
					} else {
						if (remarkIn.equals("异常")) {
							// 非正常和非公休
							cri.where().and(Cnd.exps("c.remark_in", "!=", "正常").and(Cnd.exp("c.remark_in", "!=", "公休")).or(Cnd.exp("c.remark_out", "=", remarkOut)));
							// 非请假批准
							cri.where().and(Cnd.exps("c.remarked_in", "not like", "%已批准%").or(Cnd.exp("c.remarked_in", "is", null)));
							Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
							Webs.put(mb, "remarkIn", remarkIn);
						} else if (remarkOut.equals("异常")) {
							// 非正常和非公休
							cri.where().and(Cnd.exps("c.remark_out", "!=", "正常").and(Cnd.exp("c.remark_out", "!=", "公休")).or(Cnd.exp("c.remark_in", "=", remarkIn)));
							// 非请假批准
							cri.where().and(Cnd.exps("c.remarked_out", "not like", "%已批准%").or(Cnd.exp("c.remarked_out", "is", null)));
							Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
							Webs.put(mb, "remarkOut", remarkOut);
						} else if (!remarkIn.equals("异常") && !remarkOut.equals("异常")) {
							Cnds.eq(cri, mb, "c.remark_in", "remarkIn", remarkIn);
							Cnds.eq(cri, mb, "c.remark_out", "remarkOut", remarkOut);
						}
					}

					Cnds.gte(cri, mb, "c.work_date", "startTime", startStr);
					Cnds.lte(cri, mb, "c.work_date", "endTime", endStr);

					DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
					DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");

					int months = Months.monthsBetween(start, end).getMonths();

					for (int i = 0; i < months + 1; i++) {
						DateTime plus = start.plusMonths(i);

						if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM")))
							continue;

						Map<String, String> vars = new ConcurrentHashMap<String, String>();
						vars.put("month", plus.toString("yyyyMM"));
						List<CheckedRecord> checkedRecords = mapper.query(CheckedRecord.class, "CheckedRecord.query", cri, null, vars);

						for (CheckedRecord e : checkedRecords) {
							workService.hours(e, day, weeks, monthMap, works, leaveDayMap, outworkDayMap, errandDayMap);
							rowList.add(CheckedRecords.cells(views, e, true));
						}
					}
				}
			}
			System.out.println(rowList.size());
			excels.write(output, rowList, "考勤记录");
		} catch (Exception e) {
			log.error("(Attendance:download) error: ", e);
		}
	}
	
	private void transSave(final List<User> users, final String startStr, final Integer startType, final String redressDesc, final Integer operatorId) {
		TableName.run(startStr, new Runnable() {
			@Override
			public void run() {
				Map<String, String> vars = new ConcurrentHashMap<String, String>();
				vars.put("month", Calendars.parse(startStr, Calendars.DATE).toString("yyyyMM"));
				for (User user : users) {
					Shift shift = mapper.fetch(Shift.class, "Shiftinner.query", Cnd.where("s.user_id", "=", user.getUserId()).and("s.shift_date", "=", startStr));
					if (null == shift) {
						log.error("批量补录放弃:当日没有排班，工号：" + user.getJobNumber());
						continue;
					}
					
					CheckedRecord attendance = mapper.fetch(CheckedRecord.class, "CheckedRecord.query",
							Cnd.where("c.user_id", "=", user.getUserId()).and("c.work_date", "=", startStr), null, vars);
					if (null == attendance) {
						log.error("批量补录放弃:考勤记录不存在，工号：" + user.getJobNumber());
						continue;
					}
					if (attendance.getVersion().equals(Status.ENABLED))
						throw new Errors("禁止修改已定版的考勤记录");
					
					/*
					String checkIn = null;
					String checkOut = null;
					if (shift.getNight() == ShiftC.NIGHT_IN) {
						Shift yesterday = mapper.fetch(
								Shift.class,
								"Shiftinner.query",
								Cnd.where("s.user_id", "=", user.getUserId()).and("s.shift_date", "=",
										Calendars.parse(startStr, Calendars.DATE).plusDays(-1).toString(Calendars.DATE)));
						checkIn = yesterday.getFirstNight();
						checkOut = shift.getFirstMorning();
					} else {
						checkIn = shift.getFirstMorning();
						if (shift.getSecond() == ShiftC.ENABLED) {
							checkOut = shift.getSecondNight();
						} else {
							checkOut = shift.getFirstNight();
						}
					}
					if (startType == 0) {
						attendance.setCheckedIn(checkIn);
						attendance.setRemarkIn("已补录");
					} else {
						attendance.setCheckedOut(checkOut);
						attendance.setRemarkOut("已补录");
					}
					dao.update(attendance);*/
					
					String checkedTime = startType == 0 ? attendance.getCheckedIn() : attendance.getCheckedOut();
					RedressRecord redressRecord = new RedressRecord(user.getUserId(), startStr, startType, redressDesc, Context.getUserId(), operatorId, checkedTime);
					dao.insert(redressRecord);
					
				}
			}
		});
	}
}