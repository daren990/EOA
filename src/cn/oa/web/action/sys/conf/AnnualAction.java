package cn.oa.web.action.sys.conf;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import cn.oa.model.AnnualRole;
import cn.oa.model.Org;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/sys/conf/annual")
public class AnnualAction extends Action
{
	public static Log log = Logs.getLog(AnnualAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/annual_page")
	public void page(HttpServletRequest req){
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/annual/able", token);
		
		String annualName = Https.getStr(req, "annualName", R.CLEAN, R.RANGE, "{1,20}");
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "r.annual_name", "annualName", annualName);
		
		Page<AnnualRole> page = Webs.page(req);
		page = mapper.page(AnnualRole.class, page, "AnnualRole.count", "AnnualRole.index", cri);

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		for (AnnualRole role : page.getResult()) {
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getAnnualId() == null) continue;
				if (o.getAnnualId().equals(role.getAnnualId())) {
					orgs.add(o);
				}
			}
			role.setCorps(orgs);
		}
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:sys/conf/annual_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer annualId = Https.getInt(req, "annualId", R.REQUIRED, R.I);
		
		AnnualRole role = null;
		if (annualId != null) {
			role = dao.fetch(AnnualRole.class, annualId);
		}
		if (role == null) role = new AnnualRole();

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		if (annualId != null) {
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getAnnualId() == null) continue;
				if (o.getAnnualId().equals(role.getAnnualId())) {
					orgs.add(o);
				}
			}
			role.setCorps(orgs);
		}
		
		req.setAttribute("role", role);
		req.setAttribute("corps", corps);		
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer annualId = null;
		try {
			CSRF.validate(req);
			annualId = Https.getInt(req, "annualId", R.I);
			String annualName = Https.getStr(req, "annualName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "年假规则名称");
			String annualValue = Https.getStr(req, "annualValue", R.REQUIRED, "年假规则集合");
			String corpIds = Https.getStr(req, "corpIds", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Integer[] arr = Converts.array(corpIds, ",");
			
			AnnualRole role = null;
			if (annualId != null) {
				role = dao.fetch(AnnualRole.class, annualId);
				Asserts.isNull(role, "annualName:年假规则不存在");
			} else {
				role = new AnnualRole();
			}
			
			role.setAnnualName(annualName);
			role.setAnnualValue(annualValue);
			role.setStatus(status);
			
			transSave(annualId, arr, role);
			
			Code.ok(mb, (annualId == null ? "新建" : "编辑") + "年假规则成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(AnnualRole:add) error: ", e);
			Code.error(mb, (annualId == null ? "新建" : "编辑") + "年假规则失败");
		}

		return mb;
	}
	
	private void transSave(final Integer annualId, final Integer[] corpIds, final AnnualRole role) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = annualId;
				dao.update(Org.class, Chain.make("annualId", null), Cnd.where("annualId", "=", id));
				if (annualId != null) {
					dao.update(role);
				} else {
					id = dao.insert(role).getAnnualId();
				}
				if (!Asserts.isEmpty(corpIds))
					dao.update(Org.class, Chain.make("annualId", id), Cnd.where("orgId", "in", corpIds));
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
				dao.update(AnnualRole.class, Chain.make("status", status), Cnd.where("annualId", "in", arr));
			}
			Code.ok(mb, "设置年假规则状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Day:able) error: ", e);
			Code.error(mb, "设置年假规则状态失败");
		}

		return mb;
	}
}

