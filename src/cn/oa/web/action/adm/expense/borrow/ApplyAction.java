package cn.oa.web.action.adm.expense.borrow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import cn.oa.model.Borrow;
import cn.oa.model.BorrowActor;
import cn.oa.model.User;
import cn.oa.model.Wage;
import cn.oa.utils.Asserts;
import cn.oa.utils.BorrowNumber;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.RMB;
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

@IocBean(name = "adm.expense.borrow.apply")
@At(value = "/adm/expense/borrow/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/expense/borrow/apply/del", token);
		CSRF.generate(req, "/adm/expense/borrow/approve/actors", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		String number = Https.getStr(req, "number", R.CLEAN,"{1,100}");

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("b.status", "=", Status.ENABLED)
			.and("b.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "b.approved", "approve", approve);
		Cnds.eq(cri, mb, "b.number", "number", number);
		Cnds.gte(cri, mb, "b.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "b.create_time", "endTime", endStr);
		cri.getOrderBy().desc("b.modify_time");

		Page<Borrow> page = Webs.page(req);
		page = mapper.page(Borrow.class, page, "Borrow.count", "Borrow.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/apply_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/apply_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer borrowId = Https.getInt(req, "borrowId", R.I);
		Borrow borrow = null;
		if (borrowId != null) {
			borrow = mapper.fetch(Borrow.class, "Borrow.query", Cnd
					.where("b.status", "=", Status.ENABLED)
					.and("b.user_id", "=", Context.getUserId())
					.and("b.borrow_id", "=", borrowId));
			if (borrow != null) {
				Map<String, Object> map = new ConcurrentHashMap<String, Object>();
				map.put("borrow_id", borrowId);
				
				// 助理审批
				BorrowActor actor = dao.fetch(BorrowActor.class, Cnd
						.where("borrowId", "=", borrowId)
						.and("variable", "=", Roles.ASS.getName())
						.limit(1)
						.asc("modifyTime"));
				if (actor != null) {
					borrow.setActorId(actor.getActorId());
				}
			}
		}
		if (borrow == null)
			borrow = new Borrow();

		List<User> operators = userService.operators(Context.getCorpId(), Roles.ASS.getName());

		req.setAttribute("operators", operators);
		req.setAttribute("borrow", borrow);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/apply_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/borrow/apply_add_wx")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}

	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer borrowId = null;
		try {
			CSRF.validate(req);
			borrowId = Https.getInt(req, "borrowId", R.I);
			Float money = Https.getFloat(req, "money", R.REQUIRED, R.F, "借支金额");
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "借支原因");
			String repayStr = Https.getStr(req, "repay_yyyyMMdd", R.REQUIRED, R.D, "还款日期");
			Integer actorId = Https.getInt(req, "actorId", R.REQUIRED, R.I, "助理审批");
			
			Borrow borrow = null;
			DateTime now = new DateTime();
			
			Integer amount = RMB.toMinute(money);
			Wage wage = dao.fetch(Wage.class, Context.getUserId());
			Asserts.isNull(wage, "工资设置不能为空");
			Integer standard = wage.getStandardSalary();
			Asserts.isNull(standard, "基本工资不能为空");
			if(Asserts.hasAny(Roles.GM.getName(), Context.getRoles())){
				Asserts.isLe(amount, standard*2, "借支金额不能大于基本工资的两倍");
			}
			else
				Asserts.isLe(amount, standard, "借支金额不能大于基本工资");
				
			
			
			if (borrowId != null) {
				borrow = dao.fetch(Borrow.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("borrowId", "=", borrowId));
				Asserts.isNull(borrow, "申请不存在");
				Asserts.notEq(borrow.getApproved(), Status.PROOFING, "禁止修改已审批的借支申请");
			} else {
				borrow = new Borrow();
				borrow.setReturned(Status.DISABLED);
				borrow.setStatus(Status.ENABLED);
				borrow.setCreateTime(now.toDate());
			}

			StringBuilder buff = new StringBuilder();
			buff.append(Context.getTrueName())
				.append("申请借支")
				.append(money)
				.append("元");

			
			borrow.setUserId(Context.getUserId());
			borrow.setSubject(buff.toString());
			borrow.setMoney(RMB.on(money));
			borrow.setReason(reason);
			borrow.setRepayDate(Calendars.parse(repayStr, Calendars.DATE).toDate());
			borrow.setApproved(Status.PROOFING);
			borrow.setModifyTime(now.toDate());
			
			transSave(borrowId, borrow, actorId);

			Code.ok(mb, (borrowId == null ? "新建" : "编辑") + "借支申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (borrowId == null ? "新建" : "编辑") + "借支申请失败");
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
				dao.update(Borrow.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("approved", "=", Status.PROOFING)
						.and("borrowId", "in", arr));
			}
			Code.ok(mb, "删除借支申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除借支申请失败");
		}

		return mb;
	}
	
	private void transSave(final Integer borrowId, final Borrow borrow, final Integer actorId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = borrowId;
				if (borrowId != null) {
					dao.clear(BorrowActor.class, Cnd.where("borrowId", "=", borrowId));
					borrow.setNumber(BorrowNumber.create(id));
					dao.update(borrow);
				} else {
					Borrow resultSet = dao.insert(borrow);
					resultSet.setNumber(BorrowNumber.create(resultSet.getBorrowId()));
					dao.update(resultSet);
					id = resultSet.getBorrowId();
				}
				
				// 指定助理审批
				dao.insert(new BorrowActor(id, actorId, Value.I, Status.PROOFING, Roles.ASS.getName(), borrow.getModifyTime(),Status.value));
			}
		});
	}
}
