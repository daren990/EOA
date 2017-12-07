package cn.oa.utils.validate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import cn.oa.utils.Strings;

public class Validates {

	public static boolean clean(Object target) {
		if (isTarget(target))
			if (!Clean.clean((String) target))
				return false;
		return true;
	}

	public static boolean required(Object target) {
		if (target == null || (target instanceof String && Strings.isBlank((String) target)))
			return false;
		return true;
	}

	public static boolean integer(Object target) {
		return number(target, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public static boolean biginteger(Object target) {
		return number(target, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	private static boolean number(Object target, Object min, Object max) {
		if (isTarget(target)) {
			String str = (String) target;
			if (Clean.number(str)) {
				boolean opt = Strings.startsWith(str, "-");
				int len = String.valueOf(max).length() + (opt ? 1 : 0);
				if (str.length() < len)
					return true;
				if (str.length() == len && str.compareTo(String.valueOf((opt ? min : max))) <= 0)
					return true;
			}
			return false;
		}
		return true;
	}

	public static boolean floating(Object target) {
		return floating(target, Float.MIN_VALUE, Float.MAX_VALUE);
	}
	
	public static boolean floating(Object target, Object min, Object max) {
		if (isTarget(target)) {
			String str = (String) target;
			if (Clean.floating(str)) {
				boolean opt = Strings.startsWith(str, "-");
				int len = String.valueOf(max).length() + (opt ? 1 : 0);
				if (str.length() < len)
					return true;
				if (str.length() == len && str.compareTo(String.valueOf((opt ? min : max))) <= 0)
					return true;
			}
			return false;
		}
		return true;
	}
	
	public static boolean date(Object target) {
		return formatter(target, "yyyy-MM-dd");
	}
	
	public static boolean time(Object target) {
		return formatter(target, "HH:mm");
	}
	
	public static boolean datetime(Object target) {
		return formatter(target, "yyyy-MM-dd HH:mm");
	}
	
    public static boolean formatter(Object target, String pattern) {
        if (isTarget(target)) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
            try {
                fmt.parseDateTime((String) target);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

	public static boolean equals(Object target, String value) {
		if (isTarget(target))
			return Strings.equals((String) target, value);
		return true;
	}

	public static boolean in(Object target, Object[] objects) {
		if (objects == null || objects.length == 0)
			return false;
		if (target == null)
			return false;
		for (Object object : objects) {
			if (object.equals(target))
				return true;
		}
		return false;
	}
	
	public static boolean email(Object target) {
		if (isTarget(target))
			return Mail.validate((String) target);
		return true;
	}

	public static boolean length(Object target, Integer[] range) {
		if (target != null && target instanceof String && !Strings.isBlank((String) target)) {
			int min = 0;
			int max = Integer.MAX_VALUE;
			if (range.length >= 1)
				min = range[0];
			if (range.length >= 2)
				max = range[1];

			String str = (String) target;
			if (str.length() < min || str.length() > max)
				return false;
		}

		return true;
	}

	public static boolean size(Object target, Integer[] range) {
		if (target != null) {
			int min = 0;
			int max = Integer.MAX_VALUE;
			if (range.length >= 1)
				min = range[0];
			if (range.length >= 2)
				max = range[1];

			Integer i = null;
			if (target instanceof String)
				if (Clean.number((String) target))
					i = Integer.valueOf((String) target);
				else
					return false;
			else if (target instanceof Integer)
				i = (Integer) target;

			if (i != null && (i < min || i > max))
				return false;
		}

		return true;
	}
	
	public static boolean match(Object target, String regex) {
		if (isTarget(target)) {
			return pattern(target, regex).matches();
		}
		return true;
	}
	
	public static boolean not_match(Object target, String regex) {
		if (isTarget(target)) {
			return !pattern(target, regex).matches();
		}
		return true;
	}
	
	private static Matcher pattern(Object target, String regex) {
		final Matcher m = Pattern.compile(regex, Pattern.MULTILINE + Pattern.DOTALL).matcher((String) target);
		return m;
	}

	public static boolean isTarget(Object target) {
		return target != null && (target instanceof String && !Strings.isBlank((String) target));
	}
}
