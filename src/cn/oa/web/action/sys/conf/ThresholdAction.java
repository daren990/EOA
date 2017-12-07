package cn.oa.web.action.sys.conf;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Status;
import cn.oa.model.Org;
import cn.oa.model.Threshold;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.RMB;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/sys/conf/threshold")
public class ThresholdAction extends Action {

public static Log log = Logs.getLog(MonthAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/threshold_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/threshold/able", token);
		
		Page<Threshold> page = Webs.page(req);
		page = mapper.page(Threshold.class, page, "Threshold.count", "Threshold.index", null);

		req.setAttribute("page", page);
	}
	
	@At
	@Ok("ftl:sys/conf/threshold_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer orgId = Https.getInt(req, "orgId", R.I);
		
		Threshold threshold = null;
		if (orgId != null) {
			threshold = dao.fetch(Threshold.class, orgId);
		}
		if (threshold == null) threshold = new Threshold();
		
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		req.setAttribute("threshold", threshold);
		req.setAttribute("corps", corps);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer orgId = null;
		boolean exist = true;
		
		try {
			CSRF.validate(req);
			orgId = Https.getInt(req, "orgId", R.REQUIRED, R.I, "公司绑定");

			Integer overMinute = Https.getInt(req, "overMinute", R.REQUIRED, R.I, "分钟阈值");
			Integer overTimes = Https.getInt(req, "overTimes", R.REQUIRED, R.I, "少于阈值迟到/早退次数");
			Float deductionPerOverTime = Https.getFloat(req, "deductionPerOverTime", R.REQUIRED, R.I, "少于阈值每次扣除金额");
			Integer sickDays = Https.getInt(req, "sickDays", R.REQUIRED, R.I, "病假天数");
	//		Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			Threshold threshold = dao.fetch(Threshold.class, orgId);
			if (threshold == null){
				threshold = new Threshold();
				threshold.setOrgId(orgId);
				exist = false;
			}
			
			threshold.setOverMinute(overMinute);
			threshold.setOverTimes(overTimes);
			threshold.setDeductionPerOverTime(RMB.on(deductionPerOverTime));
			threshold.setSickDays(sickDays);
		//	threshold.setStatus(status);
			
			if (exist) {
				dao.update(threshold);
			} else {
				dao.insert(threshold);
			}
			
			Code.ok(mb, (exist ? "编辑" : "新建") + "考勤阈值成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Threshold:add) error: ", e);
			Code.error(mb, (exist ? "编辑" : "新建") + "考勤阈值失败");
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
				dao.update(Threshold.class, Chain.make("status", status), Cnd.where("orgId", "in", arr));
			}
			Code.ok(mb, "设置考勤阈值状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Threshold:able) error: ", e);
			Code.error(mb, "设置考勤阈值状态失败");
		}

		return mb;
	}
}
