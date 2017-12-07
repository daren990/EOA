package cn.oa.web.wx;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.joda.time.DateTime;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.oa.web.action.wx.comm.QiyeWeixinUtil;
import cn.oa.web.wx.pojo.AccessToken;

public class Access_TokenQiYeSingleton {

	private static final Logger log =  LoggerFactory.getLogger(Access_TokenQiYeSingleton.class);  
	
	//私有化构造方法
    private Access_TokenQiYeSingleton() {}
    //维护全局唯一的Access_TokenQiYeSingleton
    private static Access_TokenQiYeSingleton single = new Access_TokenQiYeSingleton();
    
    private Map<String, Map<String, String>> AccessTokenMap = new HashMap<String, Map<String, String>>();

    // 返回全局单例的Access_Token
    public static Access_TokenQiYeSingleton getInstance() {
        return single;
    }
    
    /**
	 * 获取单例的ACCESS_TOKEN
	 */
	public String getSingleACCESS_TOKEN(String appId, String secret) {
		Map<String, String> accessToken = AccessTokenMap.get(appId);
		if(accessToken == null || accessToken.get("access_token") == null){
			synchronized  (single){
				accessToken = AccessTokenMap.get(appId);
				if(accessToken == null || accessToken.get("access_token") == null){
					System.out.println("第一次获取。。。。");
					String requestUrl = QiyeWeixinUtil.access_token_url.replace("ID",appId).replace("SECRET",secret); 
				   //System.out.println(requestUrl);
				   JSONObject jsonObject = QiyeWeixinUtil.httpRequest(requestUrl, "GET", null);  
				   // 如果请求成功  
				   if (null != jsonObject) {  
				       try {
				    	   accessToken = new HashMap<String, String>();
				    	   AccessTokenMap.put(appId, accessToken);
				    	   accessToken.put("access_token", jsonObject.getString("access_token"));
				    	   accessToken.put("time", new Date().getTime() + "");
				    	   return accessToken.get("access_token"); 
				       } catch (JSONException e) { 
				           // 获取token失败  
				           log.error("获取token失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));  
				           return null;
				       }  
				   }else{
					   return null;
				   }
				    
				}else{
					//由于线程不可能暂停很久，所以不用判断时间直接返回access_token
					return AccessTokenMap.get(appId).get("access_token");
				}
			}
		}else{
			String existTime = accessToken.get("time");
			Long leave = new Date().getTime() - Long.parseLong(existTime);
			//token的存活时间是7200000
			if(leave > 7200000){
				synchronized  (single){
					//accessToken有可能是新的
					accessToken = AccessTokenMap.get(appId);
					existTime = accessToken.get("time");
					leave = new Date().getTime() - Long.parseLong(existTime);
					if(leave > 7200000){
						System.out.println("重新获取。。。。。");
					   String requestUrl = QiyeWeixinUtil.access_token_url.replace("ID",appId).replace("SECRET",secret); 
					   //System.out.println(requestUrl);
					   JSONObject jsonObject = QiyeWeixinUtil.httpRequest(requestUrl, "GET", null);  
					   // 如果请求成功  
					   if (null != jsonObject) {  
					       try { 
					    	   accessToken = new HashMap<String, String>();
					    	   AccessTokenMap.put(appId, accessToken);
					    	   accessToken.put("access_token", jsonObject.getString("access_token"));
					    	   accessToken.put("time", new Date().getTime() + "");
					    	   return accessToken.get("access_token");  
					       
					       } catch (JSONException e) {   
					           // 获取token失败  
					           log.error("获取token失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));  
					           return null;
					       }  
					   }else{
						   return null;
					   }
					   
					}else{
						//由于线程不可能暂停很久，所以不用判断时间直接返回access_token
						return accessToken.get("access_token");  
					}
				}
			}else{
				System.out.println("直接返回了。。。。。");
				//直接返回缓存中的有效的access_token
				return AccessTokenMap.get(appId).get("access_token");
			}
	
		}
		
	}
	






}
