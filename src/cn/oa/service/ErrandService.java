package cn.oa.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Status;
import cn.oa.model.Errand;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.WorkDay;
import cn.oa.repository.Mapper;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.except.Errors;
import cn.oa.web.Context;

@IocBean
public class ErrandService {
	
	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private CheckedRecordService checkedRecordService;
	
	public void save(final Integer errandId, final Errand errand, final DateTime orgStart, final DateTime orgEnd,
			final WorkDay day, final String[] weeks, final Map<String, Integer[]> monthMap,
			final Integer modifyId, final DateTime now) {
		Asserts.isNull(day, "日排班不能为空值");
		Asserts.isEmpty(weeks, "周排班不能为空值");
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (errandId != null)
					dao.update(errand);
				else
					dao.insert(errand);
				String startStr = new DateTime(errand.getStartTime()).toString(Calendars.DATE_TIME);
				String endStr = new DateTime(errand.getEndTime()).toString(Calendars.DATE_TIME);
				String approve = "（待审批）";
				if (errand.getApprove().equals(Status.APPROVED)) approve = "（已批准）";
				if (errand.getApprove().equals(Status.UNAPPROVED)) approve = "（未批准）";
				
				if (orgStart != null && orgEnd != null) {
					String orgStartStr = orgStart.toString(Calendars.DATE_TIME);
					String orgEndStr = orgEnd.toString(Calendars.DATE_TIME);
					checkedRecordService.update(errand.getUserId(), orgStartStr, orgEndStr, null, day, weeks, monthMap, modifyId, now.toDate());			
				}
				checkedRecordService.update(errand.getUserId(), startStr, endStr, "出差" + approve, day, weeks, monthMap, modifyId, now.toDate());
			}
		});
	}	
	
	//遍历排班表,获取请假时间
	public String[] shift(String start_yyyyMMdd,String end_yyyyMMdd,Map<String, String[]> dayMap,String typeName,Integer errarId) {
		List<Shift> shifts = dao.query(Shift.class, Cnd.where("shift_date",">=",start_yyyyMMdd).and("shift_date", "<=", end_yyyyMMdd).and("user_id", "=", errarId));
		DateTime startDate = Calendars.parse(start_yyyyMMdd, Calendars.DATE);
		DateTime endDate = Calendars.parse(end_yyyyMMdd, Calendars.DATE);
		String startStr = null;
		String endStr = null;
		if(shifts.size() == 0){
			throw new Errors("该时间段没有排班");
		}else{
			for(Shift s:shifts){
				if(s.getClasses()==null){continue;}
				ShiftClass shiftClass = dao.fetch(ShiftClass.class,  s.getClasses());
				if(shiftClass == null){continue;}
				String checkIn = null;
				String checkOut = null;
				//二头班
				if(shiftClass.getSecond()==1){
					checkIn = shiftClass.getFirstMorning();
					checkOut = shiftClass.getSecondNight();
				}else{
					checkIn = shiftClass.getFirstMorning();
					checkOut = shiftClass.getFirstNight();
				}						
				int days = Days.daysBetween(startDate, endDate).getDays();
				dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName,typeName});	
				if(days ==0){										
					startStr = start_yyyyMMdd + " " + checkIn;
					endStr = end_yyyyMMdd + " " + checkOut;
				}else if(days>0){
					if(startDate.toDate() == s.getShiftDate()||startDate.toDate().equals(s.getShiftDate())){
						startStr = start_yyyyMMdd + " " + checkIn;
					}else if(endDate.toDate() == s.getShiftDate()||endDate.toDate().equals(s.getShiftDate())){
						endStr = end_yyyyMMdd + " " + checkOut;
					}
				}else{
					throw new Errors("申请时间出错");
				}
			}
		}
		String[] time = {startStr,endStr};
		return time;
	}	
	
	public void delete(final Errand errand) {
		final Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
		shift(Calendars.str(errand.getStartTime(),Calendars.DATE), Calendars.str(errand.getEndTime(),Calendars.DATE),  dayMap,  "出差（待审批）",Context.getUserId());
		Trans.exec(new Atom() {
			@Override
			public void run() {
				errand.setStatus(Status.DISABLED);
				dao.update(errand);
				checkedRecordService.delete(errand.getUserId(), dayMap, Context.getUserId());
			}
		});
	}

}
