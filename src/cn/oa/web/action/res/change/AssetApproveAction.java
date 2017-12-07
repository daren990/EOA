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
import cn.oa.model.Asset;
import cn.oa.model.AssetTransfer;
import cn.oa.model.Dict;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
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


@IocBean(name = "res.change.assettransferApprove")
@At(value = "/res/change/assettransferApprove")
public class AssetApproveAction extends Action {
	
	public static Log log = Logs.getLog(AssetApproveAction.class);
	
	@GET
	@At
	@Ok("ftl:res/change/assettransferApprove_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/change/assettransferApprove/able", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("t.sign_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "t.approve", "approve", approve);
		Cnds.gte(cri, mb, "t.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "t.create_time", "endTime", endStr);
		cri.getOrderBy().desc("t.modify_time");

		Page<AssetTransfer> page = Webs.page(req);
		page = mapper.page(AssetTransfer.class, page, "AssetTransfer.count", "AssetTransfer.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:res/change/assettransferApprove_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer transferId = Https.getInt(req, "transferId", R.REQUIRED, R.I, "申请ID");
		Asset asset = null;
		
		 AssetTransfer assetTransfer = mapper.fetch(AssetTransfer.class, "AssetTransfer.query", Cnd
				.where("t.sign_id", "=", Context.getUserId())
				.and("t.transfer_id", "=", transferId));
		Asserts.isNull(assetTransfer, "资产移交不存在");
		
		String trueName =mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", assetTransfer.getTransferPersonId())).getTrueName();
		
		if(assetTransfer != null){
			asset = dao.fetch(Asset.class, assetTransfer.getAssetId());
			assetTransfer.setAssetName(asset.getAssetName());
			assetTransfer.setAssetNumber(asset.getAssetNumber());
			assetTransfer.setItem(asset.getTypeId());
			assetTransfer.setAssetId(asset.getAssetId());
			assetTransfer.setMoveTrueName(trueName);
			
		}
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
		req.setAttribute("assetTransfer", assetTransfer);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer transferId = Https.getInt(req, "transferId", R.REQUIRED, R.I, "移交ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "确认状态");

			 AssetTransfer assetTransfer = mapper.fetch(AssetTransfer.class, "AssetTransfer.query", Cnd
						.where("t.sign_id", "=", Context.getUserId())
						.and("t.transfer_id", "=", transferId));
			Asserts.isNull(assetTransfer, "申请不存在");
			Asserts.notEqOr(assetTransfer.getApprove(), Status.APPROVED,Status.UNAPPROVED, "严禁修改已确认的资产移交");
			
			DateTime now = new DateTime();
			
			assetTransfer.setApprove(approve);
			assetTransfer.setModifyTime(now.toDate());
			if(approve.equals(Status.APPROVED))
				assetTransfer.setTransferTime(now.toDate());

			save(assetTransfer, now, approve);

			Code.ok(mb, "资产移交确认成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "资产移交确认失败");
		}

		return mb;
		
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
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "确认状态");

			if (arr != null && arr.length > 0) {
				DateTime now = new DateTime();
				List<AssetTransfer> assetTransfers = mapper.query(AssetTransfer.class, "AssetTransfer.query", Cnd
						.where("t.sign_id", "=", Context.getUserId())
						.and("t.approve", "=", Status.PROOFING)
						.and("t.transfer_id", "in", arr));
				for (AssetTransfer transfer : assetTransfers) {
					
					transfer.setApprove(approve);
					transfer.setModifyTime(now.toDate());
					
					if(approve.equals(Status.APPROVED))
						transfer.setTransferTime(now.toDate());

					save(transfer, now, approve);
				}
			}
			Code.ok(mb, "资产移交成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:able) error: ", e);
			Code.error(mb, "资产移交失败");
		}

		return mb;
	}
	
	private void save(AssetTransfer assetTransfer, DateTime now, Integer approve){
			dao.update(assetTransfer);
			
			if(approve.equals(Status.APPROVED)){
				
				Asset asset = dao.fetch(Asset.class, assetTransfer.getAssetId());
				asset.setModifyTime(now.toDate());
				asset.setStoragePlace(assetTransfer.getStoragePlace());
				asset.setState(Status.USER);
				asset.setUserId(assetTransfer.getSignId());
				dao.update(asset);
				
			}
	}
}
