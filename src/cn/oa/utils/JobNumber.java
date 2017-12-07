package cn.oa.utils;

public class JobNumber {
	public static final String PREF = "SEN";
	public static final int LEN = 6;
	public static final int NUMBER = 3;

	public static String create(Integer userId) {
		String blank = "";
		String id = String.valueOf(userId);
		for (int i = 0; i < LEN - id.length(); i++) {
			blank += "0";
		}
		return PREF + blank + id;
	}
	
	public static String add(Integer jobPre, Integer amount){
		String blank = "";
		String pre = String.valueOf(jobPre);
		String suffix = String.valueOf(amount);
		for(int i = 0; i< NUMBER - suffix.length(); i++){
			blank +="0";
		}
		return pre + blank + suffix;
	}
}
