package cn.oa.model;

import java.io.Serializable;
import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 资源.
 * 
 * type(0:菜单, 1:路径, 2:操作)
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("sec_resource")
public class Resource implements Serializable {

	private static final long serialVersionUID = 2754110337179451309L;

	public static final int MENU = 0;
	public static final int URL = 1;
	public static final int ACTION = 2;

	@Id
	@Column("resource_id")
	private Integer resourceId;
	@Column("parent_id")
	private Integer parentId;
	@Column("resource_name")
	private String resourceName;
	@Column("url")
	private String url;
	@Column("action")
	private String action;
	@Column("position")
	private Integer position;
	@Column("type")
	private Integer type;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	
	@Readonly
	@Column("authority_id")
	private Integer authorityId;

	public Integer getResourceId() {
		return resourceId;
	}

	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
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

	public Integer getAuthorityId() {
		return authorityId;
	}

	public void setAuthorityId(Integer authorityId) {
		this.authorityId = authorityId;
	}

	@Override
	public String toString() {
		return "Resource [resourceId=" + resourceId + ", parentId=" + parentId + ", resourceName=" + resourceName
				+ ", url=" + url + ", action=" + action + ", position=" + position + ", type=" + type + ", status="
				+ status + ", createTime=" + createTime + ", modifyTime=" + modifyTime + ", authorityId=" + authorityId
				+ "]";
	}

	
	
}
