package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

@Table("res_asset_allocate_actor")
public class AssetAllocateActor {
	@Id
	@Column("id")
	private Integer Id;
	@Column("allocate_id")
	private Integer allocateId;
	@Column("actor_id")
	private Integer actorId;
	@Column("modify_time")
	private Date modifyTime;
	@Column("opinion")
	private String opinion;
	@Column("approve")
	private Integer approve;
	@Column("veriable")
	private String veriable;
	
	
	@Readonly
	@Column("actor_name")
	private String actorName;
	
	@Readonly
	@Column("true_name")
	private String trueName;
	
	public AssetAllocateActor(){}
	public AssetAllocateActor(Integer allocateId, Integer actorId, Date modifyTime, String opinion,Integer approve, String veriable){
		this.allocateId = allocateId;
		this.actorId = actorId;
		this.modifyTime = modifyTime;
		this.opinion = opinion;
		this.approve = approve;
		this.veriable = veriable;
		
	}
	
	public Integer getId() {
		return Id;
	}
	public void setId(Integer id) {
		Id = id;
	}
	public Integer getAllocateId() {
		return allocateId;
	}
	public void setAllocateId(Integer allocateId) {
		this.allocateId = allocateId;
	}
	public Integer getActorId() {
		return actorId;
	}
	public void setActorId(Integer actorId) {
		this.actorId = actorId;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getOpinion() {
		return opinion;
	}
	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}
	public Integer getApprove() {
		return approve;
	}
	public void setApprove(Integer approve) {
		this.approve = approve;
	}
	public String getVeriable() {
		return veriable;
	}
	public void setVeriable(String veriable) {
		this.veriable = veriable;
	}
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	public String getActorName() {
		return actorName;
	}
	public void setActorName(String actorName) {
		this.actorName = actorName;
	}
	
}
