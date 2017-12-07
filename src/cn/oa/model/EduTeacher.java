package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("edu_teacher")
public class EduTeacher {
	
	@Override
	public String toString() {
		return "EduTeacher [id=" + id + ", corpId=" + corpId + ", birthday="
				+ birthday + ", sex=" + sex + ", status=" + status
				+ ", telephone=" + telephone + ", name=" + name + ", weixin="
				+ weixin + ", address=" + address + ", photo=" + photo + "]";
	}

	@Id
	@Column("id")
	private Integer id;
	
	@Column("corp_id")
	private Integer corpId;
	
	@Column("birthday")
	private Date birthday;
	
	@Column("sex")
	private Integer sex;
	
	@Column("status")
	private Integer status;
	
	@Column("telephone")
	private String telephone;
	
	@Column("name")
	private String name;
	
	@Column("weixin")
	private String weixin;
	
	@Column("address")
	private String address;
	
	@Column("photo")
	private String photo;
	
	@Column("truename")
	private String truename;
	
	@Column("coopType")
	private String coopType;
	
	@Column("coopId")
	private Integer coopId;
	
	@Column("coopTeacherId")
	private Integer coopTeacherId;
	
	@Readonly
	@Column("org_name")
	private String corpName;
	
	@One(field = "corpId", target = Org.class)
	private Org corp;
	
	private List<EduTeachingSchedule> eduTeachingSchedules;
	
	private List<ShopGoods> courses;
	
	private List<EduClassify> subjects;
	

	public List<EduTeachingSchedule> getEduTeachingSchedules() {
		return eduTeachingSchedules;
	}

	public void setEduTeachingSchedules(
			List<EduTeachingSchedule> eduTeachingSchedules) {
		this.eduTeachingSchedules = eduTeachingSchedules;
	}

	public List<ShopGoods> getCourses() {
		return courses;
	}

	public void setCourses(List<ShopGoods> courses) {
		this.courses = courses;
	}

	public Org getCorp() {
		return corp;
	}

	public void setCorp(Org corp) {
		this.corp = corp;
	}

	public String getCorpName() {
		if(corpName == null && corp != null){
			corpName = corp.getOrgName();
		}
		return corpName;
	}

	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}

	public String getTruename() {
		return truename;
	}

	public void setTruename(String truename) {
		this.truename = truename;
	}

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

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWeixin() {
		return weixin;
	}

	public void setWeixin(String weixin) {
		this.weixin = weixin;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getCoopType() {
		return coopType;
	}

	public void setCoopType(String coopType) {
		this.coopType = coopType;
	}
	
	public List<EduClassify> getSubjects() {
		return subjects;
	}
	public void setSubjects(List<EduClassify> subjects) {
		this.subjects = subjects;
	}
	public Integer getCoopId() {
		return coopId;
	}

	public void setCoopId(Integer coopId) {
		this.coopId = coopId;
	}
	public Integer getCoopTeacherId() {
		return coopTeacherId;
	}

	public void setCoopTeacherId(Integer coopTeacherId) {
		this.coopTeacherId = coopTeacherId;
	}

	//获取老师所参加的课程的id
	public String getCourseIds() {
		return Converts.str(
				Converts.array(ShopGoods.class, Integer.class, getCourses(), "id"),
				",");
	}
	
	//获取老师所参加课程的名字
	public String getCourseNames() {
		return Converts.str(
				Converts.array(ShopGoods.class, String.class, getCourses(), "name"),
				",");
	}
	
	//获取老师所参加的所属学科的id
	public String getSubjectIds() {
		return Converts.str(
				Converts.array(EduClassify.class, Integer.class, getSubjects(), "id"),
				",");
	}
	
	//获取老师所参加学科的名字
	public String getSubjectNames() {
		return Converts.str(
				Converts.array(EduClassify.class, String.class, getSubjects(), "name"),
				",");
	}
	
}
