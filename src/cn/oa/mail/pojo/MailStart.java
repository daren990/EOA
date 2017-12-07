package cn.oa.mail.pojo;

import java.util.List;
import java.util.Set;

import cn.oa.consts.Mail;
public class MailStart {

	public String mail(List<String> receiverAddr,String Subject,String content){
		//这个类主要是设置邮件   
	      MailSenderInfo mailInfo = new MailSenderInfo();    
	      mailInfo.setMailServerHost(Mail.MAILSERVERHOST);    
	      mailInfo.setMailServerPort(Mail.MAILSERVERPOST);    
	      mailInfo.setValidate(Mail.VALIDATE);    
	      mailInfo.setUserName(Mail.USERNAME);    
	      mailInfo.setPassword(Mail.PASSWORD);//您的邮箱密码    
	      mailInfo.setFromAddress(Mail.FROMADDRESS);   
	      /*群发*/
	      for (String r:receiverAddr) {
	    	  mailInfo.getToListAddress().add(r);
	  	  }
	      if(mailInfo.getToListAddress().size()<1){
	    	  return Mail.NO_PEOPLE;
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
	      
	      mailInfo.setSubject(Subject);    
	      mailInfo.setContent(content);    
	         //这个类主要来发送邮件   
	      SimpleMailSender sms = new SimpleMailSender();   
	     if(sms.sendTextMails(mailInfo)){//发送普通文本信息
	    	 return Mail.RIGHT;
	     }else{
	    	 return Mail.ERROR;
	     }  	

	}
}
