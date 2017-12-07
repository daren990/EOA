package cn.oa.model;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Readonly;
import org.nutz.dao.entity.annotation.Table;

import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;

@Table("edu_teaching_schedule")
public class EduTeachingSchedule {
	
	@Override
	public String toString() {
		return "EduTeachingSchedule [id=" + id + ", eduClassesId="
				+ eduClassesId + ", eduTeacherId=" + eduTeacherId
				+ ", eduCourseId=" + eduCourseId + ", start=" + start
				+ ", end=" + end + ", location=" + location + "]";
	}

	@Id
	@Column("id")
	private Integer id;
	
	@Column("edu_classes_id")
	private Integer eduClassesId;
	
	@Column("edu_teacher_id")
	private Integer eduTeacherId;
	
	@Column("edu_course_id")
	private Integer eduCourseId;
	
	@Column("start")
	private Date start;
	
	@Column("end")
	private Date end;
	
	@Column("location")
	private String location;
	
	@Column("status")
	private Integer status;
	
	private ShopGoods course;
	
	private EduTeacher teacher;
	
	private Integer minusMonth;
	
	@Readonly
	@Column("c.name")
	private String name;
	@Readonly
	@Column("c.corp_id")
	private Integer corp_id;
	@Readonly
	@Column("c.id")
	private Integer courseId;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getEduClassesId() {
		return eduClassesId;
	}

	public void setEduClassesId(Integer eduClassesId) {
		this.eduClassesId = eduClassesId;
	}

	public Integer getEduTeacherId() {
		return eduTeacherId;
	}

	public void setEduTeacherId(Integer eduTeacherId) {
		this.eduTeacherId = eduTeacherId;
	}

	public Integer getEduCourseId() {
		return eduCourseId;
	}

	public void setEduCourseId(Integer eduCourseId) {
		this.eduCourseId = eduCourseId;
	}
	

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public ShopGoods getCourse() {
		return course;
	}

	public void setCourse(ShopGoods course) {
		this.course = course;
	}

	public EduTeacher getTeacher() {
		return teacher;
	}

	public void setTeacher(EduTeacher teacher) {
		this.teacher = teacher;
	}
	
	public Integer getMinusMonth() {
		return minusMonth;
	}

	public void setMinusMonth(Integer minusMonth) {
		this.minusMonth = minusMonth;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCorp_id() {
		return corp_id;
	}

	public void setCorp_id(Integer corp_id) {
		this.corp_id = corp_id;
	}
	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}

	public Integer getLevel(){
		DateTime date = new DateTime(getStart());
		String level1 = Calendars.str(date, "yyyy-MM-dd 08:00:00");
		String level2 = Calendars.str(date, "yyyy-MM-dd 09:00:00");
		String level3 = Calendars.str(date, "yyyy-MM-dd 10:00:00");
		String level4 = Calendars.str(date, "yyyy-MM-dd 11:00:00");
		String level5 = Calendars.str(date, "yyyy-MM-dd 12:00:00");
		String level6 = Calendars.str(date, "yyyy-MM-dd 13:00:00");
		String level7 = Calendars.str(date, "yyyy-MM-dd 14:00:00");
		String level8 = Calendars.str(date, "yyyy-MM-dd 15:00:00");
		String level9 = Calendars.str(date, "yyyy-MM-dd 16:00:00");
		String level10 = Calendars.str(date, "yyyy-MM-dd 17:00:00");
		String level11 = Calendars.str(date, "yyyy-MM-dd 18:00:00");
		String level12 = Calendars.str(date, "yyyy-MM-dd 19:00:00");
		String level13 = Calendars.str(date, "yyyy-MM-dd 20:00:00");
		String level14 = Calendars.str(date, "yyyy-MM-dd 21:00:00");
		String level15 = Calendars.str(date, "yyyy-MM-dd 22:00:00");
		String level16 = Calendars.str(date, "yyyy-MM-dd 23:00:00");
		
		if(getStart().before(Calendars.parse(level1, Calendars.DATE_TIMES).toDate())) return 1;
		if(getStart().before(Calendars.parse(level2, Calendars.DATE_TIMES).toDate())) return 2;
		if(getStart().before(Calendars.parse(level3, Calendars.DATE_TIMES).toDate())) return 3;
		if(getStart().before(Calendars.parse(level4, Calendars.DATE_TIMES).toDate())) return 4;
		if(getStart().before(Calendars.parse(level5, Calendars.DATE_TIMES).toDate())) return 5;
		if(getStart().before(Calendars.parse(level6, Calendars.DATE_TIMES).toDate())) return 6;
		if(getStart().before(Calendars.parse(level7, Calendars.DATE_TIMES).toDate())) return 7;
		if(getStart().before(Calendars.parse(level8, Calendars.DATE_TIMES).toDate())) return 8;
		if(getStart().before(Calendars.parse(level9, Calendars.DATE_TIMES).toDate())) return 9;
		if(getStart().before(Calendars.parse(level10, Calendars.DATE_TIMES).toDate())) return 10;
		if(getStart().before(Calendars.parse(level11, Calendars.DATE_TIMES).toDate())) return 11;
		if(getStart().before(Calendars.parse(level12, Calendars.DATE_TIMES).toDate())) return 12;
		if(getStart().before(Calendars.parse(level13, Calendars.DATE_TIMES).toDate())) return 13;
		if(getStart().before(Calendars.parse(level14, Calendars.DATE_TIMES).toDate())) return 14;
		if(getStart().before(Calendars.parse(level15, Calendars.DATE_TIMES).toDate())) return 15;
		if(getStart().before(Calendars.parse(level16, Calendars.DATE_TIMES).toDate())) return 16;
		return null;
	}
	
	public Integer getDayOfWeek(){
		return new DateTime(getStart()).dayOfWeek().get();
	}
	
}
