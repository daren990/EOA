package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("conf_social_security_rule_item")
public class SocialSecurityRuleItem {
	
	@Id
	@Column("id")
	private Integer id;
	@Column("name")
	private String name;

	@Column("rule")
	private String rule;

	@Column("conf_social_security_rule_id")
	private Integer socialSecurityRuleId;

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

	public Integer getSocialSecurityRuleId() {
		return socialSecurityRuleId;
	}

	public void setSocialSecurityRuleId(Integer socialSecurityRuleId) {
		this.socialSecurityRuleId = socialSecurityRuleId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	
	

}
