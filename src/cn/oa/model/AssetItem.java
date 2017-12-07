package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 申购物品
 * @author jiankun.chen
 *
 */
@Table("res_asset_item")
public class AssetItem {
	@Id
	@Column("item_id")
	private Integer itemId;
	@Column("shop_id")
	private Integer shopId;
	@Column("item")
	private Integer item;
	@Column("storage_place")
	private String storagePlace;
	@Column("amount")
	private Integer amount;
	@Column("remark")
	private String remark;
	@Column("item_name")
	private String itemName;
	@Column("unit")
	private String unit;
	@Column("status")
	private Integer status;
	
	public AssetItem(){}
	
	public AssetItem(Integer shopId, Integer item, String storagePlace, Integer amount, String remark, String itemName, String unit,Integer status){
		this.shopId = shopId;
		this.item = item;
		this.storagePlace = storagePlace;
		this.amount = amount;
		this.remark = remark;
		this.itemName = itemName;
		this.unit = unit;
		this.status = status;
	}
	
	public Integer getItemId() {
		return itemId;
	}
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
	public Integer getShopId() {
		return shopId;
	}
	public void setShopId(Integer shopId) {
		this.shopId = shopId;
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
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
