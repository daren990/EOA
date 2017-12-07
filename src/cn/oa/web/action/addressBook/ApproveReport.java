package cn.oa.web.action.addressBook;


import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;

import cn.oa.web.action.Action;
@IocBean(name = "addressBook.approveReport")
@At(value = "addressBook/approveReport")
public class ApproveReport extends Action{
	
	public static Log log = Logs.getLog(AddressBook.class);
	
	@GET
	@At
	@Ok("ftl:addressBook/approveReport")
	public void wxpage(HttpServletRequest req){}
}
