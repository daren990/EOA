package cn.oa.web.action.hrm.regular;

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
import cn.oa.model.Archive;
import cn.oa.model.RegularBusiness;
import cn.oa.model.Resign;
import cn.oa.model.ResignActor;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
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

@IocBean(name = "hrm.regular.business")
@At(value = "/hrm/regular/business")
public class ApproveBusinessAction extends Action {

	public static Log log = Logs.getLog(ApproveBusinessAction.class);
	
	@GET
	@At
	@Ok("ftl:hrm/regular/approve_business")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/hrm/resign/approve/actors", token);
		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		
		RegularBusiness rb = new RegularBusiness();
		rb = dao.fetch(RegularBusiness.class);
		List<Roles> roles = null;
		roles = dao.query(Roles.class, Cnd.where("role_name", "<>", "admin").and("status", "=", 1));
		if(roles != null)
			req.setAttribute("operators", roles);
		req.setAttribute("rb", rb);
		req.setAttribute("mb", mb);
	}

	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer resignId = Https.getInt(req, "resignId", R.REQUIRED, R.I, "申请ID");
			List<ResignActor> actors = mapper.query(ResignActor.class, "ResignActor.query", Cnd
					.where("a.resign_id", "=", resignId)
					.asc("a.modify_time"));
			mb.put("actors", actors);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:actors) error: ", e);
		}

		return mb;
	}
	
	private void transApprove(final Integer resignId, final Integer approve, final Integer actorId, final String variable,
			final Integer refererId, final String nextVariable, final DateTime now, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				dao.update(ResignActor.class, Chain
						.make("approve", approve)
						.add("refererId", refererId)
						.add("opinion", opinion)
						.add("modifyTime", now.toDate()), Cnd
						.where("resignId", "=", resignId)
						.and("actorId", "=", actorId)
						.and("variable", "=", variable));
				dao.clear(ResignActor.class, Cnd.where("resignId", "=", resignId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						dao.insert(new ResignActor(resignId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(),"-"));
					self = refererId.equals(Value.I) ? Status.OK : Status.APPROVED;
				}
				
				dao.update(Resign.class, Chain
						.make("approved", self)
						.add("modifyTime", now.toDate()), Cnd
						.where("resignId", "=", resignId));
				//如果离职审批结束,则更改人事档案离职状态和离职日期
				if(self.equals(Status.OK)){
				Resign resign = dao.fetch(Resign.class,resignId);
				/*Archive archive = dao.fetch(Archive.class,resign.getUserId());
				dao.update(archive);*/
				dao.update(Archive.class, Chain
						.make("quit_date", now.toDate())
						.add("on_position", 0), Cnd
						.where("user_id", "=", resign.getUserId()));
				}
			}
		});
	}
	
}