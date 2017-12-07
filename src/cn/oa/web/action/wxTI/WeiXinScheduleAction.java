package cn.oa.web.action.wxTI;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.model.EduStudent;
import cn.oa.model.EduTeacher;
import cn.oa.model.EduTeachingSchedule;
import cn.oa.model.ShopGoods;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.action.Action;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.view.FreemarkerView;

@Filters
@IocBean(name = "wx.schedule")
@At(value = "/wx/schedule")
public class WeiXinScheduleAction extends Action{

	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View page(HttpServletRequest req, String org_id,String code,Integer days,String openid,Integer student_id) throws Exception {		

		EduStudent student = new EduStudent();
		student = dao.fetch(EduStudent.class,Cnd.where("id","=",student_id));

		DateTime nowTime = new DateTime();
		DateTime timePoint = nowTime.plusDays(7);
		DateTime start = null;
		DateTime end = null;
		if(nowTime.isBefore(timePoint)){
			start = nowTime;
			end = timePoint;
		}else{
			start = timePoint;
			end = nowTime;
		}

		String title = student.getName() == null?start.toString("yyyy-MM-dd") + " ~ " + end.toString("yyyy-MM-dd"):student.getName() + " " + start.toString("yyyy-MM-dd") + " ~ " + end.toString("yyyy-MM-dd");
		
		req.setAttribute("title", title);
		req.setAttribute("org_id", org_id);
		req.setAttribute("student_id", student_id);
		return new FreemarkerView("wxTI/wechat_schedule");
	}
	
	@POST
	@At
	@Ok("json")
	public Object getData(HttpServletRequest req) throws Exception {

		MapBean mb = new MapBean();

		try {
	
			Integer isPull = Https.getInt(req, "isPull");
			Integer student_id = Https.getInt(req, "student_id");
			String seacherKey = Https.getStr(req, "seacherKey");
			Integer pageNo = Https.getInt(req, "pageNo");
			
			EduStudent student = new EduStudent();
			student = dao.fetch(EduStudent.class,Cnd.where("id","=",student_id));

			List<EduTeachingSchedule> voList = new ArrayList<EduTeachingSchedule>();
			
			if(isPull == null || isPull == 0 || isPull.equals(0)){
				voList = teachingSchedule.selectAll(6, null, student);
			}else{
				Date timePoint = new DateTime().plusDays((pageNo-1) * 7).toDate();
				voList = teachingSchedule.selectAll(6, timePoint, student);
			}
			
			if(voList == null ) voList = new ArrayList<EduTeachingSchedule>();
			
			if(seacherKey!=null && !seacherKey.equals("")){
				for(int i=0;i<voList.size();i++){
					EduTeachingSchedule deleteSchedule = voList.get(i);
					ShopGoods deleteCourse = deleteSchedule.getCourse();
					EduTeacher deleteTeacher = deleteSchedule.getTeacher();
					SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm");    
					String start = dateFormater.format(deleteSchedule.getStart()).toString();
					String end = dateFormater.format(deleteSchedule.getEnd()).toString();
					
					if(start.indexOf(seacherKey) == -1 || end.indexOf(seacherKey) == -1){
						if(deleteCourse.getName().indexOf(seacherKey) == -1){
							if(deleteTeacher.getTruename().indexOf(seacherKey) == -1){
								voList.remove(i);
							}
						}
					}
				}
			}
			
//			DateTime nowTime = new DateTime();
//			DateTime timePoint = nowTime.plusDays(days);
//			DateTime start = null;
//			DateTime end = null;
//			if(days == 7 || days.equals("7"))
//			{
//				if(nowTime.isBefore(timePoint)){
//					start = nowTime;
//					end = timePoint;
//				}else{
//					start = timePoint;
//					end = nowTime;
//				}
//			}
//			else
//			{
//				if(nowTime.isBefore(timePoint)){
//					start = timePoint.plusDays(-7);
//					end = timePoint;
//				}else{
//					start = timePoint;
//					end = timePoint.plusDays(7);
//				}
//
//			}
//			String title = student.getName() == null?start.toString("yyyy-MM-dd") + " ~ " + end.toString("yyyy-MM-dd"):student.getName() + " " + start.toString("yyyy-MM-dd") + " ~ " + end.toString("yyyy-MM-dd");
//			mb.put("title", title);
			mb.put("voList", voList);
			Code.ok(mb, "success");
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
}
