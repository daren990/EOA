package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("conf_leave_type")
public class ConfLeaveType {
	@Id
	@Column("type_id")
	private Integer typeId;
	@Column("conf_leave_id")
	private Integer confLeaveId;
	@Column("leave_type")
	private Integer leaveType;
	@Column("way")
	private Integer way;
	@Column("day_amount")
	private Integer dayAmount;
	@Column("multiplication")
	private Double multiplication;
	
	public ConfLeaveType(){}
	
	public ConfLeaveType(Integer confLeaveId, Integer leaveType, Integer way,
			Integer dayAmount, Double multiplication) {
		this.confLeaveId = confLeaveId;
		this.leaveType = leaveType;
		this.way = way;
		this.dayAmount = dayAmount;
		this.multiplication = multiplication;
	}
	
	public Integer getTypeId() {
		return typeId;
	}
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	public Integer getLeaveType() {
		return leaveType;
	}
	public void setLeaveType(Integer leaveType) {
		this.leaveType = leaveType;
	}
	public Integer getWay() {
		return way;
	}
	public void setWay(Integer way) {
		this.way = way;
	}
	public Integer getDayAmount() {
		return dayAmount;
	}
	public void setDayAmount(Integer dayAmount) {
		this.dayAmount = dayAmount;
	}

	public Integer getConfLeaveId() {
		return confLeaveId;
	}

	public void setConfLeaveId(Integer confLeaveId) {
		this.confLeaveId = confLeaveId;
	}

	public Double getMultiplication() {
		return multiplication;
	}

	public void setMultiplication(Double multiplication) {
		this.multiplication = multiplication;
	}
	
}
