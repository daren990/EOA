package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 年休假.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("att_annual_leave")
public class Annual {

	@Id
	@Column("annual_id")
	private Integer holidayId;
	@Column("user_id")
	private Integer userId;
	@Column("start_date")
	private Date startDate;
	@Column("end_date")
	private Date endDate;
	@Column("sum_minute")
	private Integer sumMinute;
	@Column("work_minute")
	private Integer workMinute;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;

	private Integer lastMinute;

	public Integer getHolidayId() {
		return holidayId;
	}

	public void setHolidayId(Integer holidayId) {
		this.holidayId = holidayId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
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

	public Integer getSumMinute() {
		return sumMinute;
	}

	public void setSumMinute(Integer sumMinute) {
		this.sumMinute = sumMinute;
	}

	public Integer getWorkMinute() {
		return workMinute;
	}

	public void setWorkMinute(Integer workMinute) {
		this.workMinute = workMinute;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Integer getLastMinute() {
		return lastMinute;
	}

	public void setLastMinute(Integer lastMinute) {
		this.lastMinute = lastMinute;
	}

}
