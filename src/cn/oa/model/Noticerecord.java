package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;

import org.nutz.dao.entity.annotation.Table;
@Table("conf_noticerecord")
public class Noticerecord {

	@Column("receive_id")
	private Integer receiveId;
	@Column("notice_id")
	private Integer noticeId;
	@Column("is_receive")
	private Integer receive;
	@Column("receive_time")
	private Date receiveTime;
	
	public Integer getReceiveId() {
		return receiveId;
	}
	public void setReceiveId(Integer receiveId) {
		this.receiveId = receiveId;
	}
	public Integer getNoticeId() {
		return noticeId;
	}
	public void setNoticeId(Integer noticeId) {
		this.noticeId = noticeId;
	}
	public Integer getReceive() {
		return receive;
	}
	public void setReceive(Integer receive) {
		this.receive = receive;
	}
	public Date getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}
	
}
