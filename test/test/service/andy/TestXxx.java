package test.service.andy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.joda.time.DateTime;
import org.junit.Test;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.Ioc;

import cn.oa.app.quartz.Jobs;
import cn.oa.app.schedule.ReadWechatCheckJob;
import cn.oa.model.CheckRecord;
import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentClient;
import cn.oa.model.User;
import cn.oa.repository.Mapper;
import cn.oa.service.student.StudentService;
import cn.oa.service.student.StudentServiceImpl;
import cn.oa.utils.Calendars;
import cn.oa.utils.web.Page;
import cn.oa.web.action.wx.comm.QiyeWeixinUtil;

import test.Setup;

public class TestXxx extends Setup{
	
	@Test
	public void test() {
		//获取现在所在月的第一天和最后一天
		DateTime dayDataTime = new DateTime();
		int dayOfMonth = dayDataTime.dayOfMonth().get();
		String start = Calendars.str(dayDataTime.plusDays(-dayOfMonth + 1), "yyyy-MM-dd 00:00:00");
		String end = Calendars.str(dayDataTime.plusDays(-dayOfMonth + 1).plusMonths(1).plusDays(-1), "yyyy-MM-dd 23:59:59");
		System.out.println(start+":"+end);
	}
	
	private <T> List<T> pack(T element){
		List<T> list = new ArrayList<T>();
		list.add(element);
		return list;
	}
	
	@Test
	public void test2(){
		Dao dao = ioc.get(Dao.class);
		Mapper mapper = ioc.get(Mapper.class);
		//向微信发送请求，获取密钥
		String accessToken = QiyeWeixinUtil.getAccessToken(ReadWechatCheckJob.CORPID,ReadWechatCheckJob.SERECT).getToken();
		
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
				String userJson = ReadWechatCheckJob.getJson(users, jobNumberMap);
				System.out.println(userJson);
				String postData = "{\"opencheckindatatype\":%s,\"starttime\":%s,\"endtime\":%s,\"useridlist\":%s}";
				String data = String.format(postData,"3",new DateTime(new Date()).plusDays(-1).toDate().getTime() / 1000 +"", new Date().getTime() / 1000 +"", userJson); 
				System.out.println("请求体："+data);
				//批量获取微信那边的打卡数据
				JSONObject jsonObject = QiyeWeixinUtil.httpRequest("https://qyapi.weixin.qq.com/cgi-bin/checkin/getcheckindata?access_token="+accessToken, "POST", data);
				System.out.println(jsonObject.toString());
				if(jsonObject.containsKey("errmsg") && jsonObject.get("errmsg").toString().equals("ok")){
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
		dao.fastInsert(records);
	}
	
	@Test
	
	public void test3(){
		System.out.println("凑整:Math.ceil(2.1)=" + (int)Math.ceil(2.1));
	}
}
