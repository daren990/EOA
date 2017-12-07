package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;


@Table("shop_goods_specification")
public class ShopGoodsSpecification {
	
	@Id
	@Column("id")
	private Integer id;
	
	@Column("shop_goods_id")
	private Integer shopGoodsId;
	
	@Column("shop_specification_id")
	private Integer shopSpecificationId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getShopGoodsId() {
		return shopGoodsId;
	}

	public void setShopGoodsId(Integer shopGoodsId) {
		this.shopGoodsId = shopGoodsId;
	}

	public Integer getShopSpecificationId() {
		return shopSpecificationId;
	}

	public void setShopSpecificationId(Integer shopSpecificationId) {
		this.shopSpecificationId = shopSpecificationId;
	}
	
	
	
	
	
}
