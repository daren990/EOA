package cn.oa.service.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.oa.model.EduStudent;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopWechatrelation;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Page;

public interface ClientService {
	
	/***
	 * 插入或全量更新一个客户的基本信息，不包含关联信息
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean addOrUpdate(ShopClient client) throws Exception;
	
	
	/***
	 * 根据id定位到某一个客户，增量修改关联信息，比如公司,如果不存在id，则视为新增
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean buildRelation(ShopClient client) throws Exception;
	
	
	
	/***
	 * 根据客户的各种信息查询基本信息，比如客户的id，手机号+名称，手机号
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public ShopClient selectOne(ShopClient client, Integer status) throws Exception;
	
	
	/***
	 * 组合各种条件查询客户的基本信息(分页)，支持的条件有：学生的id,公司id+学生的id,公司的id,客户的名称,客户的手机号码
	 * key的格式为：类的全路径+属性名，比如要查询学生id为1的客户，那么条件为map.put(cn.oa.model.Edustudent.id, 1)
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public Page<ShopClient> selectAll(Map<String, Object> map, Page<ShopClient> page) throws Exception;

	/***
	 * 根据学生的id查询客户基本信息	
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public List<ShopClient> selectAll(EduStudent student) throws Exception;

	/***
	 * 根据客户的id查询其所属的公司
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public List<Org> selectCorpsAll(ShopClient client) throws Exception;
	
	/***
	 * 根据id禁用或启用客户
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean able(Integer[] arr, Integer status) throws Exception;
	
	
	/**
	 * 根据课程的id查找客户，原理是先根据课程找学生，再根据学生找家长，返回的Map集合中的key是课程的id
	 * @param courseIds
	 * @return
	 */
	public Map<Integer, List<ShopClient>> getAll(Set<Integer> courseIds);
	public Map<Integer, List<ShopClient>> getAll(Set<Integer> courseIds, Set<Integer> shopClientIds);//将客户的id抽取出来
	
	/**
	 * 找到客户对应的所有的openid，key是客户的id,value是orgid和openid的键值对
	 */
	public Map<Integer, Map<Integer, String>> getOpenIdAndOrgId(Set<Integer> clientIds);
	
//--------------------微信相关的方法--------------------------------

	/**
	 * @param org_id 公司id
	 * @param xwCode 公司微信code
	 * @return [-1 参数错误]/[1 需要重定向获取code]/[2 未登记信息,需要转跳登记页面]/[3 获取到客户]
	 * @throws Exception
	 */
	public MapBean getShopClient(String org_id, String xwCode) throws Exception;
	
	
//--------------------无用--------------------------------
	/***
	 * 通过手机号判断该客户是否通过验证了，返回null表示该手机号没有注册，如果ShopClient对象中的phoneVLD字段的值为0，说明还没验证成功，需要发送验证码
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public ShopClient validate(ShopClient client) throws Exception;
	
	/***
	 * 通过手机号定位到数据库的某个客户，验证客户信息中的code是否跟传递进来的code相同,验证通过后会更新客户的phoneVLD字段
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public String validate(ShopClient client, String code) throws Exception;
	
	/***
	 * 根据手机号简单地注册一个客户，code是预设的验证码，客户一开始的status为0
	 * @param student
	 * @return
	 * @throws Exception
	 */
	public boolean validateAdd(ShopClient client, String code) throws Exception;
	

}
