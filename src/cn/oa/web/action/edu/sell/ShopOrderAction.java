
package cn.oa.web.action.edu.sell;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import cn.oa.model.ShopOrder;
import cn.oa.model.ShopOrderHandleLog;
import cn.oa.model.ShopOrderUnsubscribe;
import cn.oa.model.ShopProduct;
import cn.oa.model.vo.ShopOrderHandleLogVO;
import cn.oa.model.vo.ShopOrderUnsubscribeVO;
import cn.oa.model.vo.WxShopOrderVO;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At("/edu/sell/order")
public class ShopOrderAction extends Action
{
	
	public static Log log = Logs.getLog(ShopOrderAction.class);

	@GET
	@POST
	@At
	@Ok("ftl:edu/sell/order/order_page")
	public void page(HttpServletRequest req) throws Exception {
		SimpleCriteria cri = Cnd.cri();
		cri.desc("id");
		cri.where().and(new Static(" order_status = 1 "));
		
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);//公司id
		String clientName = Https.getStr(req, "clientName");
		String studentName = Https.getStr(req, "studentName");
		String productName = Https.getStr(req, "productName");
		
		List<Org> corps = new ArrayList<Org>();
		Org o = dao.fetch(Org.class,Context.getCorpId());
		if(o.getType() == OrgType.TRAINING){
			corpId = Context.getCorpId();	
		}else{
			corps = studentService.selectCorpsAll();
		}
		
		Integer flag2 = dao.fetch(Org.class,Cnd.where("org_id","=",Context.getCorpId())).getParentId();
		
		//用来回显
		MapBean mb = new MapBean();
		
		if(clientName!=null && !clientName.equals("")){
			mb.put("clientName",clientName);
			cri.where().and(new Static(" shop_client_id in (select id from shop_client where truename like '%"+clientName+"%') "));
		}
		if(studentName!=null && !studentName.equals("")){
			mb.put("studentName",studentName);
			cri.where().and(new Static(" edu_student_id in (select id from edu_student where name like '%"+studentName+"%') "));
		}
		if(productName!=null && !productName.equals("")){
			mb.put("productName",productName);
			cri.where().and(new Static(" shop_product_id in (select id from shop_product where name like '%"+productName+"%') "));
		}
		if(corpId != null && corpId > 0){
			mb.put("corpId",corpId);
			cri.where().and("crop_id","=",corpId);
		}
		
		Page<ShopOrder> page = Webs.page(req);
		page = mapper.page(ShopOrder.class, page, "ShopOrder.count", "ShopOrder.index",cri);
		List<WxShopOrderVO> voList = wxClientService.getShopOrderVO(page);
		req.setAttribute("page", page);
		req.setAttribute("voList", voList);
		req.setAttribute("mb", mb);
		req.setAttribute("corps", corps);
		req.setAttribute("flag2", flag2);
		req.setAttribute("unsubscribeOrder_btn", resourceRepository.getBtnAuthority("unsubscribeOrder_btn", "退订按钮"));
		req.setAttribute("addOrder_btn", resourceRepository.getBtnAuthority("addOrder_btn", "报名按钮"));
	}
	
	@POST
	@At
	@Ok("json")
	public Object deleteOrder(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		try {
			Integer id = Https.getInt(req, "id");
			orderService.OrderDelete(id);
			Code.ok(mb, "操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object addOrder(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		try {
			Integer org_id = Context.getCorpId();
			String client_Name = Https.getStr(req, "client_Name");
			String client_Phone = Https.getStr(req, "client_Phone");
			String stu_Name = Https.getStr(req, "stu_Name");
			Integer isOne = Https.getInt(req, "isOne");
			Integer isBookPrice = Https.getInt(req, "isBookPrice");
			Integer shopProduct_id = Https.getInt(req, "shopProduct_id");
			if(orderService.isFinishCourse(shopProduct_id)){
				Code.error(mb, "有课程已经完结");
			}else{
				orderService.OrderAdd(org_id, stu_Name, shopProduct_id, client_Name, client_Phone, isOne, isBookPrice, mb);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object getShopProductVo(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		try {
			Integer productId = Https.getInt(req, "productId");
			ShopProduct sp = dao.fetch(ShopProduct.class,productId);
			System.out.println("\n\n\n\n\n" + sp.getDetail() + "\n\n\n\n\n");
			mb.put("spvo", wxClientService.getShopProductVo(sp));
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:edu/sell/order/order_addView")
	public void order_addView(HttpServletRequest req) throws Exception {
		List<ShopProduct> nodes = new ArrayList<ShopProduct>();
		Org o = dao.fetch(Org.class,Context.getCorpId());
		if(o.getType() == OrgType.TRAINING){
			nodes = dao.query(ShopProduct.class, Cnd.where("corp_id", "=", Context.getCorpId()).and("onSale","=",1));
		}else{
			nodes = dao.query(ShopProduct.class, Cnd.where("1", "=", 1).and("onSale","=",1));
		}
		req.setAttribute("nodes", nodes);
	}
	
	@GET
	@At
	@Ok("ftl:edu/sell/order/order_unsubscribeView")
	public void order_unsubscribeView(HttpServletRequest req) throws Exception {
		CSRF.generate(req);
		Integer id = Https.getInt(req, "id");
		ShopOrder so = dao.fetch(ShopOrder.class, id);
		WxShopOrderVO sovo = wxClientService.getShopOrderVO(so);
		req.setAttribute("sovo", sovo);
	}
	
	@POST
	@At
	@Ok("json")
	public Object unsubscribeOrder(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			orderService.unsubscribeOrder(Https.getFloat(req, "amount"),
					Context.getUserId(), Https.getStr(req, "remark"), 
					Https.getInt(req, "shop_order_id"), Https.getInt(req, "unType"),Https.getInt(req, "isAll"));
			
			Code.ok(mb, "操作成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Subject:add) error: ", e);
			Code.error(mb, "操作失败");
		}

		return mb;
	}
	
	@GET
	@POST
	@At
	@Ok("ftl:edu/sell/order/orderUnsubscribe_page")
	public void orderUnsubscribe_page(HttpServletRequest req) throws Exception {
		SimpleCriteria cri = Cnd.cri();
		cri.desc("id");
		
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);//公司id
		String clientName = Https.getStr(req, "clientName");
		String productName = Https.getStr(req, "productName");
		String operatorName = Https.getStr(req, "operatorName");
		Integer unsubscribeType = Https.getInt(req, "unsubscribeType");
		Integer isAll = Https.getInt(req, "isAll");
		
		List<Org> corps = new ArrayList<Org>();
		Org o = dao.fetch(Org.class,Context.getCorpId());
		if(o.getType() == OrgType.TRAINING){
			corpId = Context.getCorpId();	
		}else{
			corps = studentService.selectCorpsAll();
		}
		
		//用来回显
		MapBean mb = new MapBean();
		
		if(clientName!=null && !clientName.equals("")){
			mb.put("clientName",clientName);
			cri.where().and(new Static(" shop_order_id in (select id from shop_order where shop_client_id in (select id from shop_client where truename like '%"+clientName+"%')) "));
		}
		if(productName!=null && !productName.equals("")){
			mb.put("productName",productName);
			cri.where().and(new Static(" shop_order_id in (select id from shop_order where shop_product_id in (select id from shop_product where name like '%"+productName+"%')) "));
		}
		if(operatorName!=null && !operatorName.equals("")){
			mb.put("operatorName",operatorName);
			cri.where().and(new Static(" operatorId in (select user_id from sec_user where username like '%"+operatorName+"%')"));
		}
		if(unsubscribeType != null && unsubscribeType >-1){
			mb.put("unsubscribeType",unsubscribeType);
			cri.where().and("unsubscribeType","=",unsubscribeType);
		}
		if(isAll != null && isAll >-1){
			mb.put("isAll",isAll);
			cri.where().and("isAll","=",isAll);
		}
		if(corpId != null && corpId > 0){
			mb.put("corpId",corpId);
			cri.where().and("crop_id","=",corpId);
		}
		
		Page<ShopOrderUnsubscribe> page = Webs.page(req);
		page = mapper.page(ShopOrderUnsubscribe.class, page, "shop_order_unsubscribe.count", "shop_order_unsubscribe.index",cri);
		List<ShopOrderUnsubscribeVO> voList = orderService.getShopOrderUnsubscribeVO(page);
		req.setAttribute("page", page);
		req.setAttribute("voList", voList);
		req.setAttribute("mb", mb);
		req.setAttribute("corps", corps);
	}
	
	@GET
	@POST
	@At
	@Ok("ftl:edu/sell/order/orderHandleLog_page")
	public void orderHandleLog_page(HttpServletRequest req) throws Exception {
		SimpleCriteria cri = Cnd.cri();
		cri.desc("id");
		
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);//公司id
		String clientName = Https.getStr(req, "clientName");
		String productName = Https.getStr(req, "productName");
		String operatorName = Https.getStr(req, "operatorName");
		Integer handleType = Https.getInt(req, "handleType");
		
		List<Org> corps = new ArrayList<Org>();
		Org o = dao.fetch(Org.class,Context.getCorpId());
		if(o.getType() == OrgType.TRAINING){
			corpId = Context.getCorpId();	
		}else{
			corps = studentService.selectCorpsAll();
		}
		
		//用来回显
		MapBean mb = new MapBean();
		
		if(clientName!=null && !clientName.equals("")){
			mb.put("clientName",clientName);
			cri.where().and(new Static(" shop_order_id in (select id from shop_order where shop_client_id in (select id from shop_client where truename like '%"+clientName+"%')) "));
		}
		if(productName!=null && !productName.equals("")){
			mb.put("productName",productName);
			cri.where().and(new Static(" shop_order_id in (select id from shop_order where shop_product_id in (select id from shop_product where name like '%"+productName+"%')) "));
		}
		if(operatorName!=null && !operatorName.equals("")){
			mb.put("operatorName",operatorName);
			cri.where().and(new Static(" operatorId in (select user_id from sec_user where username like '%"+operatorName+"%')"));
		}
		if(handleType != null && handleType >-1){
			mb.put("handleType",handleType);
			cri.where().and("handleType","=",handleType);
		}
		if(corpId != null && corpId > 0){
			mb.put("corpId",corpId);
			cri.where().and("crop_id","=",corpId);
		}
		
		Page<ShopOrderHandleLog> page = Webs.page(req);
		page = mapper.page(ShopOrderHandleLog.class, page, "shop_order_handle_log.count", "shop_order_handle_log.index",cri);
		List<ShopOrderHandleLogVO> voList = orderService.getShopOrderHandleLogVO(page);
		req.setAttribute("page", page);
		req.setAttribute("voList", voList);
		req.setAttribute("mb", mb);
		req.setAttribute("corps", corps);
	}
	
}
