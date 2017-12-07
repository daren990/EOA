package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("edu_student_corp")
public class EduStudentCorp {
	
	@Id
	@Column("id")
	private Integer id;
	@Column("edu_student_id")
	private Integer eduStudentId;
	@Column("sec_corp_id")
	private Integer corpId;
	
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
	public Integer getCorpId() {
		return corpId;
	}
	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

}
