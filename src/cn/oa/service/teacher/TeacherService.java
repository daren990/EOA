package cn.oa.service.teacher;

import java.util.List;
import java.util.Map;

import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.model.EduStudent;
import cn.oa.model.EduTeacher;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopGoods;
import cn.oa.utils.web.Page;

public interface TeacherService {
	/***
	 * 组合各种条件查询老师的基本信息(分页)，支持的条件有：公司的id，真实的名字,学科，在指定时间内需要上课的，
	 * key的格式为：类的全路径+属性名，比如要查询公司id为1的老师，那么条件为map.put(cn.oa.model.Org.id, 1)
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public Page<EduTeacher> selectAll(Map<String, Object> map, Page<EduTeacher> page) throws Exception;
	
	/***
	 * 根据老师的id查询基本信息	
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public EduTeacher selectOne(EduTeacher teacher) throws Exception;
	
	/***
	 * 插入或全量更新一个老师的基本信息,
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean addOrUpdateOne(EduTeacher teacher) throws Exception;
	
	/***
	 * 根据id禁用或启用老师
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean able(Integer[] arr, Integer status) throws Exception;
	
	/***
	 * 根据公司的id获取公司的资料
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public Org selectCorpOne(Org corp) throws Exception;
	
	/***
	 * 根据公司的id获取所有老师,不包含合作的老师
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public List<EduTeacher> selectAll(Org corp) throws Exception;
	
	/***
	 * 为某家公司新增合作类型的老师
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean insertCoopTeacher(Integer corpId, Integer coopTeacherId, Integer coopCorpId) throws Exception;
	
	/***
	 * 查询合作类型老师和公司之间的关系，即根据公司的id获取所有合作的老师的id;corpId为null时，查询所有的老师
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public List<ShopGoods> selectIds(Integer corpId) throws Exception;
		
}
