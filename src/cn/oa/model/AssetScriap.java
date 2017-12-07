package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("res_asset_scriap")
public class AssetScriap {
	@Id
	@Column("scriap_id")
	private Integer scriapId;
	@Column("asset_id")
	private Integer assetId;
	@Column("user_id")
	private Integer userId;
	@Column("apply_id")
	private Integer applyId;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("scriap_time")
	private Date scriapTime;
	@Column("scriap_type")
	private Integer scriapType;
	@Column("depreciation")
	private Integer depreciation;
	@Column("reason")
	private String reason;
	@Column("approve")
	private Integer approve;
	@Column("storagePlace")
	private String storagePlace;
	@Column("realAge")
	private Integer realAge;
	@Column("subject")
	private String subject;
	
	private Integer actorId;
	private String applyName;
	private Integer item;
	
	
	public Integer getApprove() {
		return approve;
	}
	public void setApprove(Integer approve) {
		this.approve = approve;
	}
	public Integer getScriapId() {
		return scriapId;
	}
	public void setScriapId(Integer scriapId) {
		this.scriapId = scriapId;
	}
	public Integer getAssetId() {
		return assetId;
	}
	public void setAssetId(Integer assetId) {
		this.assetId = assetId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getApplyId() {
		return applyId;
	}
	public void setApplyId(Integer applyId) {
		this.applyId = applyId;
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
	public Date getScriapTime() {
		return scriapTime;
	}
	public void setScriapTime(Date scriapTime) {
		this.scriapTime = scriapTime;
	}
	public Integer getScriapType() {
		return scriapType;
	}
	public void setScriapType(Integer scriapType) {
		this.scriapType = scriapType;
	}
	public Integer getDepreciation() {
		return depreciation;
	}
	public void setDepreciation(Integer depreciation) {
		this.depreciation = depreciation;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public Integer getActorId() {
		return actorId;
	}
	public void setActorId(Integer actorId) {
		this.actorId = actorId;
	}
	public String getApplyName() {
		return applyName;
	}
	public void setApplyName(String applyName) {
		this.applyName = applyName;
	}
	public Integer getItem() {
		return item;
	}
	public void setItem(Integer item) {
		this.item = item;
	}
	public String getStoragePlace() {
		return storagePlace;
	}
	public void setStoragePlace(String storagePlace) {
		this.storagePlace = storagePlace;
	}
	public Integer getRealAge() {
		return realAge;
	}
	public void setRealAge(Integer realAge) {
		this.realAge = realAge;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
}
