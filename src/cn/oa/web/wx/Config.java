package cn.oa.web.wx;

public class Config {
	private String APPID;
	private String SECRET;
	
	public Config(String APPID,String SECRET){
		this.APPID = APPID;
		this.SECRET = SECRET;
	}
	
	public String getAPPID() {
		return APPID;
	}
	public void setAPPID(String aPPID) {
		APPID = aPPID;
	}
	public String getSECRET() {
		return SECRET;
	}
	public void setSECRET(String sECRET) {
		SECRET = sECRET;
	}
	
	
}
