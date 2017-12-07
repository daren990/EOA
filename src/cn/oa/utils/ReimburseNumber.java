package cn.oa.utils;

public class ReimburseNumber {
	public static final String PREF = "RE-";
	public static final int LEN = 7;

	public static String create(Integer reimburseId) {
		String blank = "";
		String id = String.valueOf(reimburseId);
		for (int i = 0; i < LEN - id.length(); i++) {
			blank += "0";
		}
		return PREF + blank + id;
	}
}
