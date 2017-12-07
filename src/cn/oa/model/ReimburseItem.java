package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;
@Table("epn_reimburse_item")
public class ReimburseItem {
	@Id
	@Column("item_id")
	private Integer itemId;
	@Column("reimburse_id")
	private Integer reimburseId;
	@Column("bigType")
	private Integer bigType;
	@Column("money")
	private Integer money;
	@Column("bill")
	private Integer bill;
	@Column("purpose")
	private String purpose;
	@Column("smallType")
	private Integer smallType;
	
	private String smallName;
	
	private String bigName;
	
	public ReimburseItem(){};
	
	public ReimburseItem(Integer reimburseId, Integer bigType, Integer money, Integer bill, String purpose,Integer smallType){
		this.reimburseId = reimburseId;
		this.bigType = bigType;
		this.money = money;
		this.bill = bill;
		this.purpose = purpose;
		this.smallType = smallType;
	}
	
	public Integer getItemId() {
		return itemId;
	}
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
	public Integer getReimburseId() {
		return reimburseId;
	}
	public void setReimburseId(Integer reimburseId) {
		this.reimburseId = reimburseId;
	}
	public Integer getMoney() {
		return money;
	}
	public void setMoney(Integer money) {
		this.money = money;
	}
	public Integer getBill() {
		return bill;
	}
	public void setBill(Integer bill) {
		this.bill = bill;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public Integer getBigType() {
		return bigType;
	}

	public void setBigType(Integer bigType) {
		this.bigType = bigType;
	}

	public Integer getSmallType() {
		return smallType;
	}

	public void setSmallType(Integer smallType) {
		this.smallType = smallType;
	}

	public String getSmallName() {
		return smallName;
	}

	public void setSmallName(String smallName) {
		this.smallName = smallName;
	}

	public String getBigName() {
		return bigName;
	}

	public void setBigName(String bigName) {
		this.bigName = bigName;
	}
	
}
