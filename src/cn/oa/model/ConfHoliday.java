package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 法定假日配置
 * @author jiankun.chen
 *
 */
@Table("conf_holiday")
public class ConfHoliday {
	
	@Id
	@Column("holiday_id")
	private Integer holidayId;
	@Column("year")
	private String year;
	@Column("month")
	private String month;
	@Column("holiday_name")
	private String holidayName;
	@Column("holiday_days")
	private String holidayDays;
	@Column("Status")
	private Integer Status;
	
	
	private List<Org> corps;
	public Integer getHolidayId() {
		return holidayId;
	}
	public void setHolidayId(Integer holidayId) {
		this.holidayId = holidayId;
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
	public String getHolidayName() {
		return holidayName;
	}
	public void setHolidayName(String holidayName) {
		this.holidayName = holidayName;
	}
	public String getHolidayDays() {
		return holidayDays;
	}
	public void setHolidayDays(String holidayDays) {
		this.holidayDays = holidayDays;
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
	public List<Org> getCorps() {
		return corps;
	}
	public void setCorps(List<Org> corps) {
		this.corps = corps;
	}
	public Integer getStatus() {
		return Status;
	}
	public void setStatus(Integer status) {
		Status = status;
	}
	
	
}
