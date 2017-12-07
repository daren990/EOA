package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Name;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("edu_student")
public class EduStudent {
	

	@Id
	@Column("id")
	private Integer id;
	
	@Column("name")
	private String name;
	
	@Column("birthday")
	private Date birthday;
	
	@Column("hobby")
	private String hobby;
	
	@Column("sex")
	private Integer sex;
	
	@Column("status")
	private Integer status;
	
	@Column("telephone")
	private String telephone;
	
	@Column("qq")
	private String qq;
	
	@Column("weixin")
	private String weixin;
	
	@Column("address")
	private String address;
	
	@Column("photo")
	private String photo;
	
	@Name
	@Column("number")
	private String number;

	private List<ShopClient> shopClient;
	private List<ShopGoods> eduCourse;
	private List<Org> corps;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getHobby() {
		return hobby;
	}

	public void setHobby(String hobby) {
		this.hobby = hobby;
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

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
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

	public List<ShopClient> getShopClients() {
		return shopClient;
	}

	public void setShopClients(List<ShopClient> shopClient) {
		this.shopClient = shopClient;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public List<ShopGoods> getEduCourses() {
		return eduCourse;
	}

	public void setEduCourses(List<ShopGoods> eduCourse) {
		this.eduCourse = eduCourse;
	}

	public List<Org> getCorps() {
		return corps;
	}

	public void setCorps(List<Org> corps) {
		this.corps = corps;
	}
	
	//获取学生所属培训机构的id
	public String getCorpIds() {
		return Converts.str(
				Converts.array(Org.class, Integer.class, getCorps(), "orgId"),
				",");
	}
	
	//获取学生所属培训机构的名字
	public String getCorpNames() {
		return Converts.str(
				Converts.array(Org.class, String.class, getCorps(), "orgName"),
				",");
	}
	
	//获取学生所属客户的的id
	public String getClientIds() {
		return Converts.str(
				Converts.array(ShopClient.class, Integer.class, getShopClients(), "id"),
				",");
	}
	
	//获取学生所属客户的名字
	public String getClientNames() {
		return Converts.str(
				Converts.array(ShopClient.class, String.class, getShopClients(), "truename"),
				",");
	}
	
	//获取学生所参加的课程的id
	public String getCourseIds() {
		return Converts.str(
				Converts.array(ShopGoods.class, Integer.class, getEduCourses(), "id"),
				",");
	}
	
	//获取学生所参加课程的名字
	public String getCourseNames() {
		return Converts.str(
				Converts.array(ShopGoods.class, String.class, getEduCourses(), "name"),
				",");
	}

}
