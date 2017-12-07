package cn.oa.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.TableName;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.consts.Check;
import cn.oa.consts.Status;
import cn.oa.model.CheckedRecord;
import cn.oa.model.User;
import cn.oa.model.WorkDay;
import cn.oa.repository.Mapper;
import cn.oa.repository.WorkRepository;
import cn.oa.utils.Calendars;
import cn.oa.utils.Strings;
import cn.oa.utils.helper.Works;

@IocBean
public class CheckedRecordService {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private WorkRepository workRepository;

	public void update(Integer userId, String startStr, String endStr, String remark, WorkDay day, String[] weeks, Map<String, Integer[]> monthMap,
			Integer modifyId, Date modifyTime) {
		Map<String, String[]> remarkMap = Works.getRemarks(startStr, endStr,
				day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut(), monthMap, weeks, remark);

		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.update"));
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			DateTime now = Calendars.parse(entry.getKey(), Calendars.DATE);
			String[] remarks = entry.getValue();
			createTable(dao, now);
			sql.vars().set("month", now.toString("yyyyMM"));
			sql.params()
				.set("userId", userId)
				.set("workDate", now.toDate())
				.set("checkedIn", null)
				.set("checkedOut", null)
				.set("remarkIn", null)
				.set("remarkOut", null)
				.set("remarkedIn", remarks[0])
				.set("remarkedOut", remarks[1])
				.set("version", Status.DISABLED)
				.set("modifyId", modifyId)
				.set("modifyTime", modifyTime);
			dao.execute(sql);
		}
	}
	
	public void update2(Integer userId, String startStr, String endStr, String remark, WorkDay day, String[] weeks, Map<String, Integer[]> monthMap,
			Integer modifyId, Date modifyTime) {
		Map<String, String[]> remarkMap = Works.getRemarks(startStr, endStr,
				day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut(), monthMap, weeks, remark);
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.update"));
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			DateTime now = Calendars.parse(entry.getKey(), Calendars.DATE);
			String[] remarks = entry.getValue();
			createTable(dao, now);
			sql.vars().set("month", now.toString("yyyyMM"));
			sql.params()
				.set("userId", userId)
				.set("workDate", now.toDate())
				.set("checkedIn", null)
				.set("checkedOut", null)
				.set("remarkIn", null)
				.set("remarkOut", null)
				.set("remarkedIn", remarks[0])
				.set("remarkedOut", remarks[1])
				.set("version", Status.DISABLED)
				.set("modifyId", modifyId)
				.set("modifyTime", modifyTime);
			dao.execute(sql);
		}
	}
	
	/**
	 * 更新考勤记录
	 * @param userId 用户ID
	 * @param modifyId 操作者
	 * @param remarkMap 备注
	 */
	public void update3(Integer userId, Integer modifyId, Map<String, String[]> remarkMap) {
		
		Sql sql = Sqls.create(dao.sqls().get("NewCheckedRecord.update"));
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			DateTime workDate = Calendars.parse(entry.getKey(), Calendars.DATE);
			String[] remarks = entry.getValue();
			List<String> rms = Arrays.asList(remarks);
			createTable(dao, workDate);
			if(rms.contains(null)){ // 上午或者下午有申请单
				Map<String, String> vars = new ConcurrentHashMap<String, String>();
				vars.put("month", Calendars.str(workDate.toDate(), "yyyyMM"));
				//向考勤日志表获取考勤当天的记录，用于更新day中的value的数组中的值
				List<CheckedRecord> attendances = mapper.query(CheckedRecord.class, "CheckedRecord.query", Cnd
						.where("c.user_id", "=", userId)
						.and("c.work_date", "=", workDate.toString(Calendars.DATE)), null, vars);
				if(attendances.size()>0){
					CheckedRecord cr = attendances.get(0);
					if(rms.get(0)==null){
						if(cr.getRemarkedIn()!=null){
							remarks[0]=cr.getRemarkedIn();
						}
					}else{
						if(cr.getRemarkedOut()!=null){
							remarks[1]=cr.getRemarkedOut();
						}
					}
				}
			}
			
			sql.vars().set("month", workDate.toString("yyyyMM"));
			sql.params()
				.set("userId", userId)
				.set("workDate", workDate.toDate())
				.set("checkedIn", null)
				.set("checkedOut", null)
				.set("remarkIn", null)
				.set("remarkOut", null)
				.set("remarkedIn", (remarks[0]==null)?null:remarks[0])
				.set("remarkedOut", (remarks[1]==null)?null:remarks[1])
				.set("version", Status.DISABLED)
				.set("modifyId", modifyId)
				.set("modifyTime", new Date());
			dao.execute(sql);
		}
	}
	
	public void updateNewLeave(Integer userId, String startStr, String endStr, String remark, Map<String, String[]>  remarkMap,
			Integer modifyId, Date modifyTime) {		
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			DateTime now = Calendars.parse(entry.getKey(), Calendars.DATE);
			User user = dao.fetch(User.class,userId);
			//同步考勤
			List<CheckedRecord> records = mapper.query(CheckedRecord.class, "CheckRecord.query2", Cnd
					.where("check_time", ">=",Calendars.str(now, Calendars.DATE)+" 00:00:00")
					.and("check_time", "<=",Calendars.str(now, Calendars.DATE)+" 23:59:59")
					.and("u.job_number", "=", user.getJobNumber())
					.groupBy("u.job_number"));
			if(records.size()==0){
				Sql sql = Sqls
						.create(dao.sqls().get("NewCheckedRecord.update"));
				String[] remarks = entry.getValue();
				List<String> rms = Arrays.asList(remarks);
				if(rms.get(0)==null||rms.get(1)==null){
					Map<String, String> vars = new ConcurrentHashMap<String, String>();
					vars.put("month", Calendars.str(now.toDate(), "yyyyMM"));
					List<CheckedRecord> attendances = mapper.query(CheckedRecord.class, "CheckedRecord.query", Cnd
							.where("c.user_id", "=", userId)
							.and("c.work_date", "=", now.toString(Calendars.DATE)), null, vars);
					if(attendances.size()>0){
						CheckedRecord cr = attendances.get(0);
						if(rms.get(0)==null){
							if(cr.getRemarkedIn()!=null){
								remarks[0]=cr.getRemarkedIn();
							}
						}else{
							if(cr.getRemarkedOut()!=null){
								remarks[1]=cr.getRemarkedOut();
							}
						}
					}
				}
				createTable(dao, now);
				sql.vars().set("month", now.toString("yyyyMM"));

				sql.params()
						.set("userId", userId)
						.set("workDate", now.toDate())
						.set("checkedIn", null)
						.set("checkedOut", null)
						.set("remarkIn", null)
						.set("remarkOut", null)
						.set("remarkedIn",
								(remarks[0] == null) ? "1" : remarks[0])
						.set("remarkedOut",
								(remarks[1] == null) ? "1" : remarks[1])
						.set("version", Status.DISABLED)
						.set("modifyId", modifyId)
						.set("modifyTime", modifyTime);
				dao.execute(sql);
			}else{
				Sql sql = Sqls.create(dao.sqls().get("OperateCheckedRecord.update"));
				String[] remarks = entry.getValue();
				createTable(dao, now);
				sql.vars().set("month", now.toString("yyyyMM"));
				
				String minIn = null;
				String maxOut = null;
				for(CheckedRecord record:records){
					minIn = record.getMinIn();
					maxOut = record.getMaxOut();
				}
				String checkIn = remarks[2];
				String checkOut = remarks[3];
				String restIn = remarks[4];
				String restOut = remarks[5];
				String remarkIn = null;
				String remarkOut = null;
				String checkedIn = null;
				String checkedOut = null;
				if(remarks[0]!=null&&remarks[1]!=null){//一天都请假
					checkedIn = minIn.compareTo(restIn) < 0 ? minIn : null;
					checkedOut = maxOut.compareTo(restOut) > 0 ? maxOut : null;
					remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(checkIn) < 1 ? Check.NORMAL : Check.LATE);
					remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(checkOut) > -1 ? Check.NORMAL : Check.EARLY);
				}else if(remarks[0]==null&&remarks[1]!=null){//下午请假,上午上班	
					String midle = Calendars.middleTime(checkIn, restIn);
					checkedIn = minIn.compareTo(midle) < 0 ? minIn : null;
					checkedOut = maxOut.compareTo(midle) > 0 ? maxOut : null;					
					remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(checkIn) < 1 ? Check.NORMAL : Check.LATE);
					remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(restIn) > -1 ? Check.NORMAL : Check.EARLY);
				}else if(remarks[0]!=null&&remarks[1]==null){//上午请假,下午上班
					String midle = Calendars.middleTime(restOut, checkOut);
					checkedIn = minIn.compareTo(midle) < 0 ? minIn : null;
					checkedOut = maxOut.compareTo(midle) > 0 ? maxOut : null;
					remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(restOut) < 1 ? Check.NORMAL : Check.LATE);
					remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(checkOut) > -1 ? Check.NORMAL : Check.EARLY);
				}
				sql.params()
					.set("userId", userId)
					.set("workDate", now.toDate())
					.set("checkedIn", checkedIn==null?"1":checkedIn)
					.set("checkedOut", checkedOut==null?"1":checkedOut)
					.set("remarkIn", remarkIn==null?"1":remarkIn)
					.set("remarkOut", remarkOut==null?"1":remarkOut)
					.set("remarkedIn", (remarks[0]==null)?"1":remarks[0])
					.set("remarkedOut", (remarks[1]==null)?"1":remarks[1])
					.set("version", Status.DISABLED)
					.set("modifyId", modifyId)
					.set("modifyTime", modifyTime);
			dao.execute(sql);
			}
		}
	}
	
	public void updateClear(Integer userId, String startStr, String endStr, String remark, Map<String, String[]>  remarkMap, Integer modifyId) {
		
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.update"));
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			DateTime now = Calendars.parse(entry.getKey(), Calendars.DATE);
			String[] remarks = entry.getValue();
			System.out.println(entry.getKey());
			System.out.println(remarks[0]+" "+remarks[1]);
			createTable(dao, now);
			sql.vars().set("month", now.toString("yyyyMM"));
			sql.params()
				.set("userId", userId)
				.set("workDate", now.toDate())
				.set("checkedIn", null)
				.set("checkedOut", null)
				.set("remarkIn", null)
				.set("remarkOut", null)
				.set("remarkedIn", remarks[0])
				.set("remarkedOut", remarks[1])
				.set("version", Status.DISABLED)
				.set("modifyId", modifyId)
				.set("modifyTime", new Date());
			dao.execute(sql);
		}
	}
	public void update4(Integer userId, String startStr, String endStr, String remark, Map<String, String[]>  remarkMap,
			Integer modifyId, Date modifyTime) {
		
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.update"));
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			DateTime now = Calendars.parse(entry.getKey(), Calendars.DATE);
			String[] remarks = entry.getValue();
			createTable(dao, now);
			sql.vars().set("month", now.toString("yyyyMM"));
			sql.params()
				.set("userId", userId)
				.set("workDate", now.toDate())
				.set("checkedIn", null)
				.set("checkedOut", null)
				.set("remarkIn", null)
				.set("remarkOut", null)
				.set("remarkedIn", remarks[0])
				.set("remarkedOut", remarks[1])
				.set("version", Status.DISABLED)
				.set("modifyId", modifyId)
				.set("modifyTime", modifyTime);
			dao.execute(sql);
		}
	}
	
	/**
	 * 删除申请单
	 * @param userId
	 * @param remarkMap
	 * @param modifyId
	 */
	public void delete(Integer userId, Map<String, String[]>  remarkMap, Integer modifyId) {
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.update"));
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			DateTime now = Calendars.parse(entry.getKey(), Calendars.DATE);
//			createTable(dao, now);
			sql.vars().set("month", now.toString("yyyyMM"));
			sql.params()
				.set("userId", userId)
				.set("workDate", now.toDate())
//				.set("checkedIn", null)
//				.set("checkedOut", null)
//				.set("remarkIn", null)
//				.set("remarkOut", null)
				.set("remarkedIn", null)
				.set("remarkedOut", null)
//				.set("version", Status.DISABLED)
				.set("modifyId", modifyId)
				.set("modifyTime", new Date());
			dao.execute(sql);
		}
	}
	
	public void version(final Integer userId, final String startStr, final String endStr, final Integer modifyId, final DateTime now) {
		DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
		DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");
		int months = Months.monthsBetween(start, end).getMonths();

		for (int i = 0; i < months + 1; i++) {
			DateTime plus = start.plusMonths(i);
			if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM"))) continue;
			
			TableName.run(plus.toString("yyyyMM"), new Runnable() {
				@Override
				public void run() {
					dao.update(CheckedRecord.class, Chain
							.make("version", Status.ENABLED)
							.add("modifyId", modifyId)
							.add("modifyTime", now.toDate()), Cnd
							.where("userId", "=", userId)
							.and("workDate", ">=", startStr.substring(0, 10))
							.and("workDate", "<=", endStr.substring(0, 10)));
				}
			});
		}
	}
	
	public void createTable(final Dao dao, final DateTime now) {
		TableName.run(now.toString("yyyyMM"), new Runnable() {
			@Override
			public void run() {
				dao.create(cn.oa.model.table.CheckedRecord.class, false);
			}
		});
	}
}
