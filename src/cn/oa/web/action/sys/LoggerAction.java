package cn.oa.web.action.sys;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.model.Logger;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/sys/logger")
public class LoggerAction extends Action {

	@GET
	@At
	@Ok("ftl:sys/logger_page")
	public void page(HttpServletRequest req) {
		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		
		if (Asserts.hasAny(Roles.ADM.getName(), Context.getRoles())) {
			Integer userId = Https.getInt(req, "userId", R.I);
			Cnds.eq(cri, mb, "l.user_id", "userId", userId);
		} else {
			Cnds.eq(cri, mb, "l.user_id", "userId", Context.getUserId());
		}
		Cnds.gte(cri, mb, "l.modify_time", "startTime", startStr);
		Cnds.lte(cri, mb, "l.modify_time", "endTime", endStr);
		cri.getOrderBy().desc("l.modify_time");
		
		Page<Logger> page = Webs.page(req);
		page = mapper.page(Logger.class, page, "Logger.count", "Logger.index", cri);

		if (Asserts.hasAny(Roles.ADM.getName(), Context.getRoles())) {
			List<User> users = dao.query(User.class, Cnd.where("status", "=", Status.ENABLED));
			req.setAttribute("users", users);
		} 
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
}
