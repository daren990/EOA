package test.dao.andy;

import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.joda.time.DateTime;
import org.junit.Test;

import cn.oa.consts.ParamesAPI;
import cn.oa.web.action.wx.WxSendService;
import cn.oa.web.action.wx.comm.QiyeWeixinUtil;
import cn.oa.web.wx.Access_TokenQiYeSingleton;

import test.Setup;

public class TestWeixin  extends Setup{
	
	@Test
	public void test(){
		WxSendService wx=new WxSendService();
//		wx.addUser();
//		wx.getDept();
//		wx.sendMessage();
		wx.getUsers();
//		wx.delUser();
	}
	
	@Test
	public void test2(){
		WxSendService wx=new WxSendService();
		wx.getApp();

	}
	
	@Test
	public void test3(){
		String accessToken = Access_TokenQiYeSingleton.getInstance().getSingleACCESS_TOKEN(ParamesAPI.CORPID,ParamesAPI.SERECT);
		System.out.println(accessToken);
		accessToken = Access_TokenQiYeSingleton.getInstance().getSingleACCESS_TOKEN(ParamesAPI.CORPID,ParamesAPI.SERECT);
		System.out.println(accessToken);
		accessToken = Access_TokenQiYeSingleton.getInstance().getSingleACCESS_TOKEN(ParamesAPI.CORPID,ParamesAPI.SERECT);
		System.out.println(accessToken);
	}
	
	@Test
	public void test4(){
//		String accessToken = Access_TokenQiYeSingleton.getInstance().getSingleACCESS_TOKEN(ParamesAPI.CORPID,ParamesAPI.SERECT);
//		System.out.println(accessToken);
		String accessToken = "xcSC1dRecDlEzfgHGQblAzkUySdTF6K4ek7W0KkJuvLrDdiYlyu9OEl1TXBfHbXCTIJSJajYwBhvmGhYh03bGjNeY6S65wMYH87G_WCkthPBImO2sBN6GMv2MbqyDAV8rnnTPAXyI6K8W1mzlf6qW0EOPzdMPtLo-Jdapg2zjKJOx7893qXA20oBuYtTt8jJMYznzBRDwAnfGKa8YSFrnrIupMRMf-M-4SZMyVIfOCzudKtdG6RfOPZ0OSC6CzaGLFoomP9i8NladGB2ai0p71rOj25Oln1HdUOv-2sav2Q";
		String postData = "{\"opencheckindatatype\":%s,\"starttime\":%s,\"endtime\":%s,\"useridlist\": [\"ruianliu\"]}";
		
		String data = String.format(postData,"3",new DateTime(new Date()).plusDays(-1).toDate().getTime() / 1000 +"", new Date().getTime() / 1000 +""); 
		System.out.println(data);
		JSONObject jsonObject = QiyeWeixinUtil.httpRequest("https://qyapi.weixin.qq.com/cgi-bin/checkin/getcheckindata?access_token="+accessToken, "POST", data);
		System.out.println(jsonObject.toString());
		JSONArray jsonArray = jsonObject.getJSONArray("checkindata");
		System.out.println(jsonArray.toString());
		String userid = (String) jsonArray.getJSONObject(0).get("userid");
		String checkin_time = jsonArray.getJSONObject(0).get("checkin_time").toString();
		String location_detail = (String) jsonArray.getJSONObject(0).get("location_detail");
		System.out.println(userid+"|"+checkin_time+"|"+location_detail+"|");
	}
	
	@Test
	public void test5(){
		
		System.out.println(new Date().getTime());
		System.out.println(new DateTime(new Date()).plusDays(-1).toDate().getTime());
	}
	
}
