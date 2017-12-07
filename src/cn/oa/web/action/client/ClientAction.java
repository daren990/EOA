package cn.oa.web.action.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Status;
import cn.oa.model.Address;
import cn.oa.model.AddressMenu;
import cn.oa.model.AddressUser;
import cn.oa.model.Archive;
import cn.oa.model.Dict;
import cn.oa.model.EduStudent;
import cn.oa.model.Org;
import cn.oa.model.Role;
import cn.oa.model.ShopClient;
import cn.oa.model.User;
import cn.oa.service.AddressBookService;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "client.client")
@At(value = "client/client")
public class ClientAction extends Action{
	
	public static Log log = Logs.getLog(ClientAction.class);
	
	public static final int PAGE_SIZE = 100000;
	
	@POST
	@GET
	@At
	@Ok("ftl:client/client_showAll")
	public void showall(HttpServletRequest req) throws Exception{
		
//		String token = CSRF.generate(req);
//		CSRF.generate(req, "/sys/user/nodes", token);		
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);//公司id
		String trueName = Https.getStr(req, "trueName", R.CLEAN, R.RANGE, "{1,20}");
		String telephone = Https.getStr(req, "telephone", R.CLEAN);
		//用来回显
		MapBean mb = new MapBean();

		//默认查询所有的培训机构，以及其下的所有客户
		List<Org> corps = studentService.selectCorpsAll();
		//客户和登录用户的公司id的交集
		Integer  clientAndCorpId = null;
		for(Org org : corps){
			//判断登录用户是否是培训机构的用户
			if(org.getOrgId().equals(Context.getCorpId())){
				List<Org> orgs = new ArrayList<Org>();
				orgs.add(org);
				corps = orgs;
				//只查询登录用户所属公司下的客户
				corpId = org.getOrgId();
				clientAndCorpId = org.getOrgId();
				break;
			}
		}
		
		Map<String, Object> cnd = new HashMap<String, Object>();
		cnd.put("cn.oa.model.Org.orgId", corpId);
		if(corpId != null) mb.put("corpId", corpId);
		if(trueName != null && trueName != ""){
			cnd.put("cn.oa.model.ShopClient.truename", trueName);
			mb.put("trueName", trueName);
		}
		if(telephone != null && telephone != ""){
			cnd.put("cn.oa.model.EduTeacher.telephone", telephone);
			mb.put("telephone", telephone);
		}
		
		Page<ShopClient> page = Webs.page(req);
		page = clientService.selectAll(cnd, page);
		
		req.setAttribute("clientAndCorpId", clientAndCorpId);
		req.setAttribute("corps", corps);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
		
	}
	
	@POST
	@GET
	@At
	@Ok("ftl:client/client_showAll")
	public void page(HttpServletRequest req) throws Exception{
		showall(req);
	}
	
	@At
	@Ok("ftl:client/client_add")
	public void add(HttpServletRequest req) throws Exception{
		editpage(req);
	}
	
	@At
	@Ok("ftl:client/client_add")
	public void editpage(HttpServletRequest req) throws Exception{
//		CSRF.generate(req);
		Integer clientId = Https.getInt(req, "clientId", R.REQUIRED, R.I);
		
		//登录用户和客户所属的公司的id，当不存在时为null，用于在页面判断登录用户和客户所属的公司是不是同一间
		Integer corpId = null;
		ShopClient client = new ShopClient(); 
		client.setId(clientId);
		client = clientService.selectOne(client, clientId);
		Asserts.isNull(client, "客户不存在");
		List<Org> orgs = clientService.selectCorpsAll(client);
		client.setCorps(orgs);
	
		for(Org org : orgs){
			//只获取登录用户所属的公司的相关信息
			if(Context.getCorpId().equals(org.getOrgId())){
				corpId = Integer.valueOf(org.getOrgId());
				List<Org> os = new ArrayList<Org>();
				os.add(org);
				client.setCorps(os);
			}
		}
		
		//corpId等于null时，表示是平台管理员
		if(corpId == null){
			List<EduStudent> eduStudents = studentService.selectAll(client);
			client.setEduStudents(eduStudents);
		}else{
			Org org = new Org();
			org.setOrgId(corpId);
			List<EduStudent> eduStudents = studentService.selectAll(client, org);
			client.setEduStudents(eduStudents);
		}
		req.setAttribute("corpId", corpId);
		req.setAttribute("client", client);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer clientId = null;
		try {
//			CSRF.validate(req);
			
			clientId = Https.getInt(req, "clientId", R.REQUIRED, R.I, "客户ID");
			Integer gender = Https.getInt(req, "gender", R.REQUIRED, R.I, R.IN, "in [0,1]", "性别");
			String phone = Https.getStr(req, "telephone", R.CLEAN,R.RANGE, "{1,20}", "联系电话");
			String address = Https.getStr(req, "address", R.CLEAN,R.RANGE, "{1,200}", "联系地址");
			String birthday_yyyyMMdd = Https.getStr(req, "birthday_yyyyMMdd", R.D, "出生年月");
			String name = Https.getStr(req, "name", R.REQUIRED, "用户名");
			String truename = Https.getStr(req, "truename", R.REQUIRED, "客户姓名");
			String weixin = Https.getStr(req, "weixin", R.CLEAN,R.RANGE, "{1,20}", "联系电话");
			String password = Https.getStr(req, "password", R.CLEAN,R.RANGE, "{1,20}", "密码");
			DateTime birthday = Calendars.parse(birthday_yyyyMMdd, Calendars.DATE);
			String status = Https.getStr(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]",  "状态");
			
			ShopClient client = new ShopClient();
			client.setId(clientId);
			client.setSex(gender);
			client.setTelephone(phone);
			client.setAddress(address);
			client.setWeixin(weixin);
			client.setBirthday(birthday.toDate());
			client.setName(name);
			client.setTruename(truename);
			client.setStatus(Integer.valueOf(status));
			client.setPassword(password);
			clientService.addOrUpdate(client);
			Code.ok(mb, (clientId == null ? "新建" : "编辑") + "客户成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Client:add) error: ", e);
			Code.error(mb, "编辑失败");
		}
		return mb;
	}
	
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
//			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");

			if (arr != null && arr.length > 0) {
				clientService.able(arr, status);
			}
			Code.ok(mb, "设置客户状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Client:able) error: ", e);
			Code.error(mb, "设置客户状态失败");
		}
		return mb;
	}
	
}
