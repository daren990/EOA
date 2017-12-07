package cn.oa.web.filter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.oa.consts.Sessions;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopWechatrelation;
import cn.oa.service.WxClientService;
import cn.oa.service.client.ClientService;
import cn.oa.utils.Asserts;
import cn.oa.utils.DesUtils;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.PathUtil;
import cn.oa.utils.web.Webs;
import cn.oa.web.wx.Config;
import cn.oa.web.wx.InterfacePath;
/**
 * 强制用来让微信回调本方法 ,由于微信回调以get请求方式进行，
 * 因此原本post的方法应该要添加get方式，而且原本请求体中的请求参数，会自动进行加密，从parameter_json参数中获取
 * 加密后的parameter_json的还原，可以调用本类的public方法以实现
 * @author Administrator
 *
 */
public class WechatCallback implements ActionFilter {

	public static Log log = Logs.getLog(WechatCallback.class);

	public static String KEY = "ANDY";
	
	@Override
	public View match(ActionContext ctx) {
		Ioc ioc = ctx.getIoc();
		WxClientService wxClientService = ioc.get(WxClientService.class, "wxClientService");
		
		HttpServletRequest req = ctx.getRequest();
		String org_id = req.getParameter("org_id");
		Asserts.isNull(org_id, "请传入公司的id");
		
		try {
			String redirectUrl = null;
			if("POST".equals(req.getMethod())){
				Map<String, String[]> paremeterMap = req.getParameterMap();
				String parameterJson = getParameterJson(paremeterMap);
				String secrectJson = encrypt(parameterJson);
				Config c = wxClientService.initOrgConfig(org_id);
//				System.out.println(secrectJson);
				redirectUrl = PathUtil.getRequestURL(req)+"?parameter_json="+secrectJson+"%26org_id="+org_id;
				String url = String.format(InterfacePath.AUTH_URL, c.getAPPID(), redirectUrl,"snsapi_base");
//				System.out.println(redirectUrl);
//				System.out.println(url);
				return new ServerRedirectView(url);
			}else if("GET".equals(req.getMethod())){
				String code = req.getParameter("code");
				if(code != null && !"".equals(code)){
					return null;
				}else{
					Config c = wxClientService.initOrgConfig(org_id);
					String queryString = req.getQueryString();
					queryString = queryString.replace("&", "%26");
					redirectUrl = PathUtil.getRequestURL(req)+"?"+queryString;
					String url = String.format(InterfacePath.AUTH_URL, c.getAPPID(), redirectUrl,"snsapi_base");
//					System.out.println(redirectUrl);
//					System.out.println(url);
					return new ServerRedirectView(url);
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return null;
	}
	
	
	private String encrypt(String str) throws Exception{
		DesUtils des = new DesUtils(WechatCallback.KEY);//自定义密钥   
		return des.encrypt(str);
	}
	
	private String getParameterJson(Map<String, String[]> paremeterMap){
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(paremeterMap);
		System.out.println(json);

		return json;
	}
	
	public static String getValue(JSONObject jsonObject, String key){
		String value = jsonObject.getString(key);
		JSONArray jsonArray = JSONArray.fromObject(value);
		if(jsonArray.get(0) != null){
			return (String) jsonArray.get(0);
		}
		return null;
	}
	
	//解密Json并转换成JsonObject
	public static JSONObject getJsonObject(String parameter_json) throws Exception{
		DesUtils des = new DesUtils(WechatCallback.KEY);
		String json = des.decrypt(parameter_json);
//		System.out.println(json);
		JSONObject jsonObject = JSONObject.fromObject(json);
		return jsonObject;
	}
}
