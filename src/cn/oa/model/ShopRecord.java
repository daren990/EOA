package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("shop_record")
public class ShopRecord {

	@Id
	@Column("id")
	private Integer id;

	@Column("customer")
	private String customer;

	@Column("customer_id")
	private Integer customerId;

	@Column("product")
	private String product;

	@Column("product_id")
	private Integer productId;

	@Column("phone")
	private String phone;

	@Column("price")
	private float price;

	@Column("pattype")
	private String payType;

	@Column("payee")
	private String payee;

	@Column("addTime")
	private Date addTime;

	@Column("isone")
	private String isone; // 是否一对一

	@Column("needbook")
	private Date needbook; // 是否需要教辅资料

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public Integer getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getPayee() {
		return payee;
	}

	public void setPayee(String payee) {
		this.payee = payee;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public String getIsone() {
		return isone;
	}

	public void setIsone(String isone) {
		this.isone = isone;
	}

	public Date getNeedbook() {
		return needbook;
	}

	public void setNeedbook(Date needbook) {
		this.needbook = needbook;
	}

	@Override
	public String toString() {
		return "ShopRecord [id=" + id + ", customer=" + customer + ", customerId=" + customerId + ", product=" + product
				+ ", productId=" + productId + ", phone=" + phone + ", price=" + price + ", payType=" + payType
				+ ", payee=" + payee + ", addTime=" + addTime + "]";
	}

}
