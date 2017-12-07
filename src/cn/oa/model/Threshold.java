package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 考勤工资阈值.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("conf_threshold")
@PK({ "orgId" })
public class Threshold {

	@Column("org_id")
	private Integer orgId;
	@Column("over_times")
	private Integer overTimes;
	@Column("deduction_per_over_time")
	private Integer deductionPerOverTime;
	@Column("sick_days")
	private Integer sickDays;
	@Column("over_minute")
	private Integer overMinute;
	@Column("status")
	private Integer status;

	@Readonly
	@Column("corp_name")
	private String corpName;

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public Integer getOverTimes() {
		return overTimes;
	}

	public void setOverTimes(Integer overTimes) {
		this.overTimes = overTimes;
	}

	public Integer getDeductionPerOverTime() {
		return deductionPerOverTime;
	}

	public void setDeductionPerOverTime(Integer deductionPerOverTime) {
		this.deductionPerOverTime = deductionPerOverTime;
	}

	public Integer getSickDays() {
		return sickDays;
	}

	public void setSickDays(Integer sickDays) {
		this.sickDays = sickDays;
	}

	public Integer getOverMinute() {
		return overMinute;
	}

	public void setOverMinute(Integer overMinute) {
		this.overMinute = overMinute;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getCorpName() {
		return corpName;
	}

	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}

}
