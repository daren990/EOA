package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;

import org.nutz.dao.entity.annotation.Table;
@Table("wp_job_time")
public class JobTime {
	@Id
	@Column("time_id")
	private Integer timeId;
	@Column("user_id")
	private Integer userId;
	@Column("end_time")
	private String endTime;
	@Column("type")
	private Integer type;
	
	public Integer getTimeId() {
		return timeId;
	}
	public void setTimeId(Integer timeId) {
		this.timeId = timeId;
	}
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	
}
