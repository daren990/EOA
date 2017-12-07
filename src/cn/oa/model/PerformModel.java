package cn.oa.model;

import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

@Table("exm_perform_model")
public class PerformModel {
	@Id
	@Column("model_id")
	private Integer modelId;
	@Column("model_name")
	private String modelName;
	@Column("corp_id")
	private Integer corpId;
	@Column("model_users")
	private String modelUsers;
	@Column("first_step")
	private Integer firstStep;
	@Column("second_step")
	private Integer secondStep;
	@Column("third_step")
	private Integer thirdStep;
	@Column("first_referer")
	private Integer firstReferer;
	@Column("second_referer")
	private Integer secondReferer;
	@Column("third_referer")
	private Integer thirdReferer;
	
	
	@Readonly
	@Column("org_name")
	private String orgName;
	
	@Readonly
	@Column("first_name")
	private String firstName;
	
	@Readonly
	@Column("second_name")
	private String secondName;
	
	@Readonly
	@Column("third_name")
	private String thirdName;
	
	private List<User> users;
	
	
	public Integer getModelId() {
		return modelId;
	}
	public void setModelId(Integer modelId) {
		this.modelId = modelId;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public Integer getCorpId() {
		return corpId;
	}
	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

	public String getModelUsers() {
		return modelUsers;
	}
	public void setModelUsers(String modelUsers) {
		this.modelUsers = modelUsers;
	}
	public Integer getFirstStep() {
		return firstStep;
	}
	public void setFirstSetp(Integer firstStep) {
		this.firstStep = firstStep;
	}
	public Integer getSecondStep() {
		return secondStep;
	}
	public void setSecondStep(Integer secondStep) {
		this.secondStep = secondStep;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	public void setFirstStep(Integer firstStep) {
		this.firstStep = firstStep;
	}

	public Integer getThirdStep() {
		return thirdStep;
	}
	public void setThirdStep(Integer thirdStep) {
		this.thirdStep = thirdStep;
	}
	public Integer getFirstReferer() {
		return firstReferer;
	}
	public void setFirstReferer(Integer firstReferer) {
		this.firstReferer = firstReferer;
	}
	public Integer getSecondReferer() {
		return secondReferer;
	}
	public void setSecondReferer(Integer secondReferer) {
		this.secondReferer = secondReferer;
	}
	public Integer getThirdReferer() {
		return thirdReferer;
	}
	public void setThirdReferer(Integer thirdReferer) {
		this.thirdReferer = thirdReferer;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	public String getThirdName() {
		return thirdName;
	}
	public void setThirdName(String thirdName) {
		this.thirdName = thirdName;
	}
	public String getUserNames() {
		return Converts.str(
				Converts.array(User.class, String.class, getUsers(), "trueName"),
				",");
	}

	
	
}
