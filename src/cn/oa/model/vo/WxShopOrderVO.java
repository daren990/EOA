package cn.oa.model.vo;




public class WxShopOrderVO {

	private Integer id;
	private Integer edu_student_id;
	private String edu_student_name;
	private Integer shop_product_id;
	private String shop_product_name;
	private Integer shop_client_id;
	private String shop_client_name;
	private String create_time;
	private Integer orderStatus;
	private Integer payStatus;
	private Float amount;
	private Float shop_product_price;
	private Integer isUnsubscribe;
	private Integer isSettlement;
	
	public Integer getIsSettlement() {
		return isSettlement;
	}
	public void setIsSettlement(Integer isSettlement) {
		this.isSettlement = isSettlement;
	}
	public Integer getIsUnsubscribe() {
		return isUnsubscribe;
	}
	public void setIsUnsubscribe(Integer isUnsubscribe) {
		this.isUnsubscribe = isUnsubscribe;
	}
	public Integer getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(Integer orderStatus) {
		this.orderStatus = orderStatus;
	}
	public Integer getPayStatus() {
		return payStatus;
	}
	public void setPayStatus(Integer payStatus) {
		this.payStatus = payStatus;
	}
	public Integer getShop_client_id() {
		return shop_client_id;
	}
	public void setShop_client_id(Integer shop_client_id) {
		this.shop_client_id = shop_client_id;
	}
	public String getShop_client_name() {
		return shop_client_name;
	}
	public void setShop_client_name(String shop_client_name) {
		this.shop_client_name = shop_client_name;
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getEdu_student_id() {
		return edu_student_id;
	}
	public void setEdu_student_id(Integer edu_student_id) {
		this.edu_student_id = edu_student_id;
	}
	public String getEdu_student_name() {
		return edu_student_name;
	}
	public void setEdu_student_name(String edu_student_name) {
		this.edu_student_name = edu_student_name;
	}
	public Integer getShop_product_id() {
		return shop_product_id;
	}
	public void setShop_product_id(Integer shop_product_id) {
		this.shop_product_id = shop_product_id;
	}
	public String getShop_product_name() {
		return shop_product_name;
	}
	public void setShop_product_name(String shop_product_name) {
		this.shop_product_name = shop_product_name;
	}
	public Float getAmount() {
		return amount;
	}
	public void setAmount(Float amount) {
		this.amount = amount;
	}
	public Float getShop_product_price() {
		return shop_product_price;
	}
	public void setShop_product_price(Float shop_product_price) {
		this.shop_product_price = shop_product_price;
	}
}
