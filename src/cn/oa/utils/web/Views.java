package cn.oa.utils.web;

import cn.oa.consts.Value;
import cn.oa.utils.MapBean;

public class Views {

	public static void ok(MapBean mb, String message) {
		mb.put("code", Value.T);
		mb.put("message", message);
	}

	public static void error(MapBean mb, String message) {
		mb.put("code", Value.F);
		mb.put("message", message);
	}
}
