package cn.oa.service.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Status;
import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentClient;
import cn.oa.model.EduStudentCorp;
import cn.oa.model.EduStudentCourse;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopClientCorp;
import cn.oa.model.ShopWechatrelation;
import cn.oa.repository.Mapper;
import cn.oa.service.WxClientService;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Page;
import cn.oa.web.wx.Config;

@IocBean(name="clientService")
public class ClientServiceImpl implements ClientService {

	@Inject
	private Dao dao;
	
	@Inject
	private Mapper mapper;
	
	@Inject
	private WxClientService wxClientService;

	
	@Override
	public boolean addOrUpdate(ShopClient client) throws Exception {
		if(client.getId() == null){
			dao.insert(client);
		}else{
			dao.updateIgnoreNull(client);
		}
		return true;
	}
	
	@Override
	public ShopClient selectOne(ShopClient client, Integer status) throws Exception {
		SimpleCriteria cri = Cnd.cri();
		if(status == null){
			//不作任何限制
		}else if(status.equals(Status.DISABLED) ){
			cri.where().and("status", "=", Status.DISABLED);
		}else if (status.equals(Status.ENABLED) ){
			cri.where().and("status", "=", Status.ENABLED);
		}
		if(client.getId() != null){
			cri.where().and("id", "=", client.getId());
			return dao.fetch(ShopClient.class, cri);
		}else if(client.getTelephone() != null && client.getTruename() != null){
			cri.where().and("telephone", "=", client.getTelephone()).and("truename", "=", client.getTruename());
			return dao.fetch(ShopClient.class, cri);
		}else if(client.getTelephone() != null){
			cri.where().and("telephone", "=", client.getTelephone());
			return dao.fetch(ShopClient.class, cri);
		}
		return null;
	}

	@Override
	public Page<ShopClient> selectAll(Map<String, Object> map,
			Page<ShopClient> page) throws Exception {
		SimpleCriteria cri = Cnd.cri();
		if(map.containsKey("cn.oa.model.EduStudent.id") && map.size() == 1){
			Asserts.isNull(map.get("cn.oa.model.EduStudent.id"), "没有指定学生的id");
			
			List<EduStudentClient> eduStudentClients = dao.query(EduStudentClient.class, Cnd.where("eduStudentId", "=", map.get("cn.oa.model.EduStudent.id")));
			if(eduStudentClients == null || eduStudentClients.size() == 0 ){
				return page;
			}
			String[] ids = Converts.array(EduStudentClient.class, String.class, eduStudentClients, "shopClientId");
			cri.where().and("status", "=", Status.ENABLED).and("id", "in", ids);
	
		}
		
		if(map.containsKey("cn.oa.model.EduStudent.id") && map.containsKey("cn.oa.model.Org.orgId")){	
			Asserts.isNull(map.get("cn.oa.model.EduStudent.id"), "没有指定学生或公司的id");
			Asserts.isNull(map.get("cn.oa.model.Org.orgId"), "没有指定学生或公司的id");
			
			List<EduStudentClient> eduStudentClients = dao.query(EduStudentClient.class, Cnd.where("eduStudentId", "=", map.get("cn.oa.model.EduStudent.id")));
			if(eduStudentClients == null || eduStudentClients.size() == 0 ){
				return null;
			}
			Integer[] conectIds = Converts.array(EduStudentClient.class, Integer.class, eduStudentClients, "shopClientId");
			
			List<ShopClientCorp> shopClientCorps = dao.query(ShopClientCorp.class, Cnd.where("corpId", "=", map.get("cn.oa.model.Org.orgId")));
			if(shopClientCorps == null || shopClientCorps.size() == 0 ){
				return page;
			}
			Integer[] ids = Converts.array(ShopClientCorp.class, Integer.class, shopClientCorps, "shopClientId");
			
			//ids和conectIds的交集
			List<Integer> clientAndCorpIds = new ArrayList<Integer>();
			
			for(int x = 0; x < ids.length; x++){
				for(Integer conectId : conectIds){
					if(ids[x].equals(conectId)){
						clientAndCorpIds.add(conectId);
						break;
					}
				}
			}
			cri.where().and("status", "=", Status.ENABLED).and("id", "in", clientAndCorpIds);
		}
		if(map.containsKey("cn.oa.model.Org.orgId")){
			if(map.get("cn.oa.model.Org.orgId") != null){
				List<ShopClientCorp> shopClientCorps = dao.query(ShopClientCorp.class, Cnd.where("corpId", "=", map.get("cn.oa.model.Org.orgId")));
				if(shopClientCorps == null || shopClientCorps.size() == 0 ){
					return page;
				}
				Integer[] ids = Converts.array(ShopClientCorp.class, Integer.class, shopClientCorps, "shopClientId");
				cri.where().and("id", "in", ids);
			}else{
				//应该先进行权限的校验,待实现
			}
		}
		if(map.containsKey("cn.oa.model.ShopClient.truename")){
			Asserts.isNull(map.get("cn.oa.model.ShopClient.truename"), "没有指定客户的名称");
			cri.where().and("truename", "like", map.get("cn.oa.model.ShopClient.truename"));
	
		}
		if(map.containsKey("cn.oa.model.EduTeacher.telephone")){
			Asserts.isNull(map.get("cn.oa.model.EduTeacher.telephone"), "没有指定客户的手机号码");
			cri.where().and("telephone", "=", map.get("cn.oa.model.EduTeacher.telephone"));
	
		}
		page = mapper.page(ShopClient.class, page, "Client.count", "Client.index",  cri);
		return page;
	}

	@Override
	public List<ShopClient> selectAll(EduStudent student) throws Exception {
		Asserts.isNull(student.getId(), "没有指定学生的id");
		List<EduStudentClient> eduStudentClients = dao.query(EduStudentClient.class, Cnd.where("eduStudentId", "=",student.getId()));
		if(eduStudentClients == null || eduStudentClients.size() == 0 ){
			return null;
		}
		String[] ids = Converts.array(EduStudentClient.class, String.class, eduStudentClients, "shopClientId");
		return dao.query(ShopClient.class, Cnd.where("id", "in", ids).and("status", "=", Status.ENABLED));
	}

	@Override
	public List<Org> selectCorpsAll(ShopClient client) throws Exception {
		List<ShopClientCorp> shopClientCorps = dao.query(ShopClientCorp.class, Cnd.where("shopClientId", "=", client.getId()));
		if(shopClientCorps == null || shopClientCorps.size() == 0 ){
			return null;
		}
		String[] ids = Converts.array(ShopClientCorp.class, String.class, shopClientCorps, "corpId");
		return dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("orgId", "in", ids));
	}
	
	@Override
	public boolean able(Integer[] arr, Integer status) throws Exception {
		dao.update(ShopClient.class, Chain.make("status", status), Cnd.where("id", "in", arr));
		return true;
	}

	@Override
	public boolean buildRelation(final ShopClient client) throws Exception {
		if(client.getId() != null){
			final ShopClient c = dao.fetch(ShopClient.class, client.getId());
			if(c.getId() == null){
				throw new RuntimeException("不存在该id的客户");
			}
			Trans.exec(new Atom() {
				@Override
				public void run() {
					//查询已经存在的关联关系
					List<ShopClientCorp> shopClientCorps = dao.query(ShopClientCorp.class, Cnd.where("shopClientId", "=", c.getId()));
					if(shopClientCorps == null ){shopClientCorps = new ArrayList<ShopClientCorp>();}

					//增量更新公司和客户的关系
					List<Org> corps = client.getCorps();
					if(corps != null){
						List<Org> remove1 = new ArrayList<Org>();
						
						for(Org corp : corps){
							//判断数据库中是否已经存在客户和公司的关联
							for(ShopClientCorp existShopClientCorp : shopClientCorps){
								if(corp.getOrgId().equals(existShopClientCorp.getCorpId())){
									remove1.add(corp);
								}
							}
						}
						corps.removeAll(remove1);
						changeRelation(corps, c.getId(), Org.class, false);
					}

				}
			});
		}else{
			Trans.exec(new Atom() {
				@Override
				public void run() {
					dao.insert(client);
					List<Org> corps = client.getCorps();
					Asserts.isNull(corps, "请指定新增客户所属的公司");
					changeRelation(corps, client.getId(), Org.class, false);
				}
			});

		}
		return true;
	}
	
	 /**
	  * 全量或增量更新客户的关联关系
	  * @param list
	  * @param clientId
	  * @param type
	  * @param clear
	  */
	private <T> void changeRelation(Collection<T> list, Integer clientId, Class<T> type, boolean clear){
		if(type.toString().contains("cn.oa.model.Org")){
			List<ShopClientCorp> shopClientCorps = new ArrayList<ShopClientCorp>();
			for(T c : list){
				Org corp = (Org)c;
				ShopClientCorp shopClientCorp = new ShopClientCorp();
				shopClientCorp.setShopClientId(clientId);
				shopClientCorp.setCorpId(corp.getOrgId());
				shopClientCorps.add(shopClientCorp);
			}
			if(clear){
				dao.clear(ShopClientCorp.class, Cnd.where("shopClientId", "=", clientId));
			}
			dao.fastInsert(shopClientCorps);
		}
	}

	@Override
	public ShopClient validate(ShopClient client) throws Exception {
		if(client.getTelephone() != null){
			ShopClient shopClient = dao.fetch(ShopClient.class, client.getTelephone());
			if(shopClient == null){
				return null;
			}else{
				return shopClient;
			}
		}
		return null;
	}
	
	@Override
	public String validate(ShopClient client, String code) throws Exception {
		if(client.getTelephone() != null){
			ShopClient shopClient = dao.fetch(ShopClient.class, client.getTelephone());
			if(shopClient == null){
				return "false";
			}else{
				if(code != null && shopClient.getMsgcode() != null && code.equals(shopClient.getMsgcode())){
					shopClient.setPhoneVLD(1);
					dao.updateIgnoreNull(shopClient);
					return "true";
				}
			}
		}
		return "false";
	}

	@Override
	public boolean validateAdd(ShopClient client, String code) throws Exception {
		Asserts.isNull(client.getTelephone(), "请输入客户的手机号");
		Asserts.isNull(code, "请预设验证码");
		client.setStatus(0);
		client.setMsgcode(code);
		dao.insert(client);
		return true;
	}

	/**
	 * @param org_id 公司id
	 * @param xwCode 公司微信code
	 * @return -1 参数错误
	 * @return 1 需要重定向获取code
	 * @return 2 未登记信息,需要转跳登记页面
	 * @return 3 获取到客户
	 */
	@Override
	public MapBean getShopClient(String org_id, String xwCode)
			throws Exception {
		MapBean mb = new MapBean();
		String openid = null;
		if(xwCode != null || org_id != null || !xwCode.equals("") || !org_id.equals("")){
			Config c = wxClientService.initOrgConfig(org_id);
			openid = wxClientService.getOpenid(xwCode, c);
			//openid为reset，说明调用微信api后无法获取到openid
			if(openid.equals("reset")){
				mb.put("type",1);
				return mb;
			}
		}else{
			mb.put("type",-1);
			return mb;
		}
		mb.put("openid",openid);
		ShopWechatrelation shopWechatrelation = wxClientService.getByOrgIdAndOpenId(org_id, openid);
		if(shopWechatrelation == null){
			mb.put("type",2);
			return mb;
		}
		Integer clientId = shopWechatrelation.getClientId();
		ShopClient shopClient = new ShopClient();
		shopClient.setId(clientId);
		ShopClient sc = selectOne(shopClient, null);
		mb.put("type",3);
		mb.put("shopClient",sc);
		return mb;
	}

	@Override
	public Map<Integer, List<ShopClient>> getAll(Set<Integer> courseIds) {
		Sql sql = Sqls.queryEntity(dao.sqls().get("Client.query.byCourses"));
		
		SimpleCriteria cri = Cnd.cri();
		cri.where().and("cli.status", "=", Status.ENABLED);
		cri.where().and("stu.status", "=", Status.ENABLED);
		cri.where().and("sc.edu_course_id", "in", courseIds);
		sql.setCondition(cri);
		sql.setCallback(Sqls.callback.entities());
		sql.setEntity(dao.getEntity(ShopClient.class));
		dao.execute(sql);
		
		List<ShopClient> shopClients = sql.getList(ShopClient.class);
		Map<Integer, List<ShopClient>> shopClientMap = new HashMap<Integer, List<ShopClient>>();
		if(shopClients != null){
			for(ShopClient shopClient : shopClients){
				List<ShopClient> cs = shopClientMap.get(shopClient.getCourseId());
				if(cs == null){
					cs = new ArrayList<ShopClient>();
					shopClientMap.put(shopClient.getCourseId(), cs);
				}
				cs.add(shopClient);
			}
		}else{
			return null;
		}
		return shopClientMap;
	}

	@Override
	public Map<Integer, List<ShopClient>> getAll(Set<Integer> courseIds,
			Set<Integer> shopClientIds) {
		if(shopClientIds == null){
			return getAll(courseIds);
		}
		
		Sql sql = Sqls.queryEntity(dao.sqls().get("Client.query.byCourses"));
		
		SimpleCriteria cri = Cnd.cri();
		cri.where().and("cli.status", "=", Status.ENABLED);
		cri.where().and("stu.status", "=", Status.ENABLED);
		cri.where().and("sc.edu_course_id", "in", courseIds);
		sql.setCondition(cri);
		sql.setCallback(Sqls.callback.entities());
		sql.setEntity(dao.getEntity(ShopClient.class));
		dao.execute(sql);
		
		List<ShopClient> shopClients = sql.getList(ShopClient.class);
		Map<Integer, List<ShopClient>> shopClientMap = new HashMap<Integer, List<ShopClient>>();
		if(shopClients != null){
			for(ShopClient shopClient : shopClients){
				List<ShopClient> cs = shopClientMap.get(shopClient.getCourseId());
				if(cs == null){
					cs = new ArrayList<ShopClient>();
					shopClientMap.put(shopClient.getCourseId(), cs);
				}
				cs.add(shopClient);
				shopClientIds.add(shopClient.getId());
				
			}
		}else{
			return null;
		}
		return shopClientMap;
	}

	@Override
	public Map<Integer, Map<Integer, String>> getOpenIdAndOrgId(
			Set<Integer> clientIds) {
		Asserts.isNull(clientIds, "请输入客户的ids");
		List<ShopWechatrelation> shopWechatrelations = dao.query(ShopWechatrelation.class, Cnd.where("clientId", "in", clientIds));
		if(shopWechatrelations == null || shopWechatrelations.size() == 0){
			return null;
		}
		Map<Integer, Map<Integer, String>> map = new HashMap<Integer, Map<Integer, String>>();
		for(ShopWechatrelation shopWechatrelation : shopWechatrelations){
			Map<Integer, String> orgIdAndOpenIdMap = map.get(shopWechatrelation.getClientId());
			if(orgIdAndOpenIdMap == null){
				orgIdAndOpenIdMap = new HashMap<Integer, String>();
				map.put(shopWechatrelation.getClientId(), orgIdAndOpenIdMap);
			}
			orgIdAndOpenIdMap.put(shopWechatrelation.getOrgId(), shopWechatrelation.getOpenId());
		}
		return map;
	}



}
