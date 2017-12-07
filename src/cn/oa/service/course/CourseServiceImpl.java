package cn.oa.service.course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;

import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;

import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.consts.Status;

import cn.oa.model.EduClassify;
import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentCourse;
import cn.oa.model.EduTeacher;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.Org;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopGoodsDisplay;
import cn.oa.model.StudentCourseCount;
import cn.oa.repository.Mapper;
import cn.oa.utils.Asserts;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import junit.framework.Assert;

@IocBean(name="courseService")
public class CourseServiceImpl implements CourseService
{
	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	
	@Override
	public Page<ShopGoods> findCourseByTeacherId(String tId, String cId) 
	{
		Page<ShopGoods> page = new Page<ShopGoods>(1, 1000);
		page.setAutoCount(false);
		Asserts.isNull(tId, "teacherId:老师ID为空");
		Asserts.isNull(cId, "corpId:组织架构ID为空");
		page = mapper.page(ShopGoods.class, page, "TeacherAndCorp.count", "TeacherAndCorp.index", 
			Cnd.where("c.edu_teacher_id","=",tId)
			   .and("c.corp_id","=",cId)
			   .and("c.status","=",Status.ENABLED));
		return page;
	}

	@Override
	public Page<ShopGoods> findCourseByStudentId(String sId, String cId) 
	{
		Page<ShopGoods> page = new Page<ShopGoods>(1, 1000);
		page.setAutoCount(false);
		Asserts.isNull(sId, "studentId:学生ID为空");
		Asserts.isNull(cId, "corpId:组织架构ID为空");
		page = mapper.page(ShopGoods.class, page, "StudentAndCorp.count", "StudentAndCorp.index", 
				Cnd.where("s.id","=",sId)
				   .and("c.corp_id","=",cId)
				   .and("c.status","=",Status.ENABLED));
		return page;
	}

	@Override
	public List<ShopGoods> findAll(EduTeacher teacher) {
		Asserts.isNull(teacher.getId(), "老师ID不能为空");
		List<ShopGoods> eduCourses = dao.query(ShopGoods.class, Cnd.where("eduTeacherId", "=", teacher.getId()).and("status", "=", 1));
		return eduCourses;
	}

	@Override
	public List<ShopGoods> findAll(EduStudent student) {
		Asserts.isNull(student.getId(), "学生ID不能为空");
		List<EduStudentCourse> studentCourses = dao.query(EduStudentCourse.class, Cnd.where("eduStudentId", "=", student.getId()));
		List<ShopGoods> courses = new ArrayList<ShopGoods>();
		for(EduStudentCourse eduStudentCourse : studentCourses){
			dao.fetchLinks(eduStudentCourse, "course");
			courses.add(eduStudentCourse.getCourse());
		}
		return courses;
	}

	@Override
	public List<EduTeacher> findAll(List<EduTeacher> teachers) {
		if(teachers == null || teachers.size() == 0) return null;
		List<Integer> ids = new ArrayList<Integer>();
		//将老师的id抽取出来
		for(int x = 0; x < teachers.size(); x++){
			System.out.println(teachers.get(x));
			ids.add(teachers.get(x).getId());
		}

		Map<Integer, List<ShopGoods>> map = new HashMap<Integer, List<ShopGoods>>();
		List<ShopGoods> eduCourses = dao.query(ShopGoods.class, Cnd.where("eduTeacherId", "in", ids).and("status", "=", 1).and("type", "=", "课程").groupBy("edu_teacher_id,edu_classify_id"));
		if(eduCourses.size() == 0){
			return teachers;
		}
		
		Integer[] courseIds = new Integer[eduCourses.size()];
		int z = 0;
		//将eduCourses根据eduTeacherId进行分类放置,并且将科目的id抽取出来
		for(ShopGoods xxx : eduCourses){
			List<ShopGoods> value =null;
			if(map.get(xxx.getEduTeacherId()) == null){
				value = new ArrayList<ShopGoods>();
				map.put(xxx.getEduTeacherId(), value);
			}else{
				value = map.get(xxx.getEduTeacherId());
			}
			value.add(xxx);
			courseIds[z] = xxx.getEduClassifyId();
			z++;
		}
		
//		Integer[] courseIds = new Integer[eduCourses.size()];
//		for(int x = 0; x < eduCourses.size(); x++){
//			courseIds[x] = eduCourses.get(x).getParentId();
//		}
		//查询所有学科并将id抽取出来,目的是通过id找到对应的学科
		List<EduClassify> subjects = dao.query(EduClassify.class, Cnd.where("id", "in", courseIds).and("status", "=", 1));
		List<Integer> subjectIds = new ArrayList<Integer>();
		for(int x = 0; x < subjects.size(); x++){
			subjectIds.add(subjects.get(x).getId());
		}
		
		//将上面的map中的value中的元素进行替换，根据parentId替换成学科,而且注入了相应的老师中
		Set<Integer> teacherIds = map.keySet();
		for(Integer id : teacherIds){
			List<EduClassify> classifies = new ArrayList<EduClassify>();
			List<ShopGoods> courses = map.get(id);
			for(int x = 0; x < courses.size(); x++){
				int index = subjectIds.indexOf(courses.get(x).getEduClassifyId());
				if(index != -1){
					classifies.add(subjects.get(index));
				}
			}
			int indexT = ids.indexOf(id);
			EduTeacher teacher = teachers.get(indexT);
			teacher.setSubjects(classifies);
		}
		return teachers;
	}

	@Override
	public List<EduClassify> findAllSubjects() {
		List<EduClassify> subjects = dao.query(EduClassify.class, Cnd.where("type", "=", "学科").and("status", "=", Status.ENABLED));
		return subjects;
	}

	@Override
	public List<ShopGoods> findAll(Org corp) {
		Asserts.isNull(corp.getOrgId(), "请输入公司的id");
		List<ShopGoods> courses = dao.query(ShopGoods.class, Cnd.where("corpId", "=", corp.getOrgId()));
		return courses;
	}

	@Override
	public List<ShopGoods> findAll(Org corp, List<ShopGoods> subjects) {
		Asserts.isNull(corp.getOrgId(), "请输入公司的id");
		Asserts.isNull(subjects, "请输入学科");
		List<Integer> coursePids = new ArrayList<Integer>();
		for(ShopGoods subject : subjects){
			coursePids.add(subject.getId());
		}
		List<ShopGoods> courses = dao.query(ShopGoods.class, Cnd.where("corpId", "=", corp.getOrgId()).and("parentId", "in", coursePids).and("status", "=", Status.ENABLED));
		//获取课程对应的老师，并注入课程中
//		List<Integer> teacherIds = new ArrayList<Integer>();
//		for(EduCourse course : courses){
//			teacherIds.add(course.getEduTeacherId());
//		}
//		List<EduTeacher> teachers = dao.query(EduTeacher.class, Cnd.where("corpId", "=", corp.getOrgId()).and("id", "in", teacherIds).and("status", "=", Status.ENABLED));
//		for(EduTeacher teacher : teachers){
//			int index = teacherIds.indexOf(teacher.getId());
//			EduCourse course = courses.get(index);
//			course.setTeacher(teacher);
//		}
		return courses;
	}

	@Override
	public List<ShopGoods> findCoop(Integer corp) {
		SimpleCriteria cri = Cnd.cri();
		if(corp != null){
			cri.where().and("corp_id", "=", corp);
		}
		cri.where().and("cooptype", "=", "合作");
		cri.groupBy("corp_id,edu_teacher_id");
		cri.where().andNotIsNull("eduTeacherId");
		cri.where().and("status", "=", Status.ENABLED);
		return dao.query(ShopGoods.class, cri);
	}


	@Override
	public Map<Integer, List<EduTeachingSchedule>> findAll(String start, String end) {
		Sql sql = Sqls.queryEntity(dao.sqls().get("course.teachingSchedule.query"));
		
		SimpleCriteria cri = Cnd.cri();
		cri.where().and("ts.status", "=", Status.ENABLED);
		cri.where().and("ts.start", ">", start);
		cri.where().and("ts.end", "<", end);
		cri.where().and("c.status", "=", Status.ENABLED);
		sql.setCondition(cri);
		sql.setCallback(Sqls.callback.entities());
		sql.setEntity(dao.getEntity(EduTeachingSchedule.class));
		dao.execute(sql);
		
		List<EduTeachingSchedule> teachingSchedules = sql.getList(EduTeachingSchedule.class);
		Map<Integer, List<EduTeachingSchedule>> shopGoodsMap = new HashMap<Integer, List<EduTeachingSchedule>>();
		if(teachingSchedules != null){
			for(EduTeachingSchedule ts :  teachingSchedules){
				List<EduTeachingSchedule> tss = shopGoodsMap.get(ts.getCourseId());
				if(tss == null){
					tss = new ArrayList<EduTeachingSchedule>();
					shopGoodsMap.put(ts.getCourseId(), tss);
				}
				tss.add(ts);
			}
		}else{
			return null;
		}
		return shopGoodsMap;
	}

	@Override
	public List<ShopGoodsDisplay> findCourseDisplay(Integer student_id,Integer course_id) {
		
		StringBuffer str = new StringBuffer("SELECT g.id,g.dependId,g.name,g.coucount,g.price,g.location,g.couTime,g.startDate,g.sold,"
				+ "t.truename as teacherName,t.telephone as teacherTelephone,"
				+ "o.org_name as corpName,g.status as isOver,countAppear "
				+ "from shop_goods g "
				+ "LEFT JOIN edu_teacher t on g.edu_teacher_id = t.id  "
				+ "LEFT JOIN sec_org o on g.corp_id = o.org_id "
				+ "LEFT JOIN edu_student_course sc on g.id = sc.edu_course_id "
				+ "LEFT JOIN (select g.id as gid,count(*) as countAppear from edu_student_sign s,shop_goods g where s.course_id = g.id and s.student_id = " + student_id + " and g.id = " + course_id + " and s.isCome != 0) s on gid = g.id "
//				+ "LEFT JOIN (select g.id as sgid,count(*) as countDisAppear from edu_student_sign s,shop_goods g where s.course_id = g.id and s.student_id = " + student_id + " and g.id = " + course_id + " and s.isCome = 0) sg on sgid = g.id "
				+ "WHERE sc.edu_student_id = " + student_id + " "
				+ "and g.id = " + course_id + " "
				+ "and g.status != 0 "
				+ "ORDER BY g.status asc,g.startDate DESC ");
		
		List<ShopGoodsDisplay> nodes = new ArrayList<ShopGoodsDisplay>();
        
		Sql sql = Sqls.create(str.toString());
		//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
		
		sql.setCallback(Sqls.callback.entities());
		Entity<ShopGoodsDisplay> entity = dao.getEntity(ShopGoodsDisplay.class);
	    sql.setEntity(entity);
	    dao.execute(sql);
		nodes = sql.getList(ShopGoodsDisplay.class);
		
		return nodes;
	}



	@Override
	public Map<Integer, ShopGoods> findNeedDepend(Set<Integer> courseIds, Map<Integer, List<ShopGoods>> shopGoodsMap) {
		if(shopGoodsMap == null){
			throw new RuntimeException("请传入用来对课程进行分类的容器");
		}
		List<ShopGoods> shopGoodses = dao.query(ShopGoods.class, Cnd.where("dependId", "in", courseIds).and("status", "=", Status.ENABLED));
		Map<Integer,ShopGoods> map = new HashMap<Integer, ShopGoods>();
		if(shopGoodses != null){
			for(ShopGoods shopGoods : shopGoodses){
				map.put(shopGoods.getId(), shopGoods);
				List<ShopGoods> gs = shopGoodsMap.get(shopGoods.getDependId());
				if(gs == null){
					gs = new ArrayList<ShopGoods>();
					shopGoodsMap.put(shopGoods.getDependId(), gs);
				}
				gs.add(shopGoods);
			}
		}else{
			return null;
		}
		return map;
	}




}
