package cn.oa.model;


import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;



@Table("shop_order")
public class ShopOrder {
	
	@Id
	@Column("id")
	private Integer id;
	
	@Column("edu_student_id")
	private Integer edu_student_id;
	
	@Column("shop_client_id")
	private Integer shopClientId;
	
	@Column("pay_shop_client_id")
	private Integer payShopClientId;
	
	@Column("shop_product_id")
	private Integer shop_product_id;
	
	@Column("order_status")
	private Integer orderStatus;

	@Column("pay_status")
	private Integer payStatus;
	
	@Column("crop_id")
	private Integer crop_id;
	
	@Column("pay_id")
	private String pay_id;
	
	@Column("amount")
	private Float amount;
	
	@Column("create_time")
	private Date create_time;
	
	@Column("isSettlement")
	private Integer isSettlement;
	
	@Column("isUnsubscribe")
	private Integer isUnsubscribe;
	
	@Column("isMemberPay")
	private Integer isMemberPay;
	
	@Column("payMemberLevel")
	private Integer payMemberLevel;
	
	public Integer getPayShopClientId() {
		return payShopClientId;
	}

	public void setPayShopClientId(Integer payShopClientId) {
		this.payShopClientId = payShopClientId;
	}

	public Integer getIsSettlement() {
		return isSettlement;
	}

	public void setIsSettlement(Integer isSettlement) {
		this.isSettlement = isSettlement;
	}

	public Integer getIsMemberPay() {
		return isMemberPay;
	}

	public void setIsMemberPay(Integer isMemberPay) {
		this.isMemberPay = isMemberPay;
	}

	public Integer getPayMemberLevel() {
		return payMemberLevel;
	}

	public void setPayMemberLevel(Integer payMemberLevel) {
		this.payMemberLevel = payMemberLevel;
	}

	public Integer getIsUnsubscribe() {
		return isUnsubscribe;
	}

	public void setIsUnsubscribe(Integer isUnsubscribe) {
		this.isUnsubscribe = isUnsubscribe;
	}

	public Integer getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(Integer payStatus) {
		this.payStatus = payStatus;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public Integer getCrop_id() {
		return crop_id;
	}

	public void setCrop_id(Integer crop_id) {
		this.crop_id = crop_id;
	}

	public String getPay_id() {
		return pay_id;
	}

	public void setPay_id(String pay_id) {
		this.pay_id = pay_id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEdu_student_id() {
		return edu_student_id;
	}

	public void setEdu_student_id(Integer edu_student_id) {
		this.edu_student_id = edu_student_id;
	}

	public Integer getShopClientId() {
		return shopClientId;
	}

	public void setShopClientId(Integer shopClientId) {
		this.shopClientId = shopClientId;
	}

	public Integer getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Integer orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	public Integer getShop_product_id() {
		return shop_product_id;
	}

	public void setShop_product_id(Integer shop_product_id) {
		this.shop_product_id = shop_product_id;
	}
	
}
