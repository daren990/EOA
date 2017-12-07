package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;
/**
 * 微信通知.
 * 
 * @author qiumingxie
 */
@Table("wx_notice")
public class Wxnotice {
	@Name
	@Column("type")
	private String type;
	
	@Column("status")
	private Integer status;
	
	@Column("name")
	private String name;
	
	public Wxnotice(){}
	public Wxnotice(String type,String name,Integer status){
		this.type = type;
		this.name = name;
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
