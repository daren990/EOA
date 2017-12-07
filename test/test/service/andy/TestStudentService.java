package test.service.andy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;


import test.Setup;

import cn.oa.model.EduStudent;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopGoods;
import cn.oa.service.student.StudentService;
import cn.oa.service.student.StudentServiceImpl;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;


public class TestStudentService extends Setup{
	
	//addOrUpdateOne()
	@Test
	public void test() throws Exception{
		StudentService studentService = ioc.get(StudentServiceImpl.class, "studentService");
		EduStudent student = new EduStudent();
		student.setId(13);
		student.setName("祖国的花朵3");
		student.setNumber("6666");
		ShopClient shopClient = new ShopClient();
		shopClient.setId(2);
		ShopGoods eduCourse = new ShopGoods();
		eduCourse.setId(2);
//		student.setShopClients(pack(shopClient));
//		student.setEduCourses(pack(eduCourse));
//		student.setShopClients(new ArrayList<ShopClient>());
//		student.setEduCourses(new ArrayList<EduCourse>());
//		student.setShopClients(null);
//		student.setEduCourses(null);
		studentService.addOrUpdateOne(student);
	}

	
	//buildRelationOfStudent()
	@Test
	public void test2() throws Exception{
		StudentService studentService = ioc.get(StudentServiceImpl.class, "studentService");
		EduStudent student = new EduStudent();
		student.setId(13);
//		student.setName("祖国的花朵");
//		student.setNumber("6666");
		ShopClient shopClient = new ShopClient();
		shopClient.setId(2);
		ShopGoods eduCourse = new ShopGoods();
		eduCourse.setId(2);
		Org org = new Org();
		org.setOrgId(276);
		student.setShopClients(pack(shopClient));
		student.setEduCourses(pack(eduCourse));
		student.setCorps(pack(org));
		studentService.buildRelationOfStudent(student);
	}
	
	//able()
	@Test
	public void test3() throws Exception{
		StudentService studentService = ioc.get(StudentServiceImpl.class, "studentService");
		EduStudent student = new EduStudent();
		student.setId(13);
		student.setStatus(1);
//		studentService.able(student);
	}
	
	
	//selectAll(org)
	@Test
	public void test4() throws Exception{
		StudentService studentService = ioc.get(StudentServiceImpl.class, "studentService");

		Map<String, Object> cnd = new HashMap<String, Object>();
		cnd.put("cn.oa.model.ShopClient.id", 2);
		Page<EduStudent> page = new Page<EduStudent>(1, 20);
		page = studentService.selectAll(cnd, page);
		System.out.println(page.getResult().size());
	}
	
	//selectAll(client)
	@Test
	public void test5() throws Exception{
		StudentService studentService = ioc.get(StudentServiceImpl.class, "studentService");
		ShopClient client = new ShopClient();
		client.setId(2);
		List<EduStudent> students = studentService.selectAll(client);
		System.out.println(students.size());
	}
	
	//insertSign（）
	@Test
	public void test6() throws Exception{
		StudentService studentService = ioc.get(StudentServiceImpl.class, "studentService");
		studentService.insertSign();
	}
	
	private <T> List<T> pack(T element){
		List<T> list = new ArrayList<T>();
		list.add(element);
		return list;
	}
}
