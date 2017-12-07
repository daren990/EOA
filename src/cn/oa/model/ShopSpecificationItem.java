package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("shop_specification_item")
public class ShopSpecificationItem {
	
	@Id
	@Column("id")
	private Integer id;
	
	@Column("shop_specification_id")
	private Integer shopSpecificationId;

	@Column("name")
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getShopSpecificationId() {
		return shopSpecificationId;
	}

	public void setShopSpecificationId(Integer shopSpecificationId) {
		this.shopSpecificationId = shopSpecificationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

}
