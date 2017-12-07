package cn.oa.service.teacher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.trans.Molecule;

import cn.oa.consts.Status;
import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentClient;
import cn.oa.model.EduTeacher;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopClientCorp;
import cn.oa.model.ShopGoods;
import cn.oa.repository.Mapper;
import cn.oa.service.course.CourseService;
import cn.oa.service.schedule.TeachingSchedule;
import cn.oa.service.student.StudentService;
import cn.oa.service.student.StudentServiceImpl;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.web.Page;

@IocBean(name="teacherService")
public class TeacherServiceImpl implements TeacherService{
	
	@Inject
	private Dao dao;
	
	@Inject
	private Mapper mapper;
	
	@Inject
	private TeachingSchedule teachingSchedule;
	
	@Inject
	private CourseService courseService;
	

	@Override
	public Page<EduTeacher> selectAll(final Map<String, Object> map,
			Page<EduTeacher> page) throws Exception {
		SimpleCriteria cri = Cnd.cri();
		List<Integer> coopTeacherIds = new ArrayList<Integer>();
		
		if(map.containsKey("cn.oa.model.Org.orgId")){
			if(map.get("cn.oa.model.Org.orgId") != null){
				Org crop = new Org();
				crop.setOrgId((Integer)(map.get("cn.oa.model.Org.orgId")));
				
				//根据公司获取所有与其合作的老师,一个EduCourse中包含的老师id和公司id就表示他们之间有合作关系
				List<ShopGoods> courses = selectIds((Integer)(map.get("cn.oa.model.Org.orgId")));
				if(courses != null && courses.size() != 0){
					for(ShopGoods course : courses){
						coopTeacherIds.add(course.getEduTeacherId());
					}
					cri.where().and(new Static("(true")).and("t.id", "in", coopTeacherIds).or("t.corp_id", "=", (Integer)(map.get("cn.oa.model.Org.orgId"))).and(new Static("true)"));
				}else{
					cri.where().and("t.corp_id", "=", (Integer)(map.get("cn.oa.model.Org.orgId")));
				}
				
			}else{
				//查询所有的非合作类型的老师，应该先进行权限的校验,待实现
				cri.where().and("t.coopType", "!=", "合作");
			}
		}
		
		if(map.containsKey("cn.oa.model.EduTeacher.truename")){
			if(map.get("cn.oa.model.EduTeacher.truename") != null){
				Asserts.isNull(map.get("cn.oa.model.EduTeacher.truename"), "没有指定老师的名称");
				cri.where().and("t.truename", "like", "%"+map.get("cn.oa.model.EduTeacher.truename")+"%");
			}
		}

		if(map.containsKey("cn.oa.model.EduTeacher.subjects")){
			Asserts.isNull(map.get("cn.oa.model.EduTeacher.subjects"), "没有指定学科的ids");
			List<ShopGoods> subjects = (List<ShopGoods>)map.get("cn.oa.model.EduTeacher.subjects");
			List<Integer> subjectsIds = new ArrayList<Integer>();
			for(ShopGoods subject : subjects){
				subjectsIds.add(subject.getId());
			}

			List<ShopGoods> courses = dao.query(ShopGoods.class, Cnd.where("eduClassifyId", "in", subjectsIds).and("type", "=", "课程").and("status", "=", Status.ENABLED).groupBy("edu_teacher_id,edu_classify_id"));
			List<Integer> teacherIds = new ArrayList<Integer>();
			for(ShopGoods course : courses){
				teacherIds.add(course.getEduTeacherId());
			}
			if(teacherIds.size() != 0){
				cri.where().and("t.id", "in", teacherIds);
			}else{
				return page;
			}
		}
		if(map.containsKey("start") && map.containsKey("end")){
			if(map.get("start") == null || map.get("end") == null){
				throw new RuntimeException("请指定正确上课的时间");
			}
			Integer orgId = (Integer)map.get("cn.oa.model.Org.orgId");
			Org crop = new Org();
			crop.setOrgId(orgId);
			List<Integer> teacherIds = teachingSchedule.selectBySchedule((String)(map.get("start")), (String)(map.get("end")), crop);
			if(teacherIds.size() != 0){
				cri.where().and("t.id", "in", teacherIds);
			}else{
				return page;
			}
		}
		page = mapper.page(EduTeacher.class, page, "Teacher.count", "Teacher.index",  cri);
		replaceCoopTeacher(page, coopTeacherIds);
		return page;
	}
	
	//将合作类型的老师替换成真正的老师
	private Page<EduTeacher> findCoopTeacher(Page<EduTeacher> page){
		List<EduTeacher> teachers = page.getResult();
		//记录合作老师的id和index
		Map<Integer, List<Integer>> coopTeacherIds = new TreeMap<Integer, List<Integer>>();
		for(int x = 0; x < teachers.size() ; x++){
			if("合作".equals(teachers.get(x).getCoopType())){
				if(coopTeacherIds.get(teachers.get(x).getCoopTeacherId()) == null){
					List<Integer> list = new ArrayList<Integer>();
					coopTeacherIds.put(teachers.get(x).getCoopTeacherId(), list);
				}
				List<Integer> list = coopTeacherIds.get(teachers.get(x).getCoopTeacherId());
				list.add(x);
				
			}
		}
		SimpleCriteria cri = Cnd.cri();
		cri.where().and("id", "in", coopTeacherIds.keySet());
		cri.where().and("status", "=", Status.ENABLED);
		cri.asc("id");
		//根据id从小到大排序
		List<EduTeacher> coopTeachers = dao.query(EduTeacher.class, cri);
		
		//teacherId从小到大遍历
		int x = 0; //用来控制定位到，查询得到的集合中的哪个元素
		List<EduTeacher> remove = new ArrayList<EduTeacher>();
		for(Integer teacherId : coopTeacherIds.keySet()){
			List<Integer> indexs = coopTeacherIds.get(teacherId);
			boolean flag = true;
			//充分遍历查询得到的每一个元素，用来替换teachers中合作类型的老师，coopTeachers中存在的，teachers中一定存在
			for(; x < coopTeachers.size();){
				//teacherId不一定跟查询得到的老师对象中的id相同，因为掺和其它查询条件
				if(coopTeachers.get(x).getId().equals(teacherId)){
					flag = false;
					for(Integer index : indexs){
						coopTeachers.get(x).setCoopType("合作");
						teachers.set(index, coopTeachers.get(x));
					}
					x++;
					break;
				}else{
					break;
				}
			}
			//将找不到真正老师的元素从teachers中剔除
			if(flag){
				for(Integer index : indexs){
					remove.add(teachers.get(index));
				}
			}
		}
		teachers.removeAll(remove);
		return page;
	}
	
	private Page<EduTeacher> replaceCoopTeacher(Page<EduTeacher> page, List<Integer> coopTeacherIds){
		List<EduTeacher> teachers = page.getResult();
		if(coopTeacherIds.size() != 0){
			for(EduTeacher teacher : teachers){
				if(coopTeacherIds.contains((teacher.getId()))){
					teacher.setCoopType("合作");
				}
			}
		}
		return page;
	}
	
	

	@Override
	public EduTeacher selectOne(EduTeacher teacher) throws Exception {

		teacher =  dao.fetch(EduTeacher.class, Cnd.where("id", "=", teacher.getId()).and("status", "=", Status.ENABLED));
		dao.fetchLinks(teacher, "corp");
		return teacher;
	}


	@Override
	public boolean addOrUpdateOne(EduTeacher teacher) throws Exception {
		if(teacher.getId() == null){
			dao.insert(teacher);
		}else{
			dao.updateIgnoreNull(teacher);
		}
		return true;
	}

	@Override
	public boolean able(Integer[] arr, Integer status) throws Exception {
		dao.update(EduTeacher.class, Chain.make("status", status), Cnd.where("id", "in", arr));
		return true;
	}

	@Override
	public Org selectCorpOne(Org corp) throws Exception {
		corp = dao.fetch(Org.class, corp.getOrgId());
		return corp;
	}

	@Override
	public List<EduTeacher> selectAll(Org corp) throws Exception {
		Asserts.isNull(corp.getOrgId(), "公司的id不能为null");
		SimpleCriteria cri = Cnd.cri();
		cri.where().and("corp_id", "=", corp.getOrgId());
		cri.where().and("status", "=", Status.ENABLED);
		return dao.query(EduTeacher.class, cri);
	}

	@Override
	public boolean insertCoopTeacher(Integer corpId, Integer coopTeacherId,
			Integer coopCorpId) throws Exception {
		EduTeacher teacher = dao.fetch(EduTeacher.class, Cnd.where("status", "=", Status.ENABLED).and("corpId", "=", corpId).and("coopTeacherId", "=", coopTeacherId).and("coopType", "=", "合作"));
		if(teacher == null){
			teacher = new EduTeacher();
			teacher.setStatus(Status.ENABLED);
			teacher.setCorpId(corpId);
			teacher.setCoopType("合作");
			teacher.setCoopId(coopCorpId);
			teacher.setCoopTeacherId(coopTeacherId);
			dao.insert(teacher);
		}
		return true;
	}

	@Override
	public List<ShopGoods> selectIds(Integer corpId) throws Exception {
//		Sql sql = Sqls.queryEntity(dao.sqls().get("Teacher.query.ids"));
		
//		if(corp != null && corp.getOrgId() != null){
//			sql.setCondition(Cnd.where("t.corp_id", "=", corp.getOrgId()).and("status", "=", 1));
//		}
//		sql.setCallback(Sqls.callback.entities());
//		sql.setEntity(dao.getEntity(EduTeacher.class));
//		dao.execute(sql);
//		List<EduTeacher> ids = sql.getList(EduTeacher.class);
		return courseService.findCoop(corpId);
	}


	


}
