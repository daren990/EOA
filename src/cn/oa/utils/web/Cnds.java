package cn.oa.utils.web;

import org.nutz.dao.sql.Criteria;

import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;

public class Cnds {

	public static void like(Criteria cri, MapBean mb, String field, String key, String value) {
		if (Strings.isNotBlank(key) && Strings.isNotBlank(value)) {
			cri.where().and(field, "like", "%" + value + "%");
			mb.put(key, value);
		}
	}
	
	public static void eq(Criteria cri, MapBean mb, String field, String key, String value) {
		if (Strings.isNotBlank(key) && Strings.isNotBlank(value)) {
			cri.where().and(field, "=", value);
			mb.put(key, value);
		}
	}
	
	public static void eq(Criteria cri, MapBean mb, String field, String key, Integer value) {
		if (Strings.isNotBlank(key) && value != null) {
			cri.where().and(field, "=", value);
			mb.put(key, value);
		}
	}
	
	public static void lte(Criteria cri, MapBean mb, String field, String key, String value) {
		if (Strings.isNotBlank(key) && Strings.isNotBlank(value)) {
			cri.where().and(field, "<=", value + " 23:59:59");
			mb.put(key, value);
		}
	}
	public static void lte2(Criteria cri, MapBean mb, String field, String key, String value) {
		if (Strings.isNotBlank(key) && Strings.isNotBlank(value)) {
			cri.where().and(field, "<=", value + " 00:00:00");
			mb.put(key, value);
		}
	}
	
	public static void gte(Criteria cri, MapBean mb, String field, String key, String value) {
		if (Strings.isNotBlank(key) && Strings.isNotBlank(value)) {
			cri.where().and(field, ">=", value + " 00:00:00");
			mb.put(key, value);
		}
	}
	public static void gte2(Criteria cri, MapBean mb, String field, String key, String value) {
		if (Strings.isNotBlank(key) && Strings.isNotBlank(value)) {
			cri.where().and(field, ">=", value + " 23:59:59");
			mb.put(key, value);
		}
	}
}
