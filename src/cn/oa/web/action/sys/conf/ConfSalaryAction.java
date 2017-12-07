package cn.oa.web.action.sys.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import cn.oa.consts.Status;
import cn.oa.model.AttendanceThreshold;
import cn.oa.model.AttendanceThresholdItem;
import cn.oa.model.Org;
import cn.oa.model.SalaryRule;
import cn.oa.model.SalaryRuleItem;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Servlets;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

/**
 * 工资规则配置
 * @author Andy
 */
@IocBean
@At("/sys/conf/salaryRule")
public class ConfSalaryAction extends Action {
	public static Log log = Logs.getLog(ConfSalaryAction.class);
	
	/**
	 * 规则列表
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:sys/conf/salaryRule_page")
	public void page(HttpServletRequest req){
//		String token = CSRF.generate(req);
//		CSRF.generate(req, "/sys/conf/attendanceThreshold/able", token);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Page<SalaryRule> page = Webs.page(req);
		page = mapper.page(SalaryRule.class, page, "ConfSalaryRule.count","ConfSalaryRule.index",cri);
		System.out.println(page.getResult().size());
		for(SalaryRule salaryRule : page.getResult()){
			Integer arr[] = Converts.array(salaryRule.getOrgIds(), ",");
			if(arr == null){
				arr = new Integer[100];
				arr[0] = -1; 
			}
			List<Org> corps = dao.query(Org.class, Cnd.where("orgId", "in", arr));
//			threshold.setCorps(corps);
			salaryRule.setCorps(corps);
		}
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	/**
	 * 规则编辑
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:sys/conf/salaryRule_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer salaryRuleId = Https.getInt(req, "id", R.REQUIRED,R.I);
		// 迟到早退
		List<SalaryRuleItem> salaryRuleItems = null;
//		// 旷工
//		List<AttendanceThresholdItem> absentItem = null;
//		// 未打卡
//		List<AttendanceThresholdItem> forgetItem = null;
//		// 迟到早退、旷工、未打卡累积
//		List<AttendanceThresholdItem> accumulateItem = null;
		
		SalaryRule salaryRule;
		if(null == salaryRuleId){
			salaryRule = new SalaryRule();
			salaryRuleItems = dao.query(SalaryRuleItem.class,Cnd.where("salaryRuleId", "=", -1).and("status", "=", Status.ENABLED));
		} else {
			salaryRule = dao.fetch(SalaryRule.class, salaryRuleId);
			Asserts.isNull(salaryRule, "配置不存在");
			salaryRuleItems = dao.query(SalaryRuleItem.class,Cnd.where("salaryRuleId", "=", salaryRuleId).and("status", "=", Status.ENABLED));
//			absentItem = dao.query(AttendanceThresholdItem.class,Cnd.where("thresholdId", "=", thresholdId).and("type", "=", Status.A).and("status", "=", Status.ENABLED));
//			forgetItem = dao.query(AttendanceThresholdItem.class,Cnd.where("thresholdId", "=", thresholdId).and("type", "=", Status.F).and("status", "=", Status.ENABLED));
//			accumulateItem = dao.query(AttendanceThresholdItem.class,Cnd.where("thresholdId", "=", thresholdId).and("type", "=", Status.ACCUMULATE).and("status", "=", Status.ENABLED));
		}
		
		//查询所有公司，不包含下属机构
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		if(salaryRule.getOrgIds() != null){
			Integer orgs[] = Converts.array(salaryRule.getOrgIds(), ",");
			List<Org> arrcorps = dao.query(Org.class, Cnd.where("orgId", "in", orgs));
			salaryRule.setCorps(arrcorps);
		}

		

//		if(Asserts.isEmpty(absentItem)){
//			absentItem = new ArrayList<AttendanceThresholdItem>();
//			absentItem.add(new AttendanceThresholdItem());
//		}
//		if(Asserts.isEmpty(forgetItem)){
//			forgetItem = new ArrayList<AttendanceThresholdItem>();
//			forgetItem.add(new AttendanceThresholdItem());
//		}
//		if(Asserts.isEmpty(accumulateItem)){
//			accumulateItem = new ArrayList<AttendanceThresholdItem>();
//			accumulateItem.add(new AttendanceThresholdItem());
//		}
	
		req.setAttribute("corps", corps);
		req.setAttribute("salaryRuleItems", salaryRuleItems);
//		req.setAttribute("forgetItem", forgetItem);
//		req.setAttribute("absentItem", absentItem);
//		req.setAttribute("accumulateItem", accumulateItem);
		req.setAttribute("salaryRule", salaryRule);
	}
	
	/**
	 * 规则保存
	 * @param req
	 * @param res
	 * @return
	 */
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer salaryRuleId = null;
		try {
//			CSRF.validate(req);
			salaryRuleId = Https.getInt(req, "salaryRuleId", R.I);
			String corpIds = Https.getStr(req, "corpIds", R.CLEAN,R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			String salaryRuleName = Https.getStr(req, "salaryRuleName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "配置名称");
			
			SalaryRule salaryRule = null;
			if(salaryRuleId !=null){
				salaryRule = dao.fetch(SalaryRule.class,salaryRuleId);
				Asserts.isNull(salaryRule, "配置不存在");
			}
			else{
				salaryRule = new SalaryRule();
			}
			salaryRule.setOrgIds(corpIds);
			salaryRule.setName(salaryRuleName);
			salaryRule.setStatus(Status.ENABLED);
			
			Map<String, Object> ruleItemMap = Servlets.startsWith(req, "item_");
			
			for(Entry<String, Object> entry : ruleItemMap.entrySet()){
				Validator.validate(entry.getValue(), R.REQUIRED);
			}
			
			transSave(ruleItemMap, salaryRule);
			

			
			Code.ok(mb, (salaryRuleId == null ? "新建" : "编辑") + "工资规则配置成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(AnnualRole:add) error: ", e);
			Code.error(mb, (salaryRuleId == null ? "新建" : "编辑") + "工资规则配置失败");
		}
		return mb;
	}
	
	private void transSave(final Map<String, Object> ruleItemMap, final SalaryRule salaryRule) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (salaryRule.getId() != null) {
					dao.update(salaryRule);
				} else {
					salaryRule.setId(dao.insert(salaryRule).getId());
				}
				
				Sql sql = Sqls.create("UPDATE conf_salary_rule_item SET rule = @rule, conf_salary_rule_id = @salaryRuleId WHERE id = @ruleItemId");		
				
				//判断要更新还是插入
				boolean flag = false;
				
				for(Entry<String, Object> entry : ruleItemMap.entrySet()){
					if(new Integer(entry.getKey()) < 0){
						//要进行读取并插入操作
						flag = true;
						break;
					}
					sql.params()
						.set("rule", entry.getValue())
						.set("salaryRuleId", salaryRule.getId())
						.set("ruleItemId", entry.getKey());
					sql.addBatch();
				}
				List<SalaryRuleItem> salaryRuleItems = null;
				if(flag){
					salaryRuleItems = dao.query(SalaryRuleItem.class,Cnd.where("id", "<", 0).and("status", "=", Status.ENABLED));
					for(SalaryRuleItem salaryRuleItem : salaryRuleItems){
						//替换请求参数的rule，并将id置为null后插入
						Object rule = ruleItemMap.get(salaryRuleItem.getId()+"");
						salaryRuleItem.setRule((String)rule);
						salaryRuleItem.setId(null);
						salaryRuleItem.setSalaryRuleId(salaryRule.getId());
					}
					dao.fastInsert(salaryRuleItems);
				}else{
					//批量更新的操作
					dao.execute(sql);
				}
	
				

				
			}
		});
	}
}
