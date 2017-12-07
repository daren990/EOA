package cn.oa.model.vo;

import java.util.Date;
import java.util.List;

public class WxShopProductVO {

	private Integer id;
	private String name;
	private List<WxShopProductVO> childList;
	private String detail;
	private String location;
	
	private Float price;
	
	private Integer onSale;
	private Date startTime;
	private Date endTime;
	private String st;
	private String et;
	
	private Integer max;
	private Integer sold;
	
	private Integer goods_id;
	private WxShopGoodsVO sgvo;
	
	public Integer getGoods_id() {
		return goods_id;
	}
	public void setGoods_id(Integer goods_id) {
		this.goods_id = goods_id;
	}
	public WxShopGoodsVO getSgvo() {
		return sgvo;
	}
	public void setSgvo(WxShopGoodsVO sgvo) {
		this.sgvo = sgvo;
	}
	public List<WxShopProductVO> getChildList() {
		return childList;
	}
	public void setChildList(List<WxShopProductVO> childList) {
		this.childList = childList;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
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
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getSt() {
		return st;
	}
	public void setSt(String st) {
		this.st = st;
	}
	public String getEt() {
		return et;
	}
	public void setEt(String et) {
		this.et = et;
	}
	
	
}
