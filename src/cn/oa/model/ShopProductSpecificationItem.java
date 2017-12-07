package cn.oa.model;


import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;


@Table("shop_product_specification_item")
public class ShopProductSpecificationItem {
	
	@Id
	@Column("id")
	private Integer id;
	
	@Column("shop_product_id")
	private Integer shopProductId;
	
	@Column("shop_specification_item_id")
	private Integer shopSpecificationItemId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getShopProductId() {
		return shopProductId;
	}

	public void setShopProductId(Integer shopProductId) {
		this.shopProductId = shopProductId;
	}

	public Integer getShopSpecificationItemId() {
		return shopSpecificationItemId;
	}

	public void setShopSpecificationItemId(Integer shopSpecificationItemId) {
		this.shopSpecificationItemId = shopSpecificationItemId;
	}
	
}
