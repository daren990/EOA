package cn.oa.web.action.res.ticket;

import java.util.List;
import java.util.Map;

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
import cn.oa.model.Dict;
import cn.oa.model.Ticket;
import cn.oa.model.TicketActor;
import cn.oa.model.User;
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

@IocBean(name = "res.ticket.apply")
@At(value = "/res/ticket/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	@GET
	@At
	@Ok("ftl:res/ticket/apply_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/ticket/apply/del", token);
		CSRF.generate(req, "/res/ticket/approve/actors", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("t.status", "=", Status.ENABLED)
			.and("t.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "t.approved", "approve", approve);
		Cnds.gte(cri, mb, "t.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "t.create_time", "endTime", endStr);
		cri.getOrderBy().desc("t.modify_time");

		Page<Ticket> page = Webs.page(req);
		page = mapper.page(Ticket.class, page, "Ticket.count", "Ticket.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@GET
	@At
	@Ok("ftl:res/ticket/apply_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer ticketId = Https.getInt(req, "ticketId", R.I);
		Ticket ticket = null;
		if (ticketId != null) {
			ticket = mapper.fetch(Ticket.class, "Ticket.query", Cnd
					.where("t.status", "=", Status.ENABLED)
					.and("t.user_id", "=", Context.getUserId())
					.and("t.ticket_id", "=", ticketId));
			if (ticket != null) {
				// 助理审批
				TicketActor actor = dao.fetch(TicketActor.class, Cnd
						.where("ticketId", "=", ticketId)
						.and("variable", "=", Roles.ASS.getName())
						.limit(1)
						.asc("modifyTime"));
				if (actor != null) {
					ticket.setActorId(actor.getActorId());
				}
			}
		}
		if (ticket == null)
			ticket = new Ticket();

		List<User> operators = userService.operators(Context.getCorpId(), Roles.ASS.getName());

		req.setAttribute("operators", operators);
		req.setAttribute("ticket", ticket);
		req.setAttribute("ticketMap", dictService.map(Dict.TICKET));
	}

	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer ticketId = null;
		try {
			CSRF.validate(req);
			ticketId = Https.getInt(req, "ticketId", R.I);
			
			Integer typeId = Https.getInt(req, "typeId", R.REQUIRED, R.I, "票务类型");
			String startPlace = Https.getStr(req, "startPlace", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "出发地点");
			String endPlace = Https.getStr(req, "endPlace", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "到达地点");
			String phone = Https.getStr(req, "phone", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "联系电话");
			String idcard = Https.getStr(req, "idcard", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "身份证");
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "订票原因");
			String remark = Https.getStr(req, "remark", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "备注");
			Integer actorId = Https.getInt(req, "actorId", R.REQUIRED, R.I, "助理审批");
			
			Ticket ticket = null;
			DateTime now = new DateTime();
			
			if (ticketId != null) {
				ticket = dao.fetch(Ticket.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("ticketId", "=", ticketId));
				Asserts.isNull(ticket, "申请不存在");
				Asserts.notEq(ticket.getApproved(), Status.PROOFING, "禁止修改已审批的票务申请");
			} else {
				ticket = new Ticket();
				ticket.setStatus(Status.ENABLED);
				ticket.setCreateTime(now.toDate());
			}

			Map<String, String> ticketMap = dictService.map(Dict.TICKET);
			
			StringBuilder buff = new StringBuilder();
			buff.append("票务申请_")
				.append(Context.getUsername())
				.append("预订")
				.append(ticketMap.get(String.valueOf(typeId)))
				.append(startPlace)
				.append("至")
				.append(endPlace);
			
			ticket.setUserId(Context.getUserId());
			ticket.setSubject(buff.toString());
			ticket.setTypeId(typeId);
			ticket.setStartPlace(startPlace);
			ticket.setEndPlace(endPlace);
			ticket.setPhone(phone);
			ticket.setIdcard(idcard);
			ticket.setReason(reason);
			ticket.setRemark(remark);
			ticket.setApproved(Status.PROOFING);
			ticket.setModifyTime(now.toDate());
			
			transSave(ticketId, ticket, actorId);

			Code.ok(mb, (ticketId == null ? "新建" : "编辑") + "票务申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (ticketId == null ? "新建" : "编辑") + "票务申请失败");
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
				dao.update(Ticket.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("approved", "=", Status.PROOFING)
						.and("ticketId", "in", arr));
			}
			Code.ok(mb, "删除票务申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除票务申请失败");
		}

		return mb;
	}
	
	private void transSave(final Integer ticketId, final Ticket ticket, final Integer actorId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = ticketId;
				if (ticketId != null) {
					dao.clear(TicketActor.class, Cnd.where("ticketId", "=", ticketId));
					dao.update(ticket);
				} else {
					id = dao.insert(ticket).getTicketId();
				}
				
				// 指定助理审批
				dao.insert(new TicketActor(id, actorId, Value.I, Status.PROOFING, Roles.ASS.getName(), ticket.getModifyTime(), Status.value));
			}
		});
	}
}