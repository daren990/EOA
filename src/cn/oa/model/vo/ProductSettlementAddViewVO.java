package cn.oa.model.vo;

import java.util.List;

import cn.oa.model.ShopProduct;

public class ProductSettlementAddViewVO {

	ShopProduct sp;	//商品
	Integer times;	//第几次结算
	float amount;	//本次结算金额
	float unsubscribe;	//本次结算退费金额
	String orderids;	//本次结算订单id
	int effective_orders;	//本次结算有效订单数
	float temp_amount;	//(总收入-总支出)
	float expenditure;	//本次结算支出金额
	String operatorName;	//操作人名
	Integer operatorId;		//操作人ID
	List<WxShopOrderVO> ovoList;	//订单数据
	List<ShopOrderUnsubscribeVO> uvoList;	//退订数据
	List<ShopProductSettlementTargetVO> targetList;	//结算目标数据
	
	public float getExpenditure() {
		return expenditure;
	}

	public void setExpenditure(float expenditure) {
		this.expenditure = expenditure;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public float getUnsubscribe() {
		return unsubscribe;
	}

	public void setUnsubscribe(float unsubscribe) {
		this.unsubscribe = unsubscribe;
	}

	public int getEffective_orders() {
		return effective_orders;
	}

	public void setEffective_orders(int effective_orders) {
		this.effective_orders = effective_orders;
	}

	public float getTemp_amount() {
		return temp_amount;
	}

	public void setTemp_amount(float temp_amount) {
		this.temp_amount = temp_amount;
	}

	public String getOrderids() {
		return orderids;
	}

	public void setOrderids(String orderids) {
		this.orderids = orderids;
	}

	public ShopProduct getSp() {
		return sp;
	}

	public void setSp(ShopProduct sp) {
		this.sp = sp;
	}

	public Integer getTimes() {
		return times;
	}

	public void setTimes(Integer times) {
		this.times = times;
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

	public List<WxShopOrderVO> getOvoList() {
		return ovoList;
	}

	public void setOvoList(List<WxShopOrderVO> ovoList) {
		this.ovoList = ovoList;
	}

	public List<ShopOrderUnsubscribeVO> getUvoList() {
		return uvoList;
	}

	public void setUvoList(List<ShopOrderUnsubscribeVO> uvoList) {
		this.uvoList = uvoList;
	}

	public List<ShopProductSettlementTargetVO> getTargetList() {
		return targetList;
	}

	public void setTargetList(List<ShopProductSettlementTargetVO> targetList) {
		this.targetList = targetList;
	}
}
