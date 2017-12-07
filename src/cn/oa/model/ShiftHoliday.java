package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Table;
@Table("conf_shift_holiday")
@PK({ "holiday" })
public class ShiftHoliday {

	/*@Column("year")
	private Integer year;
	@Column("month")
	private Integer month;
	@Column("day")
	private Integer day;*/
	@Column("holiday")
	private Date holiday;
	@Column("remarks")
	private String remarks;
	
	private Integer year;
	private String month;
	private String day;
	
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public Date getHoliday() {
		return holiday;
	}
	public void setHoliday(Date holiday) {
		this.holiday = holiday;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
