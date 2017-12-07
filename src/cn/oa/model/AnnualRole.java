package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 年假规则
 * 
 * @author jiankun.chen
 *  
 */
@Table("conf_annual_role")
public class AnnualRole {
	
	@Id
	@Column("annual_id")
	private Integer annualId;
	@Column("annual_name")
	private String annualName;
	@Column("annual_value")
	private String annualValue;
	@Column("status")
	private Integer status;
	
	private List<Org> corps;
	
	public Integer getAnnualId() {
		return annualId;
	}
	public void setAnnualId(Integer annualId) {
		this.annualId = annualId;
	}
	public String getAnnualName() {
		return annualName;
	}
	public void setAnnualName(String annualName) {
		this.annualName = annualName;
	}
	public String getAnnualValue() {
		return annualValue;
	}
	public void setAnnualValue(String annualValue) {
		this.annualValue = annualValue;
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
	
	public String getCorpIds() {
		return Converts.str(
				Converts.array(Org.class, Integer.class, getCorps(), "orgId"),
				",");
	}

	public String getCorpNames() {
		return Converts.str(
				Converts.array(Org.class, String.class, getCorps(), "orgName"),
				",");
	}
}
