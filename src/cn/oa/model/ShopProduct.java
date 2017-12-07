package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("shop_product")
public class ShopProduct {

	@Id
	@Column("id")
	private Integer id;

	@Column("parent_id")
	private Integer parentId;

	@Column("shop_goods_id")
	private Integer shopGoodsId;

	@Column("corp_id")
	private Integer corpId;

	@Column("name")
	private String name;

	@Column("photo")
	private String photo;

	@Column("location")
	private String location;

	@Column("detail")
	private String detail;

	@Column("status")
	private Integer status;

	@Column("price")
	private Float price;

	@Column("saleType")
	private Integer saleType; // 0：后台列表，1：全部列表

	@Column("onSale")
	private Integer onSale;

	@Column("startTime")
	private Date startTime;

	@Column("endTime")
	private Date endTime;

	@Column("max")
	private Integer max;

	@Column("sold")
	private Integer sold;

	@Column("percent")
	private Integer percent;

	@Column("type")
	private String type;

	@Column("isSettlement")
	private Integer isSettlement;

	public Integer getIsSettlement() {
		return isSettlement;
	}

	public void setIsSettlement(Integer isSettlement) {
		this.isSettlement = isSettlement;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getSaleType() {
		return saleType;
	}

	public void setSaleType(Integer saleType) {
		this.saleType = saleType;
	}

	public Integer getOnSale() {
		return onSale;
	}

	public void setOnSale(Integer onSale) {
		this.onSale = onSale;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

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

	public Integer getCorpId() {
		return corpId;
	}

	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public Integer getSold() {
		return sold;
	}

	public void setSold(Integer sold) {
		this.sold = sold;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getPercent() {
		return percent;
	}

	public void setPercent(Integer percent) {
		this.percent = percent;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ShopProduct [id=" + id + ", parentId=" + parentId + ", shopGoodsId=" + shopGoodsId + ", corpId="
				+ corpId + ", name=" + name + ", photo=" + photo + ", location=" + location + ", detail=" + detail
				+ ", status=" + status + ", price=" + price + ", saleType=" + saleType + ", onSale=" + onSale
				+ ", startTime=" + startTime + ", endTime=" + endTime + ", max=" + max + ", sold=" + sold + ", percent="
				+ percent + ", type=" + type + ", isSettlement=" + isSettlement + "]";
	}

}
