package cn.oa.utils;

import java.util.Random;

public class Rnds {

	public static final String ALPHA_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	public static final String ALPHA_NUMBER = "1234567890";
	
	public static String getStr(int length) {
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(ALPHA_STRING.length());
			sb.append(ALPHA_STRING.charAt(number));
		}
		return sb.toString();
	}
	
	public static String getNum(int length) {
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(ALPHA_NUMBER.length());
			sb.append(ALPHA_NUMBER.charAt(number));
		}
		return sb.toString();
	}
	
	public static String getStr(String alphaString, String split) {
		if (Strings.isBlank(alphaString)) {
			return "";
		}
		Random random = new Random();
		String[] tokens = Strings.splitIgnoreBlank(alphaString, split);
		int number = random.nextInt(tokens.length);
		return tokens[number];
	}
}
