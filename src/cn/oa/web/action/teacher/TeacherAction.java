package cn.oa.web.action.teacher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Status;

import cn.oa.model.EduClassify;
import cn.oa.model.EduStudent;
import cn.oa.model.EduTeacher;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopGoods;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
import cn.oa.web.action.student.StudentAction;


@IocBean(name = "teacher.teacher")
@At(value = "teacher/teacher")
public class TeacherAction extends Action{
	public static Log log = Logs.getLog(TeacherAction.class);
	
	public static final int PAGE_SIZE = 100000;
	
	@POST
	@GET
	@At
	@Ok("ftl:teacher/teacher_showAll")
	public void showall(HttpServletRequest req) throws Exception{
		
//		String token = CSRF.generate(req);
//		CSRF.generate(req, "/sys/user/nodes", token);		
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);//公司id
		String trueName = Https.getStr(req, "trueName", R.CLEAN, R.RANGE, "{1,20}");
		Integer subjectId = Https.getInt(req, "subjectId", R.I); //学科id
		String start_yyyyMMdd = Https.getStr(req, "start_yyyyMMdd", R.CLEAN); //上课开始时间
		String end_yyyyMMdd = Https.getStr(req, "end_yyyyMMdd", R.CLEAN); //上课结束时间

		//用来回显
		MapBean mb = new MapBean();
		
		
		//默认查询所有的培训机构，以及其下的所有老师
		List<Org> corps = studentService.selectCorpsAll();
		//老师所属公司和登录用户所属公司的交集
		Integer  myCorpId = null;
		for(Org org : corps){
			//判断登录用户是否是培训机构的用户
			if(org.getOrgId().equals(Context.getCorpId())){
				List<Org> orgs = new ArrayList<Org>();
				orgs.add(org);
				corps = orgs;
				//只查询登录用户所属公司下的学生
				corpId = org.getOrgId();
				myCorpId = org.getOrgId();
			}
		}
		
		List<EduClassify> subjects = courseService.findAllSubjects();

		Map<String, Object> cnd = new HashMap<String, Object>();
		
		cnd.put("cn.oa.model.Org.orgId", corpId);
		if(corpId != null) mb.put("corpId", corpId);
		if(trueName != null && trueName != ""){
			System.out.println("\n\n\n\n\nName:" + trueName);
			cnd.put("cn.oa.model.EduTeacher.truename", trueName);
			mb.put("trueName", trueName);
		}
		if(subjectId != null){
			List<ShopGoods> subs = new ArrayList<ShopGoods>();
			ShopGoods subject = new ShopGoods();
			subject.setId(subjectId);
			subs.add(subject);
			cnd.put("cn.oa.model.EduTeacher.subjects", subs);
			mb.put("subjectId", subjectId);
		}
		if(start_yyyyMMdd != null && start_yyyyMMdd != "" && end_yyyyMMdd != null && end_yyyyMMdd != ""){
			cnd.put("start", start_yyyyMMdd+" 00:00:00");
			cnd.put("end", end_yyyyMMdd+" 23:59:59");
			mb.put("startStr", start_yyyyMMdd);
			mb.put("endStr", end_yyyyMMdd);
		}
		
		Page<EduTeacher> page = Webs.page(req);
		teacherService.selectAll(cnd, page);
		
		courseService.findAll(page.getResult());
		
		req.setAttribute("myCorpId", myCorpId);
		req.setAttribute("corps", corps);
		req.setAttribute("subjects", subjects);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
		
	}
	
	@POST
	@GET
	@At
	@Ok("ftl:teacher/teacher_showAll")
	public void page(HttpServletRequest req) throws Exception{
		showall(req);
	}
	
	@At
	@Ok("ftl:teacher/teacher_add")
	public void add(HttpServletRequest req) throws Exception{
		editpage(req);
	}
	
	@At
	@Ok("ftl:teacher/teacher_add")
	public void editpage(HttpServletRequest req) throws Exception{
//		CSRF.generate(req);
		Integer teacherId = Https.getInt(req, "teacherId", R.I);
		
		//登录用户和老师所属的公司的id，当不存时在为null，表示是平台管理员
		Integer myCorpId = null;
		EduTeacher teacher = new EduTeacher(); 
		
		//用来标志老师所属的公司是不是登录用户所属的公司
		Integer sameCorp = null;

		if(teacherId != null){
			teacher.setId(teacherId);
			teacher = teacherService.selectOne(teacher);
			Asserts.isNull(teacher, "不存在该id的老师");

			//查询所有的培训机构，然后判断登录用户所属的公司是否属于其中
			List<Org> corps = studentService.selectCorpsAll();
			for(Org org : corps){
				//判断登录用户是否是培训机构的用户
				if(org.getOrgId().equals(Context.getCorpId())){
					myCorpId = org.getOrgId();
				}
			}
			List<ShopGoods> courses = null;
			//区分出平台管理员还是培训机构的用户以进行不同的操作
			if(myCorpId == null){
				courses = courseService.findAll(teacher);
			}else{
				//判断老师所属的公司是不是登录用户所属的公司
				if(!teacher.getCorpId().equals(Context.getCorpId())){
					teacher.setCoopType("合作");
					sameCorp = 1;
				}
				courses = courseService.findCourseByTeacherId(teacher.getId()+"", Context.getCorpId()+"").getResult();
			}
			teacher.setCourses(courses);
			//获取老师所属的学科
			List<EduTeacher> list = new ArrayList<EduTeacher>();
			list.add(teacher);
			courseService.findAll(list);
			
		}else{
			//由于只有机构才可以新建,因此这个流程只有合作机构才会走
			Org corp = new Org();
			corp.setOrgId(Context.getCorpId());
			corp = teacherService.selectCorpOne(corp);
			teacher.setCorpId(corp.getOrgId());
			teacher.setCorpName(corp.getOrgName());
			myCorpId = corp.getOrgId();
		}
		
		req.setAttribute("sameCorp", sameCorp);
		req.setAttribute("myCorpId", myCorpId);
		req.setAttribute("teacher", teacher);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer studentId = null;
		try {
//			CSRF.validate(req);
			
			studentId = Https.getInt(req, "teacherId", R.I, "老师ID");
			Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I, "公司ID");
			Integer gender = Https.getInt(req, "gender", R.REQUIRED, R.I, R.IN, "in [0,1]", "性别");
			String phone = Https.getStr(req, "telephone", R.CLEAN,R.RANGE, "{1,20}", "联系电话");
			String address = Https.getStr(req, "address", R.CLEAN,R.RANGE, "{1,200}", "联系地址");
			String birthday_yyyyMMdd = Https.getStr(req, "birthday_yyyyMMdd", R.D, "出生年月");
			String truename = Https.getStr(req, "truename", R.REQUIRED, "老师姓名");
			String gainSharingType = Https.getStr(req, "gainSharingType", R.REQUIRED, "合作方式");
			String weixin = Https.getStr(req, "weixin", R.CLEAN,R.RANGE, "{1,20}", "微信");
			String status = Https.getStr(req, "status", R.I, "状态");
			if(status == null || status.equals("")) status = Status.ENABLED + "";
			DateTime birthday = Calendars.parse(birthday_yyyyMMdd, Calendars.DATE);
			
			EduTeacher teacher = new EduTeacher();
			teacher.setId(studentId);
			teacher.setSex(gender);
			teacher.setTelephone(phone);
			teacher.setAddress(address);
			teacher.setWeixin(weixin);
			teacher.setBirthday(birthday.toDate());
			teacher.setCoopType(gainSharingType);
			teacher.setTruename(truename);
			teacher.setCorpId(corpId);
			teacher.setStatus(Integer.valueOf(status));
			teacherService.addOrUpdateOne(teacher);
			Code.ok(mb, (studentId == null ? "新建" : "编辑") + "教师成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Student:add) error: ", e);
			Code.error(mb, "编辑失败");
		}
		return mb;
	}
	
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
//			CSRF.validate(req);
			
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");

			if (arr != null && arr.length > 0) {
				teacherService.able(arr, status);
			}
			Code.ok(mb, "设置老师状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Student:able) error: ", e);
			Code.error(mb, "设置老师状态失败");
		}

		return mb;
	}
	
	@At
	@Ok("ftl:teacher/weekcalendar_full_demo")
	public void schedulepage(HttpServletRequest req) throws Exception{
//		CSRF.generate(req);
		Integer teacherId = Https.getInt(req, "teacherId", R.I);
		EduTeacher teacher = new EduTeacher();
		teacher.setId(teacherId);
		//获取现在上一个月的第一天和下个月的最后一天
		DateTime dayDataTime = new DateTime();
		int dayOfMonth = dayDataTime.dayOfMonth().get();
		String start = Calendars.str(dayDataTime.plusDays(-dayOfMonth + 1).plusMonths(-1), "yyyy-MM-dd 00:00:00");
		String end = Calendars.str(dayDataTime.plusDays(-dayOfMonth + 1).plusMonths(2).plusDays(-1), "yyyy-MM-dd 23:59:59");
		
		//查询所有的培训机构，然后判断登录用户所属的公司是否属于其中
		List<Org> corps = studentService.selectCorpsAll();
		Org crop = null;
		for(Org org : corps){
			//判断登录用户是否是培训机构的用户
			if(org.getOrgId().equals(Context.getCorpId())){
				crop = new Org();
				crop.setOrgId(Context.getCorpId());
			}
		}

		List<EduTeacher> teachers = teachingSchedule.selectAll(start, end, teacher, crop);
		if(teachers.get(0).getEduTeachingSchedules() == null){
			req.setAttribute("teacher", teachers.get(0));
			return ;
		}
//		System.out.println(teachers.get(0).getEduTeachingSchedules().size());
		List<EduTeachingSchedule> schedules = teachers.get(0).getEduTeachingSchedules();
		//为适应页面，将月份减一，没有实际意义
		for(EduTeachingSchedule schedule : schedules){
			schedule.setMinusMonth(new DateTime(schedule.getEnd()).plusMonths(-1).monthOfYear().get());
		}
		System.out.println(schedules.size());
		System.out.println(schedules.get(0).getCourse().getName());
		req.setAttribute("schedules", schedules);
		req.setAttribute("teacher", teachers.get(0));
	}
}
