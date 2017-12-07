package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 对应表格sec_org_leave，orderId为公司编号，
 * valid为0则代表通常请假模式
 * 		为1则代表必须以半天为单位模式
 * @author Administrator
 *
 */
@Table("sec_org_leave")
public class LeaveType {
	
	/**公司id*/
	@Column("org_id")
	private Integer orderId;
	/**请假类型id*/
	@Column("leave_type_id")
	private Integer leaveTypeId;
	/**该公司该类型请假状态信息*/
	@Column("valid")
	private Integer valid;
	
	public Integer getOrderId() {
		return orderId;
	}
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	public Integer getLeaveTypeId() {
		return leaveTypeId;
	}
	public void setLeaveTypeId(Integer leaveTypeId) {
		this.leaveTypeId = leaveTypeId;
	}
	public Integer getValid() {
		return valid;
	}
	public void setValid(Integer valid) {
		this.valid = valid;
	}
	
	@Override
	public String toString() {
		return "LeaveType [orderId=" + orderId + ", leaveTypeId=" + leaveTypeId + ", valid=" + valid + "]";
	}
	
}
