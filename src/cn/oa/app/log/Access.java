package cn.oa.app.log;

import org.joda.time.DateTime;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import cn.oa.utils.Calendars;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;

public class Access {

	public static Log log = Logs.getLog(Access.class);

	public static void log(String message, DateTime now) {
		String ip = Webs.ip(Webs.getReq());
		Integer userId = Context.getUserId();
		log.info("(Access:log) ip: " + ip + ", userId: " + userId + ", message: " + message + ", modify_time: " + now.toString(Calendars.DATE_TIME));
	}
}
