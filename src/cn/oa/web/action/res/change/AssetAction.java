package cn.oa.web.action.res.change;

import java.io.UnsupportedEncodingException;

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

import cn.oa.consts.Status;
import cn.oa.model.Asset;
import cn.oa.model.Dict;
import cn.oa.model.Overtime;
import cn.oa.utils.Asserts;
import cn.oa.utils.AssetNumber;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.RMB;
import cn.oa.utils.Rnds;
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

@IocBean(name = "res.change.asset")
@At(value = "/res/change/asset")
public class AssetAction extends Action {

	public static Log log = Logs.getLog(AssetAction.class);
	
	@GET
	@At
	@Ok("ftl:res/change/asset_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/asset/able", token);
		
		String assetName = Https.getStr(req, "assetName", R.CLEAN);
		String assetNumber = Https.getStr(req, "assetNumber", R.CLEAN);
		Integer typeId = Https.getInt(req, "typeId", R.CLEAN, R.I);
		Integer state= Https.getInt(req, "state", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.eq(cri, mb, "a.asset_name", "assetName", assetName);
		Cnds.eq(cri, mb, "a.asset_number", "assetNumber", assetNumber);
		Cnds.eq(cri, mb, "a.type_id", "typeId", typeId);
		Cnds.eq(cri, mb, "a.state", "state", state);
		cri.getOrderBy().desc("a.modify_time");
		
		
		
		Page<Asset> page = Webs.page(req);
		page = mapper.page(Asset.class, page, "Asset.count", "Asset.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
	}

	@GET
	@At
	@Ok("ftl:res/change/asset_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer assetId = Https.getInt(req, "assetId", R.REQUIRED, R.I);
		Asset asset = null;
		if (assetId != null) {
			asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=", assetId));
		}
		if (asset == null)
			asset = new Asset();
		asset.setAmount(1);
		req.setAttribute("asset", asset);
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer assetId = null;
		try {
			CSRF.validate(req);
			
			assetId = Https.getInt(req, "assetId", R.I);
			
			String assetName = Https.getStr(req, "assetName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "资产名称");
			Integer typeId = Https.getInt(req, "typeId", R.REQUIRED, R.I, "资产类型");
			Integer amount = Https.getInt(req, "amount", R.REQUIRED, R.I, "数量");
			String unit = Https.getStr(req, "unit", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "单位");
			Float price = Https.getFloat(req, "price", R.REQUIRED, R.F, "单价");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			String shopTime_yyyyMMdd = Https.getStr(req, "shopTime_yyyyMMdd", R.REQUIRED, R.D, "购买日期");
			String storagePlace = Https.getStr(req, "storagePlace", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "存放地点");
			String planAge_yyyyMMdd = Https.getStr(req, "planAge_yyyyMMdd", R.REQUIRED, R.D, "计划使用年限");
			String model = Https.getStr(req, "model");
			String custodian = Https.getStr(req, "custodian");
			if(model.length()>30){
				throw new Errors("资产型号的值过长");}
			if(custodian.length()>30){
				throw new Errors("保管人的值过长");}
			DateTime now = new DateTime();
			Asset asset = null;
			
			if (assetId != null) {
				asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=", assetId));
				Asserts.isNull(asset, "资产不存在");
			} else {
				asset = new Asset();
				asset.setCreateTime(now.toDate());
			}
			DateTime shopTime = Calendars.parse(shopTime_yyyyMMdd, Calendars.DATE);
			DateTime planAge = Calendars.parse(planAge_yyyyMMdd, Calendars.DATE);
			
			asset.setShopTime(shopTime.toDate());
			asset.setStoragePlace(storagePlace);
			asset.setPlanAge(planAge.toDate());
			asset.setAssetName(assetName);
			asset.setTypeId(typeId);
			asset.setAmount(amount);
			asset.setUnit(unit);
			asset.setPrice(RMB.on(price));
			asset.setStatus(status);
			asset.setModifyTime(now.toDate());
			asset.setState(Status.UNUSED);
			asset.setModel(model);
			asset.setCustodian(custodian);
			
			transSave(assetId, asset);
			
			if(amount > 1){
				for(int i=0; i<amount - 1; i++){
					Asset countAsset = new Asset();
					
					countAsset.setShopTime(shopTime.toDate());
					countAsset.setStoragePlace(storagePlace);
					countAsset.setPlanAge(planAge.toDate());
					countAsset.setAssetName(assetName);
					countAsset.setTypeId(typeId);
					countAsset.setAmount(amount);
					countAsset.setUnit(unit);
					countAsset.setPrice(RMB.on(price));
					countAsset.setStatus(status);
					countAsset.setModifyTime(now.toDate());
					countAsset.setCreateTime(now.toDate());
					countAsset.setState(Status.UNUSED);
					countAsset.setModel(model);
					countAsset.setCustodian(custodian);
					transSave(null,countAsset);
				}
			}
			
			
			Code.ok(mb, (assetId == null ? "新建" : "编辑") + "资产成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Asset:add) error: ", e);
			Code.error(mb, (assetId == null ? "新建" : "编辑") + "资产失败");
		}

		return mb;
	}
	
	private void transSave(final Integer assetId, final Asset asset) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (assetId != null) {
					dao.update(asset);
				} else {
					asset.setAssetNumber(AssetNumber.PREF + Rnds.getStr(6));
					Asset resultSet = dao.insert(asset);
					resultSet.setAssetNumber(AssetNumber.create(resultSet.getAssetId()));
					dao.update(resultSet);
				}
			}
		});
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
				dao.update(Asset.class, Chain.make("status", status), Cnd.where("assetId", "in", arr));
			}
			Code.ok(mb, "设置资产状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Asset:able) error: ", e);
			Code.error(mb, "设置资产状态失败");
		}

		return mb;
	}
}
