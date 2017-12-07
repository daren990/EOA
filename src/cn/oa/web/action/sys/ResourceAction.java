package cn.oa.web.action.sys;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Resource;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Code;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/sys/resource")
public class ResourceAction extends Action {

	public static Log log = Logs.getLog(ResourceAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/resource_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/resource/nodes", token);
		CSRF.generate(req, "/sys/resource/able", token);
		
		List<Resource> nodes = dao.query(Resource.class, Cnd.where("parentId", "=", Value.I).asc("position"));
		req.setAttribute("nodes", nodes);
	}
	
	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer resourceId = Https.getInt(req, "resourceId", R.REQUIRED, R.I, "资源id");
			List<Resource> nodes = dao.query(Resource.class, Cnd.where("parentId", "=", resourceId).desc("position"));
			mb.put("nodes", nodes);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Resource:nodes) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:sys/resource_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer resourceId = Https.getInt(req, "resourceId", R.REQUIRED, R.I);
		Resource parent = null;
		Resource resource = null;
		if (resourceId != null) {
			resource = dao.fetch(Resource.class, resourceId);
			if (resource != null) {
				parent = dao.fetch(Resource.class, resource.getParentId());
			}
		}

		if (parent == null)
			parent = new Resource();
		if (resource == null)
			resource = new Resource();

		List<Resource> nodes = dao.query(Resource.class, Cnd.where("status", "=", Status.ENABLED).asc("position"));

		req.setAttribute("parent", parent);
		req.setAttribute("resource", resource);
		req.setAttribute("nodes", nodes);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer resourceId = null;
		try {
			CSRF.validate(req);
			resourceId = Https.getInt(req, "resourceId", R.I);
			Integer parentId = Values.getInt(Https.getInt(req, "parentId", R.I), Value.I);
//			String orgResourceName = Https.getStr(req, "orgResourceName", R.CLEAN, R.RANGE, "{1,60}", "资源名称");
			String resourceName = Https.getStr(req, "resourceName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "资源名称");
			Integer type = Values.getInt(Https.getInt(req, "type", R.I), Resource.MENU);
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Integer required = R.CLEAN;
			if (type.equals(Resource.MENU)) required = R.REQUIRED;
			Integer position = Https.getInt(req, "position", required, R.I);

			required = R.RANGE;
			if (type.equals(Resource.URL)) required = R.REQUIRED;
			String url = Https.getStr(req, "url", required, R.RANGE, "{1,200}", "请求");

			required = R.CLEAN;
			if (type.equals(Resource.ACTION)) required = R.REQUIRED;
			String action = Https.getStr(req, "action", R.CLEAN, required, R.RANGE, "{1,60}", "操作");
			
//			Asserts.notUnique(resourceName, orgResourceName,
//					dao.count(Resource.class, Cnd.where("resourceName", "=", resourceName).and("type", "=", type)),
//					"资源名称已存在");
			
			DateTime now = new DateTime();
			Resource resource = null;
			if (resourceId != null) {
				resource = dao.fetch(Resource.class, resourceId);
				Asserts.isNull(resource, "资源不存在");
			} else {
				resource = new Resource();
				resource.setCreateTime(now.toDate());
			}
			
			resource.setParentId(parentId);
			resource.setResourceName(resourceName);
			resource.setUrl(url);
			resource.setAction(action);
			resource.setPosition(position);
			resource.setType(type);
			resource.setStatus(status);
			resource.setModifyTime(now.toDate());
			
			cache.removeAll(Cache.FQN_RESOURCES);
			
			if (resourceId != null)
				dao.update(resource);
			else
				dao.insert(resource);

			Code.ok(mb, (resourceId == null ? "新建" : "编辑") + "资源成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Resource:add) error: ", e);
			Code.error(mb, (resourceId == null ? "新建" : "编辑") + "资源失败");
		}

		return mb;
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
				dao.update(Resource.class, Chain.make("status", status), Cnd.where("resourceId", "in", arr));
			}
			Code.ok(mb, "设置资源状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Resource:able) error: ", e);
			Code.error(mb, "设置资源状态失败");
		}

		return mb;
	}
}
