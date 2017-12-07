package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

public class ShopGoodsDisplay {

	private Integer id;

	private Integer dependId;

	private String name;

	private Integer couCount; // 课时数量

	private float price; // 课时价格

	private String location; // 上课地点

	private String couTime; // 上课时间

	private Date startDate;

	private Integer sold;

	private int countStart;

	private int countEnd;

	private Integer countAppear;

	private Integer countDisAppear;

	private String corpName;

	private String teacherName;

	private String teacherTelephone;

	private Integer isOver;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCouCount() {
		return couCount;
	}

	public void setCouCount(Integer couCount) {
		this.couCount = couCount;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCouTime() {
		return couTime;
	}

	public void setCouTime(String couTime) {
		this.couTime = couTime;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Integer getSold() {
		return sold;
	}

	public void setSold(Integer sold) {
		this.sold = sold;
	}

	public int getCountStart() {
		return countStart;
	}

	public void setCountStart(int countStart) {
		this.countStart = countStart;
	}

	public int getCountEnd() {
		return countEnd;
	}

	public void setCountEnd(int countEnd) {
		this.countEnd = countEnd;
	}

	public Integer getCountAppear() {
		return countAppear;
	}

	public void setCountAppear(Integer countAppear) {
		this.countAppear = countAppear;
	}

	public String getCorpName() {
		return corpName;
	}

	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	public String getTeacherTelephone() {
		return teacherTelephone;
	}

	public void setTeacherTelephone(String teacherTelephone) {
		this.teacherTelephone = teacherTelephone;
	}

	public Integer getIsOver() {
		return isOver;
	}

	public void setIsOver(Integer isOver) {
		this.isOver = isOver;
	}

	public Integer getDependId() {
		return dependId;
	}

	public void setDependId(Integer dependId) {
		this.dependId = dependId;
	}

	public Integer getCountDisAppear() {
		return countDisAppear;
	}

	public void setCountDisAppear(Integer countDisAppear) {
		this.countDisAppear = countDisAppear;
	}

}
