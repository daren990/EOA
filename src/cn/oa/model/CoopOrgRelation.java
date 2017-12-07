package cn.oa.model;

public class CoopOrgRelation {

	private Integer orgId;
	private String orgName;
	private Integer coopId;
	private Integer gainSharingType;
	private float gainSharingNum;

	@Override
	public String toString() {
		return "CoopOrgRelation [orgId=" + orgId + ", orgName=" + orgName + ", coopId=" + coopId + ", gainSharingType="
				+ gainSharingType + ", gainSharingNum=" + gainSharingNum + "]";
	}

	public CoopOrgRelation() {

	}

	public CoopOrgRelation(Integer orgId, String orgName, Integer coopId, Integer gainSharingType,
			float gainSharingNum) {
		this.orgId = orgId;
		this.orgName = orgName;
		this.coopId = coopId;
		this.gainSharingType = gainSharingType;
		this.gainSharingNum = gainSharingNum;
	}

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
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

}
