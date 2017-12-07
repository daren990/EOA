package cn.oa.service;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.consts.Status;
import cn.oa.model.CheckedRecord;
import cn.oa.model.Errand;
import cn.oa.model.Leave;
import cn.oa.model.Outwork;
import cn.oa.model.WorkDay;
import cn.oa.utils.Calendars;
import cn.oa.utils.Strings;
import cn.oa.utils.helper.Works;

@IocBean
public class WorkService {

	@Inject
	private Dao dao;

	public void dayMaps(Integer userId, String startStr, String endStr,
			Map<String, String[]> leaveDayMap,
			Map<String, String[]> outworkDayMap,
			Map<String, String[]> errandDayMap) {
		Criteria cnd = Cnd
				.where("status", "=", Status.ENABLED)
				.and("userId", "=", userId)
				.and("approve", "=", Status.OK)
				.and("startTime", ">=", startStr + " 00:00:00")
				.and("startTime", "<=", endStr + " 23:59:59");
		
		List<Leave> leaves = dao.query(Leave.class, cnd);
		List<Outwork> outworks = dao.query(Outwork.class, cnd);
		List<Errand> errands = dao.query(Errand.class, cnd);
		
		for (Leave leave : leaves) {
			DateTime leaveStart = new DateTime(leave.getStartTime());
			DateTime leaveEnd = new DateTime(leave.getEndTime());
			Works.dayMap(leaveDayMap, leaveStart.toString(Calendars.DATE_TIME), leaveEnd.toString(Calendars.DATE_TIME));
		}
		for (Outwork outwork : outworks) {
			DateTime outworkStart = new DateTime(outwork.getStartTime());
			DateTime outworkEnd = new DateTime(outwork.getEndTime());
			Works.dayMap(outworkDayMap, outworkStart.toString(Calendars.DATE_TIME), outworkEnd.toString(Calendars.DATE_TIME));
		}
		for (Errand errand : errands) {
			DateTime errandStart = new DateTime(errand.getStartTime());
			DateTime errandEnd = new DateTime(errand.getEndTime());
			Works.dayMap(errandDayMap, errandStart.toString(Calendars.DATE_TIME), errandEnd.toString(Calendars.DATE_TIME));
		}
	}
	
	public void hours(CheckedRecord e, WorkDay day, String[] weeks, Map<String, Integer[]> monthMap, int works,
			Map<String, String[]> leaveDayMap, Map<String, String[]> outworkDayMap, Map<String, String[]> errandDayMap) {
		String date = new DateTime(e.getWorkDate()).toString(Calendars.DATE);
		boolean next = true;
		String in = null;
		String out = null;
		if (leaveDayMap.containsKey(date)) {
			String[] arr = leaveDayMap.get(date);
			in = Strings.isBlank(arr[0]) ? day.getCheckIn() : arr[0];
			out = Strings.isBlank(arr[1]) ? day.getCheckOut() : arr[1];
			next = false;
		}
		if (next) {
			if (outworkDayMap.containsKey(date)) {
				String[] arr = outworkDayMap.get(date);
				in = Strings.isBlank(arr[0]) ? day.getCheckIn() : arr[0];
				out = Strings.isBlank(arr[1]) ? day.getCheckOut() : arr[1];
				next = false;
			}
		}
		if (next) {
			if (errandDayMap.containsKey(date)) {
				in = day.getCheckIn();
				out = day.getCheckOut();
				next = false;
			}
		}
		
		if (!Strings.isBlank(in) && !Strings.isBlank(out)) {
			int minute = Works.getMinute(date + " " + in, date + " " + out, day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut(), works, monthMap, weeks);
			e.setMinute(minute);
		} else {
			e.setMinute(0);
		}
	}
	public void hours(CheckedRecord e, WorkDay day, int works,
			Map<String, String[]> leaveDayMap, Map<String, String[]> outworkDayMap, Map<String, String[]> errandDayMap) {
		String date = new DateTime(e.getWorkDate()).toString(Calendars.DATE);

		boolean next = true;
		String in = null;
		String out = null;
		if (leaveDayMap.containsKey(date)) {
			String[] arr = leaveDayMap.get(date);
			in = Strings.isBlank(arr[0]) ? day.getCheckIn() : arr[0];
			out = Strings.isBlank(arr[1]) ? day.getCheckOut() : arr[1];
			next = false;
		}
		if (next) {
			if (outworkDayMap.containsKey(date)) {
				String[] arr = outworkDayMap.get(date);
				in = Strings.isBlank(arr[0]) ? day.getCheckIn() : arr[0];
				out = Strings.isBlank(arr[1]) ? day.getCheckOut() : arr[1];
				next = false;
			}
		}
		if (next) {
			if (errandDayMap.containsKey(date)) {
				in = day.getCheckIn();
				out = day.getCheckOut();
				next = false;
			}
		}
		
		if (!Strings.isBlank(in) && !Strings.isBlank(out)) {
			int minute = Works.getMinute(date + " " + in, date + " " + out, day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut(), works);
			e.setMinute(minute);
		} else {
			e.setMinute(0);
		}
	}
}
