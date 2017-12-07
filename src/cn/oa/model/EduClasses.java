package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("edu_classes")
public class EduClasses {

	@Override
	public String toString() {
		return "EduClasses [id=" + id + ", eduCourseId=" + eduCourseId + ", name=" + name + ", status=" + status
				+ ", sdcount=" + sdcount + "]";
	}

	@Id
	@Column("id")
	private Integer id;

	@Column("edu_course_id")
	private Integer eduCourseId;

	@Column("name")
	private String name;

	@Column("status")
	private Integer status;

	@Column("sducount")
	private Integer sdcount;// 学生人数

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEduCourseId() {
		return eduCourseId;
	}

	public void setEduCourseId(Integer eduCourseId) {
		this.eduCourseId = eduCourseId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getSdcount() {
		return sdcount;
	}

	public void setSdcount(Integer sdcount) {
		this.sdcount = sdcount;
	}

}
