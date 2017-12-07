package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 工作日排班.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("conf_work_day")
public class WorkDay {

	@Id
	@Column("day_id")
	private Integer dayId;
	@Column("work_name")
	private String workName;
	@Column("check_in")
	private String checkIn;
	@Column("check_out")
	private String checkOut;
	@Column("rest_in")
	private String restIn;
	@Column("rest_out")
	private String restOut;
	@Column("status")
	private Integer status;

	private List<Org> corps;

	public Integer getDayId() {
		return dayId;
	}

	public void setDayId(Integer dayId) {
		this.dayId = dayId;
	}

	public String getWorkName() {
		return workName;
	}

	public void setWorkName(String workName) {
		this.workName = workName;
	}

	public String getCheckIn() {
		return checkIn;
	}

	public void setCheckIn(String checkIn) {
		this.checkIn = checkIn;
	}

	public String getRestIn() {
		return restIn;
	}

	public void setRestIn(String restIn) {
		this.restIn = restIn;
	}

	public String getRestOut() {
		return restOut;
	}

	public void setRestOut(String restOut) {
		this.restOut = restOut;
	}

	public String getCheckOut() {
		return checkOut;
	}

	public void setCheckOut(String checkOut) {
		this.checkOut = checkOut;
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
