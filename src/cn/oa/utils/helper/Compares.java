package cn.oa.utils.helper;

import java.util.Date;

import org.joda.time.DateTime;

import cn.oa.utils.Calendars;

public class Compares {

	public static boolean in(String startStr, String endStr, Date start, Date end) {
		String startStr2 = new DateTime(start).toString(Calendars.DATE_TIME);
		String endStr2 = new DateTime(end).toString(Calendars.DATE_TIME);
		boolean a = startStr2.compareTo(startStr) < 1 && endStr2.compareTo(startStr) < 1;
		boolean b = startStr2.compareTo(endStr) > -1 && endStr2.compareTo(endStr) > -1;

		return !(a || b);
	}
}
