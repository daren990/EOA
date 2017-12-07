package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

public class EduStudentSignTemp {

	private Integer id;

	private Integer studentId;

	private String studentName;

	private Integer courseId;

	private String courseName;

	private Integer teacherId;

	private String teacherName;

	private Integer scheduleId;

	private Date checkTime;

	private Date startTime;

	private Date endTime;

	private Integer studentIsRate;

	private Integer teacherIsRate;

	private Integer isCome;

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

	public Integer getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Integer teacherId) {
		this.teacherId = teacherId;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}

	public Integer getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Integer scheduleId) {
		this.scheduleId = scheduleId;
	}

	public Date getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(Date checkTime) {
		this.checkTime = checkTime;
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

	@Override
	public String toString() {
		return "EduStudentSignTemp [id=" + id + ", studentId=" + studentId + ", studentName=" + studentName
				+ ", courseId=" + courseId + ", courseName=" + courseName + ", teacherId=" + teacherId
				+ ", teacherName=" + teacherName + ", scheduleId=" + scheduleId + ", startTime=" + startTime
				+ ", endTime=" + endTime + ", studentIsRate=" + studentIsRate + ", teacherIsRate=" + teacherIsRate
				+ ", isCome=" + isCome + "]";
	}

}
