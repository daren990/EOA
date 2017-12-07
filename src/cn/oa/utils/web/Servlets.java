package cn.oa.utils.web;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletRequest;

public class Servlets {
	//获取指定前缀请求的方法
	public static Map<String, Object> startsWith(ServletRequest req, String prefix) {
		Enumeration<?> names = req.getParameterNames();
		Map<String, Object> params = new TreeMap<String, Object>();
		if (prefix == null) {
			prefix = "";
		}
		while (names != null && names.hasMoreElements()) {
			String name = (String) names.nextElement();
			if ("".equals(prefix) || name.startsWith(prefix)) {
				String unprefixed = name.substring(prefix.length());
				String[] values = req.getParameterValues(name);
				if (values == null || values.length == 0) {
					// Do nothing, no values found at all.
				} else if (values.length > 1) {
					params.put(unprefixed, values);
				} else {
					params.put(unprefixed, values[0]);
				}
			}
		}
		return params;
	}
}
