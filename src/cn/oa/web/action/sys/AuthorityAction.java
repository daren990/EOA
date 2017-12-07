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
import cn.oa.model.Resource;
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
@At(value = "/sys/authority")
public class AuthorityAction extends Action {

	public static Log log = Logs.getLog(AuthorityAction.class);
	
	@POST
	@GET
	@At
	@Ok("ftl:sys/authority_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/authority/able", token);
		
		String authorityName = Https.getStr(req, "authorityName", R.CLEAN, R.RANGE, "{1,20}");
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "a.authority_name", "authorityName", authorityName);
		cri.getOrderBy().desc("a.modify_time");
		
		Page<Authority> page = Webs.page(req);
		page = mapper.page(Authority.class, page, "Authority.count", "Authority.index", cri);

		List<Resource> resources = mapper.query(Resource.class, "AuthorityResource.join", Cnd
				.where("r.status", "=", Status.ENABLED));

		for (Authority a : page.getResult()) {
			List<Resource> resourceList = new ArrayList<Resource>();
			for (Resource r : resources) {
				if (a.getAuthorityId().equals(r.getAuthorityId())) {
					resourceList.add(r);
				}
			}
			a.setResources(resourceList);
		}
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:sys/authority_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer authorityId = Https.getInt(req, "authorityId", R.REQUIRED, R.I);
		Authority authority = null;
		if (authorityId != null) {
			authority = dao.fetch(Authority.class, authorityId);
			if (authority != null) {
				dao.fetchLinks(authority, "resources");
			}
		}
		if (authority == null) authority = new Authority();
		
		List<Resource> resources = dao.query(Resource.class, Cnd
				.where("status", "=", Status.ENABLED)
				.asc("position").asc("type"));
		
		req.setAttribute("authority", authority);
		req.setAttribute("resources", resources);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer authorityId = null;
		try {
			CSRF.validate(req);
			
			authorityId = Https.getInt(req, "authorityId", R.I);
			String resourceIds = Https.getStr(req, "resourceIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			String orgAuthorityName = Https.getStr(req, "orgAuthorityName", R.CLEAN, R.RANGE, "{1,20}", "权限名称");
			String authorityName = Https.getStr(req, "authorityName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "权限名称");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Asserts.notUnique(authorityName, orgAuthorityName,
					dao.count(Authority.class, Cnd.where("authorityName", "=", authorityName).and("status", "=", Status.ENABLED)),
					"权限名称已存在");
			
			DateTime now = new DateTime();
			Authority authority = null;
			if (authorityId != null) {
				authority = dao.fetch(Authority.class, authorityId);
				Asserts.isNull(authority, "权限不存在");
			} else {
				authority = new Authority();
				authority.setStatus(Status.ENABLED);
				authority.setCreateTime(now.toDate());
			}
			
			authority.setAuthorityName(authorityName);
			authority.setStatus(status);
			authority.setModifyTime(now.toDate());
			
			List<Resource> resources = new ArrayList<Resource>();
			for (Integer id : Converts.array(resourceIds, ",")) {
				Resource r = new Resource();
				r.setResourceId(id);
				resources.add(r);
			}
			authority.setResources(resources);
			cache.removeAll(Cache.FQN_RESOURCES);
			transSave(authorityId, authority);

			Code.ok(mb, (authorityId == null ? "新建" : "编辑") + "权限成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Authority:add) error: ", e);
			Code.error(mb, (authorityId == null ? "新建" : "编辑") + "权限失败");
		}

		return mb;
	}
	
	private void transSave(final Integer authorityId, final Authority authority) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (authorityId != null) {
					dao.update(authority);
					dao.clearLinks(authority, "resources");
				} else {
					dao.insert(authority);
				}
				dao.insertRelation(authority, "resources");
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
				dao.update(Authority.class, Chain.make("status", status), Cnd.where("authorityId", "in", arr));
			}
			Code.ok(mb, "设置权限状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Authority:able) error: ", e);
			Code.error(mb, "设置权限状态失败");
		}

		return mb;
	}
}
