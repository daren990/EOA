package cn.oa.utils.validate;

import cn.oa.utils.Converts;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;

public class Validator {

	public static Integer getInt(Object target, Object... validators) {
		String value = (String) validate(target, validators);
		return Strings.isBlank(value) ? null : Integer.valueOf(value);
	}
	
	public static String getStr(Object target, Object... validators) {
		return (String) validate(target, validators);
	}
	
	public static Object validate(Object target, Object... validators) {
		if (validators != null && validators.length > 0) {
			String message = null;
			String eq = null;
			String regex = null;
			String error = null;
			boolean matche = false;
			Integer[] range = null;
			String[] in = null;
			
			for (Object validator : validators) {
				if (validator instanceof String) {
					String value = (String) validator;
					if (Strings.startsWith(value, "{") && Strings.endsWith(value, "}")) {
						String r = Strings.removeEnd(Strings.removeStart(value, "{"), "}");
						range = Converts.array(r, ",");
					} else if (Strings.startsWith(value, "eq:")) {
						eq = Strings.removeStart(value, "eq:");
					} else if (Strings.startsWith(value, "in [") && Strings.endsWith(value, "]")) {
						String i = Strings.removeEnd(Strings.removeStart(value, "in ["), "]");
						in = Strings.splitIgnoreBlank(i);
					} else if (Strings.startsWith(value, "regex:")) {
						String r = Strings.removeStart(value, "regex:");
						regex = Strings.before(r, ":");
						matche = true;
						error = Strings.after(r, ":");
					} else if (Strings.startsWith(value, "!regex:")) {
						String r = Strings.removeStart(value, "!regex:");
						regex = Strings.before(r, ":");
						matche = false;
						error = Strings.after(r, ":");
					} else {
						message = value;
					}
				}
			}

			boolean throwable = false;

			if (!Strings.isBlank(message) || !Strings.isBlank(error))
				throwable = true;
			if (Strings.isBlank(message))
				message = "";
			if (Strings.isBlank(error))
				error = "";

			boolean n = false;
			
			for (Object validator : validators) {
				if (validator instanceof Integer) {
					if (validator == R.REQUIRED && !Validates.required(target))
						return throwable(throwable, message + "不能为空值");
					else if (validator == R.I) {
						n = true;
						if (!Validates.integer(target))
							return throwable(throwable, message + "不是合法的整数值");
					} else if (validator == R.L) {
						n = true;
						if (!Validates.biginteger(target))
							return throwable(throwable, message + "不是合法的长整数值");
					} else if (validator == R.F) {
						n = true;
						if (!Validates.floating(target))
							return throwable(throwable, message + "不是合法的浮点数值");
					}
					else if (validator == R.E && !Validates.email(target))
						return throwable(throwable, message + "不是合法的邮件格式");
					else if (validator == R.D && !Validates.date(target))
						return throwable(throwable, message + "不是合法的日期");
					else if (validator == R.T && !Validates.time(target))
						return throwable(throwable, message + "不是合法的时间");
					else if (validator == R.DT && !Validates.datetime(target))
						return throwable(throwable, message + "不是合法的日期时间");
					else if (validator == R.yyyy && !Validates.formatter(target, "yyyy"))
						return throwable(throwable, message + "不是合法的年份");
					else if (validator == R.MM && !Validates.formatter(target, "MM"))
						return throwable(throwable, message + "不是合法的月份");
					else if (validator == R.dd && !Validates.formatter(target, "dd"))
						return throwable(throwable, message + "不是合法的日期");
					else if (validator == R.HH && !Validates.formatter(target, "HH"))
						return throwable(throwable, message + "不是合法的小时");
					else if (validator == R.mm && !Validates.formatter(target, "mm"))
						return throwable(throwable, message + "不是合法的分钟");
					else if (validator == R.RANGE) {
						if (n) {
							if (!Validates.size(target, range))
								return throwable(throwable, message + "的值只能介于" + range[0] + "到" + range[1] + "之间");
						} else {
							if (!Validates.length(target, range))
								return throwable(throwable, message + "的字符长度只能介于" + range[0] + "到" + range[1] + "之间");
						}
					} else if (validator == R.EQ && !Validates.equals(target, eq))
						return throwable(throwable, message + "不一致");
					else if (validator == R.IN && !Validates.in(target, in))
						return throwable(throwable, message + "不在集合范围内");
					else if (validator == R.CLEAN && !Validates.clean(target))
						return throwable(throwable, message + "不是合法值");
					else if (validator == R.REGEX) {
						if (matche) {
							if (!Validates.match(target, regex)) {
								return throwable(throwable, message + error);
							}
						} else {
							if (!Validates.not_match(target, regex)) {
								return throwable(throwable, message + error);
							}
						}
					}
				}
			}
		}
		
		return target;
	}
	
	private static Object throwable(boolean throwable, String message) {
		if (throwable)
			throw new Errors(message);
		return null;
	}
}
