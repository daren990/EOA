package cn.oa.web.action.sys.conf;

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
import cn.oa.model.AttendanceThresholdItem;
import cn.oa.model.ConfHoliday;
import cn.oa.model.Org;
import cn.oa.model.WorkMonth;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
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
@At(value = "/sys/conf/confHoliday")
public class ConfHolidayAction extends Action{
	public static Log log = Logs.getLog(ConfHolidayAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/confHoliday_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/confHoliday/days", token);
		CSRF.generate(req, "/sys/conf/confHoliday/able", token);
		
		String holidayName = Https.getStr(req, "holidayName", R.CLEAN, R.RANGE, "{1,20}");
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "h.holiday_name", "holidayName", holidayName);
		cri.getOrderBy().desc("h.year").desc("h.month");
		Page<ConfHoliday> page = Webs.page(req);
		page = mapper.page(ConfHoliday.class, page, "ConfHoliday.count", "ConfHoliday.index", cri);
		for (ConfHoliday holiday : page.getResult()) {
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getHolidayId() == null) continue;
				if (o.getHolidayId().equals(holiday.getHolidayId())) {
					orgs.add(o);
				}
			}
			holiday.setCorps(orgs);
		}
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:sys/conf/confHoliday_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer holidayId = Https.getInt(req, "holidayId", R.REQUIRED, R.I);
		
		ConfHoliday holiday = null;
		if (holidayId != null) {
			holiday = mapper.fetch(ConfHoliday.class, "ConfHoliday.query", Cnd.where("h.holiday_id", "=", holidayId));
			Asserts.isNull(holiday, "配置不存在");
		}
		if (holiday == null) holiday = new ConfHoliday();
		
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		if(holidayId !=null){
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getHolidayId() == null) continue;
				if (o.getHolidayId().equals(holiday.getHolidayId())) {
					orgs.add(o);
				}
			}
			holiday.setCorps(orgs);
		}
		
		DateTime now = new DateTime();
		MapBean mb = new MapBean();
		mb.put("years", now.getYear());
		mb.put("months", now.plusMonths(-1).toString("MM"));
		
		req.setAttribute("holiday", holiday);
		req.setAttribute("corps", corps);
		req.setAttribute("mb", mb);
	}
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer holidayId = null;
		try {
			CSRF.validate(req);
			holidayId = Https.getInt(req, "holidayId", R.I);
			String holidayName = Https.getStr(req, "holidayName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,50}", "配置名称");
			String holidayDays = Https.getStr(req, "holidayDays", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "日期");
			String year = Https.getStr(req, "year", R.REQUIRED, R.yyyy, "年份");
			String month = Https.getStr(req, "month", R.REQUIRED, R.MM, "月份");
			String corpIds = Https.getStr(req, "corpIds", R.REQUIRED,R.CLEAN,R.RANGE, "{1,100}", "公司绑定");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			Integer[] arr = Converts.array(corpIds, ",");
			ConfHoliday holiday = null;
			if(holidayId!=null){
				holiday = dao.fetch(ConfHoliday.class, holidayId);
				Asserts.isNull(holiday, "配置不存在");
			}
			else{
				holiday = new ConfHoliday();
			}
			
			holiday.setHolidayDays(holidayDays);
			holiday.setHolidayName(holidayName);
			holiday.setMonth(month);
			holiday.setYear(year);
			holiday.setStatus(status);
			transSave(holidayId, arr , holiday);
			
			Code.ok(mb, (holidayId == null ? "新建" : "编辑") + "配置成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Holiday:add) error: ", e);
			Code.error(mb, (holidayId == null ? "新建" : "编辑") + "配置失败");
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
				dao.update(ConfHoliday.class, Chain.make("status", status), Cnd.where("holidayId", "in", arr));
			}
			Code.ok(mb, "设置配置状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Month:able) error: ", e);
			Code.error(mb, "设置配置状态失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object days(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String yearStr = Https.getStr(req, "year", R.REQUIRED, R.yyyy, "年份");
			String monthStr = Https.getStr(req, "month", R.REQUIRED, R.MM, "月份");
			
			DateTime month = Calendars.parse(yearStr + monthStr, "yyyyMM");
			
			int maxDay = month.dayOfMonth().withMaximumValue().getDayOfMonth();
			int start = Integer.valueOf(month.dayOfMonth().withMinimumValue().toString("e"));
			int end = Integer.valueOf(month.dayOfMonth().withMaximumValue().toString("e"));
			
			StringBuilder buff = new StringBuilder();
			if (start > 1) {
				for (int i = 0; i < start - 1; i++) buff.append("0").append(",");
			}
			for (int i = 1; i < maxDay + 1; i++) {
				if (i < 10) buff.append("0");
				buff.append(i).append(",");
			}
			
			if (end < 7) {
				for (int i = 0; i < 7 - end; i++) buff.append("0").append(",");
			}
			
			String[] days = Strings.splitIgnoreBlank(buff.toString());
			mb.put("days", days);
			
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Month:days) error: ", e);
		}

		return mb;
	}
	
	private void transSave(final Integer holidayId, final Integer[] arr, final ConfHoliday holiday){
		Trans.exec(new Atom(){

			@Override
			public void run() {
				Integer id = holidayId;
				dao.update(Org.class, Chain.make("holidayId", null), Cnd.where("holidayId", "=", holidayId));
				if(holidayId == null){
					id = dao.insert(holiday).getHolidayId();
				}
				else
					dao.update(holiday);
				
				dao.update(Org.class, Chain.make("holidayId", id), Cnd.where("orgId", "in", arr));
			}
		});
	}
	
}
