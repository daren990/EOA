package cn.oa.utils.web;

import javax.servlet.http.HttpServletRequest;

import org.nutz.log.Log;
import org.nutz.log.Logs;

import cn.oa.utils.Strings;
import cn.oa.utils.validate.Validator;
import cn.oa.web.action.IndexAction;

public class Https {
	private static final Log log = Logs.getLog(Https.class);

	public static String getStr(HttpServletRequest req, String name, Object... validators) {
		
		String value = Strings.trim(req.getParameter(name));

		return (String) Validator.validate(value, validators);
	}

	public static Integer getInt(HttpServletRequest req, String name, Object... validators) {
		
		String value = Strings.trim(req.getParameter(name));
		value = (String) Validator.validate(value, validators);
		return Strings.isBlank(value) ? null : Integer.valueOf(value);
	}
	
	public static Float getFloat(HttpServletRequest req, String name, Object... validators) {
		
		String value = Strings.trim(req.getParameter(name));
		value = (String) Validator.validate(value, validators);
		return Strings.isBlank(value) ? null : Float.valueOf(value);
	}
	
	public static Double getDouble(HttpServletRequest req, String name, Object... validators) {
		
		String value = Strings.trim(req.getParameter(name));
		value = (String) Validator.validate(value, validators);
		return Strings.isBlank(value) ? null : Double.valueOf(value);
	}
}
