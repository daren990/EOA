package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("shop_order_unsubscribe")
public class ShopOrderUnsubscribe {

	@Id
	@Column("id")
	private Integer id;
	
	@Column("shop_order_id")
	private Integer shop_order_id;
	
	@Column("crop_id")
	private Integer crop_id;
	
	@Column("unsubscribeType")
	private Integer unsubscribeType;
	
	@Column("isAll")
	private Integer isAll;
	
	@Column("remark")
	private String remark;
	
	@Column("amount")
	private float amount;
	
	@Column("operatorId")
	private Integer operatorId;
	
	@Column("createTime")
	private Date createTime;

	public Integer getIsAll() {
		return isAll;
	}

	public void setIsAll(Integer isAll) {
		this.isAll = isAll;
	}

	public Integer getCrop_id() {
		return crop_id;
	}

	public void setCrop_id(Integer crop_id) {
		this.crop_id = crop_id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getShop_order_id() {
		return shop_order_id;
	}

	public void setShop_order_id(Integer shop_order_id) {
		this.shop_order_id = shop_order_id;
	}

	public Integer getUnsubscribeType() {
		return unsubscribeType;
	}

	public void setUnsubscribeType(Integer unsubscribeType) {
		this.unsubscribeType = unsubscribeType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public Integer getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Integer operatorId) {
		this.operatorId = operatorId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
