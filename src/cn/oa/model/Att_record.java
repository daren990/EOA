package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

@Table("att_record")
public class Att_record {
	
	@Id
	@Column("recordId")
	private Integer recordId;
	
	@Column("type")
	private String type;
	
	@Column("opinion")
	private String opinion;
	
	@Column("modifyTime")
	private Date modifyTime;
	
	@Column("userId")
	private int userId;
	
	@Column("reason")
	private String reason;
	
	@Column("approveId")
	private int approveId;
	
	@Readonly
	@Column("true_name")
	private String trueName;
	
	@Column("date")
	private Date date;
	
	@Column("approve")
	private Integer approve;
	
	@Column("quantum")
	private Integer quantum;
	
	@Column("needYearDay")
	private Date needYearDay;
	
	@Column("subject")
	private String subject;
	
	@Column("status")
	private int status;

	@Column("actorName")
	private String actorName;
	
	
	
	public Integer getQuantum() {
		return quantum;
	}

	public void setQuantum(Integer quantum) {
		this.quantum = quantum;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Integer getRecordId() {
		return recordId;
	}

	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}


	public Integer getApprove() {
		return approve;
	}

	public void setApprove(Integer approve) {
		this.approve = approve;
	}

	public Date getNeedYearDay() {
		return needYearDay;
	}

	public void setNeedYearDay(Date needYearDay) {
		this.needYearDay = needYearDay;
	}
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getApproveId() {
		return approveId;
	}

	public void setApproveId(int approveId) {
		this.approveId = approveId;
	}

	public String getTrueName() {
		return trueName;
	}

	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}
	
	
	public String getActorName() {
		return actorName;
	}

	public void setActorName(String actorName) {
		this.actorName = actorName;
	}

	public Att_record() {
		super();
	}

	public Att_record(String type, int userId, String reason, int approveId,
			Date date, Integer approve, Date needYearDay, String subject,
			int status,Date modifyTime,String actorName,String opinion) {
		super();
		this.type = type;
		this.modifyTime = modifyTime;
		this.userId = userId;
		this.reason = reason;
		this.approveId = approveId;
		this.date = date;
		this.approve = approve;
		this.needYearDay = needYearDay;
		this.subject = subject;
		this.status = status;
		this.actorName = actorName;
		this.opinion = opinion;
	}


	
	
}
