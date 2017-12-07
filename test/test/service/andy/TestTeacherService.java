package test.service.andy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import test.Setup;

import cn.oa.model.EduStudent;
import cn.oa.model.EduTeacher;
import cn.oa.model.Org;
import cn.oa.model.ShopGoods;
import cn.oa.service.course.CourseService;
import cn.oa.service.course.CourseServiceImpl;
import cn.oa.service.student.StudentService;
import cn.oa.service.student.StudentServiceImpl;
import cn.oa.service.teacher.TeacherService;
import cn.oa.service.teacher.TeacherServiceImpl;
import cn.oa.utils.web.Page;

public class TestTeacherService extends Setup{
	//selectAll(Map<String, Object> map,Page<EduTeacher> page)
	@Test
	public void test1() throws Exception{
		TeacherService teacherService = ioc.get(TeacherServiceImpl.class, "teacherService");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cn.oa.model.Org.orgId", null);
		Page<EduTeacher> page = new Page<EduTeacher>(1, 20);
		page = teacherService.selectAll(map, page);
		System.out.println(page.getResult().get(1).getCorpName());
	}
	
	//selectAll(Map<String, Object> map,Page<EduTeacher> page)
	@Test
	public void test6() throws Exception{
		TeacherService teacherService = ioc.get(TeacherServiceImpl.class, "teacherService");
		Map<String, Object> map = new HashMap<String, Object>();
		ShopGoods subject = new ShopGoods();
		subject.setId(3);
		List<ShopGoods> subjects = new ArrayList<ShopGoods>();
		subjects.add(subject);
		map.put("cn.oa.model.EduTeacher.subjects", subjects);
		Page<EduTeacher> page = new Page<EduTeacher>(1, 20);
		page = teacherService.selectAll(map, page);
		System.out.println(page.getResult().size());
	}

	//selectAll(Org corp)
	@Test
	public void test5() throws Exception{
		TeacherService teacherService = ioc.get(TeacherServiceImpl.class, "teacherService");
		Org corp = new Org();
		corp.setOrgId(276);
		List<EduTeacher>  teachers = teacherService.selectAll(corp);
		System.out.println(teachers.size());
	}
	
	//insertCoopTeacher(Integer corpId, Integer teacherId, Integer coopCorpId)
	@Test
	public void test7() throws Exception{
		TeacherService teacherService = ioc.get(TeacherServiceImpl.class, "teacherService");
		teacherService.insertCoopTeacher(277, 1, 1);
	}
	
	//selectAll(Map<String, Object> map,Page<EduTeacher> page)
	@Test
	public void test8() throws Exception{
		TeacherService teacherService = ioc.get(TeacherServiceImpl.class, "teacherService");
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("start", "2017-06-30 00:00:00");
		map.put("end", "2017-09-19 23:59:59");
		map.put("cn.oa.model.Org.orgId", 276);
		Page<EduTeacher> page = new Page<EduTeacher>(1, 20);
		page = teacherService.selectAll(map, page);
		System.out.println(page.getResult().size());
	}
	
	//selectIds(Org corp)
	@Test
	public void test9() throws Exception{
		TeacherService teacherService = ioc.get(TeacherServiceImpl.class, "teacherService");
		teacherService.selectIds(275);
	}
	
	
}
