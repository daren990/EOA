package cn.oa.web.action.sys;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Org;
import cn.oa.model.JobNumber;
import cn.oa.model.ShiftHoliday;
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
@At(value = "/sys/org")
public class OrgAction extends Action {

	public static Log log = Logs.getLog(OrgAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/org_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		System.out.println(token);
		CSRF.generate(req, "/sys/org/nodes", token);
		CSRF.generate(req, "/sys/org/able", token);
//		List<Org> root = mapper.query(Org.class, "Org.query", Cnd.where("parentId", "=", 0));
		List<Org> root = dao.query(Org.class, Cnd.where("parentId", "=", 0));

		req.setAttribute("root", root);
	}

	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer orgId = Https.getInt(req, "orgId", R.REQUIRED, R.I);
			if (orgId != null) {
				List<Org> nodes = dao.query(Org.class, Cnd.where("parentId", "=", orgId));
				mb.put("nodes", nodes);
			}
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Org:nodes) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:sys/org_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		
		Integer orgId = Https.getInt(req, "orgId", R.REQUIRED, R.I);
		
		Org parent = null;
		Org org = null;

		if (orgId != null) {
			org = dao.fetch(Org.class, orgId);
			if (org != null) {
				parent = dao.fetch(Org.class, org.getParentId());
			}
		}
		if (parent == null)
			parent = new Org();
		if (org == null)
			org = new Org();
		
		List<Org> orgs = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED));
		
		req.setAttribute("parent", parent);
		req.setAttribute("org", org);
		req.setAttribute("orgs", orgs);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer orgId = null;
		Integer corpType = null;
		try {
			CSRF.validate(req);

			orgId = Https.getInt(req, "orgId", R.I);
			Integer parentId = Values.getInt(Https.getInt(req, "parentId", R.CLEAN, R.I), Value.I);
//			String orgOrgName = Https.getStr(req, "orgOrgName", R.CLEAN, R.RANGE, "{1,20}", "架构名称");
			String orgName = Https.getStr(req, "orgName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "架构名称");
			String orgDesc = Https.getStr(req, "orgDesc", R.CLEAN, R.RANGE, "{1,60}", "架构描述");
			Integer type = Https.getInt(req, "type", R.REQUIRED, R.I, R.IN, "in [0,1]", "类型");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
//			Asserts.NOT_UNIQUE(orgName, orgOrgName,
//					dao.count(Org.class, Cnd.where("orgName", "=", orgName).and("status", "=", Status.ENABLED)),
//					"架构名称已存在");
			
			DateTime now = new DateTime();
			Org org = null;
			if (orgId != null) {
				org = dao.fetch(Org.class, orgId);
				Asserts.isNull(org, "架构不存在");
				corpType = org.getType();
			} else {
				org = new Org();
				org.setStatus(Status.ENABLED);
				org.setCreateTime(now.toDate());
			}
			if(orgId!=null&&corpType!=null&&corpType==1&&type==0)
				Asserts.notEq(1, 2, "禁止修改子公司类型");

			org.setParentId(parentId);
			org.setOrgName(orgName);
			org.setOrgDesc(orgDesc);
			org.setType(type);
			org.setStatus(status);
			org.setModifyTime(now.toDate());
			
				
			
			//Integer id = 0;
			if (orgId != null){
				//id = orgId;
				dao.update(org);
			}
			else{
				transSave(org,type);
			}
			
		
				
			Code.ok(mb, (orgId == null ? "新建" : "编辑") + "组织架构成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Org:add) error: ", e);
			Code.error(mb, (orgId == null ? "新建" : "编辑") + "组织架构失败");
		}

		return mb;
	}
	
	private void transSave(final Org org,final Integer type) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = dao.insert(org).getOrgId();
				if(type == 1){					
					JobNumber jobNumber = new JobNumber();
					Integer count = dao.count(JobNumber.class);
					jobNumber.setCorpId(id);
					jobNumber.setJobPre(count+10);
					dao.insert(jobNumber);
					}
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
			if (arr != null && arr.length == 1) {
				Org org = dao.fetch(Org.class, arr[0]);
				Asserts.isNull(org, "架构不存在");
				int status = org.getStatus();
				org.setStatus(status == Status.ENABLED ? Status.DISABLED : Status.ENABLED);
				dao.update(org);
			}
			
			Code.ok(mb, "设置组织架构状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Org:able) error: ", e);
			Code.error(mb, "设置组织架构状态失败");
		}

		return mb;
	}
}
