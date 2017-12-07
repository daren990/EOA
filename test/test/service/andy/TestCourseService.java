package test.service.andy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import test.Setup;

import cn.oa.model.EduStudent;
import cn.oa.model.EduTeacher;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.ShopGoods;
import cn.oa.service.course.CourseService;
import cn.oa.service.course.CourseServiceImpl;
import cn.oa.utils.web.Page;

public class TestCourseService extends Setup{
	@Test
	public void test1() throws Exception{
		CourseService courseService = ioc.get(CourseServiceImpl.class, "courseService");
		List<EduTeacher> teachers = new ArrayList<EduTeacher>();
		EduTeacher teacher1 = new EduTeacher();
		teacher1.setId(1);
		EduTeacher teacher2 = new EduTeacher();
		teacher2.setId(2);
		EduTeacher teacher3 = new EduTeacher();
		teacher3.setId(3);
		EduTeacher teacher4 = new EduTeacher();
		teacher4.setId(4);
		teachers.add(teacher1);
		teachers.add(teacher2);
		teachers.add(teacher3);
		teachers.add(teacher4);
		teachers = courseService.findAll(teachers);
		System.out.println(teachers.get(3).getSubjects().size());
	}
	
	@Test
	public void test3() throws Exception{
		CourseService courseService = ioc.get(CourseServiceImpl.class, "courseService");
		EduStudent student = new EduStudent();
		student.setId(13);
		List<ShopGoods> courses = courseService.findAll(student);
		System.out.println(courses.size());
	}
	
	@Test
	public void test2() throws Exception{
		CourseService courseService = ioc.get(CourseServiceImpl.class, "courseService");
//		Page<EduCourse> page = courseService.findCourseByStudentId(13+"", 275+"");
//		System.out.println(page.getResult().size());
		
		Page<ShopGoods> page = courseService.findCourseByTeacherId(1+"", 275+"");
		System.out.println(page.getResult().size());

	}
	
	@Test
	public void test4() throws Exception{
		CourseService courseService = ioc.get(CourseServiceImpl.class, "courseService");
		Integer orgId = 275;
		List<ShopGoods> courses =courseService.findCoop(orgId);
		System.out.println(courses.size());
	}
	
	//findAll(String start, String end)
	@Test
	public void test5() throws Exception{
		CourseService courseService = ioc.get(CourseServiceImpl.class, "courseService");
		Map<Integer, List<EduTeachingSchedule>> shopGoodsMap = courseService.findAll("2017-08-07 00:00:00", "2017-08-07 23:59:59");
		System.out.println(shopGoodsMap.size());
	}
	
	//findNeedDepend(Set<Integer> courseIds)
	@Test
	public void test6() throws Exception{
		CourseService courseService = ioc.get(CourseServiceImpl.class, "courseService");
		Set courseIds = new HashSet();
		courseIds.add(19);
		Map<Integer, List<ShopGoods>> shopGoodsMap = new HashMap<Integer, List<ShopGoods>>();
		Map<Integer, ShopGoods> dependCourses = courseService.findNeedDepend(courseIds, shopGoodsMap);
		
		System.out.println(shopGoodsMap.get(19).size());
		System.out.println(dependCourses.get(161));
	}
	
}
