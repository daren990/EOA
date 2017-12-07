package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 资产审批人员.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("res_change_actor")
@PK({ "changeId", "actorId", "variable" })
public class ChangeActor {

	@Column("change_id")
	private Integer changeId;
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

	public ChangeActor() {
	}

	public ChangeActor(Integer changeId, Integer actorId, Integer refererId,
			Integer approve, String variable, Date modifyTime, String opinion) {
		this.changeId = changeId;
		this.actorId = actorId;
		this.refererId = refererId;
		this.approve = approve;
		this.variable = variable;
		this.modifyTime = modifyTime;
		this.opinion = opinion;
	}

	public Integer getChangeId() {
		return changeId;
	}

	public void setChangeId(Integer changeId) {
		this.changeId = changeId;
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
