	package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("edu_student_course")
public class EduStudentCourse {

	@Override
	public String toString() {
		return "EduStudentCourse [id=" + id + ", eduStudentId=" + eduStudentId + ", eduCourseId=" + eduCourseId + "]";
	}

	@Id
	@Column("id")
	private Integer id;

	@Column("edu_student_id")
	private Integer eduStudentId;

	@Column("edu_course_id")
	private Integer eduCourseId;
	
	@One(field = "eduCourseId", target = ShopGoods.class)
	private ShopGoods course;
	
	
	public ShopGoods getCourse() {
		return course;
	}

	public void setCourse(ShopGoods course) {
		this.course = course;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEduStudentId() {
		return eduStudentId;
	}

	public void setEduStudentId(Integer eduStudentId) {
		this.eduStudentId = eduStudentId;
	}

	public Integer getEduCourseId() {
		return eduCourseId;
	}

	public void setEduCourseId(Integer eduCourseId) {
		this.eduCourseId = eduCourseId;
	}

}
