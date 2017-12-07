package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 工作周排班.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("conf_work_week")
public class WorkWeek {

	@Id
	@Column("week_id")
	private Integer weekId;
	@Column("work_name")
	private String workName;
	@Column("work_days")
	private String workDays;
	@Column("status")
	private Integer status;

	private List<Org> corps;

	public Integer getWeekId() {
		return weekId;
	}

	public void setWeekId(Integer weekId) {
		this.weekId = weekId;
	}

	public String getWorkName() {
		return workName;
	}

	public void setWorkName(String workName) {
		this.workName = workName;
	}

	public String getWorkDays() {
		return workDays;
	}

	public void setWorkDays(String workDays) {
		this.workDays = workDays;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public List<Org> getCorps() {
		return corps;
	}

	public void setCorps(List<Org> corps) {
		this.corps = corps;
	}

	public String getCorpIds() {
		return Converts.str(Converts.array(Org.class, Integer.class, getCorps(), "orgId"), ",");
	}

	public String getCorpNames() {
		return Converts.str(Converts.array(Org.class, String.class, getCorps(), "orgName"), ",");
	}
}
