package cn.oa.app.quartz;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

public class Jobs {

	public static Log log = Logs.getLog(Jobs.class);
	public static SchedulerFactory sf = new StdSchedulerFactory();

	private static final String HANDLE_IOC = "org.nutz.ioc.Ioc";
	
	public static void start(Ioc ioc, int id, String name, String group, String cron, Class<? extends Job> jobClass) {
		try {
			Scheduler scheduler = sf.getScheduler();
			JobDetail job = JobBuilder.newJob(jobClass)
					.withIdentity("job_" + name + "_" + id, "group_" + group + "_" + id)
					.requestRecovery()
					.build();
			CronTrigger trigger = TriggerBuilder.newTrigger()
					.withIdentity("trigger_" + name + "_" + id, "group_" + group + "_" + id)
					.withSchedule(CronScheduleBuilder.cronSchedule(cron))
					.build();
			scheduler.scheduleJob(job, trigger);
			scheduler.getContext().put(HANDLE_IOC, ioc);

			scheduler.start();
		} catch (Exception e) {
			log.error("(Jobs:start) error: ", e);
		}
	}
	
	public static void stop(int id, String name, String group) {
		try {
			if (sf != null) {
				Scheduler scheduler = sf.getScheduler();
				TriggerKey key = TriggerKey.triggerKey("trigger_" + name + "_" + id, "group_" + group + "_" + id);
				Trigger trigger = scheduler.getTrigger(key);
				if (trigger != null) {
					scheduler.pauseTrigger(key);
					scheduler.unscheduleJob(key);
					scheduler.deleteJob(trigger.getJobKey());
				}
			}
		} catch (Exception e) {
			log.error("(Jobs:stop) error: ", e);
		}
	}

	public static Ioc getIoc(JobExecutionContext context) {
		try {
			return (Ioc) context.getScheduler().getContext().get(HANDLE_IOC);
		} catch (SchedulerException e) {
			log.error("(Jobs:getIoc) error: ", e);
		}
		return null;
	}
}
