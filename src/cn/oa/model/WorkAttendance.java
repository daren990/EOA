package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Table;

/**
 * 最近考勤汇总周期
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("conf_work_attendance")
@PK({ "orgId" })
public class WorkAttendance {

	@Column("org_id")
	private Integer orgId;
	@Column("start_date")
	private Date startDate;
	@Column("end_date")
	private Date endDate;

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
