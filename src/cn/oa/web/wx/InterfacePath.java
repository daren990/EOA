package cn.oa.web.wx;

public class InterfacePath {
	public static final String JSAPI = "JSAPI";

	//public static final Object PAY_NOTIFY_URL_WECHAT = "http://www.yayunbus.com/groupoa/pay/payCallBack";
	public static final Object PAY_NOTIFY_URL_WECHAT = "http://kaitest.tunnel.qydev.com/wx/pay/payCallBack";

	public static final String GATEWAY_WECHAT_PAY = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	
	// 请求授权url
	public static String AUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=STATE#wechat_redirect";

	// 获取access token url(openid);
	public static String NET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?";

	// 获取 access_token
	public static String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?";

	// 获取微信用户信息
	public static String USER_INTO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?";

	// 发送模板消息
	public static final String SEND_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?";
	public static final String SEND_MESSAGE_URL_ANDY = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN";
	
	//JSTICKET
	public static final String JSAPITICKET = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?";
}
