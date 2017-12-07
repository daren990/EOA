package cn.oa.app.schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.oa.app.quartz.Jobs;
import cn.oa.consts.Check;
import cn.oa.consts.Roles;
import cn.oa.consts.ShiftC;
import cn.oa.consts.Status;
import cn.oa.model.Archive;
import cn.oa.model.CheckRecord;
import cn.oa.model.Leave;
import cn.oa.model.Role;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.ShopGoods;
import cn.oa.model.User;
import cn.oa.model.WorkDay;
import cn.oa.repository.Mapper;
import cn.oa.repository.WorkRepository;
import cn.oa.service.CheckedRecordService;
import cn.oa.service.schedule.TeachingSchedule;
import cn.oa.service.schedule.TeachingScheduleImpl;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.Strings;

public class AttendClassJob implements Job {

	private static Log log = Logs.getLog(AttendClassJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Ioc ioc = Jobs.getIoc(context);
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		try {
			teachingSchedule.sendWeixin();
		} catch (Exception e) {
			log.debug("使用微信推送上课通知的时候出现问题");
		}
	}
}
