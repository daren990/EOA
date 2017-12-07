package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 补录历史记录
 * @author SimonTang
 */
@Table("att_redress_record")
public class RedressRecord {
	
	public RedressRecord(){
		
	}

	@Id
	@Column("record_id")
	private Integer recordId;
	@Column("user_id")
	private Integer userId;
	/**
	 * 补录日期
	 */
	@Column("redress_date")
	private String redressDate;
	/**
	 * 补录时间,0:上班，1：下班
	 */
	@Column("redress_time")
	private Integer redressTime;
	/**
	 * 补录原因
	 */
	@Column("redress_desc")
	private String redressDesc;
	/**
	 * 提单者
	 */
	@Column("creator")
	private Integer creator;
	/**
	 * 提单时间
	 */
	@Column("creator_time")
	private Date creatorTime;
	/**
	 * 审批者
	 */
	@Column("approver")
	private Integer approver;
	/**
	 * 审批时间
	 */
	@Column("approve_time")
	private Date approveTime;
	/**
	 * 状态，0：已提交，1：审批通过，-1：审批未通过
	 */
	@Column("status")
	private Integer status;
	/**
	 * 原始打卡时间
	 */
	@Column("checked_time")
	private String checkedTime;
	
	@Readonly
	@Column("true_name")
	private String trueName;
	
	public Integer getRecordId() {
		return recordId;
	}
	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getRedressDate() {
		return redressDate;
	}
	public void setRedressDate(String redressDate) {
		this.redressDate = redressDate;
	}
	public Integer getRedressTime() {
		return redressTime;
	}
	public void setRedressTime(Integer redressTime) {
		this.redressTime = redressTime;
	}
	public String getRedressDesc() {
		return redressDesc;
	}
	public void setRedressDesc(String redressDesc) {
		this.redressDesc = redressDesc;
	}
	public Integer getCreator() {
		return creator;
	}
	public void setCreator(Integer creator) {
		this.creator = creator;
	}
	public Date getCreatorTime() {
		return creatorTime;
	}
	public void setCreatorTime(Date creatorTime) {
		this.creatorTime = creatorTime;
	}
	public Integer getApprover() {
		return approver;
	}
	public void setApprover(Integer approver) {
		this.approver = approver;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getApproveTime() {
		return approveTime;
	}
	public void setApproveTime(Date approveTime) {
		this.approveTime = approveTime;
	}
	public String getCheckedTime() {
		return checkedTime;
	}
	public void setCheckedTime(String checkedTime) {
		this.checkedTime = checkedTime;
	}
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	
	
	public RedressRecord(Integer userId, String redressDate, Integer redressTime, 
			String redressDesc,	Integer creator, Integer approver, String checkedTime) {
		this.userId = userId;
		this.redressDate = redressDate;
		this.redressTime = redressTime;
		this.redressDesc = redressDesc;
		this.creator = creator;
		this.creatorTime = new Date();
		this.approver = approver;
		this.status = 0;
		this.checkedTime = checkedTime;
	}
}
