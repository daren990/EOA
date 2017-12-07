package cn.oa.model.vo;

public class ShopProductSettlementCountVO {
	
	private Integer shop_product_id;
	private String shop_product_name;
	private float count_amount;
	private float count_expenditure;
	private Integer count_times;
	private String count_operatorName;
	
	private String[] orderTimes;
	private String[] amounts;
	private String[] unsubscribes;
	private String[] expenditures;
	
	public String[] getUnsubscribes() {
		return unsubscribes;
	}
	public void setUnsubscribes(String[] unsubscribes) {
		this.unsubscribes = unsubscribes;
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
	public float getCount_amount() {
		return count_amount;
	}
	public void setCount_amount(float count_amount) {
		this.count_amount = count_amount;
	}
	public float getCount_expenditure() {
		return count_expenditure;
	}
	public void setCount_expenditure(float count_expenditure) {
		this.count_expenditure = count_expenditure;
	}
	public Integer getCount_times() {
		return count_times;
	}
	public void setCount_times(Integer count_times) {
		this.count_times = count_times;
	}
	public String getCount_operatorName() {
		return count_operatorName;
	}
	public void setCount_operatorName(String count_operatorName) {
		this.count_operatorName = count_operatorName;
	}
	public String[] getOrderTimes() {
		return orderTimes;
	}
	public void setOrderTimes(String[] orderTimes) {
		this.orderTimes = orderTimes;
	}
	public String[] getAmounts() {
		return amounts;
	}
	public void setAmounts(String[] strings) {
		this.amounts = strings;
	}
	public String[] getExpenditures() {
		return expenditures;
	}
	public void setExpenditures(String[] expenditures) {
		this.expenditures = expenditures;
	}
}
