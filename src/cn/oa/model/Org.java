package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 组织架构.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("sec_org")
public class Org {

	public static final int DEFAULT = 0;
	public static final int CORP = 1;

	@Id
	@Column("org_id")
	private Integer orgId;
	@Column("parent_id")
	private Integer parentId;
	@Column("org_name")
	private String orgName;
	@Column("org_desc")
	private String orgDesc;
	@Column("type")
	private Integer type;
	@Column("day_id")
	private Integer dayId;
	@Column("week_id")
	private Integer weekId;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("annual_id")
	private Integer annualId;
	@Column("accountant_id")
	private Integer accountantId;
	@Column("threshold_id")
	private Integer thresholdId;
	@Column("confLeave_id")
	private Integer confLeaveId;
	@Column("holiday_id")
	private Integer holidayId;
	@Column("contact_name")
	private String contactName;
	@Column("contact_number")
	private String contactNumber;
	@Column("location")
	private String location;

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getOrgDesc() {
		return orgDesc;
	}

	public void setOrgDesc(String orgDesc) {
		this.orgDesc = orgDesc;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getDayId() {
		return dayId;
	}

	public void setDayId(Integer dayId) {
		this.dayId = dayId;
	}

	public Integer getWeekId() {
		return weekId;
	}

	public void setWeekId(Integer weekId) {
		this.weekId = weekId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Integer getAnnualId() {
		return annualId;
	}

	public void setAnnualId(Integer annualId) {
		this.annualId = annualId;
	}

	public Integer getAccountantId() {
		return accountantId;
	}

	public void setAccountantId(Integer accountantId) {
		this.accountantId = accountantId;
	}

	public Integer getThresholdId() {
		return thresholdId;
	}

	public void setThresholdId(Integer thresholdId) {
		this.thresholdId = thresholdId;
	}

	public Integer getConfLeaveId() {
		return confLeaveId;
	}

	public void setConfLeaveId(Integer confLeaveId) {
		this.confLeaveId = confLeaveId;
	}

	public Integer getHolidayId() {
		return holidayId;
	}

	public void setHolidayId(Integer holidayId) {
		this.holidayId = holidayId;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return "Org [orgId=" + orgId + ", parentId=" + parentId + ", orgName=" + orgName + ", orgDesc=" + orgDesc
				+ ", type=" + type + ", dayId=" + dayId + ", weekId=" + weekId + ", status=" + status + ", createTime="
				+ createTime + ", modifyTime=" + modifyTime + ", annualId=" + annualId + ", accountantId="
				+ accountantId + ", thresholdId=" + thresholdId + ", confLeaveId=" + confLeaveId + ", holidayId="
				+ holidayId + ", contactName=" + contactName + ", contactNumber=" + contactNumber + "]";
	}

}
