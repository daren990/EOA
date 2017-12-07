package cn.oa.web.action.edu.course.conf;

import java.util.ArrayList;
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

import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.model.EduLocation;
import cn.oa.model.Org;
import cn.oa.utils.Asserts;
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

/**
 * 上课地点管理
 * @author Ash
 */
@IocBean
@At(value = "/edu/course/conf/location")
public class LocationAction extends Action
{

	public static Log log = Logs.getLog(LocationAction.class);

	@GET
	@POST
	@At
	@Ok("ftl:edu/course/conf/location_page")
	public void page(HttpServletRequest req) {
		
		String location = Https.getStr(req, "name", R.CLEAN, R.RANGE, "{1,20}");
		Integer coopSearchId = Https.getInt(req, "coopSearchId");
		String couSearchName = Https.getStr(req, "couSearchName");
		
		Integer flag2 = 1; // 0为平台管理员
		
		MapBean mb = new MapBean();
		
		Boolean flag = false; //true则有子类
		
		List<Org> flagList = new ArrayList<Org>();
		
		flagList = dao.query(Org.class,Cnd.where("parent_id","=",Context.getCorpId()).and("type","=","3"));
		
		//如果存在他的上级
		if(flagList == null || flagList.size() == 0)
		{
			flag = true;
		}
		else
		{
			flagList = dao.query(Org.class,Cnd.where("org_id","=",Context.getCorpId()));
			flag2 = flagList.get(0).getParentId();
		}

        StringBuffer str = new StringBuffer("select distinct o.* "
        		+ "from sec_org as o,edu_location as l "
        		+ "where o.org_id = l.org_id and o.type = 3 "
        		+ "and o.parent_id = " + Context.getCorpId() + " "
        		+ "and o.status = 1 "
        		+ "and l.status != -1 ");

		if(flag)
		{
			str = new StringBuffer("select distinct o.* "
	        		+ "from sec_org as o,edu_location as l "
	        		+ "where o.org_id = l.org_id and o.type = 3 "
	        		+ "and (o.parent_id = " + Context.getCorpId() + " or o.org_id = " + Context.getCorpId() + ") "
	        		+ "and o.status = 1 "
	        		+ "and l.status != -1 ");
		}
        
		if(coopSearchId != null && coopSearchId != 0)
		{
			str.append("and o.org_id = " + coopSearchId + " ");
		}
		if(couSearchName != null && !couSearchName.equals(""))
		{
			str.append("and l.location like '%" + couSearchName + "%' ");
		}
        
        List<Org> nodes = new ArrayList<Org>();
		
		Sql sql = Sqls.create(str.toString());
		//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
		
		sql.setCallback(Sqls.callback.entities());
		Entity<Org> entity = dao.getEntity(Org.class);
	    sql.setEntity(entity);

		dao.execute(sql);
		nodes = sql.getList(Org.class);
	
		Page<EduLocation> page = Webs.page(req);
		page = mapper.page(EduLocation.class, page, "Location.count", "Location.index", couSearchName==null?Cnd.where("org_id","=",Context.getCorpId()).and("status","!=","-1"):Cnd.where("e.location","like","%" + couSearchName + "%").and("org_id","=",Context.getCorpId()).and("status","!=","-1"));
				
		if(location != null && !location.equals(""))
		{
			mb.put("name",location);
		}
		
		List<Org> allnotes = new ArrayList<Org>();
        if(flag2 == 0)
        {
        	allnotes = dao.query(Org.class, Cnd.where("status","=","1").and("parent_id","=",Context.getCorpId()));
        }
        else
        {
        	allnotes = dao.query(Org.class, Cnd.where("status","=","1").and("parent_id","=",Context.getCorpId()).or("org_id","=",Context.getCorpId()));
        }
 
		req.setAttribute("flag", flag);
		req.setAttribute("flag2", flag2);
		req.setAttribute("flag3", resourceRepository.getBtnAuthority("/edu/course/conf/location/add", "上课地点编辑权力"));
		req.setAttribute("flagOrg", Context.getCorpId());
		req.setAttribute("allNodes", allnotes);
		req.setAttribute("corpId", coopSearchId);
		req.setAttribute("couSearchName", couSearchName);
		req.setAttribute("couSearchNameVal", couSearchName);
		req.setAttribute("page", page);
		req.setAttribute("nodes", nodes);
		req.setAttribute("mb", mb);

	}
		
	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer orgId = Https.getInt(req, "orgid");
			String couSearchName = Https.getStr(req, "couSearchName");
			
			List<EduLocation> nodes = dao.query(EduLocation.class, Cnd.where("org_id","=",orgId));
			if(couSearchName == null || couSearchName.equals(""))
			{
				System.out.println("A:");
				nodes = dao.query(EduLocation.class, 
						Cnd.where("org_id","=",orgId).and("status","!=","99").and("status","!=","-1")
						   .asc("id"));
			}
			else
			{
				nodes = dao.query(EduLocation.class, 
						Cnd.where("org_id","=",orgId)
						   .and("location","like","%" + couSearchName + "%").and("status","!=","99").and("status","!=","-1")
						   .asc("id"));
			}

			mb.put("orgFlag", Context.getCorpId());
            mb.put("orgId", orgId);
			mb.put("nodes", nodes);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(EduCourse:nodes) error: ", e);
		}

		return mb;
	}

	
	@At
	@Ok("ftl:edu/course/conf/location_add")
	public void add(HttpServletRequest req) {
		Boolean flag = false;
		Integer flag2 = 1;
		Integer id = Https.getInt(req, "id", R.REQUIRED, R.I);
		EduLocation location = null;
		
		List<Org> flagList = dao.query(Org.class, Cnd
				.where("status", "=", Status.ENABLED).and("parent_id","=",Context.getCorpId()));

		if(flagList == null || flagList.size() == 0)
		{
			flag = true;
		}
		else
		{
			flagList = dao.query(Org.class,Cnd.where("org_id","=",Context.getCorpId()));
			flag2 = flagList.get(0).getParentId();
		}
		
		if (id != null) {
			location = dao.fetch(EduLocation.class, id);
			if(location.getOrgId().equals(Context.getCorpId()))
			{
				flag = true;
			}
			else
			{
				flag = false;
			}
			if (location != null) {
				dao.fetchLinks(location, "location");
			}
		}
		if (location == null)
		{
			flag = true;
			location = new EduLocation();
		}
		
		List<EduLocation> locationList = dao.query(EduLocation.class, Cnd
				.where("status", "=", Status.ENABLED));
		
		req.setAttribute("flag", flag);
		req.setAttribute("flag2", flag2);
		req.setAttribute("location", location);
		req.setAttribute("locationList", locationList);
		req.setAttribute("flag3", resourceRepository.getBtnAuthority("/edu/course/conf/location/add", "上课地点编辑权力"));

	}
	
	@POST
	@At
	@Ok("json")
	public Object deleteLocation(HttpServletRequest req, HttpServletResponse res) {
				
		Integer id = Https.getInt(req, "id");
		
		MapBean mb = new MapBean();
		
		EduLocation co = dao.fetch(EduLocation.class,id);		
		
		try
		{
			co.setStatus(-1);
			dao.update(co);
		    Code.ok(mb, "删除机构成功");
	    } catch (Errors e) {
		    Code.error(mb, e.getMessage());
	    } catch (Exception e) {
		    log.error("(Cooperation:add) error: ", e);
		    Code.error(mb, "删除机构失败");
	}
        return mb;

	}

	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer id = null;
		try {
			id = Https.getInt(req, "id", R.I);
			String name = Https.getStr(req, "location", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "权限名称");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			
			EduLocation location = null;
			System.out.println("\n\n\n\n\n" + id + "\n\n\n\n\n");
			if (id != null) {
				location = dao.fetch(EduLocation.class, id);
				Asserts.isNull(location, "机构不存在");
				location.setLocation(name);
				location.setStatus(status);
				dao.update(location);
			} else {
				Asserts.notUnique(name, name,dao.count(EduLocation.class, Cnd.where("location", "=", name)),"上课地点已存在");
				location = new EduLocation();
				location.setOrgId(Context.getCorpId());
				location.setLocation(name);
				location.setStatus(status);
				dao.insert(location);
			}

			cache.removeAll(Cache.FQN_RESOURCES);

			Code.ok(mb, (id == null ? "新建" : "编辑") + "上课地点成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Cooperation:add) error: ", e);
			Code.error(mb, (id == null ? "新建" : "编辑") + "上课地点失败");
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

			if (arr != null && arr.length > 0) {
				cache.removeAll(Cache.FQN_RESOURCES);
				dao.update(EduLocation.class, Chain.make("status", status), Cnd.where("id", "in", arr));
			}
			Code.ok(mb, "设置上课地点状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Org:able) error: ", e);
			Code.error(mb, "设置上课地点状态失败");
		}

		return mb;
	}

	
}
