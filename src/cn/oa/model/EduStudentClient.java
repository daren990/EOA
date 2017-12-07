package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("edu_student_client")
public class EduStudentClient {
	
	@Id
	@Column("id")
	private Integer id;
	@Column("edu_student_id")
	private Integer eduStudentId;
	@Column("shop_client_id")
	private Integer shopClientId;
	
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
	public Integer getShopClientId() {
		return shopClientId;
	}
	public void setShopClientId(Integer shopClientId) {
		this.shopClientId = shopClientId;
	}
	
	
}
