package cn.oa.web.action.sys;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
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
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Mail;
import cn.oa.consts.Status;
import cn.oa.consts.Suffix;
import cn.oa.consts.Value;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Archive;
import cn.oa.model.Dict;
import cn.oa.model.Org;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.JobNumber;
import cn.oa.utils.MailC;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.Values;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.except.Errors;
import cn.oa.utils.helper.Archives;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/sys/user")
public class UserAction extends Action {

	public static Log log = Logs.getLog(UserAction.class);

	@POST
	@GET
	@At
	@Ok("ftl:sys/user_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/able", token);

		String username = Https.getStr(req, "username", R.CLEAN, R.RANGE, "{1,60}");
		String trueName = Https.getStr(req, "trueName", R.CLEAN, R.RANGE, "{1,20}");
		Integer jobNumber = Https.getInt(req, "jobNumber",R.CLEAN, R.I);
		Integer corpId = Https.getInt(req, "corpId",R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.like(cri, mb, "u.username", "username", username);
		Cnds.like(cri, mb, "u.true_name", "trueName", trueName);
		Cnds.eq(cri, mb, "u.job_number", "jobNumber", jobNumber);
		Cnds.eq(cri, mb, "u.corp_id", "corpId", corpId);
		cri.getOrderBy().desc("u.modify_time");
		
		Page<User> page = Webs.page(req);
		page = mapper.page(User.class, page, "User.count", "User.index", cri);

		List<Role> roles = mapper.query(Role.class, "UserRole.join", Cnd.where("r.status", "=", Status.ENABLED));
		List<Org> corps = dao.query(Org.class, Cnd.where("type", "=", Status.ENABLED));
		
		for (User u : page.getResult()) {
			int minute = overtimeService.lastDeferredMinute(u.getUserId());
			float m = new Float(minute)/60;
			String ms = new String(m+"");
			int index = ms.indexOf(".");
			ms = ms.substring(0,index+3>ms.length()?ms.length():index+3);
			u.setOverTimeRest(ms);
			List<Role> roleList = new ArrayList<Role>();
			for (Role r : roles) {
				if (u.getUserId().equals(r.getUserId())) {
					roleList.add(r);
				}
			}
			u.setRoles(roleList);
		}
		req.setAttribute("corps", corps);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:");
			Integer[] arr = Converts.array(checkedIds, ",");
			
			Criteria cri = Cnd.cri();
			cri.where().and("status", "=", Status.ENABLED);
			if (!Asserts.isEmpty(arr)) {
				cri.where().and("corpId", "in", arr);
			}
			
			List<User> nodes = dao.query(User.class, cri);
			mb.put("nodes", nodes);
			
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(User:users) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:sys/user_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);
		User user = null;
		if (userId != null) {
			user = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId));
			if (user != null) {
				dao.fetchLinks(user, "roles");
			}
		}
		if (user == null)
			user = new User();

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		List<Org> orgs = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED));

		List<User> users = dao.query(User.class, Cnd.where("status", "=", Status.ENABLED));
		List<Role> roles = dao.query(Role.class, Cnd.where("status", "=", Status.ENABLED));

		req.setAttribute("user", user);
		req.setAttribute("corps", corps);
		req.setAttribute("orgs", orgs);
		req.setAttribute("users", users);
		req.setAttribute("roles", roles);
		
		req.setAttribute("levelMap", dictService.map(Dict.LEVEL));
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer userId = null;
		try {
			CSRF.validate(req);
			
			userId = Https.getInt(req, "userId", R.I);
			Integer corpId = Values.getInt(Https.getInt(req, "corpId", R.I), Value.I);
			Integer orgId = Values.getInt(Https.getInt(req, "orgId", R.I), Value.I);
			Integer managerId = Values.getInt(Https.getInt(req, "managerId", R.I), Value.I);
			Integer level = Values.getInt(Https.getInt(req, "level", R.CLEAN, R.I), Value.I);
			String roleIds = Https.getStr(req, "roleIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "拥有角色组");
			String username = Https.getStr(req, "username", R.CLEAN, R.REQUIRED, R.RANGE, R.REGEX, "{4,60}", R.USERNAME, "用户名");
			String orgUsername = Https.getStr(req, "orgUsername", R.CLEAN, R.RANGE, "{4,60}", "用户名");
			String password = Https.getStr(req, "password", R.CLEAN, R.RANGE, R.REGEX, "{6,32}", R.PASSWORD, "密码");
			String trueName = Https.getStr(req, "trueName", R.CLEAN, R.RANGE, "{1,60}", "姓名");
			String email = Https.getStr(req, "email", R.E, R.RANGE, "{1,64}", "邮箱");

			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Asserts.notUnique(username, orgUsername,
					dao.count(User.class, Cnd.where("username", "=", username).and("status", "=", Status.ENABLED)),
					"用户名已存在");
			
			DateTime now = new DateTime();
			User user = null;
			if (userId != null) {
				user = dao.fetch(User.class, userId);
				Asserts.isNull(user, "用户不存在");
				if (Strings.isNotBlank(password)) user.setPassword(md5.encode(password));
			} else {
				user = new User();
				user.setPassword(md5.encode("123456"));
				user.setStatus(Status.ENABLED);
				user.setCreateTime(now.toDate());
			}

			user.setCorpId(corpId);
			user.setOrgId(orgId);
			user.setManagerId(managerId);
			user.setLevel(level);
			user.setUsername(username);
			user.setTrueName(trueName);
			user.setEmail(email);
			user.setStatus(status);
			user.setModifyTime(now.toDate());

			List<Role> roles = new ArrayList<Role>();
			for (Integer id : Converts.array(roleIds, ",")) {
				Role r = new Role();
				r.setRoleId(id);
				roles.add(r);
			}
			user.setRoles(roles);
			System.out.println(user);
			//发送邮件验证邮箱
			List<String> maiList = new ArrayList<String>();
			String mailR = null;
			//User u = dao.fetch(User.class,reimburse.getUserId());
			if(email!=null&&!"".equals(email)){
				if(MailC.checkEmail(email)){
					maiList.add(email);
				}else{
					throw new Errors("邮箱不是合法的邮箱格式!");
				}
				String subject = "OA邮箱信息核对";

				String content = "你的个人信息填写完成,请到OA上进行核对";
				MailStart mail = new MailStart();
				mailR = mail.mail(maiList,subject,content);			
			}
			if(Mail.ERROR.equals(mailR)){
				throw new Errors("邮箱验证失败!");
			}
			transSave(userId, user);

			Code.ok(mb, (userId == null ? "新建" : "编辑") + "用户成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(User:add) error: ", e);
			Code.error(mb, (userId == null ? "新建" : "编辑") + "用户失败");
		}

		return mb;
	}
	
	private void transSave(final Integer userId, final User user) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (userId != null) {
					dao.update(user);
					dao.clearLinks(user, "roles");
				} else {
					Integer amount = dao.count(User.class, Cnd.where("corpId", "=", user.getCorpId()));
					cn.oa.model.JobNumber jobNumber = dao.fetch(cn.oa.model.JobNumber.class, Cnd.where("corpId", "=", user.getCorpId()));
					Integer jobPre = null;
					if(jobNumber == null){
						Sql sql = Sqls.fetchInt(("select max(job_pre) from sec_jobnumber_pre"));
						dao.execute(sql);
						jobPre = sql.getObject(Integer.class) + 1;
						//配置公司的前缀
						jobNumber = new cn.oa.model.JobNumber();
						jobNumber.setCorpId(user.getCorpId());
						jobNumber.setJobPre(jobPre);
						dao.insert(jobNumber);
					}else{
						jobPre = jobNumber.getJobPre();
					}
	
					amount = amount + 1;
					while(true){
						User u = dao.fetch(User.class, Cnd.where("jobNumber", "=", JobNumber.add(jobPre, amount)));
						if(u != null){
							amount++;
						}else{
							break;
						}
					}
					user.setJobNumber(JobNumber.add(jobPre, amount));
					dao.insert(user);
				//	resultSet.setJobNumber(JobNumber.create(resultSet.getUserId()));
				//	dao.update(resultSet);
				}
				dao.insertRelation(user, "roles");
			}
		});
	}
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			if (arr != null && arr.length > 0) {
				dao.update(User.class, Chain.make("status", status), Cnd.where("userId", "in", arr));
			}
			Code.ok(mb, "设置用户状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(User:able) error: ", e);
			Code.error(mb, "设置用户状态失败");
		}

		return mb;
	}
	
	@GET
	@At
	public void download(HttpServletRequest req, HttpServletResponse res) {
		try {
			ServletOutputStream output = Webs.output(req, res, Encodes.urlEncode("个人档案." + Suffix.XLSX));
			Excels excels = new Excels(Suffix.XLSX);
			List<List<Data>> rowList = new ArrayList<List<Data>>();
			rowList.add(Archives.subjects());
			
			List<Archive> archives = mapper.query(Archive.class, "Archive.query", Cnd.where("u.status", "=", Status.ENABLED));
			for (Archive e : archives) {
				rowList.add(Archives.cells(e));
			}
			
			excels.write(output, rowList, "个人档案");
		} catch (Exception e) {
			log.error("(User:download) error: ", e);
		}
	}
}
