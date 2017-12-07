package cn.oa.web.action.sys.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import cn.oa.consts.Status;
import cn.oa.model.AttendanceThreshold;
import cn.oa.model.AttendanceThresholdItem;
import cn.oa.model.Org;
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
 * 考勤规则配置
 * @author SimonTang
 */
@IocBean
@At("/sys/conf/attendanceThreshold")
public class AttendanceThresholdAction extends Action{
	
	public static Log log = Logs.getLog(AttendanceThresholdAction.class);
	
	/**
	 * 规则列表
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:sys/conf/attendanceThreshold_page")
	public void page(HttpServletRequest req){
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/attendanceThreshold/able", token);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Page<AttendanceThreshold> page = Webs.page(req);
		page = mapper.page(AttendanceThreshold.class, page, "AttendanceThreshold.count","AttendanceThreshold.index",cri);
		for(AttendanceThreshold threshold : page.getResult()){
			Integer arr[] = Converts.array(threshold.getOrgIds(), ",");
			if(arr == null){
				arr = new Integer[100];
				arr[0] = -1; 
			}
			List<Org> corps = dao.query(Org.class, Cnd.where("orgId", "in", arr));
			threshold.setCorps(corps);
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
	@Ok("ftl:sys/conf/attendanceThreshold_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer thresholdId = Https.getInt(req, "thresholdId", R.REQUIRED,R.I);
		// 迟到早退
		List<AttendanceThresholdItem> lateItem = null;
		// 旷工
		List<AttendanceThresholdItem> absentItem = null;
		// 未打卡
		List<AttendanceThresholdItem> forgetItem = null;
		// 迟到早退、旷工、未打卡累积
		List<AttendanceThresholdItem> accumulateItem = null;
		
		AttendanceThreshold threshold;
		if(null == thresholdId){
			threshold = new AttendanceThreshold();
		} else {
			threshold = dao.fetch(AttendanceThreshold.class, thresholdId);
			Asserts.isNull(threshold, "配置不存在");
			lateItem = dao.query(AttendanceThresholdItem.class,Cnd.where("thresholdId", "=", thresholdId).and("type", "=", Status.L).and("status", "=", Status.ENABLED));
			absentItem = dao.query(AttendanceThresholdItem.class,Cnd.where("thresholdId", "=", thresholdId).and("type", "=", Status.A).and("status", "=", Status.ENABLED));
			forgetItem = dao.query(AttendanceThresholdItem.class,Cnd.where("thresholdId", "=", thresholdId).and("type", "=", Status.F).and("status", "=", Status.ENABLED));
			accumulateItem = dao.query(AttendanceThresholdItem.class,Cnd.where("thresholdId", "=", thresholdId).and("type", "=", Status.ACCUMULATE).and("status", "=", Status.ENABLED));
			System.out.println(accumulateItem.size());
		}
		
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		Integer orgs[] = Converts.array(threshold.getOrgIds(), ",");
		if(orgs == null){
			orgs = new Integer[100];
			orgs[0]=-1;
		}
		List<Org> arrcorps = dao.query(Org.class, Cnd.where("orgId", "in", orgs));
		threshold.setCorps(arrcorps);
		
		if(Asserts.isEmpty(lateItem)){
			lateItem = new ArrayList<AttendanceThresholdItem>();
			lateItem.add(new AttendanceThresholdItem());
		}
		if(Asserts.isEmpty(absentItem)){
			absentItem = new ArrayList<AttendanceThresholdItem>();
			absentItem.add(new AttendanceThresholdItem());
		}
		if(Asserts.isEmpty(forgetItem)){
			forgetItem = new ArrayList<AttendanceThresholdItem>();
			forgetItem.add(new AttendanceThresholdItem());
		}
		if(Asserts.isEmpty(accumulateItem)){
			accumulateItem = new ArrayList<AttendanceThresholdItem>();
			accumulateItem.add(new AttendanceThresholdItem());
		}
		System.out.println(accumulateItem.size());
		req.setAttribute("corps", corps);
		req.setAttribute("lateItem", lateItem);
		req.setAttribute("forgetItem", forgetItem);
		req.setAttribute("absentItem", absentItem);
		req.setAttribute("accumulateItem", accumulateItem);
		req.setAttribute("threshold", threshold);
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
		Integer thresholdId = null;
		try {
			CSRF.validate(req);
			thresholdId = Https.getInt(req, "thresholdId", R.I);
			String corpIds = Https.getStr(req, "corpIds", R.CLEAN,R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			String thresholdName = Https.getStr(req, "thresholdName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "配置名称");
			AttendanceThreshold threshold = null;
			if(thresholdId !=null){
				threshold = dao.fetch(AttendanceThreshold.class,thresholdId);
				Asserts.isNull(threshold, "配置不存在");
			}
			else{
				threshold = new AttendanceThreshold();
			}
			Map<String, Object> minuteStartMap = Servlets.startsWith(req, "minuteStart_");
			Map<String, Object> minuteEndMap = Servlets.startsWith(req, "minuteEnd_");
			Map<String, Object> amountStartMap = Servlets.startsWith(req, "amountStart_");
			Map<String, Object> amountEndMap = Servlets.startsWith(req, "amountEnd_");
			Map<String, Object> wayMap = Servlets.startsWith(req, "way_");
			Map<String, Object> unitMap = Servlets.startsWith(req, "unit_");
			Map<String, Object> wageTypeMap = Servlets.startsWith(req, "wageType_");
			Map<String, Object> deductMap = Servlets.startsWith(req, "deduct_");
			
			Map<String, Object> forgetAmountStartMap = Servlets.startsWith(req, "forgetAmountStart_");
			Map<String, Object> forgetAmountEndMap = Servlets.startsWith(req, "forgetAmountEnd_");
			Map<String, Object> forgetwayMap = Servlets.startsWith(req, "forgetway_");
			Map<String, Object> forgetdeductMap = Servlets.startsWith(req, "forgetdeduct_");
			Map<String, Object> forgetunitMap = Servlets.startsWith(req, "forgetunit_");
			Map<String, Object> forgetwageTypeMap = Servlets.startsWith(req, "forgetwageType_");
			
			Map<String, Object> absentAmountStartMap = Servlets.startsWith(req, "absentAmountStart_");
			Map<String, Object> absentAmountEndMap = Servlets.startsWith(req, "absentAmountEnd_");
			Map<String, Object> absentwayMap = Servlets.startsWith(req, "absentway_");
			Map<String, Object> absentdeductMap = Servlets.startsWith(req, "absentdeduct_");
			Map<String, Object> absentunitMap = Servlets.startsWith(req, "absentunit_");
			Map<String, Object> absentwageTypeMap = Servlets.startsWith(req, "absentwageType_");
			
			Map<String, Object> accumulateAmountStartMap = Servlets.startsWith(req, "accumulateAmountStart_");
			Map<String, Object> accumulateAmountEndMap = Servlets.startsWith(req, "accumulateAmountEnd_");
			Map<String, Object> accumulatewayMap = Servlets.startsWith(req, "accumulateway_");
			Map<String, Object> accumulatedeductMap = Servlets.startsWith(req, "accumulatededuct_");
			Map<String, Object> accumulateunitMap = Servlets.startsWith(req, "accumulateunit_");
			Map<String, Object> accumulatewageTypeMap = Servlets.startsWith(req, "accumulatewageType_");
			
			for(Entry<String, Object> entry : unitMap.entrySet()){
				String index = entry.getKey();
				Validator.validate(entry.getValue(), R.REQUIRED, R.I, "迟到/早退单位");
				Validator.validate(minuteStartMap.get(index), R.REQUIRED, R.I, "迟到/早退分钟区间");
				Validator.validate(minuteEndMap.get(index), R.REQUIRED, R.I, "迟到/早退分钟区间");
				Validator.validate(amountStartMap.get(index), R.REQUIRED, R.I, "迟到/早退次数区间");
				Validator.validate(amountEndMap.get(index), R.REQUIRED, R.I, "迟到/早退分钟区间");
				Validator.validate(wayMap.get(index), R.REQUIRED, R.I, "扣款方式");
				Validator.validate(deductMap.get(index), R.REQUIRED, R.F, "单价");
			}
			for(Entry<String, Object> entry : forgetunitMap.entrySet()){
				String index = entry.getKey();
				Validator.validate(entry.getValue(), R.REQUIRED, R.I, "未打卡单位");
				Validator.validate(forgetAmountStartMap.get(index), R.REQUIRED, R.I, "未打卡次数区间");
				Validator.validate(forgetAmountEndMap.get(index), R.REQUIRED, R.I, "未打卡次数区间");
				Validator.validate(forgetwayMap.get(index), R.REQUIRED, R.I, "未打卡扣款方式");
				Validator.validate(forgetdeductMap.get(index), R.REQUIRED, R.F, "未打卡单价");
			}
			for(Entry<String, Object> entry : absentunitMap.entrySet()){
				String index = entry.getKey();
				Validator.validate(entry.getValue(), R.REQUIRED, R.I, "旷工单位");
				Validator.validate(absentAmountStartMap.get(index), R.REQUIRED, R.I, "旷工次数区间");
				Validator.validate(absentAmountEndMap.get(index), R.REQUIRED, R.I, "旷工次数区间");
				Validator.validate(absentwayMap.get(index), R.REQUIRED, R.I, "旷工扣款方式");
				Validator.validate(absentdeductMap.get(index), R.REQUIRED, R.F, "旷工单价");
				Validator.validate(absentunitMap.get(index), R.REQUIRED, R.I, "旷工单位");
			}
			for(Entry<String, Object> entry : accumulateunitMap.entrySet()){
				String index = entry.getKey();
				Validator.validate(entry.getValue(), R.REQUIRED, R.I, "累积单位");
				Validator.validate(accumulateAmountStartMap.get(index), R.REQUIRED, R.I, "累积次数区间");
				Validator.validate(accumulateAmountEndMap.get(index), R.REQUIRED, R.I, "累积次数区间");
				Validator.validate(accumulatewayMap.get(index), R.REQUIRED, R.I, "累积的扣款方式");
				Validator.validate(accumulatedeductMap.get(index), R.REQUIRED, R.F, "累积的扣款金额");
			}
			List<AttendanceThresholdItem> item = new ArrayList<AttendanceThresholdItem>();
			
			for(Entry<String, Object> entry : unitMap.entrySet()){
				String index = entry.getKey();
				String StringUnit = (String) entry.getValue();
				Integer unit = Integer.parseInt(StringUnit);
				Integer minuteStart =Values.getInt(minuteStartMap.get(index));
				Integer minuteEnd =Values.getInt(minuteEndMap.get(index));
				Integer amountStart =Values.getInt(amountStartMap.get(index));
				Integer amountEnd =Values.getInt(amountEndMap.get(index));
				Integer way =Values.getInt(wayMap.get(index));
				Integer wageType = null; 
				if((wageTypeMap.get(index))!=null){
					wageType = Values.getInt(wageTypeMap.get(index));
				}
				String StringDeduct = (String) deductMap.get(index);
				Double deduct = Double.parseDouble(StringDeduct);
				item.add(new AttendanceThresholdItem(Status.L, minuteStart, minuteEnd, amountStart, amountEnd, way, unit, wageType,deduct,Status.ENABLED));
			}
			for(Entry<String, Object> entry : forgetunitMap.entrySet()){
				String index = entry.getKey();
				String  StringUnit = (String)entry.getValue();
				Integer unit = Integer.parseInt(StringUnit);
				Integer amountStart =Values.getInt(forgetAmountStartMap.get(index));
				Integer amountEnd =Values.getInt(forgetAmountEndMap.get(index));
				Integer way =Values.getInt(forgetwayMap.get(index));
				Integer wageType = null; 
				if((forgetwageTypeMap.get(index))!=null){
					wageType = Values.getInt(forgetwageTypeMap.get(index));
				}
				
				String StringDeduct = (String) forgetdeductMap.get(index);
				Double deduct = Double.parseDouble(StringDeduct);
				item.add(new AttendanceThresholdItem(Status.F, null, null, amountStart, amountEnd, way, unit, wageType,deduct,Status.ENABLED));
			}
			
			for(Entry<String, Object> entry : absentunitMap.entrySet()){
				String index = entry.getKey();
				String  StringUnit = (String)entry.getValue();
				Integer unit = Integer.parseInt(StringUnit);
				Integer amountStart =Values.getInt(absentAmountStartMap.get(index));
				Integer amountEnd =Values.getInt(absentAmountEndMap.get(index));
				Integer way =Values.getInt(absentwayMap.get(index));
				Integer wageType = null; 
				if((absentwageTypeMap.get(index))!=null){
					wageType = Values.getInt(absentwageTypeMap.get(index));
				}
				String StringDeduct = (String) absentdeductMap.get(index);
				Double deduct = Double.parseDouble(StringDeduct);
				item.add(new AttendanceThresholdItem(Status.A, null, null, amountStart, amountEnd, way, unit, wageType,deduct,Status.ENABLED));
			}
			
			for(Entry<String, Object> entry : accumulateunitMap.entrySet()){
				String index = entry.getKey();
				String  stringUnit = (String)entry.getValue();
				Integer unit = Integer.parseInt(stringUnit);
				Integer amountStart =Values.getInt(accumulateAmountStartMap.get(index));
				Integer amountEnd =Values.getInt(accumulateAmountEndMap.get(index));
				Integer way =Values.getInt(accumulatewayMap.get(index));
				Integer wageType = null; 
				if((accumulatewageTypeMap.get(index))!=null){
					wageType = Values.getInt(accumulatewageTypeMap.get(index));
				}
				String stringDeduct = (String) accumulatedeductMap.get(index);
				Double deduct = Double.parseDouble(stringDeduct);
				item.add(new AttendanceThresholdItem(Status.ACCUMULATE, null, null, amountStart, amountEnd, way, unit, wageType,deduct,Status.ENABLED));
			}
			
			threshold.setOrgIds(corpIds);
			threshold.setThresholdName(thresholdName);
			threshold.setStatus(Status.ENABLED);
			
			
			
			transSave(thresholdId, corpIds , threshold,item);
			Code.ok(mb, (thresholdId == null ? "新建" : "编辑") + "考勤阀值配置成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(AnnualRole:add) error: ", e);
			Code.error(mb, (thresholdId == null ? "新建" : "编辑") + "考勤阀值配置失败");
		}
		return mb;
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
				dao.update(AttendanceThreshold.class, Chain.make("status", status), Cnd.where("thresholdId", "in", arr));
			}
			Code.ok(mb, "设置配置状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Accountant:able) error: ", e);
			Code.error(mb, "设置配置状态失败");
		}
		return mb;
	}
	
	private void transSave(final Integer thresholdId, final String corpIds, final AttendanceThreshold threshold, final List<AttendanceThresholdItem> item) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = thresholdId;
				if (thresholdId != null) {
					dao.update(threshold);
					dao.update(AttendanceThresholdItem.class, Chain.make("status", Status.DISABLED), Cnd.where("thresholdId", "=", thresholdId));
				} else {
					id = dao.insert(threshold).getThresholdId();
				}
				for(AttendanceThresholdItem thresholdItem : item){
					thresholdItem.setThresholdId(id);
					thresholdItem.setStatus(Status.ENABLED);
				}
				dao.fastInsert(item);
			}
		});
	}
}
