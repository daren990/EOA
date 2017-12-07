package cn.oa.service;

import java.util.Date;

import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.model.ToolsSmsInfo;
import cn.oa.repository.Mapper;
import cn.oa.utils.SmsUtil;
import cn.oa.web.Context;

@IocBean(name="toolsService")
public class ToolsService {


	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	
	
	public int sendSmsForIHuYi(String clientPhone,String smsCode){
		String text = "您的验证码是："+smsCode+"。请不要把验证码泄露给其他人。";
		int code = SmsUtil.sendIHuYi(clientPhone, text);
		ToolsSmsInfo sms = new ToolsSmsInfo();
		sms.setReturnCode(code);
		sms.setCreateTime(new Date());
		sms.setSmsFrom(1);
		sms.setText(text);
		sms.setPhone(clientPhone);
		sms.setType(1);
		dao.insert(sms);
		return code;
	}
}
