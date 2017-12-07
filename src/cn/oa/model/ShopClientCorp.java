package cn.oa.model;


import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;


@Table("shop_client_corp")
public class ShopClientCorp {
	
	@Id
	@Column("id")
	private Integer id;
	
	@Column("shop_client_id")
	private Integer shopClientId;
	
	@Column("corp_id")
	private Integer corpId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getShopClientId() {
		return shopClientId;
	}

	public void setShopClientId(Integer shopClientId) {
		this.shopClientId = shopClientId;
	}

	public Integer getCorpId() {
		return corpId;
	}

	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}
	
	


}
