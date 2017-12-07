package test.service.andy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.util.cri.Static;

import cn.oa.model.EduStudent;
import cn.oa.model.EduTeacher;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.Org;
import cn.oa.model.ShopGoods;
import cn.oa.service.course.CourseService;
import cn.oa.service.course.CourseServiceImpl;
import cn.oa.service.schedule.TeachingSchedule;
import cn.oa.service.schedule.TeachingScheduleImpl;
import cn.oa.service.teacher.TeacherService;
import cn.oa.service.teacher.TeacherServiceImpl;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.web.Page;
import cn.oa.web.wx.Config;
import test.Setup;

public class TestTeachingScheduleService extends Setup{
	
	@Test
	public void test() throws Exception{
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		ShopGoods course = dao.fetch(ShopGoods.class, 12);
		teachingSchedule.teachingSchedule(course, true);
	}
	
	@Test
	public void test2(){
		String couTimestr = "08:30-10:30|||||08:30-10:30|";
		int start = 0;
		int index = couTimestr.indexOf("|");
		
		while(index != -1){
			String item = couTimestr.substring(start, index);
			System.out.println(item);
			start = index + 1;
			index = couTimestr.indexOf("|", index +1);
		}
		
		if(start <= couTimestr.length()){
//			System.out.println(start+":"+couTimestr.length());
			String item = couTimestr.substring(start, couTimestr.length());
			System.out.println(item);
		}

	}
	
	//selectByDay(Date day);
	@Test
	public void test3(){
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		Date day = new Date();
		List<EduTeacher> teachers = teachingSchedule.selectByDay(day);
		System.out.println(teachers.get(0).getEduTeachingSchedules().get(0).getCourse());
	}
	
	//selectByDayOfWeek(Date day);
	@Test
	public void test4(){
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		Date day = new Date();
		List<EduTeacher> teachers = teachingSchedule.selectByDayOfWeek(day);
		System.out.println(teachers.get(0).getEduTeachingSchedules().get(6).getCourse());
	}
	
	//selectAll(Date start, Date end)
	//selectAll(String start, Sring end)
	@Test
	public void test5(){
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		DateTime start = Calendars.parse("2017-07-04 08:00:00", Calendars.DATE_TIMES);
		DateTime end = Calendars.parse("2017-07-04 10:00:00", Calendars.DATE_TIMES);
//		List<EduTeachingSchedule> schedules = teachingSchedule.selectAll(start.toDate(), end.toDate());
		List<EduTeachingSchedule> schedules = teachingSchedule.selectAll("2017-07-04 08:00:00", "2017-07-04 10:00:00");
		System.out.println(schedules.size());
	}

	//selectAll(String start, String end, Org corp, List<EduCourse> subjects);
	@Test
	public void test6(){
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		CourseService courseService = ioc.get(CourseServiceImpl.class, "courseService");
		Org corp = new Org();
		corp.setOrgId(275);
		List<ShopGoods> subjects = new ArrayList<ShopGoods>();
		ShopGoods subject = new ShopGoods();
		subject.setId(2);
		subjects.add(subject);
		List<ShopGoods> courses = courseService.findAll(corp, subjects);
		List<EduTeachingSchedule> schedules = teachingSchedule.selectAll("2017-07-04 08:00:00", "2017-07-04 10:00:00", courses);
		System.out.println(schedules.get(0).getTeacher());
	}
	
	//selectBySchedule(String start, String end, Org crop)
	@Test
	public void test7() throws Exception{
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		Org crop = new Org();
		crop.setOrgId(275);
		List<Integer> ids = teachingSchedule.selectBySchedule("2017-07-04 00:00:00", "2017-07-05 23:59:59", crop);
		System.out.println(ids.size());
	}
	
	//teachingSchedule(EduCourse course)
	@Test
	public void test8() throws Exception{
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");

		ShopGoods course = new ShopGoods();
		course.setCouTime("3||12:00-13:00|||||14:00-15:00");
		course.setCouCount(42);
		course.setStartDate(new DateTime().toDate());
		course.setEduTeacherId(1);
		course.setId(40);
		course.setLocation("abcadf");
		
		System.out.println(teachingSchedule.teachingSchedule(course, true));
	}
	
	//findOutdatedCourseIds()
	@Test
	public void test9() throws Exception{
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		List<Integer> ids = teachingSchedule.findOutdatedCourseIds();
		System.out.println(ids);
	}
	
	//selectAll(int days, EduStudent student)
	@Test
	public void test10() throws Exception{
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		EduStudent student = new EduStudent();
		student.setId(54);
		List<EduTeachingSchedule> tss = teachingSchedule.selectAll(7, null, student);	
		for(EduTeachingSchedule ts : tss){
			System.out.println(ts.getTeacher());
		}
	}
	
	//selectAllCompanyWechatConfig()
	@Test
	public void test11() throws Exception{
		TeachingScheduleImpl teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		Map<Integer, Config> map = teachingSchedule.selectAllCompanyWechatConfig();
		System.out.println(map.size());
	}
	
	//sendWeixin()
	@Test
	public void test12() throws Exception{
		TeachingScheduleImpl teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		boolean flag = teachingSchedule.sendWeixin();
		System.out.println(flag);
	}
	
	@Test
	public void test13() {
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		//查询所有需要标记为过时的课程，并且改变status字段为2
		List<Integer> courseIds = teachingSchedule.findOutdatedCourseIds();
		dao.update(ShopGoods.class, Chain.make("status", 2), Cnd.where(new Static("(true")).and("id", "in", courseIds).or("dependId", "in", courseIds).and(new Static("true)")).and("status", "=", 1));
	}
}
