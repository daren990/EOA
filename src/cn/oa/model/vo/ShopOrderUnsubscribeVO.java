package cn.oa.model.vo;

public class ShopOrderUnsubscribeVO {

	private Integer id;
	private Integer shop_order_id;
	private String title;
	private Integer crop_id;
	private Integer unsubscribeType;
	private Integer isAll;
	private String remark;
	private float amount;
	private String operatorName;
	private Integer operatorId;
	private String createTime;
	public Integer getIsAll() {
		return isAll;
	}
	public void setIsAll(Integer isAll) {
		this.isAll = isAll;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getShop_order_id() {
		return shop_order_id;
	}
	public void setShop_order_id(Integer shop_order_id) {
		this.shop_order_id = shop_order_id;
	}
	public Integer getCrop_id() {
		return crop_id;
	}
	public void setCrop_id(Integer crop_id) {
		this.crop_id = crop_id;
	}
	public Integer getUnsubscribeType() {
		return unsubscribeType;
	}
	public void setUnsubscribeType(Integer unsubscribeType) {
		this.unsubscribeType = unsubscribeType;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
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
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
}
