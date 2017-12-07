package test.dao;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.junit.Test;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.TableName;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import test.Setup;
import cn.oa.consts.Check;
import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.model.Archive;
import cn.oa.model.AttendanceResult;
import cn.oa.model.AttendanceThreshold;
import cn.oa.model.AttendanceThresholdItem;
import cn.oa.model.CheckRecord;
import cn.oa.model.CheckedRecord;
import cn.oa.model.Measure;
import cn.oa.model.SalaryRule;
import cn.oa.model.SalaryRuleItem;
import cn.oa.model.Shift;
import cn.oa.model.SocialSecurityRule;
import cn.oa.model.SocialSecurityRuleItem;
import cn.oa.model.Threshold;
import cn.oa.model.User;
import cn.oa.model.WorkDay;
import cn.oa.model.WorkMonth;
import cn.oa.repository.Mapper;
import cn.oa.repository.WorkRepository;
import cn.oa.service.CheckedRecordService;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.Rule;
import cn.oa.utils.Strings;
import cn.oa.utils.helper.Works;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;

public class DaoTest extends Setup {

	
	@Test
	public void coll() {
		User user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", 7));
		Threshold threshold = dao.fetch(Threshold.class, 1);
		String startStr = "2014-07-01";
		String endStr = "2014-07-10";
	//	resultService.coll(user, threshold, startStr, endStr, 1);
		
		
		AttendanceResult att = dao.fetch(AttendanceResult.class, Cnd
				.where("resultMonth", "=", "201407")
				.and("userId", "=", user.getUserId())
				.and("version", "=", Status.ENABLED));
		
		List<Measure> measures = dao.query(Measure.class, Cnd.where("orgId", "=", user.getCorpId()).desc("score"));
		Map<Float, Float> measureMap = new LinkedHashMap<Float, Float>();
		for (Measure measure : measures) {
			measureMap.put(measure.getScore(), measure.getCoefficient());
		}

		System.out.println(resultService);
		System.out.println(measureMap);
		System.out.println(threshold);
		System.out.println(att);
		
	//	resultService.wage(user, 3, measureMap, threshold, att.getWorkDay(), startStr, endStr, 1, new DateTime());
	}
	
	public void query() {
		List<User> users = mapper.query(User.class, "User.query",
				Cnd.where("u.status", "=", Status.ENABLED));
		for (User user : users) {
			System.out.println(user.getJobNumber());
		}
	}
	
	
	public void operators() {
		Integer corpId = 2;
		String roleName = Roles.PFM.getName();
		List<User> users = mapper.query(User.class, "User.operator", Cnd
				.where("u.status", "=", Status.ENABLED)
				.and("u.corp_id", "=", corpId)
				.and("r.role_name", "=", roleName));
		for (User u : users) {
			System.out.println(u.getTrueName());
		}
	}
	
	public void leave() {
		Long hour = mapper.count("Leave.hours", Cnd
				.where("status", "=", Status.ENABLED)
				.and("user_id", "=", 6)
				.and("type_id", "=", 1)
				.and("approve", "in", new int[] { Status.PROOFING, Status.APPROVED , Status.UNAPPROVED})
				.and("start_time", ">=", "2014-04-01 00:00:00")
				.and("end_time", "<=", "2015-04-01 23:59:59"));

		System.out.println(hour);
	}
	
	public void archive() {
		DateTime now = new DateTime();
		String yesterday = now.plusDays(0).toString("MM-dd");
		List<Archive> archives = mapper.query(Archive.class, "Archive.query", Cnd
				.where("u.status", "=", Status.ENABLED)
				.and("date_format(entry_date,'%m-%d')", "=", yesterday));

		for (Archive archive : archives) {
			System.out.println(archive.getEntryDate());
		}
	}
	
	public void attendance() {
		TableName.run("201404", new Runnable() {
			@Override
			public void run() {
				dao.create(CheckedRecord.class, false);				
			}
		});
	}

	public void works() {
		WorkRepository workRepository = ioc.get(WorkRepository.class);
		CheckedRecordService checkedRecordService = ioc.get(CheckedRecordService.class);
		Map<String, String[]> remarkMap = Works.getRemarks(
				"2014-04-12 09:00", "2014-04-15 13:00",
				"09:00", "18:00", "12:00", "13:00",
				workRepository.monthMap(2), workRepository.weekMap().get(1), "请假（待审批）");
		
		Integer userId = 5;
		
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.update"));
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			DateTime now = Calendars.parse(entry.getKey(), Calendars.DATE);
			String[] remarks = entry.getValue();
			checkedRecordService.createTable(dao, now);
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
					.set("modifyId", null)
					.set("modifyTime", null);
			dao.execute(sql);
		}
	}

	public void create() {
		Map<String, String> vars = new ConcurrentHashMap<String, String>();
		vars.put("month", "201012");
		List<CheckedRecord> attendances = mapper.query(CheckedRecord.class, "CheckedRecord.query",null,null, vars);
		for(CheckedRecord att: attendances) {
			System.out.println(att.getUserId());
			System.out.println(att.getJobNumber());
		}
	}
	
	public void table() {
		CheckedRecordService checkedRecordService = ioc.get(CheckedRecordService.class);
		checkedRecordService.createTable(dao, Calendars.parse("2010-12-10", Calendars.DATE));
	}
	
	public void checks() {
		WorkRepository workRepository = ioc.get(WorkRepository.class);
		CheckedRecordService checkedRecordService = ioc.get(CheckedRecordService.class);
		for (int i = 1; i < 9; i++) {
			String day = String.valueOf(i);
			if (day.length() == 1)
				day = "0" + day;
			
			DateTime now = Calendars.parse("2014-07-" + day, Calendars.DATE);
			// 1) 清除1个月前的打卡记录
			dao.clear(CheckRecord.class, Cnd.where("checkTime", "<", now.plusMonths(-1).toString("yyyy-MM-dd 23:59:59")));
			
			// 2) 排班
			Map<Integer, WorkDay> dayMap = workRepository.dayMap();
			Map<Integer, String[]> weekMap = workRepository.weekMap();
			
			// 3) 按月份新建考勤表
			checkedRecordService.createTable(dao, now);
						
			fill(now, dao, mapper, dayMap, weekMap, workRepository);
			sync(now, dao, mapper, dayMap, weekMap, workRepository);
		}
	}
	
	public void fill(DateTime now, Dao dao, Mapper mapper, Map<Integer, WorkDay> dayMap, Map<Integer, String[]> weekMap, WorkRepository workRepository) {
		List<User> users = mapper.query(User.class, "User.query", Cnd.where("u.status", "=", Status.ENABLED));
		
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.inserts"));
		sql.vars().set("month", now.toString("yyyyMM"));
		int batch = 0;
		for (User user : users) {
			if (user.getDayId() == null || user.getWeekId() == null) continue;
			if (!dayMap.containsKey(user.getDayId())) continue;
			Map<String, Integer[]> monthMap = workRepository.monthMap(user.getCorpId());
			
			boolean rest = false; // 公休
			
			if (monthMap.containsKey(now.toString("yyyyMM"))) {
				Integer[] days = monthMap.get(now.toString("yyyyMM"));
				if (!Asserts.hasAny(now.getDayOfMonth(), days)) rest = true;
			} else {
				if (!weekMap.containsKey(user.getWeekId())) continue;
				String[] days = weekMap.get(user.getWeekId());
				if (!Asserts.hasAny(now.toString("e"), days)) rest = true;
			}
			
			sql.params()
					.set("userId", user.getUserId())
					.set("workDate", now.toString(Calendars.DATE))
					.set("checkedIn", null)
					.set("checkedOut", null)
					.set("remarkedIn", null)
					.set("remarkedOut", null)
					.set("remarkIn", rest ? Check.REST : Check.ABSENT)
					.set("remarkOut", rest ? Check.REST : Check.ABSENT)
					.set("version", Status.DISABLED)
					.set("modifyId", null)
					.set("modifyTime", null);
			sql.addBatch();
			batch++;
		}
		if (batch > 0) {
			dao.execute(sql);
		}
	}
	
	public void sync(DateTime now, Dao dao, Mapper mapper, Map<Integer, WorkDay> dayMap, Map<Integer, String[]> weekMap, WorkRepository workRepository) {
		List<CheckRecord> records = mapper.query(CheckRecord.class, "CheckRecord.query", Cnd
				.where("r.check_time", ">=", now.toString("yyyy-MM-dd 00:00:00"))
				.and("r.check_time", "<=", now.toString("yyyy-MM-dd 23:59:59"))
				.groupBy("u.job_number"));
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.updates"));
		sql.vars().set("month", now.toString("yyyyMM"));
		int batch = 0;
		for (CheckRecord record : records) {
			if (record.getUserId() == null
					|| record.getDayId() == null
					|| record.getWeekId() == null)
				continue;
			if (!dayMap.containsKey(record.getDayId())) continue;
			Map<String, Integer[]> monthMap = workRepository.monthMap(record.getCorpId());
			
			boolean rest = false;
			
			if (monthMap.containsKey(now.toString("yyyyMM"))) {
				Integer[] days = monthMap.get(record.getCorpId() + "#" + now.toString("MM"));
				if (!Asserts.hasAny(now.getDayOfMonth(), days)) rest = true;
			} else {
				if (!weekMap.containsKey(record.getWeekId())) continue;
				String[] days = weekMap.get(record.getWeekId());
				if (!Asserts.hasAny(now.toString("e"), days)) rest = true;
			}
			
			WorkDay day = dayMap.get(record.getDayId());
			
			String checkIn = day.getCheckIn();
			String checkOut = day.getCheckOut();
//			String restIn = day.getRestIn();
			String restOut = day.getRestOut();
			
			String minIn = record.getMinIn();
			String maxOut = record.getMaxOut();

//			String checkedIn = minIn.compareTo(restIn) < 0 ? record.getMinIn() : null;
			String checkedIn = minIn.compareTo(restOut) < 0 ? record.getMinIn() : null;
			String checkedOut = maxOut.compareTo(restOut) > 0 ? record.getMaxOut() : null;
			
			String remarkIn = null;
			String remarkOut = null;
			
			if (rest) {
				remarkIn = Check.REST;
				remarkOut = Check.REST;
			} else {
				if (Strings.isBlank(checkedIn) && Strings.isBlank(checkedOut)) {
					remarkIn = Check.ABSENT;
					remarkOut = Check.ABSENT;
				} else {
					remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(checkIn) < 1 ? Check.NORMAL : Check.LATE);
					remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(checkOut) > -1 ? Check.NORMAL : Check.EARLY);
				}
			}
			
			sql.params()
					.set("userId", record.getUserId())
					.set("workDate", record.getCheckTime())
					.set("checkedIn", checkedIn)
					.set("checkedOut", checkedOut)
					.set("remarkedIn", null)
					.set("remarkedOut", null)
					.set("remarkIn", remarkIn)
					.set("remarkOut", remarkOut)
					.set("version", Status.DISABLED)
					.set("modifyId", null)
					.set("modifyTime", null);
			sql.addBatch();
			batch++;
		}
		if (batch > 0) {
			dao.execute(sql);
		}
	}
	
	public static void main(String[] args) {
		Criteria cri2 = Cnd
		.where("status", "=", Status.ENABLED)
		.orNot("startTime", "<=", "2014-10-1" + " 00:00:00")
		.orNot("startTime", ">=", "2014-10-31" + " 00:00:00")
		.orNot("endTime", "<=", "2014-10-1" + " 00:00:00")
		.orNot("startTime", ">=", "2014-10-31" + " 00:00:00");
	}
	
	@Test
	public void test(){
		List<SalaryRule> salaryRules = dao.query(SalaryRule.class, Cnd.where("status", "=", Status.ENABLED));
		Asserts.isEmpty(salaryRules, "工资规则配置集合不能为空");
		Map<Integer, SalaryRule> salaryRuleMap = new ConcurrentHashMap<Integer, SalaryRule>();
		for(SalaryRule salaryRule : salaryRules){
			Integer arr[] = Converts.array(salaryRule.getOrgIds(), ",");
			for(Integer a : arr){
				salaryRuleMap.put(a, salaryRule);
			}
		}
		SalaryRule salaryRule = salaryRuleMap.get(122);
		Asserts.isNull(salaryRule, "工资规则配置集合不能为空");
		System.out.println(salaryRule);
		
		List<SalaryRuleItem> salaryRuleItems= dao.query(SalaryRuleItem.class, Cnd.where("salaryRuleId", "=", salaryRule.getId()).and("status", "=", Status.ENABLED));
		Asserts.isNull(salaryRuleItems, "工资规则配置集合不能为空");

		for(SalaryRuleItem salaryRuleItem : salaryRuleItems){
			System.out.println(salaryRuleItem);
		}
	}
	
	//测试rule对象
	@Test
	public void test2(){
		List<String> strs = Rule.getChinese("应得工资 + 津贴 + 扣除");
		Rule rule = new Rule("应得工资 + 津贴 + 扣除", strs);
		rule.assign("应得工资", "100");
		rule.assign("津贴", "222");
		rule.assign("扣除", "333");
		System.out.println(rule.calculate());
	}
	
	
	@Test
	public void test3(){
		Date nowDate = new Date();
		
		WorkMonth workMonth = dao.fetch(WorkMonth.class, Cnd
				.where("orgId", "=", 121)
				.and("year", "=", "2017")
				.and("month", "=", "05")
				.and("status", "=", Status.ENABLED));

		String startStr = workMonth.getYear()+"-"+workMonth.getMonth();
		DateTime startDate = Calendars.parse(startStr, "yyyy-MM");
		DateTime endDate =startDate.plusMonths(1).plusDays(-1);
		String endStr = endDate.toString("yyyy-MM-dd");
		startStr = startDate.toString("yyyy-MM-dd");
		
		System.out.println(workMonth);
		
		Criteria criUser = Cnd.cri();
		criUser.where()
			.and("corp_id", "=", workMonth.getOrgId())
			.and("status", "=", Status.ENABLED);
		criUser.getOrderBy().desc("user_id");	
		//获取本公司所有非禁用的员工,遍历表单
		List<User> users = dao.query(User.class, criUser);
		System.out.println(users);
		System.out.println(endDate.getDayOfMonth());
		
		//获取排班的日子的集合
		String[] days = workMonth.getWorkDays().split(",");
		System.out.println(days.length);

		transSave(users, 55, startStr, endStr, nowDate, endDate, days);
	}
	
	private void transSave(final List<User> users, final Integer ClassesId, final String startStr,final String endStr,final Date nowDate,final DateTime endDate, final String[] days) {
		Trans.exec(new Atom() {
			@Override
			public void run() {		
				for(User u :users){

					dao.clear(Shift.class,Cnd.where("shift_date", ">=", startStr).and("shift_date", "<=", endStr).and("user_id", "=", u.getUserId()));
					
					for(int i=0; i < endDate.getDayOfMonth(); i++){
						boolean flag = true;
						for(String day : days){
							if(new Integer(day) == endDate.plusDays(-i).getDayOfMonth()){
								flag = false;
								break;
							}
						}
						if(flag){
							continue;
						}
						Shift shift = new Shift();							
						shift.setClasses(45);
						shift.setShiftDate(endDate.plusDays(-i).toDate());
						shift.setUserId(u.getUserId());
						shift.setCreateTime(nowDate);
						shift.setIsUsed(Status.ENABLED);
						shift.setModifyId(45);
						shift.setStatus(Status.ENABLED);
						dao.insert(shift);
					}
				}
			}
		});
	}
	
	@Test
	public void test4(){
//		SocialSecurityRule ssr = dao.fetch(SocialSecurityRule.class, Cnd.where("corpId", "=", 121).and("status", "=", Status.ENABLED));
//		List<SocialSecurityRuleItem> ssris = dao.query(SocialSecurityRuleItem.class, Cnd.where("socialSecurityRuleId", "=", ssr.getId()).and("status", "=", Status.ENABLED));
//		System.out.println(ssris);
		String rule = "工资*0.45[80000,-1]";
		 String str = rule.substring(rule.indexOf(',')+1, rule.indexOf(']'));
		System.out.println(str);
	}
}
