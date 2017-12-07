package cn.oa.model;


import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;


@Table("shop_order_item")
public class ShopOrderItem {
	
	@Id
	@Column("id")
	private Integer id;
	
	@Column("shop_order_id")
	private Integer shopOrderId;
	
	@Column("shop_product_id")
	private Integer shopProductId;
	
	@Column("amount")
	private Float amount;
	
	@Column("Integer")
	private Integer quantity;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getShopOrderId() {
		return shopOrderId;
	}

	public void setShopOrderId(Integer shopOrderId) {
		this.shopOrderId = shopOrderId;
	}

	public Integer getShopProductId() {
		return shopProductId;
	}

	public void setShopProductId(Integer shopProductId) {
		this.shopProductId = shopProductId;
	}

	public Float getAmount() {
		return amount;
	}

	public void setAmount(Float amount) {
		this.amount = amount;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	
	
	
	
}
