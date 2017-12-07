package cn.oa.utils;

public class AssetNumber {
	public static final String PREF = "RS-";
	public static final int LEN = 6;

	public static String create(Integer assetId) {
		String blank = "";
		String id = String.valueOf(assetId);
		for (int i = 0; i < LEN - id.length(); i++) {
			blank += "0";
		}
		return PREF + blank + id;
	}
}
