package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 加班.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("att_overtime")
public class Overtime {

	@Id
	@Column("overtime_id")
	private Integer overtimeId;
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
	@Column("work_minute")
	private Integer workMinute;
	@Column("project")
	private String project;
	@Column("content")
	private String content;
	@Column("remark")
	private String remark;
	@Column("operator_id")
	private Integer operatorId;
	@Column("approve")
	private Integer approve;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("opinion")
	private String opinion;

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

	public Integer getOvertimeId() {
		return overtimeId;
	}

	public void setOvertimeId(Integer overtimeId) {
		this.overtimeId = overtimeId;
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

	public Integer getWorkMinute() {
		return workMinute;
	}

	public void setWorkMinute(Integer workMinute) {
		this.workMinute = workMinute;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Integer operatorId) {
		this.operatorId = operatorId;
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

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

}
