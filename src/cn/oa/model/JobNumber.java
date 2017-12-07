package cn.oa.model;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("sec_jobNumber_pre")
public class JobNumber {
	@Id
	@Column("pre_id")
	private Integer preId;
	@Column("corp_id")
	private Integer corpId;
	@Column("job_pre")
	private Integer jobPre;
	public Integer getPreId() {
		return preId;
	}
	public void setPreId(Integer preId) {
		this.preId = preId;
	}
	public Integer getCorpId() {
		return corpId;
	}
	public void setCorpId(Integer corpId) {
		this.corpId = corpId;
	}
	public Integer getJobPre() {
		return jobPre;
	}
	public void setJobPre(Integer jobPre) {
		this.jobPre = jobPre;
	}
	
}
