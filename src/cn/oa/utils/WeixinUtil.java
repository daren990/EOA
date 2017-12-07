package cn.oa.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.http.HttpServletResponse;

public class WeixinUtil {

	/**
     * @Description：sign签名
     * @param characterEncoding 编码格式
     * @param parameters 请求参数
     * @param partenrKey 微信商户号付款唯一标识
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static String createSign(String characterEncoding, SortedMap<String, Object> parameters, String partenrKey) {

        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + partenrKey);
        String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }

    public static String CreateNoncestr() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String res = "";
        for (int i = 0; i < 16; i++) {
            Random rd = new Random();
            res += chars.charAt(rd.nextInt(chars.length() - 1));
        }
        return res;
    }
    
    /**
	 * 返回给微信服务的消息
	 * @param returnCode
	 * @param returnMsg
	 * @return
	 */
	public static String setXML(String returnCode, String returnMsg) {
		return "<xml><return_code><![CDATA[" + returnCode
				+ "]]></return_code><return_msg><![CDATA[" + returnMsg
				+ "]]></return_msg></xml>";
	}
	
	/**
	 * 返回给微信的消息
	 * @param code SUCCESS/FAIL
	 * @param msg
	 * @return
	 * @throws IOException 
	 */
	public static void respToWeixin(HttpServletResponse response, String code, String msg) throws IOException {
		response.getWriter().write("<xml><return_code><![CDATA[" + code
				+ "]]></return_code><return_msg><![CDATA[" + msg
				+ "]]></return_msg></xml>");
	}
	
}
