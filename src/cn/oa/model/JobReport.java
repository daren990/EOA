package cn.oa.model;
import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;

import org.nutz.dao.entity.annotation.Table;
@Table("wp_job_report")
public class JobReport {
	@Id
	@Column("report_id")
	private Integer reportId;
	@Column("user_id")
	private Integer userId;
	@Column("title")
	private String title;
	@Column("type")
	private Integer type;
	@Column("start_date")
	private String startDate;
	@Column("end_date")
	private String endDate;
	@Column("content1")
	private String content1;
	@Column("content2")
	private String content2;
	@Column("summary")
	private String summary;
	@Column("create_Time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("approve_actor")
	private Integer approveActor;
	@Column("approve")
	private Integer approve;
	@Column("submit")
	private String submit;
	@Column("share_number")
	private Integer shareNumber;
	
	private String yearMonth;
	
	@Readonly
	@Column("true_name")
	private String trueName;
	
	@Readonly
	@Column("corp_name")
	private String corpName;
	
	@Readonly
	@Column("org_name")
	private String orgName;
	
	@Readonly
	@Column("manager_id")
	private Integer managerId;
	
	@Readonly
	@Column("share_id")
	private Integer shareId;
	@Readonly
	@Column("sreport_id")
	private Integer sreportId;
	@Readonly
	@Column("touser_id")
	private Integer touserId;
	
	private long count;
	
	
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public String getYearMonth() {
		return yearMonth;
	}
	public void setYearMonth(String yearMonth) {
		this.yearMonth = yearMonth;
	}
	public Integer getManagerId() {
		return managerId;
	}
	public void setManagerId(Integer managerId) {
		this.managerId = managerId;
	}
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	public String getCorpName() {
		return corpName;
	}
	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public Integer getReportId() {
		return reportId;
	}
	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	public Integer getShareNumber() {
		return shareNumber;
	}
	public void setShareNumber(Integer shareNumber) {
		this.shareNumber = shareNumber;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getContent1() {
		return content1;
	}
	public void setContent1(String content1) {
		this.content1 = content1;
	}
	public String getContent2() {
		return content2;
	}
	public void setContent2(String content2) {
		this.content2 = content2;
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
	public Integer getApproveActor() {
		return approveActor;
	}
	public void setApproveActor(Integer approveActor) {
		this.approveActor = approveActor;
	}
	public Integer getApprove() {
		return approve;
	}
	public void setApprove(Integer approve) {
		this.approve = approve;
	}
	
	public String getSubmit() {
		return submit;
	}
	public void setSubmit(String submit) {
		this.submit = submit;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public Integer getShareId() {
		return shareId;
	}
	public void setShareId(Integer shareId) {
		this.shareId = shareId;
	}
	public Integer getSreportId() {
		return sreportId;
	}
	public void setSreportId(Integer sreportId) {
		this.sreportId = sreportId;
	}
	public Integer getTouserId() {
		return touserId;
	}
	public void setTouserId(Integer touserId) {
		this.touserId = touserId;
	}
	
	
}
