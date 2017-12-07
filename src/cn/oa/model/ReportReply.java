package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

@Table("report_reply")
public class ReportReply {
	@Id
	@Column("reply_id")
	private Integer replyId;
	@Column("comment_id")
	private Integer commentId;
	@Column("create_time")
	private Date createTime;
	@Column("content")
	private String content;
	@Column("replyed_id")
	private Integer replyedId;
	@Column("user_id")
	private Integer userId;
	@Column("replyed_user_id")
	private Integer replyedUserId;
	
	@Readonly
	@Column("replyed_name")
	private String replyedName;
	@Readonly
	@Column("true_name")
	private String trueName;
	
	@Readonly
	@Column("report_id")
	private Integer reportId;
	
	public Integer getReplyId() {
		return replyId;
	}
	public void setReplyId(Integer replyId) {
		this.replyId = replyId;
	}
	public Integer getCommentId() {
		return commentId;
	}
	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Integer getReplyedId() {
		return replyedId;
	}
	public void setReplyedId(Integer replyedId) {
		this.replyedId = replyedId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getReportId() {
		return reportId;
	}
	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	public Integer getReplyedUserId() {
		return replyedUserId;
	}
	public void setReplyedUserId(Integer replyedUserId) {
		this.replyedUserId = replyedUserId;
	}
	public String getReplyedName() {
		return replyedName;
	}
	public void setReplyedName(String replyedName) {
		this.replyedName = replyedName;
	}
	
	
}
