package cn.oa.mail.pojo;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

import cn.oa.consts.Mail;

public class mailpojo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		  //这个类主要是设置邮件   
	      MailSenderInfo mailInfo = new MailSenderInfo();    
	      mailInfo.setMailServerHost(Mail.MAILSERVERHOST);    
	      mailInfo.setMailServerPort(Mail.MAILSERVERPOST);    
	      mailInfo.setValidate(Mail.VALIDATE);    
	      mailInfo.setUserName(Mail.USERNAME);    
	      mailInfo.setPassword(Mail.PASSWORD);//您的邮箱密码    
	      mailInfo.setFromAddress(Mail.FROMADDRESS);   
	      /*群发*/
	      String[] receiverAddr = {"909743558@qq.com","752893896@qq.com"};
	      int receiverLen = receiverAddr.length;
	      for (int i = 0; i < receiverLen; i++) {
	    	  mailInfo.getToListAddress().add(receiverAddr[i]);
	  	  }
	      StringBuffer sb = new StringBuffer();
	      int l = mailInfo.getToListAddress().size();
	      int count = 0;
	      for(String s:mailInfo.getToListAddress()){	
	    	  count++;
	    	  sb.append(s);
	    	 if(count < l){
	    		 sb.append(",");
	    	 }
	      }
	      mailInfo.setToAddress(sb.toString());
	      mailInfo.setSubject(Mail.SUBJECT);    
	      mailInfo.setContent(Mail.CONTENT);    
	         //这个类主要来发送邮件   
	      SimpleMailSender sms = new SimpleMailSender();   
	      sms.sendTextMails(mailInfo);//发送文体格式    
	      /*sms.sendHtmlMail(mailInfo);*///发送html格式   
	      
	}

	
/*	public static void main(String[] args) throws Exception {
		Properties pro = new Properties();
		pro.put("mail.smtp.auth", "true");
		pro.put("mail.smtp.host", "smtp.exmail.qq.com");
		pro.put("mail.transport.protocol", "smtp");
		pro.put("username", "groupoa@gzsensoft.com");
		pro.put("password", "oa123456");
	          
	        根据配置文件生成一个session环境对象  
	        Session session=Session.getInstance(pro);  
	        使用smtp协议获取session环境的Transprot对象来发送邮件 javamial使用Transport对象来管理发送邮件服务  
	        Transport tran=session.getTransport("smtp");  
	        链接邮箱服务器，host是你的邮箱服务器 如：sina.smtp.com  
	        tran.connect(pro.getProperty("mail.smtp.host"),pro.getProperty("username"),pro.getProperty("password"));  
	        创建一个扩展信息对象，用来包装要发送的多媒体信息格式，也可以只是简单的文本  
	        MimeMessage msg=new MimeMessage(session);  
	        设置Content 浏览器解析编码和格式等  
	        msg.setContent("xxxxxxxxx","text/html;charset=utf-8");  
	        设置内容体 这里仅仅是简单的html文本  
	        msg.setSubject("sssssssssssss");  
	        设置发送邮件方 地址  
	        msg.setFrom(new InternetAddress(pro.getProperty("username")));  
	        SendMessage方法第一个参数是邮件对象，第二个是发送的邮件地址数组。InternetAddress.parse(address)方法解析返回一个地址数组  
	        tran.sendMessage(msg,InternetAddress.parse("909743558@qq.com"));  
	        tran.close(); 
	}*/
}
