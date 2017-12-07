package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;

import org.nutz.dao.entity.annotation.Table;
@Table("ab_address_menu")
public class AddressMenu {
	@Id
	@Column("group_id")
	private Integer groupid;
	@Column("user_id")
	private Integer userid;
	@Column("name")
	private String name;
	@Column("sort")
	private Integer sort;
	public Integer getGroupid() {
		return groupid;
	}
	public void setGroupid(Integer groupid) {
		this.groupid = groupid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public Integer getSort() {
		return sort;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}


	
}
