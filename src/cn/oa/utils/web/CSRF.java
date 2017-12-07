package cn.oa.utils.web;

import javax.servlet.http.HttpServletRequest;

import cn.oa.consts.Value;
import cn.oa.utils.Rnds;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;

public class CSRF {

	private static final String TOKEN = "token";
	private static final String ERROR = "您访问的页面已过期，请刷新重试！";
	
	public static String generate(HttpServletRequest req) {
		String link = req.getServletPath();
//		System.out.println(link);
		String mapper = mapper(link);
		String code = Rnds.getStr(32);

		req.getSession().setAttribute(TOKEN + "_" + mapper, code);
		req.setAttribute(TOKEN, code);

		return code;
	}

	public static void generate(HttpServletRequest req, String link, String token) {
		String mapper = mapper(link);
		req.getSession().setAttribute(TOKEN + "_" + mapper, token);
	}
	
	public static void validate(HttpServletRequest req) {
		String client = Https.getStr(req, "token", R.CLEAN, R.REQUIRED, R.RANGE, "{1,32}");
		validate(req, client);
	}
	
	public static void validate(HttpServletRequest req, String token) {
		String link = req.getServletPath();
		String mapper = mapper(link);
		String server = (String) req.getSession().getAttribute(TOKEN + "_" + mapper);
		if (Strings.isBlank(token) || !Strings.equals(token, server)) {
			throw new Errors(ERROR);
		}
	}
	
	private static String mapper(String link) {
		String[] split = Strings.splitIgnoreBlank(link, "/");

		String mapper = Value.S;

		if (split.length > 2) {
			String module = split[0];
			String action = split[1];
			String m = split[2];
			mapper = module + "_" + action + "_" + m;
		}
		if (split.length == 1) {
			mapper = split[0];
		}

		return mapper;
	}
	
}
