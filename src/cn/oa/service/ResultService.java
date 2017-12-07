package cn.oa.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;








import cn.oa.app.schedule.CheckedRecordJob;
import cn.oa.consts.Check;
import cn.oa.consts.ShiftC;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Archive;
import cn.oa.model.AttendanceResult;
import cn.oa.model.AttendanceThresholdItem;
import cn.oa.model.CheckedRecord;
import cn.oa.model.ConfLeaveType;
import cn.oa.model.Dict;
import cn.oa.model.Errand;
import cn.oa.model.HistoryWage;
import cn.oa.model.Leave;
import cn.oa.model.LeaveActor;
import cn.oa.model.Outwork;
import cn.oa.model.Overtime;
import cn.oa.model.SalaryRuleItem;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.ShiftHoliday;
import cn.oa.model.SocialSecurityRule;
import cn.oa.model.SocialSecurityRuleItem;
import cn.oa.model.Target;
import cn.oa.model.TaxRule;
import cn.oa.model.TaxRuleItem;
import cn.oa.model.User;
import cn.oa.model.Wage;
import cn.oa.model.WageResult;
import cn.oa.model.WorkDay;
import cn.oa.repository.Mapper;
import cn.oa.repository.WorkRepository;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.RMB;
import cn.oa.utils.Rule;
import cn.oa.utils.Strings;
import cn.oa.utils.Values;
import cn.oa.utils.helper.Works;
import cn.oa.web.Context;

@IocBean
public class ResultService{
	
	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private WorkRepository workRepository;
	@Inject
	private CheckedRecordService checkedRecordService;
	
	@Inject
	private DictService dictService;
	
	public static Log log = Logs.getLog(ResultService.class);
	
	
	//生成汇总
	public void coll(User user, List<AttendanceThresholdItem> thresholdItem, String startStr, String endStr, Integer modifyId,String attendanceStartStr, String attendanceEndStr) {
		String startWork = startStr;
		
		if (user.getEntryDate() == null) return; //如果入职日期为空，不汇总
		DateTime entry = new DateTime(user.getEntryDate()); 
		String entryStr = entry.toString(Calendars.DATE);
		//获取工作时间
		if (entryStr.compareTo(startStr) > 0) startWork = entryStr;
		
		Criteria cri = Cnd
				.where("status", "=", Status.ENABLED)
				.and("userId", "=", user.getUserId())
				.and("approve", "=", Status.APPROVED)
				/*.and("startTime", ">=", startWork + " 00:00:00")
				.and("startTime", "<=", endStr + " 23:59:59");*/
				.and(Cnd.exps(Cnd.exps("startTime", ">=", startWork + " 00:00:00").and("startTime", "<=", endStr+" 23:59:59")).or(Cnd.exps("endTime", ">=", startStr).and("endTime", "<=", endStr)));
		
		Criteria criLeave = Cnd
		.where("status", "=", Status.ENABLED)
		.and("userId", "=", user.getUserId())
		.and("approve", "=", Status.OK)
//		.and("startTime", ">=", startWork + " 00:00:00")
//		.and("startTime", "<=", endStr + " 23:59:59");
		.and(Cnd.exps(Cnd.exps("startTime", ">=", startWork + " 00:00:00").and("startTime", "<=", endStr+" 23:59:59")).or(Cnd.exps("endTime", ">=", startStr).and("endTime", "<=", endStr)));
		
		List<Leave> leaves = dao.query(Leave.class, criLeave);
		List<Outwork> outworks = dao.query(Outwork.class, cri);
		List<Errand> errands = dao.query(Errand.class, cri);
		List<Overtime> overtimes = dao.query(Overtime.class, cri);
		
		//查询日排班，周排班，月排班，可进一步查询指定的日期是否在排班中
		Map<Integer, WorkDay> dayMap = workRepository.dayMap();
		Map<Integer, String[]> weekMap = workRepository.weekMap();
		Map<String, Integer[]> monthOfDays = workRepository.monthMap(user.getCorpId()); //月排班

		WorkDay day = dayMap.get(user.getDayId());
		Asserts.isNull(day, "日排班不能为空值");
		String[] weekOfDays = weekMap.get(user.getWeekId());
		Asserts.isEmpty(weekOfDays, "周排班不能为空值");
		
	//	Map<String, Integer[]> holidayOfDays = workRepository.monthMap(user.getCorpId()); //法定假期
		
		Map<String, String> dictMap = dictService.map(Dict.LEAVE);
		//根据排班获取指定时间段内工作日的天数，注意包含节假日
		
		int workDay = Works.holidayWork(attendanceStartStr, attendanceEndStr, monthOfDays, weekOfDays); //根据月排班和周排班设定工作日,月排班比周排班优先级高.此处工作日包含法定假期
		int workMinute = Works.workMinute(day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut()); //日工作分钟
		
		DateTime now = new DateTime();
		
		// 1) 统计旷工, 迟到, 早退的次数
		Map<Integer,Integer> thresholdMap = times(thresholdItem, user.getUserId(), startWork, endStr, leaves, outworks, errands, day);
		// 2) 请假
		int unpaidLeaveMinute[] = leave(startWork, endStr, "无薪事假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
		int sickLeaveMinute[] = leave(startWork, endStr, "病假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
		int funeralLeaveMinute[] = leave(startWork, endStr, "丧假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
		int maritalLeaveMinute[] = leave(startWork, endStr, "婚假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
		int annualLeaveMinute[] = leave(startWork, endStr, "年休假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
		int injuryLeaveMinute[] = leave(startWork, endStr, "工伤假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
		int deferredLeaveMinute[] = leave(startWork, endStr, "加班补休", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
		int paternityLeaveMinute[] = leave(startWork, endStr, "陪产假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
		int paidLeaveMinute[] = leave(startWork, endStr, "有薪事假", leaves, dictMap, workMinute, day, monthOfDays, weekOfDays);
		// 3) 外勤
		int outworkMinute[] = outwork(startWork, endStr, outworks, workMinute, day, monthOfDays, weekOfDays);
		// 4) 出差
		int errandMinute[] = errand(startWork, endStr, errands, workMinute, day, monthOfDays, weekOfDays);
		// 5) 加班
		int overtimeMinute = overtime(startWork, endStr, overtimes);

		boolean exist = true;
		
		String resultMonth = Calendars.parse(endStr, Calendars.DATE).toString("yyyyMM");
		if(user.getInterim().equals("interim1"))
			resultMonth = Calendars.parse(endStr, Calendars.DATE).toString("yyyyMM")+"-01";
		if(user.getInterim().equals("interim2"))
			resultMonth = Calendars.parse(endStr, Calendars.DATE).toString("yyyyMM")+"-02";
		AttendanceResult att = dao.fetch(AttendanceResult.class, Cnd.where("resultMonth", "=", resultMonth).and("userId", "=", user.getUserId()));
		
		if (att != null) {
			if (att.getVersion().equals(Status.ENABLED)) return;
		} else {
			att = new AttendanceResult();
			exist = false;
		}
		Integer shouldWorkDay = 0; //应出勤天数
		/*if(user.getQuitDate()==null)
			shouldWorkDay = workDay;
		else{
			DateTime end = Calendars.parse(user.getQuitDate(), Calendars.DATE).plusDays(-1);
			String endStart = Calendars.str(end, Calendars.DATE);
			shouldWorkDay = Works.holidayWork(startStr,endStart, monthOfDays, weekOfDays);//应出勤天数区间为[),
		}*/
		if(user.getQuitDate()==null){
			shouldWorkDay = Works.holidayWork(startWork, endStr, monthOfDays, weekOfDays);
		}
		else{
			DateTime end = Calendars.parse(user.getQuitDate(), Calendars.DATE).plusDays(-1);
			String endStart = Calendars.str(end, Calendars.DATE);
			shouldWorkDay = Works.holidayWork(startWork, endStart, monthOfDays, weekOfDays);
		}
			
		att.setShouldWorkDay(shouldWorkDay);
		String thresholdMapStart = Strings.removeStart(thresholdMap.toString(), "{");
		String thresholdMapEnd = Strings.removeEnd(thresholdMapStart, "}");
		att.setThreshold(thresholdMapEnd);
		att.setResultMonth(resultMonth);
		att.setUserId(user.getUserId());
		att.setStartDate(Calendars.parse(startWork, Calendars.DATE).toDate());
		att.setEndDate(Calendars.parse(endStr, Calendars.DATE).toDate());
		att.setWorkDay(workDay);
		att.setWorkMinute(workMinute);
		att.setUnpaidLeaveMinute(unpaidLeaveMinute[0]);
		att.setSickLeaveMinute(sickLeaveMinute[0]);
		att.setFuneralLeaveMinute(funeralLeaveMinute[0]);
		att.setMaritalLeaveMinute(maritalLeaveMinute[0]);
		att.setAnnualLeaveMinute(annualLeaveMinute[0]);
		att.setInjuryLeaveMinute(injuryLeaveMinute[0]);
		att.setDeferredLeaveMinute(deferredLeaveMinute[0]);
		att.setPaternityLeaveMinute(paternityLeaveMinute[0]);
		att.setPaidLeaveMinute(paidLeaveMinute[0]);
		att.setOutworkMinute(outworkMinute[0]);
		att.setErrandMinute(errandMinute[0]);
		att.setOvertimeMinute(overtimeMinute);
		att.setVersion(Status.DISABLED);
		att.setModifyId(modifyId);
		att.setModifyTime(now.toDate());
		att.setAbsentAmount(thresholdMap.get(-3));
		att.setLateAmount(thresholdMap.get(-1)+thresholdMap.get(-2));
		att.setForgetAmount(thresholdMap.get(-4));
		//停薪
		Archive archive = dao.fetch(Archive.class,Cnd.where("user_id", "=", user.getUserId()));
		if(archive!=null){
		if(archive.getOnPosition()==3){
			att.setShouldWorkDay(0);
			att.setWorkDay(0);
			/*att.setWorkMinute(workMinute);*/
			att.setUnpaidLeaveMinute(0);
			att.setSickLeaveMinute(0);
			att.setFuneralLeaveMinute(0);
			att.setMaritalLeaveMinute(0);
			att.setAnnualLeaveMinute(0);
			att.setInjuryLeaveMinute(0);
			att.setDeferredLeaveMinute(0);
			att.setPaternityLeaveMinute(0);
			att.setPaidLeaveMinute(0);
			att.setOutworkMinute(0);
			att.setErrandMinute(0);
			att.setOvertimeMinute(0);
			att.setModifyTime(now.toDate());
			att.setAbsentAmount(0);
			att.setLateAmount(0);
			att.setForgetAmount(0);
		}
		}
		if (exist)
			dao.update(att);
		else
			dao.insert(att);
	}
	
	//生成考勤汇总
	public void newColl(User user, List<AttendanceThresholdItem> thresholdItem, String startStr, String endStr, Integer modifyId,String attendanceStartStr, String attendanceEndStr) {

		
		String startWork = startStr;
		
		if (user.getEntryDate() == null){
			log.error("考勤汇总放弃：入职日期为空！工号:" + user.getJobNumber());
			return; //如果入职日期为空，不汇总
		}
		DateTime entry = new DateTime(user.getEntryDate()); 
		String entryStr = entry.toString(Calendars.DATE);
		//获取工作时间
		if (entryStr.compareTo(startStr) > 0){ 
			startWork = entryStr;}
		
		Criteria cri = Cnd
				.where("status", "=", Status.ENABLED)
				.and("userId", "=", user.getUserId())
				.and("approve", "=", Status.APPROVED)
				.and(Cnd.exps(Cnd.exps("startTime", ">=", startWork + " 00:00:00").and("startTime", "<=", endStr+" 23:59:59")).or(Cnd.exps("endTime", ">=", startStr).and("endTime", "<=", endStr)));
		
		Criteria criLeave = Cnd
		.where("status", "=", Status.ENABLED)
		.and("userId", "=", user.getUserId())
		.and("approve", "=", Status.OK)
		.and(Cnd.exps(Cnd.exps("startTime", ">=", startWork + " 00:00:00").and("startTime", "<=", endStr+" 23:59:59")).or(Cnd.exps("endTime", ">=", startStr).and("endTime", "<=", endStr)));
		DateTime now = new DateTime();
		List<Leave> leaves = dao.query(Leave.class, criLeave);
		System.out.println(leaves.size());
		for(Leave leave : leaves){
			System.out.println(leave);
		}
		List<Outwork> outworks = dao.query(Outwork.class, cri);
		List<Errand> errands = dao.query(Errand.class, cri);
		List<Overtime> overtimes = dao.query(Overtime.class, cri);
		
		
		Map<String, String[]> leaveDayMap = new ConcurrentHashMap<String, String[]>();
		Map<String, String[]> outworkDayMap = new ConcurrentHashMap<String, String[]>();
		Map<String, String[]> errandDayMap = new ConcurrentHashMap<String, String[]>();
		Map<String, String[]> dayMapForCheckedRecord = new ConcurrentHashMap<String, String[]>();
		//请假类型
		Map<String, String> leaveMap = dictService.map(Dict.LEAVE);
		
		for (Leave l : leaves) {
			DateTime leaveStart = new DateTime(l.getStartTime());
			DateTime leaveEnd = new DateTime(l.getEndTime());
			String typeName = leaveMap.get(String.valueOf(l.getTypeId()));
			//以请假的每一天作为key，请假的类型作为value，存入map集合中
			
			LeaveActor leaveActor = dao.fetch(LeaveActor.class, Cnd.where("leaveId", "=", l.getLeaveId()).and("refererId", "=", 0).and("approve", "=", 1));
			String approve = "未通过";
			if(leaveActor != null){
				approve = "已通过";
			}
			Works.dayMap(leaveDayMap, leaveStart.toString(Calendars.DATE_TIME), leaveEnd.toString(Calendars.DATE_TIME),typeName, approve);
			System.out.println(leaveDayMap.size());

		}
		for (Outwork outwork : outworks) {
			DateTime outworkStart = new DateTime(outwork.getStartTime());
			DateTime outworkEnd = new DateTime(outwork.getEndTime());
			//以外勤的每一天作为key，存入map集合中
			Works.dayMap(outworkDayMap, outworkStart.toString(Calendars.DATE_TIME), outworkEnd.toString(Calendars.DATE_TIME));
		}
		for (Errand errand : errands) {
			DateTime errandStart = new DateTime(errand.getStartTime());
			DateTime errandEnd = new DateTime(errand.getEndTime());
			//以出差的每一天作为key，存入map集合中
			Works.dayMap(errandDayMap, errandStart.toString(Calendars.DATE_TIME), errandEnd.toString(Calendars.DATE_TIME));
		}
		
		
		int unpaidLeaveMinute = 0;
		int sickLeaveMinute = 0;
		int funeralLeaveMinute = 0;
		int maritalLeaveMinute = 0;
		int annualLeaveMinute = 0;
		int injuryLeaveMinute = 0;
		int deferredLeaveMinute = 0;
		int paternityLeaveMinute = 0;
		int paidLeaveMinute = 0;
		int outworkMinute = 0;
		int errandMinute = 0;
		Map<Date, WorkDay> day = new ConcurrentHashMap<Date, WorkDay>();
		
		//根据startStr获取当月的第一天和最后一天		
		String[] firstAndLastDay = Calendars.timeToMT(Calendars.parse(startStr, Calendars.DATE));
		// 当月的法定假期
		List<ShiftHoliday> curMonthHoliday = dao.query(ShiftHoliday.class, Cnd.where("holiday", ">=", firstAndLastDay[0]).and("holiday", "<=", firstAndLastDay[1]));
		//标准月法定假天数
		Integer standardHolidayDays = 0 ;
		Date[] curMonthHolidays = null;
		if(curMonthHoliday != null){
			standardHolidayDays = curMonthHoliday.size();
			curMonthHolidays = new Date[standardHolidayDays];
			for(int i = 0; i < standardHolidayDays; i++){
				curMonthHolidays[i] = curMonthHoliday.get(i).getHoliday();
			}
		}
		// 元素为节假日的每一天的日期
		List<Date> curMonthHolidayList = Arrays.asList(curMonthHolidays);
		

		List<Shift> shifts = null;
		//法定节假日
		List<ShiftHoliday> shiftHolidays = null;
		
		//入职日期呢？在上面，根据情况，startWork可能是入职日期
		//离职日期
		Date quitDate = user.getQuitDate();
		
		//为考勤时间段内的每一天，对当前用户进行考勤评估
		if(quitDate!=null&&( !"".equals(quitDate))){
			//对当前用户在这个月的的每一天进行考勤
			dailyAttendance(startStr, Calendars.str(quitDate, Calendars.DATE), user);
		}else{
			//对当前用户在这个月的的每一天进行考勤
			dailyAttendance(startStr, endStr, user);
		}
		
		if(quitDate!=null&&(!"".equals(quitDate))){
			shifts = dao.query(Shift.class, Cnd.where("shift_date", ">=", startWork).and("shift_date", "<", quitDate).and("user_id","=",user.getUserId()));				
			shiftHolidays = dao.query(ShiftHoliday.class, Cnd.where("holiday", ">=", startWork).and("holiday", "<", quitDate));
		}else{
			shifts = dao.query(Shift.class, Cnd.where("shift_date", ">=", startWork).and("shift_date", "<=", endStr).and("user_id","=",user.getUserId()));
			shiftHolidays = dao.query(ShiftHoliday.class, Cnd.where("holiday", ">=", startWork).and("holiday", "<=", endStr));
		}
		
		if (shifts == null || shifts.size() == 0){
			log.error("考勤汇总放弃：排班为空！工号:" + user.getJobNumber());
			return; //如果没有排班，不汇总
		}

		//汇总时间段内的法定假期天数
		Integer holidayDay = 0;
		if(shiftHolidays != null){
			holidayDay = shiftHolidays.size();
		}
		
		boolean isNight = false;
		
		//遍历指定时间段内的排班，对各种数据进行统计
		int workDayCount = 0; //应出勤天数
		int workMinute = 0; // 应出勤分钟数
		for(Shift f:shifts){
			if(f.getClasses()==null){continue;}
			ShiftClass shiftClass = dao.fetch(ShiftClass.class, f.getClasses());
			if (shiftClass == null) {
				log.error("考勤汇总放弃：班次设置为空！工号:" + user.getJobNumber());
				continue;
			}
			
			String checkIn = null;
			String checkOut = null;
			String restIn = null;
			String restOut = null;
			
			//白班
			if(shiftClass.getNight() == ShiftC.DAY_IN){
				//二头班
				if(shiftClass.getSecond()==1){
					checkIn = shiftClass.getFirstMorning();
					restIn = shiftClass.getFirstNight();
					restOut = shiftClass.getSecondMorning();
					checkOut = shiftClass.getSecondNight();
					filldayMapForCheckedRecord(dayMapForCheckedRecord, f, leaveDayMap, restIn, restOut);

				}else{
					//一头班
					checkIn = shiftClass.getFirstMorning();
					checkOut = shiftClass.getFirstNight();
					
					//获取两个时间的中间值,2015-2-6 为任意时间
					DateTime fm = Calendars.parse("2015-2-6 "+checkIn, Calendars.DATE_TIME);
					DateTime fn = Calendars.parse("2015-2-6 "+checkOut, Calendars.DATE_TIME);
					long beginDate = (fn.toDate().getTime()+fm.toDate().getTime())/2;
					SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");  				  
					String middle = sdf.format(new Date(beginDate));
					restIn = middle;
					restOut = middle;
					
					filldayMapForCheckedRecord(dayMapForCheckedRecord, f, leaveDayMap, restIn, restOut);
				}
			//夜班
			}else if(shiftClass.getNight() == ShiftC.NIGHT_IN){
				DateTime nowN = Calendars.parse(f.getShiftDate(), Calendars.DATE).plus(-1);
				Shift shiftN = dao.fetch(Shift.class, Cnd.where("user_id", "=", user.getUserId()).and("shift_date", "=", nowN.toString("yyyy-MM-dd")));
				if(shiftN == null)continue;
				ShiftClass shiftClassN = dao.fetch(ShiftClass.class, shiftN.getClasses());
				if(shiftClassN == null)continue;
				checkIn = shiftClassN.getFirstNight();
				checkOut = shiftClass.getFirstMorning();
				//获取两个时间的中间值,2015-2-6 为任意时间，夜班没有二头班
				DateTime fm = Calendars.parse("2015-2-6 "+checkIn, Calendars.DATE_TIME);
				DateTime fn = Calendars.parse("2015-2-6 "+checkOut, Calendars.DATE_TIME);
				long beginDate = (fn.toDate().getTime()+fm.toDate().getTime())/2;
				SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");  				  
				String middle = sdf.format(new Date(beginDate));
				restIn = middle;
				restOut = middle;
				isNight = true;
			}else{
				continue;
			}
			
			
			//当天工作时长（分钟）
			int workDayMinute = Works.workMinute(checkIn, checkOut, restIn, restOut);
			if(isNight){
				workDayMinute = 1440 - workDayMinute;
			}
			
			WorkDay workDay = new WorkDay();
			workDay.setCheckIn(checkIn);
			workDay.setCheckOut(checkOut);
			workDay.setRestIn(restIn);
			workDay.setRestOut(restOut);
			//key为需要上班的日期，value为这一天需要上下班的时候
			day.put(f.getShiftDate(), workDay);
			
			
			//0)获取当天请假时间
			unpaidLeaveMinute += leave(leaveDayMap, workDayMinute,"无薪事假",f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			sickLeaveMinute += leave(leaveDayMap, workDayMinute, "病假",f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			funeralLeaveMinute += leave(leaveDayMap, workDayMinute, "丧假",f.getShiftDate(), checkIn, restIn, restOut, checkOut);

			maritalLeaveMinute += leave(leaveDayMap, workDayMinute, "婚假",f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			annualLeaveMinute += leave(leaveDayMap, workDayMinute, "年休假",f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			injuryLeaveMinute += leave(leaveDayMap, workDayMinute, "工伤假",f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			deferredLeaveMinute += leave(leaveDayMap, workDayMinute, "加班补休",f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			paternityLeaveMinute += leave(leaveDayMap, workDayMinute, "陪产假",f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			paidLeaveMinute += leave(leaveDayMap, workDayMinute, "有薪事假",f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			// 1) 获取当天外勤的时间
			outworkMinute += outWorksErrand(outworkDayMap, workDayMinute,  f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			// 2) 获取当天出差的时间
			errandMinute += outWorksErrand(errandDayMap, workDayMinute, f.getShiftDate(), checkIn, restIn, restOut, checkOut);
			
			System.out.println(Arrays.binarySearch(curMonthHolidays, f.getShiftDate()));
			if(curMonthHolidays != null && curMonthHolidayList.contains(f.getShiftDate())){
				holidayDay--;
			}
			workMinute += workDayMinute;
			workDayCount ++;
		}
		
		//更新考勤日志的信息
		checkedRecordService.update3(user.getUserId(), modifyId, dayMapForCheckedRecord);
		
		
		// 3) 加班
		int overtimeMinute = overtime(startWork, endStr, overtimes);
		// 4) 旷工, 迟到, 早退, 未打卡
		Map<Integer,Integer> thresholdMap = statistics(thresholdItem, user.getUserId(), startWork, endStr, leaves, outworks, errands, day,isNight,quitDate);
					
		boolean exist = true;
		
		String resultMonth = Calendars.parse(endStr, Calendars.DATE).toString("yyyyMM");
		if(user.getInterim().equals("interim1"))
			resultMonth = Calendars.parse(endStr, Calendars.DATE).toString("yyyyMM")+"-01";
		if(user.getInterim().equals("interim2"))
			resultMonth = Calendars.parse(endStr, Calendars.DATE).toString("yyyyMM")+"-02";
		AttendanceResult att = dao.fetch(AttendanceResult.class, Cnd.where("resultMonth", "=", resultMonth).and("userId", "=", user.getUserId()));
		
		if (att != null) {
			//如果考勤总汇启用之后，不能再修改考勤总汇中的内容
			if (att.getVersion().equals(Status.ENABLED)) return;
		} else {
			att = new AttendanceResult();
			exist = false;
		}

		//标准月应出勤天数(未包含法定假期在内)，体现在standardWorkDaysCri中
		Criteria standardWorkDaysCri = Cnd.cri();
		if(curMonthHolidays != null && curMonthHolidays.length != 0){
			standardWorkDaysCri = Cnd.where("user_id","=",user.getUserId())
			.and("shift_date", ">=", firstAndLastDay[0])
			.and("shift_date", "<=", firstAndLastDay[1])
			.and("shift_date", "not in", curMonthHolidays);
		} else {
			standardWorkDaysCri = Cnd.where("user_id","=",user.getUserId())
					.and("shift_date", ">=", firstAndLastDay[0])
					.and("shift_date", "<=", firstAndLastDay[1]);
		}
		Integer standardWorkDays = dao.count(Shift.class, standardWorkDaysCri);
		
		att.setShouldWorkDay(standardWorkDays + standardHolidayDays);
		att.setShouldWorkDayNH(standardWorkDays);
		//去除map集合转成的字符串的大括号
		String thresholdMapStart = Strings.removeStart(thresholdMap.toString(), "{");
		String thresholdMapEnd = Strings.removeEnd(thresholdMapStart, "}");
		att.setThreshold(thresholdMapEnd);
		att.setResultMonth(resultMonth);
		att.setUserId(user.getUserId());
		att.setStartDate(Calendars.parse(startWork, Calendars.DATE).toDate());
		att.setEndDate(Calendars.parse(endStr, Calendars.DATE).toDate());
		att.setWorkDay(workDayCount+holidayDay);//应出勤天数
		att.setWorkDayNH(workDayCount);//应出勤天数
		att.setWorkMinute(workDayCount==0?0:workMinute/workDayCount);
		att.setUnpaidLeaveMinute(unpaidLeaveMinute);
		att.setSickLeaveMinute(sickLeaveMinute);
		att.setFuneralLeaveMinute(funeralLeaveMinute);
		att.setMaritalLeaveMinute(maritalLeaveMinute);
		att.setAnnualLeaveMinute(annualLeaveMinute);
		att.setInjuryLeaveMinute(injuryLeaveMinute);
		att.setDeferredLeaveMinute(deferredLeaveMinute);
		att.setPaternityLeaveMinute(paternityLeaveMinute);
		att.setPaidLeaveMinute(paidLeaveMinute);
		att.setOutworkMinute(outworkMinute);
		att.setErrandMinute(errandMinute);
		att.setOvertimeMinute(overtimeMinute);
		att.setVersion(Status.DISABLED);
		att.setModifyId(modifyId);
		att.setModifyTime(now.toDate());
		att.setAbsentAmount(thresholdMap.get(-3));
		att.setAbsentAmountTotal((float)(thresholdMap.get(-6) *1.0 / 2));
		att.setLateAmount(thresholdMap.get(-1)+thresholdMap.get(-2));
		att.setForgetAmount(thresholdMap.get(-4));
		//停薪
		Archive archive = dao.fetch(Archive.class,Cnd.where("user_id", "=", user.getUserId()));
		if(archive!=null){
			if(archive.getOnPosition() != null && archive.getOnPosition() ==3){
				att.setShouldWorkDay(0);
				att.setShouldWorkDayNH(0);
				att.setWorkDay(0);
				att.setWorkDayNH(0);
				att.setUnpaidLeaveMinute(0);
				att.setSickLeaveMinute(0);
				att.setFuneralLeaveMinute(0);
				att.setMaritalLeaveMinute(0);
				att.setAnnualLeaveMinute(0);
				att.setInjuryLeaveMinute(0);
				att.setDeferredLeaveMinute(0);
				att.setPaternityLeaveMinute(0);
				att.setPaidLeaveMinute(0);
				att.setOutworkMinute(0);
				att.setErrandMinute(0);
				att.setOvertimeMinute(0);
				att.setModifyTime(now.toDate());
				att.setAbsentAmount(0);
				att.setAbsentAmountTotal((float)0);
				att.setLateAmount(0);
				att.setForgetAmount(0);
			}
		}
		if (exist){
			dao.update(att);
		} else {
			dao.insert(att);
		}
	}
	
	//根据leaveDayMap填充dayMapForCheckedRecord中的内容
	private void filldayMapForCheckedRecord(Map<String, String[]> dayMapForCheckedRecord, Shift f, Map<String, String[]> leaveDayMap
			,String restIn , String restOut){
		List<String> keys = isLeave(leaveDayMap, f.getShiftDate());
		if(keys != null){
			for(String key : keys){
				String[] checked = leaveDayMap.get(key);
				String in = checked[0];
				String out = checked[1];
				if(in == null && out == null){
					putDayMapForCheckedRecord(dayMapForCheckedRecord, Calendars.str(f.getShiftDate(),Calendars.DATE), new String[] {checked[2]+"（"+checked[3]+"）",checked[2]+"（"+checked[3]+"）"});
				}else if(in != null && out != null){
					if(out.compareTo(restIn)<=0){
						putDayMapForCheckedRecord(dayMapForCheckedRecord, Calendars.str(f.getShiftDate(),Calendars.DATE), new String[] {checked[2]+"（"+checked[3]+"）",null});
					}else if(out.compareTo(restOut) > 0 && in.compareTo(restIn) < 0){
						putDayMapForCheckedRecord(dayMapForCheckedRecord, Calendars.str(f.getShiftDate(),Calendars.DATE), new String[] {checked[2]+"（"+checked[3]+"）",checked[2]+"（"+checked[3]+"）"});
					}if(out.compareTo(restOut) > 0 && in.compareTo(restOut) >= 0){
						putDayMapForCheckedRecord(dayMapForCheckedRecord, Calendars.str(f.getShiftDate(),Calendars.DATE), new String[] {null,checked[2]+"（"+checked[3]+"）"});
					}
				}else if(in == null && out != null){
					if(out.compareTo(restIn)<=0){
						putDayMapForCheckedRecord(dayMapForCheckedRecord, Calendars.str(f.getShiftDate(),Calendars.DATE), new String[] {checked[2]+"（"+checked[3]+"）",null});
					}else{
						putDayMapForCheckedRecord(dayMapForCheckedRecord, Calendars.str(f.getShiftDate(),Calendars.DATE), new String[] {checked[2]+"（"+checked[3]+"）",checked[2]+"（"+checked[3]+"）"});
					}
				}else if(in != null && out == null){
					if(in.compareTo(restOut) > 0){
						putDayMapForCheckedRecord(dayMapForCheckedRecord, Calendars.str(f.getShiftDate(),Calendars.DATE), new String[] {null,checked[2]+"（"+checked[3]+"）"});
					}else{
						putDayMapForCheckedRecord(dayMapForCheckedRecord, Calendars.str(f.getShiftDate(),Calendars.DATE), new String[] {checked[2]+"（"+checked[3]+"）",checked[2]+"（"+checked[3]+"）"});
					}
				}
			}
		}
	}
	
	//判断当天是否存在请假,换言之：从leaveDayMap中搜索，返回的是指定日期的key
	private List<String> isLeave(Map<String, String[]> leaveDayMap, Date date){
		List<String> keys = new ArrayList<String>();
		String dateStr = Calendars.str(date, Calendars.DATE);
		for(Entry<String, String[]> entry : leaveDayMap.entrySet()){
			if(entry.getKey().contains(dateStr)){
				keys.add(entry.getKey());
			}
		}
		if(keys.size() > 0){
			return keys;
		}else{
			return null;
		}
	}
	
	private boolean putDayMapForCheckedRecord(Map<String, String[]> dayMapForCheckedRecord, String key, String[] value){
		if(dayMapForCheckedRecord.get(key) == null){
			dayMapForCheckedRecord.put(key, value);
			return true;
		}else{
			String[] v = dayMapForCheckedRecord.get(key);
			if(v[0] != null && value[0]!= null){
				v[0] = value[0] + v[0];
			}else if(v[1] != null && value[1] != null){
				v[1] = value[1] + v[1];
			}else if(v[0] == null && value[0]!= null){
				v[0] = value[0];
			}else if(v[1] == null && value[1]!= null){
				v[1] = value[1];
			}
			return true;
		}
	}
	
	//为考勤时间段内的每一天，对当前用户进行考勤评估
	public void dailyAttendance(String startStr, String endStr, User user){
		
		CheckedRecordJob recordJob = new CheckedRecordJob();

		DateTime startTime = Calendars.parse(startStr, Calendars.DATE);
		DateTime endTime = Calendars.parse(endStr, Calendars.DATE);
		DateTime checkTime = startTime;
		while(!checkTime.isAfter(endTime)){
			// 3) 按用户指定的时间所在的月份新建考勤表
			checkedRecordService.createTable(dao, checkTime);
			// 注意：要和groupoa工程的填充考勤表、考勤同步方法一致!!!!!!!!
			// 4) 填充考勤表
//			recordJob.newFill(checkTime, dao, mapper);
			// 5) 考勤同步
//			recordJob.newNync(checkTime, dao, mapper);
			//对某个用户的某一天的上班情况进行初步的考勤
			recordJob.syncAttendance(dao, mapper, checkTime, user);

			startTime = startTime.plusDays(1);
			checkTime = startTime;
		}
	}
	
	/**
	 * 统计应出勤天数
	 * @param startWork 起始日期
	 * @param endStr 终止日期
	 * @param userId 用户
	 * @return 【应出勤天数，应出勤分钟数】
	 */
	/*private Integer[] workDay(String startWork,String endStr,Integer userId){
		int count = 0; //应出勤天数
		int workMinute = 0; // 应出勤分钟数
		boolean isNight = false;
		List<Shift> shifts = dao.query(Shift.class, Cnd.where("shift_date", ">=", startWork).and("shift_date", "<=", endStr).and("user_id","=",userId));
		for(Shift f:shifts){
			if(f.getClasses()==null){continue;}
			ShiftClass shiftClass = dao.fetch(ShiftClass.class, f.getClasses());
			if(shiftClass == null){continue;}
			String checkIn = null;
			String checkOut = null;
			String restIn = null;
			String restOut = null;	
			
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
				//获取两个时间的中间值,2015-2-6 为任意时间
				DateTime fm = Calendars.parse("2015-2-6 "+checkIn, Calendars.DATE_TIME);
				DateTime fn = Calendars.parse("2015-2-6 "+checkOut, Calendars.DATE_TIME);
				long beginDate = (fn.toDate().getTime()+fm.toDate().getTime())/2;
				SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");  				  
				String middle = sdf.format(new Date(beginDate));
				restIn = middle;
				restOut = middle;
			}
			//夜班
			}else if(shiftClass.getNight() == ShiftC.NIGHT_IN){
				DateTime nowN = Calendars.parse(f.getShiftDate(), Calendars.DATE).plus(-1);
				Shift shiftN = dao.fetch(Shift.class, Cnd.where("user_id", "=", userId).and("shift_date", "=", nowN.toString("yyyy-MM-dd")));
				if(shiftN == null)continue;
				ShiftClass shiftClassN = dao.fetch(ShiftClass.class, shiftN.getClasses());
				if(shiftClassN == null)continue;
				checkIn = shiftClassN.getFirstNight();
				checkOut = shiftClass.getFirstMorning();
				//获取两个时间的中间值,2015-2-6 为任意时间
				DateTime fm = Calendars.parse("2015-2-6 "+checkIn, Calendars.DATE_TIME);
				DateTime fn = Calendars.parse("2015-2-6 "+checkOut, Calendars.DATE_TIME);
				long beginDate = (fn.toDate().getTime()+fm.toDate().getTime())/2;
				SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");  				  
				String middle = sdf.format(new Date(beginDate));
				restIn = middle;
				restOut = middle;
				isNight = true;
			}else{
				continue;
			}
			// 统计当天的应出勤分钟数
			int workDayMinute = Works.workMinute(checkIn, checkOut, restIn, restOut);
			if(isNight){
				workDayMinute = 1440-workDayMinute;
			}
			workMinute += workDayMinute;
			count ++;
		}
		return new Integer[]{count,workMinute};
	}*/
	
	//获取当天出差,外勤时间
	private int outWorksErrand(Map<String, String[]> map, int works,Date date,String checkIn,String restIn,String restOut,String checkOut){			
		int minute = 0;
		String in = null;
		String out = null;
		String dateStr = Calendars.str(date, Calendars.DATE);
		if (map.containsKey(dateStr)) {				
			String[] arr = map.get(dateStr);						
			in = Strings.isBlank(arr[0]) ? checkIn : arr[0];
			out = Strings.isBlank(arr[1]) ? checkOut : arr[1];
		}
		if (!Strings.isBlank(in) && !Strings.isBlank(out)) {
			minute = Works.getMinute(dateStr + " " + in, dateStr + " " + out, checkIn, checkOut, restIn, restOut, works);
		}					
		return minute;
	}
	//获取当天请假时间
	private int leave(Map<String, String[]> leaveDayMap, int works, String typeName,Date date,String checkIn,String restIn,String restOut,String checkOut){			
		int minute = 0;
		String in = null;
		String out = null;
		String dateStr = Calendars.str(date, Calendars.DATE);
		if (leaveDayMap.containsKey(dateStr + typeName)) {				
			String[] arr = leaveDayMap.get(dateStr + typeName);
			if(!(arr[2].equals(typeName))){return minute;}
			in = Strings.isBlank(arr[0]) ? checkIn : arr[0];
			out = Strings.isBlank(arr[1]) ? checkOut : arr[1];
		}
		if (!Strings.isBlank(in) && !Strings.isBlank(out)) {
			minute = Works.getMinute(dateStr + " " + in, dateStr + " " + out, checkIn, checkOut, restIn, restOut, works);
		}			
		return minute;
	}

	/**
	 *  汇总定版,出工资条
	 * @param user
	 * @param releaseId
	 * @param measureMap 绩效评分标准
	 * @param thresholdItem
	 * @param leaveType
	 * @param standardWorkDay // 标准工作日,由排班决定
	 * @param startStr
	 * @param endStr
	 * @param modifyId
	 * @param now
	 * @param att
	 */
	public void newWage(User user, Integer releaseId, Map<Float, Float> measureMap, List<AttendanceThresholdItem> thresholdItem, List<SalaryRuleItem> salaryRuleItems,
			List<ConfLeaveType> leaveType, String startStr, String endStr, Integer modifyId, DateTime now, AttendanceResult att) {

		if (att.getVersion().equals(Status.DISABLED)) {
			log.error("出工资条失败：已经出过工资条，工号：" + user.getJobNumber());
			return;
		}
		
		Wage wage = dao.fetch(Wage.class, user.getUserId());
		Wage hwage = new Wage();
		
		if (wage == null) {
			log.error("出工资条失败：未设置工资，工号：" + user.getJobNumber());
			return;
		}
		
		DateTime effectTime = Calendars.parse(wage.getEffectTime(), Calendars.DATE);
		DateTime startTime = Calendars.parseDateTime(startStr, Calendars.DATE_TIME);
		DateTime endTime = Calendars.parseDateTime(endStr, Calendars.DATE_TIME);
		
		int postSalary = 0; // 岗位工资
		int standardSalary = 0; // 基本工资
		int rewardSalary = 0;
		int serviceAward = 0;
		int communicationAllowance = 0;
		int oilAllowance = 0;
		int mealAllowance = 0;
		int overtimeAllowance = 0;
		int maternityInsurance = 0;
		int housingSubsidies = 0;
		int tax = 0;
		int socialSecurity = 0;
		int socialSecurityDeduction = 0;
		int heatingAllowance = 0;
		Double subsidies = 0.0;
		
		boolean historyOrNot = false;
		//生效时间在考勤开始时间之前 则使用当前工资
		if(effectTime.isBefore(startTime)||effectTime.isEqual(startTime)){
			
			postSalary = wage.getPostSalary();
			standardSalary = wage.getStandardSalary();
			rewardSalary = wage.getRewardSalary();
			serviceAward = wage.getServiceAward();
			communicationAllowance = wage.getCommunicationAllowance();
			oilAllowance = wage.getOilAllowance();
			mealAllowance = wage.getMealAllowance();
			overtimeAllowance = wage.getOvertimeAllowance();
			maternityInsurance = wage.getMaternityInsurance();
			housingSubsidies = wage.getHousingSubsidies();
			tax = wage.getTax();
			socialSecurity = wage.getSocialSecurity();
			socialSecurityDeduction = wage.getSocialSecurityDeduction();
			heatingAllowance = wage.getHeatingAllowance();
			if(wage.getSubsidies() != null){
				subsidies = wage.getSubsidies();
			}
		}//生效时间在考勤时间之后,则使用过渡工资
		else if(effectTime.isAfter(endTime)){
			historyOrNot = true;
			List<HistoryWage> historyWage = dao.query(HistoryWage.class, Cnd.where("userId", "=", user.getUserId()).and("status", "=", Status.INTERIM));
			if(historyWage==null)return;
			for(HistoryWage history : historyWage){
				
				postSalary = history.getPostSalary();
				standardSalary = history.getStandardSalary();
				rewardSalary = history.getRewardSalary();
				serviceAward = history.getServiceAward();
				communicationAllowance = history.getCommunicationAllowance();
				oilAllowance = history.getOilAllowance();
				mealAllowance = history.getMealAllowance();
				overtimeAllowance = history.getOvertimeAllowance();
				maternityInsurance = history.getMaternityInsurance();
				housingSubsidies = history.getHousingSubsidies();
				tax = history.getTax();
				socialSecurity = history.getSocialSecurity();
				socialSecurityDeduction = history.getSocialSecurityDeduction();
				heatingAllowance = history.getHeatingAllowance();
				
				//为需要进行计算的分数注入到hwage对象中
				hwage.setMealAllowance(mealAllowance);
				hwage.setHousingSubsidies(housingSubsidies);
				hwage.setCommunicationAllowance(communicationAllowance);
				hwage.setOilAllowance(oilAllowance);
				hwage.setOvertimeAllowance(overtimeAllowance);
				hwage.setHeatingAllowance(heatingAllowance);
				hwage.setStandardSalary(standardSalary);
				hwage.setPostSalary(postSalary);
				hwage.setPerformSalary(wage.getPerformSalary());
				hwage.setRewardSalary(rewardSalary);
				hwage.setServiceAward(serviceAward);
				hwage.setMaternityInsurance(maternityInsurance);
				hwage.setSocialSecurityDeduction(socialSecurityDeduction);
				hwage.setTax(tax);
				
				if(att.getResultMonth().length()!=6){
					
					rewardSalary = 0;
					serviceAward = 0;
					communicationAllowance = 0;
					oilAllowance = 0;
					mealAllowance = 0;
					maternityInsurance = 0;
					housingSubsidies = 0;
					tax = 0;
					socialSecurity = 0;
					socialSecurityDeduction = 0;
					heatingAllowance = 0;
					
					//为需要进行计算的分数注入到hwage对象中
					hwage.setMealAllowance(mealAllowance);
					hwage.setHousingSubsidies(housingSubsidies);
					hwage.setCommunicationAllowance(communicationAllowance);
					hwage.setOilAllowance(oilAllowance);
					hwage.setHeatingAllowance(heatingAllowance);
					hwage.setRewardSalary(rewardSalary);
					hwage.setServiceAward(serviceAward);
					hwage.setMaternityInsurance(maternityInsurance);
					hwage.setSocialSecurityDeduction(socialSecurityDeduction);
					hwage.setTax(tax);
				}
				break;
			}
		} else {
			return;
		}

		
//		Integer standardWorkDay = att.getWorkDay();

		
		Map<Integer, ConfLeaveType> typeMap = new HashMap<Integer, ConfLeaveType>();
		for(ConfLeaveType type :  leaveType){
			System.out.println(type.getLeaveType());
			typeMap.put(type.getLeaveType(), type);
		}
		
		
		// 6) 加班,新的规定：加班不计工资，可累计供加班补休
	//	int overtimeAllowance = (int) (att.getOvertimeMinute() * wage.getOvertimeAllowance() / 60.d);
		
		boolean exist = true;
		WageResult wrs = dao.fetch(WageResult.class, Cnd.where("resultMonth", "=", att.getResultMonth()).and("userId", "=", user.getUserId()));
		if (wrs == null) {
			wrs = new WageResult();
			exist = false;
		}
		
		//对实际工资计算计算，并将过程中的各项计算结果存入wrs中,但由于每个公司的计算规则不同，因此注意初始化wrs中的一些属性。
		Double realSalary = null;
		for(SalaryRuleItem salaryRuleItem : salaryRuleItems){
			if(salaryRuleItem.getName().equals("税后工资")){
				if(historyOrNot){
					realSalary = salary(user, salaryRuleItem, salaryRuleItems, att, wrs, hwage, thresholdItem, typeMap, releaseId, measureMap);
				}else{
					realSalary = salary(user, salaryRuleItem, salaryRuleItems, att, wrs, wage, thresholdItem, typeMap, releaseId, measureMap);
				}
				break;
			}
		}
		if(realSalary != null){
			wrs.setRealSalary(realSalary);
		}else{
			Asserts.isNull(realSalary, "税后工资计算失败");
		}
		
		wrs.setResultMonth(att.getResultMonth());
		wrs.setUserId(user.getUserId());
		wrs.setStandardSalary(Double.valueOf(standardSalary));
		wrs.setPostSalary(Double.valueOf(postSalary));
		wrs.setRewardSalary(Double.valueOf(rewardSalary));
		wrs.setServiceAward(Double.valueOf(serviceAward));
		wrs.setCommunicationAllowance(Double.valueOf(communicationAllowance));
		wrs.setOilAllowance(Double.valueOf(oilAllowance));
		wrs.setHeatingAllowance(Double.valueOf(heatingAllowance));
		wrs.setSubsidies(subsidies);
		wrs.setOvertimeAllowance(Double.valueOf(overtimeAllowance));
		wrs.setMaternityInsurance(Double.valueOf(maternityInsurance));
		wrs.setHousingSubsidies(Double.valueOf(housingSubsidies));
		if(wrs.getTax() == null)wrs.setTax(0.0);
		wrs.setSocialSecurity(Double.valueOf(socialSecurity));
		if(wrs.getTax() == null)wrs.setTax(0.0);
		wrs.setModifyId(modifyId);
		wrs.setModifyTime(now.toDate());
		Archive archive = dao.fetch(Archive.class,Cnd.where("user_id", "=", user.getUserId()));
		if(archive!=null){
			//3为在职状态为离薪
			if(archive.getOnPosition()!= null && archive.getOnPosition()==3){
				wrs.setStandardSalary(0.00);
				wrs.setPostSalary(0.00);
				wrs.setPerformSalary(0.00);
				wrs.setRewardSalary(0.00);
				wrs.setServiceAward(0.00);
				wrs.setCommunicationAllowance(0.00);
				wrs.setOilAllowance(0.00);
				wrs.setHeatingAllowance(0.00);
				wrs.setMealAllowance(0.00);
				wrs.setOvertimeAllowance(0.00);
				wrs.setMaternityInsurance(0.00);
				wrs.setHousingSubsidies(0.00);
				wrs.setLateDeduction(0.00);
				wrs.setAbsentDeduction(0.00);
				wrs.setForgetDeduction(0.00);
				wrs.setLeaveDeduction(0.00);
				wrs.setSickDeduction(0.00);
				wrs.setTax(0.00);
				wrs.setSocialSecurity(0.00);
				wrs.setSocialSecurityDeduction(0.00);
				wrs.setAccumulateDeduction(0.00);
				wrs.setRealSalary(0.00);
			}
		}


		if (exist)
			dao.update(wrs);
		else
			dao.insert(wrs);
	}
	
	
	private Double salary(User user, SalaryRuleItem salaryRuleItem, List<SalaryRuleItem> salaryRuleItems,  AttendanceResult att, WageResult wrs, Wage wage, List<AttendanceThresholdItem> thresholdItem,
			Map<Integer, ConfLeaveType> typeMap, Integer releaseId, Map<Float, Float> measureMap){
		if("税后工资".equals(salaryRuleItem.getName())){
			List<String> itemsStrs = Rule.getChinese(salaryRuleItem.getRule());
			Rule rule = new Rule(salaryRuleItem.getRule(), itemsStrs);
			Double salary = null;
			for(String itemsStr : itemsStrs){
				if("实际工资".equals(itemsStr)){
					if(salary == null){
						for(SalaryRuleItem item : salaryRuleItems){
							if(itemsStr.equals(item.getName())){
								salary = salary(user, item, salaryRuleItems, att, wrs, wage, thresholdItem, typeMap, releaseId, measureMap);
								rule.assign(itemsStr, salary.toString());
								break;
							}
						}
						Asserts.isNull(salary, "税前工资计算失败");
						wrs.setGrossPay(salary);
					}
				}else if(itemsStr.equals("税收")){
					if(wage.getTax() > 0){
						//先计算当月的税前工资
						if(salary == null){
							for(SalaryRuleItem item : salaryRuleItems){
								if("实际工资".equals(item.getName())){
									salary = salary(user, item, salaryRuleItems, att, wrs, wage, thresholdItem, typeMap, releaseId, measureMap);
									rule.assign("实际工资", salary.toString());
									break;
								}
							}
							Asserts.isNull(salary, "税前工资计算失败");
						}
						
						//获取上个月的税前工资
						DateTime now = new DateTime().plusMonths(-1);
						String resultMonth = Calendars.str(now, "yyyyMM");
						WageResult wageResult = dao.fetch(WageResult.class, Cnd.where("userId", "=", user.getUserId()).and("resultMonth", "=", resultMonth));
						if(wageResult != null){
							salary = wageResult.getGrossPay();
						}
						
						TaxRule tr = dao.fetch(TaxRule.class, Cnd.where("corpId", "=", user.getCorpId()).and("status", "=", Status.ENABLED));
						Asserts.isNull(tr, "税收规则未配置");
						List<TaxRuleItem> tris = dao.query(TaxRuleItem.class, Cnd.where("taxRuleId", "=", tr.getId()).and("status", "=", Status.ENABLED));
						Double taxTotal = 0.0;
						boolean flag = true;
						for(TaxRuleItem tri  : tris){
							if("税收金额".equals(tri.getName())){
								flag = false;
								taxTotal = tax(tris, tri, salary/100.0 ) * 100;
								break;
							}
						}
						if(flag){
							throw new RuntimeException("规则中不存在在描述税收金额的公式");
						}

						rule.assign(itemsStr, taxTotal.toString());
						wrs.setTax(taxTotal);
//						rule.assign(itemsStr, wage.getTax().toString());
				
						
					}else{
						rule.assign(itemsStr, "0");
						wrs.setTax(0.0);
					}

				}
			}
			return Double.parseDouble(String.valueOf(rule.calculate()));

		}else if("实际工资".equals(salaryRuleItem.getName())){
			List<String> itemsStrs = Rule.getChinese(salaryRuleItem.getRule());
			Rule rule = new Rule(salaryRuleItem.getRule(), itemsStrs);
			for(String itemsStr : itemsStrs){
				if(itemsStr.equals("应得工资")){
					for(SalaryRuleItem item : salaryRuleItems){
						if(itemsStr.equals(item.getName())){
							Double value = salary(user, item, salaryRuleItems, att, wrs, wage, thresholdItem, typeMap, releaseId, measureMap);
							if(value == null){return null;}
							rule.assign(itemsStr, value.toString());
						}
					}
				}else if("津贴".equals(itemsStr)){
					Double mealAllowance = null;
					if(att.getWorkDay()!=0){
						//请假时间
						long leaveMinute = att.getUnpaidLeaveMinute()+att.getSickLeaveMinute()+att.getFuneralLeaveMinute()+att.getMaritalLeaveMinute()+att.getAnnualLeaveMinute()+att.getInjuryLeaveMinute()+att.getDeferredLeaveMinute()+att.getPaternityLeaveMinute()+att.getPaidLeaveMinute();
						
						long haveWork = att.getWorkDay()*att.getWorkMinute()-(att.getAbsentAmount()*att.getWorkMinute()+leaveMinute);
						//实际出勤的天数
						long haveDay = haveWork*100/att.getWorkMinute();
						System.out.println("实际出勤的天数"+haveDay);
						//计算伙食津贴，按照实际出勤的天数/标准出勤天数的百分比计算，换言之假期本身是没有伙食津贴
						mealAllowance = Double.valueOf(wage.getMealAllowance()*haveDay/att.getShouldWorkDay()/100);	

					}else{
						mealAllowance = 0.00;
					}
					if(mealAllowance == null){throw new RuntimeException("伙食津贴计算失败");}
					wrs.setMealAllowance(mealAllowance);
					rule.assign(itemsStr, (wage.getHousingSubsidies()+wage.getCommunicationAllowance()+wage.getOilAllowance()+wage.getOvertimeAllowance()+ mealAllowance +wage.getHeatingAllowance())+"");
				}else if("扣除".equals(itemsStr)){
					for(SalaryRuleItem item : salaryRuleItems){
						if(itemsStr.equals(item.getName())){
							Double value = salary(user, item, salaryRuleItems, att, wrs, wage, thresholdItem, typeMap, releaseId, measureMap);
							if(value == null){return null;}
							rule.assign(itemsStr, value.toString());
						}
					}
//					rule.assign(itemsStr, (wrs.getLateDeduction()+wrs.getForgetDeduction()+wrs.getAbsentDeduction()+wage.getMaternityInsurance()+wage.getTax()+wage.getSocialSecurityDeduction()+wrs.getLeaveDeduction())+wrs.getAccumulateDeduction()+"");
				}
			}
			return Double.parseDouble(String.valueOf(rule.calculate()));
		}else if("应得工资".equals(salaryRuleItem.getName())){
			List<String> itemsStrs = Rule.getChinese(salaryRuleItem.getRule());
			Rule rule = new Rule(salaryRuleItem.getRule(), itemsStrs);
			for(String itemsStr : itemsStrs){
				if(itemsStr.equals("标准工资")){
					for(SalaryRuleItem item : salaryRuleItems){
						if(itemsStr.equals(item.getName())){
							Double value = salary(user, item, salaryRuleItems, att, wrs, wage, thresholdItem, typeMap, releaseId, measureMap);
							if(value == null){return null;}
							rule.assign(itemsStr, value.toString());
						}
					}
				
				}else if(itemsStr.equals("标准工作天数")){
					rule.assign(itemsStr, att.getShouldWorkDayNH().toString());
				}else if(itemsStr.equals("应该工作天数")){
					rule.assign(itemsStr, att.getWorkDayNH().toString());
				}
			}
			return Double.parseDouble(String.valueOf(rule.calculate()));
		}else if("标准工资".equals(salaryRuleItem.getName())){
			List<String> itemsStrs = Rule.getChinese(salaryRuleItem.getRule());
			Rule rule = new Rule(salaryRuleItem.getRule(), itemsStrs);
			for(String itemsStr : itemsStrs){
				 if("基本工资".equals(itemsStr)){
					 rule.assign(itemsStr, wage.getStandardSalary().toString());
				 }else if("岗位工资".equals(itemsStr)){
					 rule.assign(itemsStr, wage.getPostSalary().toString());
				 }else if("绩效".equals(itemsStr)){
					Double performSalary = 0.0;
						// 5) 绩效
					if (releaseId != null) {
						// 绩效目标，查询到某个用户在某次绩效考核中的各项成绩
						List<Target> targets = mapper.query(
								Target.class,
								"Target.query",
								Cnd.where("p.user_id", "=", att.getUserId())
										.and("p.release_id", "=", releaseId)
										.and("p.approved", "=", Status.OK)
										.and("p.status", "=", Status.ENABLED));
						if (!Asserts.isEmpty(targets)) {
							double scored = 0; // 绩效总分
							for (Target target : targets) {
								double partScoreA = 0;
								if(target.getFirstStep() != null){
									partScoreA = target.getFirstStep() * target.getScore() / 100;
								}else{
									partScoreA = target.getScore();
								}
								
								double partScoreB = 0;
								if(target.getSecondStep() != null){
									partScoreB = target.getSecondStep() * target.getManscore() / 100;
								}
								
								scored += target.getWeight()
										* (partScoreA + partScoreB) / 100;

							}
							// 前提是measureMap已经有序
							for (Entry<Float, Float> entry : measureMap
									.entrySet()) {
								float score = entry.getKey();
								float coefficient = entry.getValue();
								if (scored >= score) {
									performSalary = Double.valueOf((Values
											.getInt(wage.getPerformSalary(),
													Value.I) * coefficient));
									break;
								}
							}
						}
					}
					wrs.setPerformSalary(performSalary);
					rule.assign(itemsStr, performSalary.toString());
//					rule.assign(itemsStr, wrs.getPerformSalary().toString());
				 }else if("奖金".equals(itemsStr)){
					 rule.assign(itemsStr, wage.getRewardSalary().toString());
				 }else if("工龄奖".equals(itemsStr)){
					 rule.assign(itemsStr, wage.getServiceAward().toString());
				 }
				
			}
			return Double.parseDouble(String.valueOf(rule.calculate()));
		}else if("扣除".equals(salaryRuleItem.getName())){
			List<String> itemsStrs = Rule.getChinese(salaryRuleItem.getRule());
			Rule rule = new Rule(salaryRuleItem.getRule(), itemsStrs);
			for(String itemsStr : itemsStrs){
				if(itemsStr.equals("生育保险")){
					rule.assign(itemsStr, wage.getMaternityInsurance().toString());
				}else if(itemsStr.equals("社保")){
					if(wage.getSocialSecurityDeduction() != 0){
						SocialSecurityRule ssr = dao.fetch(SocialSecurityRule.class, Cnd.where("corpId", "=", user.getCorpId()).and("status", "=", Status.ENABLED));
						Asserts.isNull(ssr, "社保规则未配置");
						List<SocialSecurityRuleItem> ssris = dao.query(SocialSecurityRuleItem.class, Cnd.where("socialSecurityRuleId", "=", ssr.getId()).and("status", "=", Status.ENABLED));
						Double socialSecurityTotal = 0.0;
						boolean flag = true;
						for(SocialSecurityRuleItem ssri  : ssris){
							if("社保金额".equals(ssri.getName())){
								flag = false;
								socialSecurityTotal = socialSecurity(ssris, ssri, wage.getSocialSecurityDeduction()/100) * 100;
								break;
							}
							if(flag){
								throw new RuntimeException("规则中不存在存在在描述社保金额的公式");
							}
						}
						rule.assign(itemsStr, socialSecurityTotal.toString());
						wrs.setSocialSecurityDeduction(socialSecurityTotal);
					}else{
						rule.assign(itemsStr, "0");
						wrs.setSocialSecurityDeduction(0.0);
					}
//					rule.assign(itemsStr, wage.getSocialSecurityDeduction().toString());
				}else if(itemsStr.equals("请假扣除")){
//					rule.assign(itemsStr, wrs.getLeaveDeduction().toString());
					
					int salary = wage.getStandardSalary() + wage.getPostSalary();
					int standardsalary = wage.getStandardSalary();
					Integer standardWorkDay = att.getShouldWorkDayNH();
					int workMinute = att.getWorkMinute();
					
					Double salaryPerDay = Double.valueOf(salary) / Double.valueOf(standardWorkDay);	 //全额日工资 , 工作日根据月排班和周排班计算
					Double salaryPerMinute = salaryPerDay / workMinute;  //全额分钟工资
					Double standardsalaryPerDay = Double.valueOf(standardsalary) / Double.valueOf(standardWorkDay);	 //基本日工资 , 工作日根据月排班和周排班计算
					Double standardsalaryMinute = standardsalaryPerDay / workMinute; //基本分钟工资
					
					Double leaveDeduction = 0.0;
					Double sickDeduction = 0.0;
					leaveDeduction += salaryModel(typeMap, Value.ONE, att.getUnpaidLeaveMinute(), workMinute , salaryPerMinute , standardsalaryMinute); //无薪事假
					sickDeduction = salaryModel(typeMap, Value.TWO, att.getSickLeaveMinute(), workMinute , salaryPerMinute , standardsalaryMinute);	//病假
					leaveDeduction += sickDeduction;
					leaveDeduction += salaryModel(typeMap, Value.THREE, att.getFuneralLeaveMinute(), workMinute , salaryPerMinute , standardsalaryMinute);//丧假
					leaveDeduction += salaryModel(typeMap, Value.FOUR, att.getMaritalLeaveMinute(), workMinute , salaryPerMinute , standardsalaryMinute);//婚假
				//	salaryModel(leaveDeduction, typeMap, Value.FIVE, att.getUnpaidLeaveUnit(), salaryhalfDay, StandardsalaryhalfDay); //年休假
					leaveDeduction += salaryModel(typeMap, Value.SIX, att.getInjuryLeaveMinute(), workMinute , salaryPerMinute , standardsalaryMinute);//工伤假
				//	salaryModel(leaveDeduction, typeMap, Value.SEVEN, att.getDeferredLeaveUnit(), salaryhalfDay, StandardsalaryhalfDay); //加班补休
					leaveDeduction += salaryModel(typeMap, Value.EIGHT, att.getPaternityLeaveMinute(), workMinute , salaryPerMinute , standardsalaryMinute); //陪产假
					leaveDeduction += salaryModel(typeMap, Value.NINE, att.getPaidLeaveMinute(), workMinute , salaryPerMinute , standardsalaryMinute); //有薪事假
					
					wrs.setLeaveDeduction(leaveDeduction);
					wrs.setSickDeduction(sickDeduction);
					System.out.println("请假扣除金额："+leaveDeduction);
					rule.assign(itemsStr, leaveDeduction.toString());
				
				}else if(itemsStr.equals("考勤扣除")){
//					rule.assign(itemsStr, (wrs.getLateDeduction()+wrs.getForgetDeduction()+wrs.getAbsentDeduction()+wrs.getAccumulateDeduction())+"");
					int salary = wage.getStandardSalary() + wage.getPostSalary();
					int standardsalary = wage.getStandardSalary();
					Integer standardWorkDay = att.getShouldWorkDayNH();
					
					Double salaryPerDay = Double.valueOf(salary) / Double.valueOf(standardWorkDay);	 //全额日工资 , 工作日根据月排班和周排班计算
					Double standardsalaryPerDay = Double.valueOf(standardsalary) / Double.valueOf(standardWorkDay);	 //基本日工资 , 工作日根据月排班和周排班计算

					
					Map<String,String> threshold = Converts.map(att.getThreshold());
					Double absentDeduction = 0.0;
					Double forgetDeduction = 0.0;
					Double lateDeduction = 0.0;
					Double accumulateDeduction = 0.0;
					// 2) 迟到, 早退, 旷工
					for(AttendanceThresholdItem item : thresholdItem){
						//Status.A跟absentDeduction是匹配的
						if(item.getType().equals(Status.A)){
							//将该用户符合考勤阀值项的数量，转换成扣除的金额
							if(threshold.get(item.getItemId()+"")!=null){
								String a = threshold.get(item.getItemId()+"");
								Integer amount = Integer.parseInt(a);
								absentDeduction = absentDeduction + deduction(item,amount,salaryPerDay,standardsalaryPerDay, 0);
							}
						}else if(item.getType().equals(Status.F)){
							if(threshold.get(item.getItemId()+"")!=null){
								String a = threshold.get(item.getItemId()+"");
								Integer amount = Integer.parseInt(a);
								forgetDeduction = forgetDeduction + deduction(item,amount,salaryPerDay,standardsalaryPerDay, 0);
							}
						}else if(item.getType().equals(Status.L)){
							if(threshold.get(item.getItemId()+"")!=null){
								String a = threshold.get(item.getItemId()+"");
								Integer amount = Integer.parseInt(a);
								lateDeduction = lateDeduction + deduction(item,amount,salaryPerDay,standardsalaryPerDay, 0);
		
							}
							//根据map集合中表示累计的-5进行扣除
						} else if(item.getType().equals(Status.ACCUMULATE)){
							if(threshold.get("-5")!=null){
								String a = threshold.get("-5");
								Integer amount = Integer.parseInt(a);
								accumulateDeduction += deduction(item,amount,salaryPerDay,standardsalaryPerDay, 0);

							}
						}
							
					}
					wrs.setLateDeduction(RMB.toMinute(lateDeduction));
					wrs.setAbsentDeduction(RMB.toMinute(absentDeduction));
					wrs.setForgetDeduction(RMB.toMinute(forgetDeduction));
					wrs.setAccumulateDeduction(RMB.toMinute(accumulateDeduction));
					rule.assign(itemsStr, (RMB.toMinute(lateDeduction) + RMB.toMinute(forgetDeduction) + RMB.toMinute(absentDeduction) + RMB.toMinute(accumulateDeduction))+"");
				}
			}
			return Double.parseDouble(String.valueOf(rule.calculate()));
		}
		return null;
		
	}
//-------------------------------------------------------------------------------------------------
		
	// 汇总定版,出工资条
	public void wage(User user, Integer releaseId, Map<Float, Float> measureMap, List<AttendanceThresholdItem> thresholdItem,List<ConfLeaveType> leaveType, Integer standardWorkDay, String startStr, String endStr, Integer modifyId, DateTime now,AttendanceResult att) {
		
		if (att.getVersion().equals(Status.DISABLED)) return;
		
		Wage wage = dao.fetch(Wage.class, user.getUserId());
		if (wage == null) return;
		
		DateTime effectTime = Calendars.parse(wage.getEffectTime(), Calendars.DATE);
		DateTime startTime = Calendars.parseDateTime(startStr, Calendars.DATE_TIME);
		DateTime endTime = Calendars.parseDateTime(endStr, Calendars.DATE_TIME);
	
		int postSalary = 0;
		int standardSalary = 0;
		int rewardSalary = 0;
		int serviceAward = 0;
		int communicationAllowance = 0;
		int oilAllowance = 0;
		int mealAllowance = 0;
		int overtimeAllowance = 0;
		int maternityInsurance = 0;
		int housingSubsidies = 0;
		int tax = 0;
		int socialSecurity = 0;
		int socialSecurityDeduction = 0;
		int heatingAllowance = 0;
		//生效时间在考勤开始时间之前 则使用当前工资
		if(effectTime.isBefore(startTime)||effectTime.isEqual(startTime)){
			
			postSalary = wage.getPostSalary();
			standardSalary = wage.getStandardSalary();
			rewardSalary = wage.getRewardSalary();
			serviceAward = wage.getServiceAward();
			communicationAllowance = wage.getCommunicationAllowance();
			oilAllowance = wage.getOilAllowance();
			mealAllowance = wage.getMealAllowance();
			overtimeAllowance = wage.getOvertimeAllowance();
			maternityInsurance = wage.getMaternityInsurance();
			housingSubsidies = wage.getHousingSubsidies();
			tax = wage.getTax();
			socialSecurity = wage.getSocialSecurity();
			socialSecurityDeduction = wage.getSocialSecurityDeduction();
			heatingAllowance = wage.getHeatingAllowance();
		}
		//生效时间在考勤时间之后,则使用过渡工资
		else if(effectTime.isAfter(endTime)){
			List<HistoryWage> historyWage = dao.query(HistoryWage.class, Cnd.where("userId", "=", user.getUserId()).and("status", "=", Status.INTERIM));
			if(historyWage==null)return;
			for(HistoryWage history : historyWage){
				
				postSalary = history.getPostSalary();
				standardSalary = history.getStandardSalary();
				rewardSalary = history.getRewardSalary();
				serviceAward = history.getServiceAward();
				communicationAllowance = history.getCommunicationAllowance();
				oilAllowance = history.getOilAllowance();
				mealAllowance = history.getMealAllowance();
				overtimeAllowance = history.getOvertimeAllowance();
				maternityInsurance = history.getMaternityInsurance();
				housingSubsidies = history.getHousingSubsidies();
				tax = history.getTax();
				socialSecurity = history.getSocialSecurity();
				socialSecurityDeduction = history.getSocialSecurityDeduction();
				heatingAllowance = history.getHeatingAllowance();
				
				if(att.getResultMonth().length()!=6){
					rewardSalary = 0;
					serviceAward = 0;
					communicationAllowance = 0;
					oilAllowance = 0;
					mealAllowance = 0;
					maternityInsurance = 0;
					housingSubsidies = 0;
					tax = 0;
					socialSecurity = 0;
					socialSecurityDeduction = 0;
					heatingAllowance = 0;
				}
				break;
			}
		}
		else
			return;
		
		//从数据库获取日排班的所有记录
		Map<Integer, WorkDay> dayMap = workRepository.dayMap();
		//获取用户所属的公司的日排班
		WorkDay day = dayMap.get(user.getDayId());
		//计算工作时长
		int workMinute = Works.workMinute(day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut());
		
		// 1) 工资单位
		int salary = standardSalary + postSalary; //基本工资+岗位工资
		int Standardsalary = standardSalary; //基本工资
		
		Double salaryPreDay = Double.valueOf(salary) / Double.valueOf(standardWorkDay);	 //全额日工资 , 工作日根据月排班和周排班计算
		Double StandardsalaryPreDay = Double.valueOf(Standardsalary) / Double.valueOf(standardWorkDay);	 //基本日工资 , 工作日根据月排班和周排班计算
		Double salaryPreMinute = salaryPreDay / workMinute;  //全额分钟工资
		Double StandardsalaryMinute = StandardsalaryPreDay / workMinute; //基本分钟工资
		
	//	Double salaryhalfDay =salaryPreDay/2.0;  //半日全额工资
	//	Double StandardsalaryhalfDay =StandardsalaryPreDay/2.0;  //半日基本工资
		
		Double performSalary = 0.0; 		//绩效工资
		
		Double forgetDeduction = 0.0;   //未打卡
		Double lateDeduction = 0.0;  	//迟到扣除
	//	Double earlyDeduction = 0.0; 	//早退扣除
		Double absentDeduction = 0.0; 	//旷工扣除
		Double leaveDeduction = 0.0;  	//请假扣除
		Double sickDeduction = 0.0;		//病假扣除

		Map<String,String> threshold = Converts.map(att.getThreshold());
		
		// 2) 迟到, 早退, 旷工
		for(AttendanceThresholdItem item : thresholdItem){
			if(item.getType().equals(Status.A)){
				if(threshold.get(item.getItemId()+"")!=null){
					String a = threshold.get(item.getItemId()+"");
					Integer amount = Integer.parseInt(a);
					absentDeduction = absentDeduction + deduction(item,amount,salaryPreDay,StandardsalaryPreDay, 0);
				}
			}else if(item.getType().equals(Status.F)){
				if(threshold.get(item.getItemId()+"")!=null){
					String a = threshold.get(item.getItemId()+"");
					Integer amount = Integer.parseInt(a);
					forgetDeduction = forgetDeduction + deduction(item,amount,salaryPreDay,StandardsalaryPreDay, 0);
				}
			}else if(item.getType().equals(Status.L)){
				if(threshold.get(item.getItemId()+"")!=null){
					String a = threshold.get(item.getItemId()+"");
					Integer amount = Integer.parseInt(a);
					lateDeduction = lateDeduction + deduction(item,amount,salaryPreDay,StandardsalaryPreDay, 0);
				}
			}
				
		}
		
		
		
		Map<Integer, ConfLeaveType> typeMap = new HashMap<Integer, ConfLeaveType>();
		
		for(ConfLeaveType type :  leaveType){
			
			typeMap.put(type.getLeaveType(), type);
		}
		
		// 3) 事假扣款规则 有配置到的事假按配置执行，没有配置按时间单位的扣全额工资
		
		
		leaveDeduction += salaryModel(typeMap, Value.ONE, att.getUnpaidLeaveMinute(), workMinute , salaryPreMinute , StandardsalaryMinute); //无薪事假
		leaveDeduction += salaryModel(typeMap, Value.TWO, att.getSickLeaveMinute(), workMinute , salaryPreMinute , StandardsalaryMinute);	//病假
		leaveDeduction += salaryModel(typeMap, Value.THREE, att.getFuneralLeaveMinute(), workMinute , salaryPreMinute , StandardsalaryMinute);//丧假
		leaveDeduction += salaryModel(typeMap, Value.FOUR, att.getMaritalLeaveMinute(), workMinute , salaryPreMinute , StandardsalaryMinute);//婚假
	//	salaryModel(leaveDeduction, typeMap, Value.FIVE, att.getUnpaidLeaveUnit(), salaryhalfDay, StandardsalaryhalfDay); //年休假
		leaveDeduction += salaryModel(typeMap, Value.SIX, att.getInjuryLeaveMinute(), workMinute , salaryPreMinute , StandardsalaryMinute);//工伤假
	//	salaryModel(leaveDeduction, typeMap, Value.SEVEN, att.getDeferredLeaveUnit(), salaryhalfDay, StandardsalaryhalfDay); //加班补休
		leaveDeduction += salaryModel(typeMap, Value.EIGHT, att.getPaternityLeaveMinute(), workMinute , salaryPreMinute , StandardsalaryMinute); //陪产假
		leaveDeduction += salaryModel(typeMap, Value.NINE, att.getPaidLeaveMinute(), workMinute , salaryPreMinute , StandardsalaryMinute); //有薪事假
		
		// 5) 绩效
	/*	if (releaseId != null) {
			List<Target> targets = mapper.query(Target.class, "Target.query", Cnd
					.where("p.user_id", "=", user.getUserId())
					.and("p.release_id", "=", releaseId)
					.and("p.approved", "=", Status.OK)
					.and("p.status", "=", Status.ENABLED));
			if (!Asserts.isEmpty(targets)) {
				int scored = 0;
				for (Target target : targets) {
					scored += target.getWeight() * target.getScore() / 100;
				}
				
				for (Entry<Integer, Float> entry : measureMap.entrySet()) {
					int score = entry.getKey();
					float coefficient = entry.getValue();
					if (scored >= score) {
						performSalary = (int) (Values.getInt(wage.getPerformSalary(), Value.I) * coefficient);
						break;
					}
				}
			}
		}*/
		
		// 6) 加班
	//	int overtimeAllowance = (int) (att.getOvertimeMinute() * wage.getOvertimeAllowance() / 60.d);
		
		boolean exist = true;
		
		
		WageResult wrs = dao.fetch(WageResult.class, Cnd.where("resultMonth", "=", att.getResultMonth()).and("userId", "=", user.getUserId()));
		if (wrs == null) {
			wrs = new WageResult();
			exist = false;
		}
		
		wrs.setResultMonth(att.getResultMonth());
		wrs.setUserId(user.getUserId());
		wrs.setStandardSalary(Double.valueOf(standardSalary));
		wrs.setPostSalary(Double.valueOf(postSalary));
		wrs.setPerformSalary(performSalary);
		wrs.setRewardSalary(Double.valueOf(rewardSalary));
		wrs.setServiceAward(Double.valueOf(serviceAward));
		wrs.setCommunicationAllowance(Double.valueOf(communicationAllowance));
		wrs.setOilAllowance(Double.valueOf(oilAllowance));
		wrs.setHeatingAllowance(Double.valueOf(heatingAllowance));
		wrs.setMealAllowance(Double.valueOf(mealAllowance));
		wrs.setOvertimeAllowance(Double.valueOf(overtimeAllowance));
		wrs.setMaternityInsurance(Double.valueOf(maternityInsurance));
		wrs.setHousingSubsidies(Double.valueOf(housingSubsidies));
		wrs.setLateDeduction(RMB.toMinute(lateDeduction));
		wrs.setAbsentDeduction(RMB.toMinute(absentDeduction));
		wrs.setForgetDeduction(RMB.toMinute(forgetDeduction));
		wrs.setLeaveDeduction(leaveDeduction);
		wrs.setSickDeduction(RMB.toMinute(sickDeduction));
		wrs.setTax(Double.valueOf(tax));
		wrs.setSocialSecurity(Double.valueOf(socialSecurity));
		wrs.setSocialSecurityDeduction(Double.valueOf(socialSecurityDeduction));
		wrs.setModifyId(modifyId);
		wrs.setModifyTime(now.toDate());
		Archive archive = dao.fetch(Archive.class,Cnd.where("user_id", "=", user.getUserId()));
		if(archive!=null){
			//3为在职状态为离薪
			if(archive.getOnPosition()==3){
				wrs.setStandardSalary(0.00);
				wrs.setPostSalary(0.00);
				wrs.setPerformSalary(performSalary);
				wrs.setRewardSalary(0.00);
				wrs.setServiceAward(0.00);
				wrs.setCommunicationAllowance(0.00);
				wrs.setOilAllowance(0.00);
				wrs.setHeatingAllowance(0.00);
				wrs.setMealAllowance(0.00);
				wrs.setOvertimeAllowance(0.00);
				wrs.setMaternityInsurance(0.00);
				wrs.setHousingSubsidies(0.00);
				wrs.setLateDeduction(0.00);
				wrs.setAbsentDeduction(0.00);
				wrs.setForgetDeduction(0.00);
				wrs.setLeaveDeduction(0.00);
				wrs.setSickDeduction(0.00);
				wrs.setTax(0.00);
				wrs.setSocialSecurity(0.00);
				wrs.setSocialSecurityDeduction(0.00);
			}
		}
		if (exist)
			dao.update(wrs);
		else
			dao.insert(wrs);
	}
//-------------------------------------------------------------------------------------------------
	private Double salaryModel(Map<Integer, ConfLeaveType> typeMap,int leaveType, int minute,int workMinute, Double salaryPreMinute, Double StandardsalaryMinute){
		Double leaveDeduction = 0.0;
		if(typeMap.get(leaveType) == null){
			//根据请假的时间（分钟）进行扣除
			leaveDeduction += minute * salaryPreMinute; //如果没有配置到这个请假类型则根据时间扣全额工资
		}
		else{
			if(minute != 0){
				ConfLeaveType confType  = typeMap.get(leaveType); //请假类型编号不能更改，否则计算工资有误
				Double salaryModel = salaryPreMinute;
				 // 1为基本工资， 0为全额工资
				if(confType.getWay().equals(1))                                            
					salaryModel = StandardsalaryMinute;
				// 根据扣除系数，扣除工资
				if(confType.getMultiplication() != null && confType.getMultiplication() != 0){
					Double multiplication = confType.getMultiplication();
					leaveDeduction += confType.getDayAmount()* workMinute * salaryModel * multiplication;
				}
				//实际请假的时间大于预定的请假时间
				if(confType.getDayAmount()* workMinute < minute )
					leaveDeduction += (minute - confType.getDayAmount()* workMinute ) * salaryModel;
			}
			
		}
		
		return leaveDeduction;
	}

	private int[] leave(String startStr, String endStr, String typeName, List<Leave> leaves, Map<String, String> dictMap, 
			int workMinute, WorkDay day, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
		Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();//String{开始时分,null}String{null,结束时分}保存请假的时间详细
		
		for (Leave leave : leaves) {
			if (!dictMap.containsKey(String.valueOf(leave.getTypeId()))) continue;
			if (!dictMap.get(String.valueOf(leave.getTypeId())).equals(typeName)) continue;
			
			DateTime start = new DateTime(leave.getStartTime());
			DateTime end = new DateTime(leave.getEndTime());
			
			//修复跨周期请假的问题
			if(leave.getEndTime().after(Calendars.parse(endStr, Calendars.DATE).toDate())){
				String leaveEndStr = endStr + " " +day.getCheckOut()+":00";
				end = Calendars.parse(leaveEndStr, Calendars.DATE_TIMES); //加上时分秒
			}
			if(leave.getStartTime().before(Calendars.parse(startStr, Calendars.DATE).toDate())){
				start = Calendars.parse(startStr +" " + day.getCheckIn()+":00", Calendars.DATE_TIMES); //加上时分秒
			}
			
			Works.dayMap(dayMap, start.toString(Calendars.DATE_TIME), end.toString(Calendars.DATE_TIME));
		}
		
		return minute(startStr, endStr, dayMap, workMinute, day, monthOfDays, weekOfDays);//得到一次请假的分钟数
	}
	
	private int[] outwork(String startStr, String endStr, List<Outwork> outworks,
			int workMinute, WorkDay day, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
		Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
		
		for (Outwork outwork : outworks) {
			DateTime start = new DateTime(outwork.getStartTime());
			DateTime end = new DateTime(outwork.getEndTime());
			Works.dayMap(dayMap, start.toString(Calendars.DATE_TIME), end.toString(Calendars.DATE_TIME));
		}
		
		return minute(startStr, endStr, dayMap, workMinute, day, monthOfDays, weekOfDays);
	}
	
	private int[] errand(String startStr, String endStr, List<Errand> errands,
			int workMinute, WorkDay day, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
		Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
		
		for (Errand errand : errands) {
			DateTime start = new DateTime(errand.getStartTime());
			DateTime end = new DateTime(errand.getEndTime());
			Works.dayMap(dayMap, start.toString(Calendars.DATE_TIME), end.toString(Calendars.DATE_TIME));
		}
		
		return minute(startStr, endStr, dayMap, workMinute, day, monthOfDays, weekOfDays);
	}
	
	private int overtime(String startStr, String endStr, List<Overtime> overtimes) {
		int minute = 0;
		for (Overtime overtime : overtimes) {
			DateTime start = new DateTime(overtime.getStartTime());
			DateTime end = new DateTime(overtime.getEndTime());
			minute += Minutes.minutesBetween(start, end).getMinutes();
		}
		return minute;
	}
	
	private int[] minute(String startStr, String endStr, Map<String, String[]> dayMap,
			int workMinute, WorkDay day, Map<String, Integer[]> monthOfDays, String[] weekOfDays) {
		DateTime start = Calendars.parse(startStr.substring(0, 10), Calendars.DATE);
		DateTime end = Calendars.parse(endStr.substring(0, 10), Calendars.DATE);
	
		int days = Days.daysBetween(start, end).getDays();
		
		int minute = 0;
		int dayminute = 0;
		int unitMinute[] = new int[2];
		
		for (int i = 0; i < days + 1; i++) {
			DateTime plus = start.plusDays(i);
			String date = plus.toString(Calendars.DATE);
			if (!dayMap.containsKey(date)) continue;
			String[] arr = dayMap.get(date);
			String in = arr[0];
			String out = arr[1];
			if (Strings.isBlank(in)) in = day.getCheckIn();
			if (Strings.isBlank(out)) out = day.getCheckOut();
			
			dayminute = Works.getMinute(date + " " + in, date + " " + out, 
					day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut(),
					workMinute, monthOfDays, weekOfDays);
			
			minute = minute + dayminute;
		}
		unitMinute[0] = minute;
		return unitMinute;
	}
	
	private Map<Integer,Integer> times(List<AttendanceThresholdItem> thresholdItem , Integer userId, String startStr, String endStr,
			List<Leave> leaves, List<Outwork> outworks, List<Errand> errands, WorkDay day) {
		DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
		DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");
		
		//获取两个时间段之间的月数
		int months = Months.monthsBetween(start, end).getMonths();
		
		//将所有的请假转换成map集合，key是请假的每一天
		Map<String, String[]> leaveDayMap = new ConcurrentHashMap<String, String[]>();
		for (Leave leave : leaves) {
			DateTime leaveStart = new DateTime(leave.getStartTime());
			DateTime leaveEnd = new DateTime(leave.getEndTime());
			Works.dayMap(leaveDayMap, leaveStart.toString(Calendars.DATE_TIME), leaveEnd.toString(Calendars.DATE_TIME));
		}
		//将所有的外勤转换成map集合，key是外勤的每一天
		Map<String, String[]> outworkDayMap = new ConcurrentHashMap<String, String[]>();
		for (Outwork outwork : outworks) {
			DateTime outworkStart = new DateTime(outwork.getStartTime());
			DateTime outworkEnd = new DateTime(outwork.getEndTime());
			Works.dayMap(outworkDayMap, outworkStart.toString(Calendars.DATE_TIME), outworkEnd.toString(Calendars.DATE_TIME));
		}
		//将所有的出差转换成map集合，key是出差的每一天
		Map<String, String[]> errandDayMap = new ConcurrentHashMap<String, String[]>();
		for (Errand errand : errands) {
			DateTime errandStart = new DateTime(errand.getStartTime());
			DateTime errandEnd = new DateTime(errand.getEndTime());
			Works.dayMap(errandDayMap, errandStart.toString(Calendars.DATE_TIME), errandEnd.toString(Calendars.DATE_TIME));
		}
		
		//旷工
		int absentTimes = 0;
		//未打卡
		int forgetTimes = 0;
		//迟到
		int lateTimes = 0;
		//早退
		int earlyTimes = 0;
		
		Map<Integer,Integer> thresholdMap =new HashMap<Integer,Integer>();
		Map<Integer,Integer> late =new HashMap<Integer, Integer>();
		
		for (int i = 0; i < months + 1; i++) {
			DateTime plus = start.plusMonths(i);
			
			if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM"))) continue;
			
			Map<String, String> vars = new ConcurrentHashMap<String, String>();
			vars.put("month", plus.toString("yyyyMM"));
			List<CheckedRecord> attendances = mapper.query(CheckedRecord.class, "CheckedRecord.query", Cnd
					.where("c.user_id", "=", userId)
					.and("c.work_date", ">=", startStr)
					.and("c.work_date", "<=", endStr), null, vars);
			
			for (CheckedRecord attendance : attendances) {
				String checkedIn = attendance.getCheckedIn();
				String checkedOut = attendance.getCheckedOut();
				String checkIn = day.getCheckIn();
				String checkOut = day.getCheckOut();
				String remarkIn = attendance.getRemarkIn();
				String remarkOut = attendance.getRemarkOut();
				
				if (Strings.isNotBlank(remarkIn) && Strings.isNotBlank(remarkOut)) {
					if (remarkIn.equals(Check.REST) && remarkOut.equals(Check.REST)) continue;
					if (remarkIn.equals(Check.NORMAL) && remarkOut.equals(Check.NORMAL)) continue;
				}
				if (remarkIn.equals(Check.DIMISSION) && remarkOut.equals(Check.DIMISSION)) continue;

				String date = new DateTime(attendance.getWorkDate()).toString(Calendars.DATE);
				
				// 优先级: 请假>外勤>出差.
				boolean next = true;
				
				String leaveIn = null;
				String leaveOut = null;
				
				boolean leave = false;
				if (leaveDayMap.containsKey(date)) {   //在当天考勤非正常的情况下，判断当天是否有请假，外勤，出差等
					String[] arr = leaveDayMap.get(date);
					leaveIn = arr[0];
					leaveOut = arr[1];
					
					if (Strings.isBlank(leaveIn)) leaveIn = checkIn;
					if (Strings.isBlank(leaveOut)) leaveOut = checkOut;

					if (leaveIn.compareTo(leaveOut) < 0) {
						if (Strings.isBlank(checkedIn)) checkedIn = leaveIn;
						if (Strings.isBlank(checkedOut)) checkedOut = leaveOut;
					}
					
					leave = true;
					next = false;
				}
				
				if (next) {
					if (outworkDayMap.containsKey(date)) {
						String[] arr = outworkDayMap.get(date);
						if (Strings.isBlank(checkedIn)) checkedIn = arr[0];
						if (Strings.isBlank(checkedOut)) checkedOut = arr[1];
						next = false;
					}
				}
				if (next) {
					if (errandDayMap.containsKey(date)) {
						if (Strings.isBlank(checkedIn)) checkedIn = day.getCheckIn();
						if (Strings.isBlank(checkedOut)) checkedOut = day.getCheckOut();
						next = false;
					}
				}
				
				// 旷工:两次打卡都为空  
				if (Strings.isBlank(checkedIn) && Strings.isBlank(checkedOut)) {
					//得到checkedIn这天
					absentTimes++;
				}
				
				//未打卡
				if((Strings.isBlank(checkedIn)&& Strings.isNotBlank(checkedOut)) || (Strings.isNotBlank(checkedIn) && Strings.isBlank(checkedOut))){
					forgetTimes ++;
				}
				
				// 迟到
				if (Strings.isNotBlank(checkedIn)) {
					int minute = 0;
					if (leave) {
						if (leaveIn.compareTo(checkIn) == 0) {
							if (leaveOut.compareTo(day.getRestIn()) < 0) {
								minute = Works.minutesBetween(leaveOut, checkedIn);	
							} else {
								minute = Works.minutesBetween(checkedIn, leaveOut);	
							}
						}
						if (leaveIn.compareTo(checkIn) > 0) {
							if (leaveIn.compareTo(checkedIn) > 0) minute = Works.minutesBetween(checkedIn, checkIn);
							else minute = Works.minutesBetween(leaveIn, checkIn);
						}
					} else {
						if (checkedIn.compareTo(checkIn) > 0) minute = Works.minutesBetween(checkedIn, checkIn);
					}
					
					if(minute < 0)
						lateTimes++;
					
					for(AttendanceThresholdItem item : thresholdItem){
						if(item.getType().equals(Status.L)){
							if(minute < -(item.getMinuteStart())&& minute >= -(item.getMinuteEnd())){
								if(late.get(item.getItemId())==null){
									late.put(item.getItemId(), Value.ONE);
								}else{
									Integer amount = late.get(item.getItemId())+ 1;
									late.remove(item.getItemId());
									late.put(item.getItemId(), amount);
								}
							}
								
						}
					}
				} 
				
				// 早退
				if (Strings.isNotBlank(checkedOut)) {
					int minute = 0;
					if (leave) {
						if (leaveOut.compareTo(checkOut) == 0) {
							if (leaveIn.compareTo(day.getRestOut()) > 0) {
								minute = Works.minutesBetween(leaveIn, checkedOut);									
							} else {
								minute = Works.minutesBetween(checkedOut, leaveIn);	
							}
						}
						if (leaveOut.compareTo(checkOut) < 0) {
							if (leaveOut.compareTo(checkedOut) > 0) minute = Works.minutesBetween(leaveOut, checkOut);
							else minute = Works.minutesBetween(checkedOut, checkOut);
						}
					} else {
						if (checkedOut.compareTo(checkOut) < 0) minute = Works.minutesBetween(checkedOut, checkOut);
					}
					if(minute > 0)
						earlyTimes++;
					
					for(AttendanceThresholdItem item : thresholdItem){
						if(item.getType().equals(Status.L)){
							if(minute > item.getMinuteStart()&& minute <= item.getMinuteEnd()){
								if(late.get(item.getItemId())==null){
									late.put(item.getItemId(), Value.ONE);
								}else{
									Integer amount = late.get(item.getItemId())+ 1;
									late.remove(item.getItemId());
									late.put(item.getItemId(), amount);
								}
							}
								
						}
					}
				} 
			}
			
			
			//遍历考勤阀值项，将匹配考勤阀值项的情况存入thresholdMap，进而生成在resut_by_attaxxx的threshold值中
			for(AttendanceThresholdItem item : thresholdItem){
				//验证类型 旷工
				if(item.getType().equals(Status.A)){
					//验证次数
					if(absentTimes > item.getAmountStart() && absentTimes <= item.getAmountEnd())
						thresholdMap.put(item.getItemId(), absentTimes - item.getAmountStart());
				}
				//未打卡
				else if(item.getType().equals(Status.F)){
					if(forgetTimes > item.getAmountStart() && forgetTimes <= item.getAmountEnd())
						thresholdMap.put(item.getItemId(), forgetTimes - item.getAmountStart());
				}
				//迟到早退
				if(item.getType().equals(Status.L)){
					if(late.get(item.getItemId()) !=null){
						Integer amount = late.get(item.getItemId());
						if(amount > item.getAmountStart() && amount <= item.getAmountEnd())
							thresholdMap.put(item.getItemId(), amount - item.getAmountStart());
					}
				}
			}
		}
		thresholdMap.put(-1, lateTimes); // -1为迟到
		thresholdMap.put(-2, earlyTimes); // -2为早退
		thresholdMap.put(-3, absentTimes); // -3为旷工
		thresholdMap.put(-4, forgetTimes); // -4为未打卡
		return thresholdMap;
	}
	
	/**
	 * 统计迟到、旷工、早退
	 * @param thresholdItem
	 * @param userId
	 * @param startStr
	 * @param endStr
	 * @param leaves
	 * @param outworks
	 * @param errands
	 * @param day
	 * @param isNight
	 * @param quitDate
	 * @return
	 */
	private Map<Integer,Integer> statistics(List<AttendanceThresholdItem> thresholdItem , Integer userId, String startStr, String endStr,
			List<Leave> leaves, List<Outwork> outworks, List<Errand> errands,Map<Date, WorkDay> day,boolean isNight,Date quitDate) {
		
		DateTime start = Calendars.parse(startStr.substring(0, 7), "yyyy-MM");
		DateTime end = Calendars.parse(endStr.substring(0, 7), "yyyy-MM");
		
		int months = Months.monthsBetween(start, end).getMonths();
		
		Map<String, String[]> leaveDayMap = new ConcurrentHashMap<String, String[]>();
		for (Leave leave : leaves) {
			DateTime leaveStart = new DateTime(leave.getStartTime());
			DateTime leaveEnd = new DateTime(leave.getEndTime());
			Works.dayMap(leaveDayMap, leaveStart.toString(Calendars.DATE_TIME), leaveEnd.toString(Calendars.DATE_TIME));
		}
		
		Map<String, String[]> outworkDayMap = new ConcurrentHashMap<String, String[]>();
		for (Outwork outwork : outworks) {
			DateTime outworkStart = new DateTime(outwork.getStartTime());
			DateTime outworkEnd = new DateTime(outwork.getEndTime());
			Works.dayMap(outworkDayMap, outworkStart.toString(Calendars.DATE_TIME), outworkEnd.toString(Calendars.DATE_TIME));
		}
		
		Map<String, String[]> errandDayMap = new ConcurrentHashMap<String, String[]>();
		for (Errand errand : errands) {
			DateTime errandStart = new DateTime(errand.getStartTime());
			DateTime errandEnd = new DateTime(errand.getEndTime());
			Works.dayMap(errandDayMap, errandStart.toString(Calendars.DATE_TIME), errandEnd.toString(Calendars.DATE_TIME));
		}
		
		//旷工
		int absentTimes = 0;
		//旷工半天
		int absentHalfTimes = 0;
		//未打卡
		int forgetTimes = 0;
		//迟到
		int lateTimes = 0;
		//早退
		int earlyTimes = 0;
		
		Map<Integer,Integer> thresholdMap =new HashMap<Integer,Integer>();
		Map<Integer,Integer> late =new HashMap<Integer, Integer>();
		//用了记录可能需要被移除的考勤阀值项
		List<Integer> removeThreshold = new ArrayList<Integer>();
		
		for (int i = 0; i < months + 1; i++) {
			DateTime plus = start.plusMonths(i);
			
			if (!dao.exists("att_checked_record_" + plus.toString("yyyyMM"))) continue;
			
			Map<String, String> vars = new ConcurrentHashMap<String, String>();
			vars.put("month", plus.toString("yyyyMM"));
			List<CheckedRecord> attendances = mapper.query(CheckedRecord.class, "CheckedRecord.query", Cnd
					.where("c.user_id", "=", userId)
					.and("c.work_date", ">=", startStr)
					.and("c.work_date", "<=", endStr), null, vars);

			
			for (CheckedRecord attendance : attendances) {
				System.out.println(attendance.getWorkDate());
				WorkDay workDay = day.get(attendance.getWorkDate());
				if(workDay == null)continue;
				String checkedIn = attendance.getCheckedIn();
				String checkedOut = attendance.getCheckedOut();
				String restIn = workDay.getRestIn();
				String restOut = workDay.getRestOut();
				String checkIn = workDay.getCheckIn();
				String checkOut = workDay.getCheckOut();
				String remarkIn = attendance.getRemarkIn();
				String remarkOut = attendance.getRemarkOut();

				
				if (Strings.isNotBlank(remarkIn) && Strings.isNotBlank(remarkOut)) {
					if (remarkIn.equals(Check.REST) && remarkOut.equals(Check.REST)) continue;
					if (remarkIn.equals(Check.NORMAL) && remarkOut.equals(Check.NORMAL)) continue;
				}
				if (remarkIn.equals(Check.DIMISSION) && remarkOut.equals(Check.DIMISSION)) continue;

				String date = new DateTime(attendance.getWorkDate()).toString(Calendars.DATE);
				
				// 优先级: 请假>外勤>出差.
				boolean next = true;
				
				String leaveIn = null;
				String leaveOut = null;
				
				boolean leave = false;
				if (leaveDayMap.containsKey(date)) {   //在当天考勤非正常的情况下，判断当天是否有请假，外勤，出差等
					String[] arr = leaveDayMap.get(date);
					leaveIn = arr[0];
					leaveOut = arr[1];
					
					System.out.println(leaveIn+ ":" + leaveOut);
					
					
					//跨天请假的时候，自动补上请假时间
					if (Strings.isBlank(leaveIn)) leaveIn = checkIn;
					if (Strings.isBlank(leaveOut)) leaveOut = checkOut;
					//获取打卡时间
					if (leaveIn.compareTo(leaveOut) < 0) {
						if(leaveIn.compareTo(restIn)<0&&leaveOut.compareTo(restOut)<0){
							//只有早上请假,补录打卡信息
							checkedIn = leaveIn;
						}else if(leaveIn.compareTo(restIn)>0&&leaveOut.compareTo(restOut)>0){
							//只有下午请假,补录打卡信息
							checkedOut = leaveOut;		
						}else{
							if (Strings.isBlank(checkedIn)) checkedIn = leaveIn;
							if (Strings.isBlank(checkedOut)) checkedOut = leaveOut;		
						}
					}
					
					leave = true;
					next = false;
				}
				
				if (next) {
					if (outworkDayMap.containsKey(date)) {
						String[] arr = outworkDayMap.get(date);
						if(arr[0].compareTo(restIn)<0&&arr[1].compareTo(restOut)<0){
							//只有早上外勤
							checkedIn = arr[0];
						}else if(arr[0].compareTo(restIn)>0&&arr[1].compareTo(restOut)>0){
							//只有下午外勤
							checkedOut = arr[1];
						}else{
							if (Strings.isBlank(checkedIn)) checkedIn = arr[0];
							if (Strings.isBlank(checkedOut)) checkedOut = arr[1];
						}
						next = false;
					}
				}
				if (next) {
					if (errandDayMap.containsKey(date)) {
						if (Strings.isBlank(checkedIn)) checkedIn = workDay.getCheckIn();
						if (Strings.isBlank(checkedOut)) checkedOut = workDay.getCheckOut();
						next = false;
					}
				}				
				
				// 旷工:两次打卡都为空  
				if (Strings.isBlank(checkedIn) && Strings.isBlank(checkedOut)) {
					if(!(quitDate != null && quitDate.before(attendance.getWorkDate()))){
						//得到checkedIn这天
						absentTimes ++;
						absentHalfTimes = absentHalfTimes + 2;
					}
				}
		
				//未打卡或旷工半天
				if((Strings.isBlank(checkedIn)&& Strings.isNotBlank(checkedOut)) || (Strings.isNotBlank(checkedIn) && Strings.isBlank(checkedOut))){
					//判断是旷工半天还是未打卡
					if("旷工".equals(remarkIn) || "旷工".equals(remarkOut)){
							absentHalfTimes ++;
					}else{
						forgetTimes ++;
					}

				}
				//夜班
				if(isNight){
					if (Strings.isNotBlank(checkedIn)) {if (checkedIn.compareTo(checkOut) > 0){lateTimes++;}}
					if (Strings.isNotBlank(checkedOut)) {if (checkedOut.compareTo(checkIn) < 0){earlyTimes++;}}
				}else{
				// 迟到
				if (Strings.isNotBlank(checkedIn)) {
					//算迟到时间,算算符合哪个考勤阀值
					int minute = 0;
					if (leave) {
						if (leaveIn.compareTo(checkIn) == 0) {
							if (leaveOut.compareTo(workDay.getRestIn()) < 0) {
								minute = Works.minutesBetween(checkedIn, restOut);	
							} else {
								minute = Works.minutesBetween(checkedIn, restOut);	
							}
						}
						if (leaveIn.compareTo(checkIn) > 0) {
							minute = Works.minutesBetween(checkedIn, checkIn);
						}
					} else {
						if (checkedIn.compareTo(checkIn) > 0) minute = Works.minutesBetween(checkedIn, checkIn);
					}
					
					if(minute < 0)
						lateTimes++;
					
					for(AttendanceThresholdItem item : thresholdItem){
						if(item.getType().equals(Status.L)){
							if(minute < -(item.getMinuteStart())&& minute >= -(item.getMinuteEnd())){
								if(late.get(item.getItemId())==null){
									late.put(item.getItemId(), Value.ONE);
								}else{
									Integer amount = late.get(item.getItemId())+ 1;
									late.remove(item.getItemId());
									late.put(item.getItemId(), amount);
								}
							}
								
						}
					}
				} 
				
				// 早退
				if (Strings.isNotBlank(checkedOut)) {
					int minute = 0;
					if (leave) {
						if (leaveOut.compareTo(checkOut) == 0) {
							if (leaveIn.compareTo(workDay.getRestOut()) > 0) {
								minute = Works.minutesBetween(checkedOut, restOut);									
							} else {
								minute = Works.minutesBetween(checkedOut, restOut);	
							}
						}
						if (leaveOut.compareTo(checkOut) < 0) {
							minute = Works.minutesBetween(checkedOut, checkOut);
						}
					} else {
						if (checkedOut.compareTo(checkOut) < 0) minute = Works.minutesBetween(checkedOut, checkOut);
					}
					if(minute > 0)
						earlyTimes++;
					
					for(AttendanceThresholdItem item : thresholdItem){
						//验证类型
						if(item.getType().equals(Status.L)){
							//验证时间
							if(minute > item.getMinuteStart()&& minute <= item.getMinuteEnd()){
								if(late.get(item.getItemId())==null){
									late.put(item.getItemId(), Value.ONE);
								}else{
									Integer amount = late.get(item.getItemId())+ 1;
									late.remove(item.getItemId());
									late.put(item.getItemId(), amount);
								}
							}
								
						}
					}
				} 
			}
			
			}
			
			boolean first = true;
			//遍历考勤阀值项，将匹配考勤阀值项的情况存入thresholdMap，进而生成在resut_by_attaxxx的threshold值中
			for(AttendanceThresholdItem item : thresholdItem){
				//验证类型 旷工(半天)
				if(item.getType().equals(Status.A)){
					//验证次数
					if(absentHalfTimes > item.getAmountStart() && absentHalfTimes <= item.getAmountEnd()){
						thresholdMap.put(item.getItemId(), absentHalfTimes - item.getAmountStart() );
					}
						
				}
				//未打卡
				else if(item.getType().equals(Status.F)){
					if(first){
						if(forgetTimes > 0 && forgetTimes <= item.getAmountEnd()){
							//允许未打卡次数由申请补录代替
							thresholdMap.put(item.getItemId(), forgetTimes);
							removeThreshold.add(item.getItemId());
						}
						first = false;
					}else{
						if(forgetTimes > item.getAmountStart() && forgetTimes <= item.getAmountEnd()){
						//允许未打卡次数由申请补录代替
						thresholdMap.put(item.getItemId(), forgetTimes);
						removeThreshold.add(item.getItemId());
						}
					}
				}
				//迟到早退
				if(item.getType().equals(Status.L)){
					if(late.get(item.getItemId()) !=null){
						Integer amount = late.get(item.getItemId());
						if(amount > item.getAmountStart() && amount <= item.getAmountEnd()){
							thresholdMap.put(item.getItemId(), amount - item.getAmountStart());
							removeThreshold.add(item.getItemId());
						}
					}
				}
			}
		}
		
		//每三次，就对迟到或早退或未打卡进行取消，因为存在累积扣除的情况，即每累积三次，就扣一天工资
		List<Integer> remove = new ArrayList<Integer>();
		if((forgetTimes + earlyTimes + lateTimes) / 3 > 0){
			//获取需要取消的个数
			int cancel = ((forgetTimes + earlyTimes + lateTimes) / 3) * 3;
			for(Integer key : removeThreshold){
				if(cancel == 0) break;
				if(thresholdMap.get(key) > cancel){
					thresholdMap.put(key, (thresholdMap.get(key) - cancel));
					break;
				}else{
					cancel = cancel - thresholdMap.get(key);
					remove.add(key);
				}
			}
		}
		for(Integer key : remove){
			thresholdMap.remove(key);
		}
		

		thresholdMap.put(-1, lateTimes); // -1为迟到
		thresholdMap.put(-2, earlyTimes); // -2为早退
		thresholdMap.put(-3, absentTimes); // -3为旷工（全天）
		thresholdMap.put(-6, absentHalfTimes); // -6为旷工(半天)
		thresholdMap.put(-4, forgetTimes); // -4为未打卡
		thresholdMap.put(-5, forgetTimes + earlyTimes + lateTimes); // -5为累积
		return thresholdMap;
	}
	
	/**
	 * 扣除工资
	 * @param item
	 * @param amount
	 * @param salaryPerDay 全额日工资
	 * @param standardsalaryPerDay 基本日工资
	 * @return
	 */
	public static Double deduction(AttendanceThresholdItem item, Integer amount,Double salaryPerDay, Double standardsalaryPerDay, Integer half){
		Double deduction = 0.0;
//		Double amountTotal = 0.0;
//		amountTotal = amount * 1.0 + (half * 1.0 / 2);
		if(item.getUnit().equals(Status.Y)){ // 按金额扣除
			if(item.getWay().equals(Status.C)){
				deduction = item.getDeduct(); // 按数额
			} else if(item.getWay().equals(Status.E)){ // 每次
				deduction = amount * item.getDeduct();
			} else if(2 == item.getWay()){// 每2次
				deduction = (amount/2) * item.getDeduct();
			} else if(3 == item.getWay()){// 每3次
				deduction = (amount/3) * item.getDeduct();
			} else if(4 == item.getWay()){// 每4次
				deduction = (amount/4) * item.getDeduct();
			} else if(5 == item.getWay()){// 每5次
				deduction = (amount/5) * item.getDeduct();
			}
		}else{ // 按天薪扣除,此时结算方式才有意义
			if(item.getWay().equals(Status.C)){
				if(item.getWageType().equals(Status.S)){
					deduction =RMB.offDouble(item.getDeduct() * standardsalaryPerDay);
				} else {
					deduction =RMB.offDouble(item.getDeduct() * salaryPerDay);
				}
			} else if(item.getWay().equals(Status.E)){
				if(item.getWageType().equals(Status.S)){ // 基本工资
					deduction = RMB.offDouble(amount * item.getDeduct() * standardsalaryPerDay);  //单位元
				} else { // 全额工资
					deduction = RMB.offDouble( amount * item.getDeduct() * salaryPerDay);
				}
			} else if(2 == item.getWay()){// 每2次
				if(item.getWageType().equals(Status.S)){
					deduction = RMB.offDouble((amount/2) * item.getDeduct() * standardsalaryPerDay);
				} else {
					deduction = RMB.offDouble( (amount/2) * item.getDeduct() * salaryPerDay);
				}
			} else if(3 == item.getWay()){// 每3次
				if(item.getWageType().equals(Status.S)){
					deduction = RMB.offDouble((amount/3) * item.getDeduct() * standardsalaryPerDay);
				} else {
					deduction = RMB.offDouble( (amount/3) * item.getDeduct() * salaryPerDay);
				}
			} else if(4 == item.getWay()){// 每4次
				if(item.getWageType().equals(Status.S)){
					deduction = RMB.offDouble((amount/4) * item.getDeduct() * standardsalaryPerDay);
				} else {
					deduction = RMB.offDouble( (amount/4) * item.getDeduct() * salaryPerDay);
				}
			} else if(5 == item.getWay()){// 每5次
				if(item.getWageType().equals(Status.S)){
					deduction = RMB.offDouble((amount/5) * item.getDeduct() * standardsalaryPerDay);
				} else {
					deduction = RMB.offDouble( (amount/5) * item.getDeduct() * salaryPerDay);
				}
			}
		}
		return deduction;
	}

	private Double socialSecurity(List<SocialSecurityRuleItem> ssris, SocialSecurityRuleItem ssri, Integer base){
		if("社保金额".equals(ssri.getName())){
			List<String> itemsStrs = Rule.getChinese(ssri.getRule());
			Rule rule = new Rule(ssri.getRule(), itemsStrs);
			for(String itemsStr : itemsStrs){
				//找出跟变量名称匹配的规则，然后递归调用本方法
				for(SocialSecurityRuleItem item : ssris){
					if(itemsStr.equals(item.getName())){
						Double value = socialSecurity(ssris, item, base);
						if(value == null){return null;}
						rule.assign(itemsStr, value.toString());
						break;
					}
				}
			}
			return Double.parseDouble(String.valueOf(rule.calculate()));
		}else if("养老".equals(ssri.getName()) || "失业".equals(ssri.getName()) || "医疗".equals(ssri.getName()) || "工伤".equals(ssri.getName()) 
				|| "生育".equals(ssri.getName()) || "重大疾病".equals(ssri.getName())){
			Map<String, String> map = parseRule(ssri.getRule());
			List<String> itemsStrs = Rule.getChinese(map.get("rule"));
			Rule rule = new Rule(map.get("rule"), itemsStrs);
			for(String itemsStr : itemsStrs){
				if("缴费基数".equals(itemsStr)){
					if("-1".equals(map.get("max"))){
						if(base >= Integer.parseInt(map.get("least"))){
							rule.assign(itemsStr, base.toString());
						}else{
							rule.assign(itemsStr, map.get("least"));
						}
					}
	
				}
			}
			return Double.parseDouble(String.valueOf(rule.calculate()));
		}
		return null;
	}
	
	private Double tax(List<TaxRuleItem> tris, TaxRuleItem tri, Double salary ){
		if("税收金额".equals(tri.getName())){
			List<String> itemsStrs = Rule.getChinese(tri.getRule());
			Rule rule = new Rule(tri.getRule(), itemsStrs);
			for(String itemsStr : itemsStrs){
				//找出跟变量名称匹配的规则，然后递归调用本方法
				for(TaxRuleItem item : tris){
					if(itemsStr.equals(item.getName())){
						Double value = tax(tris, item, salary);
						if(value == null){return null;}
						rule.assign(itemsStr, value.toString());
						break;
					}
				}
			}
			return Double.parseDouble(String.valueOf(rule.calculate()));
		}else if("级数一".equals(tri.getName()) || "级数二".equals(tri.getName()) || "级数五".equals(tri.getName()) || "级数六".equals(tri.getName()) 
				|| "级数三".equals(tri.getName()) || "级数四".equals(tri.getName()) || "级数七".equals(tri.getName())){
			Map<String, String> map = parseRule(tri.getRule());
			List<String> itemsStrs = Rule.getChinese(map.get("rule"));
			Rule rule = new Rule(map.get("rule"), itemsStrs);
			for(String itemsStr : itemsStrs){
				if("工资".equals(itemsStr)){
//					String least = map.get("least");
//					String max = map.get("max");
					//直接将税前工资赋值到公式中
					rule.assign(itemsStr, salary.toString());
					
//					Double s = null;
//					if(!"-1".equals(max)){
//						if(Double.parseDouble(max) < salary ){
//							s = Double.parseDouble(max) - Double.parseDouble(least);
//							rule.assign(itemsStr, s.toString());
//						}else{
//							s = salary - Double.parseDouble(least);
//							if(s <= 0.0){
//								return 0.0;
//							}else{
//								rule.assign(itemsStr, s.toString());
//							}
//						}
//					}else{
//						if(salary > Double.parseDouble(least)){
//							s = salary - Double.parseDouble(least);
//							rule.assign(itemsStr, s.toString());
//						}else{
//							return 0.0;
//						}
//					}
				}else if("起征点".equals(itemsStr)){
					//找出跟变量名称匹配的规则，然后递归调用本方法
					for(TaxRuleItem item : tris){
						if(itemsStr.equals(item.getName())){
							Double value = tax(tris, item, salary);
							if(value == null){return null;}
							rule.assign(itemsStr, value.toString());
							break;
						}
					}
				}
			}
			//对要进行的计算的情况进行逻辑判断
			System.out.println(Double.parseDouble(rule.getVal("起征点")));
			System.out.println(Double.parseDouble(rule.getVal("工资")));
			if((Double.parseDouble(rule.getVal("工资")) - Double.parseDouble(rule.getVal("起征点"))) <= 0){
				return 0.0;
			}else{
				Double least = Double.parseDouble(map.get("least"));
				Double max = Double.parseDouble(map.get("max"));
				//要征税的工资部分
				Double taxMoney = Double.parseDouble(rule.getVal("工资")) - Double.parseDouble(rule.getVal("起征点"));
				if(!"-1".equals(max)){
					if(taxMoney > least && taxMoney <= max){
						//放行并计算
					}else{
						return 0.0;
					}
				}else{
					if(taxMoney > least){
						//放行并计算
					}else{
						return 0.0;
					}
				}
			}
			return Double.parseDouble(String.valueOf(rule.calculate()));
		}else if("起征点".equals(tri.getName())){
			//直接将起征点中规则描述的金额返回
			return Double.parseDouble(tri.getRule());
		}
		return null;
	}
	
	public Map<String, String> parseRule(String rule){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("rule", rule.substring(0, rule.indexOf('[')));
		map.put("least", rule.substring(rule.indexOf('[')+1, rule.indexOf(',')));
		map.put("max", rule.substring(rule.indexOf(',')+1, rule.indexOf(']')));
		return map;
	}
	
}
