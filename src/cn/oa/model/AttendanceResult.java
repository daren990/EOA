package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 考勤汇总.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("result_by_attendance")
@PK({ "resultMonth", "userId" })
public class AttendanceResult {

	@Column("result_month")
	private String resultMonth;
	@Column("user_id")
	private Integer userId;
	@Column("start_date")
	private Date startDate;
	@Column("end_date")
	private Date endDate;
	@Column("work_day")
	private Integer workDay;// 应出勤天数
	@Column("work_day_nh")
	private Integer workDayNH;// 应出勤天数
	@Column("work_minute")
	private Integer workMinute; // 每天工作分钟数
	@Column("threshold")
	private String threshold;
	@Column("unpaid_leave_minute")
	private Integer unpaidLeaveMinute;
	@Column("sick_leave_minute")
	private Integer sickLeaveMinute;
	@Column("funeral_leave_minute")
	private Integer funeralLeaveMinute;
	@Column("marital_leave_minute")
	private Integer maritalLeaveMinute;
	@Column("annual_leave_minute")
	private Integer annualLeaveMinute;
	@Column("injury_leave_minute")
	private Integer injuryLeaveMinute;
	@Column("deferred_leave_minute")
	private Integer deferredLeaveMinute;
	@Column("paternity_leave_minute")
	private Integer paternityLeaveMinute;
	@Column("paid_leave_minute")
	private Integer paidLeaveMinute;
	@Column("errand_minute")
	private Integer errandMinute;
	@Column("outwork_minute")
	private Integer outworkMinute;
	@Column("overtime_minute")
	private Integer overtimeMinute;
	@Column("version")
	private Integer version;
	@Column("modify_id")
	private Integer modifyId;
	@Column("modify_time")
	private Date modifyTime;
	
	@Column("absentAmount")
	private Integer absentAmount;
	@Column("absentAmountTotal")
	private Float absentAmountTotal;
	@Column("lateAmount")
	private Integer lateAmount;
	@Column("forgetAmount")
	private Integer forgetAmount;
	@Column("should_work_day")
	private Integer shouldWorkDay;// 标准应出勤天数(排班决定)
	@Column("should_work_day_nh")
	private Integer shouldWorkDayNH;// 标准应出勤天数(排班决定),不包含节假日

	@Readonly
	@Column("corp_id")
	private String corpId;
	@Readonly
	@Column("job_number")
	private String jobNumber;
	@Readonly
	@Column("true_name")
	private String trueName;

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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Integer getWorkDay() {
		return workDay;
	}

	public void setWorkDay(Integer workDay) {
		this.workDay = workDay;
	}

	public Integer getWorkMinute() {
		return workMinute;
	}

	public void setWorkMinute(Integer workMinute) {
		this.workMinute = workMinute;
	}

	public String getThreshold() {
		return threshold;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
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

	public String getCorpId() {
		return corpId;
	}

	public void setCorpId(String corpId) {
		this.corpId = corpId;
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

	public Integer getWorkDayNH() {
		return workDayNH;
	}

	public void setWorkDayNH(Integer workDayNH) {
		this.workDayNH = workDayNH;
	}

	public Float getAbsentAmountTotal() {
		return absentAmountTotal;
	}

	public void setAbsentAmountTotal(Float absentAmountTotal) {
		this.absentAmountTotal = absentAmountTotal;
	}


	
	
}
