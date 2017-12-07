package cn.oa.app.schedule;

import org.joda.time.DateTime;
import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.oa.app.quartz.Jobs;
import cn.oa.consts.Status;
import cn.oa.service.ShiftService;

/**
 * 定时禁用排班编辑
 * @author SimonTang
 */
public class ShiftJob implements Job {

	private static Log log = Logs.getLog(ShiftJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			Ioc ioc = Jobs.getIoc(context);
			ShiftService shiftService = ioc.get(ShiftService.class);
			DateTime curTime = new DateTime();
			shiftService.changeStatus(null, curTime.plusMonths(1).toString("yyyy-MM"), Status.DISABLED);
		} catch (Exception e) {
			log.error("(ShiftJob:execute) error: ", e);
		}
	}
	
}
