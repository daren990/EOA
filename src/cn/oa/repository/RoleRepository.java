package cn.oa.repository;

import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.app.cache.Ehcache;
import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.model.Role;
import cn.oa.utils.Asserts;

@IocBean
public class RoleRepository {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private Ehcache cache;
	
	//第一次的时候将roles存入缓存，后来就直接从缓存中获取roles
	public List<Role> find(Integer userId) {
		@SuppressWarnings("unchecked")
		List<Role> roles = (List<Role>) cache.get(Cache.FQN_ROLES, Integer.toString(userId));
		if (Asserts.isEmpty(roles)) {
			roles = mapper.query(Role.class, "Role.query", Cnd
					.where("r.status", "=", Status.ENABLED)
					.and("u.user_id", "=", userId));
			cache.put(Cache.FQN_ROLES, Integer.toString(userId), roles);
			return roles;
		}
		return roles;
	}
}
