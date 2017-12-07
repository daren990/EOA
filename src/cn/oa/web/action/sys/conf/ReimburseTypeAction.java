package cn.oa.web.action.sys.conf;

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

import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.model.AnnualRole;
import cn.oa.model.ReimburseThreshold;
import cn.oa.model.ReimburseType;
import cn.oa.model.Resign;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
@IocBean
@At(value = "/sys/conf/reimburseTypeAction")
public class ReimburseTypeAction extends Action{
	public static Log log = Logs.getLog(ReimburseTypeAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/reimburseType_page")
	public void page(HttpServletRequest req) {
		
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/reimburseTypeAction/able", token);
		MapBean mb = new MapBean();
		Page<ReimburseType> page = Webs.page(req);
		page = mapper.page(ReimburseType.class, page, "ReimburseType.count", "ReimburseType.index", null);
		
		Page<ReimburseThreshold> threshold = Webs.page(req);
		threshold = mapper.page(ReimburseThreshold.class, threshold, "ReimburseThreshold.count", "ReimburseThreshold.index", null);
		req.setAttribute("page", page);
		req.setAttribute("threshold", threshold);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:sys/conf/reimburseType_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer reimburseTypeId = Https.getInt(req, "reimburseTypeId", R.REQUIRED, R.I);
		ReimburseType reimburseType = null;
		if(reimburseTypeId != null){
			reimburseType = dao.fetch(ReimburseType.class,reimburseTypeId);
		}
		if(reimburseTypeId == null){
			reimburseType = new ReimburseType();
		}
		req.setAttribute("reimburseType", reimburseType);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer reimburseTypeId = null;
		ReimburseType reimburseType =null;
		try{
			CSRF.validate(req);
			reimburseTypeId = Https.getInt(req, "reimburseTypeId", R.I);
			String bigType = Https.getStr(req, "bigType", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "类型名称");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			String smallType = Https.getStr(req, "smallType", R.REQUIRED, "分类集合");
			if(reimburseTypeId !=null){
				reimburseType = dao.fetch(ReimburseType.class,reimburseTypeId);
			}
			if(reimburseTypeId == null){
				reimburseType = new ReimburseType();
			}
			reimburseType.setStatus(status);
			reimburseType.setBigType(bigType);
			reimburseType.setSmallType(smallType);
			
			save(reimburseTypeId, reimburseType);
			
			Code.ok(mb, (reimburseTypeId == null ? "新建" : "编辑") + "报销类型成功");
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (reimburseTypeId == null ? "新建" : "编辑") + "报销类型失败");
		}
		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:sys/conf/reimburseThreshold_add")
	public void addThreshold(HttpServletRequest req) {
		CSRF.generate(req);
		Integer thresholdId = Https.getInt(req, "thresholdId", R.REQUIRED, R.I);
		ReimburseThreshold threshold = null;
		if(thresholdId != null){
			threshold = dao.fetch(ReimburseThreshold.class,thresholdId);
		}
		if(thresholdId == null){
			threshold = new ReimburseThreshold();
		}
		req.setAttribute("threshold", threshold);
	}
	
	@POST
	@At
	@Ok("json")
	public Object addThreshold(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer thresholdId = null;
		ReimburseThreshold threshold = null;
		try{
			CSRF.validate(req);
			thresholdId = Https.getInt(req, "thresholdId", R.I);
			Integer thresholdValue = Https.getInt(req, "thresholdValue", R.REQUIRED, R.I,"审批阀值");
			String thresholdName = Https.getStr(req, "thresholdName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "配置名称");
			if(thresholdId !=null){
				threshold = dao.fetch(ReimburseThreshold.class,thresholdId);
			}
			if(thresholdId == null){
				threshold = new ReimburseThreshold();
			}
			threshold.setThresholdName(thresholdName);
			threshold.setThresholdValue(thresholdValue);
			if(thresholdId ==null)
				dao.insert(threshold);
			else
				dao.update(threshold);
			
			
			Code.ok(mb, (thresholdId == null ? "新建" : "编辑") + "报销审批阀值成功");
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (thresholdId == null ? "新建" : "编辑") + "报销审批阀值失败");
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
				dao.update(ReimburseType.class, Chain.make("status", status), Cnd.where("reimburseTypeId", "in", arr));
			}
			Code.ok(mb, "设置报销类别状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Day:able) error: ", e);
			Code.error(mb, "设置报销类别状态失败");
		}

		return mb;
	}
	
	public void save(Integer reimburseTypeId , ReimburseType reimburseType){
		if(reimburseTypeId == null)
			dao.insert(reimburseType);
		else
			dao.update(reimburseType);
	}
}
