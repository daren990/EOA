package cn.oa.web.action.wx;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
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
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


import cn.oa.model.Archive;
import cn.oa.model.User;
import cn.oa.repository.Mapper;
import cn.oa.utils.JobNumber;
import cn.oa.web.action.Action;
import cn.oa.web.action.wx.comm.AesException;
import cn.oa.web.action.wx.comm.WXBizMsgCrypt;

@Filters
@IocBean(name = "wx.HrcoreAction")
@At(value="/wx/HrcoreAction")
public class HrCoreAction extends Action{
	
	
	@GET
	@At
	public void Hr(HttpServletRequest req,HttpServletResponse res) throws AesException, Exception{
		
		String sToken = "FrSszzGrqP9A";
		String sCorpID = "wxe0fa059268bdce6f";
		String sEncodingAESKey = "GwXOQf8q3aI9jrJXyI2lfhV2hwDDy8E7Uv1w9ym3lid";
		//Map<String, String> map = new HashMap<String, String>();

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
	public  void Hr(HttpServletRequest req ,HttpServletResponse res ,Boolean bn) throws AesException, IOException{
		String sToken = "FrSszzGrqP9A";
		String sCorpID = "wxe0fa059268bdce6f";
		String sEncodingAESKey = "GwXOQf8q3aI9jrJXyI2lfhV2hwDDy8E7Uv1w9ym3lid";
		 //获得请求参数  
        String sReqMsgSig =  req.getParameter("msg_signature");  
        String sReqTimeStamp = req.getParameter("timestamp");  
        String sReqNonce = req.getParameter("nonce");  
        //获得post提交的数据  
        BufferedReader br=new BufferedReader(new InputStreamReader(req.getInputStream()));  
        StringBuilder sbuff=new StringBuilder();  
        String tmp=null;      
        while((tmp=br.readLine())!=null){  
            sbuff.append(tmp);  
        }  
        String sReqData = sbuff.toString();  
        //System.out.println("sReqData " + sReqData);
		WXBizMsgCrypt wxcpt = null;
		try {
			wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);
		} catch (AesException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			String sMsg = wxcpt.DecryptMsg(sReqMsgSig, sReqTimeStamp, sReqNonce, sReqData);
			System.out.println("after decrypt msg: " + sMsg);
			// TODO: 解析出明文xml标签的内容进行处理
			//String rt = HrCoreService.processRequest(sMsg);
			// TODO: 解析出明文xml标签的内容进行处理
			// For example:
			String sendText = null;
			String text = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			StringReader sr = new StringReader(sMsg);
			InputSource is = new InputSource(sr);
			Document document = db.parse(is);

			Element root = document.getDocumentElement();
			// 得到根元素的所有子节点  
			NodeList ToUserName = root.getElementsByTagName("ToUserName");
			NodeList FromUserName = root.getElementsByTagName("FromUserName");
			NodeList CreateTime = root.getElementsByTagName("CreateTime");
			NodeList MsgType = root.getElementsByTagName("MsgType");
			NodeList AgentID = root.getElementsByTagName("AgentID");
			Map<String, String> map = new HashMap<String, String>();
			map.put("ToUserName", ToUserName.item(0).getTextContent());
			map.put("FromUserName", FromUserName.item(0).getTextContent());
			map.put("CreateTime", CreateTime.item(0).getTextContent());
			map.put("MsgType", MsgType.item(0).getTextContent());
			map.put("AgentID", AgentID.item(0).getTextContent());
			//判断接收方式为event
			if(map.get("MsgType").equals("event")){
				NodeList Event = root.getElementsByTagName("Event");
				map.put("Event", Event.item(0).getTextContent());
				//判断事件类型为click
				if(map.get("Event").equals("click")){
					NodeList EventKey = root.getElementsByTagName("EventKey");
					map.put("EventKey", EventKey.item(0).getTextContent());
					//判断事件KEY值，与自定义菜单接口中KEY值对应
					if(map.get("EventKey").equals("1")){
						text = "你好!\n";						
						text += "1.输入(@用户名)\n ";
						text += "  可以查询员工信息\n";
						text += "2.输入(&用户名)\n";
						text += "  可以查询员工档案\n";
						text += "  如:@dehualiu\n";
						sendText = HrCoreService.STextMsg(map, text);	
					}
					/*if(map.get("EventKey").equals("2")){
						sendText = HrCoreService.STextMsg(map, "测试发送消息");					
					}
					if(map.get("EventKey").equals("3")){
						sendText = HrCoreService.STextMsg(map, "测试发送消息");					
					}*/
				}
			}
			//判断接收方式为text
			if(map.get("MsgType").equals("text")){
				NodeList Content = root.getElementsByTagName("Content");
				map.put("Content", Content.item(0).getTextContent());
				//查询用户档案
				if(map.get("Content").indexOf("&") == 0){
					try{
					   Archive archive = mapper.fetch(Archive.class, "Archive.query", Cnd.where("u.username", "=",  map.get("Content").substring(1)));
					   text =  findArchive(archive);
						} catch (Exception e) {
						 e.printStackTrace();
						 text = "该员工不存在!";
					}
					sendText = HrCoreService.STextMsg(map, text);
					}
				
				//查询用户信息
				else if(map.get("Content").indexOf("@") == 0){
					try{						
						User user = mapper.fetch(User.class, "User.query", Cnd.where("u.username", "=", map.get("Content").substring(1)));
						text = findUser(user);
						} catch (Exception e) {
							 text = "该员工不存在!";
						}
					sendText = HrCoreService.STextMsg(map, text);
					}
				else if(map.get("Content").indexOf("%") == 0){
					text = "您的输入有误!";
					sendText = HrCoreService.STextMsg(map, text);
					}
				else{
					text = "你好!\n";						
					text += "1.输入(@用户名)\n ";
					text += "  可以查询员工信息\n";
					text += "2.输入(&用户名)\n";
					text += "  可以查询员工档案\n";
					text += "  如:@dehualiu\n";
					sendText = HrCoreService.STextMsg(map, text);					
				}
			}
			String ec = wxcpt.EncryptMsg(sendText, sReqTimeStamp, sReqNonce);
			res.getWriter().write(ec);
		} catch (Exception e) {
			// TODO
			// 解密失败，失败原因请查看异常
			e.printStackTrace();
		}
	}
	
	private String findArchive(Archive archive) {
		   SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		   String text = "工号:" + archive.getJobNumber() + "\n";
		   text += "姓名:" + archive.getTrueName() + "\n";	
		   if(archive.getGender().equals(1)){
		   text += "性别:男\n";
		   }else if(archive.getGender().equals(0)){
		   text += "性别:女\n";}
		   text += "".equals(archive.getIdcard())||archive.getIdcard()==null?"":"身份证:\n"+archive.getIdcard()+"\n";		   
		   text += "".equals(archive.getBirthday())||archive.getBirthday()==null?"":"出生年月日:"+format.format(archive.getBirthday())+"\n";
		   text += "".equals(archive.getPlace())||archive.getPlace()==null?"":"贯籍:"+archive.getPlace()+"\n";
		   text += "".equals(archive.getMarry())||archive.getMarry()==null?"":"婚否:"+intToString(archive.getMarry())+"\n";
		   text += "".equals(archive.getDegree())||archive.getDegree()==null?"":"最高学历:"+archive.getDegree()+"\n";
		   text += "".equals(archive.getMajor())||archive.getMajor()==null?"":"所学专业:"+archive.getMajor()+"\n";
		   text += "".equals(archive.getSchool())||archive.getSchool()==null?"":"毕业院校:\n"+archive.getSchool()+"\n";
		   text += "".equals(archive.getPhone())||archive.getPhone()==null?"":"联系电话:"+archive.getPhone()+"\n";
		   text += "".equals(archive.getAddress())||archive.getAddress()==null?"":"联系地址:"+archive.getAddress()+"\n";
		   text += "".equals(archive.getQQ())||archive.getQQ()==null?"":"QQ:"+archive.getQQ()+"\n";
		   text += "".equals(archive.getExigencyName())||archive.getExigencyName()==null?"":"紧急联系人:"+archive.getExigencyName()+"\n";
		   text += "".equals(archive.getExigencyPhone())||archive.getExigencyPhone()==null?"":"紧急联系电话:"+archive.getExigencyPhone()+"\n";
		   text += "".equals(archive.getEmail())||archive.getEmail()==null?"":"邮箱:\n"+archive.getEmail()+"\n";
		   text += "".equals(archive.getContractStart())||archive.getContractStart()==null?"":"合同开始时间:"+format.format(archive.getOnPosition())+"\n";
		   text += "".equals(archive.getAmount())||archive.getAmount()==null?"":"签约次数:"+archive.getAmount()+"\n";
		   text += "".equals(archive.getEntryDate())||archive.getEntryDate()==null?"":"入职日期:"+format.format(archive.getEntryDate())+"\n";
		   text += "".equals(archive.getFullDate())||archive.getFullDate()==null?"":"转正日期:"+format.format(archive.getFullDate())+"\n";
		   text += "".equals(archive.getPosition())||archive.getPosition()==null?"":"职务:"+archive.getPosition()+"\n";
		   text += "".equals(archive.getOnPosition())||archive.getOnPosition()==null?"":"是否在职:"+intToString(archive.getOnPosition())+"\n";
		return text;
	}
	
	private String findUser(User user) {
		String text = "工号:" + user.getJobNumber() + "\n";
		text += "姓名:" + user.getTrueName() + "\n";	
		text += "用户名:"+user.getUsername()+"\n";
		text += "所属公司:\n"+user.getCorpName()+"\n";
		text += "所属架构:"+user.getOrgName()+"\n";
		text += "直属上司:"+user.getManagerName()+"\n";
		text += "审核等级:"+user.getLevel()+"\n";
		text += "邮箱:\n"+user.getEmail()+"\n";
		text += user.getStatus().equals(1)?"状态:启用\n":"状态:禁用\n";
		return text;
	}
	private String intToString(int i){		
		return i==1?"是":"否";
	}
}
