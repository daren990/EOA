package cn.oa.web.action.wx;


public class dfmain {
	public static void main(String[] args) {
		WxSendService wx = new WxSendService();
		wx.sendarticle("@all", "", "", "2", "发工资", "发工资了!", "", "");
	}
}
