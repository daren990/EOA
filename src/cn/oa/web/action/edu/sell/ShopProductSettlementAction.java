package cn.oa.web.action.edu.sell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.OrgType;
import cn.oa.model.Org;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopOrder;
import cn.oa.model.ShopProduct;
import cn.oa.model.ShopProductSettlement;
import cn.oa.model.vo.ProductSettlementAddViewVO;
import cn.oa.model.vo.ShopOrderUnsubscribeVO;
import cn.oa.model.vo.ShopProductSettlementCountVO;
import cn.oa.model.vo.ShopProductSettlementLinePageVO;
import cn.oa.model.vo.ShopProductSettlementTargetVO;
import cn.oa.model.vo.ShopProductSettlementVO;
import cn.oa.model.vo.WxShopOrderVO;
import cn.oa.utils.MapBean;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At("/edu/sell/shopProductSettlement")
public class ShopProductSettlementAction extends Action
{
	
	public static Log log = Logs.getLog(ShopProductSettlementAction.class);

	@GET
	@POST
	@At
	@Ok("ftl:edu/sell/settlement/shopProductSettlement_page")
	public void page(HttpServletRequest req) throws Exception {
		SimpleCriteria cri = Cnd.cri();
		cri.desc("shop_product_id").desc("createTime");
		
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);//公司id
		String productName = Https.getStr(req, "productName");
		String operatorName = Https.getStr(req, "operatorName");
		Integer settlementType = Https.getInt(req, "settlementType");
		
		List<Org> corps = new ArrayList<Org>();
		Org o = dao.fetch(Org.class,Context.getCorpId());
		if(o.getType() == OrgType.TRAINING){
			corpId = Context.getCorpId();	
		}else{
			corps = studentService.selectCorpsAll();
		}
		
		//用来回显
		MapBean mb = new MapBean();
		if(productName!=null && !productName.equals("")){
			mb.put("productName",productName);
			cri.where().and(new Static(" shop_product_id in (select id from shop_product where name like '%"+productName+"%') "));
		}
		if(operatorName!=null && !operatorName.equals("")){
			mb.put("operatorName",operatorName);
			cri.where().and(new Static(" operatorId in (select user_id from sec_user where username like '%"+operatorName+"%')"));
		}
		if(settlementType != null){
			mb.put("settlementType",settlementType);
			cri.where().and("type","=",settlementType);
		}
		if(corpId != null && corpId > 0){
			mb.put("corpId",corpId);
			cri.where().and("crop_id","=",corpId);
		}
		
		Page<ShopProductSettlement> page = Webs.page(req);
		page = mapper.page(ShopProductSettlement.class, page, "shop_product_settlement.count", "shop_product_settlement.index",cri);
		List<ShopProductSettlementVO> voList = orderService.getShopProductSettlementVO(page);
		//ShopProductSettlementLinePageVO vo = orderService.getShopProductSettlementLinePageVO(page);
		
		req.setAttribute("page", page);
		//req.setAttribute("vo", vo);
		req.setAttribute("voList", voList);
		req.setAttribute("mb", mb);
		req.setAttribute("corps", corps);
	}
	
	@GET
	@POST
	@At
	@Ok("json")
	public Object settlement(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		Integer id = Https.getInt(req, "id");
		if(!orderService.isFinishCourse(id)){
			Code.error(mb, "有课程尚未完结");
		}else{
			productSettlementService.productSettlement(id);
			Code.ok(mb, "操作成功");
		}
		return mb;
	}
	
	@GET
	@POST
	@At
	@Ok("ftl:edu/sell/settlement/shopProductSettlement_addView")
	public void addView(HttpServletRequest req) throws Exception {
		Integer id = Https.getInt(req, "id");
		ProductSettlementAddViewVO vo = productSettlementService.getAddViewVO(id);
		req.setAttribute("vo", vo);
	}
	
	@GET
	@POST
	@At
	@Ok("json")
	public Object addSettlement(HttpServletRequest req) throws Exception {
		
		MapBean mb = new MapBean();
		
		try {
			ShopProductSettlementVO vo = new ShopProductSettlementVO();
			vo.setShop_product_id(Https.getInt(req, "shop_product_id"));
			vo.setExpenditure(Https.getFloat(req, "expenditure"));
			vo.setOperatorId(Https.getInt(req, "operatorId"));
			vo.setTimes(Https.getInt(req, "times"));
			vo.setUnsubscribe(Https.getFloat(req, "unsubscribe"));
			vo.setAmount(Https.getFloat(req, "amount"));
			vo.setCrop_id(Https.getInt(req, "corp_id"));
			vo.setOrderids(Https.getStr(req, "orderids"));
			vo.setType(Https.getInt(req, "type"));
			
			Integer tsize = Https.getInt(req, "tsize");
			
			List<ShopProductSettlementTargetVO> targetList = new ArrayList<ShopProductSettlementTargetVO>();
			for (int i = 1; i <= tsize; i++) {
				
				String tcoopType = Https.getStr(req, "tcoopType_"+i);
				if(!tcoopType.equals("自营")){
					ShopProductSettlementTargetVO t = new ShopProductSettlementTargetVO();
					
					float tamount = Https.getFloat(req, "tamount_"+i);
					float tlastBalance = Https.getFloat(req, "tlastBalance_"+i);
					float tsj = Https.getFloat(req, "tsj_"+i);
					
					t.setAmount(tsj);
					t.setLastBalance((tamount + tlastBalance) - tsj);
					t.setTargerId(Https.getInt(req, "targetid_"+i));
					t.setType(Https.getInt(req, "ttype_"+i));
					t.setShop_product_id(Https.getInt(req, "spid_"+i));
					t.setShop_goods_id(Https.getInt(req, "sgid_"+i));
					targetList.add(t);
				}
			}
			
			productSettlementService.InsertSettlement(vo, targetList);
			
			Code.ok(mb, "操作成功");
			
		} catch (Exception e) {
			Code.error(mb,"服务器错误");
			e.printStackTrace();
		}
		
		return mb;
	}
	
	@GET
	@POST
	@At
	@Ok("ftl:edu/sell/settlement/shopProductSettlement_view")
	public void view(HttpServletRequest req) throws Exception {
		Integer id = Https.getInt(req, "id");
		ShopProductSettlementVO vo = orderService.getShopProductSettlementVO(dao.fetch(ShopProductSettlement.class,id));
		
		
		List<ShopOrder> oList = new ArrayList<ShopOrder>();
		List<ShopOrderUnsubscribeVO> uvoList = new ArrayList<ShopOrderUnsubscribeVO>();
		if(!vo.getOrderids().equals("")){
			oList = dao.query(ShopOrder.class, Cnd.where(new Static(" id in ("+vo.getOrderids()+") ")));
			uvoList = orderService.getUnsubsribeVOByOrderids(vo.getOrderids());
		}
		List<WxShopOrderVO> ovoList = wxClientService.getShopOrderVO(oList);
		
		List<ShopProductSettlementTargetVO> targetList = productSettlementService.getShopProductSettlementTargetVO(id);
		
		
		req.setAttribute("vo", vo);
		req.setAttribute("ovoList", ovoList);
		req.setAttribute("targetList", targetList);
		req.setAttribute("uvoList", uvoList);
	}
	
	@GET
	@POST
	@At
	@Ok("ftl:edu/sell/settlement/shopProductSettlement_product")
	public void product(HttpServletRequest req) throws Exception {
		Integer id = Https.getInt(req, "id");
		ShopProductSettlementCountVO vo = productSettlementService.getShopProductSettlementCountVO(id);
		req.setAttribute("vo", vo);
	}
}
