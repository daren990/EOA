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

import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Asset;
import cn.oa.model.AssetTransfer;
import cn.oa.model.Change;
import cn.oa.model.Dict;
import cn.oa.model.User;

import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.MapBean;
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

@IocBean(name = "res.change.assettransfer")
@At(value = "/res/change/assettransfer")
public class AssetTransferAction extends Action{
	
	public static Log log = Logs.getLog(AssetTransferAction.class);
	
	@GET
	@At
	@Ok("ftl:res/change/assettransfer_page")
	public void page(HttpServletRequest req) {

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		String assetName = Https.getStr(req, "assetName", R.CLEAN);
		String assetNumber = Https.getStr(req, "assetNumber", R.CLEAN);
		Integer approve= Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.eq(cri, mb, "a.asset_name", "assetName", assetName);
		Cnds.eq(cri, mb, "a.asset_number", "assetNumber", assetNumber);
		Cnds.eq(cri, mb, "t.approve", "approve", approve);
		Cnds.gte(cri, mb, "t.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "t.create_time", "endTime", endStr);
		cri.getOrderBy().desc("t.create_time");
		
		
		
		Page<AssetTransfer> page = Webs.page(req);
		page = mapper.page(AssetTransfer.class, page, "AssetTransfer.count", "AssetTransfer.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:res/change/assettransfer_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer transferId = Https.getInt(req, "transferId", R.I);
		Integer assetId = Https.getInt(req, "assetId", R.I);
		AssetTransfer assetTransfer = null;
		Asset asset = null;
		String trueName ="";
		
		if(transferId != null){
			assetTransfer = mapper.fetch(AssetTransfer.class, "AssetTransfer.query", Cnd
					.where("t.transfer_id", "=" ,transferId));
		}
			
		if(assetTransfer != null){
			asset = dao.fetch(Asset.class, assetTransfer.getAssetId());
			trueName =mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", assetTransfer.getTransferPersonId())).getTrueName();
			}
		else{
			assetTransfer = new AssetTransfer();
		}
			
		if(assetId !=null){
			asset = dao.fetch(Asset.class, assetId);
			trueName = Context.getTrueName();
		}	
		
		assetTransfer.setAssetName(asset.getAssetName());
		assetTransfer.setAssetNumber(asset.getAssetNumber());
		assetTransfer.setItem(asset.getTypeId());
		assetTransfer.setAssetId(asset.getAssetId());
		assetTransfer.setMoveTrueName(trueName);
		
		
		List<User> operators = mapper.query(User.class, "User.query", Cnd.where("u.status", "=", Status.ENABLED));
		req.setAttribute("operators", operators);
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
		req.setAttribute("assetTransfer", assetTransfer);
		
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer transferId = null;
		Integer assetId = null;
		try {
			CSRF.validate(req);
			transferId = Https.getInt(req, "transferId", R.I);
			Integer signId = Https.getInt(req, "signId", R.REQUIRED, R.I, "签收人名称");
			assetId = Https.getInt(req, "assetId",R.I);
			String remark = Https.getStr(req, "remark", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "特殊说明");
			String storagePlace = Https.getStr(req, "storagePlace", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "使用地点");
			AssetTransfer assetTransfer = null;
			DateTime now = new DateTime();
			
			if (transferId != null) {
				assetTransfer = mapper.fetch(AssetTransfer.class, "AssetTransfer.query", Cnd
						.where("t.transfer_id", "=" ,transferId));
				Asserts.isNull(assetTransfer, "资产移交不存在");
				Asserts.notEqOr(assetTransfer.getApprove(), Status.APPROVED,Status.OK, "禁止修改已确认的资产移交");
			} else {
				assetTransfer = new AssetTransfer();
				assetTransfer.setCreateTime(now.toDate());
			}
			
			Asset asset = dao.fetch(Asset.class, assetId);
			User sign = dao.fetch(User.class, signId);
			
			StringBuilder buff = new StringBuilder();
			buff.append("资产移交_")
				.append(Context.getTrueName())
				.append("将")
				.append(asset.getAssetName())
				.append("移交至")
				.append(sign.getTrueName());
			
			assetTransfer.setModifyTime(now.toDate());
			assetTransfer.setAssetId(assetId);
			assetTransfer.setTransferPersonId(Context.getUserId());
			assetTransfer.setSignId(signId);
			assetTransfer.setRemark(remark);
			assetTransfer.setApprove(Status.PROOFING);
			assetTransfer.setSubject(buff.toString());
			assetTransfer.setStoragePlace(storagePlace);
			
			save(assetTransfer, transferId);
			
			Code.ok(mb, (transferId == null ? "新建" : "编辑") + "资产移交申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (transferId == null ? "新建" : "编辑") + "资产移交申请失败");
		}
		return mb;
	}
	
	public void  save(AssetTransfer assetTransfer, Integer transferId){
		if(transferId != null)
			
			dao.update(assetTransfer);
		
		else 
			dao.insert(assetTransfer);
	
	}
}
