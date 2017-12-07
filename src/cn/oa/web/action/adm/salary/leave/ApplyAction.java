package cn.oa.web.action.adm.salary.leave;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.DomainName;
import cn.oa.consts.Roles;
import cn.oa.consts.ShiftC;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.consts.WxOpen;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Dict;
import cn.oa.model.Leave;
import cn.oa.model.LeaveActor;
import cn.oa.model.LeaveType;
import cn.oa.model.Org;
import cn.oa.model.Role;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.ShiftCorp;
import cn.oa.model.User;
import cn.oa.model.WechatStaffmatching;
import cn.oa.model.WorkAttendance;
import cn.oa.model.WorkDay;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.helper.Works;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
import cn.oa.web.action.wx.WxSendService;
import cn.oa.web.action.wx.comm.WxRoleUtil;

/**
 * 请假申请
 * @author SimonTang
 */
@IocBean(name = "adm.salary.leave.apply")
@At(value = "/adm/salary/leave/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	
	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
//		System.out.println(token);
		CSRF.generate(req, "/adm/salary/leave/apply/del", token);
		CSRF.generate(req, "/adm/salary/leave/approve/actors", token);
		CSRF.generate(req, "/adm/salary/leave/approve/add", token);
		CSRF.generate(req, "/adm/salary/leave/approve/wxadd", token);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		System.out.println(startStr);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		System.out.println(endStr);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		System.out.println(approve);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("l.status", "=", Status.ENABLED)
				.and("l.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "l.approve", "approve", approve);
		Cnds.gte(cri, mb, "l.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "l.create_time", "endTime", endStr);
		cri.getOrderBy().desc("l.modify_time");

		Page<Leave> page = Webs.page(req);
		page = mapper.page(Leave.class, page, "Leave.count", "Leave.index", cri);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/leave/apply_page_wx")
	public void wxpage(HttpServletRequest req){
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/leave/apply_page")
	public void page(HttpServletRequest req){
		pageUtil(req);
	}

	
	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);		
		Integer leaveId = Https.getInt(req, "leaveId", R.I);
		String mTime = null;
		String nTime = null;
		Leave leave = null;
		if (leaveId != null)
			leave = dao.fetch(Leave.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("userId", "=", Context.getUserId())
					.and("leaveId", "=", leaveId));
		if (leave!= null) {
			mTime = Calendars.str(leave.getStartTime(),"HH:mm");
			nTime = Calendars.str(leave.getEndTime(),"HH:mm");
			// 审批
			LeaveActor actor = dao.fetch(LeaveActor.class, Cnd
					.where("leaveId", "=", leaveId)
					.and("step", "=", 1)
					.limit(1)
					.asc("modifyTime"));
			if (actor != null) {
				//审批人id
				leave.setActorId(actor.getActorId());
			}
		} else {
			leave = new Leave();
		}
		
		List<User> operators = null;
		
		User manager = dao.fetch(User.class, Context.getUser().getManagerId());
		if(manager != null){
			operators = new ArrayList<User>();
			operators.add(manager);
		} else{
			//返回等级大于当前用户的所有上级
			operators = userService.operators(Context.getCorpId(), Context.getLevel());
		}
		String name = req.getParameter("name");
		if(StringUtils.isNotBlank(name)){
			String date = req.getParameter("date");
			Shift shift = mapper.fetch(Shift.class, "Shiftinner.query",Cnd.where("s.user_id", "=", Context.getUser().getUserId()).and("s.shift_date", "=", date));
			ShiftClass shiftClass = dao.fetch(ShiftClass.class, shift.getClasses());
			if(name.equals("checkedIn")){
				leave.setStartTime(Calendars.parse(date+" "+shiftClass.getFirstMorning(), Calendars.DATE_TIME).toDate());
				leave.setEndTime(Calendars.parse(date+" "+shiftClass.getFirstNight(), Calendars.DATE_TIME).toDate());
			}else{
				leave.setStartTime(Calendars.parse(date+" "+shiftClass.getSecondMorning(), Calendars.DATE_TIME).toDate());
				leave.setEndTime(Calendars.parse(date+" "+shiftClass.getSecondNight(), Calendars.DATE_TIME).toDate());
			}
		}
		req.setAttribute("mTime", mTime);
		req.setAttribute("nTime", nTime);
		req.setAttribute("operators", operators);
		req.setAttribute("leave", leave);
		req.setAttribute("leaveMap", dictService.map(Dict.LEAVE));
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/leave/apply_add")
	public void add(HttpServletRequest req){		
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/leave/apply_add_wx")
	public void wxadd(HttpServletRequest req){
		addUtil(req);
	}
	private Object newAddPostUtil(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer leaveId = null;
		try {
			CSRF.validate(req);
			leaveId = Https.getInt(req, "leaveId", R.I);
			Integer typeId = Https.getInt(req, "typeId", R.REQUIRED, R.I, "请假类型");
			String start_yyyyMMdd = Https.getStr(req, "start_yyyyMMdd",  R.D, "开始时间");
			String end_yyyyMMdd = Https.getStr(req, "end_yyyyMMdd",  R.D, "结束时间");
			String startTypes = Https.getStr(req, "startType", "请假开始时段");
			String endTypes = Https.getStr(req, "endType", "请假结束时段");
			String date_yyyyMMdd = Https.getStr(req, "date_yyyyMMdd",  R.D, "请假日期");
			String timeInterval = Https.getStr(req, "timeInterval", "请假长度");
            
			String disappear_startTypes = Https.getStr(req, "disappear_startType", "销假开始时段");
			String disappear_endTypes = Https.getStr(req, "disappear_endType", "销假结束时段");
			
			
			if(timeInterval != null && !timeInterval.equals("") && Double.parseDouble(timeInterval) % 0.5 > 0) throw new Errors("请假长度以半天为单位进行输入");
		
			//判断是否根据date_yyyyMMdd和timeInterval来自动计算start_yyyyMMdd, end_yyyyMMdd, startTypes, endTypes
			if(validateLeaveTime(start_yyyyMMdd, end_yyyyMMdd, startTypes, endTypes)){
				Map<String, String> map = handleLeaveTime(date_yyyyMMdd, timeInterval);
				if(map != null){
					start_yyyyMMdd = map.get("start_yyyyMMdd");
					end_yyyyMMdd = map.get("end_yyyyMMdd");
					startTypes = map.get("startTypes");
					endTypes = map.get("endTypes");
				}else{
					throw new Errors("请输入正确的请假时间");
				}
			}
			Integer startType = null;
			Integer endType = null;
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "reason:请假原因");
			String titleName = Https.getStr(req, "titleName", "{1,60}");			
			Integer actorId = Https.getInt(req, "operatorId", R.REQUIRED, R.I, "operatorId:上级审批");
			String actorName = Https.getStr(req, actorId.toString(), "{1,60}");
			
			Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
			Map<String, String[]> dayMapOld = new ConcurrentHashMap<String, String[]>();
			String startStr = null;
			String endStr = null;
			DateTime startDate = Calendars.parse(start_yyyyMMdd, Calendars.DATE);
			DateTime endDate = Calendars.parse(end_yyyyMMdd, Calendars.DATE);
			Leave leave = null;
			DateTime now = new DateTime();
			List<Shift> shiftsOld = null;
			//请假类型
			Map<String, String> leaveMap = dictService.map(Dict.LEAVE);
			String typeName = leaveMap.get(String.valueOf(typeId));
			System.out.println(typeId);
			if (leaveId != null) {
				leave = dao.fetch(Leave.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("leaveId", "=", leaveId));
				Asserts.isNull(leave, "申请不存在");
				Asserts.notEqOr(leave.getApprove(), Status.APPROVED, Status.OK, "禁止修改已审批的请假申请");
				//修改前的排班
				shiftsOld = dao.query(Shift.class, Cnd.where("shift_date",">=",Calendars.str(leave.getStartTime(), Calendars.DATE)).and("shift_date", "<=", Calendars.str(leave.getEndTime(), Calendars.DATE)).and("user_id", "=", Context.getUserId()));
				if(shiftsOld.size() == 0){
		//			throw new Errors("之前的排班已被修改,无法更改");
				}else{
					for(Shift s:shiftsOld){
						if(s.getClasses()==null){continue;}
						ShiftClass shiftClass = dao.fetch(ShiftClass.class, s.getClasses());
						if(shiftClass == null){continue;}
							dayMapOld.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {null,null});
						
					}
				}
			} else {
				leave = new Leave();
				leave.setStatus(Status.ENABLED);
				leave.setCreateTime(now.toDate());
			}
			DateTime mTime = Calendars.parse(start_yyyyMMdd+" "+startTypes, Calendars.DATE_TIME);
			DateTime nTime = Calendars.parse(end_yyyyMMdd+" "+endTypes, Calendars.DATE_TIME);	
			Criteria cri = Cnd.cri();
			cri.where().and("shift_date",">=",start_yyyyMMdd)
					   .and("shift_date", "<=", end_yyyyMMdd)
					   .and("user_id", "=", Context.getUserId());
			cri.getOrderBy().asc("shift_date");
			List<Shift> shifts = dao.query(Shift.class, cri);
			boolean first = true;
			
			//请假分钟数
			int minute = 0;
			if(shifts.size() == 0){	
		//		throw new Errors("该请假时间段没有排班");
				startStr = start_yyyyMMdd + " " + startTypes;
				endStr = end_yyyyMMdd + " " + endTypes;

				
			}else{
				for(Shift s:shifts){
					if(s.getClasses()==null){continue;}
					ShiftClass shiftClass = dao.fetch(ShiftClass.class, s.getClasses());
					if(shiftClass == null){continue;}
					if(shiftClass.getNight() == ShiftC.NIGHT_IN){
//						throw new Errors("夜班不能申请请假");
					}
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
						System.out.println("checkIn:"+checkIn+"checkOut:"+checkOut+"restIn:"+restIn+"restOut:"+restOut);
						System.out.println(startTypes);
						if(first){
							//开始的时间确定是早上还是下午
							startType = (startTypes.compareTo(restIn)<=0)?Leave.AM:Leave.PM;
							if((startTypes.compareTo(restIn)>0&&startTypes.compareTo(restOut)<0)||startTypes.compareTo(checkIn)<0||startTypes.compareTo(checkOut)>0)
	//							throw new Errors("开始时间段不正确");
							
							first = false;
						}
													
						//获取上午还是下午时间段
						endType = (endTypes.compareTo(restIn)<=0)?Leave.AM:Leave.PM;
								
						int days = Days.daysBetween(startDate, endDate).getDays();
						if(days ==0){
							if((endTypes.compareTo(restIn)>0&&endTypes.compareTo(restOut)<0)||endTypes.compareTo(checkIn)<0||endTypes.compareTo(checkOut)>0){
								//		throw new Errors("结束时间段不正确33");
							}
							startStr = start_yyyyMMdd + " " + startTypes;
							endStr = end_yyyyMMdd + " " + endTypes;
							
							if(startType.equals(Leave.AM)&&endType.equals(Leave.AM)){
								minute += Minutes.minutesBetween(mTime, nTime).getMinutes();
								dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",null});
							}
							else if(startType.equals(Leave.AM)&&endType.equals(Leave.PM)){
								minute += Works.workMinute(startTypes, endTypes, restIn, restOut);
								dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",typeName+"（待审批）"});
							}
							else if(startType.equals(Leave.PM)&&endType.equals(Leave.PM)){
								minute += Minutes.minutesBetween(mTime, nTime).getMinutes();
								dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {null,typeName+"（待审批）"});							
							}
							else{
								throw new Errors("申请时间出错");
							}
						}else if(days>0){
							if(startDate.toDate() == s.getShiftDate()||startDate.toDate().equals(s.getShiftDate())){
								startStr = start_yyyyMMdd + " " + (startType.equals(Leave.AM) ? checkIn : restOut);
								if(startType.equals(Leave.AM)){	
									minute += Works.workMinute(startTypes, checkOut, restIn, restOut);
									dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",typeName+"（待审批）"});
								}
								else if(startType.equals(Leave.PM)){									
									minute += Minutes.minutesBetween(mTime, Calendars.parse(start_yyyyMMdd+" " + checkOut, Calendars.DATE_TIME)).getMinutes();
									dayMap.put(Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {null,typeName+"（待审批）"});
								}
								else{
									throw new Errors("申请时间出错");
								}
							}else if(endDate.toDate() == s.getShiftDate()||endDate.toDate().equals(s.getShiftDate())){
								if((endTypes.compareTo(restIn)>0&&endTypes.compareTo(restOut)<0)||endTypes.compareTo(checkIn)<0||endTypes.compareTo(checkOut)>0){
									//	throw new Errors("结束时间段不正确222");
								}
								endStr = end_yyyyMMdd + " " + (endType.equals(Leave.AM) ? restIn : checkOut);
								if(endType.equals(Leave.AM)){	
									minute += Minutes.minutesBetween(Calendars.parse(end_yyyyMMdd+" " + checkIn, Calendars.DATE_TIME), nTime).getMinutes();
									dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",null});
								}
								else if(endType.equals(Leave.PM)){
									minute += Works.workMinute(checkIn, endTypes, restIn, restOut);
									dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",typeName+"（待审批）"});
								}
								else{
									throw new Errors("申请时间出错");
								}
							}else{
								dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",typeName+"（待审批）"});
								minute += Works.workMinute(checkIn, checkOut, restIn, restOut);
							}
						}else{
							throw new Errors("申请时间出错");
						}
					}else{
						checkIn = shiftClass.getFirstMorning();
						checkOut = shiftClass.getFirstNight();
						if(first){
							startType = Leave.AM;
							if(startTypes.compareTo(checkIn)<0||startTypes.compareTo(checkOut)>0)
								throw new Errors("开始时间段不正确");
							first = false;
						}
						
								
						
						endType = Leave.PM;
						checkIn = shiftClass.getFirstMorning();
						checkOut = shiftClass.getFirstNight();
						
						int days = Days.daysBetween(startDate, endDate).getDays();
						if(days ==0){
								if(endTypes.compareTo(checkIn)<0||endTypes.compareTo(checkOut)>0)
									throw new Errors("结束时间段不正确");
								startStr = start_yyyyMMdd + " " + startTypes;
								endStr = end_yyyyMMdd + " " + endTypes;
								minute += Minutes.minutesBetween(mTime, nTime).getMinutes();
								dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",typeName+"（待审批）"});
						}else if(days>0){
							if(startDate.toDate() == s.getShiftDate()||startDate.toDate().equals(s.getShiftDate())){
								startStr = start_yyyyMMdd + " " + startTypes;	
								minute += Minutes.minutesBetween(mTime, Calendars.parse(start_yyyyMMdd+" " + checkOut, Calendars.DATE_TIME)).getMinutes();
								dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",typeName+"（待审批）"});								
							}else if(endDate.toDate() == s.getShiftDate()||endDate.toDate().equals(s.getShiftDate())){
								if(endTypes.compareTo(checkIn)<0||endTypes.compareTo(checkOut)>0)
									throw new Errors("结束时间段不正确");
								endStr = end_yyyyMMdd + " " + endTypes;
								minute += Minutes.minutesBetween(Calendars.parse(end_yyyyMMdd+" " + checkIn, Calendars.DATE_TIME), nTime).getMinutes();
								dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",typeName+"（待审批）"});
							}else{
								DateTime checkInTime = Calendars.parse(start_yyyyMMdd+" " + checkIn, Calendars.DATE_TIME);
								DateTime checkOutTime = Calendars.parse(start_yyyyMMdd+" " + checkOut, Calendars.DATE_TIME);
								minute += Minutes.minutesBetween(checkInTime, checkOutTime).getMinutes();
								dayMap.put( Calendars.str(s.getShiftDate(),Calendars.DATE), new String[] {typeName+"（待审批）",typeName+"（待审批）"});
							}
						}else{
							throw new Errors("申请时间出错");
						}
					}
					
				}
			}
			
			
			WorkAttendance att = dao.fetch(WorkAttendance.class, Context.getCorpId());
//			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());
//			if(startStr == null || endStr == null)throw new Errors("申请时间出错,前后时间段没有配置排班");
			DateTime start = Calendars.parse(startStr, Calendars.DATE_TIME);
			System.out.println(startStr);
			DateTime end = Calendars.parse(endStr, Calendars.DATE_TIME);
			System.out.println(endStr);
	//		if (start.isBefore(pos))
	//			throw new Errors("开始时间不能小于" + pos.toString("yyyy年MM月dd日"));
			if (start.isAfter(end))
				throw new Errors("开始时间不能大于结束时间");

			
			if (typeName.equals("年休假")) {
				int lastMinute = leaveService.lastAnnualMinute(Context.getUserId(), leaveId, start, end, leaveMap);
				if (lastMinute <= 0)
					throw new Errors("不能申请年休假");
				if (minute > lastMinute)
					throw new Errors("申请年休假不能大于" + (lastMinute/60) + "小时");
			}
			if (typeName.equals("加班补休")) {
				int lastMinute = overtimeService.lastDeferredMinute(Context.getUserId(), leaveId, start, end, leaveMap);
				System.out.println(lastMinute);
				if (lastMinute <= 0)
					throw new Errors("不能申请加班补休");
				System.out.println(minute);
				if (minute > lastMinute)
					throw new Errors("申请加班补休不能大于" + lastMinute + "分钟");
			}
			
			StringBuilder buff = new StringBuilder();
				buff.append(Context.getTrueName())
				.append("于")
				.append(Calendars.str(now,Calendars.DATE))
				.append("申请请假");
			
			leave.setUserId(Context.getUserId());
			leave.setSubject(buff.toString());
			leave.setTypeId(typeId);
			leave.setStartTime(mTime.toDate());
			leave.setEndTime(nTime.toDate());
			leave.setLeaveMinute(minute);
//			System.out.println("minute:---------------------"+minute);
			leave.setReason(reason);
			leave.setApprove(Status.PROOFING);
			leave.setModifyTime(now.toDate());
			leave.setStartType(startType);
			leave.setEndType(endType);
			//得到上级的所拥有的所有角色
			String [] str=userService.findRoleNames(actorId);
			
			String variable=null;
		
			if(Asserts.hasAny(Roles.BOSS.getName(),str)){
				variable=Roles.BOSS.getName();
			}
			else if(Asserts.hasAny(Roles.GM.getName(),str)){
				variable=Roles.GM.getName();
			}
			else if(Asserts.hasAny(Roles.MAN.getName(),str)){
				variable=Roles.MAN.getName();
			}
			else if(Asserts.hasAny(Roles.SVI.getName(),str)){
				variable=Roles.SVI.getName();
			}
			else if(Asserts.hasAny(Roles.EMP.getName(),str)){
				variable=Roles.EMP.getName();
			}
			else {
				variable=Roles.EMP.getName();
			}
			
			String atitle=titleName+"申请请假";
			
			String url = null;
			url = "http://"+DomainName.OA+"/"+WxOpen.LEAVE_APPLY.getURL();


			transSave(leaveId, leave, actorId,variable,start, end, Context.getUserId(), typeName, dayMap,dayMapOld,atitle,url,actorName);	

			
			Code.ok(mb, (leaveId == null ? "新建" : "编辑") + "请假申请成功");
					
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (leaveId == null ? "新建" : "编辑") + "请假申请失败");
		}

		return mb;
	}
	
	//根据date_yyyyMMdd和timeInterval计算出start_yyyyMMdd、end_yyyyMMdd、startTypes、endTypes
	private Map<String, String> handleLeaveTime(String date_yyyyMMdd, String timeInterval){
		if(date_yyyyMMdd == null || date_yyyyMMdd.equals("") || timeInterval == null || timeInterval.equals("")){
			throw new Errors("请输入正确的请假时间");
		}
		Map<String, String> map = new HashMap<String, String>();
		DateTime startDate = Calendars.parse(date_yyyyMMdd, Calendars.DATE);
		double length = Double.parseDouble(timeInterval);
		DateTime endDate = startDate.plusDays((int)Math.ceil(length - 1));
		System.out.println(endDate);
		Criteria cri = Cnd.cri();
		cri.where().and("shift_date",">=",Calendars.str(startDate, Calendars.DATE)+" 00:00:00")
				   .and("shift_date", "<=", Calendars.str(endDate, Calendars.DATE)+" 23:59:59")
				   .and("user_id", "=", Context.getUserId());
		cri.getOrderBy().asc("shift_date");
		List<Shift> shifts = dao.query(Shift.class, cri);
		if(shifts.size() == 0){
//			throw new Errors("该请假时间段没有排班");
			
		
		}else{
			ShiftClass startShiftClass = null;
			ShiftClass endShiftClass = null;
			Date endShiftDate = null;
			Integer classId = null;
			for(int x = 0; x < shifts.size(); x++){
				if(shifts.get(x).getClasses() == null) continue;
				startShiftClass = dao.fetch(ShiftClass.class, shifts.get(x).getClasses());
				if(startShiftClass == null){continue;}
				classId = startShiftClass.getClassId();
				break;
			}
			for(int x = shifts.size() - 1; x >= 0; x--){
				if(shifts.get(x).getClasses() == null) continue;
				if(shifts.get(x).getClasses().equals(classId)){
					endShiftClass = startShiftClass;
				}else{
					endShiftClass = dao.fetch(ShiftClass.class, shifts.get(x).getClasses());
					if(endShiftClass == null){continue;}
				}
				endShiftDate = shifts.get(x).getShiftDate();
				break;
			}
//			if(startShiftClass == null) throw new Errors("找不到开始时间对应的班次");
//			if(endShiftClass == null) throw new Errors("找不到结束时间对应的班次");
			map.put("start_yyyyMMdd", date_yyyyMMdd);
			map.put("end_yyyyMMdd", Calendars.str(endDate, Calendars.DATE));
			map.put("startTypes", startShiftClass.getFirstMorning());
			if((Double.parseDouble(timeInterval) / 0.5) % 2 == 0){
				map.put("endTypes", endShiftClass.getSecondNight() != null? endShiftClass.getSecondNight() : endShiftClass.getFirstNight());
			}else{
				if(endShiftClass.getSecond().equals(0)){//一头班的情况
					String startStr = Calendars.str(endShiftDate, Calendars.DATE) + " " + endShiftClass.getFirstMorning();
					String endStr = Calendars.str(endShiftDate, Calendars.DATE) + " " + endShiftClass.getFirstNight();
					DateTime start = Calendars.parse(startStr, Calendars.DATE_TIME);
					DateTime end = Calendars.parse(endStr, Calendars.DATE_TIME);
					Long subtract = end.toDate().getTime() - start.toDate().getTime();
					System.out.println(Calendars.str(start.plus(subtract/2), Calendars.DATE_TIME));
					map.put("endTypes", Calendars.str(start.plus(subtract), "HH:mm"));
				}else{//二头班的情况
					map.put("endTypes", endShiftClass.getFirstNight());	
				}
			}
			if(map.containsKey("start_yyyyMMdd") && map.get("start_yyyyMMdd") != null 
					&& map.containsKey("end_yyyyMMdd")  && map.get("end_yyyyMMdd") != null 
					&& map.containsKey("startTypes")  && map.get("startTypes") != null 
					&& map.containsKey("endTypes") && map.get("endTypes") != null ){
				return map;
			}else{
				return null;
			}
		}
		return map;
	}
	
	private boolean validateLeaveTime(String start_yyyyMMdd, String end_yyyyMMdd, String startTypes, String endTypes){
		if((start_yyyyMMdd == null || start_yyyyMMdd.equals("")) || (end_yyyyMMdd == null || end_yyyyMMdd.equals("")) || (startTypes == null || startTypes.equals("")) || (endTypes == null || endTypes.equals(""))){
			return true;
		}else{
			return false;
		}
	}
	
	private Object addPostUtil(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		WxSendService wx=new WxSendService();
		Integer leaveId = null;
		try {
			CSRF.validate(req);
			leaveId = Https.getInt(req, "leaveId", R.I);
			Integer typeId = Https.getInt(req, "typeId", R.REQUIRED, R.I, "请假类型");
			String start_yyyyMMdd = Https.getStr(req, "start_yyyyMMdd", R.REQUIRED, R.D, "开始时间");
			String end_yyyyMMdd = Https.getStr(req, "end_yyyyMMdd", R.REQUIRED, R.D, "结束时间");
			Integer startType = Https.getInt(req, "startType", R.REQUIRED, R.I, R.IN, "in [0,1]", "请假开始时段");
			
			Integer endType = Https.getInt(req, "endType", R.REQUIRED, R.I, R.IN, "in [0,1]", "请假结束时段");
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "reason:请假原因");
			String titleName = Https.getStr(req, "titleName", "{1,60}");			
			Integer actorId = Https.getInt(req, "operatorId", R.REQUIRED, R.I, "operatorId:上级审批");
			String actorName = Https.getStr(req, actorId.toString(), "{1,60}");
			Map<Integer, WorkDay> dayMap = workRepository.dayMap();
			Map<Integer, String[]> weekMap = workRepository.weekMap();
			Map<String, Integer[]> monthMap = workRepository.monthMap(Context.getCorpId()); 

			WorkDay day = dayMap.get(Context.getDayId());
//			Asserts.isNull(day, "日排班不能为空值");
			String[] weeks = weekMap.get(Context.getWeekId());
//			Asserts.isEmpty(weeks, "周排班不能为空值");
			
			WorkAttendance att = dao.fetch(WorkAttendance.class, Context.getCorpId());
//			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());
			
			String startStr = start_yyyyMMdd + " " + (startType.equals(Leave.AM) ? day.getCheckIn() : day.getRestOut());
			String endStr = end_yyyyMMdd + " " + (endType.equals(Leave.AM) ? day.getRestIn() : day.getCheckOut());
			
			DateTime start = Calendars.parse(startStr, Calendars.DATE_TIME);
			DateTime end = Calendars.parse(endStr, Calendars.DATE_TIME);

			
//			if (start.isBefore(pos))
//				throw new Errors("开始时间不能小于" + pos.toString("yyyy年MM月dd日"));
			if (start.isAfter(end))
				throw new Errors("开始时间不能大于结束时间");

			Leave leave = null;
			DateTime now = new DateTime();

			if (leaveId != null) {
				leave = dao.fetch(Leave.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("leaveId", "=", leaveId));
				Asserts.isNull(leave, "申请不存在");
				Asserts.notEqOr(leave.getApprove(), Status.APPROVED, Status.OK, "禁止修改已审批的请假申请");
			} else {
				leave = new Leave();
				leave.setStatus(Status.ENABLED);
				leave.setCreateTime(now.toDate());
			}

			//一天工作分钟数
			int works = Works.workMinute(day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut());
			
			//请假分钟数
			int minute = Works.getMinute(startStr, endStr, day.getCheckIn(), day.getCheckOut(), day.getRestIn(), day.getRestOut(), works, monthMap, weeks);

			Map<String, String> leaveMap = dictService.map(Dict.LEAVE);
			String typeName = leaveMap.get(String.valueOf(typeId));
			if (typeName.equals("年休假")) {
				int lastMinute = leaveService.lastAnnualMinute(Context.getUserId(), leaveId, start, end, leaveMap);
				if (lastMinute <= 0)
					throw new Errors("不能申请年休假");
				if (minute > lastMinute)
					throw new Errors("申请年休假不能大于" + (lastMinute / (works * 1.0d)) + "天");
			}
			if (typeName.equals("加班补休")) {
				int lastMinute = overtimeService.lastDeferredMinute(Context.getUserId(), leaveId, start, end, leaveMap);
				if (lastMinute <= 0)
					throw new Errors("不能申请加班补休");
				if (minute > lastMinute)
					throw new Errors("申请加班补休不能大于" + (lastMinute / (works * 1.0d)) + "天");
			}
			
			StringBuilder buff = new StringBuilder();
				buff.append(Context.getTrueName())
				.append("于")
				.append(Calendars.str(now,Calendars.DATE))
				.append("申请请假");
				
			
			leave.setUserId(Context.getUserId());
			leave.setSubject(buff.toString());
			leave.setTypeId(typeId);
			leave.setStartTime(start.toDate());
			leave.setEndTime(end.toDate());
			leave.setLeaveMinute(minute);
			leave.setReason(reason);
			leave.setApprove(Status.PROOFING);
			leave.setModifyTime(now.toDate());
			leave.setStartType(startType);
			leave.setEndType(endType);

			String [] str=userService.findRoleNames(actorId);
			
			String variable=null;
		
			if(Asserts.hasAny(Roles.BOSS.getName(),str)){
				variable=Roles.BOSS.getName();
			}
			else if(Asserts.hasAny(Roles.GM.getName(),str)){
					variable=Roles.GM.getName();
				}
			else if(Asserts.hasAny(Roles.MAN.getName(),str)){
				variable=Roles.MAN.getName();
			}
			else if(Asserts.hasAny(Roles.SVI.getName(),str)){
				variable=Roles.SVI.getName();
			}
			else if(Asserts.hasAny(Roles.EMP.getName(),str)){
				variable=Roles.EMP.getName();
			}
			 
			else 
					variable=Roles.EMP.getName();
			
			/*参数 	必须 	说明
			touser 	否 	UserID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
			toparty 否 	PartyID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
			totag 	否 	TagID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
			agentid 是 	企业应用的id，整型。可在应用的设置页面查看
			title 	否 	标题
	    	description 否 	描述
			url 	否 	点击后跳转的链接。企业可根据url里面带的code参数校验员工的真实身份。具体参考“9 微信页面跳转员工身份查询”
			picurl 	否 	图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图640*320，小图80*80。如不填，在客户端不显示图片 */

			if(WxOpen.LEAVE_APPLY.isOPEN()){			
			String atitle=titleName+"申请请假";
			String description="请假原因:"+leave.getReason();
			List<Role> roles = roleRepository.find(actorId);
			String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
			String agentid=WxRoleUtil.wxrole(roleNames);
			//actorName
			wx.sendarticle(actorName,"","",agentid,atitle,description,WxOpen.LEAVE_APPLY.getURL(),"");	
			//end of weixin!
			}
			transSave(leaveId, leave, actorId,variable, day, weeks, monthMap,start, end, Context.getUserId(),now, typeName);			
			Code.ok(mb, (leaveId == null ? "新建" : "编辑") + "请假申请成功");
					
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (leaveId == null ? "新建" : "编辑") + "请假申请失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		//获取登录用户的公司使用的排班类型
		ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class,"ShiftCorp.query",Cnd.where("o.status","=",Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", Context.getCorpId()));
		//新排班
		if(shiftCorp!=null){
			return newAddPostUtil(req,res);
		}else{
			return addPostUtil(req,res);
		}
	}

	@POST
	@At
	@Ok("json")
	public Object getTime(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			Integer id = Https.getInt(req, "mOrN", R.REQUIRED, R.I,R.IN,"in [0,1]", "类型");
			String time = Https.getStr(req, "start_yyyyMMdd", R.REQUIRED,R.D, "请假时间");
			Shift shift = dao.fetch(Shift.class,Cnd.where("shift_date", "=", time));
	//		if(shift==null)throw new Errors("当日没有排班");
			ShiftClass shiftClass = dao.fetch(ShiftClass.class,Cnd.where("class_id", "=", shift.getClasses()));
	//		if(shiftClass==null)throw new Errors("当日没有排班");
			if(id==0){
				mb.put("first",shiftClass.getFirstMorning());
				if(ShiftC.ENABLED == shiftClass.getSecond()){
					mb.put("last",shiftClass.getSecond());
				}
			}else{
				mb.put("first",shiftClass.getFirstNight());
				if(ShiftC.ENABLED == shiftClass.getSecond()){
					mb.put("last",shiftClass.getSecondNight());
				}
			}
			Code.ok(mb,"");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, "当日没有排班");
		}
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object del(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			if (arr != null && arr.length > 0) {
				List<Leave> leaves = mapper.query(Leave.class, "Leave.query", Cnd
						.where("l.status", "=", Status.ENABLED)
						.and("l.user_id", "=", Context.getUserId())
						.and("l.approve", "=", Status.PROOFING)
						.and("l.leave_id", "in", arr));
				if(leaves.size() == 0){
					throw new Errors("必须选定待审批的申请");
				}
				for (Leave leave : leaves) {
					leaveService.delete(leave);
				}
			}
			Code.ok(mb, "删除请假申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除请假申请失败");
		}
		return mb;
	}
	
	private void transSave(final Integer leaveId, final Leave leave, final Integer actorId,
						final String variable, final WorkDay day, final String[] weeks, 
						final Map<String, Integer[]> monthMap,final DateTime orgStart, final DateTime orgEnd,
						final Integer modifyId, final DateTime now, final String typeName) {
	//	Asserts.isNull(day, "日排班不能为空值");
	//	Asserts.isEmpty(weeks, "周排班不能为空值");
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = leaveId;
				if (leaveId != null) {
					dao.clear(LeaveActor.class, Cnd.where("leaveId", "=", leaveId));
					dao.update(leave);
				} else {
					id = dao.insert(leave).getLeaveId();
				}
				
				// 指定审批
				dao.insert(new LeaveActor(id, actorId, Value.F, leave.getApprove(), variable, leave.getModifyTime(),Value.T, Status.value));
				leaveService.save(leave, orgStart, orgEnd, typeName, day, weeks, monthMap, modifyId, now, leave.getApprove());
			}
		});
	}
	private void transSave(final Integer leaveId, final Leave leave, final Integer actorId, final String variable,
			final DateTime orgStart, final DateTime orgEnd,
			final Integer modifyId, final String typeName,final Map<String, String[]> dayMap,final Map<String, String[]> dayMapOld,final String atitle,final String url,final String actorName) {
		//获取用户注册在企业号中的名字，用于发送微信
		final WechatStaffmatching matching = dao.fetch(WechatStaffmatching.class, Cnd.where("userId", "=", actorId));
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = leaveId;
				if (leaveId != null) {
					dao.clear(LeaveActor.class, Cnd.where("leaveId", "=", leaveId));
					checkedRecordService.updateClear(leave.getUserId(), new DateTime(leave.getStartTime()).toString(Calendars.DATE_TIME), new DateTime(leave.getEndTime()).toString(Calendars.DATE_TIME), null, dayMapOld, modifyId);						
					dao.update(leave);
				} else {
					id = dao.insert(leave).getLeaveId();
				}

				// 指定审批
				dao.insert(new LeaveActor(id, actorId, Value.F, leave.getApprove(), variable, leave.getModifyTime(), Value.T, Status.value));
				if(dayMap != null){
					checkedRecordService.update3(leave.getUserId(), modifyId, dayMap);
				}
				

				String description="请假原因:"+leave.getReason();
				
				
				
				User user = dao.fetch(User.class, Cnd.where("user_id", "=", actorId));
				List<String> maiList = new ArrayList<String>();
				if(StringUtils.isNotBlank(user.getEmail())){
					maiList.add(user.getEmail());
				}
				MailStart mail = new MailStart();
				mail.mail(maiList,atitle,description);
				

			}
			
		

		});

		String description="请假原因:"+leave.getReason();
		
		if(WxOpen.LEAVE_APPLY.isOPEN()&&leaveId == null&&wxUserService.leave() && matching != null && matching.getWechatName() != null && !matching.getWechatName().equals("")){
			List<Role> roles = roleRepository.find(actorId);
			String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
			String agentid=WxRoleUtil.wxrole(roleNames);
			WxSendService wx=new WxSendService();
			
			
			wx.sendarticle(matching.getWechatName(),"","",agentid,atitle,description,url,"");
			}
		
		
		

	}
	
	/**检验用户选择的请假类型。获得相对应的类型信息*/
	@At
	@POST
	@Ok("json")
	public Integer leaveType(Integer typeId){
		//检验参数有效性
		if (typeId==null||typeId<=0){
			throw new Errors("无效的类型！");
		}
		Integer corpId = Context.getCorpId();
		//获得该用户所属公司次请假类型的长度类型，返回
		LeaveType lt = dao.fetch(LeaveType.class,Cnd.where("org_id","=",corpId).and("leave_type_id","=",typeId));
		//如果查询返回null，则代表这是新增加的公司或者改公司还没有配置请假类型长度，那就按照默认配置返回
		if (lt==null){
			lt = dao.fetch(LeaveType.class,Cnd.where("org_id","=",-1).and("leave_type_id","=",typeId));
		}
		Integer valid = lt.getValid();
		return valid;
	}
	
}
