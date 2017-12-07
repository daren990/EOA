package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("edu_student_classes")
public class EduStudentClasses {
	
	@Override
	public String toString() {
		return "EduStudentClasses [id=" + id + ", eduStudentId=" + eduStudentId
				+ ", eduClassesId=" + eduClassesId + "]";
	}

	@Id
	@Column("id")
	private Integer id;
	
	@Column("edu_student_id")
	private Integer eduStudentId;
	
	@Column("edu_classes_id")
	private Integer eduClassesId;

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

	public Integer getEduClassesId() {
		return eduClassesId;
	}

	public void setEduClassesId(Integer eduClassesId) {
		this.eduClassesId = eduClassesId;
	}
	
}
