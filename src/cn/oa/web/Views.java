package cn.oa.web;

import java.util.Date;

import org.joda.time.DateTime;
import org.nutz.lang.Strings;

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.RMB;
import cn.oa.utils.Values;

public class Views {

	public String status(Integer status) {
		status = Values.getInt(status, Status.DISABLED);
		if (status.equals(Status.ENABLED))
			return "启用";
		return "禁用";
	}
	
	public String approve(Integer approve) {
		approve = Values.getInt(approve, Status.PROOFING);
		if (approve.equals(Status.APPROVED))
			return "已批准";
		else if (approve.equals(Status.UNAPPROVED))
			return "未批准";
		return "待审批";
	}
	
	public String cron(String cron, Date start, Date end) {
		if (Strings.isBlank(cron)) return null;
		
		String[] arr = Strings.splitIgnoreBlank(cron, " ");
		if (arr.length != 3) return null;
		
		String cronStr = null;
		if (arr[0].equals("*") && arr[1].equals("*") && arr[2].equals("*")) {
			cronStr = "每天" + fmt(start, "HH:mm") + "至" + fmt(end, "HH:mm");
		} else if (!arr[1].equals("*")) {
			cronStr = "每周" + fmt(start, "E") + fmt(start, "HH:mm") + "至" + fmt(end, "HH:mm");
		} else if (!arr[0].equals("*")) {
			cronStr = "每月" + fmt(start, "dd") + "日" + fmt(start, "HH:mm") + "至" + fmt(end, "HH:mm");
		}
		
		return cronStr;
	}
	
	public double day(Integer minute, Integer works) {
		return minute / (works * 1.0d);
	}
	
	public double hour(Integer minute) {
		return minute / 60.0d;
	}
	
	public String fmt(Date date, String fmt) {
		return new DateTime(date).toString(fmt);
	}
	
/*	public float rmb(Integer money) {
		return RMB.off(money);
	}*/
	
	public Double rmb(Double money) {
		return RMB.offDouble(money);
	}
	
	public boolean hasAnyRole(String role, String[] roles) {
		return Asserts.hasAny(role, roles);
	}

	public String role(String name) {
		return Roles.getDesc(name);
	}
	public String date(Date date) {
		if(date == null)
			return "";
		else
			return Calendars.str(date, Calendars.DATE);
	}
}
