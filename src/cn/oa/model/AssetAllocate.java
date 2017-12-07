package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

@Table("res_asset_allocate")
public class AssetAllocate {
	@Id
	@Column("allocate_id")
	private Integer allocateId;
	@Column("asset_id")
	private Integer assetId;
	@Column("user_id")
	private Integer userId;
	@Column("nowUser_id")
	private Integer nowUserId;
	@Column("remark")
	private String remark;
	@Column("subject")
	private String subject;
	@Column("storage_place")
	private String storagePlace;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("allocate_time")
	private Date allocateTime;
	@Column("approved")
	private Integer approved;
	@Column("allocatePerson_id")
	private Integer allocatePersonId;
	
	@Readonly
	@Column("true_name")
	private String trueName;
	
	@Readonly
	@Column("org_name")
	private String orgName;
	
	private String assetName;
	private String assetNumber;
	private Integer item;
	private String allocateTrueName;
	
	public Integer getAllocateId() {
		return allocateId;
	}
	public void setAllocateId(Integer allocateId) {
		this.allocateId = allocateId;
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
	public Integer getNowUserId() {
		return nowUserId;
	}
	public void setNowUserId(Integer nowUserId) {
		this.nowUserId = nowUserId;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getStoragePlace() {
		return storagePlace;
	}
	public void setStoragePlace(String storagePlace) {
		this.storagePlace = storagePlace;
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
	public Date getAllocateTime() {
		return allocateTime;
	}
	public void setAllocateTime(Date allocateTime) {
		this.allocateTime = allocateTime;
	}
	public Integer getApproved() {
		return approved;
	}
	public void setApproved(Integer approved) {
		this.approved = approved;
	}
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getAssetName() {
		return assetName;
	}
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}
	public String getAssetNumber() {
		return assetNumber;
	}
	public void setAssetNumber(String assetNumber) {
		this.assetNumber = assetNumber;
	}
	public Integer getItem() {
		return item;
	}
	public void setItem(Integer item) {
		this.item = item;
	}
	public Integer getAllocatePersonId() {
		return allocatePersonId;
	}
	public void setAllocatePersonId(Integer allocatePersonId) {
		this.allocatePersonId = allocatePersonId;
	}
	public String getAllocateTrueName() {
		return allocateTrueName;
	}
	public void setAllocateTrueName(String allocateTrueName) {
		this.allocateTrueName = allocateTrueName;
	}
	
}
