package cn.oa.utils;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/** 
 * 短信工具类
 */
public class SmsUtil {
	
	public static void main(String [] args) {
		int RESTULT = sendIHuYi("13622247860","您的验证码是：1111。请不要把验证码泄露给其他人。");
		System.out.println(RESTULT);
	}
	 
	//短信商   http://www.ihuyi.com
	private static String Url_IHuYi = "http://106.ihuyi.com/webservice/sms.php?method=Submit";
	
	
	/**
	 * @param mobile 手机号
	 * @param code  短信内容
	 * @return code >> [2:提交成功]、[4030:手机号码已被列入黑名单]、[4051:剩余条数不足]、[4085:同一手机号验证码短信发送超出5条]
	 */
	public static int sendIHuYi(String mobile,String code){
		HttpClient client = new HttpClient(); 
		PostMethod method = new PostMethod(Url_IHuYi); 
		client.getParams().setContentCharset("UTF-8");
		method.setRequestHeader("ContentType","application/x-www-form-urlencoded;charset=UTF-8");
	    String content = new String(code);  
	    String account = "", password = "";
		account = "C36832580"; 
		password = "90e27af155c55fa3cfbcc29f44cc7345";
		NameValuePair[] data = {//提交短信
		    new NameValuePair("account", account), 
		    new NameValuePair("password", password), 			//密码可以使用明文密码或使用32位MD5加密
		    new NameValuePair("mobile", mobile), 
		    new NameValuePair("content", content),
		};
		method.setRequestBody(data);
		try {
			client.executeMethod(method);
			String SubmitResult =method.getResponseBodyAsString();
			Document doc = DocumentHelper.parseText(SubmitResult); 
			Element root = doc.getRootElement();
			code = root.elementText("code");
			String msg = root.elementText("msg");
			String smsid = root.elementText("smsid");
			System.out.println(code);
			System.out.println(msg);
			System.out.println(smsid);
			if(code == "2"){
				System.out.println("短信提交成功");
			}
			return Integer.valueOf(code);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}	
		return 0;
	}	
}