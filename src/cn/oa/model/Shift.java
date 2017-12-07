package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 排班表
 * 
 * @author SimonTang
 */
@Table("conf_shift")
public class Shift {

	@Column("user_id")
	private Integer userId;
	@Column("shift_date")
	private Date shiftDate;
	@Column("is_used")
	private Integer isUsed;
	@Column("classes")
	private Integer classes;
	@Column("modify_time")
	private Date modifyTime;
	@Column("create_time")
	private Date createTime;
	@Column("modify_id")
	private Integer modifyId;
	
	/**
	 * 锁状态
	 */
	@Column("status")
	private Integer status;

	@Readonly
	@Column("color")
	private String color;
	@Readonly
	@Column("class_name")
	private String className;
	@Readonly
	@Column("is_second")
	private Integer second;
	@Readonly
	@Column("first_m")
	private String firstMorning;
	@Readonly
	@Column("first_n")
	private String firstNight;

	@Readonly
	@Column("second_m")
	private String secondMorning;
	@Readonly
	@Column("second_n")
	private String secondNight;
	@Readonly
	@Column("is_night")
	private Integer night;

	private String holiday;
	private ShiftClass shiftClass;
	private int sort;
	private int day;
	private Integer week;
	private float sumtime;
	
	@Readonly
	@Column("org_name")
	private String orgName;
	
	@Readonly
	@Column("corp_id")
	private Integer corpId;
	
	@Readonly
	@Column("shift_month")
	private String shiftMonth;
	

	public String getHoliday() {
		return holiday;
	}

	public void setHoliday(String holiday) {
		this.holiday = holiday;
	}

	public Integer getNight() {
		return night;
	}

	public void setNight(Integer night) {
		this.night = night;
	}

	public Integer getSecond() {
		return second;
	}

	public void setSecond(Integer second) {
		this.second = second;
	}

	public String getFirstMorning() {
		return firstMorning;
	}

	public void setFirstMorning(String firstMorning) {
		this.firstMorning = firstMorning;
	}

	public String getFirstNight() {
		return firstNight;
	}

	public void setFirstNight(String firstNight) {
		this.firstNight = firstNight;
	}

	public String getSecondMorning() {
		return secondMorning;
	}

	public void setSecondMorning(String secondMorning) {
		this.secondMorning = secondMorning;
	}

	public String getSecondNight() {
		return secondNight;
	}

	public void setSecondNight(String secondNight) {
		this.secondNight = secondNight;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Integer getWeek() {
		return week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public float getSumtime() {
		return sumtime;
	}

	public void setSumtime(float sumtime) {
		this.sumtime = sumtime;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public ShiftClass getShiftClass() {
		return shiftClass;
	}

	public void setShiftClass(ShiftClass shiftClass) {
		this.shiftClass = shiftClass;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getIsUsed() {
		return isUsed;
	}

	public void setIsUsed(Integer isUsed) {
		this.isUsed = isUsed;
	}

	public Date getShiftDate() {
		return shiftDate;
	}

	public void setShiftDate(Date shiftDate) {
		this.shiftDate = shiftDate;
	}

	public Integer getClasses() {
		return classes;
	}

	public void setClasses(Integer classes) {
		this.classes = classes;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getModifyId() {
		return modifyId;
	}

	public void setModifyId(Integer modifyId) {
		this.modifyId = modifyId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getShiftMonth() {
		return shiftMonth;
	}

	public void setShiftMonth(String shiftMonth) {
		this.shiftMonth = shiftMonth;
	}

	public Integer getCorpId() {
		return corpId;
	}

	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

}
