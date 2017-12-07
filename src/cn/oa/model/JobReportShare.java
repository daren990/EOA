package cn.oa.model;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;

import org.nutz.dao.entity.annotation.Table;
@Table("wp_job_report_share")
public class JobReportShare {
	@Id
	@Column("share_id")
	private Integer shareId;
	@Column("sreport_id")
	private Integer sreportId;
	@Column("touser_id")
	private Integer touserId;
	@Readonly
	@Column("true_name")
	private String trueName;
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
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	
}
