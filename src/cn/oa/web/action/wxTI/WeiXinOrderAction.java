
package cn.oa.web.action.wxTI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jdom.JDOMException;
import org.nutz.dao.Cnd;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.ClientType;
import cn.oa.model.EduStudent;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopMember;
import cn.oa.model.ShopOrder;
import cn.oa.model.ShopPayOrder;
import cn.oa.model.ShopProduct;
import cn.oa.model.vo.WxShopOrderVO;
import cn.oa.utils.MapBean;
import cn.oa.utils.WeixinUtil;
import cn.oa.utils.XMLUtil;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.PathUtil;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;
import cn.oa.web.action.wx.comm.QiyeWeixinUtil;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.view.FreemarkerView;
import cn.oa.web.wx.Config;
import cn.oa.web.wx.InterfacePath;
import cn.oa.web.wx.Merchant;

@Filters
@IocBean(name = "wx.order")
@At(value = "/wx/order")
public class WeiXinOrderAction extends Action {
	
	private static final Log log = Logs.getLog(WeiXinOrderAction.class);
	
	@POST
	@At
	@Ok("json")
	public Object order_add(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		try {
			Integer org_id = Https.getInt(req, "org_id");
			String openid = Webs.getOpenidInSession(req, org_id.toString());
			String stu_Name = Https.getStr(req, "stu_Name");
			Integer shopProduct_id = Https.getInt(req, "shopProduct_id");
			Integer member_id = Https.getInt(req, "member_id");
			Integer member_level = Https.getInt(req, "member_level") == null ? 0 : Https.getInt(req, "member_level");
			if(orderService.isFinishCourse(shopProduct_id)){
				Code.error(mb, "有课程已经完结");
			}else{
				wxClientService.wx_OrderAdd(org_id,stu_Name, openid, shopProduct_id,member_id,member_level,mb);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
	@GET
	@At
	public View order_addView(HttpServletRequest req,Integer shopProduct_id, String org_id) throws Exception {
		Config c = wxClientService.initOrgConfig(org_id);
		wxClientService.setSignature(req,c);
		req.setAttribute("appId", c.getAPPID());
		req.setAttribute("org_id", org_id);
		ShopProduct sp = dao.fetch(ShopProduct.class,shopProduct_id);
		req.setAttribute("spvo", wxClientService.getShopProductVo(sp));
		ShopMember sm = wxClientService.getByClientId(Webs.getSCInSession(req).getId().toString());
		if(sm!=null){
			req.setAttribute("sm", sm);
		}
		return new FreemarkerView("wxTI/wechat_orderAdd");
	}
	
	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View list(HttpServletRequest req, String org_id,String code,String openid) throws Exception {
		req.setAttribute("org_id", org_id);
		return new FreemarkerView("wxTI/wechat_orderList");
	}
	
	@GET
	@POST
	@At
	@Ok("json")
	public Object getData(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		try {
			SimpleCriteria cri = Cnd.cri();
			cri.desc("id");
			cri.where().and(new Static(" order_status = 1 "));
			
			Integer orgId = Https.getInt(req, "org_id", R.REQUIRED, R.I, "机构id");
			cri.where().and("crop_id","=",orgId);
			cri.where().and("shop_client_id","=",Webs.getSCInSession(req).getId());
			
			String seacherKey = Https.getStr(req, "seacherKey");
			if(seacherKey!=null && !seacherKey.equals("")){
				cri.where().and(new Static(" (edu_student_id in (select id from edu_student where name like '%"+seacherKey+"%') or "
						+ "shop_product_id in (select id from shop_product where name like '%"+seacherKey+"%')) "));
			}
			
			Page<ShopOrder> page = Webs.page_jm(req);
			page = mapper.page(ShopOrder.class, page, "ShopOrder.count", "ShopOrder.index",cri);
			List<WxShopOrderVO> voList = wxClientService.getShopOrderVO(page);
			
			mb.put("voList", voList);
			Code.ok(mb, "");
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View order_payView(HttpServletRequest req, String org_id,String orderid) throws Exception {
		String basePath = PathUtil.getBasePath(req);
		Config c = wxClientService.initOrgConfig(org_id);
		wxClientService.setSignature(req,c);
		List<WxShopOrderVO> voList = wxClientService.getShopOrderVO(orderid);
		req.setAttribute("voList", voList);
		req.setAttribute("appId", c.getAPPID());
		req.setAttribute("org_id", org_id);
		req.setAttribute("basePath", basePath);
		return new FreemarkerView("wxTI/wechat_orderPay");
	}

	@GET
	@At
	@Ok("json")
	public Object prePay(HttpServletRequest req,String org_id,Integer orderId) throws Exception {
		MapBean mb = new MapBean();
		try {
			org_id = Https.getStr(req, "org_id");
			orderId = Https.getInt(req, "orderId");
			Config c = wxClientService.initOrgConfig(org_id);
			Merchant m = wxClientService.initOrgMerchant(org_id);
			
			String openid = Webs.getOpenidInSession(req, org_id);
			ShopClient sc = Webs.getSCInSession(req);
			
			TreeMap<String, Object> params = new TreeMap<String, Object>();
	        SortedMap<String, Object> parameters = new TreeMap<String, Object>();
	        String appId = c.getAPPID();
	        String mchId = m.getMERCHANTID();
	        String nonceStr = WeixinUtil.CreateNoncestr();
	        String payId = System.currentTimeMillis() + "";
	        ShopPayOrder payOrder = new ShopPayOrder();
	        ShopOrder order = dao.fetch(ShopOrder.class,orderId);
	        order.setPay_id(payId);
	        order.setPayShopClientId(sc.getId());
	        dao.update(order);
	        parameters.put("appid", appId);
	        parameters.put("mch_id", mchId);
	        parameters.put("nonce_str", nonceStr);
	        parameters.put("body", "购买课程");
	        parameters.put("out_trade_no", payId);
	        payOrder.setPay_id(payId);
	        payOrder.setOrder_id(orderId);
	        payOrder.setPer_pay_time(new Date());
	        parameters.put("total_fee", (int)(order.getAmount() * 100) + "");
	        payOrder.setAmount(order.getAmount());
	        payOrder.setStatus(0);
	        String ipAddr = getIpAddr(req);
	        if (StringUtils.isNotBlank(ipAddr) && ipAddr.indexOf(",") != -1) {
	            ipAddr = ipAddr.split(",")[0];
	        }
	        parameters.put("spbill_create_ip", ipAddr);
	        parameters.put("notify_url", PathUtil.getBasePath(req)+"/wx/order/payCallBack");
	        parameters.put("trade_type", InterfacePath.JSAPI);
	        parameters.put("openid", openid);
	        String mchKey = m.getMERCHANTSECRET();// EnvConfig.weixin.PARTNER_KEY;
	        String sign = WeixinUtil.createSign("UTF-8", parameters, mchKey);
	        parameters.put("sign", sign);
	        String xml = XMLUtil.getRequestXml(parameters);
	        log.info("预付订单提交数据" + xml);
	        String result = QiyeWeixinUtil.httpsRequest(InterfacePath.GATEWAY_WECHAT_PAY, "POST", xml);
	        log.info("请求预付订单返回数据" + result);
	        Map<String, String> resultMap = XMLUtil.doXMLParse(result);
	        String return_code = resultMap.get("return_code");
	        String return_msg = resultMap.get("return_msg");
	        if (return_code.equals("SUCCESS")) {
	            String result_code = resultMap.get("result_code");
	            String err_code = resultMap.get("err_code");
	            String err_code_des = resultMap.get("err_code_des");
	            if (result_code.equals("SUCCESS")) {
	                String prepayid = resultMap.get("prepay_id");
	                if (StringUtils.isNotEmpty(prepayid)) {
	                    SortedMap<String, Object> payInfoMap = new TreeMap<String, Object>();
	                    payInfoMap.put("appId", appId);
	                    payInfoMap.put("timeStamp", System.currentTimeMillis() / 1000 + "");
	                    payInfoMap.put("nonceStr", WeixinUtil.CreateNoncestr());
	                    payInfoMap.put("package", "prepay_id=" + resultMap.get("prepay_id"));
	                    payInfoMap.put("signType", "MD5");
	                    String paySign = WeixinUtil.createSign("UTF-8", payInfoMap, mchKey);
	                    payInfoMap.put("paySign", paySign);
	                    //payInfoMap.put("code_url", resultMap.get("code_url"));
	                    payInfoMap.put("packageValue", "prepay_id=" + resultMap.get("prepay_id"));
	                    log.info("payInfo:{"+payInfoMap+"}");
	                    params.put("pay_info", payInfoMap);
	                    
	                    dao.insert(payOrder);

	                    params.put("payOrderId", payOrder.getId());
	                }
	            }
	            params.put("result_code", result_code);
	            params.put("err_code", err_code);
	            params.put("err_code_des", err_code_des);
	        }
	        params.put("return_code", return_code);
	        params.put("return_msg", return_msg);
	        return params;
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public void payCallBack(HttpServletRequest req, HttpServletResponse res) throws IOException,
		    JDOMException {
		long startTime = System.currentTimeMillis();
		
		InputStream inStream = req.getInputStream();
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
		    outSteam.write(buffer, 0, len);
		}
		// 获取微信调用我们notify_url的返回信息
		String result = new String(outSteam.toByteArray(), "utf-8");
		log.info("微信支付回调内容:\n{"+result+"}");
		if (StringUtils.isEmpty(result)) {
		    log.error("微信支付回调内容为空!!!!");
		    res.getWriter().write(WeixinUtil.setXML("FAIL", ""));
		    return;
		}
		Map<String, String> map = XMLUtil.doXMLParse(result);
		outSteam.close();
		inStream.close();
		String code = map.get("result_code");
		log.info("支付状态:{"+code+"}");
		if ("SUCCESS".equals(code)) {
		    String payId = map.get("out_trade_no");
		    String outTraceId = map.get("transaction_id");
		    String onlineMoney = map.get("total_fee");// 交易金额，单位：分
		    String callerName = map.get("attach");
		    log.info("wechatPayCallback payId:{"+payId+"}, outTraceId:{"+outTraceId+"}, onlineMoney:{"+onlineMoney+"}, callerName:{"+callerName+"}");
		    WeixinUtil.respToWeixin(res, "SUCCESS", "OK");
		    ShopPayOrder payOrder = dao.fetch(ShopPayOrder.class,Cnd.where("pay_id","=",payId));
		    
		    if (payOrder != null) {
		        if (payOrder.getStatus().equals("1")) {
		        	WeixinUtil.respToWeixin(res, "SUCCESS", "OK");
		        } else {
		            long costTime = System.currentTimeMillis() - startTime;
		            log.info("wechatPayCallback costtime:{"+costTime+"} ms");
		            WeixinUtil.respToWeixin(res, "SUCCESS", "OK");
		            payOrder.setFinish_time(new Date());
		            payOrder.setTransaction_id(outTraceId);
		            payOrder.setStatus(1);
		            dao.update(payOrder);
		            ShopOrder so = dao.fetch(ShopOrder.class, Cnd.where("pay_id","=",payId));
		            if(so!=null){
		            	so.setPayStatus(1);
		            	dao.update(so);
		            	ShopClient sc = dao.fetch(ShopClient.class,so.getShopClientId());
		            	sc.setStatus(ClientType.ENABLE);
		            	dao.update(sc);
		            	EduStudent es = dao.fetch(EduStudent.class,so.getEdu_student_id());
		            	es.setStatus(1);
		            	dao.update(es);
		            }
		        }
		
		    } else {
		        log.error("pay order:{"+payId+"} not exists");
		    	WeixinUtil.respToWeixin(res, "FAIL", "");
		    }
		} else {
		    log.error("支付状态{"+code+"}返回异常！！！");
		    WeixinUtil.respToWeixin(res, "FAIL", "");
		    return;
		}
		
		}
	
	public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
	
}
