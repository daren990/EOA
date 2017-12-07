package cn.oa.web.action.sys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.encoder.Md5;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Code;
import cn.oa.web.Context;

@IocBean
@At(value = "/sys/password")
public class PasswordAction {

	public static Log log = Logs.getLog(PasswordAction.class);
	
	@Inject
	private Dao dao;
	@Inject
	private Md5 md5;
	
	@GET
	@At
	@Ok("ftl:sys/password_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		User user = Context.getUser();
		req.setAttribute("user", user);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			
			String oldPassword = Https.getStr(req, "oldPassword", R.CLEAN, R.REQUIRED, R.RANGE, R.REGEX, "{6,32}", R.PASSWORD, "oldPassword:原密码");
			String password = Https.getStr(req, "password", R.CLEAN, R.REQUIRED, R.RANGE, R.REGEX, "{6,32}", R.PASSWORD, "password:新密码");
			Https.getStr(req, "passwordConfirm", R.CLEAN, R.REQUIRED, R.RANGE, R.REGEX, R.EQ, "{6,32}", R.PASSWORD, "eq:" + password, "passwordConfirm:重复密码");

			User user = dao.fetch(User.class, Cnd.where("userId", "=", Context.getUser().getUserId()));

			Asserts.notEq(md5.encode(oldPassword), user.getPassword(), "oldPassword:原密码不正确");
			
			user.setPassword(md5.encode(password));
			dao.update(user);

			Code.ok(mb, "修改密码成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Password:add) error: ", e);
			Code.error(mb, "修改密码失败");
		}

		return mb;
	}
}
