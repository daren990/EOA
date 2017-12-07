package cn.oa.utils.helper;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.joda.time.Months;

import cn.oa.model.WorkDay;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.except.Errors;

public class Works {
	
	/**
	 * 获取请假分钟.
	 * startStr=yyyy-MM-dd HH:mm, endStr=yyyy-MM-dd HH:mm
	 */
	public static int getMinute(String startStr, String endStr,
			String checkIn, String checkOut, String restIn, String restOut, int workMinute,
			Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
		DateTime startDate = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
		DateTime endDate = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);

		int days = Days.daysBetween(startDate, endDate).getDays();
		int minute = 0;
		
		if (days == 0) {
			if (isHolidayWork(startDate, monthOfDays, weekOfDays)) minute = minute(startStr, endStr, restIn, restOut);
		} else if (days > 0) {
			int first = 0;
			int last = 0;
			if (isHolidayWork(startDate, monthOfDays, weekOfDays)) first = minute(startStr, startStr.substring(0, 10) + " " + checkOut, restIn, restOut);
			if (isHolidayWork(endDate, monthOfDays, weekOfDays)) last = minute(endStr.substring(0, 10) + " " + checkIn, endStr, restIn, restOut);
			minute = first + last;
			if (days > 1) {
				int n = 0;
				for (int i = 0; i < days - 1; i++) {
					DateTime pos = startDate.plusDays(i + 1);
					if (isHolidayWork(pos, monthOfDays, weekOfDays)) n++;
				}
				minute = minute + n * workMinute;
			}
		}
		
		return minute;
	}
	/**
	 * 获取请假分钟.
	 * startStr=yyyy-MM-dd HH:mm, endStr=yyyy-MM-dd HH:mm
	 */
	public static int getMinute(String startStr, String endStr,
			String checkIn, String checkOut, String restIn, String restOut, int workMinute) {
		DateTime startDate = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
		DateTime endDate = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);

		int days = Days.daysBetween(startDate, endDate).getDays();
		int minute = 0;
		
		if (days == 0) {
			minute = minute(startStr, endStr, restIn, restOut);
		} else if (days > 0) {
				minute = minute(startStr, startStr.substring(0, 10) + " " + checkOut, restIn, restOut);
		}
		
		return minute;
	}
	/**
	 * 获取请假分钟.
	 * startStr=yyyy-MM-dd HH:mm, endStr=yyyy-MM-dd HH:mm
	 */
	public static int getMinute(String checkIn, String checkOut, String restIn, String restOut) {

			int minute = minute(checkIn, checkOut, restIn, restOut);

		
		return minute;
	}
	/**
	 * 获取请假分钟.
	 * startStr=yyyy-MM-dd HH:mm, endStr=yyyy-MM-dd HH:mm
	 */
	public static int getMinute(String checkIn, String checkOut) {

			int minute = minute(checkIn, checkOut);

		
		return minute;
	}
	/**
	 * 以天设置请假日期时间.
	 * startStr=yyyy-MM-dd HH:mm, endStr=yyyy-MM-dd HH:mm
	 */
	public static void dayMap(Map<String, String[]> dayMap, String startStr, String endStr) {
		DateTime startDate = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
		DateTime endDate = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);

		String in = startStr.substring(11, 16);
		String out = endStr.substring(11, 16);
		
		//获取两个日期之间的天数
		int days = Days.daysBetween(startDate, endDate).getDays();
		
		if (days == 0) {
			String[] checked = { in, out };
			dayMap.put(startDate.toString(Calendars.DATE), checked);
		} else if (days > 0) {
			String[] startChecked = { in, null };
			dayMap.put(startDate.toString(Calendars.DATE), startChecked);
			
			String[] endChecked = { null, out };
			dayMap.put(endDate.toString(Calendars.DATE), endChecked);
			
			String[] checked = { null, null };
			if (days > 1) {
				for (int i = 0; i < days - 1; i++) {
					DateTime pos = startDate.plusDays(i + 1);
					dayMap.put(pos.toString(Calendars.DATE), checked);
				}
			}
		}
	}
	
	/**
	 * 以天设置请假日期时间.
	 * startStr=yyyy-MM-dd HH:mm, endStr=yyyy-MM-dd HH:mm
	 */
	public static void dayMap(Map<String, String[]> dayMap, String startStr, String endStr, String typeName, String approve) {
		DateTime startDate = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
		DateTime endDate = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);

		String in = startStr.substring(11, 16);
		String out = endStr.substring(11, 16);
		
		int days = Days.daysBetween(startDate, endDate).getDays();
		
		if (days == 0) {
			String[] checked = { in, out ,typeName, approve};
			dayMap.put(startDate.toString(Calendars.DATE) + typeName, checked);
		

		} else if (days > 0) {
			String[] startChecked = { in, null ,typeName, approve};
			dayMap.put(startDate.toString(Calendars.DATE) + typeName, startChecked);
			
			String[] endChecked = { null, out ,typeName, approve};
			dayMap.put(endDate.toString(Calendars.DATE) + typeName, endChecked);
			
			String[] checked = { null, null ,typeName, approve};
			if (days > 1) {
				for (int i = 0; i < days - 1; i++) {
					DateTime pos = startDate.plusDays(i + 1);
					dayMap.put(pos.toString(Calendars.DATE) + typeName, checked);
				}
			}
		}
	}
	
	/**
	 * 获取请假时段备注.
	 * startStr=yyyy-MM-dd HH:mm, endStr=yyyy-MM-dd HH:mm
	 */
	public static Map<String, String[]> getRemarks(String startStr, String endStr,
			String checkIn, String checkOut, String restIn, String restOut,
			Map<String, Integer[]> monthOfDays, String[] weekOfDays, String remark) {
		DateTime startDate = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
		DateTime endDate = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);

		int days = Days.daysBetween(startDate, endDate).getDays();
		
		Map<String, String[]> remarkMap = new ConcurrentSkipListMap<String, String[]>();
		
		if (days == 0) {
			if (isWork(startDate, monthOfDays, weekOfDays)) {
				String[] remarks = remarks(startStr, endStr, restIn, restOut, remark);
				remarkMap.put(startDate.toString(Calendars.DATE), remarks);
			}
		} else if (days > 0) {
			if (isWork(startDate, monthOfDays, weekOfDays)) {
				String[] remarks = remarks(startStr, startStr.substring(0, 10) + " " + checkOut, restIn, restOut, remark);
				remarkMap.put(startDate.toString(Calendars.DATE), remarks);
			}
			if (isWork(endDate, monthOfDays, weekOfDays)) {
				String[] remarks = remarks(endStr.substring(0, 10) + " " + checkIn, endStr, restIn, restOut, remark);
				remarkMap.put(endDate.toString(Calendars.DATE), remarks);
			}
			if (days > 1) {
				for (int i = 0; i < days - 1; i++) {
					DateTime pos = startDate.plusDays(i + 1);
					if (isWork(pos, monthOfDays, weekOfDays)) {
						String[] remarks = remarks(pos.toString(Calendars.DATE) + " " + checkIn, pos.toString(Calendars.DATE) + " " + checkOut, restIn, restOut, remark);
						remarkMap.put(pos.toString(Calendars.DATE), remarks);					
					}
				}
			}
		}
		return remarkMap;
	}
	
	/**
	 * 获取当天请假时段备注.
	 * startStr=yyyy-MM-dd HH:mm, endStr=yyyy-MM-dd HH:mm
	 */
	private static String[] remarks(String startStr, String endStr, String restIn, String restOut, String remark) {
		String remarkIn = null;
		String remarkOut = null;
		
		int o1 = startStr.substring(11).compareTo(restIn);
		int o2 = startStr.substring(11).compareTo(restOut);
		int o3 = endStr.substring(11).compareTo(restIn);
		int o4 = endStr.substring(11).compareTo(restOut);
		
		if ((o1 < 0 && o3 < 0) || (o2 > 0 && o4 > 0)) {
			if (o1 < 0 && o3 < 0) remarkIn = remark;
			if (o2 > 0 && o4 > 0) remarkOut = remark;
		} else {
			remarkIn = remark;
			remarkOut = remark;
			if (o1 >= 0 && o2 <= 0) remarkIn = null;
			if (o3 >= 0 && o4 <= 0) remarkOut = null;
		}
		return new String[] { remarkIn, remarkOut };
	}
	
	/**
	 *  判断带薪工作日天数，用于考勤汇总日工资计算
	 */
/*	public static int isPaidWork(String startStr, String endStr, Map<String, Integer[]> monthOfDays, String[] weekOfDays){
		DateTime start = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
		DateTime end = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);
	
		int days = Days.daysBetween(start, end).getDays();
		int works = 0;
		if(!Asserts.isEmpty(monthOfDays) && monthOfDays.containsKey(start.toString("yyyyMM")+"paidDay")){
			Integer paidDay[] = monthOfDays.get(start.toString("yyyyMM")+"paidDay");
			works = paidDay[0];
		}
		else{
			for (int i = 0; i < days + 1; i++) {
				DateTime plus = start.plusDays(i);
				if (isPaidDay(plus, weekOfDays))
					works++;
			}
		}	
		
		return works;
		}*/
	
/*	public static boolean isPaidDay(DateTime datetime, String[] weekOfDays){
		boolean isWork = false;
		if(!Asserts.isEmpty(weekOfDays)){
			if (Asserts.hasAny(datetime.toString("e"), weekOfDays)) isWork = true;
		}
		else
			isWork = true;
		return isWork;
	}*/
	
	/**
	 * 当天是否为工作日.
	 */
	public static boolean isWork(DateTime datetime, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
		boolean isWork = false;
		if (!Asserts.isEmpty(monthOfDays) && monthOfDays.containsKey(datetime.toString("yyyyMM"))) {
			if (Asserts.hasAny(datetime.getDayOfMonth(), monthOfDays.get(datetime.toString("yyyyMM")))) isWork = true;
		} else if(!Asserts.isEmpty(weekOfDays)) {
			if (Asserts.hasAny(datetime.toString("e"), weekOfDays)) isWork = true;
		} else {
			isWork = true;
		}
		return isWork;
	}

	/**
	 * 获取工作日.
	 * startStr=yyyy-MM-dd, endStr=yyyy-MM-dd
	 */
	public static int workDay(String startStr, String endStr, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
		DateTime start = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
		DateTime end = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);
	
		int days = Days.daysBetween(start, end).getDays();
		int works = 0;
		
		for (int i = 0; i < days + 1; i++) {
			DateTime plus = start.plusDays(i);
			if (isWork(plus, monthOfDays, weekOfDays))
				works++;
		}
		
		return works;
	}
	
	
	/**
	 * 当天是否为工作日.此处包含法定假期
	 */
	//先判断月排版，因此比周排班的优先级要高
	public static boolean isHolidayWork(DateTime datetime, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
		boolean isWork = false;
		if (!Asserts.isEmpty(monthOfDays) && monthOfDays.containsKey(datetime.toString("yyyyMM"))) {
			if (Asserts.hasAny(datetime.getDayOfMonth(), monthOfDays.get(datetime.toString("yyyyMM")))) isWork = true;
			else if(monthOfDays.containsKey(datetime.toString("yyyyMM")+"holidays")){
				if (Asserts.hasAny(datetime.getDayOfMonth(), monthOfDays.get(datetime.toString("yyyyMM")+"holidays"))) isWork = true;
			}
		} else if(!Asserts.isEmpty(weekOfDays)) {
			//判断日排版中是不是有指定的日期
			if (Asserts.hasAny(datetime.toString("e"), weekOfDays)) isWork = true;
		} else {
			isWork = true;
		}
		return isWork;
	}
	
	/**
	 * 获取工作日在考勤汇总计算工作日时使用，
	 * 此处工作日包含法定假期，但不包含周末
	 */
	public static int holidayWork(String startStr, String endStr, Map<String, Integer[]> monthOfDays, String[] weekOfDays){
		DateTime start = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
		DateTime end = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);
		//获取两个日期之间的天数
		int days = Days.daysBetween(start, end).getDays();
		int works = 0;
		//遍历两个工作日之间的每一天
		for(int i=0; i<days + 1; i++) {
			DateTime plus = start.plusDays(i);
			if(isHolidayWork(plus, monthOfDays, weekOfDays)){
				works++;
			}
		}
		return works;
	}
	
	
	/**
	 * 获取标准工作分钟.
	 */
	public static int workMinute(String checkIn, String checkOut, String restIn, String restOut) {
		return minute("2014-01-01 " + checkIn, "2014-01-01 " + checkOut, restIn, restOut);
	}
	
	/**
	 * 获取当天应出勤分钟数.
	 */
	private static int minute(String startStr, String endStr, String restIn, String restOut) {
		int o1 = startStr.substring(11).compareTo(restIn);
		int o2 = startStr.substring(11).compareTo(restOut);
		int o3 = endStr.substring(11).compareTo(restIn);
		int o4 = endStr.substring(11).compareTo(restOut);

		DateTime start = Calendars.parse(startStr, Calendars.DATE_TIME);
		DateTime end = Calendars.parse(endStr, Calendars.DATE_TIME);

		DateTime in = Calendars.parse(startStr.substring(0, 10) + " " + restIn, Calendars.DATE_TIME);
		DateTime out = Calendars.parse(startStr.substring(0, 10) + " " + restOut, Calendars.DATE_TIME);
		
		int minutes = 0;
		if ((o1 <= 0 && o3 <= 0) || (o2 >= 0 && o4 >= 0)) {
			minutes = Minutes.minutesBetween(start, end).getMinutes();
		} else {
			if (o1 > 1 && o2 < 1) start = in;
			if (o3 > 1 && o4 < 1) end = out;
			minutes = Minutes.minutesBetween(start, end).getMinutes() - Minutes.minutesBetween(in, out).getMinutes();
		}
		return minutes;
	}
	
	/**
	 * 获取当天请假分钟.
	 */
	private static int minute(String startStr, String endStr) {

		DateTime start = Calendars.parse(startStr, Calendars.DATE_TIME);
		DateTime end = Calendars.parse(endStr, Calendars.DATE_TIME);		
		int minutes = 0;
		minutes = Minutes.minutesBetween(start, end).getMinutes();
		return minutes;
	}
	
	/**
	 * 请假时间验证.
	 */
	public static void compare(DateTime datetime, WorkDay day, String error) {
		if (datetime.toString("HH:mm").compareTo(day.getCheckIn()) < 0
				|| datetime.toString("HH:mm").compareTo(day.getCheckOut()) > 0) {
			throw new Errors(error + "只能介于" + day.getCheckIn().replace(":", "时") + "分到" + day.getCheckOut().replace(":", "时") + "分之间");
		}
	}

	/**
	 * 获取时差. start=HH:mm, end=HH:mm
	 */
	public static int minutesBetween(String start, String end) {
		return Minutes.minutesBetween(
				Calendars.parse("2014-01-01 " + start, Calendars.DATE_TIME),
				Calendars.parse("2014-01-01 " + end, Calendars.DATE_TIME)).getMinutes();
	}
	
	/**
	 * 获取日差. start=yyyy:mm:dd end=yyyy:mm:dd
	 */
	public static int dayBetween(String start, String end){
		return	Days.daysBetween(
				Calendars.parse(start, Calendars.DATE), 
				Calendars.parse(end, Calendars.DATE)).getDays();
	}
	
	public static int monthsBetween(Date start, Date end){
		return Months.monthsBetween(
				Calendars.parse(start, Calendars.DATE), 
				Calendars.parse(end, Calendars.DATE)).getMonths();
		
	}
}
