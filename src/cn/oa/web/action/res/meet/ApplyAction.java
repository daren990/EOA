package cn.oa.web.action.res.meet;

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

import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Meet;
import cn.oa.model.Room;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.helper.Compares;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/res/meet/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);
	
	@GET
	@At
	@Ok("ftl:res/meet/apply_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/meet/apply/del", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer cron = Https.getInt(req, "cron", R.I, R.IN, "in [0,1]");
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where().and("m.status", "=", Status.ENABLED);
		Cnds.gte(cri, mb, "m.start_time", "startTime", startStr);
		Cnds.lte(cri, mb, "m.start_time", "endTime", endStr);
		if (cron != null) {
			if (cron.equals(Value.F)) cri.where().andIsNull("cron");
			else cri.where().andNotIsNull("cron");
			mb.put("cron", cron);
		}
		
		Page<Meet> page = Webs.page(req);
		page = mapper.page(Meet.class, page, "Meet.count", "Meet.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:res/meet/apply_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer meetId = Https.getInt(req, "meetId", R.REQUIRED, R.I);
		
		Meet meet = null;
		if (meetId != null) {
			meet = mapper.fetch(Meet.class, "Meet.query", Cnd
					.where("m.status", "=", Status.ENABLED)
					.and("m.user_id", "=", Context.getUserId())
					.and("m.meet_id", "=", meetId));
		}
		if (meet == null) meet = new Meet();

		List<Room> rooms = dao.query(Room.class, null);
		
		req.setAttribute("meet", meet);
		req.setAttribute("rooms", rooms);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer meetId = null;
		try {
			CSRF.validate(req);
			meetId = Https.getInt(req, "meetId", R.I);
			Integer roomId = Https.getInt(req, "roomId", R.CLEAN, R.REQUIRED, R.I, "会议");
			
			String phone = Https.getStr(req, "phone", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "联系电话");

			String meet_yyyyMMdd = Https.getStr(req, "meet_yyyyMMdd", R.CLEAN, R.REQUIRED, R.D, "会议日期");
			String start_HH = Https.getStr(req, "start_HH", R.REQUIRED, R.HH, "开始时间");
			String start_mm = Https.getStr(req, "start_mm", R.REQUIRED, R.mm, "开始时间");
			String end_HH = Https.getStr(req, "end_HH", R.REQUIRED, R.HH, "结束时间");
			String end_mm = Https.getStr(req, "end_mm", R.REQUIRED, R.mm, "结束时间");
			String content = Https.getStr(req, "content", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "用途");
			
			String plan = Https.getStr(req, "plan", R.REGEX, "regex:^day|week|month$:不是合法值", "计划");
			String cron = null;
			
			if (Strings.isNotBlank(plan)) {
				if (plan.equals("day")) {
					cron = "* * *";
				} else if (plan.equals("week")){
					Integer plan_e = Https.getInt(req, "plan_e", R.REQUIRED, R.I, "周");
					cron = "* " + plan_e + " *";
				} else if (plan.equals("month")) {
					Integer plan_dd = Https.getInt(req, "plan_dd", R.REQUIRED, R.I, "日期");
					cron = plan_dd + " * *";
				}
			}
			
			Integer status = Https.getInt(req, "status", R.CLEAN, R.REQUIRED, R.IN, "in [0,1]", "状态");

			String startStr = meet_yyyyMMdd + " " + start_HH + ":" + start_mm;
			String endStr = meet_yyyyMMdd + " " + end_HH + ":" + end_mm;
			
			DateTime start = Calendars.parse(startStr, Calendars.DATE_TIME);
			DateTime end = Calendars.parse(endStr, Calendars.DATE_TIME);

			DateTime now = new DateTime();
			if (start.isAfter(end))
				throw new Errors("开始时间不能大于结束时间");
			if (start.isBefore(now))
				throw new Errors("开始时间不能小于当前时间");

			Criteria cri = Cnd
					.where("m.status", "=", Status.ENABLED)
					.and("m.room_id", "=", roomId)
					.and("m.start_time", ">=", meet_yyyyMMdd + " 00:00:00")
					.and("m.end_time", "<=", meet_yyyyMMdd + " 23:59:59");
			if (meetId != null)
				cri.where().and("m.meet_id", "!=", meetId);
			
			List<Meet> meets = mapper.query(Meet.class, "Meet.query", cri);
			
			List<Meet> plans = mapper.query(Meet.class, "Meet.query", Cnd
					.where("m.status", "=", Status.ENABLED)
					.and("m.cron", "is not", null));
			
			for (Meet e : meets) {
				if (Compares.in(startStr, endStr, e.getStartTime(), e.getEndTime())) throw new Errors("禁止重复预订会议");
			}
			
			plan(meet_yyyyMMdd, startStr, endStr, plans);

			Meet meet = null;
			if (meetId != null) {
				meet = dao.fetch(Meet.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("meetId", "=", meetId));
				Asserts.isNull(meet, "会议不存在");
			} else {
				meet = new Meet();
				meet.setUserId(Context.getUserId());
				meet.setCompleted(Value.F);
				meet.setCreateTime(now.toDate());
			}
			
			meet.setRoomId(roomId);
			meet.setPhone(phone);
			meet.setStartTime(Calendars.parse(startStr, Calendars.DATE_TIME).toDate());
			meet.setEndTime(Calendars.parse(endStr, Calendars.DATE_TIME).toDate());
			meet.setContent(content);
			meet.setCron(cron);
			meet.setStatus(status);
			meet.setModifyTime(now.toDate());
			
			if (meetId != null)
				dao.update(meet);
			else
				dao.insert(meet);
			
			Code.ok(mb, (meetId == null ? "新建" : "编辑") + "会议成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (meetId == null ? "新建" : "编辑") + "会议失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object del(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				dao.update(Meet.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("meetId", "in", arr));
			}
			Code.ok(mb, "取消会议成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "取消会议失败");
		}

		return mb;
	}
	
	public void plan(String meet_yyyyMMdd, String startStr, String endStr, List<Meet> plans) {
		for (Meet e : plans) {
			if (Strings.isBlank(e.getCron())) continue;
			
			String[] arr = Strings.splitIgnoreBlank(e.getCron(), " ");
			if (arr.length != 3) continue;

			String day = arr[0];
			String week = arr[1];
			String month = arr[2];
			DateTime now = Calendars.parse(meet_yyyyMMdd, Calendars.DATE);
			
			if (day.equals("*") && !week.equals("*") && month.equals("*")) {
				if (!now.toString("e").equals(week)) continue;
			} else if (!day.equals("*") && week.equals("*") && month.equals("*")) {
				if (!now.toString("d").equals(day)) continue;
			}
			String start_HHmm = new DateTime(e.getStartTime()).toString("HH:mm");
			String end_HHmm = new DateTime(e.getEndTime()).toString("HH:mm");
			DateTime start = Calendars.parse(meet_yyyyMMdd + " " + start_HHmm, Calendars.DATE_TIME);
			DateTime end = Calendars.parse(meet_yyyyMMdd + " " + end_HHmm, Calendars.DATE_TIME);
			
			if (Compares.in(startStr, endStr, start.toDate(), end.toDate())) {
				throw new Errors("禁止重复预订会议");
			}
		}
	}
}
