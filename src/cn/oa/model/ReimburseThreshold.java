package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 董事长报销审批阀值
 * @author jiankun.chen
 *
 */
@Table("conf_reimburse_threshold")
public class ReimburseThreshold {
	@Id
	@Column("threshold_id")
	private Integer thresholdId;
	@Column("threshold_value")
	private Integer thresholdValue;
	@Column("threshold_name")
	private String thresholdName;
	
	public Integer getThresholdId() {
		return thresholdId;
	}
	public void setThresholdId(Integer thresholdId) {
		this.thresholdId = thresholdId;
	}
	public Integer getThresholdValue() {
		return thresholdValue;
	}
	public void setThresholdValue(Integer thresholdValue) {
		this.thresholdValue = thresholdValue;
	}
	public String getThresholdName() {
		return thresholdName;
	}
	public void setThresholdName(String thresholdName) {
		this.thresholdName = thresholdName;
	}
	
}
