package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("shop_product_settlement_teacher")
public class ShopProductSettlementTeacher {

	@Id
	@Column("id")
	private Integer id;

	@Column("shop_product_Settlement_id")
	private Integer shop_product_Settlement_id;
	
	@Column("teacherId")
	private Integer teacherId;
	
	@Column("shop_product_Id")
	private Integer shop_product_Id;
	
	@Column("shop_goods_id")
	private Integer shop_goods_id;

	@Column("amount")
	private Float amount;
	
	@Column("operatorId")
	private Integer operatorId;

	@Column("createTime")
	private Date createTime;
	
	@Column("lastBalance")
	private Float lastBalance;
	

	public Float getLastBalance() {
		return lastBalance;
	}

	public void setLastBalance(Float lastBalance) {
		this.lastBalance = lastBalance;
	}

	public Integer getShop_goods_id() {
		return shop_goods_id;
	}

	public void setShop_goods_id(Integer shop_goods_id) {
		this.shop_goods_id = shop_goods_id;
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

	public Integer getShop_product_Settlement_id() {
		return shop_product_Settlement_id;
	}

	public void setShop_product_Settlement_id(Integer shop_product_Settlement_id) {
		this.shop_product_Settlement_id = shop_product_Settlement_id;
	}

	public Integer getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Integer teacherId) {
		this.teacherId = teacherId;
	}

	public Integer getShop_product_Id() {
		return shop_product_Id;
	}

	public void setShop_product_Id(Integer shop_product_Id) {
		this.shop_product_Id = shop_product_Id;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
