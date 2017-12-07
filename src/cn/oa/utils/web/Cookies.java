package cn.oa.utils.web;

import javax.servlet.http.Cookie;

public class Cookies {

	public static final int MAX_AGE = 60 * 60 * 24 * 7; // 一周
	public static final String PATH = "/";
	public static final String USER_ID = "YAYUN_UID";
	public static final String PWD = "PWD";
	public static final String PASSWD = "YAYUN_PASSWD";
	
	public static Cookie add(String key, String name) {
		Cookie cookie = new Cookie(key, name);
		cookie.setPath(PATH);
		cookie.setMaxAge(MAX_AGE);
		return cookie;
	}

	public static Cookie delete(String key) {
		Cookie cookie = new Cookie(key, "");
		cookie.setMaxAge(0);
		cookie.setPath(PATH);
		return cookie;
	}

	public static final String get(Cookie cookie, String key) {
		try {
			String name = cookie.getName();
			if (name != null && name.equals(key)) {
				return cookie.getValue();
			}
		} catch (Exception e) {
		}
		return null;
	}
}
