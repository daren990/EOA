package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("edu_student_sign")
public class EduStudentSign {

	@Id
	@Column("id")
	private Integer id;

	@Column("student_id")
	private Integer studentId;

	@Column("student_name")
	private String studentName;

	@Column("course_id")
	private Integer courseId;

	@Column("course_name")
	private String courseName;

	@Column("schedule_id")
	private Integer scheduleId;

	@Column("startTime")
	private Date startTime;

	@Column("endTime")
	private Date endTime;

	@Column("student_isRate")
	private Integer studentIsRate;

	@Column("teacher_isRate")
	private Integer teacherIsRate;

	@Column("isCome")
	private Integer isCome;

	@Column("check_id")
	private Integer checkId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public void setStudentId(Integer studentId) {
		this.studentId = studentId;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public Integer getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Integer scheduleId) {
		this.scheduleId = scheduleId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getStudentIsRate() {
		return studentIsRate;
	}

	public void setStudentIsRate(Integer studentIsRate) {
		this.studentIsRate = studentIsRate;
	}

	public Integer getTeacherIsRate() {
		return teacherIsRate;
	}

	public void setTeacherIsRate(Integer teacherIsRate) {
		this.teacherIsRate = teacherIsRate;
	}

	public Integer getIsCome() {
		return isCome;
	}

	public void setIsCome(Integer isCome) {
		this.isCome = isCome;
	}

	public Integer getCheckId() {
		return checkId;
	}

	public void setCheckId(Integer checkId) {
		this.checkId = checkId;
	}

}
