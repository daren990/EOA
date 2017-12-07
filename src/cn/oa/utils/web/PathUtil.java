package cn.oa.utils.web;

import javax.servlet.http.HttpServletRequest;

public class PathUtil {
	
	/**
	 * 返回ip地址加端口
	 * @return 120.0.1:8080
	 */
	public static String getBasePath(HttpServletRequest request){
    	String path = request.getContextPath();
		int port = request.getServerPort();
		String basePath = request.getScheme() + "://" + request.getServerName();
		if (port != 80) {
			basePath += ":" + port + path;
		} else {
			basePath += path;
		}
		return basePath;
    }
	
	public static String getRequestURL(HttpServletRequest request) {
		return request.getRequestURL().toString(); 
	}
}
