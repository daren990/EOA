package cn.oa.web.action.res.warn;

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
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Fault;
import cn.oa.model.Warn;
import cn.oa.model.WarnActor;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "res.warn.apply")
@At(value = "/res/warn/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);
	
	@GET
	@At
	@Ok("ftl:res/warn/apply_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/warn/apply/del", token);
		CSRF.generate(req, "/res/warn/approve/actors", token);
		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("w.status", "=", Status.ENABLED)
			.and("w.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "w.approved", "approve", approve);
		Cnds.gte(cri, mb, "w.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "w.create_time", "endTime", endStr);
		cri.getOrderBy().desc("w.modify_time");
		
		Page<Warn> page = Webs.page(req);
		page = mapper.page(Warn.class, page, "Warn.count", "Warn.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@GET
	@At
	@Ok("ftl:res/warn/apply_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer warnId = Https.getInt(req, "warnId", R.REQUIRED, R.I);
		Warn warn = null;
		if (warnId != null) {
			warn = mapper.fetch(Warn.class, "Warn.query", Cnd
					.where("w.user_id", "=", Context.getUserId())
					.and("w.warn_id", "=", warnId));
			if (warn != null) {
				
				// 故障处理人审批
				WarnActor actor = dao.fetch(WarnActor.class, Cnd
						.where("warnId", "=", warnId)
						.and("variable", "=", Roles.WRR.getName())
						.limit(1)
						.asc("modifyTime"));
				if (actor != null) {
					warn.setActorId(actor.getActorId());
				}
			}
		}
		if (warn == null)
			warn = new Warn();
		
		List<Fault> faults = dao.query(Fault.class, Cnd.where("status", "=", Status.ENABLED));

		req.setAttribute("warn", warn);
		req.setAttribute("faults", faults);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer warnId = null;
		try {
			CSRF.validate(req);
			
			warnId = Https.getInt(req, "warnId", R.I);
			
			Integer faultId = Https.getInt(req, "faultId", R.REQUIRED, R.I, "故障类型");
			String content = Https.getStr(req, "content", R.CLEAN, R.REQUIRED, R.RANGE, "{1,200}", "故障说明");
			
			Fault fault = dao.fetch(Fault.class, faultId);
			Asserts.isNull(fault, "故障类型不存在");
			
			DateTime now = new DateTime();
			Warn warn = null;
			if (warnId != null) {
				warn = dao.fetch(Warn.class, Cnd
						.where("userId", "=", Context.getUserId())
						.and("warnId", "=", warnId));
				Asserts.isNull(warn, "报障申请不存在");
				Asserts.notHasAny(warn.getApproved(), new Integer[] { Status.PROOFING, Status.UNAPPROVED }, "禁止编辑已批准的报障申请");
			} else {
				warn = new Warn();
				warn.setCreateTime(now.toDate());
			}
			
			StringBuilder buff = new StringBuilder();
			buff.append("报障申请_")
				.append(Context.getUsername())
				.append("因")
				.append(fault.getFaultName())
				.append("提出申请");

			warn.setUserId(Context.getUserId());
			warn.setSubject(buff.toString());
			warn.setFaultId(faultId);
			warn.setContent(content);
			warn.setApproved(Status.PROOFING);
			warn.setStatus(Status.ENABLED);
			warn.setModifyTime(now.toDate());

			transSave(warnId, warn, fault.getOperatorId());
			
			Code.ok(mb, (warnId == null ? "新建" : "编辑") + "报障申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (warnId == null ? "新建" : "编辑") + "报障申请失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object del(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				dao.update(Warn.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("approved", "=", Status.PROOFING)
						.and("warnId", "in", arr));
			}
			Code.ok(mb, "删除报障申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除报障申请失败");
		}

		return mb;
	}
	
	private void transSave(final Integer warnId, final Warn warn, final Integer actorId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = warnId;
				if (warnId != null) {
					dao.clear(WarnActor.class, Cnd.where("warnId", "=", warnId));
					dao.update(warn);
				} else {
					id = dao.insert(warn).getWarnId();
				}
				
				// 指定故障处理人审批
				dao.insert(new WarnActor(id, actorId, Value.I, Status.PROOFING, Roles.WRR.getName(), warn.getModifyTime(), Status.value));
			}
		});
	}
}