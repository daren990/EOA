package cn.oa.model;




import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;
@Table("ab_address_user")
public class AddressUser {
	@Id
	@Column("u_user_id")
	private Integer u_userid;
	@Column("u_group_id")
	private Integer u_groupid;
	@Column("username")
	private String username;
	@Column("relation")
	private String relation;
	@Column("position")
	private String position;
	@Column("phone")
	private String phone;
	@Column("email")
	private String email;
	@Column("qq")
	private String qq;
	@Column("exigency_phone")
	private String exigencyphone;
	

	public Integer getU_userid() {
		return u_userid;
	}
	public void setU_userid(Integer u_userid) {
		this.u_userid = u_userid;
	}

	public Integer getU_groupid() {
		return u_groupid;
	}
	public void setU_groupid(Integer u_groupid) {
		this.u_groupid = u_groupid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getQq() {
		return qq;
	}
	public void setQq(String qq) {
		this.qq = qq;
	}
	public String getExigencyphone() {
		return exigencyphone;
	}
	public void setExigencyphone(String exigencyphone) {
		this.exigencyphone = exigencyphone;
	}

}
