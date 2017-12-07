package cn.oa.app.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
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
import cn.oa.consts.Check;
import cn.oa.consts.Roles;
import cn.oa.consts.ShiftC;
import cn.oa.consts.Status;
import cn.oa.model.Archive;
import cn.oa.model.CheckRecord;
import cn.oa.model.Leave;
import cn.oa.model.Role;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.User;
import cn.oa.model.WorkDay;
import cn.oa.repository.Mapper;
import cn.oa.repository.WorkRepository;
import cn.oa.service.CheckedRecordService;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.Strings;

/**
 * 考勤数据同步.
 * 
 * @author JELLEE@Yeah.Net
 */
public class CheckedRecordJob implements Job {

	private static Log log = Logs.getLog(CheckedRecordJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			Ioc ioc = Jobs.getIoc(context);
			Dao dao = ioc.get(Dao.class);
			Mapper mapper = ioc.get(Mapper.class);
			
			CheckedRecordService checkedRecordService = ioc.get(CheckedRecordService.class);
			
			checkedRecordService.createTable(dao, new DateTime());
			
			this.syncAttendance(dao, mapper, new DateTime().minusDays(1));
			
		} catch (Exception e) {
			log.error("(CheckedRecordJob:execute) error: ", e);
		}
	}
	
	public void fill(DateTime now, Dao dao, Mapper mapper, Map<Integer, WorkDay> dayMap, Map<Integer, String[]> weekMap, WorkRepository workRepository) {
		List<User> users = mapper.query(User.class, "User.query", Cnd.where("u.status", "=", Status.ENABLED));
		String remarkIn = Check.ABSENT;
		String remarkOut = Check.ABSENT;
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.inserts"));
		sql.vars().set("month", now.toString("yyyyMM"));
		int batch = 0;
		for (User user : users) {
			if (user.getDayId() == null || user.getWeekId() == null) continue;
			if (!dayMap.containsKey(user.getDayId())) continue;
			Map<String, Integer[]> monthMap = workRepository.monthMap(user.getCorpId());
			if(user.getQuitDate()!=null){
				if(user.getQuitDate().compareTo(now.toDate())<=0){
					 remarkIn = Check.DIMISSION;
					 remarkOut = Check.DIMISSION;
				}
			}
			if(user.getEntryDate()!=null&&user.getEntryDate().compareTo(now.toDate())>0) continue;
			else{		
		//		boolean rest = false; // 公休
				
				//如果月排班和周排班都没有排到这一天 就是公休
				if (monthMap.containsKey(now.toString("yyyyMM"))) {
					Integer[] days = monthMap.get(now.toString("yyyyMM"));
					if (!Asserts.hasAny(now.getDayOfMonth(), days)){ remarkIn = Check.REST; remarkOut = Check.REST;}
				} 
				else{
					if (!weekMap.containsKey(user.getWeekId())) continue;
					String[] days = weekMap.get(user.getWeekId());
					if (!Asserts.hasAny(now.toString("e"), days)) { remarkIn = Check.REST; remarkOut = Check.REST;}
				}
			}
			
			sql.params()
					.set("userId", user.getUserId())
					.set("workDate", now.toString(Calendars.DATE))
					.set("checkedIn", null)
					.set("checkedOut", null)
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
				Integer[] days = monthMap.get(now.toString("yyyyMM"));
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
			//判断上班下班的打卡时间
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
	
	public void newFill(DateTime now, Dao dao, Mapper mapper) {
		List<User> users = mapper.query(User.class, "User.query", Cnd.where("u.status", "=", Status.ENABLED));
		String remarkIn = Check.ABSENT;
		String remarkOut = Check.ABSENT;
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.inserts"));
		sql.vars().set("month", now.toString("yyyyMM"));
		int batch = 0;
		for (User user : users) {
			Archive archive = dao.fetch(Archive.class,Cnd.where("user_id", "=", user.getUserId()));
			if(user.getQuitDate()!=null){
				if(user.getQuitDate().compareTo(now.toDate())<=0){// 已离职
					remarkIn = Check.DIMISSION;
					remarkOut = Check.DIMISSION;
				}
			} else if(archive != null && archive.getOnPosition() == 3) { //停薪
				remarkIn = Check.REST;
				remarkOut = Check.REST;
			} else {
				Shift shift = dao.fetch(Shift.class, Cnd.where("user_id", "=", user.getUserId()).and("shift_date", "=", now.toString("yyyy-MM-dd")));
				//如果排班表没有这个排班信息,则为公休,否则旷工
				if(shift==null){
					remarkIn = Check.REST;
					remarkOut = Check.REST;
				}else{
					boolean bl = true;
					if(shift.getClasses()==null){
						bl=false;
					}else{
						List<ShiftClass> list = dao.query(ShiftClass.class, Cnd.where("class_id", "=", shift.getClasses()));
						if(list==null){
							bl=false;
						}
					}					
					if(bl){
						remarkIn = Check.ABSENT;
						remarkOut = Check.ABSENT;
					}else{
						remarkIn = Check.REST;
						remarkOut = Check.REST;
					}
				}
			}
			sql.params()
					.set("userId", user.getUserId())
					.set("workDate", now.toString(Calendars.DATE))
					.set("checkedIn", null)
					.set("checkedOut", null)
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
	
	public void newNync(DateTime now, Dao dao, Mapper mapper) {
		List<CheckRecord> records = mapper.query(CheckRecord.class, "CheckRecord.query2", Cnd
				.where("r.check_time", ">=", now.toString("yyyy-MM-dd 00:00:00"))
				.and("r.check_time", "<=", now.toString("yyyy-MM-dd 23:59:59"))
				.and("u.status", "=", Status.ENABLED)
				.groupBy("u.job_number"));
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.updates"));
		sql.vars().set("month", now.toString("yyyyMM"));
		int batch = 0;
		for (CheckRecord record : records) {
			if (record.getUserId() == null)
				continue;
			boolean rest = false;			
			Shift shift = mapper.fetch(Shift.class, "Shiftinner.query",Cnd.where("s.user_id", "=", record.getUserId()).and("s.shift_date", "=", now.toString("yyyy-MM-dd")));
			String checkIn = null;
			String checkOut = null;
			String restIn = null;
			String restOut = null;
			boolean isNight = false;
			//如果排班表没有这个排班信息,则遍历下一个人
			if(shift==null){
				continue;
			}else{
				if(shift.getClasses()==null)continue;
				ShiftClass shiftClass = dao.fetch(ShiftClass.class, shift.getClasses());
				if(shiftClass==null)continue;				
				//获取上午上班时间:maxMorning,上午下班时间:restIn,下午下班时间:maxNight,下午上班时间:restOut
				//白班
				if(shiftClass.getNight() == ShiftC.DAY_IN){
				//二头班
				if(shiftClass.getSecond()==1){
					checkIn = shiftClass.getFirstMorning();
					restIn = shiftClass.getFirstNight();
					restOut = shiftClass.getSecondMorning();
					checkOut = shiftClass.getSecondNight();
				}else{
					checkIn = shiftClass.getFirstMorning();
					checkOut = shiftClass.getFirstNight();
				}
				//夜班
				}else if(shiftClass.getNight() == ShiftC.NIGHT_IN){
					DateTime nowN = now.plus(-1);
					Shift shiftN = dao.fetch(Shift.class, Cnd.where("user_id", "=", record.getUserId()).and("shift_date", "=", nowN.toString("yyyy-MM-dd")));
					if(shiftN == null)continue;
					ShiftClass shiftClassN = dao.fetch(ShiftClass.class, shiftN.getClasses());
					if(shiftClassN == null)continue;
					checkIn = shiftClassN.getFirstNight();
					checkOut = shiftClass.getFirstMorning();
					isNight = true;
				}else{
					continue;
				}
			}
			//上班下班的打卡时间
			String checkedIn = null;
			String checkedOut = null;
			String remarkIn = null;
			String remarkOut = null;

			//获取最大时间和最小时间,HH:mm
			String minIn = record.getMinIn();
			String maxOut = record.getMaxOut();
			//如果是一头班
			if(restIn==null||restOut==null){
				//获取两个时间的中间值,2015-2-6 为任意时间
				DateTime fm = Calendars.parse("2015-2-6 "+checkIn, Calendars.DATE_TIME);
				DateTime fn = Calendars.parse("2015-2-6 "+checkOut, Calendars.DATE_TIME);
				long beginDate = (fn.toDate().getTime()+fm.toDate().getTime())/2;
				SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");  				  
				String middle = sdf.format(new Date(beginDate)); 				
				checkedIn = minIn.compareTo(middle) < 0 ? record.getMinIn() : null;
				checkedOut = maxOut.compareTo(middle) > 0 ? record.getMaxOut() : null;
			}else{
				//上午时间段是否有请假
				int mLeave = dao.count(Leave.class,Cnd
						.where("end_time", ">=", now.toString("yyyy-MM-dd")+" "+checkIn+":00")
						.and("end_time", "<=", now.toString("yyyy-MM-dd")+" "+restIn+":00")
						.and("status","=",Status.ENABLED)
						.and("user_id","=",record.getUserId())
						.and("approve","=",Status.OK));
				//下午时间段是否有请假
				int nLeave = dao.count(Leave.class,Cnd
						.where("start_time", ">=", now.toString("yyyy-MM-dd")+" "+restIn+":00")
						.and("start_time", "<=", now.toString("yyyy-MM-dd")+" "+checkOut+":00")
						.and("status","=",Status.ENABLED)
						.and("user_id","=",record.getUserId())
						.and("approve","=",Status.OK));
				if((mLeave>0^nLeave>0)){
					String midle = null;
					if(mLeave>0){
						midle = Calendars.middleTime(restOut, checkOut);
						checkIn = restOut;
					}
					if(nLeave>0){
						midle = Calendars.middleTime(checkIn, restIn);
						checkOut = restIn;
					}	
					checkedIn = minIn.compareTo(midle) < 0 ? record.getMinIn() : null;
					checkedOut = maxOut.compareTo(midle) > 0 ? record.getMaxOut() : null;
				}else{
					checkedIn = minIn.compareTo(restIn) < 0 ? record.getMinIn() : null;
					checkedOut = maxOut.compareTo(restOut) > 0 ? record.getMaxOut() : null;
				}
			}
			
			if (rest) {
				remarkIn = Check.REST;
				remarkOut = Check.REST;
			} else {				
				//两次都没打卡为旷工
				if (Strings.isBlank(checkedIn) && Strings.isBlank(checkedOut)) {
					remarkIn = Check.ABSENT;
					remarkOut = Check.ABSENT;
				} else {
					if(isNight){
						remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(checkIn) > -1 ? Check.NORMAL : Check.EARLY);
						remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(checkOut) < 1 ? Check.NORMAL : Check.LATE);
					}else{
						remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(checkIn) < 1 ? Check.NORMAL : Check.LATE);
						remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(checkOut) > -1 ? Check.NORMAL : Check.EARLY);	
					}
				}
			}
			if(isNight){
				sql.params()
				.set("userId", record.getUserId())
				.set("workDate", record.getCheckTime())
				.set("checkedIn", checkedOut)
				.set("checkedOut", checkedIn)
				.set("remarkedIn", null)
				.set("remarkedOut", null)
				.set("remarkIn", remarkOut)
				.set("remarkOut", remarkIn)
				.set("version", Status.DISABLED)
				.set("modifyId", null)
				.set("modifyTime", null);	
			}else{
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
			}
			sql.addBatch();			
			batch++;
		}
		if (batch > 0) {
			dao.execute(sql);
		}
	}
	
	
	/**
	 * 将打卡记录汇总到考勤记录
	 * @param dao
	 * @param mapper
	 * @param syncDate 需要同步的日期
	 */
	public void syncAttendance(Dao dao, Mapper mapper, DateTime syncDate) {
		List<User> users = mapper.query(User.class, "User.query", Cnd.where("u.status", "=", Status.ENABLED));
		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.inserts"));
		sql.vars().set("month", syncDate.toString("yyyyMM"));
		int batch = 0;
		for (User user : users) {
			//上班下班的打卡时间
			String checkedIn = null;
			String checkedOut = null;
			String remarkIn = Check.ABSENT;
			String remarkOut = Check.ABSENT;
			Integer userId = user.getUserId();
			
			Archive archive = dao.fetch(Archive.class,Cnd.where("user_id", "=", userId));
			if(user.getQuitDate()!=null && user.getQuitDate().compareTo(syncDate.toDate())<=0){// 已离职
				remarkIn = Check.DIMISSION;
				remarkOut = Check.DIMISSION;
			} else if(archive != null && archive.getOnPosition() != null && archive.getOnPosition() == 3) { //停薪
				remarkIn = Check.REST;
				remarkOut = Check.REST;
			} else {
				Shift shift = dao.fetch(Shift.class, Cnd.where("user_id", "=", userId).and("shift_date", "=", syncDate.toString("yyyy-MM-dd")));
				List<CheckRecord> checkRecords = mapper.query(CheckRecord.class, "CheckRecord.query2", Cnd.where("u.user_id", "=", userId)
						.and("r.check_time", ">=", syncDate.toString("yyyy-MM-dd 00:00:00"))
						.and("r.check_time", "<=", syncDate.toString("yyyy-MM-dd 23:59:59")));
				//如果排班表没有这个排班信息,则为公休,否则旷工
				if(shift==null){
					//需要从ioc容器中获取workRepository，workRepository，workRepository
//					Map<Integer, WorkDay> dayMap = workRepository.dayMap();
//					Map<Integer, String[]> weekMap = workRepository.weekMap();
//					Map<String, Integer[]> monthOfDays = workRepository.monthMap(user.getCorpId()); //月排班
					
					remarkIn = Check.REST;
					remarkOut = Check.REST;
					//虽然是公休，但还是记录了当天的打卡信息
					if(checkRecords != null && checkRecords.size() > 0){
						CheckRecord record = checkRecords.get(0);
						checkedIn = record.getMinIn();
						checkedOut = record.getMaxOut();
						if(checkedIn != null && checkedOut != null && checkedIn.equals(checkedOut)){
							checkedOut = null;
						}
					}
				}else{
					ShiftClass shiftClass = dao.fetch(ShiftClass.class, shift.getClasses());
					
					if(checkRecords != null && checkRecords.size() > 0){
						CheckRecord record = checkRecords.get(0);
						String checkIn = null; //上午上班时间
						String checkOut = null;//下午下班时间
						String restIn = null;//上午下班时间
						String restOut = null;//下午上班时间

						//获取最早一次打卡时间和最晚一次时间,HH:mm
						String minIn = record.getMinIn();
						String maxOut = record.getMaxOut();
						if(minIn != null && maxOut != null){
							if(shiftClass.getNight() == ShiftC.DAY_IN){ //白班
								if(shiftClass.getSecond()==1){//二头班
									checkIn = shiftClass.getFirstMorning();
									restIn = shiftClass.getFirstNight();
									restOut = shiftClass.getSecondMorning();
									checkOut = shiftClass.getSecondNight();
									
									//上午时间段是否有请假
									int mLeave = dao.count(Leave.class,Cnd
											.where("user_id","=", userId)
											.and("approve","=",Status.OK)
											.and("status","=",Status.ENABLED)
											.and("end_time", ">=", syncDate.toString("yyyy-MM-dd")+" "+checkIn+":00")
											.and("end_time", "<=", syncDate.toString("yyyy-MM-dd")+" "+restIn+":00"));
									//下午时间段是否有请假
									int nLeave = dao.count(Leave.class,Cnd
											.where("user_id","=", userId)
											.and("approve","=",Status.OK)
											.and("status","=",Status.ENABLED)
											.and("start_time", ">=", syncDate.toString("yyyy-MM-dd")+" "+restIn+":00")
											.and("start_time", "<=", syncDate.toString("yyyy-MM-dd")+" "+checkOut+":00"));
									if((mLeave>0^nLeave>0)){//上午请假或者下午请假
										String midle = null;
										if(mLeave>0){
											midle = Calendars.middleTime(restOut, checkOut);
											checkIn = restOut;
										}
										if(nLeave>0){
											midle = Calendars.middleTime(checkIn, restIn);
											checkOut = restIn;
										}	
										checkedIn = midle.compareTo(minIn) > 0 ? minIn : null;
										checkedOut = midle.compareTo(maxOut) < 0 ? maxOut : null;
									}else{
										checkedIn = restIn.compareTo(minIn) > 0 ? minIn : null;
										checkedOut = restOut.compareTo(maxOut) < 0 ? maxOut : null;
									}
								}else{//一头班
									checkIn = shiftClass.getFirstMorning();
									checkOut = shiftClass.getFirstNight();
									
									//获取两个时间的中间值,2015-2-6 为任意时间，目的是判断checkIn或checkOut是否为null
									DateTime fm = Calendars.parse("2015-2-6 " + checkIn, Calendars.DATE_TIME);
									DateTime fn = Calendars.parse("2015-2-6 " + checkOut, Calendars.DATE_TIME);
									long beginDate = (fn.toDate().getTime() + fm.toDate().getTime()) / 2;
									SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
									String middle = sdf.format(new Date(beginDate));
									checkedIn = minIn.compareTo(middle) < 0 ? minIn : null;
									checkedOut = maxOut.compareTo(middle) > 0 ? maxOut : null;
								}
							} else if(shiftClass.getNight() == ShiftC.NIGHT_IN){//夜班
								checkIn = shiftClass.getFirstNight();
								checkOut = shiftClass.getFirstMorning();
								
								//晚班以12：00为中间时间
								DateTime fm = Calendars.parse("2015-2-6 12:00", Calendars.DATE_TIME);
								SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
								String middle = sdf.format(new Date(fm.toDate().getTime()));
								checkedIn = minIn.compareTo(middle) < 0 ? minIn : null; //早上下班打卡时间
								checkedOut = maxOut.compareTo(middle) > 0 ? maxOut : null;//晚上上班打卡时间
							}
						} else {
							log.info("没有当天的打卡记录!");
						}
						
						if (!(Strings.isBlank(checkedIn) && Strings.isBlank(checkedOut))) {
							if(shiftClass.getNight() == ShiftC.NIGHT_IN){
								//判断上下班是否没有打卡，或者打卡时间早于或晚于班次规定的时间
								remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(checkIn) > -1 ? Check.NORMAL : Check.EARLY);//早上下班考勤
								remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(checkOut) < 1 ? Check.NORMAL : Check.LATE);//晚上上班考勤
							}else{
								remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(checkIn) < 1 ? Check.NORMAL : Check.LATE);
								
								boolean isMan = false;
								List<Role> roles = mapper.query(Role.class, "Role.query", Cnd
										.where("r.status", "=", Status.ENABLED)
										.and("u.user_id", "=", userId));
								String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
								for(String role : roleNames){
									// 总经理角色可以下班免打卡
									if(Roles.GM.getName().equals(role)){
										isMan = true;
										break;
									}
								}
								if(isMan){
									if(Strings.isBlank(checkedOut)){
										checkedOut = shiftClass.getSecondNight();
										remarkOut = Check.NORMAL;
									} else {
										remarkOut = checkedOut.compareTo(checkOut) > -1 ? Check.NORMAL : Check.EARLY;
									}
								} else {
									remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(checkOut) > -1 ? Check.NORMAL : Check.EARLY);
								}
							}
						}
					}
				}
			}
			sql.params()
			.set("userId", userId)
			.set("workDate", syncDate.toString(Calendars.DATE))
			.set("checkedIn", checkedIn)
			.set("checkedOut", checkedOut)
			.set("remarkedIn", null)
			.set("remarkedOut", null)
//			.set("remarkIn", remarkIn)
//			.set("remarkOut", remarkOut)
			.set("remarkIn", "")
			.set("remarkOut", "")
			.set("version", Status.DISABLED)
			.set("modifyId", null)
			.set("modifyTime", new Date());
			
			sql.addBatch();
			batch++;
		}
		if (batch > 0) {
			dao.execute(sql);
		}
	}
	
	
	/**
	 * 将某个用户的打卡记录汇总到考勤记录
	 * @param dao
	 * @param mapper
	 * @param syncDate 需要同步的日期
	 */
	public void syncAttendance(Dao dao, Mapper mapper, DateTime syncDate, User user) {

		Sql sql = Sqls.create(dao.sqls().get("CheckedRecord.inserts"));
		sql.vars().set("month", syncDate.toString("yyyyMM"));
		int batch = 0;
	
		//上班下班的打卡时间
		String checkedIn = null;
		String checkedOut = null;
		String remarkIn = Check.ABSENT;
		String remarkOut = Check.ABSENT;
		Integer userId = user.getUserId();
		
		Archive archive = dao.fetch(Archive.class,Cnd.where("user_id", "=", userId));
		if(user.getQuitDate()!=null && user.getQuitDate().compareTo(syncDate.toDate())<=0){// 已离职
			remarkIn = Check.DIMISSION;
			remarkOut = Check.DIMISSION;
		} else if(archive != null && archive.getOnPosition() != null && archive.getOnPosition() == 3) { //停薪
			remarkIn = Check.REST;
			remarkOut = Check.REST;
		} else {
			Shift shift = dao.fetch(Shift.class, Cnd.where("user_id", "=", userId).and("shift_date", "=", syncDate.toString("yyyy-MM-dd")));
			List<CheckRecord> checkRecords = mapper.query(CheckRecord.class, "CheckRecord.query2", Cnd.where("u.user_id", "=", userId)
					.and("r.check_time", ">=", syncDate.toString("yyyy-MM-dd 00:00:00"))
					.and("r.check_time", "<=", syncDate.toString("yyyy-MM-dd 23:59:59")));
			//如果排班表没有这个排班信息,则为公休,否则旷工
			if(shift==null){
				//需要从ioc容器中获取workRepository，workRepository，workRepository
//					Map<Integer, WorkDay> dayMap = workRepository.dayMap();
//					Map<Integer, String[]> weekMap = workRepository.weekMap();
//					Map<String, Integer[]> monthOfDays = workRepository.monthMap(user.getCorpId()); //月排班
				
				remarkIn = Check.REST;
				remarkOut = Check.REST;
				//虽然是公休，但还是记录了当天的打卡信息
				if(checkRecords != null && checkRecords.size() > 0){
					CheckRecord record = checkRecords.get(0);
					checkedIn = record.getMinIn();
					checkedOut = record.getMaxOut();
					if(checkedIn != null && checkedOut != null && checkedIn.equals(checkedOut)){
						checkedOut = null;
					}
				}
			}else{
				ShiftClass shiftClass = dao.fetch(ShiftClass.class, shift.getClasses());
				
				if(checkRecords != null && checkRecords.size() > 0){
					CheckRecord record = checkRecords.get(0);
					String checkIn = null; //上午上班时间
					String checkOut = null;//下午下班时间
					String restIn = null;//上午下班时间
					String restOut = null;//下午上班时间

					//获取最早一次打卡时间和最晚一次时间,HH:mm
					String minIn = record.getMinIn();
					String maxOut = record.getMaxOut();
					if(minIn != null && maxOut != null){
						if(shiftClass.getNight() == ShiftC.DAY_IN){ //白班
							if(shiftClass.getSecond()==1){//二头班
								checkIn = shiftClass.getFirstMorning();
								restIn = shiftClass.getFirstNight();
								restOut = shiftClass.getSecondMorning();
								checkOut = shiftClass.getSecondNight();
								
								//上午时间段是否有请假
								int mLeave = dao.count(Leave.class,Cnd
										.where("user_id","=", userId)
										.and("approve","=",Status.OK)
										.and("status","=",Status.ENABLED)
										.and("end_time", ">=", syncDate.toString("yyyy-MM-dd")+" "+checkIn+":00")
										.and("end_time", "<=", syncDate.toString("yyyy-MM-dd")+" "+restIn+":00"));
								//下午时间段是否有请假
								int nLeave = dao.count(Leave.class,Cnd
										.where("user_id","=", userId)
										.and("approve","=",Status.OK)
										.and("status","=",Status.ENABLED)
										.and("start_time", ">=", syncDate.toString("yyyy-MM-dd")+" "+restIn+":00")
										.and("start_time", "<=", syncDate.toString("yyyy-MM-dd")+" "+checkOut+":00"));
								if((mLeave>0^nLeave>0)){//上午请假或者下午请假
									String midle = null;
									if(mLeave>0){
										midle = Calendars.middleTime(restOut, checkOut);
										checkIn = restOut;
									}
									if(nLeave>0){
										midle = Calendars.middleTime(checkIn, restIn);
										checkOut = restIn;
									}	
									checkedIn = midle.compareTo(minIn) > 0 ? minIn : null;
									checkedOut = midle.compareTo(maxOut) < 0 ? maxOut : null;
								}else{
									checkedIn = restIn.compareTo(minIn) > 0 ? minIn : null;
									checkedOut = restOut.compareTo(maxOut) < 0 ? maxOut : null;
								}
							}else{//一头班
								checkIn = shiftClass.getFirstMorning();
								checkOut = shiftClass.getFirstNight();
								
								//获取两个时间的中间值,2015-2-6 为任意时间，目的是判断checkIn或checkOut是否为null
								DateTime fm = Calendars.parse("2015-2-6 " + checkIn, Calendars.DATE_TIME);
								DateTime fn = Calendars.parse("2015-2-6 " + checkOut, Calendars.DATE_TIME);
								long beginDate = (fn.toDate().getTime() + fm.toDate().getTime()) / 2;
								SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
								String middle = sdf.format(new Date(beginDate));
								checkedIn = minIn.compareTo(middle) < 0 ? minIn : null;
								checkedOut = maxOut.compareTo(middle) > 0 ? maxOut : null;
							}
						} else if(shiftClass.getNight() == ShiftC.NIGHT_IN){//夜班
							checkIn = shiftClass.getFirstNight();
							checkOut = shiftClass.getFirstMorning();
							
							//晚班以12：00为中间时间
							DateTime fm = Calendars.parse("2015-2-6 12:00", Calendars.DATE_TIME);
							SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
							String middle = sdf.format(new Date(fm.toDate().getTime()));
							checkedIn = minIn.compareTo(middle) < 0 ? minIn : null; //早上下班打卡时间
							checkedOut = maxOut.compareTo(middle) > 0 ? maxOut : null;//晚上上班打卡时间
						}
					} else {
						log.info("没有当天的打卡记录!");
					}
					
					if (!(Strings.isBlank(checkedIn) && Strings.isBlank(checkedOut))) {
						if(shiftClass.getNight() == ShiftC.NIGHT_IN){
							//判断上下班是否没有打卡，或者打卡时间早于或晚于班次规定的时间
							remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(checkIn) > -1 ? Check.NORMAL : Check.EARLY);//早上下班考勤
							remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(checkOut) < 1 ? Check.NORMAL : Check.LATE);//晚上上班考勤
						}else{
							remarkIn = Strings.isBlank(checkedIn) ? Check.UNCHECKED : (checkedIn.compareTo(checkIn) < 1 ? Check.NORMAL : Check.LATE);
							
							boolean isMan = false;
							List<Role> roles = mapper.query(Role.class, "Role.query", Cnd
									.where("r.status", "=", Status.ENABLED)
									.and("u.user_id", "=", userId));
							String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
							for(String role : roleNames){
								// 总经理角色可以下班免打卡
								if(Roles.GM.getName().equals(role)){
									isMan = true;
									break;
								}
							}
							if(isMan){
								if(Strings.isBlank(checkedOut)){
									checkedOut = shiftClass.getSecondNight();
									remarkOut = Check.NORMAL;
								} else {
									remarkOut = checkedOut.compareTo(checkOut) > -1 ? Check.NORMAL : Check.EARLY;
								}
							} else {
								remarkOut = Strings.isBlank(checkedOut) ? Check.UNCHECKED : (checkedOut.compareTo(checkOut) > -1 ? Check.NORMAL : Check.EARLY);
							}
						}
					}
				}
			}
		}
		sql.params()
		.set("userId", userId)
		.set("workDate", syncDate.toString(Calendars.DATE))
		.set("checkedIn", checkedIn)
		.set("checkedOut", checkedOut)
		.set("remarkedIn", null)
		.set("remarkedOut", null)
			.set("remarkIn", remarkIn)
			.set("remarkOut", remarkOut)
//		.set("remarkIn", "")
//		.set("remarkOut", "")
		.set("version", Status.DISABLED)
		.set("modifyId", null)
		.set("modifyTime", new Date());
		
		sql.addBatch();
		batch++;
	
		if (batch > 0) {
			dao.execute(sql);
		}
	}
}
