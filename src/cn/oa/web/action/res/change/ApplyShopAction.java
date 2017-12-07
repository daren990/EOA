package cn.oa.web.action.res.change;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import cn.oa.model.Asset;
import cn.oa.model.AssetItem;
import cn.oa.model.AssetShop;
import cn.oa.model.AssetShopActor;
import cn.oa.model.Change;
import cn.oa.model.ChangeActor;
import cn.oa.model.Dict;
import cn.oa.model.PerformActor;
import cn.oa.model.Target;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Servlets;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "res.change.applyshop")
@At(value = "/res/change/applyshop")

public class ApplyShopAction extends Action{
	
	public static Log log = Logs.getLog(ApplyShopAction.class);
	
	@GET
	@At
	@Ok("ftl:res/change/applyshop_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/change/apply/del", token);
		CSRF.generate(req, "/res/change/approveshop/actors", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("s.status", "=" , Status.ENABLED)
			.and("s.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "s.approved", "approve", approve);
		Cnds.gte(cri, mb, "s.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "s.create_time", "endTime", endStr);
		cri.getOrderBy().desc("s.modify_time");

		Page<AssetShop> page = Webs.page(req);
		page = mapper.page(AssetShop.class, page, "AssetShop.count", "AssetShop.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:res/change/applyshop_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer shopId = Https.getInt(req, "shopId", R.I);
		AssetShop assetShop = null;
		List<AssetItem> assetItem = null;
		if (shopId != null) {
			assetShop = mapper.fetch(AssetShop.class, "AssetShop.query", Cnd
					.where("s.user_id", "=", Context.getUserId())
					.and("s.status", "=" , Status.ENABLED)
					.and("s.shop_id", "=", shopId));
			if (assetShop != null) {
				assetItem = dao.query(AssetItem.class, Cnd.where("shopId", "=", shopId));
				Map<String, Object> map = new ConcurrentHashMap<String, Object>();
				map.put("shop_id", shopId);
				
				// 上级审批
				AssetShopActor actor = dao.fetch(AssetShopActor.class, Cnd
						.where("shopId", "=", shopId)
						.and("variable", "=", Roles.GM.getName())
						.limit(1)
						.asc("modifyTime"));
				if (actor != null) {
					assetShop.setActorId(actor.getActorId());
				}
			}
		}
		if(Asserts.isEmpty(assetItem)){
			assetItem = new ArrayList<AssetItem>();
			assetItem.add(new AssetItem());
		}
		
		if (assetShop == null)
			assetShop = new AssetShop();
		List<User> operators = null;
		
		//本公司员工申请要经过总经理
		operators = userService.operators(Context.getCorpId(), Roles.GM.getName());
		String roleNames[]=userService.findRoleNames(Context.getUserId());
		
		for(User user : operators){
			//如果申请人是总经理直接跳到行政主管
			if(user.getUserId().equals(Context.getUserId())){
				operators = userService.operators(new String[]{Roles.SSVI.getName()});
			}
		}
		//如果是行政专员,或者是行政经理直接跳到行政主管
		if(Asserts.hasAny(roleNames, new String[]{ Roles.ADR.getName()})||Asserts.hasAny(roleNames, new String[]{ Roles.MADR.getName()})){
			operators = userService.operators(new String[]{Roles.SSVI.getName()});
		}
		//如果申请人是人事专员，下一级审批人是上级
		else if(Asserts.hasAny(roleNames, new String[]{ Roles.PSR.getName()})){
			operators = userService.operators(Context.getCorpId(), Context.getLevel(),new String[]{Roles.BOSS.getName()});
		}
		//如果申请人是市场专员，下一级审批人是上级
		else if(Asserts.hasAny(roleNames, new String[]{ Roles.MOR.getName()})){
			operators = userService.operators(Context.getCorpId(), Context.getLevel(), new String[]{Roles.BOSS.getName()});
		}
		//如果申请人是出纳或者会计，下一级审批人是上级
		else if(Asserts.hasAny(roleNames, new String[]{ Roles.CAS.getName(),Roles.ACC.getName()})){
			operators = userService.operators(Context.getCorpId(), Context.getLevel(), new String[]{Roles.BOSS.getName()});
		}
		//如果是人事经理和主管，市场经理和主管，直接跳去行政主管，财务经理，出纳主管，会计主管
		else if(Asserts.hasAny(roleNames, new String[]{Roles.CSVI.getName(),Roles.FIM.getName(),Roles.ACC.getName(), Roles.MMOR.getName(), Roles.MPSR.getName(), Roles.MSVI.getName(), Roles.PSVI.getName()})){
			operators = userService.operators(new String[]{Roles.SSVI.getName()});
		}
		
	//	List<Asset> assets = dao.query(Asset.class, Cnd.where("status", "=", Status.ENABLED));
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
		req.setAttribute("assetItem", assetItem);
		req.setAttribute("operators", operators);
		req.setAttribute("assetShop", assetShop);
	//	req.setAttribute("assets", assets);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer shopId = null;
		try {
			CSRF.validate(req);
			shopId = Https.getInt(req, "shopId", R.I);
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "购买原因");
			Integer actorId = Https.getInt(req, "actorId", R.REQUIRED, R.I, "审批人员");
			/*Integer item = Https.getInt(req, "item", R.CLEAN, R.I);
			String storagePlace = Https.getStr(req, "storagePlace", R.CLEAN, R.REQUIRED, R.RANGE, "{1,30}", "storagePlace:存放地点");
			Integer amount = Https.getInt(req, "amount", R.CLEAN, R.I);
			String itemName = Https.getStr(req, "itemName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,30}", "itemName:名称");
			String remark = Https.getStr(req, "remark", R.CLEAN, R.RANGE, "{1,60}", "备注");*/
			
			
			AssetShop shop = null;
			DateTime now = new DateTime();
			
			if (shopId != null) {
				shop = dao.fetch(AssetShop.class, Cnd
						.where("userId", "=", Context.getUserId())
						.and("status", "=" , Status.ENABLED)
						.and("shopId", "=", shopId));
				Asserts.isNull(shop, "申请不存在");
				Asserts.notEqOr(shop.getApprove(),Status.APPROVED,Status.OK, "禁止修改已审批的申购申请");
			} else {
				shop = new AssetShop();
				shop.setCreateTime(now.toDate());
			}
			
			Map<String, Object> itemMap = Servlets.startsWith(req, "item_");
			Map<String, Object> storagePlaceMap = Servlets.startsWith(req, "storagePlace_");
			Map<String, Object> amountMap = Servlets.startsWith(req, "amount_");
			Map<String, Object> itemNameMap = Servlets.startsWith(req, "itemName_");
			Map<String, Object> remarkMap = Servlets.startsWith(req, "remark_");
			Map<String, Object> unitMap = Servlets.startsWith(req, "unit_");
			
			for (Entry<String, Object> entry : itemNameMap.entrySet()) {
				String index = entry.getKey();
				Validator.validate(entry.getValue(), R.CLEAN, R.REQUIRED, R.RANGE, "{1,30}", "申购物品名称");
				Validator.validate(itemMap.get(index), R.REQUIRED, R.I, "类别");
				Validator.validate(amountMap.get(index),R.CLEAN, R.REQUIRED, R.I, "数量");
				Validator.validate(unitMap.get(index), R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "单位");
				Validator.validate(storagePlaceMap.get(index), R.CLEAN, R.REQUIRED, R.RANGE, "{1,50}", "存放地点");
				Validator.validate(remarkMap.get(index),R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}" , "特殊说明");
			}
			
			StringBuilder buff = new StringBuilder();
			buff.append("资产申购_")
				.append(Context.getUsername())
				.append("申请购买资产");
				

			shop.setUserId(Context.getUserId());
			shop.setModifyTime(now.toDate());
			shop.setActorId(actorId);
			shop.setReason(reason);
			shop.setApprove(Status.PROOFING);
			shop.setSubject(buff.toString());
			shop.setStatus(Status.ENABLED);
			
			String [] str=userService.findRoleNames(actorId);
			
			String variable=null;
			//总经理
			if(Asserts.hasAny(Roles.SSVI.getName(),str)){
				variable=Roles.GM.getName();
			}
			else 
					variable=Roles.SSVI.getName();
			
			transSave(shopId, shop, actorId, itemMap, storagePlaceMap, amountMap, itemNameMap, remarkMap, unitMap, variable);

			Code.ok(mb, (shopId == null ? "新建" : "编辑") + "购买申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (shopId == null ? "新建" : "编辑") + "购买申请失败");
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
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "shopIds");
			Integer[] arr = Converts.array(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				dao.update(AssetShop.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("approve", "=", Status.PROOFING)
						.and("shopId", "in", arr));
			}	
			Code.ok(mb, "删除购买申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除购买申请失败");
		}

		return mb;
	}
	     
	private void transSave(final Integer shopId, final AssetShop shop, final Integer actorId,
			final Map<String, Object> itemMap,
			final Map<String, Object> storagePlaceMap,
			final Map<String, Object> amountMap,
			final Map<String, Object> itemNameMap,
			final Map<String, Object> remarkMap,
			final Map<String, Object> unitMap, 
			final String variable) {
			
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = shopId;
				if (shopId != null) {
					dao.clear(AssetShopActor.class, Cnd.where("shopId", "=", shopId));
					dao.update(shop);
				} else {
					id = dao.insert(shop).getShopId();
				}
				
				dao.clear(AssetItem.class, Cnd.where("shopId", "=", id));
				List<AssetItem> items = new ArrayList<AssetItem>();
				for (Entry<String, Object> entry : itemNameMap.entrySet()) {
					String index = entry.getKey();
					String itemName = (String) entry.getValue();
					Integer item = Values.getInt(itemMap.get(index));
					Integer amount = Values.getInt(amountMap.get(index));
					String unit = Values.getStr((String)(unitMap.get(index)),"");
					String storagePlace = Values.getStr((String)(storagePlaceMap.get(index)),"");
					String remark = Values.getStr((String)(remarkMap.get(index)),"");
					items.add(new AssetItem(id, item, storagePlace, amount, remark, itemName, unit, Status.ENABLED));
				}
				dao.fastInsert(items);
				
				// 指定上级审批
				dao.clear(AssetShopActor.class, Cnd.where("shopId", "=", shopId).and("actorId", "=", actorId));
				dao.insert(new AssetShopActor(id, actorId, Value.I, Status.PROOFING, variable, shop.getModifyTime(), Status.value));
			}
		});
	}
}
