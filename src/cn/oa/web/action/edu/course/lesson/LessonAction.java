package cn.oa.web.action.edu.course.lesson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Trans;

import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.CoopOrgRelation;
import cn.oa.model.EduClassify;
import cn.oa.model.EduLocation;
import cn.oa.model.EduTeacher;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.Org;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopProduct;
import cn.oa.service.schedule.TeachingSchedule;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

/**
 * 课程
 * @author Ash
 */
@IocBean
@At("/edu/course/lesson/lesson")
public class LessonAction extends Action
{
	
	public static Log log = Logs.getLog(LessonAction.class);

	@GET
	@POST
	@At
	@Ok("ftl:edu/course/lesson/lesson_page")
	public void page(HttpServletRequest req) {
		
		Integer coopSearchId = Https.getInt(req, "coopSearchId");
		String couSearchName = Https.getStr(req, "couSearchName");
		String couSearchNameVal = Https.getStr(req, "couSearchName");
	
		
		Boolean flag = false; //是否有子类  true有
		Integer flag2 = 1; //是否平台管理员  1不是  0是

		String isParent = "parent_id";
		
		List<Org> flagList = new ArrayList<Org>();
		
//		flagList = dao.query(Org.class,Cnd.wrap("parent_id = " + Context.getOrgId() + " and (type = 3 or type = 4)"));
		flagList = dao.query(Org.class,Cnd.wrap("parent_id = " + Context.getCorpId() + " and type = 3"));

		if(flagList == null || flagList.size() == 0)
		{
			//无子类
			System.out.println("Loading in......");
			isParent = "org_id";
			flag = true;
		}
		else
		{
			flagList = dao.query(Org.class,Cnd.where("org_id","=",Context.getCorpId()));
			//取得parentId,判断是否平台管理员
			flag2 = flagList.get(0).getParentId();
		}
		
		List<Org> nodes = new ArrayList<Org>();
		List<Org> allNodes = new ArrayList<Org>();
	    
		if(flag) //如果无子类,最上级为科目
		{
			StringBuffer str = new StringBuffer(
	        		"select distinct s.* "
	        		+ "from edu_classify as s,edu_classify as c,shop_goods as l "
	        		+ "where s.id = c.parent_id "
	        		+ "and c.id = l.edu_classify_id "
	        		+ "and l.corp_id = " + Context.getCorpId() + " "
	        		+ "and s.status != 99 and s.status != -1 "
	        		+ "and c.status != 99 and c.status != -1 "
	        		+ "and l.status != 99 and l.status != -1 and l.isCopy is null ");
	        
	        if(coopSearchId != null && !coopSearchId.equals(""))
	        {
	        	str.append("and c.corp_id = " + coopSearchId + " ");
	        }
	        
	        //搜索，不仅要搜索当前级别，还要搜索他的下级。
	        if(couSearchName != null && !couSearchName.equals(""))
	        {
	        	str.append("and ");
	        	str.append("( ");
	        	str.append("s.name like '%" + couSearchName + "%' ");
	        	str.append("or c.name like '%" + couSearchName + "%' ");
	        	str.append("or l.name like '%" + couSearchName + "%' ");
	        	str.append(") ");
	        }

			List<ShopGoods> nodesC = new ArrayList<ShopGoods>();
	        
			Sql sqlC = Sqls.create(str.toString());
			//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
			
			sqlC.setCallback(Sqls.callback.entities());
			Entity<ShopGoods> entityC = dao.getEntity(ShopGoods.class);
		    sqlC.setEntity(entityC);
			
			dao.execute(sqlC);
			nodesC = sqlC.getList(ShopGoods.class);

			req.setAttribute("nodes", nodesC);
			
			System.out.println("\n\n\n\n\n" + str + "\n\n\n\n\n");
			
		}
		else //如果有子类,最上级为机构
		{

	        StringBuffer str = new StringBuffer("select distinct o.*"
	        		+ " from sec_org as o,shop_goods as c,"
	        		+ " edu_classify as e,edu_classify as s"
	        		+ " where (o." + isParent + " = " + Context.getCorpId() + ""
	        		+ " or o.org_id = " + Context.getCorpId() + ")"
	        		+ " and c.corp_id = o.org_id"
	        		+ " and s.id = e.parent_id"
	        		+ " and e.id = c.edu_classify_id"
	        		+ " and c.status != 99 and c.status != -1 and c.isCopy is null"
	        		+ " and s.status != 99 and s.status != -1"
	        		+ " and e.status != 99 and e.status != -1");

			if(coopSearchId != null && !coopSearchId.equals(""))
			{
				str.append(" and o.org_id = " + coopSearchId);
			}
			
			//搜索，不仅要搜索当前级别，还要搜索他的下级。
			if(couSearchName != null && !couSearchName.equals(""))
			{
				str.append(" and");
				str.append(" (");
//				str.append(" o.org_name like '%" + couSearchName + "%'");
				str.append(" s.name like '%" + couSearchName + "%'");
				str.append(" or e.name like '%" + couSearchName + "%'");
				str.append(" or c.name like '%" + couSearchName + "%'");
				str.append(" )");
			}
			
//			if(isSearch)
//			{
//				str.append(" union");
//				str.append(" select distinct ot.* from sec_org as ot,edu_course as ct where ot." + isParent + " = " + Context.getOrgId() + " and ct.corp_id = ot.org_id");
//				if(coopSearchId != null && !coopSearchId.equals(""))
//				{
//					str.append(" and ot.org_id = " + coopSearchId);
//				}
//				if(couSearchName != null && !couSearchName.equals(""))
//				{
//					str.append(" and ct.name like '%" + couSearchName + "%'");
//				}
//			}
			
			Sql sql = Sqls.create(str.toString());
			//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
			
			sql.setCallback(Sqls.callback.entities());
			Entity<Org> entity = dao.getEntity(Org.class);
		    sql.setEntity(entity);
				
			dao.execute(sql);
			nodes = sql.getList(Org.class);
			
			req.setAttribute("nodes", nodes);
			
			System.out.println("\n\n\n\n\n" + str + "\n\n\n\n\n");

		}

		
		if(flag2 == 0)
		{
			allNodes = dao.query(Org.class,Cnd.where("parent_id", "=", Context.getCorpId()).asc("org_id"));
		}
		else
		{
			allNodes = dao.query(Org.class,Cnd.where("parent_id", "=", Context.getCorpId()).or("org_id","=",Context.getCorpId()).asc("org_id"));
		}
						
		
		req.setAttribute("flag", flag);
		req.setAttribute("flag2", flag2);
		req.setAttribute("flag3", resourceRepository.getBtnAuthority("/edu/course/lesson/lesson/add", "课程编辑权力")); //当前用户是否有权限进行新增/修改/禁用操作
		req.setAttribute("isParent", isParent);
		req.setAttribute("orgId", Context.getCorpId());
		req.setAttribute("corpId", coopSearchId);
		req.setAttribute("couSearchName",couSearchName);
		req.setAttribute("couSearchNameVal",couSearchNameVal);
		req.setAttribute("userCorpId", Context.getCorpId());
		req.setAttribute("allNodes", allNodes);
	}

	@POST
	@At
	@Ok("json")
	public Object deleteLesson(HttpServletRequest req, HttpServletResponse res) {
				
		Integer id = Https.getInt(req, "id");
		
		MapBean mb = new MapBean();
		
		ShopGoods co = dao.fetch(ShopGoods.class,id);		
		
		try
		{			
			if(co.getStatus() != 2)
			{
				if(co.getSold() == 0 || co.getSold().equals(0))
				{
					Trans.begin();
					Asserts.isNull(id, "请指定课程的id");
					List<EduTeachingSchedule> tss = dao.query(EduTeachingSchedule.class, Cnd.where("eduCourseId", "=", id).and("status", "=", Status.ENABLED));
					for(EduTeachingSchedule ts : tss){
						dao.delete(ts);
					}
					co.setStatus(-1);
					dao.update(co);
					Trans.commit();
				}
				else
				{
					Code.error(mb, "课程已被购买,无法删除！");
			        return mb;
				}
			}
			else
			{
				Trans.begin();
				Asserts.isNull(id, "请指定课程的id");
				List<EduTeachingSchedule> tss = dao.query(EduTeachingSchedule.class, Cnd.where("eduCourseId", "=", id).and("status", "=", Status.ENABLED));
				for(EduTeachingSchedule ts : tss){
					dao.delete(ts);
				}
				co.setStatus(-1);
				dao.update(co);
				Trans.commit();
			}

		    Code.ok(mb, "删除课程成功");
	    } catch (Errors e) {
		    Code.error(mb, e.getMessage());
	    } catch (Exception e) {
		    log.error("(Cooperation:add) error: ", e);
		    Code.error(mb, "删除课程失败");
	}
        return mb;
	}

	
	//如果是平台级用户，则课程的最上级为机构，则调用该方法
	@POST
	@At
	@Ok("json")
	public Object nodesOrg(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer orgId = Https.getInt(req, "orgid", R.REQUIRED, R.I, "机构id");
			String couSearchName = req.getParameter("couSearchName");
	        StringBuffer str = new StringBuffer(
	        		"select distinct s.* "
	        		+ "from edu_classify as s,edu_classify as c,shop_goods as l "
	        		+ "where s.id = c.parent_id "
	        		+ "and c.id = l.edu_classify_id "
	        		+ "and l.corp_id = " + orgId + " "
	        		+ "and s.status != 99 and s.status != -1 "
	        		+ "and c.status != 99 and c.status != -1 "
	        		+ "and l.status != 99 and l.status != -1 and l.isCopy is null ");
	        
	        
	        if(couSearchName != null && !couSearchName.equals(""))
	        {
	        	str.append("and ");
	        	str.append("( ");
	        	str.append("s.name like '%" + couSearchName + "%' ");
	        	str.append("or c.name like '%" + couSearchName + "%' ");
	        	str.append("or l.name like '%" + couSearchName + "%' ");
	        	str.append(") ");
	        }
	        
//	        if(isSearch)
//			{
//				str.append("union ");
//				str.append("select distinct s.* "
//	        		+ "from edu_course as s,edu_course as c,edu_course l "
//	        		+ "where s.id = c.parent_id "
//	        		+ "and c.id = l.parent_id "
//	        		+ "and s.coutype = '科目' "
//	        		+ "and c.coutype = '学科' "
//	        		+ "and l.coutype = '课程' "
//	        		+ "and l.corp_id = " + orgId + " ");
//				if(coopSearchId != null && !coopSearchId.equals(""))
//				{
//					str.append("and l.corp_id = " + coopSearchId + " ");
//				}
//				else
//				{
//					str.append("and l.corp_id = " + orgId + " ");
//				}
//				if(couSearchName != null && !couSearchName.equals(""))
//				{
//					str.append("and c.name like '%" + couSearchName + "%' ");
//				}
//				
//				str.append("union ");
//				str.append("select distinct s.* "
//	        		+ "from edu_course as s,edu_course as c,edu_course l "
//	        		+ "where s.id = c.parent_id "
//	        		+ "and c.id = l.parent_id "
//	        		+ "and s.coutype = '科目' "
//	        		+ "and c.coutype = '学科' "
//	        		+ "and l.coutype = '课程' ");
//				if(coopSearchId != null && !coopSearchId.equals(""))
//				{
//					str.append("and l.corp_id = " + coopSearchId + " ");
//				}
//				else
//				{
//					str.append("and l.corp_id = " + orgId + " ");
//				}
//				if(couSearchName != null && !couSearchName.equals(""))
//				{
//					str.append("and l.name like '%" + couSearchName + "%' ");
//				}
//				
//			}
	        
			List<ShopGoods> nodes = new ArrayList<ShopGoods>();
	        
			Sql sql = Sqls.create(str.toString());
			//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
			
			sql.setCallback(Sqls.callback.entities());
			Entity<ShopGoods> entity = dao.getEntity(ShopGoods.class);
		    sql.setEntity(entity);
			
			dao.execute(sql);
			nodes = sql.getList(ShopGoods.class);
			
//
//			nodes = dao.query(EduCourse.class, 
//						Cnd.where("parentId", "=", Value.I)
//						   .asc("id"));
			mb.put("nodes", nodes);
			mb.put("orgId",orgId);
			mb.put("orgFlag",Context.getCorpId());
			mb.put("flag3", resourceRepository.getBtnAuthority("/edu/course/lesson/lesson/add", "课程编辑权力"));

			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(EduCourse:nodes) error: ", e);
		}

		return mb;
	}

	//如果不是平台级用户，则课程的最上级为学科（如果有的话），则调用该方法
	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer id = Https.getInt(req, "id", R.REQUIRED, R.I, "id");			
			Integer orgId = Https.getInt(req, "orgId");
			Integer coopSearchId = Https.getInt(req, "coopSearchId");
			String which = "";
			Boolean flag3 = resourceRepository.getBtnAuthority("/edu/course/lesson/lesson/add", "课程编辑权力");
			String type = req.getParameter("type");
			String couSearchName = req.getParameter("couSearchName");

			
            if(type == null)
            {
            	return null;
            }
            
            StringBuffer str = null;
            
            if(type.equals("科目"))
            {
            	which = "s";
            	str = new StringBuffer("Select distinct ");
            	str.append(which + ".* ");
                if(which.equals("s")){
                	which += ".parent_id";
                }else{
                	which += ".edu_classify_id";
                }
                str.append("From edu_classify as s ");
                str.append("where " + which + " = " + id + " ");
                str.append("and s.status != 99 and s.status != -1 ");
            }
            else
            {
            	which = "c";
                str = new StringBuffer("Select distinct ");
                str.append(which + ".* ");
                if(which.equals("s"))
                {
                	which += ".parent_id";
                }
                else
                {
                	which += ".edu_classify_id";
                }
                str.append("From edu_classify e,edu_classify as s,shop_goods as c ");
                str.append("where " + which + " = " + id + " ");
                str.append("and e.id = s.parent_id ");
                str.append("and s.id = c.edu_classify_id ");
                str.append("and e.status != 99 and e.status != -1 ");
                str.append("and s.status != 99 and s.status != -1 ");
                str.append("and c.status != 99 and c.status != -1 and c.isCopy is null ");
                	
                if(coopSearchId != null && !coopSearchId.equals("")){
                    str.append("and c.corp_id = " + coopSearchId + " ");
                }else{
    				str.append("and c.corp_id = " + orgId + " ");
    			}
                
                if(couSearchName != null && !couSearchName.equals("")){
                    str.append("and ");
                    str.append("( ");
                    str.append("e.name like '%" + couSearchName + "%' ");
                    str.append("or s.name like '%" + couSearchName + "%' ");
                    str.append("or c.name like '%" + couSearchName + "%' ");
                    str.append(") ");
                }
            }
            
			Integer flag2 = dao.fetch(Org.class,Cnd.where("org_id","=",Context.getCorpId())).getParentId();
            
            
			List<ShopGoods> nodes = new ArrayList<ShopGoods>();

			Sql sql = Sqls.create(str.toString());
			//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
			
			sql.setCallback(Sqls.callback.entities());
			Entity<ShopGoods> entity = dao.getEntity(ShopGoods.class);
		    sql.setEntity(entity);
			
			dao.execute(sql);
			nodes = sql.getList(ShopGoods.class);
						
//			nodes = dao.query(EduCourse.class, 
//					Cnd.where("parentId","=", id)
//					   .and("corp_id","=",corpId)
//					   .asc("id"));
			mb.put("orgId", orgId);
			mb.put("nodes", nodes);
			mb.put("orgFlag",Context.getCorpId());
			mb.put("flag2", flag2);
			mb.put("flag3", flag3);
            System.out.println("\n\n\n\n\n\n\n" + flag3);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(EduCourse:nodes) error: ", e);
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer id = Https.getInt(req, "id", R.REQUIRED, R.I, "ID");
			System.out.println("\n\n\n\n\n" + id + "\n\n\n\n\n");
			List<ShopGoods> actors = dao.query(ShopGoods.class, Cnd.where("id", "=", id));
			mb.put("actors", actors);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:actors) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:edu/course/lesson/lesson_add")
	public void add(HttpServletRequest req) {
		Integer flag2 = 1;
		Integer id = Https.getInt(req, "id", R.REQUIRED, R.I);
		EduClassify parent = null;
		ShopGoods course = null;
		String teacherName = null;
		String couWeekday = null;
		String startTime = null;
		String endTime = null;
		String startTime1 = null;
		String endTime1 = null;
		String startTime2 = null;
		String endTime2 = null;
		String startTime3 = null;
		String endTime3 = null;
		String startTime4 = null;
		String endTime4 = null;
		String startTime5 = null;
		String endTime5 = null;
		String startTime6 = null;
		String endTime6 = null;
		String startTime7 = null;
		String endTime7 = null;
		String dateStr = null;
		Boolean flag = false;
		
        List<Org> flagList = new ArrayList<Org>();
                        
		flagList = dao.query(Org.class,Cnd.where("status","=",1).and("parent_id","=",Context.getCorpId()));
		
		if(flagList == null || flagList.size() == 0)
		{
			flag = true;
		}
		else
		{
			try
			{
				flagList = dao.query(Org.class,Cnd.where("org_id","=",Context.getCorpId()));
				flag2 = flagList.get(0).getParentId();
			}
			catch(Exception e)
			{
				System.out.println("该用户并非培训中心用户!");
			}
		}
		
				
		if (id != null) {
			course = dao.fetch(ShopGoods.class, id);
			if(!course.getCorpId().equals(Context.getCorpId()))
			{
				flag = false;
			}
			else
			{
				flag = true;
			}			
			
			teacherName = dao.fetch(EduTeacher.class, course.getEduTeacherId()).getTruename();
			String[] temp = course.getCouTime().split("\\|");
			couWeekday = temp[0]; //获得上课时间类型  1.法定工作日  2.每天  3.自己指定
			dateStr = new SimpleDateFormat("yyyy-MM-dd").format(course.getStartDate());
			if(couWeekday.equals("3"))
			{
				int i = temp.length;
				System.out.println(i);
				if(1 < i && !temp[1].equals(""))
				{
					startTime1 = temp[1].split("-")[0];
					endTime1 = temp[1].split("-")[1];
					req.setAttribute("startTime1", startTime1);
					req.setAttribute("endTime1", endTime1);
				}
				if(2 < i && !temp[2].equals(""))
				{
					startTime2 = temp[2].split("-")[0];
					endTime2 = temp[2].split("-")[1];
					req.setAttribute("startTime2", startTime2);
					req.setAttribute("endTime2", endTime2);
				}
				if(3 < i && !temp[3].equals(""))
				{
					startTime3 = temp[3].split("-")[0];
					endTime3 = temp[3].split("-")[1];
					req.setAttribute("startTime3", startTime3);
					req.setAttribute("endTime3", endTime3);
				}
				if(4 < i && !temp[4].equals(""))
				{
					startTime4 = temp[4].split("-")[0];
					endTime4 = temp[4].split("-")[1];
					req.setAttribute("startTime4", startTime4);
					req.setAttribute("endTime4", endTime4);
				}
				if(5 < i && !temp[5].equals(""))
				{
					startTime5 = temp[5].split("-")[0];
					endTime5 = temp[5].split("-")[1];
					req.setAttribute("startTime5", startTime5);
					req.setAttribute("endTime5", endTime5);
				}
				if(6 < i && !temp[6].equals(""))
				{
					startTime6 = temp[6].split("-")[0];
					endTime6 = temp[6].split("-")[1];
					req.setAttribute("startTime6", startTime6);
					req.setAttribute("endTime6", endTime6);
				}
				if(7 < i && temp.length > 7 && !temp[7].equals(""))
				{
					startTime7 = temp[7].split("-")[0];
					endTime7 = temp[7].split("-")[1];
					req.setAttribute("startTime7", startTime7);
					req.setAttribute("endTime7", endTime7);
				}
			}
			else
			{
				startTime = temp[1].split("-")[0];
				endTime = temp[1].split("-")[1];
				req.setAttribute("startTime", startTime);
				req.setAttribute("endTime", endTime);
			}

			if (course != null) {
				parent = dao.fetch(EduClassify.class, course.getEduClassifyId());
			}
		}

		if (parent == null)
			parent = new EduClassify();
		if (course == null)
		{
			course = new ShopGoods();
			flag = true;
		}
			
		StringBuffer str = new StringBuffer("select o.org_id as orgId,o.org_name as orgName,c.coopId as coopId,c.gainsharingtype,c.gainsharingnum "
				+ "from sec_org o, sec_cooperation_org c "
				+ "where o.org_id = c.coopIdOther "
				+ "and c.coopId = " + Context.getCorpId() + " and gainsharingtype != 0 ");

		str.append("order by o.org_id asc ");
						
		Sql sql = Sqls.create(str.toString());
		//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
		
		sql.setCallback(Sqls.callback.entities());
		Entity<CoopOrgRelation> entity = dao.getEntity(CoopOrgRelation.class);
	    sql.setEntity(entity);
		
		dao.execute(sql);
		List<CoopOrgRelation> coopOrg = sql.getList(CoopOrgRelation.class);

		if(coopOrg == null || coopOrg.size() == 0)
		{
			CoopOrgRelation no = new CoopOrgRelation();
			no.setOrgName("没有数据");
			coopOrg.add(no);
		}
		
		//查询隶属上级
		List<EduClassify> nodes = dao.query(EduClassify.class, Cnd.where("status", "=", Status.ENABLED));
		//查询上课地点
		List<EduLocation> location = dao.query(EduLocation.class, Cnd.where("status", "=", Status.ENABLED).and("org_id","=",Context.getCorpId()));
		
		req.setAttribute("orgId", Context.getCorpId());
		req.setAttribute("location", location);
		req.setAttribute("parent", parent);
		req.setAttribute("course", course);
		req.setAttribute("coopOrg", coopOrg);
		req.setAttribute("nodes", nodes);
		req.setAttribute("dateStr", dateStr);
		req.setAttribute("teacherName", teacherName);
		req.setAttribute("couWeekday", couWeekday);
		req.setAttribute("flag", flag);
		req.setAttribute("flag2", flag2);
		req.setAttribute("flag3", resourceRepository.getBtnAuthority("/edu/course/lesson/lesson/add", "课程编辑权力"));
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) throws Exception {
		MapBean mb = new MapBean();
		Integer id = null;
		ShopGoods course = null;
		try {
			id = Https.getInt(req, "id", R.I);
			Integer teacherId = 0;
			Integer coopId = null;
			Integer status = Https.getInt(req, "status");
			Integer parentId = Values.getInt(Https.getInt(req, "parentId", R.I), Value.I);
			Integer couTime = Https.getInt(req, "couTime");
			Integer dependId = Https.getInt(req, "dependId");
			Integer gainSharingType = Https.getInt(req, "gainSharingType");
			Integer couCount = Https.getInt(req, "couCount");
			Float gainSharingNum = Https.getFloat(req, "gainSharingNum");
			Float couPrice = Https.getFloat(req, "couPrice");
			String name = Https.getStr(req, "couName");
			String dependName = Https.getStr(req, "dependName");
			Integer max = Https.getInt(req, "max");
			String desc = Https.getStr(req, "desc");
			String teacherName = Https.getStr(req, "teacherName");
			String location = Https.getStr(req, "location");
			String coopType = Https.getStr(req, "coopType");
  			String startDate = Https.getStr(req, "startDate");
  			ShopGoods course2 = new ShopGoods();
  			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  	        Date date = null;
  	        Boolean isbreak = false;
  	        String finalTime = null;
  	        date = dateFormat.parse(startDate);
//			Asserts.notUnique(resourceName, orgResourceName,
//					dao.count(Resource.class, Cnd.where("resourceName", "=", resourceName).and("type", "=", type)),
//					"资源名称已存在");
			
  	        System.out.println("\n\n\n\n\n" + status + "\n\n\n\n\n");
  	        
            if(parentId == null || parentId == 0)
            {
				Asserts.isNull(course, "请选择上级!");
            }
  	          	        
			if (id != null) {
				course = dao.fetch(ShopGoods.class, id);
				course2 = course;
				Asserts.isNull(course, "科目/学科不存在");	
				
				StringBuffer str = new StringBuffer("select distinct p.* "
						+ "from shop_product p,shop_product c "
						+ "where p.id = c.parent_id "
						+ "and p.status = 1 "
						+ "and p.onSale = 1 "
						+ "and c.shop_goods_id = " + id + " "
						+ "and p.corp_id = " + Context.getCorpId() + " ");
				
				Sql sql = Sqls.create(str.toString());
				
				sql.setCallback(Sqls.callback.entities());
				Entity<ShopProduct> entity = dao.getEntity(ShopProduct.class);
			    sql.setEntity(entity);
				
				dao.execute(sql);

				List<ShopProduct> onSale = sql.getList(ShopProduct.class);
				
				if(onSale != null && onSale.size() != 0)
				{
					Code.error(mb, "该课程已经包含在" + onSale.get(0).getName() + "内上架！无法进行修改！");
					return mb;
				}
				if(course.getSold() != 0 && course.getSold() != null && !course.getSold().equals("0"))
			    {
					Code.error(mb, "该课程已经售出！无法进行修改！");
					return mb;
			    }
				
			} else {				
				course = new ShopGoods();
			}			
			
			course.setEduClassifyId(parentId);
			course.setName(name);
			course.setCoopType(coopType);
			course.setPrice(couPrice);
			course.setCorpId(Context.getCorpId());
			course.setCoudesc(desc);
			course.setCouCount(couCount);
			course.setCouSeason(null);
			course.setType("课程");
			course.setMax(max);
			course.setDependName(dependName);
			course.setDependId(dependId);
			
			try
			{
				if(!coopType.equals("合作"))
				{
					teacherId = dao.query(EduTeacher.class,Cnd.where("truename","=",teacherName).and("coopId","is",null)).get(0).getId();
				}
				else
				{
					coopId = Https.getInt(req, "coopId");
					Asserts.isNull(coopId, "机构不存在");	
					teacherId = dao.query(EduTeacher.class,Cnd.where("truename","=",teacherName).and("corp_id","=",coopId).and("coopType","=","自营")).get(0).getId();
					ShopGoods flag = dao.fetch(ShopGoods.class,Cnd.where("status","=",1).and("corp_id","=",coopId).and("name","=",dependName).and("coopType","=","自营"));
					if(flag == null)
					{
						Code.error(mb,"未找到合作课程!");
						return mb;
					}
				}
				if(teacherId == null || teacherId.equals(""))
				{
					Code.error(mb,"未找到该教师!");
					return mb;
				}
				course.setEduTeacherId(teacherId);
			}
			catch(Exception E)
			{
				Code.error(mb,"未找到该教师!");
				return mb;
			}
			
			course.setGainSharingType(gainSharingType);
			course.setGainSharingNum(gainSharingNum);
			course.setLocation(location);
			
			if(coopType.equals("合作"))
			{
				course.setCoopId(coopId);
			}
			if(couTime != 3)
			{
				String startTime = Https.getStr(req, "startTime");
				String endTime = Https.getStr(req, "endTime");
				if(startTime == null || endTime == null)
				{
					Code.error(mb,"开始/结束时间不能为空");
					return mb;
				}
				String time = couTime + "|" + startTime + "-" + endTime;
				course.setCouTime(time);
			}
			else
			{
				String startTime1 = Https.getStr(req, "startTime1");
				String endTime1 = Https.getStr(req, "endTime1");
				String startTime2 = Https.getStr(req, "startTime2");
				String endTime2 = Https.getStr(req, "endTime2");
				String startTime3 = Https.getStr(req, "startTime3");
				String endTime3 = Https.getStr(req, "endTime3");
				String startTime4 = Https.getStr(req, "startTime4");
				String endTime4 = Https.getStr(req, "endTime4");
				String startTime5 = Https.getStr(req, "startTime5");
				String endTime5 = Https.getStr(req, "endTime5");
				String startTime6 = Https.getStr(req, "startTime6");
				String endTime6 = Https.getStr(req, "endTime6");
				String startTime7 = Https.getStr(req, "startTime7");
				String endTime7 = Https.getStr(req, "endTime7");
				String time1 = "|" + startTime1 + "-" + endTime1;
				String time2 = "|" + startTime2 + "-" + endTime2;
				String time3 = "|" + startTime3 + "-" + endTime3;
				String time4 = "|" + startTime4 + "-" + endTime4;
				String time5 = "|" + startTime5 + "-" + endTime5;
				String time6 = "|" + startTime6 + "-" + endTime6;
				String time7 = "|" + startTime7 + "-" + endTime7;
				if(startTime1 == null || startTime1.equals("") || endTime1.equals("") || endTime1 == null)
				{
					time1 = "|";
				}
				if(startTime2 == null || startTime2.equals("") || endTime2.equals("") || endTime2 == null)
				{
					time2 = "|";
				}
				if(startTime3 == null || startTime3.equals("") || endTime3.equals("") || endTime3 == null)
				{
					time3 = "|";
				}
				if(startTime4 == null || startTime4.equals("") || endTime4.equals("") || endTime4 == null)
				{
					time4 = "|";
				}
				if(startTime5 == null || startTime5.equals("") || endTime5.equals("") || endTime5 == null)
				{
					time5 = "|";
				}
				if(startTime6 == null || startTime6.equals("") || endTime6.equals("") || endTime6 == null)
				{
					time6 = "|";
				}
				if(startTime7 == null || startTime7.equals("") || endTime7.equals("") || endTime7 == null)
				{
					time7 = "|";
				}

				finalTime = couTime + time1 + time2 + time3 + time4 + time5 + time6 + time7;
				course.setCouTime(finalTime);
    			if(coopType.equals("合作"))
    			{
    				if(course.getCouTime().equals(finalTime));
    				{
    					Code.error(mb,"已存在相同的合作课程！");
    					return mb;
    				}
    			}
			}		
			
			course.setStartDate(date);
			
			cache.removeAll(Cache.FQN_RESOURCES);
			
//			List<ShopGoods> middleList = dao.query(ShopGoods.class, Cnd.where("status", "=", 99).and("corp_id","=",274).and("edu_teacher_id","=",teacherId).and("parent_id","=",parentId));
//			ShopGoods middle = new ShopGoods();
						
			
			if (id != null)
			{
				Trans.begin();

				System.out.println("Loading in.....");
				if(status == 1)
				{
				    isbreak = teachingSchedule.teachingSchedule(course, true);
				    if(!isbreak)
				    {
				    	Trans.rollback();
						Trans.commit();
						Code.error(mb, "该老师在同一时间段已经有其他课程!");
						return mb;
				    }
				}
				else
				{
				    teachingSchedule.deleteTeachingSchedule(course.getId());
				}
//			    if(course.getStatus() == 2 || course.getStatus().equals("2"))
//			    {
//			    	System.out.println("\nLoading in.....\n");
//			    	course.setIsCopy(1);
//				    ShopGoods flashCourse = new ShopGoods();
//				    flashCourse.setCorpId(course.getCorpId());
//				    flashCourse.setCoopId(course.getCoopId());
//				    flashCourse.setCoopType(course.getCoopType());
//				    flashCourse.setCouCount(course.getCouCount());
//				    flashCourse.setCoudesc(course.getCoudesc());
//				    flashCourse.setCouSeason(course.getCouSeason());
//				    flashCourse.setCouTime(course.getCouTime());
//				    flashCourse.setDependId(course.getDependId());
//				    flashCourse.setDependName(course.getDependName());
//				    flashCourse.setEduClassifyId(course.getEduClassifyId());
//				    flashCourse.setEduTeacherId(course.getEduTeacherId());
//				    flashCourse.setGainSharingNum(course.getGainSharingNum());
//				    flashCourse.setGainSharingType(course.getGainSharingType());
//				    flashCourse.setLocation(course.getLocation());
//				    flashCourse.setMax(course.getMax());
//				    flashCourse.setName(course.getName());
//				    flashCourse.setPrice(course.getPrice());
//				    flashCourse.setSold(course.getSold());
//				    flashCourse.setStartDate(course.getStartDate());
//				    flashCourse.setStatus(status);
//				    flashCourse.setType(course.getType());
//				    dao.update(course2);
//				    dao.insert(flashCourse);
//			    }
//			    else
//			    {
			    	course.setStatus(status);
				    dao.update(course);
//			    }
			    System.out.println("\n\n" + isbreak);
			    
//				if(middleList == null || middleList.size() == 0)
//				{
//					middle.setStatus(99);
//					middle.setParentId(course.getId());
//					middle.setCorpId(274);
//					middle.setEduTeacherId(teacherId);
//					dao.insert(middle);
//				}
//				System.out.println("Break out......");
				Trans.commit();
			}
			else
			{
				Trans.begin();
				System.out.println("Loading in......");
                if(!course.getCoopType().equals("合作"))
                {
    				List<EduTeachingSchedule> eduTeachingSchedules = teachingSchedule.getWorkDay(course.getCouTime(), course.getCouCount(), course.getStartDate());
    				//查找课程是否冲突
    				
    				if(status == 1)
    				{
        				if(teachingSchedule.checkSchedule(eduTeachingSchedules, course.getEduTeacherId()))
        				{
        					isbreak = true;
        				}
        				if(!isbreak)
        			    {
        					Trans.rollback();
        					Trans.commit();
        					Code.error(mb, "该老师在同一时间段已经有其他课程!");
        					return mb;
        			    }

    				}
    				else
    				{
    				    teachingSchedule.deleteTeachingSchedule(course.getId());
    				}
    				
                }
        		Asserts.isNull(course.getCouTime(), "请指定课程的具体时间");
        		Asserts.isNull(course.getCouCount(), "请指定课次");
        		Asserts.isNull(course.getStartDate(), "请指定课程开始日期");
        		Asserts.isNull(course.getEduTeacherId(), "请指定授课老师");
        		Asserts.isNull(course.getLocation(), "请指定上课地点");
        		course.setSold(0);
        		course.setStatus(status);
//        		List<ShopGoods > sgs = new ArrayList<ShopGoods>();
//        		sgs.add(course);
//        		dao.fastInsert(sgs);
				course = dao.insert(course);
	
				if(!course.getCoopType().equals("合作"))
                {
    				if(status == 1)
    				{
    					isbreak = teachingSchedule.teachingSchedule(course, true);
     				}
    				else
    				{
    				    teachingSchedule.deleteTeachingSchedule(course.getId());
    				}
                }
				Trans.commit();

//				if(middleList == null || middleList.size() == 0)
//				{
//					middle.setParentId(rollback.getId());
//					middle.setCorpId(274);
//					middle.setEduTeacherId(teacherId);
//					dao.insert(middle);
//				}
				System.out.println("Break out......");
			}
		
			Code.ok(mb, (id == null ? "新建" : "编辑") + course.getType() + "成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Subject:add) error: ", e);
			Code.error(mb, (id == null ? "新建" : "编辑") + course.getType() + "失败");
		}
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			String temp[] = checkedIds.split(",");
			
			Trans.begin();
			if(status == Status.ENABLED)
			{
				System.out.println("Loading in enabled......");
				for(int i=0;i<temp.length;i++)
				{
					ShopGoods courseFlag = dao.fetch(ShopGoods.class,Integer.parseInt(temp[i]));
					System.out.println(courseFlag);
					Boolean flag = teachingSchedule.teachingSchedule(courseFlag, true);
					if(!flag)
					{
						Code.error(mb, "同一老师的多个课程间的上课时间有冲突！");
						return mb;
					}
				}
			}
			else
			{
				System.out.println("Loading in unabled......");
				for(int i=0;i<temp.length;i++)
				{
					teachingSchedule.deleteTeachingSchedule(Integer.parseInt(temp[i]));
				}
			}
			
			if (arr != null && arr.length > 0) {
				cache.removeAll(Cache.FQN_RESOURCES);
				dao.update(ShopGoods.class, Chain.make("status", status), Cnd.where("id", "in", arr));
			}
			Trans.commit();
			Code.ok(mb, "设置资源状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("("
					+ ") error: ", e);
			Code.error(mb, "设置资源状态失败");
		}

		return mb;
	}

	//进行前端页面，合作课程栏目的自动填充
	@POST
	@At
	@Ok("json")
	public List<String> autoCompleteCourse(HttpServletRequest req, HttpServletResponse res) throws Exception {  
		List<String> json = new ArrayList<String>();

		String orgId = req.getParameter("coopId");
		
		List<ShopGoods> courseList = new ArrayList<ShopGoods>();
		
		System.out.println(orgId + "\n");
		
	    courseList = dao.query(ShopGoods.class,Cnd.where("status","=",Status.ENABLED).and("corp_id","=",orgId).and("coop_id","is",null).and("coopType","=","自营"));

	    //定义数组，添加返回的集合中的学校名称字段  
	    for(int i=0;i<courseList.size();i++)
	    {
	    	json.add(courseList.get(i).getName());
	    	System.out.println(courseList.get(i).getName());
	    }
	    if(courseList.size() == 0)
	    {
	    	json.add("没有数据！");
	    }
	    //返回json串  
	    return json;  
	}  

	//进行前端页面，老师栏目的自动填充
	@POST
	@At
	@Ok("json")
	public List<String> autoCompleteTeacher(HttpServletRequest req, HttpServletResponse res) throws Exception {  
		List<String> json = new ArrayList<String>();
		Integer orgId = Context.getCorpId();

		String coopId = req.getParameter("coopId");
		String couType = req.getParameter("couType");
		
		List<EduTeacher> teacherList = new ArrayList<EduTeacher>();
		
		System.out.println(coopId + "\n" + couType);
		
		if(couType.equals("自营"))
		{
		    teacherList = dao.query(EduTeacher.class, Cnd.where("status", "=", 1).and("corp_id","=",orgId).and("coopId","is",null).and("coopType","=","自营"));  
		}
		if(couType.equals("兼职"))
		{
		    teacherList = dao.query(EduTeacher.class, Cnd.where("status", "=", 1).and("corp_id","=",orgId).and("coopId","is",null).and("coopType","=","兼职"));  
		}
	    //定义数组，添加返回的集合中的学校名称字段  
	    for(int i=0;i<teacherList.size();i++)
	    {
	    	json.add(teacherList.get(i).getTruename());
	    }
	    if(teacherList.size() == 0 && !couType.equals("合作"))
	    {
	    	json.add("没有数据！");
	    }
	    //返回json串  
	    return json;  
	}  
	
	//判断是否拥有该个合作课程
	@POST
	@At
	@Ok("json")
	public Object matchCourse(HttpServletRequest req, HttpServletResponse res) throws Exception {  
		
		MapBean mb = new MapBean();
		
		String coopId = req.getParameter("coopId");
		String couName = req.getParameter("couName");
		String couWeekday = null;
		String startTime = null;
		String endTime = null;
		String startTime1 = null;
		String endTime1 = null;
		String startTime2 = null;
		String endTime2 = null;
		String startTime3 = null;
		String endTime3 = null;
		String startTime4 = null;
		String endTime4 = null;
		String startTime5 = null;
		String endTime5 = null;
		String startTime6 = null;
		String endTime6 = null;
		String startTime7 = null;
		String endTime7 = null;
		String dateStr = null;
        String teacherName = null;
		String parentName = null;
		
		ShopGoods course = new ShopGoods();
				
        course = dao.fetch(ShopGoods.class,Cnd.where("corp_id","=",coopId).and("name","=",couName).and("status","=",Status.ENABLED));
		teacherName = dao.fetch(EduTeacher.class,course.getEduTeacherId()).getTruename();
        parentName = dao.fetch(EduClassify.class,course.getEduClassifyId()).getName();
        
		String[] temp = course.getCouTime().split("\\|");
		couWeekday = temp[0];
		//转换日期格式
		dateStr = new SimpleDateFormat("yyyy-MM-dd").format(course.getStartDate());
		if(couWeekday.equals("3"))
		{
			System.out.println(temp.length);
			if(!temp[1].equals(""))
			{
				startTime1 = temp[1].split("-")[0];
				endTime1 = temp[1].split("-")[1];
				mb.put("startTime1", startTime1);
				mb.put("endTime1", endTime1);
			}
			if(!temp[2].equals(""))
			{
				startTime2 = temp[1].split("-")[0];
				endTime2 = temp[1].split("-")[1];
				mb.put("startTime2", startTime2);
				mb.put("endTime2", endTime2);
			}
			if(!temp[3].equals(""))
			{
				startTime3 = temp[1].split("-")[0];
				endTime3 = temp[1].split("-")[1];
				mb.put("startTime3", startTime3);
				mb.put("endTime3", endTime3);
			}
			if(!temp[4].equals(""))
			{
				startTime4 = temp[1].split("-")[0];
				endTime4 = temp[1].split("-")[1];
				mb.put("startTime4", startTime4);
				mb.put("endTime4", endTime4);
			}
			if(!temp[5].equals(""))
			{
				startTime5 = temp[1].split("-")[0];
				endTime5 = temp[1].split("-")[1];
				mb.put("startTime5", startTime5);
				mb.put("endTime5", endTime5);
			}
			if(!temp[6].equals(""))
			{
				startTime6 = temp[1].split("-")[0];
				endTime6 = temp[1].split("-")[1];
				mb.put("startTime6", startTime6);
				mb.put("endTime6", endTime6);
			}
			if(temp.length > 7 && !temp[7].equals(""))
			{
				startTime7 = temp[1].split("-")[0];
				endTime7 = temp[1].split("-")[1];
				mb.put("startTime7", startTime7);
				mb.put("endTime7", endTime7);
			}
		}
		else
		{
			startTime = temp[1].split("-")[0];
			endTime = temp[1].split("-")[1];
			mb.put("startTime", startTime);
			mb.put("endTime", endTime);
		}
        
        mb.put("course",course);
        mb.put("dateStr",dateStr);
		mb.put("parentName",parentName);
		mb.put("dependId",parentName);
        mb.put("couWeekday", couWeekday);
        mb.put("teacherName",teacherName);
        
	    return mb;  
	}  
	
}
