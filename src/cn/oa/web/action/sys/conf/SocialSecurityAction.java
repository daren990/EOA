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
import cn.oa.model.SocialSecurityRule;
import cn.oa.model.SocialSecurityRuleItem;
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
 * 社保规则配置
 * @author Andy
 */
@IocBean
@At("/sys/conf/socialSecurityRule")
public class SocialSecurityAction extends Action {
	
	public static Log log = Logs.getLog(SocialSecurityAction.class);
	
	/**
	 * 规则列表
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:sys/conf/socialSecurity_page")
	public void page(HttpServletRequest req){
//		String token = CSRF.generate(req);
//		CSRF.generate(req, "/sys/conf/attendanceThreshold/able", token);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Page<SocialSecurityRule> page = Webs.page(req);
		page = mapper.page(SocialSecurityRule.class, page, "SocialSecurityRule.count","SocialSecurityRule.index",cri);
		System.out.println(page.getResult().size());
		for(SocialSecurityRule socialSecurityRule : page.getResult()){
			List<Org> corps = dao.query(Org.class, Cnd.where("orgId", "in", socialSecurityRule.getCorpId()));
			System.out.println(corps);
			socialSecurityRule.setCorps(corps);
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
	@Ok("ftl:sys/conf/socialSecurityRule_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer socialSecurityRuleId = Https.getInt(req, "id", R.REQUIRED,R.I);
		// 迟到早退
		List<SocialSecurityRuleItem> socialSecurityRuleItems = new ArrayList<SocialSecurityRuleItem>();

		
		SocialSecurityRule socialSecurityRule = null;
		if(null == socialSecurityRuleId){
			socialSecurityRule = new SocialSecurityRule();
			socialSecurityRuleItems = dao.query(SocialSecurityRuleItem.class,Cnd.where("socialSecurityRuleId", "=", -1).and("status", "=", Status.ENABLED));
		} else {
			socialSecurityRule = dao.fetch(SocialSecurityRule.class, socialSecurityRuleId);
			Asserts.isNull(socialSecurityRule, "配置不存在");
			socialSecurityRuleItems = dao.query(SocialSecurityRuleItem.class,Cnd.where("socialSecurityRuleId", "=", socialSecurityRuleId).and("status", "=", Status.ENABLED));
		}
		
		//查询所有公司，不包含下属机构
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		if(socialSecurityRule.getCorpId() != null){
			List<Org> arrcorps = dao.query(Org.class, Cnd.where("orgId", "in", socialSecurityRule.getCorpId()));
			socialSecurityRule.setCorps(arrcorps);
		}

		req.setAttribute("corps", corps);
		req.setAttribute("socialSecurityRuleItems", socialSecurityRuleItems);
		req.setAttribute("socialSecurityRule", socialSecurityRule);
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
		Integer socialSecurityRuleId = null;
		try {
//			CSRF.validate(req);
			socialSecurityRuleId = Https.getInt(req, "socialSecurityRuleId", R.I);
			String corpId = Https.getStr(req, "corpId", R.CLEAN,R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			String socialSecurityRuleName = Https.getStr(req, "socialSecurityRuleName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "配置名称");
			
			SocialSecurityRule socialSecurityRule = null;
			if(socialSecurityRuleId !=null){
				socialSecurityRule = dao.fetch(SocialSecurityRule.class,socialSecurityRuleId);
				Asserts.isNull(socialSecurityRule, "配置不存在");
			}
			else{
				socialSecurityRule = new SocialSecurityRule();
			}
			socialSecurityRule.setCorpId(Integer.valueOf(corpId));
			socialSecurityRule.setName(socialSecurityRuleName);
			socialSecurityRule.setStatus(Status.ENABLED);
			
			Map<String, Object> ruleItemMap = null;
			List<Map<String, Object>> ruleItemMaps = new ArrayList<Map<String, Object>>();
			

			Map<String, Object> ruleItemIds = Servlets.startsWith(req, "socialSecurityRuleItemId_");
			Map<String, Object> ruleItemNames = Servlets.startsWith(req, "socialSecurityRuleItemName_");
			Map<String, Object> ruleItemRules = Servlets.startsWith(req, "socialSecurityRuleItemRule_");
		
			for(Entry<String, Object> entry : ruleItemIds.entrySet()){
				String index = entry.getKey();
//				String id =  (String) entry.getValue();
				Object name = ruleItemNames.get(index);
				Validator.validate(name, R.REQUIRED, "公式名称");
				Object rule = ruleItemRules.get(index);
				Validator.validate(rule, R.REQUIRED, "公式");
				ruleItemMap = new TreeMap<String, Object>();
				//在tranSave()方法中从ruleItemMap中获取value时，所用的key跟这里的key是相同的
				ruleItemMap.put("socialSecurityRuleItemName", name);
				ruleItemMap.put("socialSecurityRuleItemRule", rule);
				ruleItemMaps.add(ruleItemMap);
			}
			

			transSave(ruleItemMaps, socialSecurityRule);

			Code.ok(mb, (socialSecurityRuleId == null ? "新建" : "编辑") + "工资规则配置成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(AnnualRole:add) error: ", e);
			Code.error(mb, (socialSecurityRuleId == null ? "新建" : "编辑") + "工资规则配置失败");
		}
		return mb;
	}
	
	
	private void transSave(final List<Map<String, Object>> ruleItemMaps, final SocialSecurityRule socialSecurityRule) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = socialSecurityRule.getId();
				if (id != null) {
					dao.update(socialSecurityRule);
					dao.update(SocialSecurityRuleItem.class, Chain.make("status", Status.DISABLED), Cnd.where("socialSecurityRuleId", "=", socialSecurityRule.getId()));
				} else {
					id = dao.insert(socialSecurityRule).getId();
				}
				
				List<SocialSecurityRuleItem> ssris = new ArrayList<SocialSecurityRuleItem>();
				for(Map<String, Object> ruleItemMap : ruleItemMaps){
					SocialSecurityRuleItem socialSecurityRuleItem = new SocialSecurityRuleItem();
					socialSecurityRuleItem.setName((String) ruleItemMap.get("socialSecurityRuleItemName"));
					socialSecurityRuleItem.setRule((String) ruleItemMap.get("socialSecurityRuleItemRule"));
					socialSecurityRuleItem.setSocialSecurityRuleId(id);
					socialSecurityRuleItem.setStatus(Status.ENABLED);
					ssris.add(socialSecurityRuleItem);
				}
				dao.fastInsert(ssris);
			}
		});
	}
}
