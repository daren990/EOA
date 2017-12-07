package cn.oa.web.action.sys.conf;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;
import cn.oa.consts.Cache;
import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.model.Accountant;
import cn.oa.model.Org;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At("/sys/conf/accountant")
public class AccountantAction extends Action{
	public static Log log = Logs.getLog(AccountantAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/accountant_page")
	public void page(HttpServletRequest req){
		
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/accountant/able", token);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Page<Accountant> page = Webs.page(req);
		page = mapper.page(Accountant.class, page, "Accountant.count","Accountant.index",cri);
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		for (Accountant accountant : page.getResult()) {
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getAccountantId() == null) continue;
				if (o.getAccountantId().equals(accountant.getAccountantId())) {
					orgs.add(o);
				}
			}
			accountant.setCorps(orgs);
			Integer userIds[] = new Integer[100];
			
			userIds = Converts.array(accountant.getUserIds(), ",");
			List<User> list = mapper.query(User.class, "User.operator", Cnd.where("u.user_id", "in", userIds));
			accountant.setUsers(list);
		}
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:sys/conf/accountant_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer accountantId = Https.getInt(req, "accountantId", R.REQUIRED,R.I);
		Criteria criteria = Cnd.cri();
		
		Accountant accountant = null;
		if(accountantId !=null){
			accountant = dao.fetch(Accountant.class, accountantId);
		}
		if(accountant == null)
			accountant = new Accountant();
			
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		if(accountantId !=null){
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getAccountantId() == null) continue;
				if (o.getAccountantId().equals(accountant.getAccountantId())) {
					orgs.add(o);
				}
			}
			accountant.setCorps(orgs);
			
			Integer userIds[] = Converts.arrayTonumber(accountant.getUserIds(), ",");
			criteria.where().and("u.user_id", "in", userIds);
			List<User> list = mapper.query(User.class, "User.operator", criteria);
			accountant.setUsers(list);
		}
		List<User> users = userService.operators(new String[]{Roles.CSVI.getName(), Roles.ASVI.getName(), Roles.CAS.getName(), Roles.ACC.getName()});
		req.setAttribute("accountant", accountant);
		req.setAttribute("corps", corps);
		req.setAttribute("users", users);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer accountantId = null;
		try {
			CSRF.validate(req);
			accountantId = Https.getInt(req, "accountantId", R.I);
			String accountantName = Https.getStr(req, "accountantName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "配置名称");
			String corpIds = Https.getStr(req, "corpIds", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			String userIds = Https.getStr(req, "userIds", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "财务人员绑定");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Integer[] arr = Converts.array(corpIds, ",");
		//	Integer[] userarr = Converts.array(userIds, ",");
			
			Accountant accountant = null;
			if (accountantId != null) {
				accountant = dao.fetch(Accountant.class, accountantId);
				Asserts.isNull(accountant, "配置不存在");
			} else {
				accountant = new Accountant();
			}
			
			accountant.setAccountantName(accountantName);
			accountant.setUserIds(userIds);
			accountant.setStatus(status);
			
			transSave(accountantId, arr, accountant);
			
			Code.ok(mb, (accountantId == null ? "新建" : "编辑") + "财务人员配置成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(AnnualRole:add) error: ", e);
			Code.error(mb, (accountantId == null ? "新建" : "编辑") + "财务人员配置失败");
		}

		return mb;
	}
	
	private void transSave(final Integer accountantId, final Integer[] arr, final Accountant accountant) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = accountantId;
				dao.update(Org.class, Chain.make("accountantId", null), Cnd.where("accountantId", "=", id));
				if (accountantId != null) {
					dao.update(accountant);
				} else {
					id = dao.insert(accountant).getAccountantId();
				}
				if (!Asserts.isEmpty(arr))
					dao.update(Org.class, Chain.make("accountantId", id), Cnd.where("orgId", "in", arr));
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
				cache.removeAll(Cache.FQN_RESOURCES);
				dao.update(Accountant.class, Chain.make("status", status), Cnd.where("accountantId", "in", arr));
			}
			Code.ok(mb, "设置财务人员配置状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Accountant:able) error: ", e);
			Code.error(mb, "设置财务人员配置状态失败");
		}

		return mb;
	}
}
