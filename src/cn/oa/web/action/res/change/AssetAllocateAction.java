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
import cn.oa.model.Dict;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
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

@IocBean(name = "res.change.assetallocate")
@At(value = "/res/change/assetallocate")
public class AssetAllocateAction extends Action{
	
	public static Log log = Logs.getLog(AssetAllocateAction.class);
	
	@GET
	@At
	@Ok("ftl:res/change/assetallocate_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/change/assetallocateApprove/actors", token);
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		String assetName = Https.getStr(req, "assetName", R.CLEAN);
		String assetNumber = Https.getStr(req, "assetNumber", R.CLEAN);
		Integer approve= Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.eq(cri, mb, "a.asset_name", "assetName", assetName);
		Cnds.eq(cri, mb, "a.asset_number", "assetNumber", assetNumber);
		Cnds.eq(cri, mb, "ra.approve", "approve", approve);
		Cnds.gte(cri, mb, "ra.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "ra.create_time", "endTime", endStr);
		cri.getOrderBy().desc("ra.create_time");
		
		
		
		Page<AssetAllocate> page = Webs.page(req);
		page = mapper.page(AssetAllocate.class, page, "AssetAllocate.count", "AssetAllocate.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:res/change/assetallocate_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer allocateId = Https.getInt(req, "allocateId", R.I);
		Integer assetId = Https.getInt(req, "assetId", R.I);
		AssetAllocate assetAllocate = null;
		Asset asset = null;
		String trueName = null;
		List<AssetAllocateActor> actors = null;
		AssetAllocateActor admActor = null;
		AssetAllocateActor outActor = null;
		if(allocateId != null){
			assetAllocate = mapper.fetch(AssetAllocate.class, "AssetAllocate.query", Cnd
					.where("ra.allocate_id", "=" ,allocateId));
		}
			
		if(assetAllocate != null){
			trueName = mapper.fetch(User.class, "User.query", Cnd.where("u.user_id", "=", assetAllocate.getAllocatePersonId())).getTrueName();
			asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=", assetAllocate.getAssetId()));
			actors = mapper.query(AssetAllocateActor.class, "AssetAllocateActor.query", Cnd.where("raa.allocate_id", "=", allocateId));
			for(AssetAllocateActor assetActor : actors){
				
				if(assetActor.getVeriable().equals(Status.PERSONADM))
					admActor = assetActor;
				
				if(assetActor.getVeriable().equals(Status.PERSONOUT))
					outActor = assetActor;
				}
			}
		
		else{
			assetAllocate = new AssetAllocate();
			trueName = Context.getTrueName();
		}
			
		if(assetId !=null){
			asset = mapper.fetch(Asset.class, "Asset.query", Cnd.where("a.asset_id", "=", assetId));
		}	
		
		
		assetAllocate.setAssetName(asset.getAssetName());
		assetAllocate.setAssetNumber(asset.getAssetNumber());
		assetAllocate.setItem(asset.getTypeId());
		assetAllocate.setAssetId(asset.getAssetId());
		assetAllocate.setAllocateTrueName(trueName);
		
		//签收人
		List<User> personsign = mapper.query(User.class, "User.query", Cnd.where("u.status", "=", Status.ENABLED));
		
		//移出部门审批人，除普通员工其他可审批
		User user = dao.fetch(User.class, asset.getUserId());
		List<User> personOut = userService.operators(new String[]{Roles.EMP.getName()}, user.getCorpId());
		
		//行政部审批 拥有行政主管，行政处理人，行政经理角色的人可审批
		List<User> personAdm = userService.operators(new String[]{Roles.SSVI.getName(), Roles.ADR.getName(), Roles.MADR.getName()});
		
		if(admActor == null)
			admActor = new AssetAllocateActor();
		if(outActor ==null)
			outActor = new AssetAllocateActor();
		
		req.setAttribute("admActor", admActor);
		req.setAttribute("outActor", outActor);
		req.setAttribute("personOut", personOut);
		req.setAttribute("personAdm", personAdm);
		req.setAttribute("personsign", personsign);
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
		req.setAttribute("assetAllocate", assetAllocate);
		req.setAttribute("asset", asset);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer allocateId = null;
		Integer assetId = null;
		try {
			CSRF.validate(req);
			allocateId = Https.getInt(req, "allocateId", R.I);
			Integer signId = Https.getInt(req, "signId", R.REQUIRED, R.I, "签收人名称");
			Integer admId = Https.getInt(req, "admId", R.REQUIRED, R.I, "行政部门审批");
			Integer outId = Https.getInt(req, "outId", R.REQUIRED, R.I, "移出部门审批");
			assetId = Https.getInt(req, "assetId",R.I);
			String remark = Https.getStr(req, "remark", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "特殊说明");
			String storagePlace = Https.getStr(req, "nowstoragePlace", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "使用地点");
			AssetAllocate assetAllocate = null;
			DateTime now = new DateTime();
			
			if (allocateId != null) {
				assetAllocate = mapper.fetch(AssetAllocate.class, "AssetAllocate.query", Cnd
						.where("ra.allocate_id", "=" ,allocateId));
				Asserts.isNull(assetAllocate, "资产调配不存在");
				Asserts.notEqOr(assetAllocate.getApproved(), Status.OK, "禁止修改已审批的资产调配");
			} else {
				assetAllocate = new AssetAllocate();
				assetAllocate.setCreateTime(now.toDate());
			}
			
			Asset asset = dao.fetch(Asset.class, assetId);
			User sign = dao.fetch(User.class, signId);
			
			StringBuilder buff = new StringBuilder();
			buff.append("资产调配_")
				.append(Context.getTrueName())
				.append("将")
				.append(asset.getAssetName())
				.append("调配至")
				.append(sign.getTrueName());
			
			assetAllocate.setModifyTime(now.toDate());
			assetAllocate.setAssetId(assetId);
			assetAllocate.setAllocatePersonId(Context.getUserId());
			assetAllocate.setUserId(asset.getUserId());
			assetAllocate.setNowUserId(signId);
			assetAllocate.setRemark(remark);
			assetAllocate.setSubject(buff.toString());
			assetAllocate.setApproved(Status.PROOFING);
			assetAllocate.setStoragePlace(storagePlace);
			
			transave(assetAllocate, allocateId, admId, outId, signId);
			
			Code.ok(mb, (allocateId == null ? "新建" : "编辑") + "资产调配申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (allocateId == null ? "新建" : "编辑") + "资产调配申请失败");
		}
		return mb;
		
	}
	
	public void transave (final AssetAllocate assetAllocate, final Integer allocateId, final Integer admId, final Integer outId, final Integer signId){
		Trans.exec(new Atom(){

			@Override
			public void run() {
				Integer id = allocateId;
				if(id != null){
					dao.clear(AssetAllocateActor.class, Cnd.where("allocateId", "=", allocateId));
					dao.update(assetAllocate);
				}else{
					id = dao.insert(assetAllocate).getAllocateId();
				}
				
				//移出部门领导审批
				dao.insert(new AssetAllocateActor(id, outId, assetAllocate.getModifyTime(), Status.value, Status.PROOFING, Status.PERSONOUT));
			
				//行政部审批
				dao.insert(new AssetAllocateActor(id, admId, assetAllocate.getModifyTime(), Status.value, Status.PROOFING, Status.PERSONADM));
			
				//	签收人审批
				dao.insert(new AssetAllocateActor(id, signId, assetAllocate.getModifyTime(), Status.value, Status.CHECKING, Status.PERSONSIGN));	
				
			}
			
		});
		
	}
}
