package cn.oa.service;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.consts.ShiftC;
import cn.oa.consts.Status;
import cn.oa.model.CheckedRecord;
import cn.oa.model.Overtime;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.WorkAttendance;
import cn.oa.repository.Mapper;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.web.Context;

@IocBean
public class OvertimeService {
	
	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private CheckedRecordService checkedRecordService;
	@Inject
	private OvertimeRestService overtimeRestService;
	
	/**
	 * 查剩余加班补休时长
	 * @param userId
	 * @param leaveId
	 * @param startTime
	 * @param endTime
	 * @param leaveMap
	 * @return
	 */
	public int lastDeferredMinute(Integer userId, Integer leaveId, DateTime startTime, DateTime endTime, Map<String, String> leaveMap) {
		int overTime = overtimeRestService.getWorkMinute(userId, Status.TYPE_OVERTIME);
		int rest = overtimeRestService.getWorkMinute(userId, Status.TYPE_REST);
		int sumMinute = overTime - rest;
		Integer typeId = null;
		for (Entry<String, String> entry : leaveMap.entrySet()) {
			if (entry.getValue().equals("加班补休"))
				typeId = Validator.getInt(entry.getKey(), R.REQUIRED, R.I);
		}

		Criteria cri = Cnd.cri();
		Calendar c  = Calendar.getInstance();
		cri.where().and("status", "=", Status.ENABLED)//
			.and("user_id", "=", userId)//
			.and("type_id", "=", typeId)//
			.and("approve", "=", Status.PROOFING)//
			.and("month(create_time)", "=", c.get(Calendar.MONTH)+1)//
			.and("year(create_time)", "=", c.get(Calendar.YEAR));
		System.out.println(c.get(Calendar.MONTH)+1);
		System.out.println(c.get(Calendar.YEAR));
		Long count = mapper.count("Leave.minute", cri);

		return (int) (count == null ? sumMinute : sumMinute - count);
	}
	
	/**
	 * 查询用户的剩余补休时长，包括补休待审批
	 * @param userId
	 * @return
	 */
	public int lastDeferredMinute(Integer userId) {
		int overTime = overtimeRestService.getWorkMinute(userId, Status.TYPE_OVERTIME);
		int rest = overtimeRestService.getWorkMinute(userId, Status.TYPE_REST);
		int sumMinute = overTime - rest;
		return sumMinute;
	}
	
	/**
	 * 是否合法的申请
	 * @param startTime
	 * @param endTime
	 * @param remarkMap
	 * @param overTimeTypeName
	 * @return
	 */
	public boolean isLegalApply(DateTime startTime, DateTime endTime, Map<String, String[]> remarkMap, String overTimeTypeName){
		if (startTime.isAfter(endTime))
			throw new Errors("开始时间不能大于结束时间");

		WorkAttendance att = dao.fetch(WorkAttendance.class, Context.getCorpId());
		Asserts.isNull(att, "最近考勤周期未配置");
		//最近一次考勤汇总日期
		DateTime lastAttTime = new DateTime(att.getEndDate());

		if (startTime.isBefore(lastAttTime))
			throw new Errors("加班日期不能小于" + lastAttTime.toString("yyyy年MM月dd日"));
		
		Criteria cri = Cnd.cri();
		cri.where().and("shift_date","=", Calendars.str(startTime, Calendars.DATE))
				   .and("user_id", "=", Context.getUserId());
		List<Shift> shifts = dao.query(Shift.class, cri);
		String midTime = "12:00";
		if(shifts != null && shifts.size() == 1){
			Shift shift = shifts.get(0);
			ShiftClass shiftClass = dao.fetch(ShiftClass.class, shift.getClasses());
			String firstM = shiftClass.getFirstMorning();//上班时间
			String secondN = shiftClass.getSecondNight();
			if(shiftClass.getNight() == ShiftC.DAY_IN && shiftClass.getSecond() != 1){ // 一头班
				secondN = shiftClass.getFirstNight();
			}
			if((firstM.compareTo(Calendars.str(endTime, "HH:mm")) < 0 && secondN.compareTo(Calendars.str(endTime, "HH:mm")) > 0) 
					|| (firstM.compareTo(Calendars.str(startTime, "HH:mm")) < 0 && secondN.compareTo(Calendars.str(startTime, "HH:mm")) > 0)){
				throw new Errors("上班时间不能申请加班");
			}
			midTime = Calendars.middleTime(firstM, secondN);
		}
		String[] marks = null;
		if(Calendars.str(startTime, "HH:mm").compareTo(midTime) < 0 && Calendars.str(endTime, "HH:mm").compareTo(midTime) > 0){
			marks = new String[] {overTimeTypeName+"加班（待审批）", overTimeTypeName+"加班（待审批）"};
		} else if (Calendars.str(startTime, "HH:mm").compareTo(midTime) < 0){
			marks = new String[] {overTimeTypeName+"加班（待审批）", null};
		} else if (Calendars.str(endTime, "HH:mm").compareTo(midTime) > 0){
			marks = new String[] {null, overTimeTypeName+"加班（待审批）"};
		}
		remarkMap.put(Calendars.str(startTime, Calendars.DATE), marks);
		return true;
	}
	
	/**
	 * 是否合法的审批
	 * @param overtime
	 * @param remarkMap
	 * @return
	 */
	public boolean isLegalApprove(Overtime overtime, Map<String, String[]> remarkMap, Integer referId, Integer approveStatus){
		WorkAttendance att = dao.fetch(WorkAttendance.class, overtime.getCorpId());
		Asserts.isNull(att, "最近考勤周期未配置");
		DateTime pos = new DateTime(att.getEndDate());
		DateTime start = new DateTime(overtime.getStartTime());
		String overtimeDate = Calendars.str(overtime.getStartTime(), Calendars.DATE);

		if (start.isBefore(pos))
			throw new Errors("考勤结束日期禁止审批");
		
		Integer approve = overtime.getApprove();
		if (referId == 0 && (approve.equals(Status.OK) || approve.equals(Status.UNAPPROVED) || approve.equals(Status.APPROVED))) {
			throw new Errors("审批流程已结束，不能再修改");
		}
		
		
		String approveMark = "（待审批）";
		if (approveStatus.equals(Status.APPROVED) || approveStatus.equals(Status.OK)){
			approveMark = "（已批准）";
		}
		if (approveStatus.equals(Status.UNAPPROVED)){
			approveMark = "（未批准）";
		}

		Map<String, String> vars = new ConcurrentHashMap<String, String>();
		vars.put("month", Calendars.str(overtime.getStartTime(), "yyyyMM"));
		List<CheckedRecord> attendances = mapper.query(CheckedRecord.class, "CheckedRecord.query", Cnd
				.where("c.user_id", "=", overtime.getUserId())
				.and("c.work_date", "=", overtimeDate), null, vars);
		if(attendances != null && attendances.size() > 0){
			CheckedRecord record = attendances.get(0);
			String remarkedIn = record.getRemarkedIn();
			if(StringUtils.isNotBlank(remarkedIn)){
				remarkedIn = remarkedIn.replaceFirst("\\（.*\\）", approveMark);
			}
			String remarkedOut = record.getRemarkedOut();
			if(StringUtils.isNotBlank(remarkedOut)){
				remarkedOut = remarkedOut.replaceFirst("\\（.*\\）", approveMark);
			}
			String[] marks = new String[] {remarkedIn, remarkedOut};
			remarkMap.put(overtimeDate, marks);
		}
		
		return true;
	}
	
	public void delete(final Overtime overtime){
		overtime.setStatus(Status.DISABLED);
		dao.update(overtime);
		Map<String, String[]> remarkMap = new ConcurrentHashMap<String, String[]>();
		isLegalApply(Calendars.parse(overtime.getStartTime(), Calendars.DATE_TIMES), Calendars.parse(overtime.getEndTime(), Calendars.DATE_TIMES), remarkMap, "");
		checkedRecordService.delete(overtime.getUserId(), remarkMap, Context.getUserId());
	}
}
