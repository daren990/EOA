package cn.oa.repository;

import java.util.List;
import java.util.Map;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import cn.oa.app.cache.Ehcache;
import cn.oa.consts.Cache;
import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.model.Authority;
import cn.oa.model.Resource;
import cn.oa.model.Role;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.web.Context;

@IocBean
public class ResourceRepository {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private Ehcache cache;
	
	private static final Log log = Logs.getLog(ResourceRepository.class);
	
	public List<Resource> find(Integer userId) {
		log.debug("find(Integer userId)");
		@SuppressWarnings("unchecked")
		List<Resource> resources = (List<Resource>) cache.get(Cache.FQN_RESOURCES, Integer.toString(userId));
		
		if (Asserts.isEmpty(resources)) {
			Integer[] roleIds = Converts.array(
					Role.class, Integer.class,
					mapper.query(Role.class, "Role.join", Cnd
							.where("u.user_id", "=", userId)
							.and("u.status", "=", Status.ENABLED)
							.and("r.status", "=", Status.ENABLED)), "roleId");

			if (Asserts.isEmpty(roleIds)) {
		
				return resources;
			}
			
			resources = mapper.query(Resource.class, "Resource.join", Cnd
							.where("r.role_id", "in", roleIds)
							.and("r.status", "=", Status.ENABLED)
							.and("a.status", "=", Status.ENABLED)
							.and("rs.status", "=", Status.ENABLED)
							.asc("rs.position"));
			
			System.out.println("\n\n\n\n\n" + Cnd
							.where("r.role_id", "in", 4)
							.and("r.status", "=", Status.ENABLED)
							.and("a.status", "=", Status.ENABLED)
							.and("rs.status", "=", Status.ENABLED)
							.asc("rs.position") + "\n\n\n\n\n");
			
			cache.put(Cache.FQN_RESOURCES, Integer.toString(userId), resources);
		
			return resources;
		}
		return resources;
	}
	
	/**
	 * 传sec_resource里的action跟resource_name
	 * @param action
	 * @param resource_name
	 * @return true有按钮权限,false没有按钮权限
	 */
	public boolean getBtnAuthority(String action,String resource_name){
		if (Asserts.hasAny(Roles.ADM.getName(), Context.getRoles())) {
			return true;
		}
		List<Resource> resources = find(Context.getUserId());
		Map<String, Resource> actionMap = Context.actionMap(resources);
		
		if(actionMap.containsKey(action)){
			for (Resource resource : resources) {
				if(resource.getResourceName().equals(resource_name) && resource.getAction().equals(action)){
					return true;
				}
			}
			return false;
		}else{
			return false;
		}
	}
	
	/**
	 * 传sec_resource里的action跟resource_name
	 * @param action
	 * @param resource_name
	 * @return true有按钮权限,false没有按钮权限
	 */
	public boolean getBtnAuthority_database(String action,String resource_name){
		if (Asserts.hasAny(Roles.ADM.getName(), Context.getRoles())) {
			return true;
		}
		SimpleCriteria cri = Cnd.cri();
		cri.where().and(new Static(" authority_id = "
									+ " (select authority_id from sec_authority_resource "
										+ " where authority_id = (select authority_id from sec_role_authority where role_id = "+Context.getRoles()+") "
										+ " and resource_id = (select resource_id from sec_resource where action = '"+action+"' and resource_name = '"+resource_name+"' and type = 2))"));
		Authority a = dao.fetch(Authority.class,cri);
		if(a!=null){
			return true;
		}else{
			return false;
		}
	}
}
