package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("conf_social_security_rule")
public class SocialSecurityRule {
	
	@Id
	@Column("id")
	private Integer id;
	
	@Column("name")
	private String name;
	
	@Column("corpId")
	private Integer corpId;
	
	@Column("rule")
	private String rule;
	
	@Column("status")
	private Integer status;
	
	private List<Org> corps;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCorpId() {
		return corpId;
	}

	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		return "SocialSecurityRule [id=" + id + ", corpId=" + corpId
				+ ", rule=" + rule + ", status=" + status + "]";
	}
	
	
}
