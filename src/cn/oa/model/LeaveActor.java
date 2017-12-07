package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

@Table("att_leave_actor")
@PK({ "leaveId", "actorId", "variable" })
public class LeaveActor {
	@Column("leave_id")
	private Integer leaveId;
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
	@Column("step")
	private Integer step;
	@Column("opinion")
	private String opinion;
	
	@Readonly
	@Column("actor_name")
	private String actorName;
	
	public LeaveActor() {
	}


	public LeaveActor(Integer leaveId, Integer actorId, Integer refererId,
			Integer approve, String variable, Date modifyTime ,Integer step, String opinion) {
		this.leaveId = leaveId;
		this.actorId = actorId;
		this.refererId = refererId;
		this.approve = approve;
		this.variable = variable;
		this.modifyTime = modifyTime;
		this.step = step;
		this.opinion = opinion;
	}


	public Integer getLeaveId() {
		return leaveId;
	}


	public void setLeaveId(Integer leaveId) {
		this.leaveId = leaveId;
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


	public Integer getStep() {
		return step;
	}


	public void setStep(Integer step) {
		this.step = step;
	}


	public String getOpinion() {
		return opinion;
	}


	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}
	
}
