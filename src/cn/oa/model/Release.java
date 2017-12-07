package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Converts;

/**
 * 绩效发布.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("exm_release")
public class Release {
	
	public static final int WEIGHT = 0;
	public static final int SCORE = 1;
	
	@Id
	@Column("release_id")
	private Integer releaseId;
	@Column("user_id")
	private Integer userId;
	@Column("corp_id")
	private Integer corpId;
	@Column("release_name")
	private String releaseName;
	@Column("start_date")
	private Date startDate;
	@Column("end_date")
	private Date endDate;
	@Column("version")
	private Integer version;
	@Column("status")
	private Integer status;
	@Column("create_time")
	private Date createTime;
	@Column("modify_time")
	private Date modifyTime;
	@Column("release_start_date")
	private Date releaseStartDate;
	@Column("release_end_date")
	private Date releaseEndDate;

	@Readonly
	@Column("corp_name")
	private String corpName;
	@Readonly
	@Column("true_name")
	private String trueName;
	
	private List<User> users;

	public Integer getReleaseId() {
		return releaseId;
	}

	public void setReleaseId(Integer releaseId) {
		this.releaseId = releaseId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getCorpId() {
		return corpId;
	}

	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}

	public String getReleaseName() {
		return releaseName;
	}

	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
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

	public String getCorpName() {
		return corpName;
	}

	public void setCorpName(String corpName) {
		this.corpName = corpName;
	}

	public String getTrueName() {
		return trueName;
	}

	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
	

	public Date getReleaseStartDate() {
		return releaseStartDate;
	}

	public void setReleaseStartDate(Date releaseStartDate) {
		this.releaseStartDate = releaseStartDate;
	}

	public Date getReleaseEndDate() {
		return releaseEndDate;
	}

	public void setReleaseEndDate(Date releaseEndDate) {
		this.releaseEndDate = releaseEndDate;
	}

	public String getUserNames() {
		return Converts.str(
				Converts.array(User.class, String.class, getUsers(), "trueName"),
				",");
	}

}
