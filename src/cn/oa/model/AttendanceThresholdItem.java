package cn.oa.model;



import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;
/**
 * 考勤阀值
 * @author jiankun.chen
 *
 */
@Table("conf_attendance_threshold_Item")
public class AttendanceThresholdItem {
	@Id
	@Column("item_id")
	private Integer itemId;
	@Column("threshold_id")
	private Integer thresholdId;
	@Column("type")
	private Integer type;
	@Column("minuteStart")
	private Integer minuteStart;
	@Column("minuteEnd")
	private Integer minuteEnd;
	@Column("amountStart")
	private Integer amountStart;
	@Column("amountEnd")
	private Integer amountEnd;
	@Column("way")
	private Integer way;
	@Column("unit")
	private Integer unit;
	@Column("wageType")
	private Integer wageType;
	@Column("deduct")
	private Double deduct;
	@Column("status")
	private Integer status;
	
	public AttendanceThresholdItem(){}
	public AttendanceThresholdItem(Integer type,Integer minuteStart,Integer minuteEnd,Integer amountStart,Integer amountEnd,Integer way,Integer unit,Integer wageType,Double deduct,Integer status){
		this.amountEnd = amountEnd;
		this.amountStart = amountStart;
		this.minuteEnd = minuteEnd;
		this.minuteStart = minuteStart;
		this.way = way;
		this.wageType = wageType;
		this.unit = unit;
		this.type = type;
		this.deduct = deduct;
	}
	public Integer getThresholdId() {
		return thresholdId;
	}
	public void setThresholdId(Integer thresholdId) {
		this.thresholdId = thresholdId;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getMinuteStart() {
		return minuteStart;
	}
	public void setMinuteStart(Integer minuteStart) {
		this.minuteStart = minuteStart;
	}
	public Integer getMinuteEnd() {
		return minuteEnd;
	}
	public void setMinuteEnd(Integer minuteEnd) {
		this.minuteEnd = minuteEnd;
	}
	public Integer getAmountStart() {
		return amountStart;
	}
	public void setAmountStart(Integer amountStart) {
		this.amountStart = amountStart;
	}
	public Integer getAmountEnd() {
		return amountEnd;
	}
	public void setAmountEnd(Integer amountEnd) {
		this.amountEnd = amountEnd;
	}
	public Integer getWay() {
		return way;
	}
	public void setWay(Integer way) {
		this.way = way;
	}
	public Integer getUnit() {
		return unit;
	}
	public void setUnit(Integer unit) {
		this.unit = unit;
	}
	public Integer getWageType() {
		return wageType;
	}
	public void setWageType(Integer wageType) {
		this.wageType = wageType;
	}
	public Integer getItemId() {
		return itemId;
	}
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
	public Double getDeduct() {
		return deduct;
	}
	public void setDeduct(Double deduct) {
		this.deduct = deduct;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
