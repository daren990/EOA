package cn.oa.web.action.sys;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.model.Authority;
import cn.oa.model.Role;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/sys/role")
public class RoleAction extends Action {

	public static Log log = Logs.getLog(RoleAction.class);
	
	@POST
	@GET
	@At
	@Ok("ftl:sys/role_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/role/able", token);
		
		String roleName = Https.getStr(req, "roleName", R.CLEAN, R.RANGE, "{1,200}");
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "r.role_desc", "roleName", roleName);
		cri.getOrderBy().desc("r.modify_time");
		
		Page<Role> page = Webs.page(req);
		page = mapper.page(Role.class, page, "Role.count", "Role.index", cri);
		
		List<Authority> authorities = mapper.query(Authority.class, "RoleAuthority.join", Cnd
				.where("a.status", "=", Status.ENABLED));

		for (Role r : page.getResult()) {
			List<Authority> authorityList = new ArrayList<Authority>();
			for (Authority a : authorities) {
				if (r.getRoleId().equals(a.getRoleId())) {
					authorityList.add(a);
				}
			}
			r.setAuthorities(authorityList);
		}
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:sys/role_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer roleId = Https.getInt(req, "roleId", R.REQUIRED, R.I);
		Role role = null;
		if (roleId != null) {
			role = dao.fetch(Role.class, roleId);
			if (role != null) {
				dao.fetchLinks(role, "authorities");
			}
		}
		if (role == null) role = new Role();
		
		List<Authority> authorities = dao.query(Authority.class, Cnd.where("status", "=", Status.ENABLED));

		req.setAttribute("role", role);
		req.setAttribute("authorities", authorities);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer roleId = null;
		try {
			CSRF.validate(req);
			roleId = Https.getInt(req, "roleId", R.I);
			String authorityIds = Https.getStr(req, "authorityIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			String orgRoleName = Https.getStr(req, "orgRoleName", R.CLEAN, R.RANGE, "{1,20}", "权限名称");
			String roleName = Https.getStr(req, "roleName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "权限名称");
			String roleDesc = Https.getStr(req, "roleDesc", R.CLEAN, R.RANGE, "{1,20}", "权限描述");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Asserts.notUnique(roleName, orgRoleName,
					dao.count(Role.class, Cnd.where("roleName", "=", roleName).and("status", "=", Status.ENABLED)),
					"权限名称已存在");
			
			DateTime now = new DateTime();
			Role role = null;
			if (roleId != null) {
				role = dao.fetch(Role.class, roleId);
				Asserts.isNull(role, "roleName:权限不存在");
			} else {
				role = new Role();
				role.setStatus(Status.ENABLED);
				role.setCreateTime(now.toDate());
			}
			
			role.setRoleName(roleName);
			role.setRoleDesc(roleDesc);
			role.setStatus(status);
			role.setModifyTime(now.toDate());
			
			List<Authority> authorities = new ArrayList<Authority>();
			for (Integer id : Converts.array(authorityIds, ",")) {
				Authority a = new Authority();
				a.setAuthorityId(id);
				authorities.add(a);
			}
			role.setAuthorities(authorities);
			
			cache.removeAll(Cache.FQN_RESOURCES);
			transSave(roleId, role);
			
			Code.ok(mb, (roleId == null ? "新建" : "编辑") + "角色成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Role:add) error: ", e);
			Code.error(mb, (roleId == null ? "新建" : "编辑") + "角色失败");
		}

		return mb;
	}
	
	private void transSave(final Integer roleId, final Role role) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (roleId != null) {
					dao.update(role);
					dao.clearLinks(role, "authorities");
				} else {
					dao.insert(role);
				}
				dao.insertRelation(role, "authorities");
			}
		});
	}
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			if (arr != null && arr.length > 0) {
				cache.removeAll(Cache.FQN_RESOURCES);
				dao.update(Role.class, Chain.make("status", status), Cnd.where("roleId", "in", arr));
			}
			Code.ok(mb, "设置角色状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Role:able) error: ", e);
			Code.error(mb, "设置角色状态失败");
		}

		return mb;
	}
}
