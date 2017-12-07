package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 
 * @author jiankun.chen
 * 报销类型
 */
@Table("conf_reimburse_type")
public class ReimburseType {
	@Id
	@Column("reimburseType_id")
	private Integer reimburseTypeId;
	@Column("big_type")
	private String bigType;
	@Column("small_type")
	private String smallType;
	@Column("status")
	private Integer status;

	public String getBigType() {
		return bigType;
	}
	public void setBigType(String bigType) {
		this.bigType = bigType;
	}
	public String getSmallType() {
		return smallType;
	}
	public void setSmallType(String smallType) {
		this.smallType = smallType;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getReimburseTypeId() {
		return reimburseTypeId;
	}
	public void setReimburseTypeId(Integer reimburseTypeId) {
		this.reimburseTypeId = reimburseTypeId;
	}
	
}
