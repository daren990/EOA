package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.ManyMany;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 门禁.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("res_control")
public class Control {

	@Id
	@Column("control_id")
	private Integer controlId;
	@Column("control_name")
	private String controlName;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;

	@ManyMany(target = User.class, relation = "res_control_user", from = "control_id", to = "user_id")
	private List<User> users;

	public Integer getControlId() {
		return controlId;
	}

	public void setControlId(Integer controlId) {
		this.controlId = controlId;
	}

	public String getControlName() {
		return controlName;
	}

	public void setControlName(String controlName) {
		this.controlName = controlName;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getUserIds() {
		return Converts.str(Converts.array(User.class, Integer.class, getUsers(), "userId"), ",");
	}

	public String getTrueNames() {
		return Converts.str(Converts.array(User.class, String.class, getUsers(), "trueName"), ",");
	}
}
