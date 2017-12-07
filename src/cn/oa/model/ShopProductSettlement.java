package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("shop_product_settlement")
public class ShopProductSettlement {

	@Id
	@Column("id")
	private Integer id;

	@Column("shop_product_id")
	private Integer shop_product_id;
	
	@Column("crop_id")
	private Integer crop_id;
	
	@Column("times")
	private Integer times;
	
	// 1：多个课程、0：单个课程
	@Column("type")
	private Integer type;

	@Column("amount")
	private Float amount;
	
	@Column("unsubscribe")
	private Float unsubscribe;

	@Column("expenditure")
	private Float expenditure;

	@Column("orderids")
	private String orderids;
	
	@Column("operatorId")
	private Integer operatorId;
	
	@Column("createTime")
	private Date createTime;

	public Float getUnsubscribe() {
		return unsubscribe;
	}

	public void setUnsubscribe(Float unsubscribe) {
		this.unsubscribe = unsubscribe;
	}

	public Integer getCrop_id() {
		return crop_id;
	}

	public void setCrop_id(Integer crop_id) {
		this.crop_id = crop_id;
	}

	public Integer getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Integer operatorId) {
		this.operatorId = operatorId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getShop_product_id() {
		return shop_product_id;
	}

	public void setShop_product_id(Integer shop_product_id) {
		this.shop_product_id = shop_product_id;
	}

	public Integer getTimes() {
		return times;
	}

	public void setTimes(Integer times) {
		this.times = times;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	public Float getExpenditure() {
		return expenditure;
	}

	public void setExpenditure(Float expenditure) {
		this.expenditure = expenditure;
	}

	public String getOrderids() {
		return orderids;
	}

	public void setOrderids(String orderids) {
		this.orderids = orderids;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
