package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("hrm_regular_business")
public class RegularBusiness {
	@Id
	@Column("business_id")
	private Integer businessId;
	@Column("actor1")
	private Integer actor1;
	@Column("actor2")
	private Integer actor2;
	@Column("actor3")
	private Integer actor3;
	@Column("actor4")
	private Integer actor4;
	@Column("actor5")
	private Integer actor5;
	@Column("actor6")
	private Integer actor6;
	@Column("actor7")
	private Integer actor7;
	@Column("actor8")
	private Integer actor8;
	@Column("actor9")
	private Integer actor9;
	@Column("actor0")
	private Integer actor0;
	public Integer getBusinessId() {
		return businessId;
	}
	public void setBusinessId(Integer businessId) {
		this.businessId = businessId;
	}
	public Integer getActor1() {
		return actor1;
	}
	public void setActor1(Integer actor1) {
		this.actor1 = actor1;
	}
	public Integer getActor2() {
		return actor2;
	}
	public void setActor2(Integer actor2) {
		this.actor2 = actor2;
	}
	public Integer getActor3() {
		return actor3;
	}
	public void setActor3(Integer actor3) {
		this.actor3 = actor3;
	}
	public Integer getActor4() {
		return actor4;
	}
	public void setActor4(Integer actor4) {
		this.actor4 = actor4;
	}
	public Integer getActor5() {
		return actor5;
	}
	public void setActor5(Integer actor5) {
		this.actor5 = actor5;
	}
	public Integer getActor6() {
		return actor6;
	}
	public void setActor6(Integer actor6) {
		this.actor6 = actor6;
	}
	public Integer getActor7() {
		return actor7;
	}
	public void setActor7(Integer actor7) {
		this.actor7 = actor7;
	}
	public Integer getActor8() {
		return actor8;
	}
	public void setActor8(Integer actor8) {
		this.actor8 = actor8;
	}
	public Integer getActor9() {
		return actor9;
	}
	public void setActor9(Integer actor9) {
		this.actor9 = actor9;
	}
	public Integer getActor0() {
		return actor0;
	}
	public void setActor0(Integer actor0) {
		this.actor0 = actor0;
	}
	
	
}
