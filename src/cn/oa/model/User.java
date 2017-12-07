package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.ManyMany;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 用户.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("sec_user")
public class User {

	@Id
	@Column("user_id")
	private Integer userId;
	@Column("username")
	private String username;
	@Column("password")
	private String password;
	@Column("job_number")
	private String jobNumber;
	@Column("true_name")
	private String trueName;
	@Column("email")
	private String email;
	@Column("corp_id")
	private Integer corpId;
	@Column("org_id")
	private Integer orgId;
	//直属上司
	@Column("manager_id")
	private Integer managerId;
	@Column("level")
	private Integer level;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	

	@ManyMany(target = Role.class, relation = "sec_user_role", from = "user_id", to = "role_id")
	private List<Role> roles;
	
	private String interim;

	@Readonly
	@Column("on_position")
	private Integer onPosition;
	@Readonly
	@Column("corp_name")
	private String corpName;
	@Readonly
	@Column("org_name")
	private String orgName;
	@Readonly
	@Column("manager_name")
	private String managerName;
	@Readonly
	@Column("day_id")
	private Integer dayId;
	@Readonly
	@Column("week_id")
	private Integer weekId;
	@Readonly
	@Column("entry_date")
	private Date entryDate;
	@Readonly
	@Column("annual_id")
	private Integer annualId;
	@Readonly
	@Column("accountant_id")
	private Integer accountantId;
	@Readonly
	@Column("threshold_id")
	private Integer thresholdId;
	@Readonly
	@Column("confLeave_id")
	private Integer confLeaveId;
	@Readonly
	@Column("holiday_id")
	private Integer holidayId;
	
	@Readonly
	@Column("archive_count")
	private Integer archiveCount;
	@Readonly
	@Column("control_id")
	private Integer controlId;
	@Readonly
	@Column("quit_date")
	private Date quitDate;
	@Readonly
	@Column("full_date")
	private Date fullDate;
	
	@Readonly
	@Column("qq")
	private String qq;
	@Readonly
	@Column("phone")
	private String phone;
	@Readonly
	@Column("exigency_phone")
	private String exigencyPhone;
	@Readonly
	@Column("position")
	private String position;
	
	@Readonly
	@Column("wechat_name")
	private String wechatName;
	
	private float sumTime;
	
	private String overTimeRest;
	
	public float getSumTime() {
		return sumTime;
	}

	public void setSumTime(float sumTime) {
		this.sumTime = sumTime;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getCorpId() {
		return corpId;
	}

	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public Integer getManagerId() {
		return managerId;
	}

	public void setManagerId(Integer managerId) {
		this.managerId = managerId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
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

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public String getCorpName() {
		return corpName;
	}

	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
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

	public Date getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}

	public Integer getArchiveCount() {
		return archiveCount;
	}

	public void setArchiveCount(Integer archiveCount) {
		this.archiveCount = archiveCount;
	}

	public Integer getControlId() {
		return controlId;
	}

	public Integer getAnnualId() {
		return annualId;
	}

	public void setAnnualId(Integer annualId) {
		this.annualId = annualId;
	}

	public void setControlId(Integer controlId) {
		this.controlId = controlId;
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
	

	public Date getQuitDate() {
		return quitDate;
	}

	public void setQuitDate(Date quitDate) {
		this.quitDate = quitDate;
	}

	public Date getFullDate() {
		return fullDate;
	}

	public void setFullDate(Date fullDate) {
		this.fullDate = fullDate;
	}

	public String getRoleIds() {
		return Converts.str(Converts.array(Role.class, Integer.class, getRoles(), "roleId"), ",");
	}

	public String getRoleNames() {
		return Converts.str(Converts.array(Role.class, String.class, getRoles(), "roleName"), ",");
	}

	public String getRoleDescs() {
		return Converts.str(Converts.array(Role.class, String.class, getRoles(), "roleDesc"), ",");
	}

	public String getInterim() {
		return interim;
	}

	public void setInterim(String interim) {
		this.interim = interim;
	}

	public Integer getOnPosition() {
		return onPosition;
	}

	public void setOnPosition(Integer onPosition) {
		this.onPosition = onPosition;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getExigencyPhone() {
		return exigencyPhone;
	}

	public void setExigencyPhone(String exigencyPhone) {
		this.exigencyPhone = exigencyPhone;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getOverTimeRest() {
		return overTimeRest;
	}

	public void setOverTimeRest(String overTimeRest) {
		this.overTimeRest = overTimeRest;
	}

	public String getWechatName() {
		return wechatName;
	}

	public void setWechatName(String wechatName) {
		this.wechatName = wechatName;
	}


	
}
