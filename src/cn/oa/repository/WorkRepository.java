package cn.oa.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;

import cn.oa.app.cache.Ehcache;
import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.model.ConfHoliday;
import cn.oa.model.Org;
import cn.oa.model.WorkDay;
import cn.oa.model.WorkMonth;
import cn.oa.model.WorkWeek;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;

@IocBean
@SuppressWarnings("unchecked")
public class WorkRepository {

	@Inject
	private Dao dao;
	@Inject
	private Ehcache cache;
	
	public Map<Integer, WorkDay> dayMap() {
		Map<Integer, WorkDay> dayMap = (Map<Integer, WorkDay>) cache.get(Cache.FQN_WORKS, Cache.WORK_DAY);
		if (Asserts.isEmpty(dayMap)) {
			if (dayMap == null) dayMap = new ConcurrentHashMap<Integer, WorkDay>();
			List<WorkDay> days = dao.query(WorkDay.class, Cnd.where("status", "=", Status.ENABLED));
			for (WorkDay day : days) {
				dayMap.put(day.getDayId(), day);
			}
			cache.put(Cache.FQN_WORKS, Cache.WORK_DAY, dayMap);
			return dayMap;
		}
		return dayMap;
	}
	
	public Map<Integer, String[]> weekMap() {
		Map<Integer, String[]> weekMap = (Map<Integer, String[]>) cache.get(Cache.FQN_WORKS, Cache.WORK_WEEK);
		if (Asserts.isEmpty(weekMap)) {
			if (weekMap == null) weekMap = new ConcurrentHashMap<Integer, String[]>();
			List<WorkWeek> weeks = dao.query(WorkWeek.class, Cnd.where("status", "=", Status.ENABLED));
			for (WorkWeek week : weeks) {
				weekMap.put(week.getWeekId(), Strings.splitIgnoreBlank(week.getWorkDays(), ","));
			}
			cache.put(Cache.FQN_WORKS, Cache.WORK_WEEK, weekMap);
			return weekMap;
		}
		return weekMap;
	}
	
	
	public Map<String, Integer[]> holidayMap(Integer corpId) {
		Map<String, Integer[]> holidayMap = (Map<String, Integer[]>) cache.get(Cache.FQN_WORKS, Cache.WORK_HOLIDAY+ "." + corpId);
		if (Asserts.isEmpty(holidayMap)) {
			if (holidayMap == null) holidayMap = new ConcurrentHashMap<String, Integer[]>();
			
			Org corp = dao.fetch(Org.class, corpId);
			Integer holidayId = corp.getConfLeaveId();
			List<ConfHoliday> holidays = dao.query(ConfHoliday.class, Cnd
					.where("holidayId", "=", holidayId)
					.and("status", "=", Status.ENABLED));
			
			for (ConfHoliday holiday : holidays) {
				holidayMap.put(holiday.getYear() + holiday.getMonth(), Converts.array(holiday.getHolidayDays(), ","));
			}
			cache.put(Cache.FQN_WORKS, Cache.WORK_HOLIDAY+ "." + corpId , holidayMap);
			return holidayMap;
		}
		return holidayMap;
	}
	
	public Map<String, Integer[]> monthMap(Integer corpId) {
		Map<String, Integer[]> monthMap = (Map<String, Integer[]>) cache.get(Cache.FQN_WORKS, Cache.WORK_MONTH + "." + corpId);
		if (Asserts.isEmpty(monthMap)) {
			if (monthMap == null) monthMap = new ConcurrentHashMap<String, Integer[]>();
			List<WorkMonth> months = dao.query(WorkMonth.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("orgId", "=", corpId));
			for (WorkMonth month : months) {
				monthMap.put(month.getYear() + month.getMonth(), Converts.array(month.getWorkDays(), ","));
				if(month.getHolidays()!=null)
				monthMap.put(month.getYear() + month.getMonth()+"holidays", Converts.array(month.getHolidays(), ","));
			}
			cache.put(Cache.FQN_WORKS, Cache.WORK_MONTH + "." + corpId, monthMap);
			return monthMap;
		}
		return monthMap;
	}

}
