package cn.oa.model.vo;



public class ShopProductSettlementTargetVO {
	private Integer id;
	private Integer shop_product_Settlement_id;
	private String shop_product_name;
	private Integer shop_product_id;
	private String operatorName;
	private Integer operatorId;
	private String targername;
	private Integer targerId;
	private Integer shop_goods_id;
	private String shop_goods_name;
	private float amount;
	private String createTime;
	private Integer type; //0：合作机构 、1：兼职老师
	private String coopType; // 课程开展类型:自营 or 兼职 or 合作
	private Integer gainSharingType; 
	private float gainSharingNum;
	private float lastBalance;
	private Integer couCount;
	
	public Integer getCouCount() {
		return couCount;
	}
	public void setCouCount(Integer couCount) {
		this.couCount = couCount;
	}
	public String getCoopType() {
		return coopType;
	}
	public void setCoopType(String coopType) {
		this.coopType = coopType;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getShop_product_Settlement_id() {
		return shop_product_Settlement_id;
	}
	public void setShop_product_Settlement_id(Integer shop_product_Settlement_id) {
		this.shop_product_Settlement_id = shop_product_Settlement_id;
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
	public String getTargername() {
		return targername;
	}
	public void setTargername(String targername) {
		this.targername = targername;
	}
	public Integer getTargerId() {
		return targerId;
	}
	public void setTargerId(Integer targerId) {
		this.targerId = targerId;
	}
	public Integer getShop_goods_id() {
		return shop_goods_id;
	}
	public void setShop_goods_id(Integer shop_goods_id) {
		this.shop_goods_id = shop_goods_id;
	}
	public String getShop_goods_name() {
		return shop_goods_name;
	}
	public void setShop_goods_name(String shop_goods_name) {
		this.shop_goods_name = shop_goods_name;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getGainSharingType() {
		return gainSharingType;
	}
	public void setGainSharingType(Integer gainSharingType) {
		this.gainSharingType = gainSharingType;
	}
	public float getGainSharingNum() {
		return gainSharingNum;
	}
	public void setGainSharingNum(float gainSharingNum) {
		this.gainSharingNum = gainSharingNum;
	}
	public float getLastBalance() {
		return lastBalance;
	}
	public void setLastBalance(float lastBalance) {
		this.lastBalance = lastBalance;
	}

	
	
}
