package cn.oa.web.action.edu.course.conf;

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
import cn.oa.model.EduClassify;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.action.Action;

/**
 * 学科配置
 * @author Ash
 */
@IocBean
@At("/edu/course/conf/sub")
public class SubAction extends Action
{
	public static Log log = Logs.getLog(SubAction.class);
	
	@GET
	@POST
	@At
	@Ok("ftl:edu/course/conf/sub_page")
	public void page(HttpServletRequest req) {
		List<EduClassify> nodes = dao.query(EduClassify.class, Cnd.where("type", "=", "科目").asc("id"));
		req.setAttribute("nodes", nodes);
	}
	
	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer subId = Https.getInt(req, "id", R.REQUIRED, R.I, "学科id");
			System.out.println("\n\n\n\n\n" + subId + "\n\n\n\n\n");
			List<EduClassify> nodes = dao.query(EduClassify.class, 
					Cnd.where("parent_id", "=", subId)
					   .and("type","=","学科")
					   .asc("id"));
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
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			Integer id = Https.getInt(req, "id", R.REQUIRED, R.I, "ID");
			System.out.println("\n\n\n\n\n" + id + "\n\n\n\n\n");
			List<EduClassify> actors = dao.query(EduClassify.class, Cnd.where("id", "=", id));
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
	@Ok("ftl:edu/course/conf/sub_add")
	public void add(HttpServletRequest req) {
		Integer id = Https.getInt(req, "id", R.REQUIRED, R.I);
		EduClassify parent = null;
		EduClassify subject = null;
				
		if (id != null) {
			subject = dao.fetch(EduClassify.class, id);
			if (subject != null) {
				parent = dao.fetch(EduClassify.class, subject.getParentId());
			}
		}

		if (parent == null)
			parent = new EduClassify();
		if (subject == null)
			subject = new EduClassify();

		List<EduClassify> nodes = dao.query(EduClassify.class, Cnd.where("status", "=", Status.ENABLED));

		if(parent.getId() != null)
		{
			if(parent.getId() == 5)
			{
				parent.setId(null);
				parent.setName("根目录");
			}
		}

		req.setAttribute("parent", parent);
		req.setAttribute("subject", subject);
		req.setAttribute("nodes", nodes);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer id = null;
		EduClassify course = null;
		try {
			id = Https.getInt(req, "id", R.I);
			Integer parentId = Values.getInt(Https.getInt(req, "parentId", R.I), Value.I);
//			String orgResourceName = Https.getStr(req, "orgResourceName", R.CLEAN, R.RANGE, "{1,60}", "资源名称");
			String name = Https.getStr(req, "name", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "名称");
			String describe = Https.getStr(req, "desc");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
//			Asserts.notUnique(resourceName, orgResourceName,
//					dao.count(Resource.class, Cnd.where("resourceName", "=", resourceName).and("type", "=", type)),
//					"资源名称已存在");
			
			DateTime now = new DateTime();
			if (id != null) {
				course = dao.fetch(EduClassify.class, id);
				Asserts.isNull(course, "科目/学科不存在");
			} else {
				course = new EduClassify();
			}
			
			if(parentId != 0)
			{
				course.setParentId(parentId);
			}
			else
			{
				course.setParentId(5);
			}
			course.setName(name);
			course.setDescribe(describe);
			if(parentId != null && parentId != 0)
			{
				course.setType("学科");
			}
			else
			{
				course.setType("科目");
			}
			course.setStatus(status);
						
			cache.removeAll(Cache.FQN_RESOURCES);
			
			if (id != null)
			{
				System.out.println("Loading in.....");
				dao.update(course);
				System.out.println("Break out......");
			}
			else
			{
				System.out.println("Loading in......");
				dao.insert(course);
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
			
			if (arr != null && arr.length > 0) {
				cache.removeAll(Cache.FQN_RESOURCES);
				dao.update(EduClassify.class, Chain.make("status", status), Cnd.where("id", "in", arr));
			}
			Code.ok(mb, "设置科目/学科状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Subject:able) error: ", e);
			Code.error(mb, "设置科目/学科状态失败");
		}

		return mb;
	}

}
