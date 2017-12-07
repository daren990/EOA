package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("shop_pay_order")
public class ShopPayOrder {
	
	@Id
	@Column("id")
	private Integer id;

	@Column("amount")
	private float amount;
	
	@Column("order_id")
	private Integer order_id;
	
	@Column("finish_time")
	private Date finish_time;
	
	@Column("status")
	private Integer status;
	
	@Column("per_pay_time")
	private Date per_pay_time;
	
	@Column("pay_id")
	private String pay_id;
	
	@Column("transaction_id")
	private String transaction_id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public Integer getOrder_id() {
		return order_id;
	}

	public void setOrder_id(Integer order_id) {
		this.order_id = order_id;
	}

	public Date getFinish_time() {
		return finish_time;
	}

	public void setFinish_time(Date finish_time) {
		this.finish_time = finish_time;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getPer_pay_time() {
		return per_pay_time;
	}

	public void setPer_pay_time(Date per_pay_time) {
		this.per_pay_time = per_pay_time;
	}

	public String getPay_id() {
		return pay_id;
	}

	public void setPay_id(String pay_id) {
		this.pay_id = pay_id;
	}

	public String getTransaction_id() {
		return transaction_id;
	}

	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
}
