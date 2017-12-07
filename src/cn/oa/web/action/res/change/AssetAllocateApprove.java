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
import cn.oa.model.Asset;
import cn.oa.model.AssetAllocate;
import cn.oa.model.AssetAllocateActor;
import cn.oa.model.AssetTransfer;
import cn.oa.model.Borrow;
import cn.oa.model.BorrowActor;
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

@IocBean(name = "res.change.assetallocateApprove")
@At(value = "/res/change/assetallocateApprove")
public class AssetAllocateApprove extends Action{
	
	public static Log log = Logs.getLog(AssetAllocateApprove.class);
	
	@GET
	@At
	@Ok("ftl:res/change/assetallocateApprove_page")
	public void page(HttpServletRequest req) {
		
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/change/assetallocateApprove/actors", token);
		CSRF.generate(req, "/res/change/assetallocateApprove/able", token);
	
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
	
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("raa.actor_id", "=", Context.getUserId())
			.andNotEquals("raa.approve", Status.CHECKING);
		Cnds.gte(cri, mb, "ra.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "ra.create_time", "endTime", endStr);
		Cnds.eq(cri, mb, "raa.approve", "approve", approve);
		cri.getOrderBy().desc("ra.modify_time");
	
		Page<AssetAllocate> page = Webs.page(req);
		page = mapper.page(AssetAllocate.class, page, "AssetAllocateApprove.count", "AssetAllocateApprove.index", cri);

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
			Integer allocateId = Https.getInt(req, "allocateId", R.REQUIRED, R.I, "资产调配ID");
			List<AssetAllocateActor> actors = mapper.query(AssetAllocateActor.class, "AssetAllocateActor.query", Cnd
					.where("raa.allocate_id", "=", allocateId)
					.asc("raa.modify_time"));
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
	@Ok("ftl:res/change/assetallocateApprove_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Asset asset = null;
		AssetAllocateActor admActor = null;
		AssetAllocateActor outActor = null;
		Integer allocateId = Https.getInt(req, "allocateId", R.REQUIRED, R.I, "资产调配申请ID");
		AssetAllocate assetAllocate = mapper.fetch(AssetAllocate.class, "AssetAllocateApprove.query", Cnd
				.where("raa.actor_id", "=", Context.getUserId())
				.and("ra.allocate_id", "=", allocateId));
		Asserts.isNull(allocateId, "资产调配不存在");
		if(assetAllocate != null){
			String trueName =mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", assetAllocate.getAllocatePersonId())).getTrueName();
			asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=", assetAllocate.getAssetId()));
			assetAllocate.setAssetName(asset.getAssetName());
			assetAllocate.setAssetNumber(asset.getAssetNumber());
			assetAllocate.setItem(asset.getTypeId());
			assetAllocate.setAssetId(asset.getAssetId());
			assetAllocate.setAllocateTrueName(trueName);
		}
		List<AssetAllocateActor> actors = mapper.query(AssetAllocateActor.class, "AssetAllocateActor.query", Cnd
				.where("raa.allocate_id", "=", allocateId)
				.asc("raa.modify_time"));
		
		AssetAllocateActor actor = null;
		
		for(AssetAllocateActor e : actors){
			
			if (e.getActorId().equals(Context.getUserId())) actor = e;
			
			if(e.getVeriable().equals(Status.PERSONADM))
				admActor = e;
			
			if(e.getVeriable().equals(Status.PERSONOUT))
				outActor = e;
		}
		if(admActor == null)
			admActor = new AssetAllocateActor();
		if(outActor ==null)
			outActor = new AssetAllocateActor();
		
		req.setAttribute("admActor", admActor);
		req.setAttribute("outActor", outActor);
		
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
		req.setAttribute("actor", actor);
		req.setAttribute("assetAllocate", assetAllocate);
		req.setAttribute("asset", asset);
	}
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer allocateId = Https.getInt(req, "allocateId", R.REQUIRED, R.I, "移交ID");
			Integer approved = Https.getInt(req, "approved", R.REQUIRED, R.I, R.IN, "in [1,-1]", "确认状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");

			 AssetAllocate assetAllocate = mapper.fetch(AssetAllocate.class, "AssetAllocateApprove.query", Cnd
						.where("raa.actor_id", "=", Context.getUserId())
						.and("ra.allocate_id", "=", allocateId));
			Asserts.isNull(assetAllocate, "申请不存在");
			Asserts.notEqOr(assetAllocate.getApproved(), Status.OK,Status.UNAPPROVED, "严禁修改已确认的资产移交");
			
			List<AssetAllocateActor> actors = mapper.query(AssetAllocateActor.class, "AssetAllocateActor.query", Cnd
					.where("raa.allocate_id", "=", allocateId)
					.asc("raa.modify_time"));
			
		//	AssetAllocateActor actor = null; // 当前审批人员
			AssetAllocateActor admactor = null; //行政审批人员 
			AssetAllocateActor outactor = null; //移出审批人员 
			AssetAllocateActor signactor = null; //签收审批人员
			String actorVeriable = null;   //当前审批人的变量
			DateTime now = new DateTime();
			
			for (AssetAllocateActor e : actors) {
				
				if(Context.getUserId().equals(e.getActorId())){
					actorVeriable = e.getVeriable();
					e.setModifyTime(now.toDate());
					e.setOpinion(opinion);
					e.setApprove(approved);
					dao.update(e);
				}
					
				if (e.getVeriable().equals(Status.PERSONADM)) admactor = e;
				
				else if(e.getVeriable().equals(Status.PERSONOUT)) outactor = e;
				
				else if(e.getVeriable().equals(Status.PERSONSIGN)) signactor = e;
				
			}
			
			save(assetAllocate, now, approved, admactor, outactor, signactor, actorVeriable);

			Code.ok(mb, "资产调配确认成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "资产调配确认失败");
		}

		return mb;
	}
	
	public void save(final AssetAllocate assetAllocate,final DateTime now, final Integer approved, final AssetAllocateActor admactor,final AssetAllocateActor outactor,final AssetAllocateActor signactor, final String actorVeriable){
		Trans.exec(new Atom(){

			@Override
			public void run() {
				
				//汇签：当行政部和移出部门其中一个同意时，判断另外一个是否同意，如果两个都同意，下一步为签收人
				if(approved.equals(Status.APPROVED)&&!(actorVeriable.equals(Status.PERSONSIGN))){
						
						if(actorVeriable.equals(Status.PERSONADM)){
							
							if(outactor.getApprove().equals(Status.APPROVED)){
								signactor.setModifyTime(now.toDate());
								signactor.setApprove(Status.PROOFING);
								dao.update(signactor);
							}
						}
						
						else if(actorVeriable.equals(Status.PERSONOUT)){
							
							if(admactor.getApprove().equals(Status.APPROVED)){
								signactor.setModifyTime(now.toDate());
								signactor.setApprove(Status.PROOFING);
								dao.update(signactor);
						}
					}
				
				}
				//当行政部和移出部门审批不同意，取消下一级流程
				else if(approved.equals(Status.UNAPPROVED)&&!(actorVeriable.equals(Status.PERSONSIGN))){
					
					if(signactor.getApprove().equals(Status.PROOFING)){
						signactor.setApprove(Status.CHECKING);
						assetAllocate.setApproved(Status.UNAPPROVED);
						dao.update(signactor);
					}
						
					
				}
				
				else if(actorVeriable.equals(Status.PERSONSIGN)){
					
					//如果当前审批人是签收人且审批同意就更新资产表，调配表
					if(approved.equals(Status.APPROVED)){
						assetAllocate.setModifyTime(now.toDate());
						assetAllocate.setAllocateTime(now.toDate());
						assetAllocate.setApproved(Status.OK);
						dao.update(assetAllocate);
						Asset asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=", assetAllocate.getAssetId()));
						asset.setModifyTime(now.toDate());
						asset.setStoragePlace(assetAllocate.getStoragePlace());
						asset.setUserId(assetAllocate.getNowUserId());
						dao.update(asset);
						
						
					//如果不同意，更新调配表	
					}else{
						assetAllocate.setModifyTime(now.toDate());
						assetAllocate.setAllocateTime(now.toDate());
						assetAllocate.setApproved(approved);
						dao.update(assetAllocate);
					}
				}
			} 
		});

	}
}
