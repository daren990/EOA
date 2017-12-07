package cn.oa.web.action.wx;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import cn.oa.consts.ParamesAPI;
import cn.oa.utils.except.Errors;
import cn.oa.web.action.wx.comm.QiyeWeixinUtil;
import cn.oa.web.wx.Access_TokenQiYeSingleton;
import cn.oa.web.wx.pojo.Article;
import cn.oa.web.wx.pojo.SMessage;

public class WxSendService {
	private final String GETEMPLOYEE= "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN&userid=lisi";
	/**
	 * 参数 	必须 	说明
	 * touser 	否 	UserID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
	 * toparty 否 	PartyID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
	 * totag 	否 	TagID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
	 * agentid 是 	企业应用的id，整型。可在应用的设置页面查看
	 * title 	否 	标题
	 * description 否 	描述
	 * url 	否 	点击后跳转的链接。企业可根据url里面带的code参数校验员工的真实身份。具体参考“9 微信页面跳转员工身份查询”
	 * picurl 	否 	图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图640*320，小图80*80。如不填，在客户端不显示图片 
	 * 
	 * */
	public void sendarticle(String touser,String toparty,String totag,String agentid,String title,String description,String url,String picurl){
		
		// 调取凭证  
	    //String access_token = QiyeWeixinUtil.getAccessToken(ParamesAPI.CORPID,ParamesAPI.SERECT).getToken(); 
		//向微信发送请求，获取密钥
		String access_token = Access_TokenQiYeSingleton.getInstance().getSingleACCESS_TOKEN(ParamesAPI.CORPID,ParamesAPI.SERECT);
		System.out.println(access_token);
		//按需求拼接url
		String getUrl = GETEMPLOYEE.replace("ACCESS_TOKEN", access_token).replace("lisi", touser);
		System.out.println(access_token);
		//获取指定用户的信息，包括微信号,注意需要传入密钥access_token
		boolean isPay = QiyeWeixinUtil.GetEmployee(access_token, "GET",getUrl);
		//如果用户已关注微信（公众号？）
		System.out.println(isPay);
		if(isPay){
			//System.out.println("开始执行发送微信");
		    String tu="\""+touser+"\"";
		    String tp="\""+toparty+"\"";
		    String tt="\""+totag+"\"";
		    String ag="\""+agentid+"\"";
			//图文信息
		    Article article1=new Article();
			article1.setTitle(title);
			article1.setPicurl(picurl);
			article1.setDescription(description);
			article1.setUrl(url);
			System.out.println(url);
			// 整合图文  
		    List<Article> list = new ArrayList<Article>(); 
		    list.add(article1);
		    String articlesList = JSONArray.fromObject(list).toString(); 
		    //System.out.println(articlesList);
		    //将所要发送的消息按指定的格式进行拼接
		    String PostData=SMessage.SNewsMsg(tu, tp, tt, ag,articlesList);
			System.out.println(PostData);
			System.out.println();
		    int result = QiyeWeixinUtil.PostMessage(access_token, "POST",SMessage.POST_URL,PostData);  
		    if(0!=result){  
		          throw new Errors("微信发送失败!请稍候重试");
		    	}		    
		}
	}
	
	public void addUser(){
		//获取密钥
		String access_token = QiyeWeixinUtil.getAccessToken(ParamesAPI.CORPID,ParamesAPI.SERECT).getToken();
		System.out.println(access_token);
		String postData = "{\"userid\":%s,\"name\":%s,\"department\":[1],\"mobile\":%s}";
		String data = String.format(postData,"\"antingluo\"","\"罗安庭\"","\"18816796409\""); 
		System.out.println(data);
		JSONObject jsonObject = QiyeWeixinUtil.httpRequest("https://qyapi.weixin.qq.com/cgi-bin/user/create?access_token="+access_token	, "POST", data);
		System.out.println(jsonObject.toString());
	}
	
	public void getDept(){
		//获取密钥
		String access_token = Access_TokenQiYeSingleton.getInstance().getSingleACCESS_TOKEN(ParamesAPI.CORPID,ParamesAPI.SERECT);
		System.out.println(access_token);
		JSONObject jsonObject = QiyeWeixinUtil.httpRequest("https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="+access_token	, "GET", null);
		System.out.println(jsonObject.toString());
	}
	
	public void sendMessage(){
		//向微信发送请求，获取密钥
		String access_token = QiyeWeixinUtil.getAccessToken(ParamesAPI.CORPID,ParamesAPI.SERECT).getToken();
		System.out.println(access_token);
		//按需求拼接url
		String getUrl = GETEMPLOYEE.replace("ACCESS_TOKEN", access_token).replace("lisi", "ruianliu");
		System.out.println(access_token);
		//获取指定用户的信息，包括微信号,注意需要传入密钥access_token
		boolean isPay = QiyeWeixinUtil.GetEmployee(access_token, "GET",getUrl);
		System.out.println(isPay);
		if(isPay){
	
		}
	}
	
	public void getUsers(){
		//向微信发送请求，获取密钥
		String access_token = QiyeWeixinUtil.getAccessToken(ParamesAPI.CORPID,ParamesAPI.SERECT).getToken();
		System.out.println(access_token);

		JSONObject jsonObject = QiyeWeixinUtil.httpRequest("https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token="+access_token+"&department_id="+1+"&fetch_child=0&status=1", "GET", null);
		System.out.println(jsonObject.toString());
	}
	
	public void delUser(){
		//获取密钥
		String access_token = QiyeWeixinUtil.getAccessToken(ParamesAPI.CORPID,ParamesAPI.SERECT).getToken();
		System.out.println(access_token);
		JSONObject jsonObject = QiyeWeixinUtil.httpRequest("https://qyapi.weixin.qq.com/cgi-bin/user/delete?access_token="+access_token+"&userid="+"antingluo", "GET", null);
		System.out.println(jsonObject.toString());
	}
	
	public void getApp(){
		//获取密钥
		String access_token = QiyeWeixinUtil.getAccessToken(ParamesAPI.CORPID,ParamesAPI.SERECT).getToken();
		System.out.println(access_token);
		JSONObject jsonObject = QiyeWeixinUtil.httpRequest("https://qyapi.weixin.qq.com/cgi-bin/agent/get?access_token="+access_token+"&agentid=3", "GET", null);
		System.out.println(jsonObject.toString());
	}
	
}
