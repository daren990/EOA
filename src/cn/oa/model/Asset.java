package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 资产.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("res_asset")
public class Asset {

	@Id
	@Column("asset_id")
	private Integer assetId;
	@Column("asset_number")
	private String assetNumber;
	@Column("asset_name")
	private String assetName;
	@Column("type_id")
	private Integer typeId;
	@Column("unit")
	private String unit;
	@Column("price")
	private Integer price;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("shop_time")
	private Date shopTime;
	@Column("storage_place")
	private String storagePlace;
	@Column("plan_age")
	private Date planAge ;
	@Column("user_id")
	private Integer userId;
	@Column("state")
	private Integer state;
	
	//model资产型号,custodian保管人
	@Column("model")
	private String model;
	@Column("custodian")
	private String custodian;
	
	@Readonly
	@Column("true_name")
	private String trueName;
	
	@Readonly
	@Column("org_name")
	private String orgName;
	
	@Readonly
	@Column("org_id")
	private Integer orgId;
	
	private Integer amount;
	

	@Readonly
	@Column("last_amount")
	private Integer lastAmount;

	public Integer getAssetId() {
		return assetId;
	}

	public void setAssetId(Integer assetId) {
		this.assetId = assetId;
	}

	public String getAssetNumber() {
		return assetNumber;
	}

	public void setAssetNumber(String assetNumber) {
		this.assetNumber = assetNumber;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
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

	public Integer getLastAmount() {
		return lastAmount;
	}

	public void setLastAmount(Integer lastAmount) {
		this.lastAmount = lastAmount;
	}

	public Date getShopTime() {
		return shopTime;
	}

	public void setShopTime(Date shopTime) {
		this.shopTime = shopTime;
	}

	public String getStoragePlace() {
		return storagePlace;
	}

	public void setStoragePlace(String storagePlace) {
		this.storagePlace = storagePlace;
	}

	public Date getPlanAge() {
		return planAge;
	}

	public void setPlanAge(Date planAge) {
		this.planAge = planAge;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
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

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getCustodian() {
		return custodian;
	}

	public void setCustodian(String custodian) {
		this.custodian = custodian;
	}

	
}
