package cn.oa.model.vo;

public class ShopProductSettlementVO {

	private Integer id;
	private String shop_product_name;
	private Integer shop_product_id;
	private String operatorName;
	private Integer operatorId;
	private Integer times;
	private Integer crop_id;
	private float amount;
	private float unsubscribe;
	private float expenditure;
	private Integer type;
	private String createTime;
	private String orderids;
	
	public float getUnsubscribe() {
		return unsubscribe;
	}
	public void setUnsubscribe(float unsubscribe) {
		this.unsubscribe = unsubscribe;
	}
	public String getOrderids() {
		return orderids;
	}
	public void setOrderids(String orderids) {
		this.orderids = orderids;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getShop_product_name() {
		return shop_product_name;
	}
	public void setShop_product_name(String shop_product_name) {
		this.shop_product_name = shop_product_name;
	}
	public Integer getShop_product_id() {
		return shop_product_id;
	}
	public void setShop_product_id(Integer shop_product_id) {
		this.shop_product_id = shop_product_id;
	}
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	public Integer getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(Integer operatorId) {
		this.operatorId = operatorId;
	}
	public Integer getTimes() {
		return times;
	}
	public void setTimes(Integer times) {
		this.times = times;
	}
	public Integer getCrop_id() {
		return crop_id;
	}
	public void setCrop_id(Integer crop_id) {
		this.crop_id = crop_id;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public float getExpenditure() {
		return expenditure;
	}
	public void setExpenditure(float expenditure) {
		this.expenditure = expenditure;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	
}
