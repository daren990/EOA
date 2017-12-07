package test.weixin;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.junit.Test;

import cn.oa.consts.MessageTemplate;
import cn.oa.consts.ParamesAPI;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.ShopClient;
import cn.oa.web.action.wx.comm.QiyeWeixinUtil;
import cn.oa.web.wx.InterfacePath;

public class TestWeixin {
	private final String GETEMPLOYEE= "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN&userid=lisi";
	
	@Test
	public void test(){
		String access_token = QiyeWeixinUtil.getAccessToken(ParamesAPI.CORPID,ParamesAPI.SERECT).getToken();
		System.out.println(access_token);
		String getUrl = GETEMPLOYEE.replace("ACCESS_TOKEN", access_token).replace("lisi", "genshengtang");
		System.out.println(getUrl);
		boolean isPay = QiyeWeixinUtil.GetEmployee(access_token, "GET",getUrl);
		System.out.println(isPay);
	}
	
	//设置行业信息
	@Test
	public void test2(){
		String access_token = "bo1Mt9KAyd4RItuf2JQhZInNvZ-zdwEwBdylrDyAhKKW2d7RYbSz1QxCFn6QceMqHb3YN5fIGNOIH3Y8CPtMrLTBImq7C3gPdvzHpjcaXwYSXKfABANSF";
		String url = "https://api.weixin.qq.com/cgi-bin/template/api_set_industry?access_token=ACCESS_TOKEN";
		System.out.println(url);
		String outstr = "{\"industry_id1\":\"16\",\"industry_id2\":\"17\"}";
		int result = QiyeWeixinUtil.PostMessage(access_token, "POST", url, outstr);
		System.out.println(result);
	}
	
	//获取行业信息
	@Test
	public void test3(){
		String access_token = "bo1Mt9KAyd4RItuf2JQhZInNvZ-zdwEwBdylrDyAhKKW2d7RYbSz1QxCFn6QceMqHb3YN5fIGNOIH3Y8CPtMrLTBImq7C3gPdvzHpjcaXwYSXKfABANSF";
		String url = "https://api.weixin.qq.com/cgi-bin/template/get_industry?access_token=ACCESS_TOKEN";
		url = url.replace("ACCESS_TOKEN", access_token);
		System.out.println(url);
		JSONObject jsonObject =  QiyeWeixinUtil.httpRequest(url, "GET", null);
		System.out.println(jsonObject.toString());
	}
	
	//获取帐号下的模版列表
	@Test
	public void test4(){
		String access_token = "18y25R1XvUEwV6s2hTf0HzQ1tgPlVrNHNWtADRexAQRsJOjB-pklcv1yImojuLLRepkTS8uyW5aNIO-pPhLBV11Zjip8QU5MMGhSxDByUgl41G3jmshiem_Uc-GN8nB4SUQaADAACY";
		String url = "https://api.weixin.qq.com/cgi-bin/template/get_all_private_template?access_token=ACCESS_TOKEN";
		url = url.replace("ACCESS_TOKEN", access_token);
		System.out.println(url);
		JSONObject jsonObject =  QiyeWeixinUtil.httpRequest(url, "GET", null);
		System.out.println(jsonObject.toString());
	}
	
	//发送模版消息
	@Test
	public void test5(){
		String access_token = "uPLqoKBWtPEogtDAvU-AMTkM6deGFNw8pMdfF21kyjXFUDcd0cIBaBIqO3VflQhG1vnRIChh_f6AC2Mru7yxHuV8JzYGIRVSmYMOgUaSaD1T_6Rzm5gKPKUl9d1osIM5ZDAeAAACGD";
		String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN";
		System.out.println(url);
		String outstr = "{\"touser\":\"o3qya01yskVpEIGRmfCJcRDzO61g\",\"template_id\":\"BJsJ_VptTqRqnEdVgl2o3mA5ytOxPVgCZSMAuiXHH10\",\"data\":{\"first\":{\"value\":\"亲爱的小花同学，你有一门课程即将开始。\",\"color\":\"#173177\"},\"keyword1\":{\"value\":\"CAD基础入门\",\"color\":\"#173177\"},\"keyword2\":{\"value\":\"第一章 01节 CAD工作界面设置\",\"color\":\"#173177\"},\"keyword3\":{\"value\":\"8月28日 10:00~12:00\",\"color\":\"#173177\"},\"remark\":{\"value\":\"老师和同学都在等着你哦，抓紧来吧~\",\"color\":\"#173177\"}}}";
		QiyeWeixinUtil.PostMessage(access_token, "POST", url, outstr);
	}
	
	//发送模板消息
	@Test
	public void test7(){
		String access_token = "uPLqoKBWtPEogtDAvU-AMTkM6deGFNw8pMdfF21kyjXFUDcd0cIBaBIqO3VflQhG1vnRIChh_f6AC2Mru7yxHuV8JzYGIRVSmYMOgUaSaD1T_6Rzm5gKPKUl9d1osIM5ZDAeAAACGD";
		String outJson = getOutJson("o3qya01yskVpEIGRmfCJcRDzO61g", "BJsJ_VptTqRqnEdVgl2o3mA5ytOxPVgCZSMAuiXHH10", null, null);
		System.out.println(outJson);
		QiyeWeixinUtil.PostMessage(access_token, "POST", InterfacePath.SEND_MESSAGE_URL_ANDY, outJson);
	}
	
	private String getOutJson(String openId, String template_id, List<EduTeachingSchedule> tss, ShopClient client){
		List<String> content = new ArrayList<String>();
		content.add("亲爱的小花同学，你有一门课程即将开始。");
		content.add("CAD基础入门");
		content.add("第一章 01节 CAD工作界面设置");
		content.add("8月28日 10:00~12:00");
		content.add("老师和同学都在等着你哦，抓紧来吧~");
		return fillingTemplate(openId, template_id, content);
	}
	
	private String fillingTemplate(String openId, String template_id, List<String> content){
		String outJson = "{\"touser\":\""+openId+"\",\"template_id\":\""+template_id+"\",\"data\":{\"first\":{\"value\":\""+content.get(0)+"\",\"color\":\"#173177\"},\"keyword1\":{\"value\":\""+content.get(1)+"\",\"color\":\"#173177\"},\"keyword2\":{\"value\":\""+content.get(2)+"\",\"color\":\"#173177\"},\"keyword3\":{\"value\":\""+content.get(3)+"\",\"color\":\"#173177\"},\"remark\":{\"value\":\""+content.get(4)+"\",\"color\":\"#173177\"}}}";
		return outJson;
	}
	

}
