package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 绩效审批人员.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("exm_perform_actor")
@PK({ "performId", "actorId", "variable" })
public class PerformActor {

	@Column("perform_id")
	private Integer performId;
	@Column("actor_id")
	private Integer actorId;
	@Column("referer_id")
	private Integer refererId;
	@Column("approve")
	private Integer approve;
	@Column("variable")
	private String variable;
	@Column("modify_time")
	private Date modifyTime;
	@Column("opinion")
	private String opinion;

	@Readonly
	@Column("actor_name")
	private String actorName;
	
	public PerformActor() {
	}

	public PerformActor(Integer performId, Integer actorId, Integer refererId,
			Integer approve, String variable, Date modifyTime, String opinion) {
		this.performId = performId;
		this.actorId = actorId;
		this.refererId = refererId;
		this.approve = approve;
		this.variable = variable;
		this.modifyTime = modifyTime;
		this.opinion = opinion;
	}

	public Integer getPerformId() {
		return performId;
	}

	public void setPerformId(Integer performId) {
		this.performId = performId;
	}

	public Integer getActorId() {
		return actorId;
	}

	public void setActorId(Integer actorId) {
		this.actorId = actorId;
	}

	public Integer getRefererId() {
		return refererId;
	}

	public void setRefererId(Integer refererId) {
		this.refererId = refererId;
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

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getActorName() {
		return actorName;
	}

	public void setActorName(String actorName) {
		this.actorName = actorName;
	}

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}
}
