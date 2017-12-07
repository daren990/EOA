package cn.oa.utils;

public class BorrowNumber {
	public static final String PREF = "BW-";
	public static final int LEN = 7;

	public static String create(Integer borrowId) {
		String blank = "";
		String id = String.valueOf(borrowId);
		for (int i = 0; i < LEN - id.length(); i++) {
			blank += "0";
		}
		return PREF + blank + id;
	}
}
