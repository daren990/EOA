package cn.oa.consts;

public class Mail {
	// 发送邮件的服务器的IP和端口   
	public static final String MAILSERVERHOST = "smtp.ym.163.com";
	public static final String MAILSERVERPOST = "25";
	// 是否需要身份验证  
	public static final boolean VALIDATE = true;
	// 登陆邮件发送服务器的用户名和密码   
	public static final String USERNAME = "hede@hvedu.cn";
	public static final String PASSWORD = "123456";
	// 邮件发送者的地址 
	public static final String FROMADDRESS = "groupoa@gzsensoft.com";
	// 邮件主题
	public static final String SUBJECT = "设置邮箱标题";
	// 邮件的文本内容    
	public static final String CONTENT = "设置邮箱内容 如中国桂花网 是中国最大桂花网站==";
	
	public static final String NO_PEOPLE = "邮件没有接收人";
	public static final String RIGHT = "邮件发送成功";
	public static final String ERROR = "邮件发送失败";
}
