package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 财务人员配置
 * @author jiankun.chen
 *
 */
@Table("conf_accountant")
public class Accountant {
	@Id
	@Column("accountant_id")
	private Integer accountantId;
	
	@Column("accountant_name")
	private String accountantName;
	
	@Column("user_ids")
	private String userIds;
	
	@Column("status")
	private Integer status;
	
	private List<Org> corps;
	private List<User> users;
	
	public Integer getAccountantId() {
		return accountantId;
	}
	public void setAccountantId(Integer accountantId) {
		this.accountantId = accountantId;
	}
	public List<Org> getCorps() {
		return corps;
	}
	public void setCorps(List<Org> corps) {
		this.corps = corps;
	}
	public String getAccountantName() {
		return accountantName;
	}
	public void setAccountantName(String accountantName) {
		this.accountantName = accountantName;
	}
	public String getUserIds() {
		return userIds;
	}
	public void setUserIds(String userIds) {
		this.userIds = userIds;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getCorpIds() {
		return Converts.str(
				Converts.array(Org.class, Integer.class, getCorps(), "orgId"),
				",");
	}

	public String getCorpNames() {
		return Converts.str(
				Converts.array(Org.class, String.class, getCorps(), "orgName"),
				",");
	}
	public String getUserNames() {
		return Converts.str(
				Converts.array(User.class, String.class, getUsers(), "trueName"),
				",");
	}
}
