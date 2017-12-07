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
import cn.oa.model.Asset;
import cn.oa.model.AssetScriap;
import cn.oa.model.AssetScriapActor;
import cn.oa.model.Dict;
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

@IocBean(name = "res.change.assetscriapApprove")
@At(value = "/res/change/assetscriapApprove")
public class AssetScriptApprove extends Action{
	
	
public static Log log = Logs.getLog(AssetScriapAction.class);
	
	@GET
	@At
	@Ok("ftl:res/change/assetscriapApprove_page")
	public void page(HttpServletRequest req){
		
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/change/assetscriapApprove/actors", token);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		String assetName = Https.getStr(req, "assetName", R.CLEAN);
		String assetNumber = Https.getStr(req, "assetNumber", R.CLEAN);
		Integer approve= Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
		.and("rs.actor_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "a.asset_name", "assetName", assetName);
		Cnds.eq(cri, mb, "a.asset_number", "assetNumber", assetNumber);
		Cnds.eq(cri, mb, "rs.approve", "approve", approve);
		Cnds.gte(cri, mb, "s.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "s.create_time", "endTime", endStr);
		cri.getOrderBy().desc("s.create_time");
		
		
		
		Page<AssetScriap> page = Webs.page(req);
		page = mapper.page(AssetScriap.class, page, "AssetScriapApprove.count", "AssetScriapApprove.index", cri);

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
			Integer scriapId = Https.getInt(req, "scriapId", R.REQUIRED, R.I, "资产调配ID");
			List<AssetScriapActor> actors = mapper.query(AssetScriapActor.class, "AssetScriapActor.query", Cnd
					.where("rs.scriap_id", "=", scriapId)
					.asc("rs.modify_time"));
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
	@Ok("ftl:res/change/assetscriapApprove_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer scriapId = Https.getInt(req, "scriapId", R.REQUIRED, R.I, "申请ID");
		Asset asset = null;
		
		 AssetScriap assetScriap = mapper.fetch(AssetScriap.class, "AssetScriapApprove.query", Cnd
				.where("rs.actor_id", "=", Context.getUserId())
				.and("s.scriap_id", "=", scriapId));
		Asserts.isNull(assetScriap, "资产报废申请不存在");
		
		if(assetScriap != null){
			asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=", assetScriap.getAssetId()));
		}
		
		List<AssetScriapActor> actors = mapper.query(AssetScriapActor.class, "AssetScriapActor.query", Cnd
				.where("rs.scriap_id", "=", scriapId)
				.asc("rs.modify_time"));
		
		AssetScriapActor actor = null;
		AssetScriapActor next = null;
		
		Integer bindId = null;
		for(AssetScriapActor e : actors){
			if (e.getActorId().equals(Context.getUserId())) actor = e;
			if (e.getVariable().equals(Roles.ASS.getName())) bindId = e.getActorId();
		}
		
		if (actor.getRefererId() != 0) {
			for (AssetScriapActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId())) next = e;
			}
		}
		
		List<User> operators = null;
		if(actor.getVariable().equals(Roles.GM.getName())){
			operators = userService.operators(new String[]{Roles.SSVI.getName(), Roles.MADR.getName()});
		}
		else if(Asserts.hasAny(actor.getVariable(), new String []{Roles.SSVI.getName(), Roles.MADR.getName()})){
			operators = userService.operators(new String[]{Roles.CSVI.getName(), Roles.ASVI.getName(), Roles.FIM.getName()});
		}
		else if(Asserts.hasAny(actor.getVariable(), new String[]{Roles.CSVI.getName(), Roles.ASVI.getName(), Roles.FIM.getName()})){
			operators = userService.operators(Roles.BOSS.getName());
		}
		String applyName = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", assetScriap.getApplyId())).getTrueName();
		assetScriap.setApplyName(applyName);
		
		if(asset != null)
		req.setAttribute("asset", asset);
		req.setAttribute("operators", operators);
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
		req.setAttribute("assetScriap", assetScriap);
		req.setAttribute("bindId", bindId);
		req.setAttribute("actor", actor);
		req.setAttribute("actors", actors);
		req.setAttribute("next", next);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer scriapId = null;
		try {
			CSRF.validate(req);
			
			scriapId = Https.getInt(req, "scriapId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1,2]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			String storagePlace = Https.getStr(req, "storagePlace", R.CLEAN, R.REQUIRED, R.RANGE, "{1,30}", "storagePlace:存放地点");
			
			DateTime now = new DateTime();
			AssetScriap assetScriap = mapper.fetch(AssetScriap.class, "AssetScriapApprove.query", Cnd
					.where("rs.actor_id", "=", Context.getUserId())
					.and("s.scriap_id", "=", scriapId));
			Asserts.isNull(assetScriap, "申请不存在");	
			
			AssetScriapActor actor = dao.fetch(AssetScriapActor.class, Cnd
					.where("scriapId", "=", scriapId)
					.and("actorId", "=", Context.getUserId()));
			Asserts.isNull(actor, "禁止审批报废申请");
			
			Integer required = R.CLEAN;
			if (!actor.getVariable().equals(Roles.BOSS.getName()) && approve.equals(Status.APPROVED)) {
				required = R.REQUIRED;
			}
			Integer operatorId = Https.getInt(req, "operatorId", R.CLEAN, required, R.I, "审批人员");
			operatorId = Values.getInt(operatorId);
			
			if (actor.getRefererId() != 0) {
				AssetScriapActor next = dao.fetch(AssetScriapActor.class, Cnd
						.where("scriapId", "=", scriapId)
						.and("actorId", "=", actor.getRefererId()));
				if (next != null)
					Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的报废申请");
			}
			String nextVariable = "-";
			
			if (approve.equals(Status.APPROVED)) {
				if (actor.getVariable().equals(Roles.GM.getName())) {
						nextVariable = Roles.SSVI.getName();
				}
				if(actor.getVariable().equals(Roles.SSVI.getName())){
					
						nextVariable = Roles.CSVI.getName();
				}
				if(actor.getVariable().equals(Roles.CSVI.getName())){
					nextVariable = Roles.BOSS.getName();
				}
			}
			
			transApprove(scriapId,assetScriap.getAssetId(), approve, Context.getUserId(), actor.getVariable(), operatorId, nextVariable, now, opinion, storagePlace);
			Code.ok(mb, "申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "申请审批失败");
		}

		return mb;
		
	}
	
	private void transApprove(final Integer scriapId,final Integer assetId, final Integer approve, final Integer userId,
							final String variable, final Integer refererId, final String nextVariable, final DateTime now, final String opinion, final String storagePlace){
		
			Trans.exec(new Atom(){

				@Override
				public void run() {
					Integer self = Status.UNAPPROVED;
					dao.update(AssetScriapActor.class, Chain
							.make("approve", approve)
							.add("refererId", refererId)
							.add("modifyTime", now.toDate())
							.add("opinion", opinion), Cnd
							.where("scriapId", "=", scriapId)
							.and("actorId", "=", Context.getUserId()));
					dao.clear(AssetScriapActor.class, Cnd.where("scriapId", "=", scriapId).and("approve", "=", Status.PROOFING));
					if (approve.equals(Status.APPROVED)) {
						if (!refererId.equals(Value.I))
							dao.insert(new AssetScriapActor(scriapId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Status.value));
						self = refererId.equals(Value.I) ? Status.OK : Status.APPROVED;
					}
						
					dao.update(AssetScriap.class, Chain
							.make("approve", self)
							.add("modifyTime", now.toDate()), Cnd
							.where("scriapId", "=", scriapId));
					
					if(variable.equals(Roles.BOSS.getName())&& self.equals(Status.OK)){
					Asset asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=",assetId ));
					asset.setModifyTime(now.toDate());
					asset.setStoragePlace(storagePlace);
					asset.setState(Status.SCRAP);
					dao.update(asset);
					}
				}
				
			});
	}
}
