package cn.oa.web.action;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.view.ServerRedirectView;


import cn.oa.consts.Status;
import cn.oa.model.Logger;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cookies;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;


@IocBean
@Filters
public class LoginAction extends Action {

	private static final Log log = Logs.getLog(LoginAction.class);
	
	
	@At
	@Ok("ftl:login")
	public void login(HttpServletRequest req) {
		log.debug("login(HttpServletRequest req)...");
		CSRF.generate(req);
		Cookie[] cookies = req.getCookies();
		if (cookies == null) {
			return;
		}
		for (Cookie cookie : cookies) {
			String uid = Cookies.get(cookie, Cookies.USER_ID);
			if (uid != null) {
				req.setAttribute("uid", new String(Encodes.decodeBase64(uid)));
				req.setAttribute("remember_me", true);
				break;
			}
		}
	}

	public Object loginUtil(HttpServletRequest req, HttpServletResponse res) {
		log.debug("loginUtil(HttpServletRequest req, HttpServletResponse res)");
		MapBean mb = new MapBean();
		try {
			String username = Https.getStr(req, "username", R.CLEAN, R.REQUIRED, R.RANGE, "{4,60}", "username:用户名");
			String password = Https.getStr(req, "password", R.CLEAN, R.REQUIRED, R.RANGE, "{6,20}", "password:密码");
			
			password = md5.encode(password);
			
			User user = mapper.fetch(User.class, "User.query", Cnd
					.where("u.username", "=", username)
					.and("u.status", "=", Status.ENABLED));
			
			Asserts.isNull(user, "username:用户名错误");
			Asserts.notEq(password, user.getPassword(), "password:密码错误");

			// handler
			List<Role> roles = roleRepository.find(user.getUserId());
			String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
			Context.setUser(user, roleNames);
			
			String rememberMe = Https.getStr(req, "remember_me", R.CLEAN, R.REGEX, "regex:^true|false$");
			rememberMe = Values.getStr(rememberMe, "false");
			if (rememberMe.equals("true")) {
				res.addCookie(Cookies.add(Cookies.USER_ID, 
						Encodes.encodeBase64(Context.getUsername().getBytes())));
				res.addCookie(Cookies.add(Cookies.PASSWD, 
						Encodes.encodeBase64(password.getBytes())));
			} else {
				res.addCookie(Cookies.delete(Cookies.USER_ID));
				res.addCookie(Cookies.delete(Cookies.PASSWD));
			}
			
//			DateTime now = new DateTime();
//			Logger logger = new Logger(user.getUserId(), Webs.ip(req), Webs.browser(req), "/login", "用户登录", now.toDate());
//			dao.insert(logger);
			
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());	
		}
		
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object login(HttpServletRequest req, HttpServletResponse res) {
		log.debug("login(HttpServletRequest req, HttpServletResponse res)");
//		CSRF.validate(req);
		MapBean mb = (MapBean) loginUtil(req, res);
		return mb;
		
	}
	
	@POST
	@At
	@Ok("json")
	public Object wxlogin(HttpServletRequest req, HttpServletResponse res) {
		log.debug("wxlogin(HttpServletRequest req, HttpServletResponse res)");
//		CSRF.validate(req);
		MapBean mb = (MapBean) loginUtil(req, res);
		return mb;
	}
	
	//旧的跳转到登录页面的方法
	@At
	@Ok("ftl:wxlogin")
	public Object wxlogin2(HttpServletRequest req) {
		log.debug("wxlogin(HttpServletRequest req)");
//		CSRF.generate(req);
		Cookie[] cookies = req.getCookies();
		String url = (String) req.getParameter("url");
		if(Context.getUser()!= null){
			return new ServerRedirectView(url);
		}
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String uid = Cookies.get(cookie, Cookies.USER_ID);
				if (uid != null) {
					req.setAttribute("uid", new String(Encodes.decodeBase64(uid)));
					break;
				}
			}
		}
		req.setAttribute("url", url);
		req.setAttribute("remember_me", true);
		return null;
	}

	@At
	@Ok(">>:/login")
	public void logout(HttpServletRequest req, HttpSession session) {
		log.debug("logout(HttpServletRequest req, HttpSession session)");
		try {
			DateTime now = new DateTime();
			Logger logger = new Logger(Context.getUserId(), Webs.ip(req), Webs.browser(req), "/logout", "用户退出", now.toDate());
			dao.insert(logger);
		} catch (Exception e) {
			log.error("(Logout) error: ", e);
		}
		session.invalidate();
	}
	
	//新的跳转到登录页面的方法，新增企业号自动登录
	@At
	@Ok("ftl:wxlogin2")
	@GET
	public Object wxlogin(HttpServletRequest req) {
		log.debug("wxlogin2(HttpServletRequest req)");
		MapBean mb = new MapBean();
		try{
		User user = null;
		Cookie[] cookies = req.getCookies();
		String url = (String) req.getParameter("url");
		String error = (String) req.getParameter("error");
		if (url==null||url.equals("")){
			req.setAttribute("url", "");
			req.setAttribute("error", error);
			return null;
		}

		if(Context.getUser()!= null){
			return new ServerRedirectView(url);
			
		}else{
//			url = (String) req.getAttribute("url");
//			String userName = req.getAttribute("FromUserName").toString();
//			String value = (String) Validator.validate(userName, R.CLEAN, R.REQUIRED, R.RANGE, "{4,60}", "username:用户名");
			
			String userName = null;
			String password = null;
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					String uid = Cookies.get(cookie, Cookies.USER_ID);
					if (uid != null) {
						userName = new String(Encodes.decodeBase64(uid));
						//在跳转到登录页面的时候，自动填充用户名
						req.setAttribute("uid", userName);
					}
					
					String pwd = Cookies.get(cookie, Cookies.PASSWD);
					if (pwd != null) {
						password = new String(Encodes.decodeBase64(pwd));
					}
					
					if(userName != null && password != null){
						break;
					}
				}
			}
			
			if(userName != null && password != null){
				user = mapper.fetch(User.class, "User.query", Cnd
						.where("u.username", "=", userName)
						.and("u.password", "=", password)
						.and("u.status", "=", Status.ENABLED));
			}
			
			if(user !=null){
				List<Role> roles = roleRepository.find(user.getUserId());
				String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
				Context.setUser(user, roleNames);
				
//				DateTime now = new DateTime();
//				Logger logger = new Logger(user.getUserId(), Webs.ip(req), Webs.browser(req), "/login", "用户登录", now.toDate());
//				dao.insert(logger);
				return new ServerRedirectView(url);
			}
		}
		
//		if (cookies != null) {
//			for (Cookie cookie : cookies) {
//				String uid = Cookies.get(cookie, Cookies.USER_ID);
//				if (uid != null) {
//					req.setAttribute("uid", new String(Encodes.decodeBase64(uid)));
//					break;
//				}
//			}
//		}
		
		req.setAttribute("url", url);
		req.setAttribute("error", error);
		Code.ok(mb, "");
		
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		
		return mb;
	}
	
}
