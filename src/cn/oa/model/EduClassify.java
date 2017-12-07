package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("edu_classify")
public class EduClassify {

	@Id
	@Column("id")
	private Integer id;

	@Column("name")
	private String name;

	@Column("type")
	private String type;

	@Column("classify_describe")
	private String describe;

	@Column("status")
	private Integer status;

	@Column("parent_id")
	private Integer parentId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	@Override
	public String toString() {
		return "EduClassify [id=" + id + ", name=" + name + ", type=" + type + ", describe=" + describe + ", status="
				+ status + ", parentid=" + parentId + "]";
	}

}
