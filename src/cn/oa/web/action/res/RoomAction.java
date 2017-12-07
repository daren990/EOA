package cn.oa.web.action.res;

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

import cn.oa.model.Room;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/res/room")
public class RoomAction extends Action {

	public static Log log = Logs.getLog(RoomAction.class);
	
	@GET
	@At
	@Ok("ftl:res/room_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/room/able", token);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.getOrderBy().desc("r.modify_time");
		
		Page<Room> page = Webs.page(req);
		page = mapper.page(Room.class, page, "Room.count", "Room.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@At
	@Ok("ftl:res/room_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer roomId = Https.getInt(req, "roomId", R.REQUIRED, R.I);
		
		Room room = null;
		if (roomId != null) {
			room = dao.fetch(Room.class, roomId);
		}
		if (room == null) room = new Room();

		req.setAttribute("room", room);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer roomId = null;
		try {
			CSRF.validate(req);
			roomId = Https.getInt(req, "roomId", R.I);
			String roomName = Https.getStr(req, "roomName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "会议室名称");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Room room = null;
			DateTime now = new DateTime();
			if (roomId != null) {
				room = dao.fetch(Room.class, roomId);
				Asserts.isNull(room, "会议室不存在");
			} else {
				room = new Room();
				room.setCreateTime(now.toDate());
			}
			
			room.setRoomName(roomName);
			room.setStatus(status);
			room.setModifyTime(now.toDate());
			if (roomId != null)
				dao.update(room);
			else
				dao.insert(room);
			
			Code.ok(mb, (roomId == null ? "新建" : "编辑") + "会议室成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Room:add) error: ", e);
			Code.error(mb, (roomId == null ? "新建" : "编辑") + "会议室失败");
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
			
			if (arr != null && arr.length == 1) {
				dao.update(Room.class, Chain.make("status", status), Cnd.where("roomId", "in", arr));
			}
			Code.ok(mb, "设置会议室状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Room:able) error: ", e);
			Code.error(mb, "设置会议室状态失败");
		}

		return mb;
	}
}
