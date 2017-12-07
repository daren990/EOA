package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 假期规则配置
 * @author SimonTang
 */
@Table("conf_leave")
public class ConfLeave {
	@Id
	@Column("conf_leave_id")
	private Integer confLeaveId;
	@Column("conf_leave_name")
	private String confLeaveName;
	@Column("status")
	private Integer status; 
	
	private List<Org> corps;
	public Integer getConfLeaveId() {
		return confLeaveId;
	}
	public void setConfLeaveId(Integer confLeaveId) {
		this.confLeaveId = confLeaveId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}


	public String getConfLeaveName() {
		return confLeaveName;
	}
	public void setConfLeaveName(String confLeaveName) {
		this.confLeaveName = confLeaveName;
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
}
