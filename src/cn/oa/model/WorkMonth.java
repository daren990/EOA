package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 工作月排班.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("conf_work_month")
@PK({ "orgId", "year", "month" })
public class WorkMonth {

	@Id
	@Column("month_id")
	private Integer monthId;
	@Column("org_id")
	private Integer orgId;
	@Column("year")
	private String year;
	@Column("month")
	private String month;
	@Column("work_name")
	private String workName;
	@Column("work_days")
	private String workDays;
	@Column("status")
	private Integer status;
	@Column("holidays")
	private String holidays;
	@Column("conf_shift_class_id")
	private Integer shiftClassId;
	@Readonly
	@Column("corp_name")
	private String corpName;

	
	
	public Integer getShiftClassId() {
		return shiftClassId;
	}

	public void setShiftClassId(Integer shiftClassId) {
		this.shiftClassId = shiftClassId;
	}

	public Integer getMonthId() {
		return monthId;
	}

	public void setMonthId(Integer monthId) {
		this.monthId = monthId;
	}

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
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

	public String getCorpName() {
		return corpName;
	}

	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}

	public String getHolidays() {
		return holidays;
	}

	public void setHolidays(String holidays) {
		this.holidays = holidays;
	}

	@Override
	public String toString() {
		return "WorkMonth [monthId=" + monthId + ", orgId=" + orgId + ", year="
				+ year + ", month=" + month + ", workName=" + workName
				+ ", workDays=" + workDays + ", status=" + status
				+ ", holidays=" + holidays + ", corpName=" + corpName + "]";
	}
	
}
