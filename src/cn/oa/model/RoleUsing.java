package cn.oa.model;

/**
 * 新排班公司.
 * 
 * @author qiumingxie
 */
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

@Table("conf_role_using")
public class RoleUsing {
	@Column("roleId")
	private Integer roleId;

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

}
