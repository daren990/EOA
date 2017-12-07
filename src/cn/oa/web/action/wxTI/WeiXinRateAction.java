package cn.oa.web.action.wxTI;

import java.util.Date;
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
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.trans.Trans;

import cn.oa.model.EduCourseRate;
import cn.oa.model.EduStudentSign;
import cn.oa.model.EduTeacher;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopGoodsDisplay;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.action.Action;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.view.FreemarkerView;

@Filters
@IocBean(name = "wx.rate")
@At(value = "/wx/rate")
public class WeiXinRateAction extends Action{
	
	private static final Log log = Logs.getLog(WeiXinRateAction.class);
	
	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View page(HttpServletRequest req, String org_id,String id,String code,String openid) throws Exception {		
		
		String time = null;
		double marks = 2.5;
		ShopGoodsDisplay courseDetail = null;
		EduCourseRate rate = dao.fetch(EduCourseRate.class,Cnd.where("sign_id","=",id));
		EduStudentSign sign = dao.fetch(EduStudentSign.class,Cnd.where("id","=",id));
		
		if(sign != null)
		{
			List<ShopGoodsDisplay> goodsList = courseService.findCourseDisplay(sign.getStudentId(),sign.getCourseId());
			if(goodsList != null && goodsList.size() != 0)
			{
				courseDetail = goodsList.get(0);
				time = teachingSchedule.getScheduleTimeById(sign.getScheduleId());
			}
		}
		else
		{
			sign = new EduStudentSign();
			courseDetail = new ShopGoodsDisplay();
			String redirect = "/wx/index/indexPage?&org_id="+org_id;
			return new ServerRedirectView(redirect);
		}
		if(rate != null)
		{
			marks = rate.getMark()/20.0;			
		}
		else
		{
			rate = new EduCourseRate();
			String redirect = "/wx/index/indexPage?&org_id="+org_id;
			return new ServerRedirectView(redirect);
		}
				
		req.setAttribute("time", time);
		req.setAttribute("rate", rate);
		req.setAttribute("sign", sign);
		req.setAttribute("marks", marks);
		req.setAttribute("org_id", org_id);
		req.setAttribute("courseDetail", courseDetail);
		return new FreemarkerView("wxTI/wechat_rate");
	}

	@POST
	@At
	@Ok("json")
	@Filters({@By(type = ClientAutoLogin.class)})
	public Object rate(HttpServletRequest req) throws Exception 
	{
		MapBean mb = new MapBean();
		Integer signId = Https.getInt(req, "signId");
		double mark = Https.getDouble(req, "markStar");
		String studentRateDetail = Https.getStr(req, "studentTextArea");

		try
		{
            Trans.begin();
			
            EduStudentSign sign = new EduStudentSign();
            sign = dao.fetch(EduStudentSign.class,Cnd.where("id","=",signId));
            
            ShopGoods good = new ShopGoods();
			good = dao.fetch(ShopGoods.class,Cnd.where("id","=",sign.getCourseId()));
			
            EduTeacher teacher = new EduTeacher();
            teacher = dao.fetch(EduTeacher.class,Cnd.where("id","=",good.getEduTeacherId()));
            
			EduCourseRate rate = new EduCourseRate();
			rate.setSignId(signId);
			rate.setCourseId(sign.getCourseId());
			rate.setCourseName(sign.getCourseName());
			rate.setStudentId(sign.getStudentId());
			rate.setStudentName(sign.getStudentName());
			rate.setTeacherId(teacher.getId());
			rate.setTeacherName(teacher.getName());
			rate.setScheduleId(sign.getScheduleId());
			rate.setMark((int)(mark*20));
			rate.setStudentRateDetail(studentRateDetail);
			rate.setStudentRateTime(new Date());
			dao.insert(rate);
			
			sign.setStudentIsRate(1);
			dao.update(sign);
			
			Trans.commit();
			
			Code.ok(mb, "评价成功");
		}
		catch(Exception E)
		{
			Trans.rollback();
			Trans.commit();
			log.error("(Rate) error: ", E);
			Code.error(mb, "评价成功");
		}
		return mb;		
	}

	
}
