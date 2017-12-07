package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("tools_smsInfo")
public class ToolsSmsInfo {
	@Id
	@Column("id")
	private Integer id;

	@Column("phone")
	private String phone;
	
	@Column("text")
	private String text;
	
	@Column("returnCode")
	private Integer returnCode;
	
	@Column("type")
	private Integer type;	//1:验证码
	
	@Column("smsFrom")
	private Integer smsFrom;	//0:盘铭、1:eoa、2:幼儿园
	
	@Column("createTime")
	private Date createTime;
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(Integer returnCode) {
		this.returnCode = returnCode;
	}

	public Integer getSmsFrom() {
		return smsFrom;
	}

	public void setSmsFrom(Integer smsFrom) {
		this.smsFrom = smsFrom;
	}
}
