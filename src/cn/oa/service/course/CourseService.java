package cn.oa.service.course;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.oa.model.EduClassify;
import cn.oa.model.EduStudent;
import cn.oa.model.EduTeacher;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopGoodsDisplay;
import cn.oa.utils.web.Page;

public interface CourseService
{
	/**
	 * 根据老师的id和公司的id来查询课程
	 * @param tId
	 * @param cId
	 * @return
	 */
	public Page<ShopGoods> findCourseByTeacherId(String tId,String cId);
	
	/**
	 * 根据学生的id和公司的id来查询课程
	 * @param sId
	 * @param cId
	 * @return
	 */
	public Page<ShopGoods> findCourseByStudentId(String sId,String cId);
	
	/**
	 * 根据老师的id查询课程
	 * @return
	 */
	public List<ShopGoods> findAll(EduTeacher teacher);
	
	/**
	 * 根据学生的id查询课程
	 * @return
	 */
	public List<ShopGoods> findAll(EduStudent student);
	
	/**
	 * 根据公司的id查询课程
	 * @return
	 */
	public List<ShopGoods> findAll(Org corp);
	
	
	/**
	 * 查询合作类型老师和公司之间的关系,如果crop为nul，那么查询所有
	 * @return
	 */
	public List<ShopGoods> findCoop(Integer corp);
	
	/**
	 * 根据传入的课程，查询依赖这个课程的所有合作的课程
	 */
	public Map<Integer, ShopGoods> findNeedDepend(Set<Integer> courseIds, Map<Integer, List<ShopGoods>> shopGoodsMap);
	
//-----------跟课程安排相关的方法---------------------
	/**
	 *	查询一定时间内需要上课的课程，key是需要上课的课程id，value是对应的课程安排
	 * @return
	 */
	public Map<Integer, List<EduTeachingSchedule>> findAll(String start, String end);
	
	
//-----------------------------------------------------------------
	//以下是学科相关的方法
	
	/**
	 * 根据公司的id和学科的id查询课程,该方法失效，因为存放科目信息的表不同了
	 * @return
	 */
	public List<ShopGoods> findAll(Org corp, List<ShopGoods> subjects);

	
	/**
	 * 根据老师的id查询学科,相应的学科注入老师的对象中
	 * @return
	 */
	public List<EduTeacher> findAll(List<EduTeacher> teachers);
	
	/**
	 * 查询所有学科
	 * @return
	 */
	public List<EduClassify> findAllSubjects();

	/**
	 * 根据学生的id以及课程id查询该课程状况
	 * @return
	 */
	public List<ShopGoodsDisplay> findCourseDisplay(Integer student_id,Integer course_id);
	
}
