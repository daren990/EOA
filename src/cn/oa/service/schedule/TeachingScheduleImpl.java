package cn.oa.service.schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;
import org.nutz.trans.Transaction;

import cn.oa.app.schedule.TeachingScheduleJob;
import cn.oa.consts.MessageTemplate;
import cn.oa.consts.Status;
import cn.oa.exception.ScheduleException;

import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentDisplay;
import cn.oa.model.EduTeacher;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.Org;
import cn.oa.model.ShiftHoliday;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopCompanyWechatConfig;
import cn.oa.model.ShopGoods;
import cn.oa.model.StudentCourseCount;
import cn.oa.repository.Mapper;
import cn.oa.service.client.ClientService;
import cn.oa.service.course.CourseService;
import cn.oa.service.teacher.TeacherService;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.DesUtils;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.wx.comm.QiyeWeixinUtil;
import cn.oa.web.wx.Access_TokenSingleton;
import cn.oa.web.wx.Config;
import cn.oa.web.wx.InterfacePath;

@IocBean(name="teachingSchedule")
public class TeachingScheduleImpl implements TeachingSchedule {
	
	private static Log log = Logs.getLog(TeachingScheduleImpl.class);
	
	@Inject
	private Dao dao;
	
	@Inject
	private CourseService courseService;
	
	@Inject
	private TeacherService teacherService;
	
	@Inject
	private ClientService clientService;
	
	@Override
	public boolean teachingSchedule(ShopGoods course, boolean clear) {

		Asserts.isNull(course.getCouTime(), "请指定课程的具体时间");
		Asserts.isNull(course.getCouCount(), "请指定课次");
		Asserts.isNull(course.getStartDate(), "请指定课程开始日期");
		Asserts.isNull(course.getEduTeacherId(), "请指定授课老师");
		Asserts.isNull(course.getLocation(), "请指定上课地点");
		
		//eduTeachingSchedules中的时间必须是从早到晚
		final List<EduTeachingSchedule> eduTeachingSchedules = getWorkDay(course.getCouTime(), course.getCouCount(), course.getStartDate());
		
		
		try {

			if(Trans.isTransactionNone()){
				Trans.begin();
			}
			
			if(clear){
				//清除课程已经存在的课程安排
				if(course.getId() != null && course.getId() != 0){
					Asserts.isNull(course.getId(), "请指定课程的id");
					List<EduTeachingSchedule> tss = dao.query(EduTeachingSchedule.class, Cnd.where("eduCourseId", "=", course.getId()).and("status", "=", Status.ENABLED));
					for(EduTeachingSchedule ts : tss){
						dao.delete(ts);
					}
//					dao.clear(EduTeachingSchedule.class, Cnd.where("eduCourseId", "=", courseId).and("status", "=", Status.ENABLED));
//					deleteTeachingSchedule(course.getId());
				}
			}

			//检查将要新增的课程安排是否跟已有的安排存在冲突
			if(!checkSchedule(eduTeachingSchedules, course.getEduTeacherId())){
				Trans.rollback();
				Trans.commit();
				return false;
			}
			
			for(EduTeachingSchedule eduTeachingSchedule : eduTeachingSchedules){
				eduTeachingSchedule.setEduCourseId(course.getId());
				eduTeachingSchedule.setEduTeacherId(course.getEduTeacherId());
				eduTeachingSchedule.setLocation(course.getLocation());
				eduTeachingSchedule.setStatus(Status.ENABLED);
			}
			dao.fastInsert(eduTeachingSchedules);
			
		} catch (Exception e) {
			try {
				if(!Trans.isTransactionNone()){
					Trans.rollback();
					Trans.commit();
				}
			} catch (Exception e1) {
				throw new RuntimeException("回滚事务或提交事务的过程中出现异常");
			}
			
			throw new RuntimeException("插入课程安排的时候出现异常");
		}
		
		return true;
	} 
	

	@Override
	public boolean deleteTeachingSchedule(Integer courseId) throws Exception {
		Asserts.isNull(courseId, "请指定课程的id");
		dao.clear(EduTeachingSchedule.class, Cnd.where("eduCourseId", "=", courseId).and("status", "=", Status.ENABLED));
		return true;
	}
	
	
	//检查将要新增的课程安排是否跟已有的安排存在冲突
	public boolean checkSchedule(List<EduTeachingSchedule> eduTeachingSchedules, Integer teacherId){
		Asserts.isEmpty(eduTeachingSchedules, "无法根据课程自动生成课程安排");
		
		//查询某个老师在指定时间段内的安排情况
		SimpleCriteria cri = Cnd.cri();
		cri.asc("start");
		cri.where().and("start", ">=", eduTeachingSchedules.get(0).getStart());
		cri.where().and("end", "<=", eduTeachingSchedules.get((eduTeachingSchedules.size() -1)).getEnd());
		cri.where().and("eduTeacherId", "=", teacherId);
		cri.where().and("status", "=", Status.ENABLED);
		
		
		List<EduTeachingSchedule> existSchedules = dao.query(EduTeachingSchedule.class, cri);
		int x = 0; //用来控制定位到哪个已经存在的课程安排
		for(EduTeachingSchedule newSchedule : eduTeachingSchedules){
			for(;x < existSchedules.size();){
				
				DateTime existStartDate = new DateTime(existSchedules.get(x).getStart());
				DateTime existEndDate = new DateTime(existSchedules.get(x).getEnd());
				DateTime newStartDate = new DateTime(newSchedule.getStart());
				DateTime newEndDate = new DateTime(newSchedule.getEnd());
				
				if(Calendars.str(newStartDate, Calendars.DATE).equals(Calendars.str(existStartDate, Calendars.DATE))){
					//表示同一天，此时完成判断
					if(existStartDate.isBefore(newEndDate) && !existEndDate.isBefore(newStartDate)){
						return false;
					}else if(newStartDate.isBefore(existEndDate) && !newEndDate.isBefore(existStartDate)){
						return false;
					}
					x++;
				}else if(existStartDate.isBefore(newStartDate)){
					//下一个元素的existDate的日期更晚
					x++;
				}else if(newStartDate.isBefore(existStartDate)){
					//下一个元素的newDate的日期更晚
					break;
				}
			}
		}
		return true;
	}
	
	//根据参数生成课程安排
	public List<EduTeachingSchedule> getWorkDay(String couTimestr, Integer couCount, Date startDate){
		int index = couTimestr.indexOf("|");
		String type = couTimestr.substring(0, index);
		if("1".equals(type)){
			return getWorkDayTypeOne(couTimestr.substring(index+1), couCount, startDate);
		}else if("2".equals(type)){
			return getWorkDayTypeTwo(couTimestr.substring(index+1), couCount, startDate);
		}else if("3".equals(type)){
			return getWorkDayTypeFour(couTimestr.substring(index+1), couCount, startDate);
		}
		return null;
	}
	
	private List<EduTeachingSchedule> getWorkDayTypeOne(String couTimestr, Integer couCount, Date startDate){
		
		int index = couTimestr.indexOf("-");
		String startTimeStr = couTimestr.substring(0, index);
		String endTimeStr = couTimestr.substring(index+1);
		String dateStr = getDateStr(startDate);
		
		DateTime startTime = Calendars.parse(dateStr+" "+startTimeStr, Calendars.DATE_TIME);
		DateTime endTime = Calendars.parse(dateStr+" "+endTimeStr, Calendars.DATE_TIME);
		List<EduTeachingSchedule> list = new ArrayList<EduTeachingSchedule>();
		for(int x = 0; x < couCount; x++){
			EduTeachingSchedule ts = new EduTeachingSchedule();
			System.out.println(startTime);
			ts.setStart(startTime.plusDays(x).toDate());
			ts.setEnd(endTime.plusDays(x).toDate());
			list.add(ts);
		}
		return list;
	}
	
	private List<EduTeachingSchedule> getWorkDayTypeTwo(String couTimestr, Integer couCount, Date startDate){
		
		int index = couTimestr.indexOf("-");
		String startTimeStr = couTimestr.substring(0, index);
		String endTimeStr = couTimestr.substring(index+1);
		String dateStr = getDateStr(startDate);
		
		DateTime startTime = Calendars.parse(dateStr+" "+startTimeStr, Calendars.DATE_TIME);
		DateTime endTime = Calendars.parse(dateStr+" "+endTimeStr, Calendars.DATE_TIME);
		
		//查询上年第一天到下一年最后一天之内的ShiftHoliday
		List<ShiftHoliday> shiftHolidays = getShiftHolidays(startTime.toDate());
		
		List<EduTeachingSchedule> list = new ArrayList<EduTeachingSchedule>();
		for(int x = 0; x < couCount; x++){
			EduTeachingSchedule ts = new EduTeachingSchedule();
			DateTime plus = startTime.plusDays(x);
			//判断节假日中是否包含将要插入的课程安排的日期
			if(!contain(shiftHolidays, plus.toDate())){
				ts.setStart(plus.toDate());
				ts.setEnd(endTime.plusDays(x).toDate());
				list.add(ts);
			}else{
				couCount ++;
			}
		}
		
		return list;
	}
	
	//获取用户设置的节假日信息
	private List<ShiftHoliday> getShiftHolidays(Date date){
		DateTime datetime = Calendars.parse(date, Calendars.DATE);
		//当年第一天
		String first = Calendars.str(datetime, "yyyy-01-01");
		datetime = Calendars.parse(first, Calendars.DATE);
		DateTime start = datetime.plusYears(-1);
		DateTime end = datetime.plusYears(2).plusDays(-1);
		//查询上年第一天到下一年最后天之内的ShiftHoliday
		List<ShiftHoliday> list = dao.query(ShiftHoliday.class, Cnd.where("holiday", "<", end.toString(Calendars.DATE)).and("holiday", ">", start.toString(Calendars.DATE)));
		return list;
	}
	
	//判断节假日中是否包含将要插入的课程安排的日期
	private boolean contain(List<ShiftHoliday> shiftHolidays, Date date){
		for(ShiftHoliday shiftHoliday: shiftHolidays){
			String shiftHolidayStr = Calendars.str(new DateTime(shiftHoliday.getHoliday()), Calendars.DATE);
			String dateStr = Calendars.str(new DateTime(date), Calendars.DATE);
			if(dateStr.equals(shiftHolidayStr)){
				return true;
			}
		}
		return false;
	}
	
	private String getDateStr(Date startDate){
		return new DateTime(startDate).toString(Calendars.DATE);
	}
	
	private List<EduTeachingSchedule> getWorkDayTypeThree(String couTimestr, Integer couCount, Date startDate){
		int start = 0;
		int index = couTimestr.indexOf("|");
		//以"|"分割字符串，然后调用getStartAndStop()得到一周内每一天的课程开始时间和结束时间
		List<String[]> week = new ArrayList<String[]>();
		while(index != -1){
			String item = couTimestr.substring(start, index);
			week.add(getStartAndStop(item));
			start = index + 1;
			index = couTimestr.indexOf("|", index +1);
		}
		if(start <= couTimestr.length()){
			String item = couTimestr.substring(start, couTimestr.length());
			week.add(getStartAndStop(item));
		}

		List<EduTeachingSchedule> list = new ArrayList<EduTeachingSchedule>();
		
		for(int x = 0; x < couCount; x++){
			EduTeachingSchedule ts = new EduTeachingSchedule();
			DateTime plus = new DateTime(startDate).plusDays(x);
			int dayOfWeek = plus.dayOfWeek().get();
			String[] startAndStop = week.get(dayOfWeek - 1);
			
			
			if(startAndStop != null){
				DateTime startTime = Calendars.parse(getDateStr(startDate)+" "+startAndStop[0], Calendars.DATE_TIME);
				DateTime endTime = Calendars.parse(getDateStr(startDate)+" "+startAndStop[1], Calendars.DATE_TIME);
				ts.setStart(startTime.plusDays(x).toDate());
				ts.setEnd(endTime.plusDays(x).toDate());
				list.add(ts);
			}else{
				couCount ++;
			}
		}
		return list;
	}
	
	private List<EduTeachingSchedule> getWorkDayTypeFour(String couTimestr, Integer couCount, Date startDate){
		
		int start = 0;
		int index = couTimestr.indexOf("|");
		//以"|"分割字符串，然后调用getStartAndStop()得到一周内每一天的课程开始时间和结束时间
		List<String[]> week = new ArrayList<String[]>();
		while(index != -1){
			String item = couTimestr.substring(start, index);
			week.add(getStartAndStop(item));
			start = index + 1;
			index = couTimestr.indexOf("|", index +1);
		}
		if(start <= couTimestr.length()){
			String item = couTimestr.substring(start, couTimestr.length());
			week.add(getStartAndStop(item));
		}

		List<EduTeachingSchedule> list = new ArrayList<EduTeachingSchedule>();
		//查询上年第一天到下一年最后一天之内的ShiftHoliday
		List<ShiftHoliday> shiftHolidays = getShiftHolidays(startDate);
		
		for(int x = 0; x < couCount; x++){
			EduTeachingSchedule ts = new EduTeachingSchedule();
			DateTime plus = new DateTime(startDate).plusDays(x);
			int dayOfWeek = plus.dayOfWeek().get();
			String[] startAndStop = week.get(dayOfWeek - 1);
			
			if(!contain(shiftHolidays, plus.toDate())){
				if(startAndStop != null){
					DateTime startTime = Calendars.parse(getDateStr(startDate)+" "+startAndStop[0], Calendars.DATE_TIME);
					DateTime endTime = Calendars.parse(getDateStr(startDate)+" "+startAndStop[1], Calendars.DATE_TIME);
					ts.setStart(startTime.plusDays(x).toDate());
					ts.setEnd(endTime.plusDays(x).toDate());
					list.add(ts);
				}else{
					couCount ++;
				}
			}else{
				couCount ++;
			}
			

		}
		
		return list;
	}
	
	private String[] getStartAndStop(String item){
		String[] arr = new String[2];
		int index = item.indexOf("-");
		if(index != -1){
			String startTimeStr = item.substring(0, index);
			String endTimeStr = item.substring(index+1);
			arr[0] = startTimeStr;
			arr[1] = endTimeStr;
			return arr;
		}else{
			return null;
		}
	}

	@Override
	public List<EduTeacher> selectByDay(Date day) {
		DateTime dayDataTime = new DateTime(day);
		String start = Calendars.str(dayDataTime, "yyyy-MM-dd 00:00:00");
		String end = Calendars.str(dayDataTime, "yyyy-MM-dd 23:59:59");
//		DateTime day = Calendars.parse(day, Calendars.DATE);
		List<EduTeachingSchedule> list = dao.query(EduTeachingSchedule.class, Cnd.where("start", "<", end).and("start", ">", start).and("status", "=", Status.ENABLED));
		
		Set<Integer> courseIds = getCourseIds(list);
		List<ShopGoods> courses = dao.query(ShopGoods.class, Cnd.where("id", "in", courseIds));
		Map<Integer, ShopGoods> courseMap = new HashMap<Integer, ShopGoods>();
		for(ShopGoods course : courses){
			courseMap.put(course.getId(), course);
		}
		
		Map<Integer, List<EduTeachingSchedule>> map = getMap2(list, courseMap);
		
		List<EduTeacher> teachers = dao.query(EduTeacher.class, Cnd.where("id", "in", map.keySet()));
		for(EduTeacher teacher : teachers){
			List<EduTeachingSchedule> ts = map.get(teacher.getId());
			teacher.setEduTeachingSchedules(ts);
		}

		return teachers;
	}

	@Override
	public List<EduTeacher> selectByDayOfWeek(Date day) {
		DateTime dayDataTime = new DateTime(day);
		int dayOfWeek = dayDataTime.dayOfWeek().get();
		
		String start = Calendars.str(dayDataTime.plusDays(dayOfWeek -1), "yyyy-MM-dd 00:00:00");
		String end = Calendars.str(dayDataTime.plusDays(7 - dayOfWeek), "yyyy-MM-dd 23:59:59");
		List<EduTeachingSchedule> list = dao.query(EduTeachingSchedule.class, Cnd.where("start", "<", end).and("start", ">", start).and("status", "=", Status.ENABLED));
		
		Set<Integer> courseIds = getCourseIds(list);
		List<ShopGoods> courses = dao.query(ShopGoods.class, Cnd.where("id", "in", courseIds));
		Map<Integer, ShopGoods> courseMap = new HashMap<Integer, ShopGoods>();
		for(ShopGoods course : courses){
			courseMap.put(course.getId(), course);
		}
		//将课程注入到schedule中，而且将schedule中的老师id提取出来作为key
		Map<Integer, List<EduTeachingSchedule>> map = getMap2(list, courseMap);
		
		List<EduTeacher> teachers = dao.query(EduTeacher.class, Cnd.where("id", "in", map.keySet()));
		for(EduTeacher teacher : teachers){
			List<EduTeachingSchedule> ts = map.get(teacher.getId());
			teacher.setEduTeachingSchedules(ts);
		}
		
		return teachers;
	}
	
	//将teacherId抽取出来作为key
	private Map<Integer, EduTeacher> getMap(List<EduTeacher> teachers){
		Map<Integer, EduTeacher> map = new HashMap<Integer, EduTeacher>();
		for(EduTeacher teacher : teachers){
			map.put(teacher.getId(), teacher);
		}
		return map;
	}
	
	//将所有EduTeachingSchedule加入对应的teacher中
	private void joinTeacher(Map<Integer, EduTeacher> map, List<EduTeachingSchedule> eduTeachingSchedules){
		for(EduTeachingSchedule eduTeachingSchedule : eduTeachingSchedules){
			EduTeacher teacher = map.get(eduTeachingSchedule.getEduTeacherId());
			List<EduTeachingSchedule> teachingSchedules = null;
			if(teacher.getEduTeachingSchedules() == null){
				teachingSchedules = new ArrayList<EduTeachingSchedule>();
				teacher.setEduTeachingSchedules(teachingSchedules);
			}else{
				teachingSchedules = teacher.getEduTeachingSchedules();
			}
			teachingSchedules.add(eduTeachingSchedule);
		}
	}
	
	private Map<Integer, List<EduTeachingSchedule>> getMap2(List<EduTeachingSchedule> list, Map<Integer, ShopGoods> courseMap){
		Map<Integer, List<EduTeachingSchedule>> map = new HashMap<Integer, List<EduTeachingSchedule>>();
		List<EduTeachingSchedule> remove = new ArrayList<EduTeachingSchedule>();
		for(EduTeachingSchedule eduTeachingSchedule : list){
			List<EduTeachingSchedule> ts = null;
			if(map.get(eduTeachingSchedule.getEduTeacherId()) == null){
				ts = new ArrayList<EduTeachingSchedule>();
				map.put(eduTeachingSchedule.getEduTeacherId(), ts);
			}else{
				ts = map.get(eduTeachingSchedule.getEduTeacherId());
			}
			if(courseMap.get(eduTeachingSchedule.getEduCourseId()) != null){
				eduTeachingSchedule.setCourse(courseMap.get(eduTeachingSchedule.getEduCourseId()));
				ts.add(eduTeachingSchedule);
			}else{
				//只将包含course的schedule注入到map集合中
				remove.add(eduTeachingSchedule);
			}
		}
		list.removeAll(remove);
		return map;
	}
	
	private Set<Integer> getCourseIds(List<EduTeachingSchedule> list){
		Set<Integer> courseIds = new HashSet<Integer>();
		for(EduTeachingSchedule eduTeachingSchedule : list){
			courseIds.add(eduTeachingSchedule.getEduCourseId());
		}
		return courseIds;
	}

	@Override
	public List<EduTeachingSchedule> selectAll(Date start, Date end) {
		List<EduTeachingSchedule> schedules = dao.query(EduTeachingSchedule.class, Cnd.where("start", ">=", Calendars.str(start, Calendars.DATE_TIMES)).and("end", "<=", Calendars.str(end, Calendars.DATE_TIMES)).and("status", "=", Status.ENABLED));
		return schedules;
	}

	@Override
	public List<EduTeachingSchedule> selectAll(String start, String end) {
		List<EduTeachingSchedule> schedules = dao.query(EduTeachingSchedule.class, Cnd.where("start", ">=", start).and("end", "<=", end).and("status", "=", Status.ENABLED));
		return schedules;
	}

	@Override
	public List<EduTeachingSchedule> selectAll(String start, String end,
			List<ShopGoods> courses) {
		if(courses == null || courses.size() == 0){
			return null;
		}
		List<Integer> ids = new ArrayList<Integer>();
		for(ShopGoods course : courses){
			ids.add(course.getId());
		}
		//查询某些课程在指定时间段内的安排情况
		List<EduTeachingSchedule> schedules = dao.query(EduTeachingSchedule.class, Cnd.where("start", ">=", start).and("end", "<=", end).and("eduCourseId", "in", ids).and("status", "=", Status.ENABLED).asc("end"));
		//将课程注入schedule中
		List<Integer> teacherIds = new ArrayList<Integer>();
		for(EduTeachingSchedule eduTeachingSchedule : schedules){
			teacherIds.add(eduTeachingSchedule.getEduTeacherId());
			int index = ids.indexOf(eduTeachingSchedule.getEduCourseId());
			ShopGoods course = courses.get(index);
			eduTeachingSchedule.setCourse(course);
		}
		//将老师注入schedule中
		List<EduTeacher> teachers = null;
		if(teacherIds.size() == 0){
			teachers = new ArrayList<EduTeacher>(); 
		}else{
			teachers = dao.query(EduTeacher.class, Cnd.where("id", "in", teacherIds).and("status", "=", Status.ENABLED));
		}
		
		for(EduTeacher teacher : teachers){
			List<Integer> indexes = getIndexs(teacherIds, teacher.getId());
			for(Integer index  : indexes){
				EduTeachingSchedule schedule = schedules.get(index);
				schedule.setTeacher(teacher);
			}
			System.out.println(teacher.getTruename());
		}
		return schedules;
	}
	
	private List<Integer> getIndexs(List<Integer> teacherIds, Integer id){
		List<Integer> indexs = new ArrayList<Integer>();
		int x = 0;
		for(Integer teacherId : teacherIds){
			if(teacherId.equals(id)){
				indexs.add(x);
			}
			x++;
		}
		return indexs;
	}
	
	@Override
	public List<EduTeachingSchedule> selectAll(int days, Date timePoint,EduStudent student) {
		if(timePoint == null){
			timePoint = new Date();
		}
		List<ShopGoods> courses = courseService.findAll(student);
		if(courses == null || courses.size() == 0){return null;}
		//将合作的课程替换成自营的课程，因为课程安排只会指向自营的课程
		//用来移除合作类型的课程
		List<ShopGoods> remove = new ArrayList<ShopGoods>();
		List<ShopGoods> myCourses = new ArrayList<ShopGoods>();
		for(ShopGoods course : courses){
			if("合作".equals(course.getCoopType())){
				ShopGoods myCourse = new ShopGoods();
				myCourse.setId(course.getDependId());
				//将合作的课程的信息转移
				myCourse.setName(course.getName());
				myCourses.add(myCourse);
				remove.add(course);
			}
		}
		courses.removeAll(remove);
		courses.addAll(myCourses);
		
		DateTime timePoint1 = new DateTime(timePoint);
		DateTime timePoint2 = timePoint1.plusDays(days);
		DateTime start = null;
		DateTime end = null;
		if(timePoint1.isBefore(timePoint2)){
			start = timePoint1;
			end = timePoint2;
		}else{
			start = timePoint2;
			end = timePoint1;
		}
		return selectAll(start.toString("yyyy-MM-dd 00:00:00"), end.toString("yyyy-MM-dd 23:59:59") , courses);
	}

	@Override
	public List<EduTeachingSchedule> selectAll2(int days, EduStudent student) {
		List<ShopGoods> courses = courseService.findAll(student);
		DateTime now = new DateTime();
		DateTime timePoint = now.plusDays(days);
		DateTime start = null;
		DateTime end = null;
		if(now.isBefore(timePoint)){
			start = timePoint.plusDays(-7);
			end = timePoint;
		}else{
			start = timePoint;
			end = timePoint.plusDays(7);
		}
		
		System.out.println(start.toString("yyyy-MM-dd 00:00:00"));
		System.out.println(end.toString("yyyy-MM-dd 23:59:59"));

		return selectAll(start.toString("yyyy-MM-dd 00:00:00"), end.toString("yyyy-MM-dd 23:59:59") , courses);
	}

	
	@Override
	public List<EduTeachingSchedule> selectAll(String start, String end,
			Org corp, List<ShopGoods> subjects) {
		List<ShopGoods> courses = courseService.findAll(corp, subjects);
		return selectAll(start, end, courses);
	}

	@Override
	public List<EduTeacher> selectAll(String start, String end,
			EduTeacher teacher, Org crop) {
		Asserts.isNull(teacher, "请输入老师的id");
		List<EduTeacher> teachers = dao.query(EduTeacher.class, Cnd.where("id", "in", teacher.getId()));
		Asserts.isEmpty(teachers, "不存在该id的老师");
//		if(crop != null){
//			if(teachers.get(0).getCorpId().equals(crop.getOrgId())){
//				//如果老师所属的公司就是登录用户所属的公司，那么就查询该老师的所有课程安排，即不单单查询指定公司下面的课程
//				crop = null;
//			}
//		}
		//查询该老师的所有课程安排，不管哪个公司的
		List<EduTeachingSchedule> list = dao.query(EduTeachingSchedule.class, Cnd.where("start", "<", end).and("start", ">", start).and("eduTeacherId", "=", teacher.getId()).and("status", "=", Status.ENABLED));
		Set<Integer> courseIds = getCourseIds(list);

		List<ShopGoods> courses =  null;
		if(list.size() == 0){
			courses = new ArrayList<ShopGoods>();
		}else{
			if(crop != null){
				//查询公司所拥有的所有课程
				courses = dao.query(ShopGoods.class, Cnd.where("corpId","=", crop.getOrgId()));
				List<Integer> dependIds = new ArrayList<Integer>();
				//获取课程安排所指向的课程跟公司所拥有的课程的交集
				for(int x = 0; x < courses.size(); x++){
					if(!courseIds.contains(courses.get(x).getId())){
						//记录合作的课程所依赖的课程的id,目的是在下方查询
						if(courseIds.contains(courses.get(x).getDependId())){
							dependIds.add(courses.get(x).getDependId());
						}
						courses.remove(x);
						x--;
					}
				}
				if(dependIds.size() != 0){
					courses.addAll(dao.query(ShopGoods.class, Cnd.where("id","in", dependIds)));
				}
			
			}else{
				courses = dao.query(ShopGoods.class, Cnd.where("id", "in", courseIds));
			}
			
		}
		Map<Integer, ShopGoods> courseMap = new HashMap<Integer, ShopGoods>();
		for(ShopGoods course : courses){
			courseMap.put(course.getId(), course);
		}

		//利用courseMap将课程注入到schedule中，而且将schedule中的老师id提取出来作为key(schedules为value),并且将list当中的course属性为null的元素剔除
		Map<Integer, List<EduTeachingSchedule>> map = getMap2(list, courseMap);

		//将课程安排注入到相应的老师中
		if(map.size() != 0 && list.size() != 0){
			for(EduTeacher t : teachers){
				List<EduTeachingSchedule> ts = map.get(teacher.getId());
				t.setEduTeachingSchedules(ts);
			}
		}else{
			//此时说明没有课程安排可以显示,单纯将老师查询出来而已
		}
		return teachers;
	
	}

	@Override
	public List<Integer> selectBySchedule(String start, String end, Org crop)
			throws Exception {
		
		Sql sql = Sqls.queryEntity(dao.sqls().get("TeachingSchedule.teacherId.query"));
		
		if(crop != null && crop.getOrgId() != null){
			sql.setCondition(Cnd.where("ts.start", "<", end).and("ts.start", ">", start).and("ts.status", "=", Status.ENABLED).and("c.corp_id", "=",crop.getOrgId()));
		}else{
			sql.setCondition(Cnd.where("ts.start", "<", end).and("ts.start", ">", start).and("ts.status", "=", Status.ENABLED));
		}
		
		sql.setCallback(Sqls.callback.ints());
		dao.execute(sql);
		List<Integer> ids = sql.getObject(ArrayList.class);

		return ids;
	}


	@Override
	public List<Integer> findOutdatedCourseIds() {
		
		Sql sql = Sqls.queryEntity(dao.sqls().get("TeachingSchedule.courseIds.query"));
		
		SimpleCriteria cri = Cnd.cri();
		cri.where().and("status", "=", Status.ENABLED);
		cri.groupBy("ts.edu_course_id");
		sql.setCondition(cri);
		sql.setCallback(Sqls.callback.entities());
		sql.setEntity(dao.getEntity(EduTeachingSchedule.class));
		dao.execute(sql);
		
		List<EduTeachingSchedule> teachingSchedules = sql.getList(EduTeachingSchedule.class);
		List<Integer> courseIds = new ArrayList<Integer>();
		for(EduTeachingSchedule ts :  teachingSchedules){
			if(new DateTime(ts.getEnd()).isBeforeNow()){
				courseIds.add(ts.getEduCourseId());
			}
		}

		return courseIds;
	}


	@Override
	public List<EduStudentDisplay> scheduleCount(Integer course_id) {
		List<EduStudentDisplay> list = new ArrayList<EduStudentDisplay>();
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    
		String nowDate = dateFormater.format(new Date()).toString();
		
		StringBuffer str = new StringBuffer("SELECT count(*) as countAll,countEnd,countStart "
                                          + "FROM edu_teaching_schedule a ,"
                                          + "(SELECT count(*) as countEnd from edu_teaching_schedule where start > '" + nowDate + "' and edu_course_id = " + course_id + ") b, "
                                          + "(SELECT count(*) as countStart from edu_teaching_schedule where end < '" + nowDate + "' and edu_course_id = " + course_id + ") c "
                                          + "WHERE a.edu_course_id = " + course_id + " ");
        
		Sql sql = Sqls.create(str.toString());
		//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
		
		sql.setCallback(Sqls.callback.entities());
		Entity<EduStudentDisplay> entity = dao.getEntity(EduStudentDisplay.class);
	    sql.setEntity(entity);
		
		dao.execute(sql);
		list = sql.getList(EduStudentDisplay.class);
		
		return list;

	}


	@Override
	public boolean sendWeixin() throws Exception{
		DateTime datetime = new DateTime();
		DateTime yesterday = datetime.minusDays(-1);
		String start = Calendars.str(yesterday, "yyyy-MM-dd 00:00:00");
		String end = Calendars.str(yesterday, "yyyy-MM-dd 23:59:59");
		
		//找到明天需要上课的课程，key是需要上课的课程id
		Map<Integer, List<EduTeachingSchedule>> teachingScheduleMap = courseService.findAll(start, end);
		if(teachingScheduleMap == null || teachingScheduleMap.size() == 0){
			return false;
		}
		
		//找到明天需要上课的合作类型的课程
		Set<Integer> courseIds = teachingScheduleMap.keySet();
		//shopGoodsMap用来对合作类型的课程进行分类，key是被依赖的课程
		Map<Integer, List<ShopGoods>> shopGoodsMap = new HashMap<Integer, List<ShopGoods>>();
		Map<Integer, ShopGoods> dependCoursesMap = courseService.findNeedDepend(courseIds, shopGoodsMap);
		
		Set<Integer> clientIds = new HashSet<Integer>();
		Map<Integer, List<ShopClient>> shopClientMap = null;
		//找到各个课程对应的客户，也就是要通知的人，值得注意的是，如果一个客户有两个学生报读同一个课程，那么客户信息是重复的，但学生信息不同
		if(dependCoursesMap != null && dependCoursesMap.size() != 0){
			courseIds.addAll(dependCoursesMap.keySet());
			shopClientMap = clientService.getAll(courseIds, clientIds);
			courseIds.removeAll(dependCoursesMap.keySet());
		}else{
			shopClientMap = clientService.getAll(courseIds, clientIds);
		}

		//找到客户的微信信息
		Map<Integer, Map<Integer, String>> weixinConfigMap = clientService.getOpenIdAndOrgId(clientIds);
		
		//获取公司的所有微信信息
		Map<Integer, Config> companyWechatConfig = selectAllCompanyWechatConfig();
		
		//向客户发送课程安排信息
		Set<Entry<Integer, List<EduTeachingSchedule>>> entrys = teachingScheduleMap.entrySet();
		for(Entry<Integer, List<EduTeachingSchedule>> entry : entrys){
			//ts就是要发送的某个课程的安排的信息
			List<EduTeachingSchedule> ts = entry.getValue();
			Integer orgId = ts.get(0).getCorp_id();
			//找到要发送某条课程消息的人,并发送消息
			List<ShopClient> shopClients = shopClientMap.get(entry.getKey());
			if(shopClients == null)continue;
			for(ShopClient shopClient : shopClients){
				//以下就是发送微信的操作,shopClient中的学生名字就是要上课的人
				Map<Integer, String> orgIdsAndOpenIds = weixinConfigMap.get(shopClient.getId());
				String openId = orgIdsAndOpenIds.get(orgId);
				if(openId == null){
					log.debug("在准备发送微信的时候，发现该客户并没有在这个公司下记录openId，改客户是："+shopClient.getTruename());
					continue;
				}
				Config config = companyWechatConfig.get(orgId);
				if(config == null){
					log.debug("在准备发送微信的时候，发现公司并没有记录appID和secret");
					continue;
				}
				String access_token = Access_TokenSingleton.getInstance().getMap(config).get(config.getAPPID()).get("access_token");
				String outJson = getOutJson(openId, MessageTemplate.ClassReminderID, ts, shopClient);
				System.out.println(outJson);
				QiyeWeixinUtil.PostMessage(access_token, "POST", InterfacePath.SEND_MESSAGE_URL_ANDY, outJson);
//				print(openId, ts, shopClient);
				System.out.println("---------------");
		
			}
			//向参与合作类型的课程的人发送信息
			List<ShopGoods> dependCourses = shopGoodsMap.get(entry.getKey());
			if(dependCourses != null && dependCourses.size() != 0){
				for(ShopGoods dependCourse : dependCourses){
					//找到要发送某条课程消息的人,并发送消息
					shopClients = shopClientMap.get(dependCourse.getId());
					if(shopClients == null)continue;
					for(ShopClient shopClient : shopClients){
						//以下就是发送微信的操作,shopClient中的学生名字就是要上课的人
						Map<Integer, String> orgIdsAndOpenIds = weixinConfigMap.get(shopClient.getId());
						String openId = orgIdsAndOpenIds.get(orgId);
						if(openId == null){
							log.debug("在准备发送微信的时候，发现该客户并没有在这个公司下记录openId，该客户是："+shopClient.getTruename());
							continue;
						}
						Config config = companyWechatConfig.get(orgId);
						if(config == null){
							log.debug("在准备发送微信的时候，发现公司并没有记录appID和secret");
							continue;
						}
						String access_token = Access_TokenSingleton.getInstance().getMap(config).get(config.getAPPID()).get("access_token");
						String outJson = getOutJson(openId, MessageTemplate.ClassReminderID, ts, shopClient);
						System.out.println(outJson);
						QiyeWeixinUtil.PostMessage(access_token, "POST", InterfacePath.SEND_MESSAGE_URL_ANDY, outJson);
//						print(openId, ts, shopClient);
						System.out.println("---------------");
					}
				}
			}
		}
		return true;
	}
	
	public Map<Integer, Config> selectAllCompanyWechatConfig(){
		List<ShopCompanyWechatConfig> configs = dao.query(ShopCompanyWechatConfig.class, Cnd.wrap("true"));
		Map<Integer, Config> map = new HashMap<Integer, Config>();
		for(ShopCompanyWechatConfig config : configs){
			DesUtils des;
			try {
				des = new DesUtils();
				map.put(config.getOrgId(), new Config(des.decrypt(config.getAppid()), des.decrypt(config.getSecret())));
			} catch (Exception e) {
				throw new RuntimeException("解密微信信息失败");
			}
		}
		return map;
	}
	
	private String getOutJson(String openId, String template_id, List<EduTeachingSchedule> tss, ShopClient client){
		List<String> content = new ArrayList<String>();
		content.add("尊敬的"+client.getTruename()+"家长,"+client.getStudentName()+"同学有一门课程即将开始。");
		content.add(tss.get(0).getName());
		content.add("精彩的章节即将开始~~");
		StringBuffer sb = new StringBuffer();
		String split = "";
		
		String dateStr = Calendars.str(tss.get(0).getStart(), Calendars.DATE);
		sb.append(dateStr + " ");
		for(EduTeachingSchedule ts : tss){
			DateTime start = new DateTime(ts.getStart());
			DateTime end =new DateTime(ts.getEnd());
			String startStr = Calendars.str(start, "HH:mm");
			String endStr = Calendars.str(end, "HH:mm");
			sb.append(split + startStr + "~" + endStr);
			split = ",";
		}
		content.add(sb.toString());
		content.add("老师和同学都在等着"+client.getStudentName()+"同学哦，抓紧来吧~");
		return fillingTemplate(openId, template_id, content);
	}
	
	private String fillingTemplate(String openId, String template_id, List<String> content){
		String outJson = "{\"touser\":\""+openId+"\",\"template_id\":\""+template_id+"\",\"data\":{\"first\":{\"value\":\""+content.get(0)+"\",\"color\":\"#173177\"},\"keyword1\":{\"value\":\""+content.get(1)+"\",\"color\":\"#173177\"},\"keyword2\":{\"value\":\""+content.get(2)+"\",\"color\":\"#173177\"},\"keyword3\":{\"value\":\""+content.get(3)+"\",\"color\":\"#173177\"},\"remark\":{\"value\":\""+content.get(4)+"\",\"color\":\"#173177\"}}}";
		return outJson;
	}
	
	private String print(String openId, List<EduTeachingSchedule> tss, ShopClient client){
		System.out.println("需要上课的课程是："+tss.get(0).getName());
		for(EduTeachingSchedule ts : tss){
			System.out.println("   上课的开始时间是："+ts.getStart().toString()+"，"+"结束时间是："+ts.getEnd().toString());
		}
		System.out.println("    需要上课的人是"+client.getStudentName());
		System.out.println("    需要通知的人是"+client.getName());
		return null;
	}


	@Override
	public String getScheduleTimeById(Integer scheduleId) {
		EduTeachingSchedule ts = dao.fetch(EduTeachingSchedule.class,Cnd.where("id","=",scheduleId));
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		String week = sdf.format(ts.getStart());
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");    
		String start = dateFormater.format(ts.getStart()).toString();
		String end = dateFormater.format(ts.getEnd()).toString();
        start += "~" + end.split(" ")[1];
        start += "(" + week + ")";
		return start;
	}



}
