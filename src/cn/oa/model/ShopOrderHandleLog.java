package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("shop_order_handle_log")
public class ShopOrderHandleLog {

	@Id
	@Column("id")
	private Integer id;
	
	@Column("shop_order_id")
	private Integer shop_order_id;
	
	@Column("crop_id")
	private Integer crop_id;
	
	@Column("handleType")
	private Integer handleType;
	
	@Column("remark")
	private String remark;
	
	@Column("operatorId")
	private Integer operatorId;
	
	@Column("createTime")
	private Date createTime;

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

	public Integer getHandleType() {
		return handleType;
	}

	public void setHandleType(Integer handleType) {
		this.handleType = handleType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Integer operatorId) {
		this.operatorId = operatorId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
