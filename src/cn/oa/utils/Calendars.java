package cn.oa.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import cn.oa.utils.validate.Validates;

public class Calendars {

	public static final String DATE = "yyyy-MM-dd";
	public static final String DATE_TIME = "yyyy-MM-dd HH:mm";
	
	public static final String DATE_TIMES = "yyyy-MM-dd HH:mm:ss";

	public static DateTime parse(String str, String fmt) {
		DateTime datetime = null;
		if (Strings.isNotBlank(str) && Validates.formatter(str, fmt))
			datetime = DateTimeFormat.forPattern(fmt).parseDateTime(str);
		return datetime;
	}
	
	public static String str(Date date, String fmt) {
		return new DateTime(date).toString(fmt);
	}
	
	public static String str(DateTime date, String fmt) {
		return new DateTime(date).toString(fmt);
	}
	
	public static DateTime parse(Date date, String fmt) {
		DateTime dateTime = null;
		if(date != null)
			dateTime = new DateTime(date);
		return dateTime;
	}
	
	public static DateTime parseDateTime(String str, String fmt) {
		DateTime datetime = null;
		if (Strings.isNotBlank(str))
			datetime = DateTimeFormat.forPattern(fmt).parseDateTime(str);
		return datetime;
	}
	

	public static String[] timeToMT(DateTime date){
		String first = Calendars.str(date, "yyyy-MM-01");
		DateTime dateTime = Calendars.parse(first, "yyyy-MM-dd");
		String last = Calendars.str(dateTime.plusMonths(1).plusDays(-1),"yyyy-MM-dd");
		
		return new String[] {first,last};
	}
	/**
	 * 获取中介时间段
	 * @param start  HH:mm
	 * @param end  HH:mm
	 * @return
	 */
	public static String middleTime(String start,String end){
		start = "2015-3-27 "+start;
		end = "2015-3-27 "+end;
		DateTime fn = Calendars.parse(start, Calendars.DATE_TIME);
		DateTime fm = Calendars.parse(end, Calendars.DATE_TIME);
		long beginDate = (fn.toDate().getTime()+fm.toDate().getTime())/2;
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");  				  
		return sdf.format(new Date(beginDate));
	}
}
