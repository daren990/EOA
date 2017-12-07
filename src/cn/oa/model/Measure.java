package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 绩效衡量.
 * 绩效评分标准
 * @author JELLEE@Yeah.Net
 */
@Table("conf_measure_item")
public class Measure {

	@Column("org_id")
	private Integer orgId;
	@Column("measure_name")
	private String measureName;
	@Column("score_name")
	private String scoreName;
	@Column("score")
	private Float score;
	@Column("coefficient")
	private Float coefficient;

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public String getMeasureName() {
		return measureName;
	}

	public void setMeasureName(String measureName) {
		this.measureName = measureName;
	}

	public String getScoreName() {
		return scoreName;
	}

	public void setScoreName(String scoreName) {
		this.scoreName = scoreName;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public Float getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(Float coefficient) {
		this.coefficient = coefficient;
	}

}
