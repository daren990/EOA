package cn.oa.web.wx;

public class Merchant {

	private String MERCHANTID;
	private String MERCHANTSECRET;

	public Merchant(String merchantid, String merchantsecret) {
		this.MERCHANTID = merchantid;
		this.MERCHANTSECRET = merchantsecret;
	}

	public String getMERCHANTID() {
		return MERCHANTID;
	}

	public void setMERCHANTID(String mERCHANTID) {
		MERCHANTID = mERCHANTID;
	}

	public String getMERCHANTSECRET() {
		return MERCHANTSECRET;
	}

	public void setMERCHANTSECRET(String mERCHANTSECRET) {
		MERCHANTSECRET = mERCHANTSECRET;
	}

}
