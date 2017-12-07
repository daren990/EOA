package cn.oa.web.action;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentException;
import org.joda.time.DateTime;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.model.Logger;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Cookies;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.view.FreemarkerView;
  
@IocBean
@Filters
public class WxIndexAction extends Action
{
	
	private static String oaName = "";
	private String url = "";
	public static Log log = Logs.getLog(WxIndexAction.class);
	
	@At
	public  Object bingDingOrNo(HttpServletRequest req, HttpServletResponse res) throws  DocumentException, IOException {
		log.debug("bingDingOrNo(HttpServletRequest req, HttpServletResponse res)...");
		MapBean mb = new MapBean();
		User user = wxUserService.selectBingding(oaName);
		url = (String) req.getParameter("url");
		try
		{
			if(Context.getUser()!= null){
				return new ServerRedirectView(url);
			}
			else if(user != null)
			{
				try
				{
					List<Role> roles = roleRepository.find(user.getUserId());
					String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
					Context.setUser(user, roleNames);
					
					String rememberMe = Https.getStr(req, "remember_me", R.CLEAN, R.REGEX, "regex:^true|false$");
					rememberMe = Values.getStr(rememberMe, "false");
					if (rememberMe.equals("true"))
					{
						res.addCookie(Cookies.add(Cookies.USER_ID, 
						Encodes.encodeBase64(Context.getUsername().getBytes())));
					} 
					else
					{
						res.addCookie(Cookies.delete(Cookies.USER_ID));
					}
					
					DateTime now = new DateTime();
					Logger logger = new Logger(user.getUserId(), Webs.ip(req), "浏览器", "/login", "用户登录", now.toDate());
					dao.insert(logger);
					
					req.getSession().setAttribute("mark", "mark");
					return new ServerRedirectView(url);
					//return new ServerRedirectView("/coll/pending/page");

				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
			else
			{
				return new FreemarkerView("wx.login");
			}
			
		}
	
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		return mb;
	}
	
	@POST
	@GET
	@At
	public  Object bingDing(HttpServletRequest req, HttpServletResponse res, String telephone, String userName, String password)
	{
		log.debug("bingDing(HttpServletRequest req, HttpServletResponse res, String telephone, String userName, String password)...");
		User user = wxUserService.selectBingding(userName);
		
		/**
		 * 未绑定则跳去绑定页面，绑定则自动登录
		 */
		if(user == null)
		{
			/**
			 * format password 2 md5 
			 * 验证密码
			 */
			password = md5.encode(password);
			user = wxUserService.checkPW(userName, password);
			
			/**
			 * 密码正确自动登录，错误则返回绑定页面
			 */
			if(user != null)
			{
				try
				{
					wxUserService.saveUser(userName, telephone, user.getUserId());
					
					List<Role> roles = roleRepository.find(user.getUserId());
					String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
					Context.setUser(user, roleNames);		
					String rememberMe = Https.getStr(req, "remember_me", R.CLEAN, R.REGEX, "regex:^true|false$");
					rememberMe = Values.getStr(rememberMe, "false");
					if (rememberMe.equals("true")) 
					{
						res.addCookie(Cookies.add(Cookies.USER_ID, 
						Encodes.encodeBase64(Context.getUsername().getBytes())));
					} 
					else 
					{
						res.addCookie(Cookies.delete(Cookies.USER_ID));
					}
					DateTime now = new DateTime();
					Logger logger = new Logger(user.getUserId(), Webs.ip(req), "浏览器", "/login", "用户登录", now.toDate());
					dao.insert(logger);
					
					req.getSession().setAttribute("mark", "mark");
					return new ServerRedirectView(url);
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
				}
			}
			return new FreemarkerView("wx.login");
		}
		else
		{
			try
			{
				List<Role> roles = roleRepository.find(user.getUserId());
				String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
				Context.setUser(user, roleNames);
				
				String rememberMe = Https.getStr(req, "remember_me", R.CLEAN, R.REGEX, "regex:^true|false$");
				rememberMe = Values.getStr(rememberMe, "false");
				if (rememberMe.equals("true"))
				{
					res.addCookie(Cookies.add(Cookies.USER_ID, 
					Encodes.encodeBase64(Context.getUsername().getBytes())));
				} 
				else
				{
					res.addCookie(Cookies.delete(Cookies.USER_ID));
				}
				
				DateTime now = new DateTime();
				Logger logger = new Logger(user.getUserId(), Webs.ip(req), "浏览器", "/login", "用户登录", now.toDate());
				dao.insert(logger);
				
				req.getSession().setAttribute("mark", "mark");
				return new ServerRedirectView(url);
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
			}
			return new FreemarkerView("wx.login");
		}
	}
	public static void value(String FromUserName){
		log.debug("value(String FromUserName)...");
		oaName = FromUserName;
	}
}
