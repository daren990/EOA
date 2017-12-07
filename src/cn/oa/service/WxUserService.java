package cn.oa.service;

import java.util.Date;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.Filters;

import cn.oa.repository.Mapper;
import cn.oa.consts.Status;
import cn.oa.model.User;
import cn.oa.model.WxUser;
import cn.oa.model.Wxnotice;


@Filters
@IocBean
public class WxUserService
{
	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	
	/**
	 * 开启请假微信通知
	 */
	public boolean leave(){
		Wxnotice wx = dao.fetch(Wxnotice.class,"leave");
		if(wx!=null&&Status.ENABLED==wx.getStatus())
			return true;
		return false;
	}
	/**
	 * 开启外勤微信通知
	 */
	public boolean outWork(){
		Wxnotice wx = dao.fetch(Wxnotice.class,"outwork");
		if(wx!=null&&Status.ENABLED==wx.getStatus())
			return true;
		return false;
	}
	/**
	 * 开启出差微信通知
	 */
	public boolean errand(){
		Wxnotice wx = dao.fetch(Wxnotice.class,"errand");
		if(wx!=null&&Status.ENABLED==wx.getStatus())
			return true;
		return false;
	}
	/**
	 * 开启加班微信通知
	 */
	public boolean overtime(){
		Wxnotice wx = dao.fetch(Wxnotice.class,"overtime");
		if(wx!=null&&Status.ENABLED==wx.getStatus())
			return true;
		return false;
	}
	
	/**
	 * 增加一个微信用户
	 */
	public WxUser saveUser(String username, String telephone, Integer userId)
	{
		WxUser wxUser = new WxUser(username, telephone, userId);
		wxUser.setBindingTime(new Date());
		return dao.fastInsert(wxUser);
	}
	
	/**
	 * 查询绑定
	 */
	public User selectBingding(String oaName)
	{
		WxUser wxUser = dao.fetch(WxUser.class, oaName);
		if(wxUser != null)
		{
			User user = mapper.fetch(User.class, "User.query", Cnd
					.where("u.username", "=", wxUser.getUsername()));
			return user;
		}
		return null;
	}
	
	/**
	 * 检查OA密码
	 */
	public User checkPW(String userName, String password)
	{	
		User user = mapper.fetch(User.class, "User.query", Cnd
				.where("u.username", "=", userName)
				.and("u.password", "=", password));
		
		if(user != null)
		{
			return user;
		}
		return null;
	}
	
}
