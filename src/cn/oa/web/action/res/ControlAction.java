package cn.oa.web.action.res;

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

import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Control;
import cn.oa.model.User;
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

@IocBean(name = "res.control")
@At(value = "/res/control")
public class ControlAction extends Action {

	public static Log log = Logs.getLog(ControlAction.class);
	
	@GET
	@At
	@Ok("ftl:res/control_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/control/able", token);

		String controlName = Https.getStr(req, "controlName", R.CLEAN, R.RANGE, "{1,20}");
		Integer userId = Https.getInt(req, "userId", R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "c.control_name", "controlName", controlName);
		if (userId != null) {
			List<Control> controls = mapper.query(Control.class, "ControlUser.distinct", Cnd.where("cu.user_id", "=", userId));
			if (!Asserts.isEmpty(controls)) {
				Integer[] controlIds = Converts.array(Control.class, Integer.class, controls, "controlId");
				cri.where().and("c.control_id", "in", controlIds);
			} else {
				cri.where().and("c.control_id", "=", Value.I);
			}
			Webs.put(mb, "userId", userId);
		}
		cri.getOrderBy().desc("c.modify_time");
		
		Page<Control> page = Webs.page(req);
		page = mapper.page(Control.class, page, "Control.count", "Control.index", cri);

		List<User> users = mapper.query(User.class, "ControlUser.join", Cnd.where("u.status", "=", Status.ENABLED));
		
		for (Control c : page.getResult()) {
			List<User> userList = new ArrayList<User>();
			for (User u : users) {
				if (c.getControlId().equals(u.getControlId())) {
					userList.add(u);
				}
			}
			c.setUsers(userList);
		}

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
		req.setAttribute("users", users);
	}

	@GET
	@At
	@Ok("ftl:res/control_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer controlId = Https.getInt(req, "controlId", R.REQUIRED, R.I);
		Control control = null;
		if (controlId != null) {
			control = dao.fetch(Control.class, controlId);
			if (control != null) {
				dao.fetchLinks(control, "users");
			}
		}
		if (control == null)
			control = new Control();

		List<User> users = dao.query(User.class, Cnd.where("status", "=", Status.ENABLED));

		req.setAttribute("control", control);
		req.setAttribute("users", users);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer controlId = null;
		try {
			CSRF.validate(req);
			
			controlId = Https.getInt(req, "controlId", R.I);
			String controlName = Https.getStr(req, "controlName", R.CLEAN, R.RANGE, "{1,20}", "门禁名称");
			String userIds = Https.getStr(req, "userIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "所属用户");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			DateTime now = new DateTime();
			Control control = null;
			
			if (controlId != null) {
				control = dao.fetch(Control.class, controlId);
				Asserts.isNull(control, "门禁不存在");
			} else {
				control = new Control();
				control.setCreateTime(now.toDate());
			}

			control.setControlName(controlName);
			control.setStatus(status);
			control.setModifyTime(now.toDate());

			List<User> users = new ArrayList<User>();
			for (Integer id : Converts.array(userIds, ",")) {
				User u = new User();
				u.setUserId(id);
				users.add(u);
			}
			control.setUsers(users);
			transSave(controlId, control);
			
			Code.ok(mb, (controlId == null ? "新建" : "编辑") + "门禁成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Control:add) error: ", e);
			Code.error(mb, (controlId == null ? "新建" : "编辑") + "门禁失败");
		}

		return mb;
	}
	
	private void transSave(final Integer controlId, final Control control) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (controlId != null) {
					dao.update(control);
					dao.clearLinks(control, "users");
				} else {
					dao.insert(control);
				}
				dao.insertRelation(control, "users");
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
			
			if (arr != null && arr.length == 1) {
				dao.update(Control.class, Chain.make("status", status), Cnd.where("controlId", "in", arr));
			}
			Code.ok(mb, "设置门禁状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Control:able) error: ", e);
			Code.error(mb, "设置门禁状态失败");
		}

		return mb;
	}
}
