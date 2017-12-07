package cn.oa.web.action.sys.conf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.ioc.val.SysPropValue;
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
import cn.oa.model.ConfMeasure;
import cn.oa.model.Measure;
import cn.oa.model.Org;
import cn.oa.model.SalaryRule;
import cn.oa.model.SalaryRuleItem;
import cn.oa.model.ShiftClass;
import cn.oa.model.User;
import cn.oa.model.WorkMonth;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Servlets;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At("/sys/conf/performance")
public class PerformanceAction extends Action{
	public static Log log = Logs.getLog(PerformanceAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/performance_page")
	public void page(HttpServletRequest req){
	
//		String token = CSRF.generate(req);
//		CSRF.generate(req, "/sys/conf/attendanceThreshold/able", token);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Page<ConfMeasure> page = Webs.page(req);
		page = mapper.page(ConfMeasure.class, page, "ConfMeasure.count","ConfMeasure.index",cri);
		System.out.println(page.getResult().size());
		for(ConfMeasure confMeasure : page.getResult()){
			Integer arr[] = Converts.array(confMeasure.getOrgIds(), ",");
			if(arr == null){
				arr = new Integer[100];
				arr[0] = -1; 
			}
			List<Org> corps = dao.query(Org.class, Cnd.where("orgId", "in", arr));
			confMeasure.setCorps(corps);
		}
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@At
	@Ok("ftl:sys/conf/performance_add")
	public void add(HttpServletRequest req) {
//		CSRF.generate(req);
		Integer id = Https.getInt(req, "id", R.REQUIRED, R.I);
		
		StringBuffer buffer = new StringBuffer();
		ConfMeasure confMeasure = null;
		String split = "";
		if (id != null) {
			confMeasure = dao.fetch(ConfMeasure.class, id);
			Integer arr[] = Converts.array(confMeasure.getOrgIds(), ",");
			List<Measure> measures = dao.query(Measure.class, Cnd.where("orgId", "=", arr[0]+"").desc("score"));
			Asserts.isEmpty(measures, "未配置相应的绩效评分标准！");
			for (Measure measure : measures) {
				buffer.append(split+measure.getScore()+":"+measure.getCoefficient());
				split = "|";
			}
		}else{
			confMeasure = new ConfMeasure();
		}

		MapBean mb = new MapBean();
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		req.setAttribute("level", buffer.toString());
		req.setAttribute("corps", corps);
		req.setAttribute("confMeasure", confMeasure);
		req.setAttribute("mb", mb);
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

		Integer confMeasureId = null;
		try {
//			CSRF.validate(req);
			ConfMeasure confMeasure = null;
			confMeasureId = Https.getInt(req, "confMeasureId", R.I);
			if(confMeasureId == null){
				confMeasure = new ConfMeasure();
			}else{
				confMeasure = dao.fetch(ConfMeasure.class, confMeasureId);
				Asserts.isNull(confMeasure, "配置不存在");
			}
			String orgId = Https.getStr(req, "orgId", R.CLEAN,R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			String confMeasureName = Https.getStr(req, "confMeasureName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "配置名称");
			String level = Https.getStr(req, "level", R.REQUIRED);
			confMeasure.setOrgIds(orgId);
			confMeasure.setName(confMeasureName);
			confMeasure.setStatus(Status.ENABLED);
			

			HashMap<Float, Float> levelMap = new HashMap<Float, Float>();
			String[] levels = level.split("\\|");
			for(String l : levels){
				String[] arr = l.split(":");
				levelMap.put(Float.valueOf(arr[0]), Float.valueOf(arr[1]));
			}
	
			transSave(levelMap, confMeasure);
			

			
			Code.ok(mb, (confMeasureId == null ? "新建" : "编辑") + "工资规则配置成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(AnnualRole:add) error: ", e);
			Code.error(mb, (confMeasureId == null ? "新建" : "编辑") + "工资规则配置失败");
		}
		return mb;
	}
	
	
	private void transSave(final Map<Float, Float> levelMap, final ConfMeasure confMeasure) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (confMeasure.getId() != null) {
					dao.update(confMeasure);
				} else {
					dao.clear(ConfMeasure.class, Cnd.where("orgIds", "=", confMeasure.getOrgIds()));
					dao.insert(confMeasure).getId();
				}

				dao.clear(Measure.class, Cnd.where("orgId", "=", Integer.valueOf(confMeasure.getOrgIds())));
				List<Measure> measures = new ArrayList<Measure>();
				for(Entry<Float, Float> entry : levelMap.entrySet()){
					Measure m = new Measure();
					m.setOrgId(Integer.valueOf(confMeasure.getOrgIds()));
					m.setScore(entry.getKey());
					m.setCoefficient(entry.getValue());
					measures.add(m);
				}
				dao.fastInsert(measures);
				
			}
		});
	}

}
