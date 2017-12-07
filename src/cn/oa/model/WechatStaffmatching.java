package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;


@Table("conf_wechat_staffmatching")
public class WechatStaffmatching {
	@Id
	@Column("userId")
	private Integer userId;
	
	@Column("oa_name")
	private String oaName;
	
	@Column("wechat_name")
	private String wechatName;

	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getOaName() {
		return oaName;
	}
	public void setOaName(String oaName) {
		this.oaName = oaName;
	}
	public String getWechatName() {
		return wechatName;
	}
	public void setWechatName(String wechatName) {
		this.wechatName = wechatName;
	}
	
	
}
