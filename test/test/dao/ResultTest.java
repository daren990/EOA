//package test.dao;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.joda.time.DateTime;
//import org.joda.time.Days;
//import org.joda.time.Months;
//import org.junit.Test;
//import org.nutz.dao.Cnd;
//import org.nutz.dao.Sqls;
//import org.nutz.dao.sql.Criteria;
//import org.nutz.dao.sql.Sql;
//
//import test.Setup;
//import cn.oa.consts.Check;
//import cn.oa.consts.Status;
//import cn.oa.model.CheckedRecord;
//import cn.oa.model.Dict;
//import cn.oa.model.Errand;
//import cn.oa.model.Leave;
//import cn.oa.model.Outwork;
//import cn.oa.model.Overtime;
//import cn.oa.model.User;
//import cn.oa.model.Wage;
//import cn.oa.model.WorkDay;
//import cn.oa.repository.WorkRepository;
//import cn.oa.service.DictService;
//import cn.oa.utils.Calendars;
//import cn.oa.utils.RMB;
//import cn.oa.utils.Strings;
//import cn.oa.utils.helper.Works;
//
//public class ResultTest extends Setup {
//
//	@Test
//	public void result() {
//		Integer userId = 5;
//		
//		String startStr = "2014-04-01";
//		String endStr = "2014-04-30";
//		
//		WorkRepository workRepository = ioc.get(WorkRepository.class);
//		DictService dictService = ioc.get(DictService.class);
//		
//		User user = mapper.fetch(User.class, "User.query", Cnd
//				.where("u.user_id", "=", userId)
//				.and("u.status", "=", Status.ENABLED));
//		
//		Criteria cri = Cnd
//				.where("status", "=", Status.ENABLED)
//				.and("userId", "=", userId)
//				.and("approve", "=", Status.APPROVED)
//				.and("startTime", ">=", startStr + " 00:00:00")
//				.and("startTime", "<=", endStr + " 23:59:59");
//		
//		List<Leave> leaves = dao.query(Leave.class, cri);
//		List<Outwork> outworks = dao.query(Outwork.class, cri);
//		List<Errand> errands = dao.query(Errand.class, cri);
//		List<Overtime> overtimes = dao.query(Overtime.class, cri);
//		
//		Map<Integer, WorkDay> dayMap = workRepository.dayMap();
//		Map<Integer, String[]> weekMap = workRepository.weekMap();
//		
//		WorkDay day = dayMap.get(user.getDayId());
//		String[] weekOfDays = weekMap.get(user.getWeekId());
//		Map<String, Integer[]> monthOfDays = workRepository.monthMap(user.getCorpId());
//		
//		Map<String, String> dictMap = dictService.map(Dict.LEAVE);
//		
//		int workDay = Works.workDay(startStr, endStr, monthOfDays, weekOfDays);
//		int workMinute = Works.workMinute(day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut());
//		
//		// 1) 旷工, 迟到, 早退
//		int[] times = times(userId, startStr, endStr, leaves, day);
//		// 2) 请假
//		int unpaidLeaveMinute = leave(startStr, endStr, "无薪事假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
//		int sickLeaveMinute = leave(startStr, endStr, "病假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
//		int funeralLeaveMinute = leave(startStr, endStr, "丧假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
//		int maritalLeaveMinute = leave(startStr, endStr, "婚假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
//		int annualLeaveMinute = leave(startStr, endStr, "年休假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
//		int injuryLeaveMinute = leave(startStr, endStr, "工伤假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
//		int deferredLeaveMinute = leave(startStr, endStr, "加班补休", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
//		int paternityLeaveMinute = leave(startStr, endStr, "陪产假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
//		int paidLeaveMinute = leave(startStr, endStr, "有薪事假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
//		// 3) 外勤
//		int outworkMinute = outwork(startStr, endStr, outworks, workMinute, day, monthOfDays, weekOfDays);
//		// 4) 出差
//		int errandMinute = errand(startStr, endStr, errands, outworkMinute, day, monthOfDays, weekOfDays);
//		// 5) 加班
//		int overtimeMinute = overtime(startStr, endStr, overtimes, outworkMinute, day, monthOfDays, weekOfDays);
//		
//		Sql sql = Sqls.create(dao.sqls().get("AttendanceResult.updates"));
//		sql.params()
//				.set("resultMonth", Calendars.parse(endStr, Calendars.DATE).toString("yyyyMM"))
//				.set("userId", userId)
//				.set("workDay", workDay)
//				.set("workMinute", workMinute)
//				
//				.set("lateMinTimes", times[0])
//				.set("lateMaxTimes", times[1])
//				.set("earlyMinTimes", times[2])
//				.set("earlyMaxTimes", times[3])
//				.set("absentTimes", times[4])
//				
//				.set("unpaidLeaveMinute", unpaidLeaveMinute)
//				.set("sickLeaveMinute", sickLeaveMinute)
//				.set("funeralLeaveMinute", funeralLeaveMinute)
//				.set("maritalLeaveMinute", maritalLeaveMinute)
//				.set("annualLeaveMinute", annualLeaveMinute)
//				.set("injuryLeaveMinute", injuryLeaveMinute)
//				.set("deferredLeaveMinute", deferredLeaveMinute)
//				.set("paternityLeaveMinute", paternityLeaveMinute)
//				.set("paidLeaveMinute", paidLeaveMinute)
//				.set("outworkMinute", outworkMinute)
//				.set("errandMinute", errandMinute)
//				.set("overtimeMinute", overtimeMinute)
//				
//				.set("version", Status.DISABLED)
//				.set("modifyId", null)
//				.set("modifyTime", null);
//		dao.execute(sql);
//		
//		
//		Wage wage = dao.fetch(Wage.class, userId);
//		
//		
//		// 工资条
//		sql = Sqls.create(dao.sqls().get("WageResult.updates"));
//		
//		// 每分钟工资
//		int salary = wage.getStandardSalary() + wage.getPostSalary() + wage.getFloatingSalary();
//		int salaryPreDay = salary / workDay;
//		int salaryPreMinute = salaryPreDay / workMinute;
//		
//		int lateDeduction = 0;
//		int earlyDeduction = 0;
//		int absentDeduction = 0;
//		int leaveDeduction = 0;
//		int sickDeduction = 0;
//		
//		if ((times[0] + times[2]) < 3) {
//			lateDeduction = times[0] * RMB.on(5f);
//			earlyDeduction = times[2] * RMB.on(5f);
//		} else {
//			if (times[0] > 1 && times[1] < 2) {
//				lateDeduction = salaryPreDay;
//			} else if (times[0] < 2 && times[1] > 1) {
//				earlyDeduction = salaryPreDay;
//			} else {
//				lateDeduction = salaryPreDay / 2;
//				earlyDeduction = salaryPreDay / 2;
//			}
//		}
//		
//		lateDeduction += times[1] * (salaryPreDay / 2);
//		earlyDeduction += times[3] * (salaryPreDay / 2);
//		absentDeduction = times[4] * salaryPreDay;
//		
//		leaveDeduction += unpaidLeaveMinute * salaryPreMinute;
//		leaveDeduction += funeralLeaveMinute * salaryPreMinute;
//		leaveDeduction += maritalLeaveMinute * salaryPreMinute;
//		leaveDeduction += annualLeaveMinute * salaryPreMinute;
//		leaveDeduction += injuryLeaveMinute * salaryPreMinute;
//		leaveDeduction += deferredLeaveMinute * salaryPreMinute;
//		leaveDeduction += paternityLeaveMinute * salaryPreMinute;
//		leaveDeduction += paidLeaveMinute * salaryPreMinute;
//		leaveDeduction += outworkMinute * salaryPreMinute;
//		leaveDeduction += errandMinute * salaryPreMinute;
//		leaveDeduction += overtimeMinute * salaryPreMinute;
//		
//		if (sickLeaveMinute > (3 * workMinute)) {
//			sickDeduction = sickLeaveMinute - (3 * workMinute) * salaryPreMinute;
//		}
//		
//		sql.params()
//				.set("resultMonth", Calendars.parse(endStr, Calendars.DATE).toString("yyyyMM"))
//				.set("userId", userId)
//				
//				.set("standardSalary", wage.getStandardSalary())
//				.set("postSalary", wage.getPostSalary())
//				.set("floatingSalary", wage.getFloatingSalary())
//				
//				.set("communicationAllowance", wage.getCommunicationAllowance())
//				.set("trafficAllowance", wage.getTrafficAllowance())
//				.set("heatingAllowance", wage.getHeatingAllowance())
//				.set("holidayAllowance", wage.getHolidayAllowance())
//				.set("percentageAllowance", wage.getPercentageAllowance())
//				.set("mealAllowance", wage.getMealAllowance())
//				
//				.set("lateDeduction", lateDeduction)
//				.set("earlyDeduction", earlyDeduction)
//				.set("absentDeduction", absentDeduction)
//				.set("leaveDeduction", leaveDeduction)
//				.set("sickDeduction", sickDeduction)
//				.set("otherIncrease", 0)
//				.set("otherDeduction", 0)
//				
//				.set("taxBase", 0)
//				.set("tax", 0)
//				.set("socialSecurity", 0)
//				.set("accumulationFund", 0)
//				
//				.set("modifyId", 1)
//				.set("modifyTime", new DateTime().toDate());
//		dao.execute(sql);
//	}
//	
//	public int leave(String startStr, String endStr, String typeName, List<Leave> leaves, Map<String, String> dictMap, 
//			int workMinute, WorkDay day, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
//		Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
//		
//		for (Leave leave : leaves) {
//			if (!dictMap.containsKey(String.valueOf(leave.getTypeId()))) continue;
//			if (!dictMap.get(String.valueOf(leave.getTypeId())).equals(typeName)) continue;
//			
//			DateTime start = new DateTime(leave.getStartTime());
//			DateTime end = new DateTime(leave.getEndTime());
//			Works.dayMap(dayMap, start.toString(Calendars.DATE_TIME), end.toString(Calendars.DATE_TIME));
//		}
//		
//		return minute(startStr, endStr, dayMap, workMinute, day, monthOfDays, weekOfDays);
//	}
//	
//	public int outwork(String startStr, String endStr, List<Outwork> outworks,
//			int workMinute, WorkDay day, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
//		Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
//		
//		for (Outwork outwork : outworks) {
//			DateTime start = new DateTime(outwork.getStartTime());
//			DateTime end = new DateTime(outwork.getEndTime());
//			Works.dayMap(dayMap, start.toString(Calendars.DATE_TIME), end.toString(Calendars.DATE_TIME));
//		}
//		
//		return minute(startStr, endStr, dayMap, workMinute, day, monthOfDays, weekOfDays);
//	}
//	
//	public int errand(String startStr, String endStr, List<Errand> errands,
//			int workMinute, WorkDay day, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
//		Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
//		
//		for (Errand errand : errands) {
//			DateTime start = new DateTime(errand.getStartTime());
//			DateTime end = new DateTime(errand.getEndTime());
//			Works.dayMap(dayMap, start.toString(Calendars.DATE_TIME), end.toString(Calendars.DATE_TIME));
//		}
//		
//		return minute(startStr, endStr, dayMap, workMinute, day, monthOfDays, weekOfDays);
//	}
//	
//	public int overtime(String startStr, String endStr, List<Overtime> overtimes, 
//			int workMinute, WorkDay day, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
//		Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
//		
//		for (Overtime overtime : overtimes) {
//			DateTime start = new DateTime(overtime.getStartTime());
//			DateTime end = new DateTime(overtime.getEndTime());
//			Works.dayMap(dayMap, start.toString(Calendars.DATE_TIME), end.toString(Calendars.DATE_TIME));
//		}
//		
//		return minute(startStr, endStr, dayMap, workMinute, day, monthOfDays, weekOfDays);
//	}
//	
//	public int minute(String startStr, String endStr, Map<String, String[]> dayMap,
//			int workMinute, WorkDay day, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
//		DateTime start = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
//		DateTime end = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);
//	
//		int days = Days.daysBetween(start, end).getDays();
//		
//		int minute = 0;
//		
//		for (int i = 0; i < days + 1; i++) {
//			DateTime plus = start.plusDays(i);
//			String date = plus.toString(Calendars.DATE);
//			if (!dayMap.containsKey(date)) continue;
//			String[] arr = dayMap.get(date);
//			String in = arr[0];
//			String out = arr[1];
//			if (Strings.isBlank(in)) in = day.getCheckIn();
//			if (Strings.isBlank(out)) out = day.getCheckOut();
//			
//			minute += Works.getMinute(date + " " + in, date + " " + out, 
//					day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut(),
//					workMinute, monthOfDays, weekOfDays);
//		}
//		
//		return minute;
//	}
//	
//	public int[] times(Integer userId, String startStr, String endStr, List<Leave> leaves, WorkDay day) {
//		DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
//		DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");
//		
//		int months = Months.monthsBetween(start, end).getMonths();
//		
//		Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
//		for (Leave leave : leaves) {
//			DateTime leaveStart = new DateTime(leave.getStartTime());
//			DateTime leaveEnd = new DateTime(leave.getEndTime());
//			Works.dayMap(dayMap, leaveStart.toString(Calendars.DATE_TIME), leaveEnd.toString(Calendars.DATE_TIME));
//		}
//		
//		int lateMinTimes = 0;
//		int lateMaxTimes = 0;
//		int earlyMinTimes = 0;
//		int earlyMaxTimes = 0;
//		int absentTimes = 0;
//		
//		for (int i = 0; i < months + 1; i++) {
//			DateTime plus = start.plusMonths(i);
//			
//			if (!dao.exists("oa_attendance_" + plus.toString("yyyyMM"))) continue;
//			
//			Map<String, String> vars = new ConcurrentHashMap<String, String>();
//			vars.put("month", plus.toString("yyyyMM"));
//			List<CheckedRecord> attendances = mapper.query(CheckedRecord.class, "Attendance.query", Cnd
//					.where("a.user_id", "=", userId)
//					.and("a.work_date", ">=", startStr)
//					.and("a.work_date", "<=", endStr), null, vars);
//			
//			for (CheckedRecord attendance : attendances) {
//				String checkedIn = attendance.getCheckedIn();
//				String checkedOut = attendance.getCheckedOut();
//				String checkIn = day.getCheckIn();
//				String checkOut = day.getCheckOut();
//				String remarkIn = attendance.getRemarkIn();
//				String remarkOut = attendance.getRemarkOut();
//				
//				if (Strings.isNotBlank(remarkIn) && Strings.isNotBlank(remarkOut)) {
//					if (remarkIn.equals(Check.REST) && remarkOut.equals(Check.REST)) continue;
//					if (remarkIn.equals(Check.NORMAL) && remarkOut.equals(Check.NORMAL)) continue;
//				}
//
//				String date = new DateTime(attendance.getWorkDate()).toString(Calendars.DATE);
//				
//				String leaveIn = null;
//				String leaveOut = null;
//				
//				boolean leave = false;
//				if (dayMap.containsKey(date)) {
//					String[] arr = dayMap.get(date);
//					leaveIn = arr[0];
//					leaveOut = arr[1];
//					if (Strings.isBlank(leaveIn)) leaveIn = checkIn;
//					if (Strings.isBlank(leaveOut)) leaveOut = checkOut;
//					leave = true;
//				}
//				
//				// 旷工
//				if (Strings.isBlank(checkedIn) && Strings.isBlank(checkedOut)) {
//					absentTimes++;
//				}
//				
//				// 迟到
//				if (Strings.isNotBlank(checkedIn)) {
//					int minute = 0;
//					if (leave) {
//						if (leaveIn.compareTo(checkIn) == 0) minute = Works.minutesBetween(checkedIn, leaveOut);
//						if (leaveIn.compareTo(checkIn) > 0) {
//							if (leaveIn.compareTo(checkedIn) > 0) minute = Works.minutesBetween(checkedIn, checkIn);
//							else minute = Works.minutesBetween(leaveIn, checkIn);
//						}
//					} else {
//						if (checkedIn.compareTo(checkIn) > 0) minute = Works.minutesBetween(checkedIn, checkIn);	
//					}
//					if (minute >= -30 && minute < 0) lateMinTimes++;
//					if (minute < -30) lateMaxTimes++;
//				} else if (Strings.isBlank(checkedIn) && Strings.isNotBlank(checkedOut)) {
//					lateMaxTimes++;
//				}
//				
//				// 早退
//				if (Strings.isNotBlank(checkedOut)) {
//					int minute = 0;
//					if (leave) {
//						if (leaveOut.compareTo(checkOut) == 0) minute = Works.minutesBetween(checkedOut, leaveIn);
//						if (leaveOut.compareTo(checkOut) < 0) {
//							if (leaveOut.compareTo(checkedOut) > 0) minute = Works.minutesBetween(leaveOut, checkOut);
//							else minute = Works.minutesBetween(checkedOut, checkOut);
//						}
//					} else {
//						if (checkedOut.compareTo(checkOut) < 0) minute = Works.minutesBetween(checkedOut, checkOut);	
//					}
//					if (minute > 0 && minute <= 30) earlyMinTimes++;
//					if (minute > 30) earlyMaxTimes++;
//				} else if (Strings.isBlank(checkedOut) && Strings.isNotBlank(checkedIn)) {
//					earlyMaxTimes++;
//				}
//			}
//		}
//		int[] times = { lateMinTimes, lateMaxTimes, earlyMinTimes, earlyMaxTimes, absentTimes };
//		return times;
//	}
//}
