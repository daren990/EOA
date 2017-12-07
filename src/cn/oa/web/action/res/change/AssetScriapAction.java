package cn.oa.web.action.res.change;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
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
import cn.oa.utils.except.Errors;
import cn.oa.utils.helper.Works;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "res.change.assetscriap")
@At(value = "/res/change/assetscriap")
public class AssetScriapAction extends Action{
	
	public static Log log = Logs.getLog(AssetScriapAction.class);
	
	@GET
	@At
	@Ok("ftl:res/change/assetscriap_page")
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
		Cnds.eq(cri, mb, "a.asset_name", "assetName", assetName);
		Cnds.eq(cri, mb, "a.asset_number", "assetNumber", assetNumber);
		Cnds.eq(cri, mb, "s.approve", "approve", approve);
		Cnds.gte(cri, mb, "s.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "s.create_time", "endTime", endStr);
		cri.getOrderBy().desc("s.create_time");
		
		
		
		Page<AssetScriap> page = Webs.page(req);
		page = mapper.page(AssetScriap.class, page, "AssetScriap.count", "AssetScriap.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:res/change/assetscriap_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer scriapId = Https.getInt(req, "scriapId", R.I);
		Integer assetId = Https.getInt(req, "assetId", R.I);
		AssetScriap assetScriap = null;
		Asset asset = null;
		Integer corp_id = null;
		DateTime now =new DateTime();
		
		if(scriapId != null){
			assetScriap = mapper.fetch(AssetScriap.class, "AssetScriap.query", Cnd
					.where("s.scriap_id", "=", scriapId));
		
		
		if(assetScriap != null){
			String applyName = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", assetScriap.getApplyId())).getTrueName();
			assetScriap.setApplyName(applyName);
			asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=", assetScriap.getAssetId()));
			corp_id = asset.getOrgId();
			AssetScriapActor actor = dao.fetch(AssetScriapActor.class, Cnd
					.where("scriapId", "=", scriapId)
					.and("variable", "=", Roles.GM.getName())
					.limit(1)
					.asc("modifyTime"));
			
				if(actor != null){
					assetScriap.setActorId(actor.getActorId());
				}
					
			}
		}
		
		if(assetId != null){
			asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=", assetId));
			corp_id = asset.getOrgId();
		}
		
		if(assetScriap == null){
			assetScriap = new AssetScriap();
			assetScriap.setAssetId(assetId);
			assetScriap.setApplyName(Context.getTrueName());
			assetScriap.setCreateTime(now.toDate());
			assetScriap.setRealAge(Works.monthsBetween(asset.getShopTime(), now.toDate())) ;
		}
		
		
		
		if(assetScriap.getCreateTime().before(asset.getPlanAge())){
			assetScriap.setScriapType(1);
		}
		else 
			assetScriap.setScriapType(0);
		
		
		
		List<User> operators = userService.operators(corp_id,Roles.GM.getName());
		req.setAttribute("operators", operators);
		req.setAttribute("asset", asset);
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
		req.setAttribute("assetScriap", assetScriap);
	}
	 
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer scriapId = null;
		Integer assetId = null;
		try {
			CSRF.validate(req);
			scriapId = Https.getInt(req, "scriapId", R.I);
			assetId = Https.getInt(req, "assetId", R.I);
			Integer realage = Https.getInt(req, "realage", R.REQUIRED, R.I, "实际使用时间");
			Integer scriapType = Https.getInt(req, "scriapType", R.REQUIRED, R.I, "报废类型");
			Integer depreciation = Https.getInt(req, "depreciation", R.F, "已提折旧额");
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "报废原因");
			String storagePlace = Https.getStr(req, "storagePlace", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "报废存放地点");
			Integer refererId = Https.getInt(req, "operatorId", R.F, "审批人员");
			
			AssetScriap assetScriap = null;
			DateTime now = new DateTime();
			
			if (scriapId != null) {
				assetScriap = mapper.fetch(AssetScriap.class, "AssetScriap.query", Cnd
						.where("s.scriap_id", "=" ,scriapId));
				Asserts.isNull(assetScriap, "资产报废不存在");
				Asserts.notEqOr(assetScriap.getApprove(), Status.OK, "禁止修改已审批的资产报废申请");
			} else {
				assetScriap = new AssetScriap();
				assetScriap.setCreateTime(now.toDate());
			}
			
			Asset asset = dao.fetch(Asset.class, assetId);
			
			StringBuilder buff = new StringBuilder();
			buff.append("资产报废_")
				.append(Context.getTrueName())
				.append("关于")
				.append(asset.getAssetName())
				.append("报废的申请");
				
			assetScriap.setAssetId(assetId);
			assetScriap.setUserId(asset.getUserId());
			assetScriap.setApplyId(Context.getUserId());
			assetScriap.setModifyTime(now.toDate());
			assetScriap.setScriapType(scriapType);
			assetScriap.setDepreciation(depreciation);
			assetScriap.setReason(reason);
			assetScriap.setApprove(Status.PROOFING);
			assetScriap.setStoragePlace(storagePlace);
			assetScriap.setRealAge(realage);
			assetScriap.setSubject(buff.toString());
			
			
			transSave(scriapId, assetScriap, refererId);
			
			Code.ok(mb, (scriapId == null ? "新建" : "编辑") + "资产报废申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (scriapId == null ? "新建" : "编辑") + "资产报废申请失败");
		}
		return mb;
	}
	
	public void transSave(final Integer scriapId, final AssetScriap assetScriap, final Integer refererId){
		Trans.exec(new Atom(){

			@Override
			public void run() {
				Integer id = scriapId;
				if(scriapId != null){
					dao.clear(AssetScriapActor.class, Cnd.where("scriapId", "=", scriapId));
					dao.update(assetScriap);
				}
				else{
					id = dao.insert(assetScriap).getScriapId();
				}
				dao.insert(new AssetScriapActor(id, refererId, Value.I,Status.PROOFING, Roles.GM.getName(), assetScriap.getModifyTime(), Status.value));
			}
			
		});
		
	}
}
