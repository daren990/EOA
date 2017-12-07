package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("conf_tax_rule_item")
public class TaxRuleItem {
	@Id
	@Column("id")
	private Integer id;
	@Column("name")
	private String name;

	@Column("rule")
	private String rule;

	@Column("conf_tax_rule_id")
	private Integer taxRuleId;

	@Column("status")
	private Integer status;
	
	
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
	public Integer getTaxRuleId() {
		return taxRuleId;
	}
	public void setTaxRuleId(Integer taxRuleId) {
		this.taxRuleId = taxRuleId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}

}
