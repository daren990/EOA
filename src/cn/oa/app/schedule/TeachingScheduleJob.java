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
import org.nutz.dao.util.cri.Static;
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

public class TeachingScheduleJob implements Job {

	private static Log log = Logs.getLog(TeachingScheduleJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Ioc ioc = Jobs.getIoc(context);
		Dao dao = ioc.get(Dao.class);
//		Mapper mapper = ioc.get(Mapper.class);
		TeachingSchedule teachingSchedule = ioc.get(TeachingScheduleImpl.class, "teachingSchedule");
		//查询所有需要标记为过时的课程，并且改变status字段为2
		List<Integer> courseIds = teachingSchedule.findOutdatedCourseIds();
		dao.update(ShopGoods.class, Chain.make("status", 2), Cnd.where(new Static("(true")).and("id", "in", courseIds).or("dependId", "in", courseIds).and(new Static("true)")).and("status", "=", 1));
//		 Cnd.where(new Static("(true")).and("id", "in", courseIds).or("dependId", "in", courseIds).and(new Static("true)"))
//		cri.where().and(new Static("(true")).and("t.id", "in", coopTeacherIds).or("t.corp_id", "=", (Integer)(map.get("cn.oa.model.Org.orgId"))).and(new Static("true)"));
	}
}
