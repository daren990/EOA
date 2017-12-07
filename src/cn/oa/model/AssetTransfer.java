package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 
 * @author jiankun.chen
 *
 */
@Table("res_asset_transfer")
public class AssetTransfer {
	@Id
	@Column("transfer_id")
	private Integer transferId;
	@Column("asset_id")
	private Integer assetId;
	@Column("transferPerson_id")
	private Integer transferPersonId;
	@Column("sign_id")
	private Integer signId;
	@Column("remark")
	private String remark;
	@Column("approve")
	private Integer approve;
	@Column("create_time")
	private Date createTime;
	@Column("transfer_time")
	private Date transferTime;
	@Column("subject")
	private String subject;
	@Column("storagePlace")
	private String storagePlace;
	@Column("modify_time")
	private Date modifyTime;
	
	@Readonly
	@Column("true_name")
	private String trueName;
	
	@Readonly
	@Column("org_name")
	private String orgName;
	
	private String moveTrueName;
	private String assetName;
	private String assetNumber;
	private Integer item;
	
	
	public Integer getTransferId() {
		return transferId;
	}
	public void setTransferId(Integer transferId) {
		this.transferId = transferId;
	}
	public Integer getAssetId() {
		return assetId;
	}
	public void setAssetId(Integer assetId) {
		this.assetId = assetId;
	}
	public Integer getTransferPersonId() {
		return transferPersonId;
	}
	public void setTransferPersonId(Integer transferPersonId) {
		this.transferPersonId = transferPersonId;
	}
	public Integer getSignId() {
		return signId;
	}
	public void setSignId(Integer signId) {
		this.signId = signId;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getApprove() {
		return approve;
	}
	public void setApprove(Integer approve) {
		this.approve = approve;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getTransferTime() {
		return transferTime;
	}
	public void setTransferTime(Date transferTime) {
		this.transferTime = transferTime;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
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
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	public String getStoragePlace() {
		return storagePlace;
	}
	public void setStoragePlace(String storagePlace) {
		this.storagePlace = storagePlace;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getMoveTrueName() {
		return moveTrueName;
	}
	public void setMoveTrueName(String moveTrueName) {
		this.moveTrueName = moveTrueName;
	}
	
}
