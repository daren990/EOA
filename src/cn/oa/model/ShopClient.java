package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("shop_client")
public class ShopClient {
	
	@Id
	@Column("id")
	private Integer id;
	
	@Column("birthday")
	private Date birthday;
	
	@Column("name")
	private String name;
	
	@Column("sex")
	private Integer sex;
	
	@Column("telephone")
	private String telephone;
	
	@Column("address")
	private String address;
	
	@Column("photo")
	private String photo;
	
	@Column("truename")
	private String truename;
	
	@Column("password")
	private String password;
	
	@Column("weixin")
	private String weixin;
	
	@Column("status")
	private Integer status;
	
	@Column("phoneVLD")
	private Integer phoneVLD;
	
	@Column("msgcode")
	private String msgcode;
	
	private List<EduStudent> eduStudents;
	private List<Org> corps;
	
	@Readonly
	@Column("studentname")
	private String studentName;
	@Readonly
	@Column("sc.edu_student_id")
	private Integer studentId;
	@Readonly
	@Column("sc.edu_course_id")
	private Integer courseId;

	public String getMsgcode() {
		return msgcode;
	}

	public void setMsgcode(String msgcode) {
		this.msgcode = msgcode;
	}

	public Integer getPhoneVLD() {
		return phoneVLD;
	}

	public void setPhoneVLD(Integer phoneVLD) {
		this.phoneVLD = phoneVLD;
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
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

	public String getTruename() {
		return truename;
	}

	public void setTruename(String truename) {
		this.truename = truename;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	
	public String getWeixin() {
		return weixin;
	}

	public void setWeixin(String weixin) {
		this.weixin = weixin;
	}

	public List<EduStudent> getEduStudents() {
		return eduStudents;
	}

	public void setEduStudents(List<EduStudent> eduStudent) {
		this.eduStudents = eduStudent;
	}

	public List<Org> getCorps() {
		return corps;
	}

	public void setCorps(List<Org> corps) {
		this.corps = corps;
	}
	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}

	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}

	//获取用户所属培训机构的id
	public String getCorpIds() {
		return Converts.str(
				Converts.array(Org.class, Integer.class, getCorps(), "orgId"),
				",");
	}
	
	//获取用户所属培训机构的名字
	public String getCorpNames() {
		return Converts.str(
				Converts.array(Org.class, String.class, getCorps(), "orgName"),
				",");
	}
	
	//获取客户所属注册的学员的的id
	public String getStudentIds() {
		return Converts.str(
				Converts.array(EduStudent.class, Integer.class, getEduStudents(), "id"),
				",");
	}
	
	//获取客户所属注册的学员的名字
	public String getStudentNames() {
		return Converts.str(
				Converts.array(EduStudent.class, String.class, getEduStudents(), "name"),
				",");
	}

	@Override
	public String toString() {
		return "ShopClient [id=" + id + ", birthday=" + birthday + ", name="
				+ name + ", sex=" + sex + ", telephone=" + telephone
				+ ", address=" + address + ", photo=" + photo + ", truename="
				+ truename + ", password=" + password + ", status=" + status
				+ "]";
	}
	
	

	
	
	
	
}
