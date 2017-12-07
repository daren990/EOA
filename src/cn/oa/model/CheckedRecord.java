package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 考勤记录.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("att_checked_record_${month}")
@PK({ "userId", "workDate" })
public class CheckedRecord {

	@Column("user_id")
	private Integer userId;
	@Column("work_date")
	private Date workDate;
	@Column("checked_in")
	private String checkedIn;
	@Column("checked_out")
	private String checkedOut;
	@Column("remark_in")
	private String remarkIn;
	@Column("remark_out")
	private String remarkOut;
	@Column("remarked_in")
	private String remarkedIn;
	@Column("remarked_out")
	private String remarkedOut;
	@Column("version")
	private Integer version;
	@Column("modify_id")
	private Integer modifyId;
	@Column("modify_time")
	private Date modifyTime;
	
	private String mforget;
	private String nforget;
	@Readonly
	@Column("job_number")
	private String jobNumber;
	@Readonly
	@Column("true_name")
	private String trueName;
	@Readonly
	@Column("min_in")
	private String minIn;
	@Readonly
	@Column("max_out")
	private String maxOut;
	
	
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

	private Integer minute;

	public Integer getUserId() {
		return userId;
	}

	

	public String getMforget() {
		return mforget;
	}



	public void setMforget(String mforget) {
		this.mforget = mforget;
	}



	public String getNforget() {
		return nforget;
	}



	public void setNforget(String nforget) {
		this.nforget = nforget;
	}



	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Date getWorkDate() {
		return workDate;
	}

	public void setWorkDate(Date workDate) {
		this.workDate = workDate;
	}

	public String getCheckedIn() {
		return checkedIn;
	}

	public void setCheckedIn(String checkedIn) {
		this.checkedIn = checkedIn;
	}

	public String getCheckedOut() {
		return checkedOut;
	}

	public void setCheckedOut(String checkedOut) {
		this.checkedOut = checkedOut;
	}

	public String getRemarkIn() {
		return remarkIn;
	}

	public void setRemarkIn(String remarkIn) {
		this.remarkIn = remarkIn;
	}

	public String getRemarkOut() {
		return remarkOut;
	}

	public void setRemarkOut(String remarkOut) {
		this.remarkOut = remarkOut;
	}

	public String getRemarkedIn() {
		return remarkedIn;
	}

	public void setRemarkedIn(String remarkedIn) {
		this.remarkedIn = remarkedIn;
	}

	public String getRemarkedOut() {
		return remarkedOut;
	}

	public void setRemarkedOut(String remarkedOut) {
		this.remarkedOut = remarkedOut;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getModifyId() {
		return modifyId;
	}

	public void setModifyId(Integer modifyId) {
		this.modifyId = modifyId;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getJobNumber() {
		return jobNumber;
	}

	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}

	public String getTrueName() {
		return trueName;
	}

	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}

	public Integer getMinute() {
		return minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}

}
