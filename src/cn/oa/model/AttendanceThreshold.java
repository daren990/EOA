package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("conf_attendance_threshold")
public class AttendanceThreshold {
	@Id
	@Column("threshold_id")
	private Integer thresholdId;
	@Column("threshold_name")
	private String thresholdName;
	@Column("org_ids")
	private String orgIds;
	@Column("status")
	private Integer status;
	private List<Org> corps;
	public Integer getThresholdId() {
		return thresholdId;
	}
	public void setThresholdId(Integer thresholdId) {
		this.thresholdId = thresholdId;
	}
	public String getThresholdName() {
		return thresholdName;
	}
	public void setThresholdName(String thresholdName) {
		this.thresholdName = thresholdName;
	}
	public String getOrgIds() {
		return orgIds;
	}
	public void setOrgIds(String orgIds) {
		this.orgIds = orgIds;
	}
	public List<Org> getCorps() {
		return corps;
	}
	public void setCorps(List<Org> corps) {
		this.corps = corps;
	}
	
	public String getCorpIds() {
		return Converts.str(
				Converts.array(Org.class, Integer.class, getCorps(), "orgId"),
				",");
	}

	public String getCorpNames() {
		return Converts.str(
				Converts.array(Org.class, String.class, getCorps(), "orgName"),
				",");
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
