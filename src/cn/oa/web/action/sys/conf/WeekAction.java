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
import cn.oa.model.Org;
import cn.oa.model.WorkWeek;
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
@At(value = "/sys/conf/week")
public class WeekAction extends Action {

	public static Log log = Logs.getLog(WeekAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/week_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/week/able", token);
		
		String workName = Https.getStr(req, "workName", R.CLEAN, R.RANGE, "{1,20}");
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "w.work_name", "workName", workName);
		
		Page<WorkWeek> page = Webs.page(req);
		page = mapper.page(WorkWeek.class, page, "WorkWeek.count", "WorkWeek.index", cri);

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		for (WorkWeek week : page.getResult()) {
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getWeekId() == null) continue;
				if (o.getWeekId().equals(week.getWeekId())) {
					orgs.add(o);
				}
			}
			week.setCorps(orgs);
		}
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@At
	@Ok("ftl:sys/conf/week_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer weekId = Https.getInt(req, "weekId", R.REQUIRED, R.I);
		
		WorkWeek week = null;
		if (weekId != null) {
			week = dao.fetch(WorkWeek.class, weekId);
		}
		if (week == null) week = new WorkWeek();

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		if (weekId != null) {
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getWeekId() == null) continue;
				if (o.getWeekId().equals(week.getWeekId())) {
					orgs.add(o);
				}
			}
			week.setCorps(orgs);
		}
		
		req.setAttribute("week", week);
		req.setAttribute("corps", corps);		
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer weekId = null;
		try {
			CSRF.validate(req);
			weekId = Https.getInt(req, "weekId", R.I);
			
			String workName = Https.getStr(req, "workName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "周排班名称");
			String workDays = Https.getStr(req, "workDays", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "工作日");
			String corpIds = Https.getStr(req, "corpIds", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Integer[] arr = Converts.array(corpIds, ",");
			
			WorkWeek week = null;
			if (weekId != null) {
				week = dao.fetch(WorkWeek.class, weekId);
				Asserts.isNull(week, "周排班不存在");
			} else {
				week = new WorkWeek();
			}
			
			week.setWorkName(workName);
			week.setWorkDays(workDays);
			week.setStatus(status);
			
			transSave(weekId, arr, week);
			
			Code.ok(mb, (weekId == null ? "新建" : "编辑") + "周排班成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Week:add) error: ", e);
			Code.error(mb, (weekId == null ? "新建" : "编辑") + "周排班失败");
		}

		return mb;
	}
	
	private void transSave(final Integer weekId, final Integer[] corpIds, final WorkWeek week) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = weekId;
				dao.update(Org.class, Chain.make("weekId", null), Cnd.where("weekId", "=", id));
				if (weekId != null) {
					dao.update(week);
				} else {
					id = dao.insert(week).getWeekId();
				}
				if (!Asserts.isEmpty(corpIds))
					dao.update(Org.class, Chain.make("weekId", id), Cnd.where("orgId", "in", corpIds));
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
				dao.update(WorkWeek.class, Chain.make("status", status), Cnd.where("weekId", "in", arr));
			}
			Code.ok(mb, "设置周排班状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Week:able) error: ", e);
			Code.error(mb, "设置周排班状态失败");
		}

		return mb;
	}
}
