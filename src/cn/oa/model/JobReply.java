package cn.oa.model;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;

import org.nutz.dao.entity.annotation.Table;
@Table("wp_job_report_reply")
public class JobReply {
	@Id
	@Column("reply_id")
	private Integer replyId;
	@Column("report_id")
	private Integer reportId;
	@Column("reply_content")
	private String replyContent;
	@Column("user_id")
	private Integer userId;
	@Column("reply_time")
	private String reportTime;
	@Column("reply_count")
	private Integer replyCount;
	@Column("reply_too_user")
	private String replyTooUser;
	@Column("reply_too")
	private String replyToo;
	@Readonly
	@Column("true_name")
	private String trueName;
	public Integer getReplyId() {
		return replyId;
	}
	public void setReplyId(Integer replyId) {
		this.replyId = replyId;
	}
	public Integer getReportId() {
		return reportId;
	}
	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}
	public String getReplyContent() {
		return replyContent;
	}
	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getReportTime() {
		return reportTime;
	}
	public void setReportTime(String reportTime) {
		this.reportTime = reportTime;
	}
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	public Integer getReplyCount() {
		return replyCount;
	}
	public void setReplyCount(Integer replyCount) {
		this.replyCount = replyCount;
	}
	
	public String getReplyTooUser() {
		return replyTooUser;
	}
	public void setReplyTooUser(String replyTooUser) {
		this.replyTooUser = replyTooUser;
	}
	public String getReplyToo() {
		return replyToo;
	}
	public void setReplyToo(String replyToo) {
		this.replyToo = replyToo;
	}
	
}
