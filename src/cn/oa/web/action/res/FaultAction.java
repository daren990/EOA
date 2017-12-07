package cn.oa.web.action.res;

import java.util.List;

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

import cn.oa.consts.Status;
import cn.oa.model.Fault;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean(name = "res.fault")
@At(value = "/res/fault")
public class FaultAction extends Action {

	public static Log log = Logs.getLog(FaultAction.class);
	
	@GET
	@At
	@Ok("ftl:res/fault_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/fault/able", token);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.getOrderBy().desc("f.modify_time");
		
		Page<Fault> page = Webs.page(req);
		page = mapper.page(Fault.class, page, "Fault.count", "Fault.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@GET
	@At
	@Ok("ftl:res/fault_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer faultId = Https.getInt(req, "faultId", R.REQUIRED, R.I);
		Fault fault = null;
		if (faultId != null) {
			fault = mapper.fetch(Fault.class, "Fault.query", Cnd.where("f.fault_id", "=", faultId));
		}
		if (fault == null)
			fault = new Fault();

		List<User> operators = dao.query(User.class, Cnd.where("status", "=", Status.ENABLED));

		req.setAttribute("fault", fault);
		req.setAttribute("operators", operators);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer faultId = null;
		try {
			CSRF.validate(req);
			
			faultId = Https.getInt(req, "faultId", R.I);
			String faultName = Https.getStr(req, "faultName", R.CLEAN, R.RANGE, "{1,20}", "故障名称");
			Integer operatorId = Https.getInt(req, "operatorId", R.REQUIRED, R.I, "故障处理人");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			DateTime now = new DateTime();
			Fault fault = null;
			
			if (faultId != null) {
				fault = mapper.fetch(Fault.class, "Fault.query", Cnd.where("f.fault_id", "=", faultId));
				Asserts.isNull(fault, "故障类型不存在");
			} else {
				fault = new Fault();
				fault.setCreateTime(now.toDate());
			}

			fault.setFaultName(faultName);
			fault.setOperatorId(operatorId);
			fault.setStatus(status);
			fault.setModifyTime(now.toDate());

			if (faultId != null)
				dao.update(fault);
			else
				dao.insert(fault);
			
			Code.ok(mb, (faultId == null ? "新建" : "编辑") + "故障类型成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Fault:add) error: ", e);
			Code.error(mb, (faultId == null ? "新建" : "编辑") + "故障类型失败");
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
			
			if (arr != null && arr.length == 1) {
				dao.update(Fault.class, Chain.make("status", status), Cnd.where("faultId", "in", arr));
			}
			Code.ok(mb, "设置故障类型状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Fault:able) error: ", e);
			Code.error(mb, "设置故障类型状态失败");
		}

		return mb;
	}
}
