package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("edu_result_wage")
public class EduResultWage {
	
	@Override
	public String toString() {
		return "EduResultWage [id=" + id + ", eduTeacherId=" + eduTeacherId
				+ "]";
	}

	@Id
	@Column("id")
	private Integer id;
	
	@Column("edu_teacher_id")
	private Integer eduTeacherId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEduTeacherId() {
		return eduTeacherId;
	}

	public void setEduTeacherId(Integer eduTeacherId) {
		this.eduTeacherId = eduTeacherId;
	}
	

	
}
