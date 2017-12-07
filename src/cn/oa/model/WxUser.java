package cn.oa.model;


import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;


@Table("w_login")
public class WxUser
{
	@Id(auto=true)
	@Column("id")
	private Integer id;
	@Name
	@Column("userName")
	private String username;
	@Column("telephone")
	private String telephone;
	@Column("bindingTime")
	private Date bindingTime;
	@Column("userId")
	private Integer userId;
	
	
	public WxUser()
	{
		
	}
	
	public WxUser(String username, String telephone, Integer userId)
	{
		this.username = username;
		this.telephone = telephone;
		this.userId = userId;
	}
	
	/* getter && setter */
	public Integer getId() 
	{
		return id;
	}

	public void setId(Integer id) 
	{
		this.id = id;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getTelephone() 
	{
		return telephone;
	}

	public void setTelephone(String telephone)
	{
		this.telephone = telephone;
	}

	public Date getBindingTime() 
	{
		return bindingTime;
	}

	public void setBindingTime(Date bindingTime)
	{
		this.bindingTime = bindingTime;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
}
