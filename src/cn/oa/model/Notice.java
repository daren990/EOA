package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;

import org.nutz.dao.entity.annotation.Table;
@Table("conf_notice")
public class Notice {
	@Id
	@Column("notice_id")
	private Integer noticeId;
	@Column("title")
	private String title;
	@Column("content")
	private String content;
	@Column("corp")
	private Integer corp;
	@Column("creat_time")
	private Date creatTime;
	@Column("versions")
	private Integer versions;
	@Column("corp_id")
	private String corpId;
	@Column("role_id")
	private String roleId;
	@Column("user_id")
	private String userId;
	@Column("start_time")
	private Date startTime;
	@Column("end_time")
	private Date endTime;
	@Column("type")
	private Integer type;
	@Column("creator")
	private Integer creator;

	@Readonly
	@Column("org_name")
	private String orgName;
	@Readonly
	@Column("true_name")
	private String true_name;
	@Readonly
	@Column("is_receive")
	private Integer receive;
	
	private Integer status;
	private Integer read;
	private Integer unread;
	
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getRead() {
		return read;
	}
	public void setRead(Integer read) {
		this.read = read;
	}
	public Integer getUnread() {
		return unread;
	}
	public void setUnread(Integer unread) {
		this.unread = unread;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getTrue_name() {
		return true_name;
	}
	public void setTrue_name(String true_name) {
		this.true_name = true_name;
	}
	
	public Integer getReceive() {
		return receive;
	}
	public void setReceive(Integer receive) {
		this.receive = receive;
	}
	public Integer getNoticeId() {
		return noticeId;
	}
	public void setNoticeId(Integer noticeId) {
		this.noticeId = noticeId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Integer getCorp() {
		return corp;
	}
	public void setCorp(Integer corp) {
		this.corp = corp;
	}
	public Date getCreatTime() {
		return creatTime;
	}
	public void setCreatTime(Date creatTime) {
		this.creatTime = creatTime;
	}
	public Integer getVersions() {
		return versions;
	}
	public void setVersions(Integer versions) {
		this.versions = versions;
	}
	public String getCorpId() {
		return corpId;
	}
	public void setCorpId(String corpId) {
		this.corpId = corpId;
	}
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getCreator() {
		return creator;
	}
	public void setCreator(Integer creator) {
		this.creator = creator;
	}
	/*public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	*/
}
