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
@Table("att_recording")
public class Recording {




	@Id
	@Column("recording_id")
	private Integer recordingId;
	@Column("user_id")
	private Integer userId;
	@Column("subject")
	private String subject;
	@Column("recording_time")
	private Date recordingTime;
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
	@Column("recording_AM")
	private Integer recordingAM;
	@Column("operator_id")
	private Integer operatorId;
	@Column("opinion")
	private String opinion;
	
	@Readonly
	@Column("u.true_name")
	private String trueName;
	@Readonly
	@Column("corp_name") 
	private String corpName;
	@Readonly
	@Column("operator")
	private String operator;
	
	
	@Readonly
	@Column("u.corp_id")
	private Integer corpId;
	@Readonly
	@Column("u.org_id")
	private Integer orgId;
	@Readonly
	@Column("c.day_id")
	private Integer dayId;
	@Readonly
	@Column("c.week_id")
	private Integer weekId;
	
	private String actorName;
	
	public String getActorName(){
		if(operator != null){
			return operator;
		}
		return null;
	}
	
	public String getOpinion() {
		return opinion;
	}
	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	public String getCorpName() {
		return corpName;
	}
	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
		this.actorName = operator;
	}
	public Integer getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(Integer operatorId) {
		this.operatorId = operatorId;
	}
	public Integer getRecordingId() {
		return recordingId;
	}
	public void setRecordingId(Integer recordingId) {
		this.recordingId = recordingId;
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
	public Date getRecordingTime() {
		return recordingTime;
	}
	public void setRecordingTime(Date recordingTime) {
		this.recordingTime = recordingTime;
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
	public Integer getRecordingAM() {
		return recordingAM;
	}
	public void setRecordingAM(Integer recordingAM) {
		this.recordingAM = recordingAM;
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
	@Override
	public String toString() {
		return "Recording [recordingId=" + recordingId + ", userId=" + userId
				+ ", subject=" + subject + ", recordingTime=" + recordingTime
				+ ", reason=" + reason + ", approve=" + approve + ", status="
				+ status + ", createTime=" + createTime + ", modifyTime="
				+ modifyTime + ", recordingAM=" + recordingAM + "]";
	}

}
