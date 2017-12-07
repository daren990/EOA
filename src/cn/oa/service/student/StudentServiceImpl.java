package cn.oa.service.student;

import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
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
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.OrgType;
import cn.oa.consts.Status;
import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentCheck;
import cn.oa.model.EduStudentClient;
import cn.oa.model.EduStudentCorp;
import cn.oa.model.EduStudentCourse;

import cn.oa.model.EduStudentWechatrelation;

import cn.oa.model.EduStudentSign;
import cn.oa.model.EduStudentSignTemp;

import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopClientCorp;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopWechatrelation;
import cn.oa.model.User;
import cn.oa.repository.Mapper;
import cn.oa.service.WxClientService;
import cn.oa.service.client.ClientService;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.wx.Config;

@IocBean(name="studentService")
public class StudentServiceImpl implements StudentService {
	
	@Inject
	private Dao dao;
	
	@Inject
	private Mapper mapper;
	
	@Inject
	private ClientService clientService;
	
	@Inject
	private WxClientService wxClientService;
	
	/**
	 * 添加一个学生，包括关联关系
	 * @param student
	 * @return
	 * @throws Exception
	 */
	private boolean addOne(final EduStudent student) throws Exception {
		student.setStatus(Status.ENABLED);
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.insert(student);
				List<ShopClient> shopClients = student.getShopClients();
				changeRelation(shopClients, student.getId(), ShopClient.class, false);
				List<ShopGoods> eduCourses = student.getEduCourses();
				changeRelation(eduCourses, student.getId(), ShopGoods.class, false);
				List<Org> orgs = student.getCorps();
				changeRelation(orgs, student.getId(), Org.class, false);
				
				//增量更新客户和公司之间的关系
				if(shopClients != null && orgs != null){
					for(ShopClient shopClient : shopClients){
						try {
							shopClient.setCorps(orgs); 
							clientService.buildRelation(shopClient);
						} catch (Exception e) {
							System.out.println("在增量更新客户和公司之间的关系的时候出错了:"+e.getMessage());
							e.printStackTrace();
						}
				
					}
				}
			}
		});
		return true;
	}
	
	/**
	 * 校验一个学生有没有指定关联关系
	 * @param student
	 * @return
	 */
	private boolean validateRelation(EduStudent student){
		if(student.getShopClients() == null || student.getEduCourses() == null){
			throw new RuntimeException("请指定学生所要绑定的客户和课程");
		}
		return true;
	}

	@Override
	public boolean buildRelationOfStudent(final EduStudent student) throws Exception {
		if(student.getId() != null){
			final EduStudent s = dao.fetch(EduStudent.class, student.getId());
			if(s == null){
				throw new RuntimeException("不存在该id的学生");
			}
			Trans.exec(new Atom() {
				@Override
				public void run() {
					//查询已经存在的关联关系
					List<EduStudentClient> eduStudentClients = dao.query(EduStudentClient.class, Cnd.where("eduStudentId", "=", s.getId()));
					List<EduStudentCourse> eduStudentCourses= dao.query(EduStudentCourse.class, Cnd.where("eduStudentId", "=", s.getId()));
					List<EduStudentCorp> eduStudentCorps= dao.query(EduStudentCorp.class, Cnd.where("eduStudentId", "=", s.getId()));
					if(eduStudentClients == null ){eduStudentClients = new ArrayList<EduStudentClient>();}
					if(eduStudentCourses == null ){eduStudentCourses = new ArrayList<EduStudentCourse>();}
					if(eduStudentCorps == null ){eduStudentCorps = new ArrayList<EduStudentCorp>();}
					
					//增量更新学生和客户的关系
					List<ShopClient> shopClients = student.getShopClients();
					List<ShopClient> shopClientsCopy = copy(shopClients);
					if(shopClients != null){
						List<ShopClient> remove1 = new ArrayList<ShopClient>();
						
						for(ShopClient shopClient : shopClients){
							if(shopClient.getId() == null){
								try {
									clientService.addOrUpdate(shopClient);
								} catch (Exception e) {
									System.out.println("新增客户的时候出错了");
									e.printStackTrace();
								}
								continue;
							}
							//判断数据库中是否已经存在客户和学生的关联
							for(EduStudentClient existEduStudentClient : eduStudentClients){
								if(shopClient.getId().equals(existEduStudentClient.getShopClientId())){
									remove1.add(shopClient);
								}
							}
						}
						shopClients.removeAll(remove1);
						changeRelation(shopClients, s.getId(), ShopClient.class, false);
					}

					
					//增量更新学生和课程的关系
					List<ShopGoods> eduCourses = student.getEduCourses();
					if(eduCourses != null){
						List<ShopGoods> remove2 = new ArrayList<ShopGoods>();
						for(ShopGoods eduCourse : eduCourses){
							//判断数据库中是否已经存在课程和学生的关联
							for(EduStudentCourse existEduStudentCourse : eduStudentCourses){
								if(eduCourse.getId().equals(existEduStudentCourse.getEduCourseId())){
									remove2.add(eduCourse);
								}
							}
						}
						eduCourses.removeAll(remove2);
						changeRelation(eduCourses, s.getId(), ShopGoods.class, false);
					}

					
					//增量更新学生和公司的关系
					List<Org> corps = student.getCorps();
					List<Org> corpsCopy = copy(corps);
					if(corps != null){
						List<Org> remove3 = new ArrayList<Org>();
						for(Org org : corps){
							//判断数据库中是否已经存在课程和学生的关联
							for(EduStudentCorp existEduStudentCorp : eduStudentCorps){
								if(org.getOrgId().equals(existEduStudentCorp.getCorpId())){
									remove3.add(org);
								}
							}
						}
						corps.removeAll(remove3);
						changeRelation(corps, s.getId(), Org.class, false);
					}
					
					//增量更新客户和公司之间的关系
					if(shopClientsCopy != null && corpsCopy != null){
						for(ShopClient shopClient : shopClientsCopy){
							try {
								shopClient.setCorps(corpsCopy); 
								clientService.buildRelation(shopClient);
							} catch (Exception e) {
								System.out.println("在增量更新客户和公司之间的关系的时候出错了:"+e.getMessage());
								e.printStackTrace();
							}
					
						}
					}

				}
			});
		}else{
			//没有id号，视为新增一个学生，并且为其绑定各种关联关系
			addOne(student);
		}
		return true;
	}
	
	
	/**
	 * 全量更新一个学生，包括关联关系
	 * @param student
	 * @return
	 * @throws Exception
	 */
	private boolean updateOne(final EduStudent student) throws Exception {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.update(student);
				List<ShopClient> shopClients = student.getShopClients();
				changeRelation(shopClients, student.getId(), ShopClient.class, true);
				List<ShopGoods> eduCourses = student.getEduCourses();
				changeRelation(eduCourses, student.getId(), ShopGoods.class, true);
				List<Org> orgs = student.getCorps();
				changeRelation(orgs, student.getId(), Org.class, true);
				
				//增量更新客户和公司之间的关系
				if(shopClients != null && orgs != null){
					for(ShopClient shopClient : shopClients){
						try {
							shopClient.setCorps(orgs); 
							clientService.buildRelation(shopClient);
						} catch (Exception e) {
							System.out.println("在增量更新客户和公司之间的关系的时候出错了:"+e.getMessage());
							e.printStackTrace();
						}
				
					}
				}
			}
		});
		return true;
	}

	@Override
	public boolean addOrUpdateOne(EduStudent student) throws Exception {
		if(student.getId() == null){
			addOne(student);
		}else{
			updateOne(student);
		}
		return true;
	}
	
	
	/**
	 * 全量或增量更新一个学生的各种关联关系
	 * @param student
	 * @return
	 * @throws Exception
	 */
	private <T> void changeRelation(Collection<T> list, Integer studentId, Class<T> type, boolean clear){
		if(list != null){
			if(type.toString().contains("cn.oa.model.ShopClient")){
				List<EduStudentClient> eduStudentClients = new ArrayList<EduStudentClient>();
				for(T s : list){
					ShopClient shopClient = (ShopClient)s;
					EduStudentClient eduStudentClient = new EduStudentClient();
					eduStudentClient.setEduStudentId(studentId);
					eduStudentClient.setShopClientId(shopClient.getId());
					eduStudentClients.add(eduStudentClient);
				}
				if(clear){
					dao.clear(EduStudentClient.class, Cnd.where("eduStudentId", "=", studentId));
				}
				dao.fastInsert(eduStudentClients);
			}else if(type.toString().contains("cn.oa.model.EduCourse")){
				List<EduStudentCourse> eduStudentCourses = new ArrayList<EduStudentCourse>();
				for(T e : list){
					ShopGoods eduCourse = (ShopGoods)e;
					EduStudentCourse eduStudentCourse = new EduStudentCourse();
					eduStudentCourse.setEduStudentId(studentId);
					eduStudentCourse.setEduCourseId(eduCourse.getId());
					eduStudentCourses.add(eduStudentCourse);
				}
				if(clear){
					dao.clear(EduStudentCourse.class, Cnd.where("eduStudentId", "=", studentId));
				}
				dao.fastInsert(eduStudentCourses);
			}else if(type.toString().contains("cn.oa.model.Org")){
				List<EduStudentCorp> eduStudentCorps = new ArrayList<EduStudentCorp>();
				for(T e : list){
					Org corp = (Org)e;
					EduStudentCorp eduStudentCorp = new EduStudentCorp();
					eduStudentCorp.setEduStudentId(studentId);
					eduStudentCorp.setCorpId(corp.getOrgId());
					eduStudentCorps.add(eduStudentCorp);
				}
				if(clear){
					dao.clear(EduStudentCorp.class, Cnd.where("eduStudentId", "=", studentId));
				}
				dao.fastInsert(eduStudentCorps);
			}

		}
	}

	@Override
	public boolean able(Integer[] arr, Integer status) throws Exception {
		dao.update(EduStudent.class, Chain.make("status", status), Cnd.where("id", "in", arr));
		return true;
	}

	@Override
	public Page<EduStudent> selectAll(Map<String, Object> map, Page<EduStudent> page) throws Exception {
		SimpleCriteria cri = Cnd.cri();
		if(map.containsKey("cn.oa.model.Org.id")){
			if(map.get("cn.oa.model.Org.id") != null){
				List<EduStudentCorp> eduStudentCorps = dao.query(EduStudentCorp.class, Cnd.where("corpId", "=", map.get("cn.oa.model.Org.id")));
				if(eduStudentCorps == null || eduStudentCorps.size() == 0 ){
					return null;
				}
				String[] ids = Converts.array(EduStudentCorp.class, String.class, eduStudentCorps, "eduStudentId");
				cri.where().and("id", "in", ids);
//				List<EduStudent> students = dao.query(EduStudent.class, Cnd.where("id", "in", ids).and("status", "=", Status.ENABLED));
			}else{
				//应该先进行权限的校验,待实现
				
//				cri.where().and("status", "=", Status.ENABLED);
			}
		}
		if(map.containsKey("cn.oa.model.EduStudent.name") ){
			Asserts.isNull(map.get("cn.oa.model.EduStudent.name"), "没有指定学生的名称");
			cri.where().and("name", "like", "%"+map.get("cn.oa.model.EduStudent.name")+"%");
		}
		if(map.containsKey("cn.oa.model.EduStudent.id") && map.size() == 1){
			Asserts.isNull(map.get("cn.oa.model.EduStudent.id"), "没有指定学生的id");
			cri.where().and("id", "=", map.get("cn.oa.model.EduStudent.id")).and("status", "=", Status.ENABLED);
		}
		if(map.containsKey("cn.oa.model.ShopClient.id") && map.size() == 1){
			Asserts.isNull(map.get("cn.oa.model.ShopClient.id"), "没有指定客户的id");
			List<EduStudentClient> eduStudentClients = dao.query(EduStudentClient.class, Cnd.where("shopClientId", "=", map.get("cn.oa.model.ShopClient.id")));
			if(eduStudentClients != null && !(eduStudentClients.size() == 0) ){
				String[] ids = Converts.array(EduStudentClient.class, String.class, eduStudentClients, "eduStudentId");
				cri.where().and("status", "=", Status.ENABLED).and("id", "in", ids);
			}
		}
		if(map.containsKey("cn.oa.model.EduStudent.number")){
			Asserts.isNull(map.get("cn.oa.model.EduStudent.number"), "没有指定学生的学号");
			cri.where().and("number", "like", "%"+(map.get("cn.oa.model.EduStudent.number")+"%"));
		}
		page = mapper.page(EduStudent.class, page, "Student.count", "Student.index",  cri);
		return page;
	}

	@Override
	public EduStudent selectOne(EduStudent student) throws Exception {
		Asserts.isNull(student.getId(), "没有指定学生的id");
		return dao.fetch(EduStudent.class, student.getId());
	}

	@Override
	public List<EduStudent> selectAll(ShopClient client) throws Exception {
		Asserts.isNull(client.getId(), "没有指定客户的id");
		List<EduStudentClient> eduStudentClients = dao.query(EduStudentClient.class, Cnd.where("shopClientId", "=", client.getId()));
		if(eduStudentClients == null || eduStudentClients.size() == 0 ){
			return null;
		}
		String[] ids = Converts.array(EduStudentClient.class, String.class, eduStudentClients, "eduStudentId");
		return dao.query(EduStudent.class, Cnd.where("id", "in", ids).and("status", "=", Status.ENABLED));
	}
	
	@Override
	public List<EduStudent> selectAll(ShopClient client, Integer status)
			throws Exception {
		Asserts.isNull(client.getId(), "没有指定客户的id");
		List<EduStudentClient> eduStudentClients = dao.query(EduStudentClient.class, Cnd.where("shopClientId", "=", client.getId()));
		String[] ids = Converts.array(EduStudentClient.class, String.class, eduStudentClients, "eduStudentId");
		if(eduStudentClients == null || eduStudentClients.size() == 0 ){
			return null;
		}
		if(status == null || status == -1){
			return dao.query(EduStudent.class, Cnd.where("id", "in", ids));
		}else if(status.equals(1)){
			return dao.query(EduStudent.class, Cnd.where("id", "in", ids).and("status", "=", Status.ENABLED));
		}else if(status.equals(0)){
			return dao.query(EduStudent.class, Cnd.where("id", "in", ids).and("status", "=", Status.DISABLED));
		}
		return null;
	}

	@Override
	public List<Org> selectCorpsAll() throws Exception {
		List<Org> corps = dao.query(Org.class, Cnd.where("type", "=", OrgType.TRAINING).and("status", "=", Status.ENABLED));
		return corps;
	}

	@Override
	public List<Org> selectCorpsAll(EduStudent student) throws Exception {
		List<EduStudentCorp> eduStudentCorps = dao.query(EduStudentCorp.class, Cnd.where("eduStudentId", "=", student.getId()));
		if(eduStudentCorps == null || eduStudentCorps.size() == 0 ){
			return null;
		}
		String[] ids = Converts.array(EduStudentCorp.class, String.class, eduStudentCorps, "corpId");
		return dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("orgId", "in", ids));
	}

	@Override
	public List<EduStudent> selectAll(ShopClient client, Org org)
			throws Exception {
		Asserts.isNull(client.getId(), "没有指定客户的id");
		Asserts.isNull(org.getOrgId(), "没有指定公司的id");
		
		List<EduStudentClient> eduStudentClients = dao.query(EduStudentClient.class, Cnd.where("shopClientId", "=", client.getId()));
		if(eduStudentClients == null || eduStudentClients.size() == 0 ){
			return null;
		}
		Integer[] conectIds = Converts.array(EduStudentClient.class, Integer.class, eduStudentClients, "eduStudentId");
		
		List<EduStudentCorp> eduStudentCorps = dao.query(EduStudentCorp.class, Cnd.where("corpId", "=", org.getOrgId()));
		if(eduStudentCorps == null || eduStudentCorps.size() == 0 ){
			return null;
		}
		Integer[] ids = Converts.array(EduStudentCorp.class, Integer.class, eduStudentCorps, "eduStudentId");
		
		//ids和conectIds的交集
		List<Integer> clientAndCorpIds = new ArrayList<Integer>();
		
		for(int x = 0; x < ids.length; x++){
			for(Integer conectId : conectIds){
				if(ids[x].equals(conectId)){
					clientAndCorpIds.add(conectId);
					break;
				}
			}
		}
		if(clientAndCorpIds.size() == 0){
			return null;
		}
		return dao.query(EduStudent.class, Cnd.where("id", "in", clientAndCorpIds).and("status", "=", Status.ENABLED));
	}
	
	private <T> List<T> copy (List<T> arr){
		if(arr == null){
			return null;
		}
		List<T> list = new ArrayList<T>();
		for(T e : arr){
			list.add(e);
		}
		return list;
	}

	@Override
	public boolean deleteRelantionWithCourses(EduStudent student,
			List<ShopGoods> courses){
		Asserts.isEmpty(courses, "请指定要解除关系的课程");
		Asserts.isNull(student, "请指定要解除关系的学生");
		List<Integer> courseIds = new ArrayList<Integer>();
		for(ShopGoods course : courses){
			courseIds.add(course.getId());
		}
		dao.clear(EduStudentCourse.class, Cnd.where("eduStudentId", "=", student.getId()).and("eduCourseId", "in", courseIds));
		return true;
	}


	@Override
	public MapBean getEduStudent(String org_id, String xwCode)
			throws Exception {
		MapBean mb = new MapBean();
		String openid = null;
		if(xwCode != null || org_id != null || !xwCode.equals("") || !org_id.equals("")){
			Config c = wxClientService.initOrgConfig(org_id);
			openid = wxClientService.getOpenid(xwCode, c);
			//openid为reset，说明调用微信api后无法获取到openid
			if(openid.equals("reset")){
				mb.put("type",1);
				return mb;
			}
		}else{
			mb.put("type",-1);
			return mb;
		}
		mb.put("openid",openid);
		
		EduStudentWechatrelation eduStudentWechatrelation = getByOrgIdAndOpenId(org_id, openid);
		if(eduStudentWechatrelation == null){
			mb.put("type",2);
			return mb;
		}
		
		Integer studentId = eduStudentWechatrelation.getStudentId();
		EduStudent eduStudent = new EduStudent();
		eduStudent.setId(studentId);
		eduStudent = selectOne(eduStudent);
		mb.put("type",3);
		mb.put("eduStudent",eduStudent);
		mb.put("eduStudentWechatrelation",eduStudentWechatrelation);
		return mb;
	}

	@Override
	public EduStudentWechatrelation getByOrgIdAndOpenId(String org_id,
			String openId) {
		Criteria cri = Cnd.cri();
		cri.where().and("org_id","=",org_id).and("openID","=",openId);
		EduStudentWechatrelation stuWechatRelation = dao.fetch(EduStudentWechatrelation.class, cri);
		return stuWechatRelation;
	}

	@Override
	public EduStudentWechatrelation getWechatConfig(String org_id,Integer studentId) 
	{
		Criteria cri = Cnd.cri();
		cri.where().and("org_id","=",org_id).and("student_id","=",studentId);
		EduStudentWechatrelation stuWechatRelation = dao.fetch(EduStudentWechatrelation.class, cri);
		return stuWechatRelation;
	}

	@Override
	public boolean insertOrUpdate(EduStudentWechatrelation studentWechatrelation) {
		if(studentWechatrelation.getId() == null){
			dao.insert(studentWechatrelation);
		}else{
			dao.update(studentWechatrelation);
		}
		return true;
	}

	@Override
	public List<EduStudentWechatrelation> selectWechatrelation(
			EduStudent student) {
		Asserts.isNull(student.getId(), "请传入学生的id");
		List<EduStudentWechatrelation> eduStudentWechatrelations = dao.query(EduStudentWechatrelation.class, Cnd.where("studentId", "=", student.getId()));
		if(eduStudentWechatrelations == null || eduStudentWechatrelations.size() == 0){
			return null;
		}
		return eduStudentWechatrelations;
	}

	@Override
	public boolean sign(EduStudent student) throws Exception {
		Asserts.isNull(student.getId(), "请传入学生的id");
		EduStudentCheck check = new EduStudentCheck();
		check.setCheckTime(new DateTime().toDate());
		check.setStudentId(student.getId());
		dao.insert(check);
		return true;
	}




	@Override
	public void insertSign() throws Exception 
	{
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd hh");  
		Date formatDate = new Date();
		String nowDate = dateFormater.format(formatDate).toString();
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(formatDate);
		calendar.add(Calendar.HOUR_OF_DAY,1);//把日期往后增加一天.整数往后推,负数往前移动		
		formatDate = calendar.getTime(); 
		
		String nowDatePlus = dateFormater.format(formatDate).toString();
		
		StringBuffer str = new StringBuffer("select c.edu_student_id as studentId,s.name as studentName,c.edu_course_id as courseId,g.name as courseName,t.id as teacherId,t.truename as teacherName,ts.id as scheduleId,min(sc.check_time) as checkTime,ts.start as startTime,sc.id as checkId "
				+ "from shop_goods g,edu_student_course c ,edu_student s,edu_teacher t,edu_teaching_schedule ts,edu_student_check sc "
				+ "where c.edu_student_id in (select student_id from edu_student_check) "
				+ "and g.id = c.edu_course_id "
				+ "and g.edu_teacher_id = t.id "
				+ "and s.id = c.edu_student_id "
				+ "and s.id = sc.student_id "
				+ "and sc.id not in (select check_id from edu_student_sign where student_id = sc.student_id) "  //筛掉已经重复的签到记录
				+ "and ts.edu_course_id = (case when g.dependId is not null then g.dependId ELSE g.id end) "
				+ "and (sc.check_time >= '" + nowDate + ":00:00.000' and sc.check_time < '" + nowDatePlus + ":00:00.000') "
				+ "and ts.end >= sc.check_time "
				+ "and ((ts.start >= '" + nowDate + ":00:00.000' and ts.start < '" + nowDatePlus + ":00:00.000') or (ts.end >= '" + nowDate + ":00:00.000' and ts.end < '" + nowDatePlus + ":00:00.000')) "
				+ "group by studentId,scheduleId,check_time "
				+ "order by checkTime,startTime ");
	
		Sql sql = Sqls.create(str.toString());
		//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
		
		sql.setCallback(Sqls.callback.entities());
		Entity<EduStudentSignTemp> entity = dao.getEntity(EduStudentSignTemp.class);
	    sql.setEntity(entity);
		
		dao.execute(sql);
		List<EduStudentSignTemp> nodes = sql.getList(EduStudentSignTemp.class);
        
		//上方获取有签到的学生，下方获取无签到的学生
        System.out.println(str);
		
		StringBuffer strNone = new StringBuffer("Select c.edu_student_id as studentId,es.name as studentName,g.id as courseId,g.name as courseName,ts.id as scheduleId from edu_student_course c,edu_teaching_schedule ts,edu_student es,shop_goods g "
				+ "where c.edu_course_id = ts.edu_course_id "
				+ "and ts.edu_course_id = g.id "
				+ "and es.id = c.edu_student_id "
				+ "and ((ts.start >= '" + nowDate + ":00:00.000' and ts.start < '" + nowDatePlus + ":00:00.000') or (ts.end >= '" + nowDate + ":00:00.000' and ts.end < '" + nowDatePlus + ":00:00.000')) "
				+ "and (ts.id,c.edu_student_id) not in(select schedule_id,student_id from edu_student_sign) ");
		
		System.out.println(strNone);
		
		Sql sqlNone = Sqls.create(strNone.toString());
		//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
		
		sqlNone.setCallback(Sqls.callback.entities());
		Entity<EduStudentSignTemp> entityNone = dao.getEntity(EduStudentSignTemp.class);
		sqlNone.setEntity(entityNone);
		
		dao.execute(sqlNone);
		List<EduStudentSignTemp> nodesNone = sqlNone.getList(EduStudentSignTemp.class);
		
		nodes.addAll(nodesNone);
		
		if(nodes != null && nodes.size() != 0)
		{
			for(EduStudentSignTemp sign : nodes)
			{
				String checkDate = null;
				SimpleDateFormat dateFormater2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try
				{
					checkDate = dateFormater2.format(sign.getCheckTime()).toString();
				}
				catch(Exception e)
				{
					checkDate = "1000-01-01 01:01:01";//给于一个不存在的时间
				}

				EduStudentSign flag = dao.fetch(EduStudentSign.class,Cnd.wrap(" student_id = "+ sign.getStudentId() + " and (startTime = '" + checkDate + "' or schedule_id = " + sign.getScheduleId() + ")"));
				System.out.println(Cnd.wrap(" student_id = "+ sign.getStudentId() + " and (startTime = '" + checkDate + "' or schedule_id = " + sign.getScheduleId() + ")"));
				if(flag == null)
				{
					flag = new EduStudentSign();
					flag.setCourseId(sign.getCourseId());
					flag.setCourseName(sign.getCourseName());
					flag.setScheduleId(sign.getScheduleId());
					try
					{
						flag.setStartTime(sign.getCheckTime());
						flag.setCheckId(sign.getCheckId());
					}
					catch(Exception e)
					{
						flag.setStartTime(null);
						flag.setCheckId(null);
					}
					flag.setStudentId(sign.getStudentId());
					flag.setStudentName(sign.getStudentName());
					flag.setStudentIsRate(0);
					flag.setTeacherIsRate(0);
					if(sign.getStartTime() == null)
					{
						flag.setIsCome(0);
					}
					else if(sign.getStartTime().getTime() > sign.getCheckTime().getTime())
					{
						flag.setIsCome(1);
					}
					else if(sign.getStartTime().getTime() < sign.getCheckTime().getTime())
					{
						flag.setIsCome(2);
					}
					flag.setEndTime(null);
					dao.insert(flag);
				}
			}
		}
		
	}

}
