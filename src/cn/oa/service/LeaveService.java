package cn.oa.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Status;
import cn.oa.model.Annual;
import cn.oa.model.Leave;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.WorkDay;
import cn.oa.repository.Mapper;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.web.Context;

@IocBean
public class LeaveService {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private CheckedRecordService checkedRecordService;
	
	public void save(final Leave leave, final DateTime orgStart, final DateTime orgEnd, final String typeName,
			final WorkDay day, final String[] weeks, final Map<String, Integer[]> monthMap,
			final Integer modifyId, final DateTime now, final Integer approve) {
			Asserts.isNull(day, "日排班不能为空值");
			Asserts.isEmpty(weeks, "周排班不能为空值");
			
			String startStr = new DateTime(leave.getStartTime()).toString(Calendars.DATE_TIME);
			String endStr = new DateTime(leave.getEndTime()).toString(Calendars.DATE_TIME);
			String approve1 = "（待审批）";
			if (approve.equals(Status.APPROVED)||approve.equals(Status.OK)) approve1 = "（已批准）";
			if (approve.equals(Status.UNAPPROVED)) approve1 = "（未批准）";
				
			if (orgStart != null && orgEnd != null) {
				String orgStartStr = orgStart.toString(Calendars.DATE_TIME);
				String orgEndStr = orgEnd.toString(Calendars.DATE_TIME);
				checkedRecordService.update(leave.getUserId(), orgStartStr, orgEndStr, null, day, weeks, monthMap, modifyId, now.toDate());			
			}
			checkedRecordService.update(leave.getUserId(), startStr, endStr, typeName + approve1, day, weeks, monthMap, modifyId, now.toDate());
		}
	
	public void save(final Leave leave, final String typeName, final Integer modifyId, final Integer approve, Map<String, String[]>  remarkMap) {
		String approve1 = "（待审批）";
		if (approve.equals(Status.APPROVED)||approve.equals(Status.OK)) approve1 = "（已批准）";
		if (approve.equals(Status.UNAPPROVED)) approve1 = "（未批准）";
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			String[] remarks = entry.getValue();
			if(remarks[0]!=null){remarks[0] = typeName+approve1;}
			if(remarks[1]!=null){remarks[1] = typeName+approve1;}
			remarkMap.put(entry.getKey(), remarks);
		}
		checkedRecordService.update3(leave.getUserId(), modifyId, remarkMap);
	}
	
	public void oparateSave(final Leave leave, final String typeName,
		final Integer modifyId, final DateTime now, final Integer approve, Map<String, String[]>  remarkMap) {
		String startStr = new DateTime(leave.getStartTime()).toString(Calendars.DATE_TIME);
		String endStr = new DateTime(leave.getEndTime()).toString(Calendars.DATE_TIME);
		String approve1 = "（待审批）";
		if (approve.equals(Status.APPROVED)||approve.equals(Status.OK)) approve1 = "（已批准）";
	    //	UNAPPROVED = -1;	// 未批准
		if (approve.equals(Status.UNAPPROVED)) approve1 = "（未批准）";
		for (Entry<String, String[]> entry : remarkMap.entrySet()) {
			String[] remarks = entry.getValue();
			if(remarks[0]!=null){remarks[0] = typeName+approve1;}
			if(remarks[1]!=null){remarks[1] = typeName+approve1;}
			remarkMap.put(entry.getKey(), remarks);
		}
		if(approve == Status.PROOFING||approve == Status.UNAPPROVED){
			checkedRecordService.update3(leave.getUserId(), modifyId, remarkMap);		
		}else{	
			checkedRecordService.updateNewLeave(leave.getUserId(), startStr, endStr, typeName + approve1, remarkMap, modifyId, now.toDate());
		}
	}
	
	/**
	 * 删除请假单
	 * @param leave
	 */
	public void delete(final Leave leave) {
		final Map<String, String[]> dayMapOld = new ConcurrentHashMap<String, String[]>();
		Asserts.isNull(leave, "申请不存在");
		Asserts.notEqOr(leave.getApprove(), Status.APPROVED, Status.OK, "禁止修改已审批的请假申请");
		//修改前的排班
		List<Shift> shiftsOld = dao.query(Shift.class, Cnd.where("shift_date",">=",Calendars.str(leave.getStartTime(), Calendars.DATE)).and("shift_date", "<=", Calendars.str(leave.getEndTime(), Calendars.DATE)).and("user_id", "=", Context.getUserId()));
		if(shiftsOld.size() == 0){
			throw new Errors("没找到排班,无法删除");
		}else{
			for(Shift s:shiftsOld){
				if(s.getClasses()==null){continue;}
				ShiftClass shiftClass = dao.fetch(ShiftClass.class, s.getClasses());
				if(shiftClass == null){continue;}
				DateTime startDate = Calendars.parse(leave.getStartTime(), Calendars.DATE);
				DateTime endDate = Calendars.parse(leave.getEndTime(), Calendars.DATE); 
				int days = Days.daysBetween(startDate, endDate).getDays();
				if(days ==0){
					dayMapOld.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {null,null});
				}else if(days>0){
					if(startDate.toDate() == s.getShiftDate()||startDate.toDate().equals(s.getShiftDate())){
						dayMapOld.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {null,null});
					}else if(endDate.toDate() == s.getShiftDate()||endDate.toDate().equals(s.getShiftDate())){
						dayMapOld.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {null,null});
					}
				}
			}
		}
		Trans.exec(new Atom() {
			@Override
			public void run() {
				leave.setStatus(Status.DISABLED);
				dao.update(leave);
				checkedRecordService.update3(leave.getUserId(), Context.getUserId(), dayMapOld);		
			}
		});
	}
	
	public int lastAnnualMinute(Integer userId, Integer leaveId, DateTime startTime, DateTime endTime, Map<String, String> leaveMap) {
		Annual annual = dao.fetch(Annual.class, Cnd.where("status", "=", Status.ENABLED).and("userId", "=", userId));
		if (annual == null) return 0;
		
		DateTime annualStart = new DateTime(annual.getStartDate());
		DateTime annualEnd = new DateTime(annual.getEndDate());

		if ((startTime != null && startTime.isBefore(annualStart))|| (endTime != null && endTime.isAfter(annualEnd))) {
			throw new Errors("申请年休假只能介于" + annualStart.toString("yyyy年MM月dd日") + "到" + annualEnd.toString("yyyy年MM月dd日") + "之间");
		}
		
		Integer typeId = null;
		for (Entry<String, String> entry : leaveMap.entrySet()) {
			if (entry.getValue().equals("年休假"))
				typeId = Validator.getInt(entry.getKey(), R.REQUIRED, R.I);
		}
		
		Criteria cri = Cnd.cri();
		cri.where().and("status", "=", Status.ENABLED)
			.and("user_id", "=", userId)
			.and("type_id", "=", typeId)
			.and("approve", "in", new int[] { Status.PROOFING, Status.APPROVED, Status.OK })
			.and("start_time", ">=", annualStart.toString(Calendars.DATE) + " 00:00:00")
			.and("end_time", "<=", annualEnd.toString(Calendars.DATE) + " 23:59:59");

		if (leaveId != null) {
			cri.where().and("leave_id", "!=", leaveId);
		}

		Long count = mapper.count("Leave.minute", cri);
				
		return (int) (count == null ? annual.getSumMinute() : annual.getSumMinute() - count);
	}
}
