package cn.oa.web.wx;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.nutz.log.Log;
import org.nutz.log.Logs;

import cn.oa.web.action.wx.comm.QiyeWeixinUtil;

public class Access_TokenSingleton {

	private static final Log log = Logs.getLog(Access_TokenSingleton.class);
	
	private Map<String,Map<String,String>> c_map = new HashMap<String, Map<String, String>>();

    private Access_TokenSingleton() {
    }

    private static Access_TokenSingleton single = null;

    // 静态工厂方法
    public static Access_TokenSingleton getInstance() {
        if (single == null) {
            single = new Access_TokenSingleton();
        }
        return single;
    }
    
    /**
	 * 获取ACCESS_TOKEN
	 */
	public String getACCESS_TOKEN(Config config) {
		StringBuilder sb = new StringBuilder(InterfacePath.ACCESS_TOKEN_URL);
		sb.append("&grant_type=client_credential");
		sb.append("&appid=").append(config.getAPPID());
		sb.append("&secret=").append(config.getSECRET());
		JSONObject jsonObject = QiyeWeixinUtil.httpRequest(sb.toString(),"GET", null);
		log.debug(jsonObject);
		String access_token = null;
		if (jsonObject.containsKey("access_token")) {
			access_token = jsonObject.getString("access_token");
		}
		return access_token != null ? access_token : null;
	}
	
	//获取全局的access_token的方法
    public Map<String,Map<String,String>> getMap(Config config) {
    	Map<String, String> map = c_map.get(config.getAPPID());
    	String time = null;
		String accessToken = null;
		Long nowDate = new Date().getTime();
		boolean h = false;
    	if(map !=null){
    		h = true;
    		time = map.get("time");
    		accessToken = map.get("access_token");
    	}else{
    		map = new HashMap<String, String>();
    	}
        if (h == true && accessToken != null && time != null && nowDate - Long.parseLong(time) < 3000 * 1000) {
        } else {
            String access_token = getACCESS_TOKEN(config);
            map.put("time", nowDate + "");
            map.put("access_token", access_token);
            c_map.remove(config.getAPPID());
            c_map.put(config.getAPPID(), map);
        }
        
        return c_map;
    }

    public Map<String, Map<String, String>> getC_map() {
		return c_map;
	}

	public void setC_map(Map<String, Map<String, String>> c_map) {
		this.c_map = c_map;
	}

	public static Access_TokenSingleton getSingle() {
        return single;
    }

    public static void setSingle(Access_TokenSingleton single) {
        Access_TokenSingleton.single = single;
    }
    
    /**
	 * 获取JsapiTicket
	 */
	public String getJsapiTicket(String acces_token) {
		StringBuilder sb = new StringBuilder(InterfacePath.JSAPITICKET);
		sb.append("&access_token=").append(acces_token);
		sb.append("&type=jsapi");
		JSONObject jsonObject = QiyeWeixinUtil.httpRequest(sb.toString(),"GET", null);
		log.debug(jsonObject);
		String ticket = null;
		if (jsonObject.containsKey("ticket")) {
			ticket = jsonObject.getString("ticket");
		}
		return ticket != null ? ticket : null;
	}
}
