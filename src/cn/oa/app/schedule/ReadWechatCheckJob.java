package cn.oa.app.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;

import org.nutz.dao.sql.Criteria;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.oa.app.quartz.Jobs;
import cn.oa.consts.Check;
import cn.oa.consts.ParamesAPI;
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
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.wx.comm.QiyeWeixinUtil;

public class ReadWechatCheckJob implements Job {

	public static final String CORPID="ww02933c8bff1c92b6";
	public static final String SERECT="ZgdUF5epUHCZNANrZob1yfx3K1F_g1Mxv7whYzeoh3M";

	private static Log log = Logs.getLog(ReadWechatCheckJob.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Ioc ioc = Jobs.getIoc(context);
		Dao dao = ioc.get(Dao.class);
		Mapper mapper = ioc.get(Mapper.class);
		//向微信发送请求，获取密钥
		String accessToken = QiyeWeixinUtil.getAccessToken(CORPID,SERECT).getToken();
		
		Criteria cri = Cnd.cri();
		cri.where().and("u.status", "=", 1);
		cri.getOrderBy().desc("u.modify_time");
		
		//将要存入数据库的打卡记录放入其中
		List<CheckRecord> records = new ArrayList<CheckRecord>();
		
		Page<User> page = null;
		int x = 1;
		//循环地从微信那边获取打卡记录
		while(true)
		{
			//最多拉100个用户的打卡数据
			page = new Page<User>(x, 100);
			page.setAutoCount(false);
			page = mapper.page(User.class, page, "User.weixin.count", "User.weixin.index", cri);
			x++;
			
			if(page.getResult() != null && page.getResult().size() != 0){
				List<User> users = page.getResult();
				Map<String, String> jobNumberMap = new HashMap<String, String>();
				String userJson = getJson(users, jobNumberMap);
				System.out.println(userJson);
				String postData = "{\"opencheckindatatype\":%s,\"starttime\":%s,\"endtime\":%s,\"useridlist\":%s}";
				String data = String.format(postData,"3",new DateTime(new Date()).plusDays(-1).toDate().getTime() / 1000 +"", new Date().getTime() / 1000 +"", userJson); 
				System.out.println("请求体："+data);
				//批量获取微信那边的打卡数据
				JSONObject jsonObject = QiyeWeixinUtil.httpRequest("https://qyapi.weixin.qq.com/cgi-bin/checkin/getcheckindata?access_token="+accessToken, "POST", data);
				System.out.println(jsonObject.toString());
				if(jsonObject.containsKey("errmsg") && jsonObject.get("errmsg").toString().equals("OK")){
					//遍历用户的打卡情况
					JSONArray jsonArray = jsonObject.getJSONArray("checkindata");
					System.out.println(jsonArray.toString());
					for(int i = 0; i < jsonArray.size(); i++){
						String userid = (String) jsonArray.getJSONObject(i).get("userid");
						String checkin_time = jsonArray.getJSONObject(i).get("checkin_time").toString();
						String location_detail = (String) jsonArray.getJSONObject(i).get("location_detail");
						if(location_detail.contains("广州市花都区迎宾大道玫瑰路7号")){
							CheckRecord record = new CheckRecord();
							record.setCheckTime(new Date((long)(Integer.parseInt(checkin_time)) * 1000));
							record.setNumber(0);
							record.setEntryTime(new Date());
							record.setJobNumber(jobNumberMap.get(userid));
							records.add(record);
							System.out.println(userid+"打卡成功，打卡时间为："+checkin_time);
						}
					}
				}else{
					//继续循环地获取下一批用户的打卡数据
					continue;
				}

			}else{
				break;
			}
		}
//		dao.clear(CheckRecord.class, C  n)
		dao.fastInsert(records);
	}
	
	public static String getJson(List<User> users, Map<String, String> jobNumberMap){
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		String split = "";
		for(User user : users){
			sb.append(split);
			sb.append("\"");
			if(user.getWechatName() != null){
				sb.append(user.getWechatName());
			}
			sb.append("\"");
			split = ",";
			
			//装入jobNumberMap
			if(jobNumberMap != null){
				jobNumberMap.put(user.getWechatName(), user.getJobNumber());
			}
		}
		
		sb.append("]");
		return sb.toString();
	}
}
