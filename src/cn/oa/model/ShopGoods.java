package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("shop_goods")
public class ShopGoods {

	@Override
	public String toString() {
		return "ShopGoods [id=" + id + ", corpId=" + corpId + ", eduTeacherId=" + eduTeacherId + ", eduClassifyId="
				+ eduClassifyId + ", name=" + name + ", coudesc=" + coudesc + ", status=" + status + ", type=" + type
				+ ", couCount=" + couCount + ", price=" + price + ", location=" + location + ", couTime=" + couTime
				+ ", couSeason=" + couSeason + ", coopType=" + coopType + ", gainSharingType=" + gainSharingType
				+ ", gainSharingNum=" + gainSharingNum + ", coopId=" + coopId + ", startDate=" + startDate + ", max="
				+ max + ", sold=" + sold + ", dependName=" + dependName + ", teacher=" + teacher + "]";
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Id
	@Column("id")
	private Integer id;

	@Column("corp_id")
	private Integer corpId;

	@Column("edu_teacher_id")
	private Integer eduTeacherId;

	@Column("edu_classify_id")
	private Integer eduClassifyId;

	@Column("name")
	private String name;

	@Column("coudesc")
	private String coudesc;

	@Column("status")
	private Integer status;

	@Column("type")
	private String type; // 科目、学科、课程

	@Column("coucount")
	private Integer couCount; // 课时数量

	@Column("price")
	private float price; // 课时价格

	@Column("location")
	private String location; // 上课地点

	@Column("coutime")
	private String couTime; // 上课时间

	@Column("couseason")
	private String couSeason;// 上课季度

	@Column("cooptype")
	private String coopType; // 课程开展类型:自营 or 兼职 or 合作

	@Column("gainsharingtype")
	private Integer gainSharingType; // 分成方式:1、2、3、4
										// 1.按学生绝对值分成
										// 2.按学生比例值分成
										// 3.按课时绝对值分成
										// 4.按课时比例值分成

	@Column("gainSharingNum")
	private Float gainSharingNum;

	@Column("coop_id")
	private Integer coopId;

	@Column("startDate")
	private Date startDate;

	@Column("max")
	private Integer max;

	@Column("sold")
	private Integer sold;

	@Column("dependname")
	private String dependName;

	@Column("dependId")
	private Integer dependId;

	@Column("isCopy")
	private Integer isCopy;

	private EduTeacher teacher;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCorpId() {
		return corpId;
	}

	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

	public Integer getEduTeacherId() {
		return eduTeacherId;
	}

	public void setEduTeacherId(Integer eduTeacherId) {
		this.eduTeacherId = eduTeacherId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCoudesc() {
		return coudesc;
	}

	public void setCoudesc(String coudesc) {
		this.coudesc = coudesc;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getCouCount() {
		return couCount;
	}

	public void setCouCount(Integer couCount) {
		this.couCount = couCount;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCoopType() {
		return coopType;
	}

	public void setCoopType(String coopType) {
		this.coopType = coopType;
	}

	public Integer getGainSharingType() {
		return gainSharingType;
	}

	public void setGainSharingType(Integer gainSharingType) {
		this.gainSharingType = gainSharingType;
	}

	public Float getGainSharingNum() {
		return gainSharingNum;
	}

	public void setGainSharingNum(Float gainSharingNum) {
		this.gainSharingNum = gainSharingNum;
	}

	public Integer getEduClassifyId() {
		return eduClassifyId;
	}

	public void setEduClassifyId(Integer eduClassifyId) {
		this.eduClassifyId = eduClassifyId;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public Integer getSold() {
		return sold;
	}

	public void setSold(Integer sold) {
		this.sold = sold;
	}

	public String getCouTime() {
		return couTime;
	}

	public void setCouTime(String couTime) {
		this.couTime = couTime;
	}

	public String getCouSeason() {
		return couSeason;
	}

	public void setCouSeason(String couSeason) {
		this.couSeason = couSeason;
	}

	public Integer getCoopId() {
		return coopId;
	}

	public void setCoopId(Integer coopId) {
		this.coopId = coopId;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public EduTeacher getTeacher() {
		return teacher;
	}

	public void setTeacher(EduTeacher teacher) {
		this.teacher = teacher;
	}

	public String getDependName() {
		return dependName;
	}

	public void setDependName(String dependName) {
		this.dependName = dependName;
	}

	public Integer getDependId() {
		return dependId;
	}

	public void setDependId(Integer dependId) {
		this.dependId = dependId;
	}

	public Integer getIsCopy() {
		return isCopy;
	}

	public void setIsCopy(Integer isCopy) {
		this.isCopy = isCopy;
	}

}
