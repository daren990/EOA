package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 绩效.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("exm_perform")
public class Perform {

	@Id
	@Column("perform_id")
	private Integer performId;
	@Column("release_id")
	private Integer releaseId;
	@Column("user_id")
	private Integer userId;
	@Column("subject")
	private String subject;
	@Column("approved")
	private Integer approved;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("model_id")
	private Integer modelId;

	@Readonly
	@Column("true_name")
	private String trueName;
	@Readonly
	@Column("start_date")
	private Date startDate;
	@Readonly
	@Column("end_date")
	private Date endDate;
	@Readonly
	@Column("release_start_date")
	private Date releaseStartDate;
	@Readonly
	@Column("release_end_date")
	private Date releaseEndDate;
	@Readonly
	@Column("approve")
	private Integer approve;
	@Readonly
	@Column("variable")
	private String variable;
	@Readonly
	@Column("version")
	private Integer version;
	@Readonly
	@Column("release_name")
	private String releaseName;

	private Integer actorId;
	private String actorName;
	private Double score;

	
	public Date getReleaseStartDate() {
		return releaseStartDate;
	}

	public void setReleaseStartDate(Date releaseStartDate) {
		this.releaseStartDate = releaseStartDate;
	}

	public Date getReleaseEndDate() {
		return releaseEndDate;
	}

	public void setReleaseEndDate(Date releaseEndDate) {
		this.releaseEndDate = releaseEndDate;
	}

	public Integer getPerformId() {
		return performId;
	}

	public void setPerformId(Integer performId) {
		this.performId = performId;
	}

	public Integer getReleaseId() {
		return releaseId;
	}

	public void setReleaseId(Integer releaseId) {
		this.releaseId = releaseId;
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

	public Integer getApproved() {
		return approved;
	}

	public void setApproved(Integer approved) {
		this.approved = approved;
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

	public Integer getApprove() {
		return approve;
	}

	public void setApprove(Integer approve) {
		this.approve = approve;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getActorId() {
		return actorId;
	}

	public void setActorId(Integer actorId) {
		this.actorId = actorId;
	}

	public String getActorName() {
		return actorName;
	}

	public void setActorName(String actorName) {
		this.actorName = actorName;
	}

	public Integer getModelId() {
		return modelId;
	}

	public void setModelId(Integer modelId) {
		this.modelId = modelId;
	}

	public String getReleaseName() {
		return releaseName;
	}

	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

}
