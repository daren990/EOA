package cn.oa.utils;

public class Chars {

	public static boolean regionMatches(CharSequence str1, boolean ignoreCase, int fromStart, CharSequence str2, int toStart, int length) {
		if (str1 instanceof String && str2 instanceof String)
			return ((String) str1).regionMatches(ignoreCase, fromStart, (String) str2, toStart, length);
		else
			return str1.toString().regionMatches(ignoreCase, fromStart, str2.toString(), toStart, length);
	}
}
