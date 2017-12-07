package test.job;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.junit.Test;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import test.Setup;
import cn.oa.consts.Status;
import cn.oa.model.Annual;
import cn.oa.model.Archive;
import cn.oa.model.Dict;
import cn.oa.model.WorkDay;
import cn.oa.repository.Mapper;
import cn.oa.service.DictService;
import cn.oa.utils.Calendars;
import cn.oa.utils.Strings;
import cn.oa.utils.helper.Works;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;

/**
 * 用户年休假自动设置.
 * 
 * @author JELLEE@Yeah.Net
 */
public class AnnualTest extends Setup {

	private static Log log = Logs.getLog(AnnualTest.class);
	
	@Test
	public void execute()  {
		try {
			DictService dictService = ioc.get(DictService.class);

			Map<String, String> annualMap = dictService.map(Dict.ANNUAL);
			Map<Integer, Integer> monthMap = new ConcurrentHashMap<Integer, Integer>();
			Map<Integer, Integer> yearMap = new ConcurrentHashMap<Integer, Integer>();
			
			for (Entry<String, String> entry : annualMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (Strings.endsWith(key, "月")) {
					Integer month = Validator.getInt(Strings.before(key, "月"), R.I);
					Integer day = Validator.getInt(Strings.before(value, "天"), R.I);
					if (month == null || day == null) continue;
					monthMap.put(month, day);
				}
				if (Strings.endsWith(key, "年")) {
					Integer year = Validator.getInt(Strings.before(key, "年"), R.I);
					Integer day = Validator.getInt(Strings.before(value, "天"), R.I);
					if (year == null || day == null) continue;
					yearMap.put(year, day);
				}
			}

			DateTime now = Calendars.parse("2013-04-24", Calendars.DATE);
//			DateTime now = new DateTime();
			
			// 1) 工作时长
			List<WorkDay> workDays = dao.query(WorkDay.class, null);
			Map<Integer, Integer> dayMap = new ConcurrentHashMap<Integer, Integer>();
			for (WorkDay day : workDays) {
				int minute = Works.workMinute(day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut());
				dayMap.put(day.getDayId(), minute);
			}
			
			// 2) 过期年假无效
			dao.update(Annual.class, Chain.make("status", Status.DISABLED), Cnd.where("endDate", "<", now.toString(Calendars.DATE)));
			// 3) 试用期年假
			entry(dao, mapper, now, monthMap, dayMap);
			// 4) 转正年假
			full(dao, mapper, now, yearMap, dayMap);
		} catch (Exception e) {
			log.error("(HolidayJob:execute) error: ", e);
		}
	}
	
	public void entry(Dao dao, Mapper mapper, DateTime now, Map<Integer, Integer> monthMap, Map<Integer, Integer> dayMap) {
		DateTime last = now.plusDays(-1);
		for (Entry<Integer, Integer> entry : monthMap.entrySet()) {
			Integer month = entry.getKey();
			Integer day = entry.getValue();

			DateTime lastMonths = last.plusMonths(-month);
			List<Archive> archives = mapper.query(Archive.class, "Archive.query", Cnd
					.where("u.status", "=", Status.ENABLED)
					.and("a.entry_date", "=", lastMonths.toString(Calendars.DATE)));

			Sql sql = Sqls.create(dao.sqls().get("Annual.inserts"));
			int batch = 0;
			for (Archive archive : archives) {
				if (archive.getDayId() == null) continue;
				int minute = dayMap.get(archive.getDayId());
				sql.params()
						.set("userId", archive.getUserId())
						.set("startDate", last.toDate())
						.set("endDate", lastMonths.plusYears(1).plusDays(-1).toDate())
						.set("sumMinute", day * minute)
						.set("workMinute", minute)
						.set("status", Status.ENABLED)
						.set("createTime", now.toDate())
						.set("modifyTime", now.toDate());
				sql.addBatch();
				batch++;
			}
			if (batch > 0) {
				dao.execute(sql);
			}
		}
	}
	
	public void full(Dao dao, Mapper mapper, DateTime now, Map<Integer, Integer> yearMap, Map<Integer, Integer> dayMap) {
		int max = 0;
		for (Entry<Integer, Integer> entry : yearMap.entrySet()) {
			int year = entry.getKey();
			if (max < year) max = year;
		}
		
		DateTime last = now.plusDays(-1);
		List<Archive> archives = mapper.query(Archive.class, "Archive.query", Cnd
				.where("u.status", "=", Status.ENABLED)
				.and("date_format(a.entry_date, '%m-%d')", "=", last.toString("MM-dd")));

		Sql sql = Sqls.create(dao.sqls().get("Annual.inserts"));
		int batch = 0;
		for (Archive archive : archives) {
			if (archive.getDayId() == null) continue;
			int minute = dayMap.get(archive.getDayId());
			
			DateTime entry = new DateTime(archive.getEntryDate());
			int year = Years.yearsBetween(entry, last).getYears();
			
			if (year < 1) continue;
			if (year >= max) year = max;
			Integer day = yearMap.get(year);
			
			sql.params()
					.set("userId", archive.getUserId())
					.set("startDate", last.toDate())
					.set("endDate", last.plusYears(1).plusDays(-1).toDate())
					.set("sumMinute", day * minute)
					.set("workMinute", minute)
					.set("status", Status.ENABLED)
					.set("createTime", now.toDate())
					.set("modifyTime", now.toDate());
			sql.addBatch();
			batch++;
		}
		if (batch > 0) {
			dao.execute(sql);
		}
	}
}
