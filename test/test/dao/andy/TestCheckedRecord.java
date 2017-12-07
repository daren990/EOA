package test.dao.andy;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableWorkbook;
import jxl.write.WritableSheet;
import jxl.write.Label;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.joda.time.DateTime;
import org.junit.Test;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import test.Setup;

import cn.oa.app.quartz.Jobs;
import cn.oa.app.schedule.CheckedRecordJob;
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
import cn.oa.repository.Mapper;
import cn.oa.service.CheckedRecordService;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.Strings;

public class TestCheckedRecord extends Setup{
	private static Log log = Logs.getLog(TestCheckedRecord.class);
	
	//单独根据打卡记录表和排班情况生成考勤日志表
	public void test(){

		Mapper mapper = ioc.get(Mapper.class);
		
		CheckedRecordService checkedRecordService = ioc.get(CheckedRecordService.class);
		
		checkedRecordService.createTable(dao, new DateTime());
		
		//手动决定需要生成考勤日志表中的哪一天
		this.syncAttendance(dao, mapper, new DateTime().minusDays(-8));
	}
	
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
			} else if(archive != null && archive.getOnPosition() == 3) { //停薪
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
	
	
	@Test
   public void test3() throws RowsExceededException, WriteException, IOException, BiffException {
		
	File xlsFile = new File("C:/Users/Administrator/Desktop/7月打卡.xls");
      // 创建一个工作簿
      Workbook readwb = Workbook.getWorkbook(xlsFile); 
      // 创建一个工作表
      Sheet readsheet = readwb.getSheet(0); 
      
	//获取Sheet表中所包含的总列数  
      int rsColumns = readsheet.getColumns();  
      
      //获取Sheet表中所包含的总行数  
      int rsRows = readsheet.getRows(); 
      System.out.println("列数："+rsColumns+"||"+"行数："+rsRows);
      
      ArrayList<CheckRecord> checkRecords = new ArrayList<CheckRecord>();
      //获取指定单元格的对象引用  
      for (int i = 2; i < rsRows; i++)  { 
    	  CheckRecord checkRecord = new CheckRecord();
    	  String jobNumber = readsheet.getCell(2, i).getContents();
    	  String dateStr = readsheet.getCell(3, i).getContents();
    	  System.out.println(dateStr);
    	  DateTime dateTime = Calendars.parse(dateStr, Calendars.DATE_TIMES);
    	  Date date =  dateTime.toDate();
    	  checkRecord.setJobNumber(jobNumber);
    	  checkRecord.setEntryTime(date);
    	  checkRecord.setNumber(1);
    	  checkRecord.setCheckTime(date);
    	  checkRecords.add(checkRecord);
      } 
      dao.fastInsert(checkRecords);
   }
	
}

