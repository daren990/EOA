package cn.oa.web.action.res.change;

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
import cn.oa.model.AssetItem;
import cn.oa.model.AssetShop;
import cn.oa.model.AssetShopActor;
import cn.oa.model.Dict;
import cn.oa.model.Target;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "res.change.approveshop")
@At(value = "/res/change/approveshop")
public class ApproveShopAction extends Action{
	
	public static Log log = Logs.getLog(ApproveShopAction.class);
	
	@GET
	@At
	@Ok("ftl:res/change/approveshop_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/change/approveshop/actors", token);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("a.actor_id", "=", Context.getUserId());
		Cnds.gte(cri, mb, "s.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "s.create_time", "endTime", endStr);
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		cri.getOrderBy().desc("s.modify_time");
		
		Page<AssetShop> page = Webs.page(req);
		page = mapper.page(AssetShop.class, page, "AssetShopApprove.count", "AssetShopApprove.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer shopId = Https.getInt(req, "shopId", R.REQUIRED, R.I, "申请ID");
			List<AssetShopActor> actors = mapper.query(AssetShopActor.class, "AssetShopActor.query", Cnd
					.where("a.shop_id", "=", shopId)
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
	
	@GET
	@At
	@Ok("ftl:res/change/approveshop_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer shopId = Https.getInt(req, "shopId", R.REQUIRED, R.I, "申请ID");
		AssetShop assetShop = mapper.fetch(AssetShop.class, "AssetShopApprove.query", Cnd
				.where("a.actor_id", "=", Context.getUserId())
				.and("s.shop_id", "=", shopId));
		Asserts.isNull(assetShop, "申请不存在");
		
		List<AssetItem> assetItem = dao.query(AssetItem.class, Cnd.where("shopId", "=", shopId));
		Asserts.isEmpty(assetItem, "申购物品不存在");

		List<AssetShopActor> actors = mapper.query(AssetShopActor.class, "AssetShopActor.query", Cnd
				.where("a.shop_id", "=", shopId)
				.asc("a.modify_time"));
		AssetShopActor actor = null; // 当前审批人员
		AssetShopActor next = null; // 指向下一审批人员
		
		Integer bindId = null;
		for (AssetShopActor e : actors) {
			if (e.getActorId().equals(Context.getUserId())) actor = e;
			if (e.getVariable().equals(Roles.GM.getName())) bindId = e.getActorId();
		}
		if (actor.getRefererId() != 0) {
			for (AssetShopActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId())) next = e;
			}
		}
		
		List<User> operators = null;
		String [] str=userService.findRoleNames(Context.getUserId());
		
		if(Asserts.hasAny(Roles.SSVI.getName(),str)){
			operators = userService.operators(Roles.BOSS.getName());
		}
		else
			operators = userService.operators(Roles.SSVI.getName());
			
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
		req.setAttribute("assetItem", assetItem);
		req.setAttribute("operators", operators);
		req.setAttribute("assetShop", assetShop);
		req.setAttribute("actors", actors);
		req.setAttribute("actor", actor);
		req.setAttribute("next", next);
		req.setAttribute("bindId", bindId);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer shopId = null;
		try {
			CSRF.validate(req);
			
			shopId = Https.getInt(req, "shopId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1,2]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			
			DateTime now = new DateTime();
			AssetShop assetShop = mapper.fetch(AssetShop.class, "AssetShopApprove.query", Cnd
					.where("a.actor_id", "=", Context.getUserId())
					.and("s.shop_id", "=", shopId));
			Asserts.isNull(assetShop, "申请不存在");

			AssetShopActor actor = dao.fetch(AssetShopActor.class, Cnd
					.where("shopId", "=", shopId)
					.and("actorId", "=", Context.getUserId()));
			Asserts.isNull(actor, "禁止审批购买申请");
			
			Integer required = R.CLEAN;
			if (!actor.getVariable().equals(Roles.BOSS.getName()) && approve.equals(Status.APPROVED)) {
				required = R.REQUIRED;
			}
			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			refererId = Values.getInt(refererId);
			
			if (actor.getRefererId() != 0) {
				AssetShopActor next = dao.fetch(AssetShopActor.class, Cnd
						.where("shopId", "=", shopId)
						.and("actorId", "=", actor.getRefererId()));
				if (next != null)
					Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的购买申请");
			}
			
			String [] str=userService.findRoleNames(Context.getUserId());
			String nextVariable = null;
			if (approve.equals(Status.APPROVED)) {
				if(Asserts.hasAny(Roles.SSVI.getName(),str)){
					nextVariable = Roles.BOSS.getName();
				}
				else
					nextVariable = Roles.SSVI.getName();
			}
			
			
			transApprove(shopId, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, opinion);
			Code.ok(mb, "申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "申请审批失败");
		}

		return mb;
	}
	
	private void transApprove(final Integer shopId, final Integer approve, final Integer actorId, final String variable,
			final Integer refererId, final String nextVariable, final DateTime now, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.PROOFING;
				dao.update(AssetShopActor.class, Chain
						.make("approve", approve)
						.add("refererId", refererId)
						.add("modifyTime", now.toDate())
						.add("opinion", opinion), Cnd
						.where("shopId", "=", shopId)
						.and("actorId", "=", actorId));
				dao.clear(AssetShopActor.class, Cnd.where("shopId", "=", shopId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						dao.insert(new AssetShopActor(shopId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Status.value));
					self = refererId.equals(Value.I) ? Status.OK : Status.PROOFING;
				}
				
				else if(approve.equals(Status.UNAPPROVED)&&refererId.equals(Value.I)){
					self = Status.UNAPPROVED;
				}
				
				
				dao.update(AssetShop.class, Chain
						.make("approve", self)
						.add("modifyTime", now.toDate()), Cnd
						.where("shopId", "=", shopId));
			}
		});
	}
}
