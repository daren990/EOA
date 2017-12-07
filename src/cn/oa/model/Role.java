package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.ManyMany;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 角色.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("sec_role")
public class Role {

	@Id
	@Column("role_id")
	private Integer roleId;
	@Column("role_name")
	private String roleName;
	@Column("role_desc")
	private String roleDesc;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	
	@ManyMany(target = Authority.class, relation = "sec_role_authority", from = "role_id", to = "authority_id")
	private List<Authority> authorities;
	
	@Readonly
	@Column("user_id")
	private Integer userId;

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleDesc() {
		return roleDesc;
	}

	public void setRoleDesc(String roleDesc) {
		this.roleDesc = roleDesc;
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

	public List<Authority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<Authority> authorities) {
		this.authorities = authorities;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getAuthorityIds() {
		return Converts.str(Converts.array(Authority.class, Integer.class, getAuthorities(), "authorityId"), ",");
	}

	public String getAuthorityNames() {
		return Converts.str(Converts.array(Authority.class, String.class, getAuthorities(), "authorityName"), ",");
	}
}
