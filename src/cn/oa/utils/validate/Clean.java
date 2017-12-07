package cn.oa.utils.validate;


public class Clean {

	private static char[] CHARS = { 
		0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, // 000 - 015 0
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 016 - 031 1
		1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, // 032 - 047 2
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, // 048 - 063 3
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 064 - 079 4
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, // 080 - 095 5
		0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, // 096 - 111 6
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0  // 112 - 127 7
	};
	
	public static boolean clean(String str) {
		boolean clean = true;
		char[] ch = str.toCharArray();
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!pref(c) && !suff(c)) {
				clean = false;
			}
		}
		return clean;
	}

	private static boolean pref(char c) {
		int idx = c;
		return (idx < 128 && CHARS[idx] != 0);
	}
	
	private static boolean suff(char c) {
		return c > 127;
	}
	
	public static boolean number(String str) {
		char[] ch = str.toCharArray();
		int len = ch.length;
		for (int i = 0; i < len; i++) {
			char c = ch[i];
			if (c == '-' && (len > 1 && i == 0)) {
				continue;
			}
			if (!(c >= '0' && c <= '9')) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean floating(String str) {
		char[] ch = str.toCharArray();
		int len = ch.length;
		int point = 0;
		for (int i = 0; i < len; i++) {
			char c = ch[i];
			if (c == '-' && (len > 1 && i == 0)) {
				continue;
			}
			if (c == '.') {
				point++;
				continue;
			}
			if (!(c >= '0' && c <= '9')) {
				return false;
			}
		}
		return point <= 1;
	}
}
