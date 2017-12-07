package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 工资条.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("result_by_wage")
@PK({ "resultMonth", "userId" })
public class WageResult {

	@Column("result_month")
	private String resultMonth;
	@Column("user_id")
	private Integer userId;
	@Column("standard_salary")
	private Double standardSalary;
	@Column("real_salary")
	private Double realSalary;
	@Column("gross_pay")
	private Double grossPay;
	@Column("post_salary")
	private Double postSalary;
	@Column("perform_salary")
	private Double performSalary = 0.0;;
	@Column("reward_salary")
	private Double rewardSalary;
	@Column("service_award")
	private Double serviceAward;
	@Column("communication_allowance")
	private Double communicationAllowance;
	@Column("oil_allowance")
	private Double oilAllowance;
	@Column("heating_allowance")
	private Double heatingAllowance;
	@Column("meal_allowance")
	private Double mealAllowance;
	@Column("overtime_allowance")
	private Double overtimeAllowance;
	@Column("maternity_insurance")
	private Double maternityInsurance;
	@Column("housing_subsidies")
	private Double housingSubsidies;
	@Column("late_deduction")
	private Double lateDeduction;
	@Column("forget_deduction")
	private Double forgetDeduction;
	@Column("absent_deduction")
	private Double absentDeduction;
	@Column("leave_deduction")
	private Double leaveDeduction;
	@Column("sick_deduction")
	private Double sickDeduction;
	@Column("tax")
	private Double tax;
	@Column("social_security")
	private Double socialSecurity;
	@Column("social_security_deduction")
	private Double socialSecurityDeduction;
	@Column("modify_id")
	private Integer modifyId;
	@Column("modify_time")
	private Date modifyTime;


	// 未打卡、迟到早退、旷工累积扣除
	@Column("accumulate_deduction")
	private Double accumulateDeduction = 0.0;
	
	//增账补贴
	@Column("subsidies")
	private	Double subsidies = 0.0;
	
	//提成
	@Column("commission")
	private Double commission = 0.0;

	
	private String start;
	private String end;

	@Readonly
	@Column("quit_date")
	private Date quitDate;
	@Readonly
	@Column("position")
	private String position;
	@Readonly
	@Column("job_number")
	private String jobNumber;
	@Readonly
	@Column("true_name")
	private String trueName;
	@Readonly
	@Column("entry_date")
	private Date entryDate;
	@Readonly
	@Column("work_day")
	private Integer workDay;
	@Readonly
	@Column("work_day_nh")
	private Integer workDayNH;
	@Readonly
	@Column("work_minute")
	private Integer workMinute;
	@Readonly
	@Column("unpaid_leave_minute")
	private Integer unpaidLeaveMinute;
	@Readonly
	@Column("sick_leave_minute")
	private Integer sickLeaveMinute;
	@Readonly
	@Column("funeral_leave_minute")
	private Integer funeralLeaveMinute;
	@Readonly
	@Column("marital_leave_minute")
	private Integer maritalLeaveMinute;
	@Readonly
	@Column("annual_leave_minute")
	private Integer annualLeaveMinute;
	@Readonly
	@Column("injury_leave_minute")
	private Integer injuryLeaveMinute;
	@Readonly
	@Column("deferred_leave_minute")
	private Integer deferredLeaveMinute;
	@Readonly
	@Column("paternity_leave_minute")
	private Integer paternityLeaveMinute;
	@Readonly
	@Column("paid_leave_minute")
	private Integer paidLeaveMinute;
	@Readonly
	@Column("errand_minute")
	private Integer errandMinute;
	@Readonly
	@Column("outwork_minute")
	private Integer outworkMinute;
	@Readonly
	@Column("overtime_minute")
	private Integer overtimeMinute;
	@Readonly
	@Column("absentAmount")
	private Integer absentAmount;
	@Readonly
	@Column("absentAmountTotal")
	private Integer absentAmountTotal;
	@Readonly
	@Column("lateAmount")
	private Integer lateAmount;
	@Readonly
	@Column("forgetAmount")
	private Integer forgetAmount;
	@Readonly
	@Column("should_work_day")
	private Integer shouldWorkDay;
	@Readonly
	@Column("should_work_day_nh")
	private Integer shouldWorkDayNH;
	
	public String getResultMonth() {
		return resultMonth;
	}

	public void setResultMonth(String resultMonth) {
		this.resultMonth = resultMonth;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Double getStandardSalary() {
		return standardSalary;
	}

	public void setStandardSalary(Double standardSalary) {
		this.standardSalary = standardSalary;
	}

	public Double getPostSalary() {
		return postSalary;
	}

	public void setPostSalary(Double postSalary) {
		this.postSalary = postSalary;
	}

	public Double getRealSalary() {
		return realSalary;
	}

	public void setRealSalary(Double realSalary) {
		this.realSalary = realSalary;
	}

	public Double getPerformSalary() {
		return performSalary;
	}

	public void setPerformSalary(Double performSalary) {
		this.performSalary = performSalary;
	}

	public Double getRewardSalary() {
		return rewardSalary;
	}

	public void setRewardSalary(Double rewardSalary) {
		this.rewardSalary = rewardSalary;
	}

	public Double getServiceAward() {
		return serviceAward;
	}

	public void setServiceAward(Double serviceAward) {
		this.serviceAward = serviceAward;
	}

	public Double getCommunicationAllowance() {
		return communicationAllowance;
	}

	public void setCommunicationAllowance(Double communicationAllowance) {
		this.communicationAllowance = communicationAllowance;
	}

	public Double getOilAllowance() {
		return oilAllowance;
	}

	public void setOilAllowance(Double oilAllowance) {
		this.oilAllowance = oilAllowance;
	}

	public Double getHeatingAllowance() {
		return heatingAllowance;
	}

	public void setHeatingAllowance(Double heatingAllowance) {
		this.heatingAllowance = heatingAllowance;
	}

	public Double getMealAllowance() {
		return mealAllowance;
	}

	public void setMealAllowance(Double mealAllowance) {
		this.mealAllowance = mealAllowance;
	}

	public Double getOvertimeAllowance() {
		return overtimeAllowance;
	}

	public void setOvertimeAllowance(Double overtimeAllowance) {
		this.overtimeAllowance = overtimeAllowance;
	}

	public Double getMaternityInsurance() {
		return maternityInsurance;
	}

	public void setMaternityInsurance(Double maternityInsurance) {
		this.maternityInsurance = maternityInsurance;
	}

	public Double getHousingSubsidies() {
		return housingSubsidies;
	}

	public void setHousingSubsidies(Double housingSubsidies) {
		this.housingSubsidies = housingSubsidies;
	}

	public Double getLateDeduction() {
		return lateDeduction;
	}

	public void setLateDeduction(Double lateDeduction) {
		this.lateDeduction = lateDeduction;
	}


	public Double getForgetDeduction() {
		return forgetDeduction;
	}

	public void setForgetDeduction(Double forgetDeduction) {
		this.forgetDeduction = forgetDeduction;
	}

	public Double getAbsentDeduction() {
		return absentDeduction;
	}

	public void setAbsentDeduction(Double absentDeduction) {
		this.absentDeduction = absentDeduction;
	}

	public Double getLeaveDeduction() {
		return leaveDeduction;
	}

	public void setLeaveDeduction(Double leaveDeduction) {
		this.leaveDeduction = leaveDeduction;
	}

	public Double getSickDeduction() {
		return sickDeduction;
	}

	public void setSickDeduction(Double sickDeduction) {
		this.sickDeduction = sickDeduction;
	}

	public Double getTax() {
		return tax;
	}

	public void setTax(Double tax) {
		this.tax = tax;
	}

	public Double getSocialSecurity() {
		return socialSecurity;
	}

	public void setSocialSecurity(Double socialSecurity) {
		this.socialSecurity = socialSecurity;
	}

	public Double getSocialSecurityDeduction() {
		return socialSecurityDeduction;
	}

	public void setSocialSecurityDeduction(Double socialSecurityDeduction) {
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

	public Date getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}

	public Integer getWorkDay() {
		return workDay;
	}

	public void setWorkDay(Integer workDay) {
		this.workDay = workDay;
	}

	public Integer getWorkDayNH() {
		return workDayNH;
	}

	public void setWorkDayNH(Integer workDayNH) {
		this.workDayNH = workDayNH;
	}

	public Integer getWorkMinute() {
		return workMinute;
	}

	public void setWorkMinute(Integer workMinute) {
		this.workMinute = workMinute;
	}

	public Integer getUnpaidLeaveMinute() {
		return unpaidLeaveMinute;
	}

	public void setUnpaidLeaveMinute(Integer unpaidLeaveMinute) {
		this.unpaidLeaveMinute = unpaidLeaveMinute;
	}

	public Integer getSickLeaveMinute() {
		return sickLeaveMinute;
	}

	public void setSickLeaveMinute(Integer sickLeaveMinute) {
		this.sickLeaveMinute = sickLeaveMinute;
	}

	public Integer getFuneralLeaveMinute() {
		return funeralLeaveMinute;
	}

	public void setFuneralLeaveMinute(Integer funeralLeaveMinute) {
		this.funeralLeaveMinute = funeralLeaveMinute;
	}

	public Integer getMaritalLeaveMinute() {
		return maritalLeaveMinute;
	}

	public void setMaritalLeaveMinute(Integer maritalLeaveMinute) {
		this.maritalLeaveMinute = maritalLeaveMinute;
	}

	public Integer getAnnualLeaveMinute() {
		return annualLeaveMinute;
	}

	public void setAnnualLeaveMinute(Integer annualLeaveMinute) {
		this.annualLeaveMinute = annualLeaveMinute;
	}

	public Integer getInjuryLeaveMinute() {
		return injuryLeaveMinute;
	}

	public void setInjuryLeaveMinute(Integer injuryLeaveMinute) {
		this.injuryLeaveMinute = injuryLeaveMinute;
	}

	public Integer getDeferredLeaveMinute() {
		return deferredLeaveMinute;
	}

	public void setDeferredLeaveMinute(Integer deferredLeaveMinute) {
		this.deferredLeaveMinute = deferredLeaveMinute;
	}

	public Integer getPaternityLeaveMinute() {
		return paternityLeaveMinute;
	}

	public void setPaternityLeaveMinute(Integer paternityLeaveMinute) {
		this.paternityLeaveMinute = paternityLeaveMinute;
	}

	public Integer getPaidLeaveMinute() {
		return paidLeaveMinute;
	}

	public void setPaidLeaveMinute(Integer paidLeaveMinute) {
		this.paidLeaveMinute = paidLeaveMinute;
	}

	public Integer getErrandMinute() {
		return errandMinute;
	}

	public void setErrandMinute(Integer errandMinute) {
		this.errandMinute = errandMinute;
	}

	public Integer getOutworkMinute() {
		return outworkMinute;
	}

	public void setOutworkMinute(Integer outworkMinute) {
		this.outworkMinute = outworkMinute;
	}

	public Integer getOvertimeMinute() {
		return overtimeMinute;
	}

	public void setOvertimeMinute(Integer overtimeMinute) {
		this.overtimeMinute = overtimeMinute;
	}

	public Integer getAbsentAmount() {
		return absentAmount;
	}

	public void setAbsentAmount(Integer absentAmount) {
		this.absentAmount = absentAmount;
	}

	public Integer getLateAmount() {
		return lateAmount;
	}

	public void setLateAmount(Integer lateAmount) {
		this.lateAmount = lateAmount;
	}

	public Integer getForgetAmount() {
		return forgetAmount;
	}

	public void setForgetAmount(Integer forgetAmount) {
		this.forgetAmount = forgetAmount;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public Date getQuitDate() {
		return quitDate;
	}

	public void setQuitDate(Date quitDate) {
		this.quitDate = quitDate;
	}

	public Integer getShouldWorkDay() {
		return shouldWorkDay;
	}

	public void setShouldWorkDay(Integer shouldWorkDay) {
		this.shouldWorkDay = shouldWorkDay;
	}

	public Integer getShouldWorkDayNH() {
		return shouldWorkDayNH;
	}

	public void setShouldWorkDayNH(Integer shouldWorkDayNH) {
		this.shouldWorkDayNH = shouldWorkDayNH;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public Double getAccumulateDeduction() {
		return accumulateDeduction;
	}

	public void setAccumulateDeduction(Double accumulateDeduction) {
		this.accumulateDeduction = accumulateDeduction;
	}

	public Double getSubsidies() {
		return subsidies;
	}

	public void setSubsidies(Double subsidies) {
		this.subsidies = subsidies;
	}

	public Double getCommission() {
		return commission;
	}

	public void setCommission(Double commission) {
		this.commission = commission;
	}

	public Integer getAbsentAmountTotal() {
		return absentAmountTotal;
	}

	public void setAbsentAmountTotal(Integer absentAmountTotal) {
		this.absentAmountTotal = absentAmountTotal;
	}

	public Double getGrossPay() {
		return grossPay;
	}

	public void setGrossPay(Double grossPay) {
		this.grossPay = grossPay;
	}
	
	
}
