package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 报销.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("epn_reimburse")
public class Reimburse {

	@Id
	@Column("reimburse_id")
	private Integer reimburseId;
	@Column("user_id")
	private Integer userId;
	@Column("subject")
	private String subject;
	@Column("project_id")
	private Integer projectId;
	@Column("amount")
	private Integer amount;
	@Column("deduct")
	private Integer deduct;
	@Column("approved")
	private Integer approved;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("theme")
	private String theme;
	@Column("number")
	private String number;
	@Column("sum_bill")
	private Integer sumBill;
	@Column("remark")
	private String remark;

	@Readonly
	@Column("true_name")
	private String trueName;
	@Readonly
	@Column("corp_name")
	private String corpName;
	@Readonly
	@Column("project_name")
	private String projectName;
	@Readonly
	@Column("approve")
	private Integer approve;
	@Readonly
	@Column("variable")
	private String variable;

	private Integer actorId;

	public Integer getReimburseId() {
		return reimburseId;
	}

	public void setReimburseId(Integer reimburseId) {
		this.reimburseId = reimburseId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}
	public Integer getDeduct() {
		return deduct;
	}

	public void setDeduct(Integer deduct) {
		this.deduct = deduct;
	}

	public Integer getApproved() {
		return approved;
	}

	public void setApproved(Integer approved) {
		this.approved = approved;
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

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Integer getApprove() {
		return approve;
	}

	public void setApprove(Integer approve) {
		this.approve = approve;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public Integer getActorId() {
		return actorId;
	}

	public void setActorId(Integer actorId) {
		this.actorId = actorId;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Integer getSumBill() {
		return sumBill;
	}

	public void setSumBill(Integer sumBill) {
		this.sumBill = sumBill;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
