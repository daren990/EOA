package cn.oa.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.mvc.config.AtMap;
import org.quartz.impl.StdSchedulerFactory;

import cn.oa.app.quartz.Jobs;
import cn.oa.app.schedule.AnnualJob;
import cn.oa.app.schedule.AttendClassJob;
import cn.oa.app.schedule.CheckedRecordJob;
import cn.oa.app.schedule.MeetJob;
import cn.oa.app.schedule.ReadWechatCheckJob;
import cn.oa.app.schedule.ShiftJob;
import cn.oa.app.schedule.SignScheduleJob;
import cn.oa.app.schedule.TeachingScheduleJob;

public class AppSetup implements Setup {
	
	private static Log log = Logs.getLog(AppSetup.class);

	public static DataSource ds = null;
	
	@Override
	public void init(NutConfig cfg) {
		try {
			exportMsg();
			Jobs.sf = new StdSchedulerFactory("quartz.properties");
			Jobs.start(cfg.getIoc(), 1, "annualJob", "annualGroup", "0 05 01 * * ?", AnnualJob.class);
//			Jobs.start(cfg.getIoc(), 1, "annualJob", "annualGroup", "0 0 1 * * ?", AnnualJob.class);
//			Jobs.start(cfg.getIoc(), 2, "checkedRecordJob", "checkedRecordGroup", "0 03 09 * * ?", CheckedRecordJob.class);
//			Jobs.start(cfg.getIoc(), 3, "meetJob", "meetGroup", "0 0/1 * * * ?", MeetJob.class);
			Jobs.start(cfg.getIoc(), 3, "meetJob", "meetGroup", "0 0 0 * * ?", MeetJob.class);
			
			//录入考勤,时间要在打卡机同步打卡记录的时间之后,每天凌晨3点触发
			Jobs.start(cfg.getIoc(), 2, "checkedRecordGroup", "checkedRecordGroup", "0 00 03 * * ?", CheckedRecordJob.class);

			//每月25号，自动锁定下个月的排班
			Jobs.start(cfg.getIoc(), 4, "shiftJob", "shiftJobGroup", "0 0 0 25 * ?", ShiftJob.class);
			
			//每天凌晨4点，判断要标志为超时的课程
			Jobs.start(cfg.getIoc(), 5, "teachingScheduleJob", "teachingScheduleJobGroup", "0 00 04 * * ?", TeachingScheduleJob.class);
			
			//整点触发，填充打卡信息表
			Jobs.start(cfg.getIoc(), 6, "SignScheduleJob", "SignScheduleJobGroup", "0 0 * * * ?", SignScheduleJob.class);
			
			//每天6点30触发，发送微信推送
			Jobs.start(cfg.getIoc(), 7, "attendClassJob", "attendClassJobGroup", "0 30 18 * * ?", AttendClassJob.class);
	
			//从企业微信中读取打卡记录，每天凌晨0点10分触发
			Jobs.start(cfg.getIoc(), 8, "readWechatCheckJobJob", "readWechatCheckJobGroup", "0 10 00 * * ?", ReadWechatCheckJob.class);
		} catch (Exception e) {
			log.error("(AppSetup:init) error: ", e);
		}
	}

	@Override
	public void destroy(NutConfig cfg) {
		try {
			Jobs.sf.getScheduler().shutdown();
			Jobs.sf = null;
		} catch (Exception e) {
			log.error("(AppSetup:destroy) error: ", e);
		}
	}
	
	private void exportMsg() throws FileNotFoundException, IOException{
		JsonLoader jsonLoader = new JsonLoader("ioc.js");
		Map<String, Object> dataSourceMap = jsonLoader.getMap().get("dataSource");
		@SuppressWarnings("unchecked")
		Map<String, Object> fieldsMap =(Map<String, Object>) dataSourceMap.get("fields");
//		System.out.println(fieldsMap.get("url"));
//		System.out.println(fieldsMap.get("username"));
		Properties p = new Properties();	
		p.load(new FileReader(this.getClass().getResource("/").getPath()+"/version.properties"));
		p.setProperty("dataBaseUrl", (String)fieldsMap.get("url"));
		p.setProperty("dataBaseUsername", (String)fieldsMap.get("username"));
		Writer writer = new FileWriter(this.getClass().getResource("/").getPath()+"/version.properties");
		p.store(writer, new Date().toString());
		writer.close();
	

	}
}
