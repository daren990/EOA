package cn.oa.web.action.hrm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
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

import cn.oa.consts.Status;
import cn.oa.model.HistoryWage;
import cn.oa.model.Org;
import cn.oa.model.SalaryRuleItem;
import cn.oa.model.User;
import cn.oa.model.Wage;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.RMB;
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

/**
 * 工资设置
 * @author SimonTang
 */
@IocBean(name = "hrm.wage")
@At(value = "/hrm/wage")
public class WageAction  extends Action implements Cloneable{

	public static Log log = Logs.getLog(WageAction.class);

	@GET
	@At
	@Ok("ftl:hrm/wage_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);
		
		Integer userId = Https.getInt(req, "userId");
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);//公司id
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		//如果userId不为null，则针对某个用户进行查询
		if(userId != null){
			Cnds.eq(cri, mb, "u.user_id", "userId", userId);
		}
		cri.where().and("u.status","=",Status.ENABLED)		
		.and("u.true_name", "<>", "系统管理员");
		if(corpId!=null){
			cri.where().and("u.corp_id","=",corpId);
		}
		cri.getOrderBy().desc("u.modify_time");
		//从请求参数获取分页的信息
		Page<Wage> page = Webs.page(req);
		//分页的时候总是要发出两条查询条件相同的sql语句，查询后的内容封装在page中
		page = mapper.page(Wage.class, page, "Wage.count", "Wage.index", cri);
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		req.setAttribute("corps", corps);
		Webs.put(mb, "corpId", corpId);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@GET
	@At
	@Ok("ftl:hrm/wage_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);
		Wage wage = mapper.fetch(Wage.class, "Wage.query", Cnd.where("u.user_id", "=", userId));
		String type = Https.getStr(req, "type", R.REQUIRED);
		req.setAttribute("wage", wage);
		req.setAttribute("type", type);
	}

	@GET
	@At
	@Ok("ftl:hrm/wage_batch")
	public void batch(HttpServletRequest req) {
		CSRF.generate(req);
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);
		Wage wage = mapper.fetch(Wage.class, "Wage.query", Cnd.where("u.user_id", "=", userId));
		String type = Https.getStr(req, "type", R.REQUIRED);
		List<Org> orgs = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("parent_id", "=", 0));
		req.setAttribute("orgs", orgs);		
		req.setAttribute("wage", wage);
		req.setAttribute("type", type);
	}
	@POST
	@At
	@Ok("json")
	public Object batch(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String corpIds = Https.getStr(req, "modelOrgs", R.CLEAN,R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司");
			String userIds = Https.getStr(req, "modelUsers", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "员工");
			Float standardSalary = Https.getFloat(req, "standardSalary", R.F, "标准工资");
			Float postSalary = Https.getFloat(req, "postSalary", R.F, "岗位工资");
			Float performSalary = Https.getFloat(req, "performSalary", R.CLEAN, R.F, "绩效工资");
			Float rewardSalary = Https.getFloat(req, "rewardSalary", R.CLEAN, R.F, "奖励工资");
			Float serviceAward = Https.getFloat(req, "serviceAward", R.CLEAN, R.F, "工龄奖");
			Float communicationAllowance = Https.getFloat(req, "communicationAllowance", R.CLEAN, R.F, "通信补贴");
			Float oilAllowance = Https.getFloat(req, "oilAllowance", R.CLEAN, R.F, "油费补贴");
			Float heatingAllowance = Https.getFloat(req, "heatingAllowance", R.CLEAN, R.F, "高温补贴");
			Float mealAllowance = Https.getFloat(req, "mealAllowance", R.CLEAN, R.F, "伙食补贴");
			Float overtimeAllowance = Https.getFloat(req, "overtimeAllowance", R.CLEAN, R.F, "加班补贴");
			Float maternityInsurance = Https.getFloat(req, "maternityInsurance", R.CLEAN, R.F, "生育险");
			Float housingSubsidies = Https.getFloat(req, "housingSubsidies", R.CLEAN, R.F, "住房补贴");
			Float tax = Https.getFloat(req, "tax", R.CLEAN, R.F, "个人所得税");
			Float socialSecurity = Https.getFloat(req, "socialSecurity", R.CLEAN, R.F, "社保补贴");
			Float socialSecurityDeduction = Https.getFloat(req, "socialSecurityDeduction", R.CLEAN, R.F, "社保扣除");
			Integer[] arrCorp = Converts.array(corpIds, ",");
			Integer[] arrUser = Converts.array(userIds, ",");
			//List<User> users = null;
			if(arrCorp==null){
				throw new Errors("公司不存在");				
			}
			if(arrUser==null){
				Criteria cri = Cnd.cri();
				cri.where().and("corp_id", "in", arrCorp);
				cri.where().and("status","=",Status.ENABLED);
				List<User> users = dao.query(User.class, cri);
				if(users.size()==0)throw new Errors("没有找到员工"); 
				arrUser = new Integer[users.size()];
				int count = 0;
				for(User user : users){
					arrUser[count] = user.getUserId();
					count++;
				}
			}
			System.out.println(arrUser[0]);
			//cri.where().and("u.status","=",Status.ENABLED);
			//List<User> users = mapper.query(User.class, "User.role", cri);
			batchTransSave(arrUser, standardSalary, postSalary, performSalary, rewardSalary, serviceAward, communicationAllowance, oilAllowance, heatingAllowance, mealAllowance, overtimeAllowance, maternityInsurance, housingSubsidies, tax, socialSecurity, socialSecurityDeduction);
			Code.ok(mb, "编辑工资成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Wage:add) error: ", e);
			Code.error(mb, ("编辑工资失败"));
		}
		return mb;
	}
	
	/**
	 * 获取组织机构成员
	 * @param req
	 * @param res
	 * @return
	 */
	@POST
	@At
	@Ok("json")
	public Object change(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String corpIds = Https.getStr(req, "modelOrgs", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			Integer[] arrCorp = Converts.array(corpIds, ",");
			List<User> users = null;
			Criteria cri = Cnd.cri();
			if(arrCorp!=null){
				cri.where().and("u.corp_id", "in", arrCorp)		
				.and("u.status","=",Status.ENABLED).and("u.true_name", "<>", "系统管理员");
				users = mapper.query(User.class, "User.role", cri);
			}else{
				users = new ArrayList<User>();
			}
			mb.put("users",users);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(WageAction:change) error: ", e);
			Code.error(mb, "查找用户失败");
		}
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer userId = null;
		Wage wage = null;
		boolean exist = true;
		try {
			CSRF.validate(req);
			
			userId = Https.getInt(req, "userId", R.REQUIRED, R.I);
			String type = Https.getStr(req, "type", R.REQUIRED);
			Float standardSalary = Https.getFloat(req, "standardSalary", R.REQUIRED, R.F, "标准工资");
			Float postSalary = Https.getFloat(req, "postSalary", R.REQUIRED, R.F, "岗位工资");
			Float performSalary = Https.getFloat(req, "performSalary", R.CLEAN, R.F, "绩效工资");
			Float rewardSalary = Https.getFloat(req, "rewardSalary", R.CLEAN, R.F, "奖励工资");
			Float serviceAward = Https.getFloat(req, "serviceAward", R.CLEAN, R.F, "工龄奖");
			Float communicationAllowance = Https.getFloat(req, "communicationAllowance", R.CLEAN, R.F, "通信补贴");
			Float oilAllowance = Https.getFloat(req, "oilAllowance", R.CLEAN, R.F, "油费补贴");
			Float heatingAllowance = Https.getFloat(req, "heatingAllowance", R.CLEAN, R.F, "高温补贴");
			Float mealAllowance = Https.getFloat(req, "mealAllowance", R.CLEAN, R.F, "伙食补贴");
			Float overtimeAllowance = Https.getFloat(req, "overtimeAllowance", R.CLEAN, R.F, "加班补贴");
			Float maternityInsurance = Https.getFloat(req, "maternityInsurance", R.CLEAN, R.F, "生育险");
			Float housingSubsidies = Https.getFloat(req, "housingSubsidies", R.CLEAN, R.F, "住房补贴");
			Float tax = Https.getFloat(req, "tax", R.CLEAN, R.F, "个人所得税");
			Float socialSecurity = Https.getFloat(req, "socialSecurity", R.CLEAN, R.F, "社保补贴");
			Float socialSecurityDeduction = Https.getFloat(req, "socialSecurityDeduction", R.CLEAN, R.F, "社保扣除");
			Double subsidies = Https.getDouble(req, "subsidies", R.F);
			String effect_yyyyMMdd = Https.getStr(req, "effect_yyyyMMdd",R.REQUIRED, R.D, "生效日期");
			
			DateTime effectTime = Calendars.parse(effect_yyyyMMdd, Calendars.DATE);
			
			DateTime now = new DateTime();
			wage = dao.fetch(Wage.class, userId);
			
			User me = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", userId));
			Date enterTime_yyyyMMdd = me.getEntryDate();
			Asserts.isNull(enterTime_yyyyMMdd, "入职日期不能为空");
			DateTime enterTime = Calendars.parse(enterTime_yyyyMMdd, Calendars.DATE);
			if(effectTime.isBefore(enterTime))
				throw new Errors("生效时间不能在入职时间之前");
			
			 
			if (wage == null) {
				exist = false;
				wage = new Wage();
			}
			wage.setUserId(userId);
			wage.setStandardSalary(RMB.on(standardSalary));
			wage.setPostSalary(RMB.on(postSalary));
			wage.setPerformSalary(RMB.on(performSalary));
			wage.setRewardSalary(RMB.on(rewardSalary));
			wage.setServiceAward(RMB.on(serviceAward));
			wage.setCommunicationAllowance(RMB.on(communicationAllowance));
			wage.setOilAllowance(RMB.on(oilAllowance));
			wage.setHeatingAllowance(RMB.on(heatingAllowance));
			wage.setMealAllowance(RMB.on(mealAllowance));
			wage.setOvertimeAllowance(RMB.on(overtimeAllowance));
			wage.setMaternityInsurance(RMB.on(maternityInsurance));
			wage.setHousingSubsidies(RMB.on(housingSubsidies));
			wage.setTax(RMB.on(tax));
			wage.setSocialSecurity(RMB.on(socialSecurity));
			wage.setSocialSecurityDeduction(RMB.on(socialSecurityDeduction));
			wage.setModifyId(Context.getUserId());
			wage.setModifyTime(now.toDate());
			wage.setSubsidies(subsidies*100);
			if (Strings.isNotBlank(effect_yyyyMMdd)) wage.setEffectTime(Calendars.parse(effect_yyyyMMdd, Calendars.DATE).toDate());

			transSave(exist, wage,  userId, now, type);
			
			Code.ok(mb, (exist? "编辑" : "新建")+ "工资成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Wage:add) error: ", e);
			Code.error(mb, (exist? "编辑" : "新建") + "工资失败");
		}

		return mb;
	}
	private void transSave(final boolean exist, final Wage wage, final Integer userId, final DateTime now, final String type) {
		Trans.exec(new Atom(){

			@Override
			public void run() {
				if(exist){ //有对象
					//调薪
					if(type!=null){
						dao.update(HistoryWage.class, Chain.make("status", Status.DISABLED), Cnd.where("status", "=", Status.INTERIM).and("userId", "=", userId));
						Wage history = dao.fetch(Wage.class, userId);
						dao.clear(Wage.class, Cnd.where("userId", "=", userId));
						dao.insert(new HistoryWage(userId, history.getStandardSalary(), history.getPostSalary(), history.getPerformSalary(), history.getRewardSalary(),
								history.getServiceAward(), history.getCommunicationAllowance(), history.getOilAllowance(), history.getHeatingAllowance(), history.getMealAllowance(),
								history.getOvertimeAllowance(), history.getMaternityInsurance(), history.getHousingSubsidies(), history.getTax(), wage.getSocialSecurityDeduction(),
								history.getStandardSalary(), Context.getUserId(), now.toDate(), Status.INTERIM,history.getEffectTime()));
						dao.insert(wage);
					}//编辑
					else{
						dao.update(wage);
					}
				}else{
					//新建
					dao.insert(wage);
					dao.insert(new HistoryWage(userId, wage.getStandardSalary(), wage.getPostSalary(), wage.getPerformSalary(), wage.getRewardSalary(),
							wage.getServiceAward(), wage.getCommunicationAllowance(), wage.getOilAllowance(), wage.getHeatingAllowance(), wage.getMealAllowance(),
							wage.getOvertimeAllowance(), wage.getMaternityInsurance(), wage.getHousingSubsidies(), wage.getTax(), wage.getSocialSecurityDeduction(),
							wage.getStandardSalary(), Context.getUserId(), now.toDate(), Status.INTERIM, wage.getEffectTime()));
				}
			}});
	}
	
	private void batchTransSave(final Integer[] arrUser,final Float standardSalary,final Float postSalary,final Float performSalary,final Float rewardSalary,
			final Float serviceAward,final Float communicationAllowance,final Float oilAllowance,final Float heatingAllowance,final Float mealAllowance,
			final Float overtimeAllowance,final Float maternityInsurance,final Float housingSubsidies,final Float tax,final Float socialSecurity,final Float socialSecurityDeduction) {
		Trans.exec(new Atom(){

			@Override
			public void run() {
			 Map<String, Object> map = new HashMap<String, Object>();
			 if(standardSalary!=null)map.put("standard_salary", RMB.on(standardSalary));
			 if(postSalary!=null)map.put("post_salary", RMB.on(postSalary));
			 if(performSalary!=null)map.put("perform_salary", RMB.on(performSalary));
			 if(rewardSalary!=null)map.put("reward_salary", RMB.on(rewardSalary));
			 if(serviceAward!=null)map.put("service_award", RMB.on(serviceAward));
			 if(communicationAllowance!=null)map.put("communication_allowance", RMB.on(communicationAllowance));
			 if(oilAllowance!=null)map.put("oil_allowance",RMB.on(oilAllowance));
			 if(heatingAllowance!=null)map.put("heating_allowance", RMB.on(heatingAllowance));
			 if(mealAllowance!=null)map.put("meal_allowance", RMB.on(mealAllowance));
			 if(overtimeAllowance!=null)map.put("overtime_allowance", RMB.on(overtimeAllowance));
			 if(maternityInsurance!=null)map.put("maternity_insurance", RMB.on(maternityInsurance));

			 if(housingSubsidies!=null)map.put("housing_subsidies", RMB.on(housingSubsidies));
			 if(tax!=null)map.put("tax", RMB.on(tax));
			 if(socialSecurity!=null)map.put("social_security", RMB.on(socialSecurity));
			 if(socialSecurityDeduction!=null)map.put("social_security_deduction", RMB.on(socialSecurityDeduction));
			 int x = dao.update(Wage.class, Chain.from(map), Cnd.where("user_id", "in", arrUser));
			 
			 if(x == 0){
				 for(Integer userId : arrUser){
					 map.put("user_id", userId); 
					 map.put("modify_id", Context.getUserId());
					 map.put("modify_time", new DateTime().toDate());
					 map.put("effect_time", new DateTime().toDate());
					 dao.insert("hrm_wage", Chain.from(map));
				 }
//				 dao.fastInsert(salaryRuleItems);

			 }
			
			}});
	}
}
