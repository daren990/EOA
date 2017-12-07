package cn.oa.web.action.addressBook;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import cn.oa.model.Org;
import cn.oa.model.User;
import cn.oa.service.AddressBookService;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
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
@IocBean(name = "addressBook.AddressBook")
@At(value = "addressBook/AddressBook")
public class AddressBook extends Action{
	
	public static Log log = Logs.getLog(AddressBook.class);
	
	public static final int PAGE_SIZE = 100000;
	
	
	@GET
	@At
	@Ok("ftl:addressBook/AddressBook_company")
	public void companypage(HttpServletRequest req){
		
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);		
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);//公司id
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);//用户id,对于系统是唯一
		String trueName = Https.getStr(req, "trueName", R.CLEAN, R.RANGE, "{1,20}");
		Integer jobNumber = Https.getInt(req, "jobNumber",R.CLEAN, R.I);
		
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "u.true_name", "trueName", trueName);
		Cnds.eq(cri, mb, "u.job_number", "jobNumber", jobNumber);
		Cnds.eq(cri, mb, "u.user_id", "userId", userId);
		/*Cnds.eq(cri, mb, "u.corp_id", "corpId", Context.getCorpId());*/
		Cnds.eq(cri, mb, "u.status", "status", Status.ENABLED);
		cri.getOrderBy().asc("u.corp_id");
		req.setAttribute("pageSize", PAGE_SIZE);
		
		Page<User> page = Webs.page(req);
		page = mapper.page(User.class, page, "User.count", "User.query", cri);	
		List<Org> org = dao.query(Org.class, Cnd.where("parent_id", "=", 0).and("status", "=", Status.ENABLED));
		req.setAttribute("page", page);
		req.setAttribute("org", org);
		req.setAttribute("corps", corps);
		req.setAttribute("mb", mb);		
	}
	
	@GET
	@At
	@Ok("ftl:addressBook/AddressBook_information")
	public void information(HttpServletRequest req){	
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);//用户id,对于系统是唯一
		/*String corpName = Https.getStr(req, "corpName");*/
		Archive archive = dao.fetch(Archive.class, Cnd.where("user_id", "=", userId));		
		User user = dao.fetch(User.class, Cnd.where("user_id", "=", userId));
		Org org = new Org();
		List<AddressMenu> addressMenu = dao.query(AddressMenu.class,Cnd.where("user_id", "=", Context.getUserId()));
		if(archive == null)
			archive = new Archive();
		if(user == null)
			user = new User();
		else{
			org = dao.fetch(Org.class,user.getCorpId());
			req.setAttribute("corpName", org.getOrgName());
		}
		req.setAttribute("user", user);	
		req.setAttribute("addressMenu", addressMenu);		
		req.setAttribute("archive", archive);	
	}
	
	@GET
	@At
	@Ok("ftl:addressBook/AddressBook")
	public void wxpage(HttpServletRequest req){
		
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);		
		CSRF.generate(req, "/addressBook/AddressBook/groupadd", token);	
		CSRF.generate(req, "/addressBook/AddressBook/useradd", token);	
		CSRF.generate(req, "/addressBook/AddressBook/userdel", token);	
		CSRF.generate(req, "/addressBook/AddressBook/groupdel", token);	
		
		List<AddressMenu> corps = dao.query(AddressMenu.class, Cnd.where("user_id", "=", Context.getUserId()));
		
		
		//left join查询组别和组员
		/*Sql sql = null;
		String aString=dao.sqls().get("AddressMenu.index");
		sql = Sqls.create(aString+" where user_id like "+Context.getUserId()+" order by sort asc");
		sql.setCallback(Sqls.callback.entities());
		sql.setEntity(dao.getEntity(Address.class));
		dao.execute(sql);
		List<Address> address = sql.getList(Address.class);*/
		List<Address> address = null;
		Criteria cri = Cnd.cri();
		cri.where()
			.and("m.user_id", "=", Context.getUserId());
		cri.getOrderBy().asc("m.sort");
		address = mapper.query(Address.class, "AddressMenu.index", cri);
		req.setAttribute("address", address);

		req.setAttribute("corps", corps);
	
	}
	@GET
	@At
	@Ok("ftl:addressBook/AddressBook_group_add")
	public void groupadd(HttpServletRequest req){	
		groupaddUtil(req);
	}
	@GET
	@At
	@Ok("ftl:addressBook/AddressBook_user_add")
	public void useradd(HttpServletRequest req){
		useraddUtil(req);
	}
	
	public void useraddUtil(HttpServletRequest req){
		CSRF.generate(req);		
		Integer uuserid = Https.getInt(req, "uuserid", R.I);
		AddressUser addressUser = null;
		AddressMenu addressMenu = null;
		if(uuserid != null){
			addressUser = addressBookService.findUser(uuserid);
		}
		if(addressUser != null){
			addressMenu = addressBookService.findMenu(addressUser.getU_groupid());
			
		}else{
			addressUser = new AddressUser();
			addressMenu = new AddressMenu();
		}
		
		List<AddressMenu> groups =addressBookService.findGroup(Context.getUserId());
		req.setAttribute("groups", groups);
		req.setAttribute("group", addressMenu);
		req.setAttribute("addressUser", addressUser);
	}
	
	public void groupaddUtil(HttpServletRequest req){
		CSRF.generate(req);		
		Integer groupid = Https.getInt(req, "groupid", R.I);
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);//用户id,对于系统是唯一
		String corpName = Https.getStr(req, "corpName");
		AddressMenu addressMenu = null;
		if(groupid != null){
			addressMenu = addressBookService.findMenu(groupid);			
		}else{
			addressMenu = new AddressMenu();
		}
		req.setAttribute("userId", userId);
		req.setAttribute("corpName", corpName);
		req.setAttribute("group", addressMenu);
	}
	
	@POST
	@At
	@Ok("json")
	public Object move(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		AddressUser addressUser = new AddressUser();
		try {
			/*Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I, "用户名");*/
			Integer groupId = Https.getInt(req, "group", R.REQUIRED, R.I, "组别");
			String userName = Https.getStr(req, "userName", R.REQUIRED, "成员名字");
			/*String corpName = Https.getStr(req, "corpName");*/			
			String position = Https.getStr(req, "position");
			String QQ = Https.getStr(req, "QQ");
			String email = Https.getStr(req, "email");
			String exigency_name = Https.getStr(req, "exigency_name");
			String exigency_phone = Https.getStr(req, "exigency_phone");
			/*addressUser.setU_userid(userId);*/
			addressUser.setU_groupid(groupId);
			addressUser.setUsername(userName);
			addressUser.setRelation("同事");
			addressUser.setPosition(position);
			addressUser.setQq(QQ);
			addressUser.setEmail(email);
			addressUser.setExigencyphone(exigency_name+":"+exigency_phone);
			dao.insert(addressUser);
			Code.ok(mb, "移动成员成功!");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}catch (Exception e) {
			log.error("(AddressBook:userdel) error: ", e);
			Code.error(mb, "移动成员失败!");
		}	
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object companyPost(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		AddressMenu addressMenu = new AddressMenu();
		try {
		Integer sort =  Https.getInt(req, "corpId", R.REQUIRED, R.I);
		String name = Https.getStr(req, "name", R.CLEAN, R.RANGE, "{1,20}");		
		addressMenu.setName(name);
		addressMenu.setSort(sort);
		dao.insert(addressMenu);
		Code.ok(mb, "新添组别成功!");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}catch (Exception e) {
			log.error("(AddressBook:userdel) error: ", e);
			Code.error(mb, "新添组别失败!");
		}	
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public MapBean userdel(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			if (arr != null && arr.length > 0) {
				addressBookService.delUser(arr[0]);
				Code.ok(mb, "通讯录成员删除成功");
			}else{
				Code.error(mb, "你没有选定成员!");
			}
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}catch (Exception e) {
			log.error("(AddressBook:userdel) error: ", e);
			Code.error(mb, "通讯录成员删除失败!");
		}	
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public MapBean groupdel(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			if (arr != null && arr.length > 0) {
				addressBookService.delGroup(arr[0]);
				Code.ok(mb, "通讯录组删除成功");
			}else{
				Code.error(mb, "你没有选定组别!");
			}
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}catch (Exception e) {
			log.error("(AddressBook:groupdel) error: ", e);
			Code.error(mb, "通讯录组别删除失败!");
		}	
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public MapBean useradd(HttpServletRequest req, HttpServletResponse res) {
		return useraddUtil(req,res);
	}
	private MapBean useraddUtil (HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer uuserid = Https.getInt(req, "uuserid",R.I);
			Integer groupId = Https.getInt(req, "groupId", R.REQUIRED, R.I, "组别");
			String username = Https.getStr(req, "username", R.CLEAN, R.REQUIRED, R.RANGE, "{1,10}", "成员名字");
			String relation = Https.getStr(req, "relation", R.CLEAN, R.RANGE, "{1,30}", "成员关系");
			String position = Https.getStr(req, "position", R.CLEAN, R.RANGE, "{1,30}", "成员职务");
			String phone = Https.getStr(req, "phone", R.CLEAN, R.RANGE, "{1,30}", "成员电话");
			String qq = Https.getStr(req, "qq", R.CLEAN, R.RANGE, "{1,30}", "成员QQ");
			String email = Https.getStr(req, "email", R.CLEAN, R.RANGE, "{1,30}", "成员Email");
			String exigencyphone = Https.getStr(req, "exigency_phone", R.CLEAN, R.RANGE, "{1,30}", "紧急联系人");
			
			AddressUser addressUser = new AddressUser();
			if(uuserid != null){
				addressUser = addressBookService.findUser(uuserid);
			}			
			addressUser.setU_groupid(groupId);
			addressUser.setUsername(username);
			addressUser.setRelation(relation);
			addressUser.setPosition(position);
			addressUser.setPhone(phone);
			addressUser.setQq(qq);
			addressUser.setEmail(email);
			addressUser.setExigencyphone(exigencyphone);
			if(addressUser.getU_userid() != null){
				addressBookService.updateUser(addressUser);
			}else{
			addressBookService.saveUser(addressUser);
			}
			Code.ok(mb, ((uuserid==null)?"新添":"编辑")+"成员成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}catch (Exception e) {
			log.error("(AddressBook:useradd) error: ", e);
			Code.error(mb, "插入失败!");
		}	
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object groupadd(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer userId = null;
		try {
			CSRF.validate(req);
			userId = Https.getInt(req, "userId", R.REQUIRED, R.I);//用户id,对于系统是唯一
			/*String corpName = Https.getStr(req, "corpName");*/
			Integer groupid = Https.getInt(req, "groupid",R.I);
			String groupName = Https.getStr(req, "groupName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,10}", "组别名字");
			Integer groupNum = Https.getInt(req, "groupNum", R.REQUIRED, R.I, "排序");
			AddressMenu addressMenu = new AddressMenu();
			if(groupid != null){
				addressMenu = addressBookService.findMenu(groupid);
			}	
			addressMenu.setName(groupName);
			addressMenu.setUserid(Context.getUserId());
			addressMenu.setSort(groupNum);
			if(addressMenu.getGroupid() != null){
				addressBookService.updateMenu(addressMenu);
			}else{
				addressBookService.saveGroup(addressMenu);
			}
			/*Code.ok(mb, ((groupid==null)?"新添":"编辑")+"组别成功");*/
			if(userId!=null)
				Code.ok(mb, "组别已添加");
			else
				Code.ok(mb, ((groupid==null)?"新添":"编辑")+"组别成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}catch (Exception e) {
			log.error("(AddressBook:groupadd) error: ", e);
			Code.error(mb, "插入失败!");
		}
			return mb;
	}
}
