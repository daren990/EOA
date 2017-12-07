package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 班次设置
 * @author SimonTang
 */
@Table("conf_shift_class")
public class ShiftClass {
	@Id
	@Column("class_id")
	private Integer classId;
	@Column("corp_id")
	private Integer corpId;
	@Column("class_name")
	private String className;
	@Column("class_type")
	private String classType;
	@Column("sum_time")
	private float sumTime;
	@Column("is_second")
	private Integer second;
	@Column("first_m")
	private String firstMorning;
	@Column("first_n")
	private String firstNight;
	@Column("second_m")
	private String secondMorning;
	@Column("second_n")
	private String secondNight;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("modify_id")
	private Integer modifyId;
	@Column("status")
	private Integer status;
	@Column("is_night")
	private Integer night;
	@Column("color")
	private String color;
	
	
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public Integer getNight() {
		return night;
	}
	public void setNight(Integer night) {
		this.night = night;
	}
	public Integer getClassId() {
		return classId;
	}
	public void setClassId(Integer classId) {
		this.classId = classId;
	}
	public Integer getCorpId() {
		return corpId;
	}
	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
	}
	
	

	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getModifyId() {
		return modifyId;
	}
	public void setModifyId(Integer modifyId) {
		this.modifyId = modifyId;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public float getSumTime() {
		return sumTime;
	}
	public void setSumTime(float sumTime) {
		this.sumTime = sumTime;
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


	
	
	
}
