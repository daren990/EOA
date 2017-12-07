package cn.oa.utils;


import cn.oa.consts.Value;

public class RMB {

	public static final int PER = 100;
	public static final int PER1 = 1;

	public static Integer on(Float value) {
		return (int) (Values.getFloat(value, 0.0f) * PER);
	}

	public static float off(Integer value) {
		return Values.getInt(value, Value.I) / (PER * 1.0f);
	}
	
	public static Double offDouble(Double value) {
		return Values.getDouble(value, Value.D) / (PER * 1.0d);
	}
	
	public static Double parseMinute(Double value) {
		return Values.getDouble(value, Value.D) * (PER1 * 1.0d);
	}
	
	public static Double toMinute(Double value) {
		return Values.getDouble(value, Value.D) * (PER * 1.0d);
	}
	
	public static Integer toMinute(Float value) {
		return (int) (Values.getFloat(value, 0.0f) * PER);
	}
	public static Integer toMinute(Float value,Integer i) {
		return Math.round((Values.getFloat(value, 0.0f) * PER));
	}
	public static float toyuan(Integer value) {
		return Values.getInt(value, Value.I) / (PER * 1.0f);
	}
}
