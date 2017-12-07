package cn.oa.web.action.flow.form.custom;

import org.nutz.log.Log;
import org.nutz.log.Logs;

public class CustomProcesser {
	
	public static Log log = Logs.getLog(CustomProcesser.class);

	public String exec(String message) {
		log.info("(CustomProcesser:exec) message: " + message);
		return "result: " + message;
	}
}
