package cn.oa.consts;

public enum WxOpen {
	ERRAND(true,"wxlogin?url=/adm/salary/errand/approve/wxpage"),
	LEAVE_APPLY(true,"wxlogin?url=/adm/salary/leave/approve/wxpage"),
	LEAVE_APPROVE(true,"wxlogin?url=/adm/salary/leave/approve/wxpage"),
	OUTWORK(true,"wxlogin?url=/adm/salary/outwork/approve/wxpage"),
	OVERTIME(true,"wxlogin?url=/adm/salary/overtime/overtime/wxpage");
	//--是否开启微信功能--
	private final boolean OPEN;
	//--图文消息的链接--
	private final String URL;
	private WxOpen(boolean OPEN,String URL){
		this.OPEN = OPEN;
		this.URL = URL;
	}
	public boolean isOPEN() {
		return OPEN;
	}
	public String getURL() {
		return URL;
	}
	
}
