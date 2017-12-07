package cn.oa.web.action.wx;


import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.view.ServerRedirectView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cn.oa.consts.Status;
import cn.oa.model.Logger;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.except.Errors;
import cn.oa.utils.lang.ParseXml;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Cookies;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
import cn.oa.web.action.LoginAction;
import cn.oa.web.action.WxIndexAction;
import cn.oa.web.action.wx.comm.AesException;
import cn.oa.web.action.wx.comm.WXBizMsgCrypt;

@Filters
@IocBean(name = "wx.ValidateAction")
@At(value="/wx/ValidateAction")
public class ValidateAction extends Action{
	
	
	@GET
	@At
	public void validate(HttpServletRequest req, HttpServletResponse res) throws AesException, Exception{
		
		String sToken = "FrSszzGrqP9A";
		String sCorpID = "wxe0fa059268bdce6f";
		String sEncodingAESKey = "GwXOQf8q3aI9jrJXyI2lfhV2hwDDy8E7Uv1w9ym3lid";
		Map<String, String> map = new HashMap<String, String>();

		WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);
		/*
		*企业开启回调模式时，企业号会向验证url发送一个get请求 
		假设点击验证时，企业收到类似请求：
		* GET /cgi-bin/wxpush?msg_signature=5c45ff5e21c57e6ad56bac8758b79b1d9ac89fd3&timestamp=1409659589&nonce=263014780&echostr=P9nAzCzyDtyTWESHep1vC5X9xho%2FqYX3Zpb4yKa9SKld1DsH3Iyt3tP3zNdtp%2B4RPcs8TgAE7OaBO%2BFZXvnaqQ%3D%3D 
		* HTTP/1.1 Host: qy.weixin.qq.com
		接收到该请求时，企业应	1.解析出Get请求的参数，包括消息体签名(msg_signature)，时间戳(timestamp)，随机数字串(nonce)以及公众平台推送过来的随机加密字符串(echostr),
		这一步注意作URL解码。
		2.验证消息体签名的正确性 
		3. 解密出echostr原文，将原文当作Get请求的response，返回给公众平台
		第2，3步可以用公众平台提供的库函数VerifyURL来实现。

		*/
		// 解析出url上的参数值如下：
		 	String sVerifyMsgSig = req.getParameter("msg_signature");  
	        // 时间戳  
	        String sVerifyTimeStamp = req.getParameter("timestamp");  
	        // 随机数  
	        String sVerifyNonce = req.getParameter("nonce");  
	        // 随机字符串  
	        String sVerifyEchoStr = req.getParameter("echostr"); 
		
	       String sEchoStr; //需要返回的明文
		try {
			sEchoStr = wxcpt.VerifyURL(sVerifyMsgSig, sVerifyTimeStamp,
					sVerifyNonce, sVerifyEchoStr);
			System.out.println("verifyurl echostr: " + sEchoStr);
			res.getWriter().write(sEchoStr);
			// 验证URL成功，将sEchoStr返回
			// HttpUtils.SetResponse(sEchoStr);
		} catch (Exception e) {
			//验证URL失败，错误原因请查看异常
			e.printStackTrace();
		}
	}
	
	@POST
	@At
	public  void validate(HttpServletRequest req) throws AesException, IOException{
	//	String ip = req.getRemoteAddr();
		String sToken = "FrSszzGrqP9A";
		String sCorpID = "wxe0fa059268bdce6f";
		String sEncodingAESKey = "GwXOQf8q3aI9jrJXyI2lfhV2hwDDy8E7Uv1w9ym3lid";
	//	Map<String, String> map = new HashMap<String, String>();

		WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);
		
		String sReqMsgSig = req.getParameter("msg_signature");
		
		String sReqTimeStamp = req.getParameter("timestamp");
		
		String sReqNonce = req.getParameter("nonce");
		
		InputStream in = req.getInputStream();
		
		// post请求的密文数据
		String sReqData = ParseXml.input2String(in);
		String sMsg = wxcpt.DecryptMsg(sReqMsgSig, sReqTimeStamp, sReqNonce, sReqData);
		System.out.println("after decrypt msg: " + sMsg);
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			StringReader sr = new StringReader(sMsg);
			InputSource is = new InputSource(sr);
			Document document = db.parse(is);
			
			Element root = document.getDocumentElement();
			NodeList nodelist1 = root.getElementsByTagName("FromUserName");
			NodeList nodelist2 = root.getElementsByTagName("EventKey");
			String FromUserName = nodelist1.item(0).getTextContent();
			String EventKey = nodelist2.item(0).getTextContent();
			System.out.println("FromUserName：" + FromUserName);
			System.out.println("EventKey：" + EventKey);
		//	WxIndexAction.value(FromUserName);
			req.setAttribute("FromUserName", FromUserName);
			req.setAttribute("url", EventKey);
			
			wxlogin2(req);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	//新增企业号自动登录
	public Object wxlogin2(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try{
		User user = null;
		Cookie[] cookies = req.getCookies();
		String url = (String) req.getParameter("url");
		if(Context.getUser()!= null){
			return new ServerRedirectView(url);
			
		}else{
			url = (String) req.getAttribute("url");
			String userName = req.getAttribute("FromUserName").toString();
			String value = (String) Validator.validate(userName, R.CLEAN, R.REQUIRED, R.RANGE, "{4,60}", "username:用户名");
			
			user = mapper.fetch(User.class, "User.query", Cnd
					.where("u.username", "=", userName)
					.and("u.status", "=", Status.ENABLED));
			
			if(user !=null){
			List<Role> roles = roleRepository.find(user.getUserId());
			String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
			Context.setUser(user, roleNames);
			
			DateTime now = new DateTime();
			Logger logger = new Logger(user.getUserId(), Webs.ip(req), Webs.browser(req), "/login", "用户登录", now.toDate());
			dao.insert(logger);
			return new ServerRedirectView(url);
			}
		}
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String uid = Cookies.get(cookie, Cookies.USER_ID);
				if (uid != null) {
					req.setAttribute("uid", new String(Encodes.decodeBase64(uid)));
					break;
				}
			}
		}
		Code.ok(mb, "");
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		
		return mb;
	}
}
