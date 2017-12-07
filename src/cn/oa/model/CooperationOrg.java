package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.One;
import org.nutz.dao.entity.annotation.Table;

@Table("sec_cooperation_org")
public class CooperationOrg {

	@Id
	@Column("id")
	private Integer id;

	@Column("coopId")
	private Integer coopId;

	@Column("coopIdOther")
	private Integer coopIdOther;

	@Column("gainsharingtype")
	private Integer gainSharingType;

	@Column("gainsharingnum")
	private float gainSharingNum;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCoopId() {
		return coopId;
	}

	public void setCoopId(Integer coopId) {
		this.coopId = coopId;
	}

	public Integer getGainSharingType() {
		return gainSharingType;
	}

	public void setGainSharingType(Integer gainSharingType) {
		this.gainSharingType = gainSharingType;
	}

	public float getGainSharingNum() {
		return gainSharingNum;
	}

	public void setGainSharingNum(float gainSharingNum) {
		this.gainSharingNum = gainSharingNum;
	}

	public Integer getCoopIdOther() {
		return coopIdOther;
	}

	public void setCoopIdOther(Integer coopIdOther) {
		this.coopIdOther = coopIdOther;
	}

}
