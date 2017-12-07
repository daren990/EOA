package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("conf_salary_rule_item")
public class SalaryRuleItem {
	@Id
	@Column("id")
	private Integer id;
	@Column("name")
	private String name;

	@Column("rule")
	private String rule;
	@Column("type")
	private String type;
	@Column("conf_salary_rule_id")
	private Integer salaryRuleId;
	@Column("status")
	private Integer status;
	
	
	public Integer getSalaryRuleId() {
		return salaryRuleId;
	}
	public void setSalaryRuleId(Integer salaryRuleId) {
		this.salaryRuleId = salaryRuleId;
	}
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

	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
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
	@Override
	public String toString() {
		return "SalaryRuleItem [id=" + id + ", name=" + name + ",  rule=" + rule + ", type=" + type + ", salaryRuleId="
				+ salaryRuleId + ", status=" + status + "]";
	}

	
	
}
