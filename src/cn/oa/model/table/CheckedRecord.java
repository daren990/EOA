package cn.oa.model.table;

import java.util.Date;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
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
	@ColDefine(width = 11)
	private Integer userId;
	@Column("work_date")
	@ColDefine(type = ColType.DATE)
	private Date workDate;
	@Column("checked_in")
	@ColDefine(width = 5)
	private String checkedIn;
	@Column("checked_out")
	@ColDefine(width = 5)
	private String checkedOut;
	@Column("remark_in")
	@ColDefine(width = 20)
	private String remarkIn;
	@Column("remark_out")
	@ColDefine(width = 20)
	private String remarkOut;
	@Column("remarked_in")
	@ColDefine(width = 20)
	private String remarkedIn;
	@Column("remarked_out")
	@ColDefine(width = 20)
	private String remarkedOut;
	@Column("version")
	@ColDefine(width = 1)
	private Integer version;
	@Column("modify_id")
	@ColDefine(width = 11)
	private Integer modifyId;
	@Column("modify_time")
	private Date modifyTime;

	public Integer getUserId() {
		return userId;
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

}
