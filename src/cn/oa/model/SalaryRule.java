package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("conf_salary_rule")
public class SalaryRule {
	
	@Id
	@Column("id")
	private Integer id;
	@Column("name")
	private String name;
	@Column("org_ids")
	private String orgIds;
	@Column("status")
	private Integer status;
	
	private List<Org> corps;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrgIds() {
		return orgIds;
	}
	public void setOrgIds(String orgIds) {
		this.orgIds = orgIds;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public List<Org> getCorps() {
		return corps;
	}
	public void setCorps(List<Org> corps) {
		this.corps = corps;
	}
	public String getCorpNames() {
		return Converts.str(
				Converts.array(Org.class, String.class, getCorps(), "orgName"),
				",");
	}
	
	@Override
	public String toString() {
		return "SalaryRule [id=" + id + ", name=" + name + ", orgIds=" + orgIds
				+ ", status=" + status + "]";
	}
	
	
}
