package cn.oa.web.action.res.meet;

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

import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Meet;
import cn.oa.model.Room;
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
@At(value = "/res/meet/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);
	
	@GET
	@At
	@Ok("ftl:res/meet/approve_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/meet/approve/completed", token);

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
	@Ok("ftl:res/meet/approve_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer meetId = Https.getInt(req, "meetId", R.REQUIRED, R.I, "会议ID");
		
		Meet meet = mapper.fetch(Meet.class, "Meet.query", Cnd
				.where("m.status", "=", Status.ENABLED)
				.and("m.meet_id", "=", meetId)
				.and("m.cron", "is", null));
		Asserts.isNull(meet, "会议不存在");
		
		List<Room> rooms = dao.query(Room.class, null);
		
		req.setAttribute("meet", meet);
		req.setAttribute("rooms", rooms);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer meetId = Https.getInt(req, "meetId", R.REQUIRED, R.I, "会议ID");
			Integer completed = Https.getInt(req, "completed", R.CLEAN, R.REQUIRED, R.IN, "in [0,1]", "状态");

			Meet meet= dao.fetch(Meet.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("meetId", "=", meetId)
					.and("cron", "is", null));
			Asserts.isNull(meet, "会议不存在");

			meet.setCompleted(completed);
			dao.update(meet);
			
			Code.ok(mb, "设置会议成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "设置会议失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object completed(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				dao.update(Meet.class, Chain
						.make("completed", Value.T), Cnd
						.where("status", "=", Status.ENABLED)
						.and("meetId", "in", arr)
						.and("cron", "is", null));
			}
			Code.ok(mb, "结束会议成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:completed) error: ", e);
			Code.error(mb, "结束会议失败");
		}

		return mb;
	}
}
