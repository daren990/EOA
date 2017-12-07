package cn.oa.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Status;
import cn.oa.model.Outwork;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.WorkDay;
import cn.oa.repository.Mapper;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.except.Errors;
import cn.oa.web.Context;

@IocBean
public class OutworkService {
	
	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private CheckedRecordService checkedRecordService;
	
	public void save(final Integer outworkId, final Outwork outwork, final DateTime orgStart, final DateTime orgEnd,
			final WorkDay day, final String[] weeks, final Map<String, Integer[]> monthMap,
			final Integer modifyId, final DateTime now) {
		Asserts.isNull(day, "日排班不能为空值");
		Asserts.isEmpty(weeks, "周排班不能为空值");
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (outworkId != null)
					dao.update(outwork);
				else
					dao.insert(outwork);
				String startStr = new DateTime(outwork.getStartTime()).toString(Calendars.DATE_TIME);
				String endStr = new DateTime(outwork.getEndTime()).toString(Calendars.DATE_TIME);
				String approve = "（待审批）";
				if (outwork.getApprove().equals(Status.APPROVED)) approve = "（已批准）";
				if (outwork.getApprove().equals(Status.UNAPPROVED)) approve = "（未批准）";
				
				if (orgStart != null && orgEnd != null) {
					String orgStartStr = orgStart.toString(Calendars.DATE_TIME);
					String orgEndStr = orgEnd.toString(Calendars.DATE_TIME);
					checkedRecordService.update(outwork.getUserId(), orgStartStr, orgEndStr, null, day, weeks, monthMap, modifyId, now.toDate());			
				}
				checkedRecordService.update(outwork.getUserId(), startStr, endStr, "外勤" + approve, day, weeks, monthMap, modifyId, now.toDate());
			}
		});
	}
	
	//遍历排班表,获取外勤时间
	public String[] shift(String work_yyyyMMdd,Map<String, String[]> dayMap,String typeName,Integer applyUserId,Integer type) {
		Shift shift = dao.fetch(Shift.class, Cnd.where("shift_date","=",work_yyyyMMdd).and("user_id", "=", applyUserId));		
		String startStr = null;
		String endStr = null;
		//请假分钟数
		if(shift == null){
			throw new Errors("该时间段没有排班");
		}else{
			if(shift.getClasses()==null){throw new Errors("没有找到班次");}
			ShiftClass shiftClass = dao.fetch(ShiftClass.class, shift.getClasses());
			if(shiftClass == null){throw new Errors("没有找到班次");}
			String checkIn = null;
			String checkOut = null;
			String restIn = null;
			String restOut = null;
			//二头班
			if(shiftClass.getSecond()==1){
				checkIn = shiftClass.getFirstMorning();
				restIn = shiftClass.getFirstNight();
				restOut = shiftClass.getSecondMorning();
				checkOut = shiftClass.getSecondNight();
				if(type.equals(0)||type == 0){										
					startStr = work_yyyyMMdd + " " + checkIn;
					endStr = work_yyyyMMdd + " " + restIn;
					dayMap.put( Calendars.str(shift.getShiftDate(),Calendars.DATE), new String[] {typeName,null});
				}else if(type.equals(1)||type == 1){
					startStr = work_yyyyMMdd + " " + restOut;
					endStr = work_yyyyMMdd + " " + checkOut;
					dayMap.put( Calendars.str(shift.getShiftDate(),Calendars.DATE), new String[] {null,typeName});
				}else{
					throw new Errors("申请时间出错");
				}
			}else{
				checkIn = shiftClass.getFirstMorning();
				checkOut = shiftClass.getFirstNight();
				startStr = work_yyyyMMdd + " " + checkIn;
				endStr = work_yyyyMMdd + " " + checkOut;
				dayMap.put( Calendars.str(shift.getShiftDate(),Calendars.DATE), new String[] {typeName,typeName});
			}
		}
		String[] time = {startStr, endStr};
		return time;
	}
	
	public void delete(final Outwork outwork){
		final Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
		shift(Calendars.str(outwork.getStartTime(),Calendars.DATE), dayMap,  null,Context.getUserId(),outwork.getType());
		Trans.exec(new Atom() {
			@Override
			public void run() {
				outwork.setStatus(Status.DISABLED);
				dao.update(outwork);
				checkedRecordService.delete(outwork.getUserId(), dayMap, Context.getUserId());
			}
		});
	}

}
