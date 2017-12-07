package cn.oa.web.action.adm.salary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.TableName;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.ShiftC;
import cn.oa.consts.Status;
import cn.oa.consts.Suffix;
import cn.oa.model.Att_record;
import cn.oa.model.AttendanceThreshold;
import cn.oa.model.AttendanceThresholdItem;
import cn.oa.model.CheckedRecord;
import cn.oa.model.RedressRecord;
import cn.oa.model.Shift;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.except.Errors;
import cn.oa.utils.helper.CheckedRecords;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

/**
 * 个人考勤查询
 * @author SimonTang
 */
@IocBean(name = "adm.salary.attendance")
@At(value = "/adm/salary/attendance")
public class AttendanceAction extends Action {

	public static Log log = Logs.getLog(AttendanceAction.class);

	public void pageUtil(HttpServletRequest req) {
		DateTime now = new DateTime();
		MapBean mb = new MapBean();
		
		String year = Values.getStr(Https.getStr(req, "year", R.yyyy), String.valueOf(now.getYear()));
		String month = Values.getStr(Https.getStr(req, "month", R.MM), now.plusMonths(0).toString("MM"));

		if (dao.exists("att_checked_record_" + year + month)) {
			Map<String, String> vars = new ConcurrentHashMap<String, String>();
			vars.put("month", year + month);
			List<CheckedRecord> attendances = mapper.query(CheckedRecord.class, "CheckedRecord.query", Cnd
					.where("c.user_id", "=", Context.getUserId()), null, vars);
			String[] thisMonth = Calendars.timeToMT(Calendars.parse(year+"-"+month+"-01", Calendars.DATE));
			List<Att_record> att_records = dao.query(Att_record.class, Cnd.where("needYearDay", ">=", thisMonth[0]).and("needYearDay", "<=", thisMonth[1]).and("userId", "=", Context.getUserId()));
			for(CheckedRecord checkedRecord : attendances){
				for(Att_record att_record : att_records){
					if(att_record.getNeedYearDay().equals(checkedRecord.getWorkDate())){
						
						if(att_record.getQuantum()==0)
							checkedRecord.setMforget(("1".equals(att_record.getType())?"机器故障":"忘记打卡")+(att_record.getApprove()==0?"(待审批)":"(未批准)"));
						if(att_record.getQuantum()==1)
							checkedRecord.setNforget(("1".equals(att_record.getType())?"机器故障":"忘记打卡")+(att_record.getApprove()==0?"(待审批)":"(未批准)"));
					}
				}
			}
			req.setAttribute("attendances", attendances);
		}
		
		mb.put("year", year);
		mb.put("month", month);
		mb.put("years", now.getYear());
		mb.put("months", now.plusMonths(-1).toString("MM"));

		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/attendance_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/attendance_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}
	

	@GET
	@At
	public void download(HttpServletRequest req, HttpServletResponse res) {
		try {
			DateTime now = new DateTime();
			
			String year = Values.getStr(Https.getStr(req, "year", R.yyyy), String.valueOf(now.getYear()));
			String month = Values.getStr(Https.getStr(req, "month", R.MM), now.plusMonths(-1).toString("MM"));

			ServletOutputStream output = Webs.output(req, res, Encodes.urlEncode("考勤记录." + Suffix.XLSX));
			Excels excels = new Excels(Suffix.XLSX);
			List<List<Data>> rowList = new ArrayList<List<Data>>();
			rowList.add(CheckedRecords.subjects(false));

			if (dao.exists("att_checked_record_" + year + month)) {
				Map<String, String> vars = new ConcurrentHashMap<String, String>();
				vars.put("month", year + month);
				List<CheckedRecord> checkedRecords = mapper.query(CheckedRecord.class, "CheckedRecord.query", Cnd
						.where("c.user_id", "=", Context.getUserId())
						.and("c.work_date", "<=", now.toString(Calendars.DATE)), null, vars);
				
				for (CheckedRecord e : checkedRecords) {
					rowList.add(CheckedRecords.cells(views, e, false));
				}
			}
			
			excels.write(output, rowList, "考勤记录");
		} catch (Exception e) {
			log.error("(Attendance:download) error: ", e);
		}
	}
	
	/**
	 * 补录申请
	 * @param req
	 * @param res
	 * @throws ParseException
	 */
	@GET
	@At
	@Ok("ftl:adm/salary/attendance_record")
	public void recording(HttpServletRequest req,HttpServletResponse res) throws ParseException{
		Integer quantum = Https.getInt(req, "quantum", R.REQUIRED,R.IN,"in [1,0]","时间段");
		DateTime nowTime = new DateTime();//当前时间
		String year = String.valueOf(nowTime.getYear());
		String month = String.valueOf(nowTime.getMonthOfYear());
		String day = String.valueOf(nowTime.getDayOfMonth());
		
		String yearMD = year+"-"+month+"-"+day;
		List<User> operators = null;
		User manager = dao.fetch(User.class, Context.getUser().getManagerId());
		if(manager != null){
			operators = new ArrayList<User>();
			operators.add(manager);
		} else{
			operators = userService.operators(Context.getCorpId(), Context.getLevel());
		}
		
		String needYearDay = req.getParameter("date");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = sdf.parse(needYearDay);
		List<Att_record> att_record = dao.query(Att_record.class, Cnd.where("needYearDay", "=", date).and("quantum", "=", quantum).and("userId", "=", Context.getUserId()));
		req.setAttribute("nowTime", yearMD);//将时间放进去req
		if(att_record.size()>0){
			//有记录
			for(int i=0;i<att_record.size();i++){
				req.setAttribute("reason", att_record.get(i).getReason());
				req.setAttribute("type", att_record.get(i).getType());
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				String nowTime2 = simpleDateFormat.format(att_record.get(i).getDate());
				req.setAttribute("nowTime", nowTime2);
			}
		}
		req.setAttribute("quantumString", (quantum == 0?"上午":"下午"));
		req.setAttribute("quantum", quantum);	
		req.setAttribute("needYearDay", needYearDay);
		req.setAttribute("operators", operators);
	}
	
	// 补录提交
	@POST
	@At
	@Ok("json")
	public Object recordSubmit(HttpServletRequest req){
		MapBean mb = new MapBean();
		boolean addType = false;
		try {
			String needYearDay = Https.getStr(req, "needYearDay",R.D,R.REQUIRED,"申请时间");
			Integer quantum = Https.getInt(req, "quantum", R.REQUIRED,R.IN,"in [1,0]","申请时间段");
			String typeId = Https.getStr(req, "typeId",R.CLEAN,R.REQUIRED,"申请类型");
			String reason = Https.getStr(req, "reason",R.CLEAN,R.REQUIRED,"申请原因");
			Integer operId = Https.getInt(req, "operatorId",R.CLEAN,R.REQUIRED,"上级审批人");
			User operators1 = dao.fetch(User.class, operId);
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");//转换时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");//转换时间
			Date date = new Date();
			String dateString = sdf.format(date);
			Date dateD = sdf.parse(dateString);
			Date date2 = simpleDateFormat.parse(needYearDay);
			String year = Calendars.str(date2, "yyyy");
			String month = Calendars.str(date2, "MM");
			Att_record aRecord = dao.fetch(Att_record.class, Cnd.where("needYearDay","=",date2).and("quantum", "=", quantum).and("userId", "=", Context.getUserId()));
			if (dao.exists("att_checked_record_" + year + month)) {
				Map<String, String> vars = new ConcurrentHashMap<String, String>();
				vars.put("month", year + month);
				CheckedRecord checkedRecord = mapper.fetch(CheckedRecord.class, "CheckedRecord.query", Cnd.where("c.work_date","=", needYearDay).and("c.user_id", "=", Context.getUserId()), null, vars);
				if(quantum==0){
				if(checkedRecord.getRemarkedIn()!=null){
					if(checkedRecord.getRemarkedIn().contains("待审批")){
						throw new Errors("请假,外勤,出差,加班审批后才能申请补录");
					}
				}
				}else{
					if(checkedRecord.getRemarkedOut()!=null){
						if(checkedRecord.getRemarkedOut().contains("待审批")){
							throw new Errors("请假,外勤,出差,加班审批后才能申请补录");
						}
					}
				}
			}else{throw new Errors("没有找到该数据");}
			
			if(aRecord != null){
				throw new Errors("禁止修改已经提交的申请");
			}else{
				addType = true;
				//获取考勤未打卡的次数
				//获取本月已申请未打卡的次数
				if(typeId.equals("2")){
					AttendanceThreshold attendanceThreshold = dao.fetch(AttendanceThreshold.class,Cnd.where("org_ids", "like", "%"+String.valueOf(Context.getCorpId())+"%").and("status", "=", 1));
					AttendanceThresholdItem AttendanceThresholdItem = dao.fetch(AttendanceThresholdItem.class,Cnd.where("threshold_id", "=", attendanceThreshold.getThresholdId()).and("type", "=", Status.F).and("status", "=", Status.ENABLED));
					if(AttendanceThresholdItem == null){throw new Errors("考勤阀值没有配置未打卡次数");}
					String[] sTime = Calendars.timeToMT(Calendars.parse(needYearDay, Calendars.DATE));
					int count = dao.count(Att_record.class, Cnd.where("needYearDay", ">=", sTime[0]).and("needYearDay", "<=", sTime[1]).and("type", "=", typeId).and("userId", "=", Context.getUserId()).and("approve", "<>", "-1"));
					if(count >= AttendanceThresholdItem.getAmountStart()){
						throw new Errors("本月申请未打卡次数不能超过"+String.valueOf(AttendanceThresholdItem.getAmountStart())+"次");
					}
				}
				//无记录
				Att_record record = new Att_record(typeId, Context.getUserId(), reason, operators1.getUserId(), dateD, 0, date2, Context.getTrueName()+"补录"+needYearDay+"的申请",1,new Date(),operators1.getTrueName(),"-");
				record.setQuantum(quantum);
				dao.insert(record);
				
			}
			Code.ok(mb, (addType?"添加":"编辑")+"补录申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (addType?"添加":"编辑")+"补录申请失败");
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/attendance_recordApprove")
	public void recordApprove(HttpServletRequest req){
		
		Integer recordId = Https.getInt(req, "recordId",R.CLEAN,R.REQUIRED,"补录id" );
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Att_record att_record = dao.fetch(Att_record.class, recordId);
		
		User user = dao.fetch(User.class, att_record.getUserId());
		
		req.setAttribute("recordId", att_record.getRecordId());
		req.setAttribute("type", att_record.getType());//补录类型
		req.setAttribute("reason", att_record.getReason());//申请原因
		req.setAttribute("quantum", att_record.getQuantum()==0?"上午":"下午");//补录时间段
		req.setAttribute("nowTime", Calendars.str(att_record.getDate(), Calendars.DATE_TIME));//申请时间att_record.getDate()
		req.setAttribute("approve", att_record.getApprove());//审批状态
		req.setAttribute("needYearDay", sdf.format(att_record.getNeedYearDay()));//申请哪一天的日期att_record.getNeedYearDay()
		req.setAttribute("trueName", user.getTrueName());//申请人姓名
	}
	
	@POST
	@At
	@Ok("json")
	public Object recordSubmitApprove(HttpServletRequest req){
		MapBean mb = new MapBean();
		try {
			Integer recordId = Https.getInt(req, "recordId", R.CLEAN,R.REQUIRED,"补录申请id");
			String approve = Https.getStr(req, "approve", R.CLEAN,R.REQUIRED,"请选择");
			String opinion = Https.getStr(req, "opinion", R.CLEAN);
			
			Att_record record = dao.fetch(Att_record.class, recordId);
			if("1".equals(approve)){
				//已批准
				record.setApprove(1);
				record.setStatus(0);
				record.setOpinion(opinion);
				record.setModifyTime(new Date());
				Shift shift = mapper.fetch(Shift.class, "Shiftinner.query",Cnd.where("s.user_id", "=", record.getUserId()).and("s.shift_date", "=", record.getNeedYearDay()));
				Asserts.isNull(shift, "当日没有排班");
				String checkIn = null;
				String checkOut = null;
				if(shift.getNight()== ShiftC.NIGHT_IN){
					Shift yesterday = mapper.fetch(Shift.class, "Shiftinner.query",Cnd.where("s.user_id", "=", record.getUserId()).and("s.shift_date", "=", Calendars.parse(record.getNeedYearDay(),Calendars.DATE).plusDays(-1).toString(Calendars.DATE)));
					checkIn = yesterday.getFirstNight();
					checkOut = shift.getFirstMorning();
				}else{
					checkIn = shift.getFirstMorning();
					if(shift.getSecond() == ShiftC.ENABLED){
						checkOut = shift.getSecondNight();
					}else{
					checkOut = shift.getFirstNight();
				}
				}
				String month = Calendars.parse(record.getNeedYearDay(), Calendars.DATE).toString("yyyyMM");
				Map<String, String> vars = new ConcurrentHashMap<String, String>();
				vars.put("month", month);
				CheckedRecord attendance = mapper.fetch(CheckedRecord.class, "CheckedRecord.query", Cnd
						.where("c.user_id", "=", record.getUserId())
						.and("c.work_date", "=", record.getNeedYearDay()), null, vars);
				Asserts.isNull(attendance, "考勤记录不存在");

				if (attendance.getVersion().equals(Status.ENABLED)) throw new Errors("禁止修改已定版的考勤记录");
				if(record.getQuantum() == 0){
					attendance.setCheckedIn(checkIn);
					//attendance.setRemarkIn("正常");
					//attendance.setRemarkedIn("已补录");
					attendance.setRemarkIn("已补录");
				}else{
					attendance.setCheckedOut(checkOut);
					//attendance.setRemarkOut("正常");
					//attendance.setRemarkedOut("已补录");
					attendance.setRemarkOut("已补录");
				}
				transSave(month,attendance);
				dao.update(record);
			}else if("-1".equals(approve)){
				//未批准
				record.setApprove(-1);
				record.setStatus(0);
				record.setOpinion(opinion);
				record.setModifyTime(new Date());
				dao.update(record);
			}else{
				//待审批
				record.setApprove(0);
				dao.update(record);
			}
			
			Code.ok(mb, "审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());		
		} catch (Exception e) {
			Code.error(mb, "审批失败");
		}
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer recordId = Https.getInt(req, "recordId", R.REQUIRED,R.I,"申请ID");
			List<Att_record> actors = dao.query(Att_record.class, Cnd.where("recordId", "=", recordId));
			mb.put("actors",actors);
			Code.ok(mb, "");
		} catch (Exception e) {
			Code.error(mb, e.getMessage());
		}
		return mb;
	}
	
	/**
	 * 补录申请单查询页面
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:adm/salary/attendance_record_page")
	public void record_page(HttpServletRequest req){
		String token = CSRF.generate(req);
		CSRF.generate(req, "adm/salary/attendance/able", token);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		/*cri.where()
				.and("a.approveId", "=", Context.getUserId());
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		Cnds.gte(cri, mb, "a.date", "startTime", startStr);
		Cnds.lte(cri, mb, "a.date", "endTime", endStr);
		cri.getOrderBy().desc("a.modifyTime");

		Page<Att_record> page = Webs.page(req);
		page = mapper.page(Att_record.class, page, "Att_record.count", "Attr_record.index", cri);*/
		
//		cri.where().and("a.approver", "=", Context.getUserId());
		// 允许有权限的人查看,审批权限在提交处控制
		cri.where();
		Cnds.eq(cri, mb, "a.status", "status", approve);
		Cnds.gte(cri, mb, "a.creator_time", "startTime", startStr);
		Cnds.lte(cri, mb, "a.creator_time", "endTime", endStr);
		cri.getOrderBy().asc("a.approve_time").desc("a.creator_time");

		Page<RedressRecord> page = Webs.page(req);
		page = mapper.page(RedressRecord.class, page, "redress_record.count", "redress_record.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	/**
	 * 审批补录单
	 * @param req
	 * @param res
	 * @return
	 */
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			if (arr != null && arr.length > 0) {
				for(Integer recordId : arr){
					
					RedressRecord redressRecord = dao.fetch(RedressRecord.class, recordId);
					if(Context.getUserId() != redressRecord.getApprover()){
						throw new Errors("抱歉，您无权限审批该申请单！申请单号："+redressRecord.getRecordId());
					}
					if(Status.PROOFING != redressRecord.getStatus()){
						String errMsg = "此单已经审批过了，请刷新后选择待审批的项！申请单号："+redressRecord.getRecordId()+",补录原因："+redressRecord.getRedressDesc();
						throw new Errors(errMsg);
					}
					redressRecord.setStatus(status);
					redressRecord.setApproveTime(new Date());
					dao.update(redressRecord);
					
					if(Status.APPROVED == status){
						String redressDate = redressRecord.getRedressDate();
						Shift shift = mapper.fetch(Shift.class, "Shiftinner.query", Cnd.where("s.user_id", "=", redressRecord.getUserId()).and("s.shift_date", "=", redressDate));
						Map<String, String> vars = new ConcurrentHashMap<String, String>();
						String month = Calendars.parse(redressDate, Calendars.DATE).toString("yyyyMM");
						vars.put("month", month);
						CheckedRecord attendance = mapper.fetch(CheckedRecord.class, "CheckedRecord.query",
								Cnd.where("c.user_id", "=", redressRecord.getUserId()).and("c.work_date", "=", redressDate), null, vars);
						String checkIn = null;
						String checkOut = null;
						if (shift.getNight() == ShiftC.NIGHT_IN) {
							Shift yesterday = mapper.fetch(
									Shift.class,
									"Shiftinner.query",
									Cnd.where("s.user_id", "=", redressRecord.getUserId()).and("s.shift_date", "=",
											Calendars.parse(redressDate, Calendars.DATE).plusDays(-1).toString(Calendars.DATE)));
							checkIn = yesterday.getFirstNight();
							checkOut = shift.getFirstMorning();
						} else {
							checkIn = shift.getFirstMorning();
							if (shift.getSecond() == ShiftC.ENABLED) {
								checkOut = shift.getSecondNight();
							} else {
								checkOut = shift.getFirstNight();
							}
						}
						if (redressRecord.getRedressTime() == 0) {
							attendance.setCheckedIn(checkIn);
							attendance.setRemarkIn("已补录");
						} else {
							attendance.setCheckedOut(checkOut);
							attendance.setRemarkOut("已补录");
						}
						transSave(month, attendance);
					}
				}
			}
			Code.ok(mb, "审批补录单成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Attendance:able) error: ", e);
			Code.error(mb, "审批补录单失败");
		}
		return mb;
	}
	
	
	private void transSave(String month ,final CheckedRecord attendance) {
		TableName.run(month, new Runnable() {
			@Override
			public void run() {
				dao.update(attendance);
			}
		});
	}
}
