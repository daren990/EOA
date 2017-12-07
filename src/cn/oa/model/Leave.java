package cn.oa.model;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 请假.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("att_leave")
public class Leave {
	
	public static final int AM = 0;
	public static final int PM = 1;

	@Override
	public String toString() {
		return "Leave [leaveId=" + leaveId + ", userId=" + userId
				+ ", subject=" + subject + ", typeId=" + typeId
				+ ", startTime=" + startTime + ", endTime=" + endTime
				+ ", leaveMinute=" + leaveMinute + ", reason=" + reason
				+ ", approve=" + approve + ", status=" + status
				+ ", createTime=" + createTime + ", modifyTime=" + modifyTime
				+ ", startType=" + startType + ", endType=" + endType
				+ ", typeName=" + typeName + ", trueName=" + trueName
				+ ", operator=" + operator + ", corpId=" + corpId + ", orgId="
				+ orgId + ", dayId=" + dayId + ", weekId=" + weekId
				+ ", variable=" + variable + ", corpName=" + corpName
				+ ", actorId=" + actorId + "]";
	}

	@Id
	@Column("leave_id")
	private Integer leaveId;
	@Column("user_id")
	private Integer userId;
	@Column("subject")
	private String subject;
	@Column("type_id")
	private Integer typeId;
	@Column("start_time")
	private Date startTime;
	@Column("end_time")
	private Date endTime;
	@Column("leave_minute")
	private Integer leaveMinute;
	@Column("reason")
	private String reason;
	@Column("approve")
	private Integer approve;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("startType")
	private Integer startType;
	@Column("endType")
	private Integer endType;
	

	@Readonly
	@Column("type_name")
	private String typeName;
	@Readonly
	@Column("true_name")
	private String trueName;
	@Readonly
	@Column("operator")
	private String operator;

	@Readonly
	@Column("corp_id")
	private Integer corpId;
	@Readonly
	@Column("org_id")
	private Integer orgId;
	@Readonly
	@Column("day_id")
	private Integer dayId;
	@Readonly
	@Column("week_id")
	private Integer weekId;
	
	
	@Readonly
	@Column("variable")
	private String variable;
	@Readonly
	@Column("corp_name")
	private String corpName;
	
	private Integer actorId;


	public Integer getLeaveId() {
		return leaveId;
	}

	public void setLeaveId(Integer leaveId) {
		this.leaveId = leaveId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getLeaveMinute() {
		return leaveMinute;
	}

	public void setLeaveMinute(Integer leaveMinute) {
		this.leaveMinute = leaveMinute;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getApprove() {
		return approve;
	}

	public void setApprove(Integer approve) {
		this.approve = approve;
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

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getTrueName() {
		return trueName;
	}

	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
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

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String getCorpName() {
		return corpName;
	}

	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}

	public Integer getActorId() {
		return actorId;
	}

	public void setActorId(Integer actorId) {
		this.actorId = actorId;
	}

	public Integer getStartType() {
		return startType;
	}

	public void setStartType(Integer startType) {
		this.startType = startType;
	}

	public Integer getEndType() {
		return endType;
	}

	public void setEndType(Integer endType) {
		this.endType = endType;
	}
	
	private String days;
	
	public void setDays(String days){
		this.days = days;
	}

	public String getDays() {
		return days;
	}


	
	
}
