package cn.oa.utils.validate;

public class R {

	public static final Integer CLEAN = 0;		// XSS过滤
	public static final Integer REQUIRED = 1;	// 必填
	
	public static final Integer I = 2;			// 整数值
	public static final Integer L = 3;			// 长整数值
	public static final Integer F = 4;			// 浮点数值
	public static final Integer E = 5;			// 邮箱
	public static final Integer D = 6;			// 日期 (yyyy-MM-dd)
	public static final Integer T = 7;			// 时间 (HH:mm)
	public static final Integer DT = 8;			// 日期时间 (yyyy-MM-dd HH:mm)
	public static final Integer yyyy = 9;		// 年 (yyyy)
	public static final Integer MM = 10;		// 月(MM)
	public static final Integer dd = 11;		// 日 (dd)
	public static final Integer HH = 12;		// 小时 (HH)
	public static final Integer mm = 13;		// 分钟 (mm)
	
	public static final Integer REGEX = 14;		// 正则
	public static final Integer RANGE = 15;		// 区间
	public static final Integer EQ = 16;		// 值相等
	public static final Integer IN = 17;		// 集合
	public static final Integer NONE = -1;		// 可为空
	
	public static final String USERNAME = "regex:^[a-zA-Z][a-zA-Z0-9_]+$:只能由字母、数字或下划线组成";
	public static final String PASSWORD = "regex:^[a-zA-Z0-9_.%]+$:只能由字母、数字或下划线组成";
}
