package cn.oa.service;

import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.consts.Status;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.repository.Mapper;
import cn.oa.repository.RoleRepository;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;

@IocBean
public class UserService {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	protected RoleRepository roleRepository;

	/**
	 * 上级审批人员.
	 */
	public List<User> operators(Integer corpId, Integer level) {
		if(level == 4){ // 总经理(level=4)上级为总裁(level=5)
			return dao.query(User.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("level", "=", 5));
		}
		return dao.query(User.class, Cnd
				.where("status", "=", Status.ENABLED)
				.and("corpId", "=", corpId)
				.and("level", ">", level));
	}
	
	/**
	 * 角色审批人员.
	 */
	public List<User> operators(Integer corpId, String roleName) {
		Criteria cri = Cnd.cri();
		cri.where()
				.and("u.status", "=", Status.ENABLED)
				.and("r.role_name", "=", roleName);
		if (corpId != null)
			cri.where().and("u.corp_id", "=", corpId);
		
		return mapper.query(User.class, "User.operator", cri);
	}
	
	/**
	 * 集团角色审批人员
	 * @param roleName
	 * @return
	 */
	public List<User> operators(String roleName) {
		Criteria cri = Cnd.cri();
		cri.where()
				.and("u.status", "=", Status.ENABLED)
				.and("r.role_name", "=", roleName);
		return mapper.query(User.class, "User.operator", cri);
	}
	
	
	public List<User> operators(Integer corpId, Integer level, Integer[] excludeActors) {
		Criteria cri = Cnd.cri();
		cri.where()
				.and("u.status", "=", Status.ENABLED)
				.and("u.corp_id", "=", corpId)
				.and("u.level", ">", level)
				.and("u.user_id", "not in", excludeActors);
		return mapper.query(User.class, "User.operator", cri);
	}
	
	public List<User> operators(Integer corpId, Integer level, String[] excludeRoles) {
		Criteria cri = Cnd.cri();
		cri.where()
				.and("u.status", "=", Status.ENABLED)
				.and("u.corp_id", "=", corpId)
				.and("u.level", ">", level);
		if (!Asserts.isEmpty(excludeRoles))
			cri.where().and("r.role_name", "not in", excludeRoles);
		return mapper.query(User.class, "User.operator", cri);
	}
	
	
	public List<User> operators(String[] includeRoles){
		Criteria cri = Cnd.cri();
		cri.where()
				.and("u.status", "=", Status.ENABLED);
		if (!Asserts.isEmpty(includeRoles))
			cri.where().and("r.role_name", "in", includeRoles);
		return mapper.query(User.class, "User.operator", cri);
		}
	
	public List<User> operators(String[] excludeRoles, Integer corpId){
		Criteria cri = Cnd.cri();
		cri.where()
				.and("u.status", "=", Status.ENABLED)
				.and("u.corp_id", "=", corpId);
		if (!Asserts.isEmpty(excludeRoles))
			cri.where().and("r.role_name", "not in", excludeRoles);
		return mapper.query(User.class, "User.operator", cri);
		}
	
	public List<User> operators(Integer corpId, Integer level,
			String[] includeRoles, String[] excludeRoles, Integer[] excludeUserIds) {
		Criteria cri = Cnd.cri();
		cri.where()
				.and("u.status", "=", Status.ENABLED)
				.and("u.user_id", "not in", excludeUserIds);
		if (!Asserts.isEmpty(excludeRoles))
			cri.where().and("r.role_name", "not in", excludeRoles);
			
		SqlExpressionGroup exp = Cnd.exps("r.role_name", "in", includeRoles).or(Cnd.exps("u.level", ">", level).and("u.corp_id", "=", corpId));
		cri.where().and(exp);
		
		return mapper.query(User.class, "User.operator", cri);
	}
	
	public String[] findRoleNames(Integer userId){
		if(userId!=null){
		User user = dao.fetch(User.class, userId);
		List<Role> roles = roleRepository.find(user.getUserId());
		String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
		return roleNames;
		}
		else 
			return null;
		
	}
	
	/**
	 * 公司审核等级最高的人的集合
	 * @param corpId
	 * @return
	 */
	public List<User> levelMax(Integer corpId){
		List<User> users = dao.query(User.class, Cnd.where("corpId", "=", corpId));
		Integer max = 0;
		if(users.size()!=0){
			for(User user : users ){
				if(user.getLevel() > max)
					max = user.getLevel();
			}
		}
		List<User> levelMax = dao.query(User.class, Cnd.where("level", "=", max));
		return levelMax;
	}
	/**
	 * 集团审核等级最高的人集合
	 * @return
	 */
	public List<User> levelMax(){
		List<User> users = dao.query(User.class, null);
		Integer max = 0;
		if(users.size()!=0){
			for(User user : users ){
				if(user.getLevel() > max)
					max = user.getLevel();
			}
		}
		List<User> levelMax = dao.query(User.class, Cnd.where("level", "=", max));
		return levelMax;
	}
}
