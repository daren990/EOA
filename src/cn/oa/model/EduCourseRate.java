package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("edu_course_rate")
public class EduCourseRate {

	@Id
	@Column("id")
	private Integer id;

	@Column("sign_id")
	private Integer signId;

	@Column("teacher_id")
	private Integer teacherId;

	@Column("teacher_name")
	private String teacherName;

	@Column("course_id")
	private Integer courseId;

	@Column("course_name")
	private String courseName;

	@Column("schedule_id")
	private Integer scheduleId;

	@Column("student_id")
	private Integer studentId;

	@Column("student_Name")
	private String studentName;

	@Column("student_rateDetail")
	private String studentRateDetail;

	@Column("teacher_rateDetail")
	private String teacherRateDetail;

	@Column("mark")
	private Integer mark;

	@Column("student_rateTime")
	private Date studentRateTime;

	@Column("teacher_rateTime")
	private Date teacherRateTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getSignId() {
		return signId;
	}

	public void setSignId(Integer signId) {
		this.signId = signId;
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

	public String getStudentRateDetail() {
		return studentRateDetail;
	}

	public void setStudentRateDetail(String studentRateDetail) {
		this.studentRateDetail = studentRateDetail;
	}

	public String getTeacherRateDetail() {
		return teacherRateDetail;
	}

	public void setTeacherRateDetail(String teacherRateDetail) {
		this.teacherRateDetail = teacherRateDetail;
	}

	public Integer getMark() {
		return mark;
	}

	public void setMark(Integer mark) {
		this.mark = mark;
	}

	public Date getStudentRateTime() {
		return studentRateTime;
	}

	public void setStudentRateTime(Date studentRateTime) {
		this.studentRateTime = studentRateTime;
	}

	public Date getTeacherRateTime() {
		return teacherRateTime;
	}

	public void setTeacherRateTime(Date teacherRateTime) {
		this.teacherRateTime = teacherRateTime;
	}

}
