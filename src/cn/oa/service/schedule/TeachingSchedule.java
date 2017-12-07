package cn.oa.service.schedule;

import java.util.Date;
import java.util.List;

import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentCourse;
import cn.oa.model.EduStudentDisplay;
import cn.oa.model.EduTeacher;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopGoods;
import cn.oa.model.StudentCourseCount;
import cn.oa.utils.web.Page;

public interface TeachingSchedule {
	/**
	 * 根据课程生成向排课表批量插入记录
	 * 已经存在事务，则融入事务中，返回false或抛出异常的时候自动回滚并提交事务，返回true不会自动提交事务
	 * @param course
	 * @return
	 */
	public boolean teachingSchedule(ShopGoods course, boolean clear) throws Exception; 
	
	/**
	 * 将某个课程的课程安排将启用变为禁用（作废）
	 * @param course
	 * @return
	 */
	public boolean deleteTeachingSchedule(Integer courseId) throws Exception; 
	
	/**
	 * 根据课程安排找到需要标记过时的课程的ids
	 * @param course
	 * @return
	 */
	public List<Integer> findOutdatedCourseIds();
	
	/**
	 * 据时间段来查询排课记录
	 * @return
	 */
	public List<EduTeachingSchedule> selectAll(String start, String end);
	
	
	/**
	 * 查询某些课程在指定时间段内的安排情况,如果时间段内没有指定的课程，那么EduTeachingSchedule对象中则没有任何一个对象指向该课程
	 * 课程安排对象中关联了老师对象
	 * @return
	 */
	public List<EduTeachingSchedule> selectAll(String start, String end, List<ShopGoods> courses);
	
	
	
	/**	
	 * 查询某个学生在指定时间点未来或过去几天的的课程安排，该方法调用了上面的selectAll()方法
	 */
	public List<EduTeachingSchedule> selectAll(int days, Date timePoint, EduStudent student);
	

	/**	
	 * 查询某个学生某个七天内的课程安排，该方法调用了上面的selectAll()方法
	 */
	public List<EduTeachingSchedule> selectAll2(int days, EduStudent student); 

	/**
	 * 根据明天的课程安排发送微信通知
	 * @return
	 */
	public boolean sendWeixin() throws Exception;
	
	/**
	 * 查询当前是第几节课
	 * @return
	 */
	public List<EduStudentDisplay> scheduleCount(Integer course_id);
	
	/**
	 * 查询某一天需要上课的所有老师的id，以及这个老师的课程安排
	 * @return
	 */
	public String getScheduleTimeById(Integer scheduleId);
	
//-------------------跟老师相关的------------------------------------	
	/**
	 * 查询指定时间段内，某个老师的课程安排，如果Org不为null，说明是这个公司下的课程安排
	 * @return
	 */
	public List<EduTeacher> selectAll(String start, String end, EduTeacher teacher, Org crop);
	
	/**
	 * 查询指定时间内需要上课的所有老师的id，如果Org为null，说明不用公司作限制（即只查询公司所拥有）
	 * @return
	 */
	public List<Integer> selectBySchedule(String start, String end, Org corp) throws Exception;


	
//------------------------无用的-----------------------------	
	/**
	 * 查询某天所在的周中需要上课的所有老师的id，以及这个老师在这个周的课程安排,课程安排中包含课程的信息
	 * @return
	 */
	public List<EduTeacher> selectByDayOfWeek(Date day);
	
	/**
	 * 据时间段来查询排课记录
	 * @return
	 */
	public List<EduTeachingSchedule> selectAll(Date start, Date end);

	public boolean checkSchedule(List<EduTeachingSchedule> eduTeachingSchedules, Integer eduTeacherId);

	public List<EduTeachingSchedule> getWorkDay(String couTime, Integer couCount, Date startDate);

	/**
	 * 查询某个公司下、某个学科下、指定时间内，一些课程的安排,当中调用了上面的selectAll(String start, String end, List<EduCourse> courses);
	 * 该方法失效，因为存放科目信息的表不同了
	 * @return
	 */
	public List<EduTeachingSchedule> selectAll(String start, String end, Org corp, List<ShopGoods> subjects);

	/**
	 * 查询某一天需要上课的所有老师的id，以及这个老师的课程安排
	 * @return
	 */
	public List<EduTeacher> selectByDay(Date day);
	
}
