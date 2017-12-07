package cn.oa.model;

/**
 * 新排班公司.
 * 
 * @author qiumingxie
 */
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

@Table("conf_shift_corp")
public class ShiftCorp {
	@Column("corp_id")
	private Integer corpId;
	@Readonly
	@Column("org_name")
	private String orgName;
	
	@Readonly
	@Column("status")
	private String status;
	
	
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getCorpId() {
		return corpId;
	}

	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	
}
