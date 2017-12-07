package cn.oa.web.action.sys.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import cn.oa.model.AttendanceThresholdItem;
import cn.oa.model.ConfLeaveType;
import cn.oa.model.Org;
import cn.oa.model.SalaryRule;
import cn.oa.model.SalaryRuleItem;
import cn.oa.model.TaxRule;
import cn.oa.model.TaxRuleItem;
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
 * 税收规则配置
 * @author Andy
 */
@IocBean
@At("/sys/conf/taxRule")
public class TaxAction extends Action {
	
	public static Log log = Logs.getLog(TaxAction.class);
	
	/**
	 * 规则列表
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:sys/conf/taxRule_page")
	public void page(HttpServletRequest req){
//		String token = CSRF.generate(req);
//		CSRF.generate(req, "/sys/conf/attendanceThreshold/able", token);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Page<TaxRule> page = Webs.page(req);
		page = mapper.page(TaxRule.class, page, "TaxRule.count","TaxRule.index",cri);
		System.out.println(page.getResult().size());
		for(TaxRule taxRule : page.getResult()){
//			Integer arr[] = Converts.array(salaryRule.getOrgIds(), ",");
//			if(arr == null){
//				arr = new Integer[100];
//				arr[0] = -1; 
//			}
			List<Org> corps = dao.query(Org.class, Cnd.where("orgId", "in", taxRule.getCorpId()));
			System.out.println(corps);
			taxRule.setCorps(corps);
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
	@Ok("ftl:sys/conf/taxRule_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer taxRuleId = Https.getInt(req, "id", R.REQUIRED,R.I);
		// 迟到早退
		List<TaxRuleItem> taxRuleItems = new ArrayList<TaxRuleItem>();
//		// 旷工
//		List<AttendanceThresholdItem> absentItem = null;
//		// 未打卡
//		List<AttendanceThresholdItem> forgetItem = null;
//		// 迟到早退、旷工、未打卡累积
//		List<AttendanceThresholdItem> accumulateItem = null;
		
		TaxRule taxRule = null;
		if(null == taxRuleId){
			taxRule = new TaxRule();
			taxRuleItems = dao.query(TaxRuleItem.class,Cnd.where("taxRuleId", "=", -1).and("status", "=", Status.ENABLED));
		} else {
			taxRule = dao.fetch(TaxRule.class, taxRuleId);
			Asserts.isNull(taxRule, "配置不存在");
			taxRuleItems = dao.query(TaxRuleItem.class,Cnd.where("taxRuleId", "=", taxRuleId).and("status", "=", Status.ENABLED));
		}
		
		//查询所有公司，不包含下属机构
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		if(taxRule.getCorpId() != null){
			List<Org> arrcorps = dao.query(Org.class, Cnd.where("orgId", "in", taxRule.getCorpId()));
			taxRule.setCorps(arrcorps);
		}

		req.setAttribute("corps", corps);
		req.setAttribute("taxRuleItems", taxRuleItems);
		req.setAttribute("taxRule", taxRule);
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
		Integer taxRuleId = null;
		try {
//			CSRF.validate(req);
			taxRuleId = Https.getInt(req, "taxRuleId", R.I);
			String corpId = Https.getStr(req, "corpId", R.CLEAN,R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			String taxRuleName = Https.getStr(req, "taxRuleName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "配置名称");
			
			TaxRule taxRule = null;
			if(taxRuleId !=null){
				taxRule = dao.fetch(TaxRule.class,taxRuleId);
				Asserts.isNull(taxRule, "配置不存在");
			}
			else{
				taxRule = new TaxRule();
			}
			taxRule.setCorpId(Integer.valueOf(corpId));
			taxRule.setName(taxRuleName);
			taxRule.setStatus(Status.ENABLED);
			
			Map<String, Object> ruleItemMap = null;
			List<Map<String, Object>> ruleItemMaps = new ArrayList<Map<String, Object>>();
			

			Map<String, Object> ruleItemIds = Servlets.startsWith(req, "taxRuleItemId_");
			Map<String, Object> ruleItemNames = Servlets.startsWith(req, "taxRuleItemName_");
			Map<String, Object> ruleItemRules = Servlets.startsWith(req, "taxRuleItemRule_");
		
			for(Entry<String, Object> entry : ruleItemIds.entrySet()){
				String index = entry.getKey();
//				String id =  (String) entry.getValue();
				Object name = ruleItemNames.get(index);
				Validator.validate(name, R.REQUIRED, "公式名称");
				Object rule = ruleItemRules.get(index);
				Validator.validate(rule, R.REQUIRED, "公式");
				ruleItemMap = new TreeMap<String, Object>();
				//在tranSave()方法中从ruleItemMap中获取value时，所用的key跟这里的key是相同的
				ruleItemMap.put("taxRuleItemName", name);
				ruleItemMap.put("taxRuleItemRule", rule);
				ruleItemMaps.add(ruleItemMap);
			}
			

			transSave(ruleItemMaps, taxRule);

			Code.ok(mb, (taxRuleId == null ? "新建" : "编辑") + "工资规则配置成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(AnnualRole:add) error: ", e);
			Code.error(mb, (taxRuleId == null ? "新建" : "编辑") + "工资规则配置失败");
		}
		return mb;
	}
	
	
	private void transSave(final List<Map<String, Object>> ruleItemMaps, final TaxRule taxRule) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				
				Integer id = taxRule.getId();
				if (id != null) {
					dao.update(taxRule);
					dao.update(TaxRuleItem.class, Chain.make("status", Status.DISABLED), Cnd.where("taxRuleId", "=", taxRule.getId()));
				} else {
					id = dao.insert(taxRule).getId();
				}
				
				List<TaxRuleItem> tris = new ArrayList<TaxRuleItem>();
				for(Map<String, Object> ruleItemMap : ruleItemMaps){
					TaxRuleItem taxRuleItem = new TaxRuleItem();
					taxRuleItem.setName((String) ruleItemMap.get("taxRuleItemName"));
					taxRuleItem.setRule((String) ruleItemMap.get("taxRuleItemRule"));
					taxRuleItem.setTaxRuleId(id);
					taxRuleItem.setStatus(Status.ENABLED);
					tris.add(taxRuleItem);
				}
				dao.fastInsert(tris);

			}
		});
	}
}
