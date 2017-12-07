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
 * 权限.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("sec_authority")
public class Authority {

	@Id
	@Column("authority_id")
	private Integer authorityId;
	@Column("authority_name")
	private String authorityName;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	
	@ManyMany(target = Resource.class, relation = "sec_authority_resource", from = "authority_id", to = "resource_id")
	private List<Resource> resources;
	
	@Readonly
	@Column("role_id")
	private Integer roleId;

	public Integer getAuthorityId() {
		return authorityId;
	}

	public void setAuthorityId(Integer authorityId) {
		this.authorityId = authorityId;
	}

	public String getAuthorityName() {
		return authorityName;
	}

	public void setAuthorityName(String authorityName) {
		this.authorityName = authorityName;
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

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getResourceIds() {
		return Converts.str(Converts.array(Resource.class, Integer.class, getResources(), "resourceId"), ",");
	}

	public String getResourceNames() {
		return Converts.str(Converts.array(Resource.class, String.class, getResources(), "resourceName"), ",");
	}
}
