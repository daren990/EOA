package cn.oa.utils;

import cn.oa.consts.Value;
import cn.oa.utils.validate.Validates;

public class Values {

	public static Integer getInt(Integer value, Integer defaultValue) {
		return value != null ? value : defaultValue;
	}
	public static Double getDouble(Double value, Double defaultValue) {
		return value != null ? value : defaultValue;
	}
	public static Double getDouble(Object value) {
		return value != null ? Double.parseDouble(value.toString()) : Value.D;
	}
	public static Integer getInt(String value, Integer defaultValue) {
		if (Strings.isNotBlank(value) && Validates.integer(value))
			return Integer.valueOf(value);
		return defaultValue;
	}

	public static Integer getInt(Object value) {
		return value != null && Strings.isNotBlank(String.valueOf(value)) ? Integer.valueOf(String.valueOf(value)) : Value.I;
	}
	
	public static Float getFloat(Float value, Float defaultValue) {
		return value != null ? value : defaultValue;
	}
	
	public static String getStr(String value, String defaultValue) {
		return Strings.isNotBlank(value) ? value : defaultValue;
	}
}
