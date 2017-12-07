package cn.oa.service.student;


import java.util.List;
import java.util.Map;

import org.nutz.dao.sql.Criteria;

import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentWechatrelation;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopWechatrelation;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Page;
import cn.oa.web.wx.Config;

public interface StudentService {
	
	/***
	 * 插入或全量更新一个学生的信息,包括关联信息，比如客户和课程,公司，而且注意会自动增量更新客户和公司的关系
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean addOrUpdateOne(EduStudent student) throws Exception;
	
	/***
	 * 根据id定位到某一个学生，增量修改关联信息，比如客户和课程，公司，另外还有客户和公司之间的关系
	 * 如果学生不存在id，则视为新增学生
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean buildRelationOfStudent(EduStudent student) throws Exception;
	
	/***
	 * 删除某个学生跟一些课程的关联关系
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean deleteRelantionWithCourses(EduStudent student, List<ShopGoods> courses);
	
	/***
	 * 根据id禁用或启用学生
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean able(Integer[] arr, Integer status) throws Exception;
	
	/***
	 * 组合各种条件查询学生的基本信息(分页)，支持的条件有：学校的id,学生的名称,客户的id,学生的学号,学生的id
	 * key的格式为：类的全路径+属性名，比如要查询公司id为1的学生，那么条件为map.put(cn.oa.model.Org.id, 1)
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public Page<EduStudent> selectAll(Map<String, Object> map, Page<EduStudent> page) throws Exception;
	
	/***
	 * 根据id获取学生的详细信息
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public EduStudent selectOne(EduStudent student) throws Exception;
	
	/***
	 * 根据客户id获取其所注册的学生
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public List<EduStudent> selectAll(ShopClient client) throws Exception;
	public List<EduStudent> selectAll(ShopClient client, Integer status) throws Exception;//控制获取学生的状态
	
	/***
	 * 根据客户id+公司id获取学生
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public List<EduStudent> selectAll(ShopClient client, Org org) throws Exception;
	
	/***
	 * 查找所有非禁用的培训机构
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public List<Org> selectCorpsAll() throws Exception;
	
	/***
	 * 查找学生所属的培训机构
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public List<Org> selectCorpsAll(EduStudent student) throws Exception;
	
//--------打卡相关-------------------------
	
	/***
	 * 学生打卡
	 */
	public boolean sign(EduStudent student) throws Exception;
//------------------微信相关的方法------------------------
	/**
	 * @param org_id 公司id
	 * @param xwCode 公司微信code
	 * @return -1 参数错误
	 * @return 1 需要重定向获取code
	 * @return 2 未登记信息,需要转跳登记页面
	 * @return 3 获取到客户
	 */
	public MapBean getEduStudent(String org_id, String xwCode) throws Exception;
	
	/***
	 * 根据org_id和openId查找学生的信息
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public EduStudentWechatrelation getByOrgIdAndOpenId(String org_id, String openId); 
	
	/***
	 * 查找更加org_id和student_id查询已经存在的微信信息
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public EduStudentWechatrelation getWechatConfig(String org_id, Integer studentId); 
	
	/***
	 * 更新或插入EduStudentWechatrelation
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean insertOrUpdate(EduStudentWechatrelation studentWechatrelation); 
	
	/***
	 * 获取学生的所有微信信息
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public List<EduStudentWechatrelation> selectWechatrelation(EduStudent student); 
	


	/***
	 * 查找学生所属的培训机构
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public void insertSign() throws Exception;
	

}
