package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 打卡记录.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("att_check_record")
public class CheckRecord {

	@Column("job_number")
	private String jobNumber;
	@Column("check_time")
	private Date checkTime;
	@Column("entry_time")
	private Date entryTime;
	@Column("number")
	private Integer number;

	@Readonly
	@Column("user_id")
	private Integer userId;
	@Readonly
	@Column("corp_id")
	private Integer corpId;
	@Readonly
	@Column("day_id")
	private Integer dayId;
	@Readonly
	@Column("week_id")
	private Integer weekId;
	@Readonly
	@Column("min_in")
	private String minIn;
	@Readonly
	@Column("max_out")
	private String maxOut;

	public String getJobNumber() {
		return jobNumber;
	}

	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}

	public Date getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(Date checkTime) {
		this.checkTime = checkTime;
	}

	public Date getEntryTime() {
		return entryTime;
	}

	public void setEntryTime(Date entryTime) {
		this.entryTime = entryTime;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getCorpId() {
		return corpId;
	}

	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

	public Integer getDayId() {
		return dayId;
	}

	public void setDayId(Integer dayId) {
		this.dayId = dayId;
	}

	public Integer getWeekId() {
		return weekId;
	}

	public void setWeekId(Integer weekId) {
		this.weekId = weekId;
	}

	public String getMinIn() {
		return minIn;
	}

	public void setMinIn(String minIn) {
		this.minIn = minIn;
	}

	public String getMaxOut() {
		return maxOut;
	}

	public void setMaxOut(String maxOut) {
		this.maxOut = maxOut;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

}
