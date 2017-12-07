package cn.oa.app.schedule;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.oa.app.quartz.Jobs;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Meet;
import cn.oa.utils.Calendars;
import cn.oa.utils.Strings;

public class MeetJob implements Job {

	private static Log log = Logs.getLog(MeetJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			Ioc ioc = Jobs.getIoc(context);
			Dao dao = ioc.get(Dao.class);
			List<Meet> meets = dao.query(Meet.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("cron", "is not", null));
			
			DateTime now = new DateTime();
			
			for (Meet e : meets) {
				String cron = e.getCron();
				if (Strings.isBlank(cron)) continue;
				
				String[] arr = Strings.splitIgnoreBlank(cron, " ");
				if (arr.length != 3) continue;
				
				String day = arr[0];
				String week = arr[1];

				if (!week.equals("*")) {
					if (!week.equals(now.toString("e"))) continue;
				} else if (!day.equals("*")) {
					if (!day.equals(now.toString("d"))) continue;
				}
				
				String meet_yyyyMMdd = now.toString(Calendars.DATE);
				String start_HHmm = new DateTime(e.getStartTime()).toString("HH:mm");
				String end_HHmm = new DateTime(e.getEndTime()).toString("HH:mm");
				
				DateTime start = Calendars.parse(meet_yyyyMMdd + " " + start_HHmm, Calendars.DATE_TIME);
				DateTime end = Calendars.parse(meet_yyyyMMdd + " " + end_HHmm, Calendars.DATE_TIME);
				
				Integer count = dao.func(Meet.class, "count", "meetId", Cnd
						.where("startTime", "=", start.toDate())
						.and("endTime", "=", end.toDate()));
				
				if (count > 0) continue;
				
				Meet meet = new Meet();
				copy(meet, e, start.toDate(), end.toDate(), now);
				
				dao.insert(meet);
			}
		} catch (Exception e) {
			log.error("(MeetJob:execute) error: ", e);
		}
	}
	
	private void copy(Meet meet, Meet e, Date start, Date end, DateTime now) {
		meet.setRoomId(e.getRoomId());
		meet.setUserId(e.getUserId());
		meet.setPhone(e.getPhone());
		meet.setStartTime(start);
		meet.setEndTime(end);
		meet.setContent(e.getContent());
		meet.setCompleted(Value.F);
		meet.setStatus(Status.ENABLED);
		meet.setCreateTime(now.toDate());
		meet.setModifyTime(now.toDate());
	}
}
