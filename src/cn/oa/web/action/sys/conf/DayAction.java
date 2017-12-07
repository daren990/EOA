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
import cn.oa.model.WorkDay;
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
@At(value = "/sys/conf/day")
public class DayAction extends Action {

	public static Log log = Logs.getLog(DayAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/day_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/day/able", token);
		
		String workName = Https.getStr(req, "workName", R.CLEAN, R.RANGE, "{1,20}");
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "w.work_name", "workName", workName);
		
		Page<WorkDay> page = Webs.page(req);
		page = mapper.page(WorkDay.class, page, "WorkDay.count", "WorkDay.index", cri);

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		for (WorkDay day : page.getResult()) {
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getDayId() == null) continue;
				if (o.getDayId().equals(day.getDayId())) {
					orgs.add(o);
				}
			}
			day.setCorps(orgs);
		}
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@At
	@Ok("ftl:sys/conf/day_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer dayId = Https.getInt(req, "dayId", R.REQUIRED, R.I);
		
		WorkDay day = null;
		if (dayId != null) {
			day = dao.fetch(WorkDay.class, dayId);
		}
		if (day == null) day = new WorkDay();

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		if (dayId != null) {
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getDayId() == null) continue;
				if (o.getDayId().equals(day.getDayId())) {
					orgs.add(o);
				}
			}
			day.setCorps(orgs);
		}
		
		req.setAttribute("day", day);
		req.setAttribute("corps", corps);		
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer dayId = null;
		try {
			CSRF.validate(req);
			dayId = Https.getInt(req, "dayId", R.I);
			String workName = Https.getStr(req, "workName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "日排班名称");

			String checkIn_HH = Https.getStr(req, "checkIn_HH", R.REQUIRED, R.HH, "打卡时间");
			String checkIn_mm = Https.getStr(req, "checkIn_mm", R.REQUIRED, R.mm, "打卡时间");
			String checkOut_HH = Https.getStr(req, "checkOut_HH", R.REQUIRED, R.HH, "打卡时间");
			String checkOut_mm = Https.getStr(req, "checkOut_mm", R.REQUIRED, R.mm, "打卡时间");
			
			String restIn_HH = Https.getStr(req, "restIn_HH", R.REQUIRED, R.HH, "午休时间");
			String restIn_mm = Https.getStr(req, "restIn_mm", R.REQUIRED, R.mm, "午休时间");
			String restOut_HH = Https.getStr(req, "restOut_HH", R.REQUIRED, R.HH, "午休时间");
			String restOut_mm = Https.getStr(req, "restOut_mm", R.REQUIRED, R.mm, "午休时间");
			
			String corpIds = Https.getStr(req, "corpIds", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Integer[] arr = Converts.array(corpIds, ",");
			
			WorkDay day = null;
			if (dayId != null) {
				day = dao.fetch(WorkDay.class, dayId);
				Asserts.isNull(day, "workName:日排班不存在");
			} else {
				day = new WorkDay();
			}
			
			day.setWorkName(workName);
			day.setCheckIn(checkIn_HH + ":" + checkIn_mm);
			day.setCheckOut(checkOut_HH + ":" + checkOut_mm);
			day.setRestIn(restIn_HH + ":" + restIn_mm);
			day.setRestOut(restOut_HH + ":" + restOut_mm);
			day.setStatus(status);
			
			transSave(dayId, arr, day);
			
			Code.ok(mb, (dayId == null ? "新建" : "编辑") + "日排班成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Day:add) error: ", e);
			Code.error(mb, (dayId == null ? "新建" : "编辑") + "日排班失败");
		}

		return mb;
	}
	
	private void transSave(final Integer dayId, final Integer[] corpIds, final WorkDay day) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = dayId;
				dao.update(Org.class, Chain.make("dayId", null), Cnd.where("dayId", "=", id));
				if (dayId != null) {
					dao.update(day);
				} else {
					id = dao.insert(day).getDayId();
				}
				if (!Asserts.isEmpty(corpIds))
					dao.update(Org.class, Chain.make("dayId", id), Cnd.where("orgId", "in", corpIds));
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
				dao.update(WorkDay.class, Chain.make("status", status), Cnd.where("dayId", "in", arr));
			}
			Code.ok(mb, "设置日排班状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Day:able) error: ", e);
			Code.error(mb, "设置日排班状态失败");
		}

		return mb;
	}
}
