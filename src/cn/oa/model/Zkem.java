package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

@Table("conf_zkem")
public class Zkem {
	@Id
	@Column("zkem_id")
	private Integer zkemId;
	@Column("address")
	private String address;
	@Column("port")
	private Integer port;
	@Column("number")
	private Integer number;
	/*@Column("org_id")
	private Integer orgId;*/
	/*@Readonly
	@Column("org_name")
	private String orgName;*/
		
	public Integer getZkemId() {
		return zkemId;
	}
	public void setZkemId(Integer zkemId) {
		this.zkemId = zkemId;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	
}
