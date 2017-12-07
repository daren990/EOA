package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 日志.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("access_logger")
public class Logger {

	@Id
	@Column("logger_id")
	private Integer loggerId;
	@Column("user_id")
	private Integer userId;
	@Column("ip")
	private String ip;
	@Column("client")
	private String client;
	@Column("url")
	private String url;
	@Column("content")
	private String content;
	@Column("modify_time")
	private Date modifyTime;

	@Readonly
	@Column("true_name")
	private String trueName;
	@Readonly
	@Column("job_number")
	private String jobNumber;

	public Logger() {
	}

	public Logger(Integer userId, String ip, String client, String url,
			String content, Date modifyTime) {
		this.userId = userId;
		this.ip = ip;
		this.client = client;
		this.url = url;
		this.content = content;
		this.modifyTime = modifyTime;
	}

	public Integer getLoggerId() {
		return loggerId;
	}

	public void setLoggerId(Integer loggerId) {
		this.loggerId = loggerId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public String getJobNumber() {
		return jobNumber;
	}

	public void setJobNumber(String jobNumber) {
		this.jobNumber = jobNumber;
	}

}
