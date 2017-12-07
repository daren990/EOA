package cn.oa.model;

import java.io.Serializable;
import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 加班补休
 * @author Administrator
 *
 */
@Table("att_overtime_rest")
public class OvertimeRest implements Serializable{

	private static final long serialVersionUID = 3019523884355043983L;

	@Id
	@Column("id")
	private Integer id;
	/**
	 * 用户Id
	 */
	@Column("userid")
	private Integer userid;
	/**
	 * 类型：加班、补休
	 */
	@Column("apply_type")
	private String type;
	/**
	 * 时长
	 */
	@Column("work_minute")
	private int workMinute;
	/**
	 * 创建时间
	 */
	@Column("crete_date_time")
	private Date creteDateTime;
	
	/**
	 * 
	 */
	@Column("start_time")
	private Date startTime;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getWorkMinute() {
		return workMinute;
	}
	public void setWorkMinute(int workMinute) {
		this.workMinute = workMinute;
	}
	public Date getCreteDateTime() {
		return creteDateTime;
	}
	public void setCreteDateTime(Date creteDateTime) {
		this.creteDateTime = creteDateTime;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	
	
}
