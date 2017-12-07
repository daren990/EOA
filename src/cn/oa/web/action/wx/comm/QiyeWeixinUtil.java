package cn.oa.web.action.wx.comm;

import java.io.BufferedReader;  
import java.io.InputStream;  
import java.io.InputStreamReader;  
import java.io.OutputStream;  
import java.net.ConnectException;  
import java.net.URL;  

import javax.net.ssl.HttpsURLConnection;  
import javax.net.ssl.SSLContext;  
import javax.net.ssl.SSLSocketFactory;  
import javax.net.ssl.TrustManager;  

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.oa.web.wx.pojo.AccessToken;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/** 
* 公众平台通用接口工具类 
*  
*/  
public class QiyeWeixinUtil {  //CommonUtil
  private static Logger log = LoggerFactory.getLogger(QiyeWeixinUtil.class);  
//获取access_token的接口地址（GET） 限200（次/天）  
 public final static String access_token_url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ID&corpsecret=SECRET";   
 
 
 /**
  * 发送https请求
  * 
  * @param requestUrl 请求地址
  * @param requestMethod 请求方式（GET、POST）
  * @param outputStr 提交的数据
  * @return 返回微信服务器响应的信息
  */
 public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
     try {
         // 创建SSLContext对象，并使用我们指定的信任管理器初始化
         TrustManager[] tm = { new MyX509TrustManager() };
         SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
         sslContext.init(null, tm, new java.security.SecureRandom());
         // 从上述SSLContext对象中得到SSLSocketFactory对象
         SSLSocketFactory ssf = sslContext.getSocketFactory();
         URL url = new URL(requestUrl);
         HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
         conn.setSSLSocketFactory(ssf);
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setUseCaches(false);
         // 设置请求方式（GET/POST）
         conn.setRequestMethod(requestMethod);
         conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
         // 当outputStr不为null时向输出流写数据
         if (null != outputStr) {
             OutputStream outputStream = conn.getOutputStream();
             // 注意编码格式
             outputStream.write(outputStr.getBytes("UTF-8"));
             outputStream.close();
         }
         // 从输入流读取返回内容
         InputStream inputStream = conn.getInputStream();
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
         BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
         String str = null;
         StringBuffer buffer = new StringBuffer();
         while ((str = bufferedReader.readLine()) != null) {
             buffer.append(str);
         }
         // 释放资源
         bufferedReader.close();
         inputStreamReader.close();
         inputStream.close();
         inputStream = null;
         conn.disconnect();
         return buffer.toString();
     }
     catch (ConnectException ce) {
    	 log.error("连接超时：{}", ce);
     }
     catch (Exception e) {
    	 log.error("https请求异常：{}", e);
     }
     return null;
 }

  /** 
   * 发起https请求并获取结果 
   *  
   * @param requestUrl 请求地址 
   * @param requestMethod 请求方式（GET、POST） 
   * @param outputStr 提交的数据 
   * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值) 
   */  
  public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) {  
      JSONObject jsonObject = null;  
      StringBuffer buffer = new StringBuffer();  
      try {  
          // 创建SSLContext对象，并使用我们指定的信任管理器初始化  
          TrustManager[] tm = { new MyX509TrustManager() };  
          SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");  
          sslContext.init(null, tm, new java.security.SecureRandom());  
          // 从上述SSLContext对象中得到SSLSocketFactory对象  
          SSLSocketFactory ssf = sslContext.getSocketFactory();  

          URL url = new URL(requestUrl);  
          HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();  
          httpUrlConn.setSSLSocketFactory(ssf);  

          httpUrlConn.setDoOutput(true);  
          httpUrlConn.setDoInput(true);  
          httpUrlConn.setUseCaches(false);  
          // 设置请求方式（GET/POST）  
          httpUrlConn.setRequestMethod(requestMethod);  

          if ("GET".equalsIgnoreCase(requestMethod))  
              httpUrlConn.connect();  

          // 当有数据需要提交时  
          if (null != outputStr) {  
              OutputStream outputStream = httpUrlConn.getOutputStream();  
              // 注意编码格式，防止中文乱码  
              outputStream.write(outputStr.getBytes("UTF-8"));  
              outputStream.close();  
          }  

          // 将返回的输入流转换成字符串  
          InputStream inputStream = httpUrlConn.getInputStream();  
          InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");  
          BufferedReader bufferedReader = new BufferedReader(inputStreamReader);  

          String str = null;  
          while ((str = bufferedReader.readLine()) != null) {  
              buffer.append(str);  
          }  
          bufferedReader.close();  
          inputStreamReader.close();  
          // 释放资源  
          inputStream.close();  
          inputStream = null;  
          httpUrlConn.disconnect();  
          jsonObject = JSONObject.fromObject(buffer.toString()); //字符串转换为json对象! 
      } catch (ConnectException ce) {  
          log.error("Weixin server connection timed out.");  
      } catch (Exception e) {  
          log.error("https request error:{}", e);  
      }  
      return jsonObject;  
  }  
/** 
* 获取access_token 
*  
* @param appid 凭证 
* @param appsecret 密钥 
* @return 
*/  
public static AccessToken getAccessToken(String appid, String appsecret) {  
   AccessToken accessToken = null;  
 
   String requestUrl = access_token_url.replace("ID",appid).replace("SECRET",appsecret); 
   //System.out.println(requestUrl);
   JSONObject jsonObject = httpRequest(requestUrl, "GET", null);  
   // 如果请求成功  
   if (null != jsonObject) {  
       try {  
           accessToken = new AccessToken();  
           accessToken.setToken(jsonObject.getString("access_token"));  
       } catch (JSONException e) {  
           accessToken = null;  
           // 获取token失败  
           log.error("获取token失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));  
       }  
   }  
   return accessToken;  
}
/**
 * 数据提交与请求通用方法
 * @param access_token 凭证
 * @param RequestMt 请求方式
 * @param RequestURL 请求地址
 * @param outstr 提交json数据
 * */  
public static int PostMessage(String access_token ,String RequestMt , String RequestURL , String outstr){  
    int result = 0;  
    RequestURL = RequestURL.replace("ACCESS_TOKEN", access_token);  
    System.out.println(RequestURL);
    JSONObject jsonobject = QiyeWeixinUtil.httpRequest(RequestURL, RequestMt, outstr);
    System.out.println(jsonobject.toString());
     if (null != jsonobject) {   
        if (0 != jsonobject.getInt("errcode")) {   
            result = jsonobject.getInt("errcode");   
            //String error = String.format("操作失败 errcode:{%s} errmsg:{%s}", jsonobject.getInt("errcode"), jsonobject.getString("errmsg"));   
            //System.out.println(error);   
            log.error("发送图文消息获操作失败   errcode:{} errmsg:{}", jsonobject.getInt("errcode"), jsonobject.getString("errmsg"));  
            
        }   
    }  
    return result;  
}  
/**
 * 获取成员信息
 * @param access_token 凭证
 * @param RequestMt 请求方式
 * @param RequestURL 请求地址
 * */  
public static boolean GetEmployee(String access_token ,String RequestMt , String RequestURL){  
    int result = 0;  
    RequestURL = RequestURL.replace("ACCESS_TOKEN", access_token);  
    JSONObject jsonobject = QiyeWeixinUtil.httpRequest(RequestURL, RequestMt, null);
    System.out.println(jsonobject.toString());
     if (null != jsonobject) {   
        if (0 != jsonobject.getInt("errcode")) { 
        	log.error("微信获取用户失败   errcode:{} errmsg:{}", jsonobject.getInt("errcode"), jsonobject.getString("errmsg"));  
        }else{
        	result = jsonobject.getInt("status"); 
        	//System.out.println(result); 
        }
    }
     if(result == 1){
    	 return true;
     }
    return false;  
}  
}  

