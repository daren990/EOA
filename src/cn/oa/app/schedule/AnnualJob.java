package cn.oa.app.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Years;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.oa.app.quartz.Jobs;
import cn.oa.consts.Status;
import cn.oa.model.Annual;
import cn.oa.model.Archive;
import cn.oa.model.Org;
import cn.oa.model.User;
import cn.oa.model.WorkDay;
import cn.oa.repository.Mapper;
import cn.oa.service.AnnualService;
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
public class AnnualJob implements Job {

	private static Log log = Logs.getLog(AnnualJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			Ioc ioc = Jobs.getIoc(context);
			Dao dao = ioc.get(Dao.class);
			Mapper mapper = ioc.get(Mapper.class);
			AnnualService annualService = ioc.get(AnnualService.class);

			DateTime now = new DateTime();
			
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
			entry(dao, mapper, now, dayMap ,annualService);
			// 4) 转正年假
			full(dao, mapper, now, dayMap, annualService);
		} catch (Exception e) {
			log.error("(AnnualJob:execute) error: ", e);
		}
	}
	
	
	public void entry(Dao dao, Mapper mapper, DateTime now, Map<Integer, Integer> dayMap ,AnnualService annualService) {
		
		Calendar date = Calendar.getInstance();
		int thisYear = date.get(Calendar.YEAR);
		int thisDay = date.get(Calendar.DAY_OF_MONTH);
		List<Archive> archivesToday = mapper.query(Archive.class, "Archive.query", Cnd
				.where("u.status", "=", Status.ENABLED));
		
		for(Archive archive :archivesToday){
			Calendar caldate = Calendar.getInstance();
			if(archive.getEntryDate()==null) continue;
			caldate.setTime(archive.getEntryDate());
			int entryDay = caldate.get(Calendar.DAY_OF_MONTH);
			int entryYear = caldate.get(Calendar.YEAR);
			
			if(entryDay==thisDay-1 &&entryYear==thisYear){
				
				Integer corpId = dao.fetch(User.class, archive.getUserId()).getCorpId();
				Integer annualId = dao.fetch(Org.class, corpId).getAnnualId();
				
				List<Map<Integer, Integer>> annualRole = queryAnnual(annualService, annualId);
				
				
				for (Entry<Integer, Integer> entry : annualRole.get(0).entrySet()) {
					Integer month = entry.getKey();
					Integer day = entry.getValue();
					DateTime last = now.plusDays(-1);
					DateTime lastMonths = last.plusMonths(-month);
					if(Calendars.str(archive.getEntryDate(), Calendars.DATE).equals(lastMonths.toString(Calendars.DATE))){
						Sql sql = Sqls.create(dao.sqls().get("Annual.inserts"));
						int batch = 0;
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
				if (batch > 0) {
					dao.execute(sql);
					}
					
				}
			}
			
		}
	}
}
		
	
	public void full(Dao dao, Mapper mapper, DateTime now, Map<Integer, Integer> dayMap, AnnualService annualService) {
		
		int max = 0;
		DateTime last = now.plusDays(-1);
		List<Archive> archives = mapper.query(Archive.class, "Archive.query", Cnd
				.where("u.status", "=", Status.ENABLED)
				.and("date_format(a.entry_date, '%m-%d')", "=", last.toString("MM-dd")));

		Sql sql = Sqls.create(dao.sqls().get("Annual.inserts"));
		int batch = 0;
		for (Archive archive : archives) {
			if (archive.getDayId() == null) continue;
			int corpId =dao.fetch(User.class, archive.getUserId()).getCorpId();
			Integer annualId = dao.fetch(Org.class, corpId).getAnnualId();
			int minute = dayMap.get(archive.getDayId());
			
			List<Map<Integer, Integer>> annualRole = queryAnnual(annualService, annualId);
			
			for (Entry<Integer, Integer> entry : annualRole.get(1).entrySet()) {
				int year = entry.getKey();
				if (max < year) max = year;
			}
			DateTime entry = new DateTime(archive.getEntryDate());
			int year = Years.yearsBetween(entry, last).getYears();
			
			if (year < 1) continue;
			if (year >= max) year = max;
			Integer day = annualRole.get(1).get(year);
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
	
	public List<Map<Integer, Integer>> queryAnnual(AnnualService annualService, int annualId){
		
		Map<String, String> annualMap = annualService.map(annualId);
		Map<Integer, Integer> monthMap = new ConcurrentHashMap<Integer, Integer>();
		Map<Integer, Integer> yearMap = new ConcurrentHashMap<Integer, Integer>();
		List<Map<Integer, Integer>> annualRole =new ArrayList<Map<Integer, Integer>>();
		
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
		// 0为月份，1为年份
		annualRole.add(0, monthMap);
		annualRole.add(1, yearMap);
		return annualRole;
	}
}
