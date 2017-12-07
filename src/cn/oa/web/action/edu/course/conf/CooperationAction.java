package cn.oa.web.action.edu.course.conf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import cn.oa.model.CoopOrgRelation;
import cn.oa.model.CooperationOrg;
import cn.oa.model.Org;
import cn.oa.model.ShiftCorp;
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
 * 合作机构管理
 * @author Ash
 */
@IocBean
@At(value = "/edu/course/conf/cooperation")
public class CooperationAction extends Action
{
	
	public static Log log = Logs.getLog(CooperationAction.class);
	
	@GET
	@POST
	@At
	@Ok("ftl:edu/course/conf/coop_page")
	public void page(HttpServletRequest req) {
		
		String coopName = Https.getStr(req, "name", R.CLEAN, R.RANGE, "{1,20}");
		
		Integer myCorpId = Context.getCorpId(); 
		
		Org myOrg = dao.fetch(Org.class, Cnd.where("org_id","=",myCorpId));
		
		//是否平台级用户
		if(myOrg.getType().equals(2) || myOrg.getType() == 2)
		{
			MapBean mb = new MapBean();
			
			Page<Org> page = Webs.page(req);
			page = mapper.page(Org.class, page, "Cooperation.count", "Cooperation.index", coopName==null?Cnd.where("1","=",1).and("status","!=","-1").and("type","=","3"):Cnd.where("c.org_name","like","%" + coopName + "%").and("status","!=","-1").and("type","=","3"));
			
			if(coopName != null && !coopName.equals(""))
			{
				mb.put("name",coopName);
			}
			
			req.setAttribute("mb", mb);
			req.setAttribute("flag", true);
			req.setAttribute("page", page);
		}
		else
		{
			MapBean mb = new MapBean();
			
			Page<Org> page = Webs.page(req);
			page = mapper.page(Org.class, page, "Cooperation.count", "Cooperation.index", coopName==null?Cnd.where("parent_id","=",Context.getCorpId()).and("status","!=","-1").and("type","=","0"):Cnd.where("c.org_name","like","%" + coopName + "%").and("status","!=","-1").and("type","=","0").and("parent_id","=",Context.getCorpId()));
			
			if(coopName != null && !coopName.equals(""))
			{
				mb.put("name",coopName);
			}
			
			req.setAttribute("page", page);
			req.setAttribute("mb", mb);
		}

	}
		
	@At
	@Ok("ftl:edu/course/conf/coop_add")
	public void add(HttpServletRequest req) {
		Integer id = Https.getInt(req, "id", R.REQUIRED, R.I);
		
		Org parent = null;
		String flag = Https.getStr(req, "flag");
		StringBuffer str = new StringBuffer("select o.org_id as orgId,o.org_name as orgName,c.coopId as coopId,c.gainsharingtype,c.gainsharingnum "
				+ "from sec_org o "
				+ "left join sec_cooperation_org c "
				+ "on o.org_id = c.coopIdOther "
				+ "where o.type = 3");
		
		Org cooperation = null;
		
		//是否修改
		if (id != null) {
			cooperation = dao.fetch(Org.class, id);
			if (cooperation != null) {
				dao.fetchLinks(cooperation, "coop");
			}
			parent = dao.fetch(Org.class, Cnd.where("parent_id","=",id));
			if(parent == null)
			{
				parent = new Org();
			}
		}
		else
		{
			parent = dao.fetch(Org.class, Cnd.where("org_id","=",Context.getCorpId()));
		}
		if (cooperation == null) cooperation = new Org();
		
		Sql sql = Sqls.create(str.toString());
		//Cnd.where("parent_id", "=", Context.getOrgId()).asc("org_id")
		
		sql.setCallback(Sqls.callback.entities());
		Entity<CoopOrgRelation> entity = dao.getEntity(CoopOrgRelation.class);
	    sql.setEntity(entity);
		
		dao.execute(sql);
		List<CoopOrgRelation> coopRelationship = sql.getList(CoopOrgRelation.class);

		Org org = new Org();
		org.setOrgId(Context.getCorpId());
		List<Org> nodes = coopCorpService.findCenterAndCoopCorp(org);
		
		List<Org> orgs = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED));
		//从orgs中找到corps中的所有直接或间接的下级
		nodes = findSubordinate(nodes, orgs);
		
//		List<CooperationOrg> coopRelationship = dao.query(CooperationOrg.class, Cnd
//				.where("status", "=", Status.ENABLED));
				
		req.setAttribute("flag", flag);
		req.setAttribute("nodes", nodes);
		req.setAttribute("parent", parent);
		req.setAttribute("corpId", Context.getCorpId());
		req.setAttribute("cooperation", cooperation);
		req.setAttribute("coopRelationship", coopRelationship);
	}
	
	private List<Org> findSubordinate(List<Org> higherLevels, List<Org> allOrgs){
		//转换成key为parentId的map集合
		Map<Integer, List<Org>> pidMap = getPidMap(allOrgs);
		List<Org> orgs = new ArrayList<Org>();
		orgs.addAll(higherLevels);
		//找到所有的直接或间接的下级
		orgs.addAll(findSubordinate(higherLevels, pidMap));
		//去重
		duplicate(orgs);
		return orgs;
	}
	
	private List<Org> findSubordinate(List<Org> higherLevels, Map<Integer, List<Org>> pidMap){
		List<Org> orgs = new ArrayList<Org>();
		for(Org higherLevel : higherLevels){
			if(pidMap.containsKey(higherLevel.getOrgId())){
				List<Org> partOrgs = pidMap.get(higherLevel.getOrgId());
				//将子级注入到要返回的集合中
				orgs.addAll(partOrgs);
				//将子级当中父亲，再去递归，最终得到所有的子级
				orgs.addAll(findSubordinate(partOrgs, pidMap));
			}
		}
		return orgs;
	}
		
	private Map<Integer, List<Org>> getPidMap(List<Org> orgs){
		Map<Integer, List<Org>> map = new HashMap<Integer, List<Org>>();
		for(Org org : orgs){
			List<Org> os = map.get(org.getParentId());
			if(os == null){
				os = new ArrayList<Org>();
				map.put(org.getParentId(), os);
			}
			os.add(org);
		}
		return map;
	}
	
	private List<Org> duplicate(List<Org> orgs){
		//手动去重
		for (int i = 0; i < orgs.size(); i++){  //外循环是循环的次数
            for (int j = orgs.size() - 1; j > i; j--){  //内循环是 外循环一次比较的次数
                if (orgs.get(i).getOrgId().equals(orgs.get(j).getOrgId())){
                	orgs.remove(j);
                }
            }
        }
		return orgs;
	}
	
	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer orgId = Https.getInt(req, "orgid");
			
			if(orgId.equals(0) || orgId == null)
			{
				orgId = 0;
			}
			
			List<Org> nodes = dao.query(Org.class, Cnd.where("org_id","=",orgId));
//			if(couSearchName == null || couSearchName.equals(""))
//			{
				nodes = dao.query(Org.class,Cnd.where("parent_id","=",orgId).and("type","=","0").asc("org_id"));
//			}
//			else
//			{
//				nodes = dao.query(Org.class, 
//						Cnd.where("parent_id","=",orgId)
//						.and("org_name","like","%" + couSearchName + "%").and("type","=","4").asc("org_id"));
//			}

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
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer id = null;
		try {
			
			id = Https.getInt(req, "id", R.I);
			String flag = Https.getStr(req, "flag");
			String name = Https.getStr(req, "name", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "机构名称");
			String location = Https.getStr(req, "location", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "机构地址");
			Integer parentId = Https.getInt(req, "parentId");
			String contactName = Https.getStr(req, "contactName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "联系人");
			String contactNumber = Https.getStr(req, "contactNumber", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "联系人电话");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			System.out.println("flag : " + flag);
			
			Org cooperation = null;
			if (id != null) {
				cooperation = dao.fetch(Org.class, id);
				Asserts.isNull(cooperation, "机构不存在");
				cooperation.setOrgName(name);
				cooperation.setLocation(location);
				cooperation.setStatus(status);
				cooperation.setContactName(contactName);
				cooperation.setContactNumber(contactNumber);
				dao.update(cooperation);
			} else {
				Org org = new Org();
				org.setContactName(contactName);
				org.setContactNumber(contactNumber);
				org.setStatus(status);
				org.setCreateTime(new Date());
				org.setModifyTime(new Date());
				org.setLocation(location);
				//若为平台级用户，添加机构
				if(flag.equals("true"))
				{
					org.setType(3);
					try
					{
						List<Org> parentIdList = dao.query(Org.class,Cnd.where("type","=","2"));
						org.setParentId(parentIdList.get(0).getOrgId());
					}
					catch(Exception e)
					{
						Code.error(mb,"未有培训中心!");
					}
				}
				//若不为平台级用户，添加部门
				else
				{
					org.setType(0);		
					org.setParentId(parentId);
				}
				org.setOrgName(name);
				
				dao.insert(org);
				//添加机构的时候启用新排班
				if(flag.equals("true")){
					ShiftCorp s = dao.fetch(ShiftCorp.class, Cnd.where("corpId", "=", org.getOrgId()));
					if(s == null){
						ShiftCorp shiftCorp = new ShiftCorp();
						shiftCorp.setCorpId(org.getOrgId());
						dao.insert(shiftCorp);
					}
				}
			}

			cache.removeAll(Cache.FQN_RESOURCES);
						
			Code.ok(mb, (id == null ? "新建" : "编辑") + "机构成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Cooperation:add) error: ", e);
			Code.error(mb, (id == null ? "新建" : "编辑") + "机构失败");
		}

		return mb;
	}
	
	@GET
	@POST
	@At
	@Ok("ftl:edu/course/conf/coop_coop")
	public void cooperation(HttpServletRequest req) {
		
		Integer id = Https.getInt(req, "id");
		String flag = Https.getStr(req, "flag");
		
		if(flag != null && !flag.equals("true") )
		{
			id = Context.getCorpId();
		}
		
		String coopName = Https.getStr(req, "name", R.CLEAN, R.RANGE, "{1,20}");
		
		MapBean mb = new MapBean();
		
		Page<Org> page = Webs.page(req);
		page = mapper.page(Org.class, page, "Cooperation.count", "Cooperation.index", coopName==null?Cnd.where("c.status","in",1).and("c.org_id","!=",id).and("type","=","3"):Cnd.where("c.status","in",1).and("c.org_name","like","%" + coopName + "%").and("c.id","!=",id).and("type","=","3"));
		List<CooperationOrg> flagList = new ArrayList<CooperationOrg>();
		
		System.out.println("\n\n\n\n\n" + id + "\n\n\n\n\n");
		
		flagList = dao.query(CooperationOrg.class,Cnd.wrap("coopId = " + id + " and coopIdOther != coopId"));
		
		if(coopName != null && !coopName.equals(""))
		{
			mb.put("name",coopName);
		}		
		
		req.setAttribute("size", page.getResult().size());
		req.setAttribute("flagSize", flagList.size());
		req.setAttribute("flagList", flagList);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
		req.setAttribute("id", id);
	}
		
	@POST
	@At
	@Ok("json")
	public Object cooperationAjax(HttpServletRequest req, HttpServletResponse res) throws Exception{
		MapBean mb = new MapBean();
		Integer id = null;
		try {
			id = Https.getInt(req, "id", R.I);
			String id_name = Https.getStr(req, "coopId");
			String flag = Https.getStr(req, "flag");
			
			Integer coopId = Integer.parseInt(id_name.split("_")[0]);
			
			System.out.println("Loading in......");
			
			//如果是取消合作关系
			if(flag.equals("false"))
			{
				StringBuffer str = new StringBuffer("delete from sec_cooperation_org where coopId = " + id + " and coopIdOther = " + coopId + " or (coopId = " + coopId + " and coopIdOther = " + id + ")");
				Sql sql = Sqls.create(str.toString());
				dao.execute(sql);
				//删除自己机构和指定机构合作关系,共两条记录，我指向他人，他人指向我
				Code.ok(mb, (id == null ? "新建" : "编辑") + "机构成功");
				return mb;
			}

			CooperationOrg co = new CooperationOrg();
			co.setCoopId(id);
			co.setCoopIdOther(coopId);
			co.setGainSharingNum(50);
			co.setGainSharingType(2);
			dao.insert(co);
	        //自己指向指定机构的合作关系
			
			CooperationOrg co2 = new CooperationOrg();
			co2.setCoopId(coopId);
			co2.setCoopIdOther(id);
			co2.setGainSharingNum(50);
			co2.setGainSharingType(2);
			dao.insert(co2);
            //指定机构指向自己的合作关系
			
//					List<EduTeacher> teacherList = new ArrayList<EduTeacher>();
//					teacherList = dao.query(EduTeacher.class,Cnd.where("corp_id","=",orgId));
//					for(int j=0;j<teacherList.size();j++)
//					{
//						teacherService.insertCoopTeacher(orgId, teacherId, id);
//					}

			cache.removeAll(Cache.FQN_RESOURCES);

			Code.ok(mb, (id == null ? "新建" : "编辑") + "机构成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Cooperation:add) error: ", e);
			Code.error(mb, e.getMessage());
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
				dao.update(Org.class, Chain.make("status", status), Cnd.where("org_id", "in", arr));
			}
			Code.ok(mb, "设置权限状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Org:able) error: ", e);
			Code.error(mb, "设置机构状态失败");
		}

		return mb;
	}
}
