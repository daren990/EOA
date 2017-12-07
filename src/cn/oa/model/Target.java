package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

/**
 * 绩效目标.
 * 
 * @author JELLEE@Yeah.Net
 */
@Table("exm_target")
public class Target {

	@Column("perform_id")
	private Integer performId;
	@Column("content")
	private String content;
	@Column("grade")
	private String grade;
	@Column("weight")
	private Integer weight;
	
	@Column("my_score")
	private Double my_score;
	@Column("my_score_grade")
	private String my_score_grade;
	
	@Column("score")
	private Double score;
	@Column("score_grade")
	private String score_grade;
	
	@Column("manscore")
	private Double manscore;
	@Column("manscore_grade")
	private String manscore_grade;


	
	@Readonly
	@Column
	private Integer userId;
	@Readonly
	@Column
	private Integer approved;
	@Readonly
	@Column
	private Integer version;
	@Readonly
	@Column("first_step")
	private Integer firstStep;
	@Readonly
	@Column("first_referer")
	private Integer firstReferer;
	@Readonly
	@Column("second_step")
	private Integer secondStep;
	@Readonly
	@Column("second_referer")
	private Integer secondReferer;
	

	public Target() {
	}

	public Target(Integer performId, String content,String grade,Integer weight,
			Double my_score,String my_score_grade, Double score,String score_grade, Double manscore, String manscore_grade) {
		this.performId = performId;
		this.content = content;
		this.grade = grade;
		this.weight = weight;
		this.my_score = my_score;
		this.my_score_grade = my_score_grade;
		this.score = score;
		this.score_grade=score_grade;
		this.manscore = manscore;
		this.manscore_grade = manscore_grade;
	
	
		
	}

	public Integer getPerformId() {
		return performId;
	}

	public void setPerformId(Integer performId) {
		this.performId = performId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getgrade() {
		return grade;
	}

	public void setgrade(String grade) {
		this.grade = grade;
	}

	
	
	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	
	
	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getApproved() {
		return approved;
	}

	public void setApproved(Integer approved) {
		this.approved = approved;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Double getManscore() {
		return manscore;
	}

	public void setManscore(Double manscore) {
		this.manscore = manscore;
	}

	
	
	
	
	


	public Double getMy_score() {
		return my_score;
	}

	public void setMy_score(Double my_score) {
		this.my_score = my_score;
	}

	public String getMy_score_grade() {
		return my_score_grade;
	}

	public void setMy_score_grade(String my_score_grade) {
		this.my_score_grade = my_score_grade;
	}

	public String getScore_grade() {
		return score_grade;
	}

	public void setScore_grade(String score_grade) {
		this.score_grade = score_grade;
	}

	public String getManscore_grade() {
		return manscore_grade;
	}

	public void setManscore_grade(String manscore_grade) {
		this.manscore_grade = manscore_grade;
	}

	public Integer getFirstStep() {
		return firstStep;
	}

	public void setFirstStep(Integer firstStep) {
		this.firstStep = firstStep;
	}

	public Integer getFirstReferer() {
		return firstReferer;
	}

	public void setFirstReferer(Integer firstReferer) {
		this.firstReferer = firstReferer;
	}

	public Integer getSecondStep() {
		return secondStep;
	}

	public void setSecondStep(Integer secondStep) {
		this.secondStep = secondStep;
	}

	public Integer getSecondReferer() {
		return secondReferer;
	}

	public void setSecondReferer(Integer secondReferer) {
		this.secondReferer = secondReferer;
	}

	
	
	
	
}
