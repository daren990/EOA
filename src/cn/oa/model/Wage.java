package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 工资设置.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("hrm_wage")
@PK({ "userId" })
public class Wage {
	@Column("user_id")
	private Integer userId;
	@Column("standard_salary")
	private Integer standardSalary;
	@Column("post_salary")
	private Integer postSalary;
	@Column("perform_salary")
	private Integer performSalary;
	@Column("reward_salary")
	private Integer rewardSalary;
	@Column("service_award")
	private Integer serviceAward;
	@Column("communication_allowance")
	private Integer communicationAllowance;
	@Column("oil_allowance")
	private Integer oilAllowance;
	@Column("heating_allowance")
	private Integer heatingAllowance;
	@Column("meal_allowance")
	private Integer mealAllowance;
	@Column("overtime_allowance")
	private Integer overtimeAllowance;
	@Column("maternity_insurance")
	private Integer maternityInsurance;
	@Column("housing_subsidies")
	private Integer housingSubsidies;
	@Column("tax")
	private Integer tax;
	@Column("social_security")
	private Integer socialSecurity;
	@Column("social_security_deduction")
	private Integer socialSecurityDeduction;
	@Column("modify_id")
	private Integer modifyId;
	@Column("modify_time")
	private Date modifyTime;
	@Column("effect_time")
	private Date effectTime;

	// 增账补贴
	@Column("subsidies")
	private Double subsidies = 0.0;
	@Readonly
	@Column("job_number")
	private String jobNumber;
	@Readonly
	@Column("true_name")
	private String trueName;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getStandardSalary() {
		return standardSalary;
	}

	public void setStandardSalary(Integer standardSalary) {
		this.standardSalary = standardSalary;
	}

	public Integer getPostSalary() {
		return postSalary;
	}

	public void setPostSalary(Integer postSalary) {
		this.postSalary = postSalary;
	}

	public Integer getPerformSalary() {
		return performSalary;
	}

	public void setPerformSalary(Integer performSalary) {
		this.performSalary = performSalary;
	}

	public Integer getRewardSalary() {
		return rewardSalary;
	}

	public void setRewardSalary(Integer rewardSalary) {
		this.rewardSalary = rewardSalary;
	}

	public Integer getServiceAward() {
		return serviceAward;
	}

	public void setServiceAward(Integer serviceAward) {
		this.serviceAward = serviceAward;
	}

	public Integer getCommunicationAllowance() {
		return communicationAllowance;
	}

	public void setCommunicationAllowance(Integer communicationAllowance) {
		this.communicationAllowance = communicationAllowance;
	}

	public Integer getOilAllowance() {
		return oilAllowance;
	}

	public void setOilAllowance(Integer oilAllowance) {
		this.oilAllowance = oilAllowance;
	}

	public Integer getHeatingAllowance() {
		return heatingAllowance;
	}

	public void setHeatingAllowance(Integer heatingAllowance) {
		this.heatingAllowance = heatingAllowance;
	}

	public Integer getMealAllowance() {
		return mealAllowance;
	}

	public void setMealAllowance(Integer mealAllowance) {
		this.mealAllowance = mealAllowance;
	}

	public Integer getOvertimeAllowance() {
		return overtimeAllowance;
	}

	public void setOvertimeAllowance(Integer overtimeAllowance) {
		this.overtimeAllowance = overtimeAllowance;
	}

	public Integer getMaternityInsurance() {
		return maternityInsurance;
	}

	public void setMaternityInsurance(Integer maternityInsurance) {
		this.maternityInsurance = maternityInsurance;
	}

	public Integer getHousingSubsidies() {
		return housingSubsidies;
	}

	public void setHousingSubsidies(Integer housingSubsidies) {
		this.housingSubsidies = housingSubsidies;
	}

	public Integer getTax() {
		return tax;
	}

	public void setTax(Integer tax) {
		this.tax = tax;
	}

	public Integer getSocialSecurity() {
		return socialSecurity;
	}

	public void setSocialSecurity(Integer socialSecurity) {
		this.socialSecurity = socialSecurity;
	}

	public Integer getSocialSecurityDeduction() {
		return socialSecurityDeduction;
	}

	public void setSocialSecurityDeduction(Integer socialSecurityDeduction) {
		this.socialSecurityDeduction = socialSecurityDeduction;
	}

	public Integer getModifyId() {
		return modifyId;
	}

	public void setModifyId(Integer modifyId) {
		this.modifyId = modifyId;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getJobNumber() {
		return jobNumber;
	}

	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}

	public String getTrueName() {
		return trueName;
	}

	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}

	public Date getEffectTime() {
		return effectTime;
	}

	public void setEffectTime(Date effectTime) {
		this.effectTime = effectTime;
	}
	public Double getSubsidies() {
		return subsidies;
	}

	public void setSubsidies(Double subsidies) {
		this.subsidies = subsidies;
	}

}
