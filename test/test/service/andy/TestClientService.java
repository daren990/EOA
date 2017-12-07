package test.service.andy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;


import test.Setup;


import cn.oa.model.EduStudent;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.service.client.ClientService;
import cn.oa.service.client.ClientServiceImpl;
import cn.oa.service.student.StudentService;
import cn.oa.service.student.StudentServiceImpl;
import cn.oa.utils.web.Page;


public class TestClientService extends Setup{
	
	//addOrUpdateOne()
	@Test
	public void test() throws Exception{
		ClientService clientService = ioc.get(ClientServiceImpl.class, "clientService");
		ShopClient shopClient = new ShopClient();
		shopClient.setId(1);
		shopClient.setName("小客户");
		clientService.addOrUpdate(shopClient);
	}

	//selectOne(client)
	@Test
	public void selectOne() throws Exception{
		ClientService clientService = ioc.get(ClientServiceImpl.class, "clientService");
		ShopClient shopClient = new ShopClient();
		shopClient.setId(1);
		shopClient = clientService.selectOne(shopClient, 1);
		System.out.println(shopClient);
	}
	
	//selectAll(Map<String, Object> map,Page<ShopClient> page)
	@Test
	public void test2() throws Exception{
		ClientService clientService = ioc.get(ClientServiceImpl.class, "clientService");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cn.oa.model.EduStudent.id", 13);
		Page<ShopClient> page = new Page<ShopClient>(1, 1000);
		page.setAutoCount(false);
		page = clientService.selectAll(map, page);
		System.out.println(page.getResult().size());
	}
	
	//selectAll(EduStudent student)
	@Test
	public void test3() throws Exception{
		ClientService clientService = ioc.get(ClientServiceImpl.class, "clientService");
		EduStudent student = new EduStudent();
		student.setId(13);
		List<ShopClient> clients = clientService.selectAll(student);
		System.out.println(clients);
	}
	
	//buildRelation(ShopClient client)
	@Test
	public void test4() throws Exception{
		ClientService clientService = ioc.get(ClientServiceImpl.class, "clientService");
		ShopClient client = new ShopClient();
		client.setId(3);
		Org corp = new Org();
		corp.setOrgId(276);
		client.setCorps(pack(corp)); 
		clientService.buildRelation(client);
	}
	
	
	private <T> List<T> pack(T element){
		List<T> list = new ArrayList<T>();
		list.add(element);
		return list;
	}
	
	//getAll(Set<Integer> courseIds, Set<Integer>clientIds);
	@Test
	public void test5() throws Exception{
		ClientService clientService = ioc.get(ClientServiceImpl.class, "clientService");
		Set courseIds = new HashSet();
		courseIds.add(7);
		courseIds.add(8);
		Set clientIds = new HashSet(); 
		Map<Integer, List<ShopClient>> shopClientMap = clientService.getAll(courseIds, clientIds);
		System.out.println(shopClientMap);
		System.out.println(shopClientMap.size());
		System.out.println(shopClientMap.get(7).size());
		System.out.println(clientIds.size());
	}
	
	//getOpenIdAndOrgId()
	@Test
	public void test6(){
		ClientService clientService = ioc.get(ClientServiceImpl.class, "clientService");
		Set clientIds = new HashSet<Integer>();
		clientIds.add(10);
		Map<Integer, Map<Integer, String>> weixinConfigMap = clientService.getOpenIdAndOrgId(clientIds);
		System.out.println(weixinConfigMap.get(10).get(67));
		
	}
}
