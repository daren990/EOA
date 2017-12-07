package cn.oa.web.action.wxTI;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentDisplay;
import cn.oa.model.ShopGoodsDisplay;
import cn.oa.web.action.Action;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.view.FreemarkerView;

@Filters
@IocBean(name = "wx.count")
@At(value = "/wx/count")
public class WeiXinCountAction extends Action
{

	private static final Log log = Logs.getLog(WeiXinCountAction.class);

	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View page(HttpServletRequest req, String org_id,Integer student_id,Integer course_id,String code,String openid) throws Exception 
	{
		double percentAllAndNow = 1.0;
		double percentNowAndShould = 1.0;
		String couWeekday = "0";
		ShopGoodsDisplay courseDetail = new ShopGoodsDisplay();
		EduStudentDisplay courseCount = new EduStudentDisplay();
		List<EduStudentDisplay> list = teachingSchedule.scheduleCount(course_id);
		List<ShopGoodsDisplay> goodsList = courseService.findCourseDisplay(student_id,course_id);
		EduStudent stu = dao.fetch(EduStudent.class,Cnd.where("id","=",student_id));
		
		if(list != null && list.size() != 0)
		{
			courseCount = list.get(0);
		}

		if(goodsList != null && goodsList.size() != 0)
		{
			courseDetail = goodsList.get(0);
		}

		if(courseDetail.getCountAppear() == null)
		{
			courseDetail.setCountAppear(0);
		}
		
		try
		{
			String[] temp = courseDetail.getCouTime().split("\\|");
			couWeekday = temp[0]; //获得上课时间类型  1.法定工作日  2.每天  3.自己指定
			if(couWeekday.equals("3"))
			{
				int i = temp.length;
				System.out.println(i);
				if(1 < i && !temp[1].equals(""))
				{
					String startTime1 = temp[1].split("-")[0];
					String endTime1 = temp[1].split("-")[1];
					req.setAttribute("startTime1", startTime1);
					req.setAttribute("endTime1", endTime1);
				}
				if(2 < i && !temp[2].equals(""))
				{
					String startTime2 = temp[2].split("-")[0];
					String endTime2 = temp[2].split("-")[1];
					req.setAttribute("startTime2", startTime2);
					req.setAttribute("endTime2", endTime2);
				}
				if(3 < i && !temp[3].equals(""))
				{
					String startTime3 = temp[3].split("-")[0];
					String endTime3 = temp[3].split("-")[1];
					req.setAttribute("startTime3", startTime3);
					req.setAttribute("endTime3", endTime3);
				}
				if(4 < i && !temp[4].equals(""))
				{
					String startTime4 = temp[4].split("-")[0];
					String endTime4 = temp[4].split("-")[1];
					req.setAttribute("startTime4", startTime4);
					req.setAttribute("endTime4", endTime4);
				}
				if(5 < i && !temp[5].equals(""))
				{
					String startTime5 = temp[5].split("-")[0];
					String endTime5 = temp[5].split("-")[1];
					req.setAttribute("startTime5", startTime5);
					req.setAttribute("endTime5", endTime5);
				}
				if(6 < i && !temp[6].equals(""))
				{
					String startTime6 = temp[6].split("-")[0];
					String endTime6 = temp[6].split("-")[1];
					req.setAttribute("startTime6", startTime6);
					req.setAttribute("endTime6", endTime6);
				}
				if(7 < i && temp.length > 7 && !temp[7].equals(""))
				{
					String startTime7 = temp[7].split("-")[0];
					String endTime7 = temp[7].split("-")[1];
					req.setAttribute("startTime7", startTime7);
					req.setAttribute("endTime7", endTime7);
				}
			}
			else
			{
				String startTime = temp[1].split("-")[0];
				String endTime = temp[1].split("-")[1];
				req.setAttribute("startTime", startTime);
				req.setAttribute("endTime", endTime);
			}
		}
		catch(Exception e)
		{
			String redirect = "/wx/index/indexPage?&org_id="+org_id;
			return new ServerRedirectView(redirect);
		}
						
		if(courseCount.getCountStart() != 0)
		{
			percentNowAndShould = (double)courseDetail.getCountAppear() / (double)courseCount.getCountStart();
		}
		if(courseCount.getCountAll() != 0)
		{
			percentAllAndNow = (double)courseCount.getCountStart() / (double)courseCount.getCountAll();
		}
		
		courseCount.setName(stu.getName());
		
		req.setAttribute("org_id", org_id);
		req.setAttribute("couWeekday", couWeekday);
		req.setAttribute("courseCount", courseCount);
		req.setAttribute("courseDetail", courseDetail);
		req.setAttribute("percentAllAndNow", (percentAllAndNow*100) + "%");
		req.setAttribute("percentNowAndShould", (percentNowAndShould*100) + "%");
	    return new FreemarkerView("wxTI/wechat_count");
	}
	
}
