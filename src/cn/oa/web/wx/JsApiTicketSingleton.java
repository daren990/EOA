package cn.oa.web.wx;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.nutz.log.Log;
import org.nutz.log.Logs;

import cn.oa.web.action.wx.comm.QiyeWeixinUtil;

public class JsApiTicketSingleton {

	private static final Log log = Logs.getLog(JsApiTicketSingleton.class);
	
	private Map<String,Map<String,String>> c_map = new HashMap<String, Map<String, String>>();

    private JsApiTicketSingleton() {
    }

    private static JsApiTicketSingleton single = null;

    // 静态工厂方法
    public static JsApiTicketSingleton getInstance() {
        if (single == null) {
            single = new JsApiTicketSingleton();
        }
        return single;
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

    public Map<String,Map<String,String>> getMap(Config config,String access_token) {
    	Map<String, String> map = c_map.get(config.getAPPID());
    	String time = null;
		String ticket = null;
		Long nowDate = new Date().getTime();
		boolean h = false;
    	if(map !=null){
    		h = true;
    		time = map.get("time");
    		ticket = map.get("jsapi_ticket");
    	}else{
    		map = new HashMap<String, String>();
    	}
        if (h == true && ticket != null && time != null && nowDate - Long.parseLong(time) < 3000 * 1000) {
        } else {
            String jsapi_ticket = getJsapiTicket(access_token);
            map.put("time", nowDate + "");
            map.put("jsapi_ticket", jsapi_ticket);
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

	public static JsApiTicketSingleton getSingle() {
        return single;
    }

    public static void setSingle(JsApiTicketSingleton single) {
        JsApiTicketSingleton.single = single;
    }
    
   
}
