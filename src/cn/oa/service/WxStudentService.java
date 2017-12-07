package cn.oa.service;

import java.util.ArrayList;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.Filters;

import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentDisplay;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopProduct;
import cn.oa.model.StudentCourseCount;
import cn.oa.model.vo.WxShopProductVO;
import cn.oa.repository.Mapper;
import cn.oa.service.client.ClientService;
import cn.oa.utils.DateUtil;
import cn.oa.utils.web.Page;

@Filters
@IocBean(name="wxStudentService")
public class WxStudentService {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;

	public List<EduStudentDisplay> getEduStudentDisplay(Page<EduStudent> page){
		if(page == null){
			return null;
		}
		List<EduStudentDisplay> list = new ArrayList<EduStudentDisplay>();
		for (EduStudent sp : page.getResult()) {
			list.add(getEduStudentDisplay(sp));
		}
		return list;
	}
	
	public EduStudentDisplay getEduStudentDisplay(EduStudent sp){
		if(sp == null){
			return null;
		}
		
		EduStudentDisplay vo = new EduStudentDisplay();
		
		vo.setAddress(sp.getAddress());
        vo.setBirthday(sp.getBirthday());
        vo.setHobby(sp.getHobby());
        vo.setId(sp.getId());
		vo.setId(sp.getId());
		vo.setName(sp.getName());
		vo.setNumber(sp.getNumber());
		vo.setQq(sp.getQq());
		vo.setSex(sp.getSex());
		vo.setStatus(sp.getStatus());
		vo.setTelephone(sp.getTelephone());
		vo.setWeixin(sp.getWeixin());
		
		StringBuffer str = new StringBuffer("select g.status as status,count(*) as count "
				+ "from shop_goods g,edu_student_course c "
				+ "where g.id = c.edu_course_id "
				+ "and c.edu_student_id = " + vo.getId() + " "
				+ "group by g.status ");
		
		List<StudentCourseCount> nodes = new ArrayList<StudentCourseCount>();
        
		Sql sql = Sqls.create(str.toString());
		//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
		
		sql.setCallback(Sqls.callback.entities());
		Entity<StudentCourseCount> entity = dao.getEntity(StudentCourseCount.class);
	    sql.setEntity(entity);
		
		dao.execute(sql);
		nodes = sql.getList(StudentCourseCount.class);

		if(nodes == null || nodes.size() == 0)
		{
			vo.setCountAll(0);
			vo.setCountEnd(0);
			vo.setCountStart(0);
		}
		else
		{
			for(int i = 0;i<nodes.size();i++)
			{
				int count = 0;
				int status = 0;
				count = nodes.get(i).getCount();
				status = nodes.get(i).getStatus();
				if(status == 1)
				{
					vo.setCountStart(count);
				}
				else if(status == 2)
				{
					vo.setCountEnd(count);
				}
			}
            vo.setCountAll(vo.getCountStart() + vo.getCountEnd());
		}
		return vo;
	}
	
}
