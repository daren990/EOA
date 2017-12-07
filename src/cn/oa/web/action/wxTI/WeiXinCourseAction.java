package cn.oa.web.action.wxTI;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.view.ServerRedirectView;

import cn.oa.consts.Sessions;
import cn.oa.model.EduStudent;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopGoodsDisplay;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.view.FreemarkerView;

@Filters
@IocBean(name = "wx.course")
@At(value = "/wx/course")
public class WeiXinCourseAction extends Action
{

	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View page(HttpServletRequest req, String org_id,String code,String openid,String student_id) throws Exception {
		EduStudent stu = dao.fetch(EduStudent.class,Cnd.where("id","=",student_id));
		try
		{
			req.setAttribute("org_id", org_id);
			req.setAttribute("student_id", student_id);
			req.setAttribute("student_name", stu.getName());
		}
		catch(Exception e)
		{
			String redirect = "/wx/index/indexPage?&org_id="+org_id;
			return new ServerRedirectView(redirect);
		}

		return new FreemarkerView("wxTI/wechat_courseCountList");
	}

	@GET
	@POST
	@At
	@Ok("json")
	@Filters({@By(type = ClientAutoLogin.class)})
	public Object getData(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		try {
			SimpleCriteria cri = Cnd.cri();
			ShopClient sc = (ShopClient) req.getSession().getAttribute(Sessions.wxShopClient);

			String seacherKey = Https.getStr(req, "seacherKey");
			String student_id = Https.getStr(req, "student_id");
						
			cri.where().and(new Static("sc.edu_student_id = " + student_id + " and g.edu_teacher_id = t.id and g.corp_id = o.org_id and g.id = sc.edu_course_id and g.status != 0 "));

			if(seacherKey!=null && !seacherKey.equals("")){
				cri.where().and(new Static(" (g.name like '%" + seacherKey + "%' or t.truename like '%" + seacherKey + "%' or t.telephone like '%" + seacherKey + "%' ) "));
			}

			cri.where().and(new Static(" 1=1 ORDER BY g.status asc,g.startDate DESC "));
						
			
			Page<ShopGoodsDisplay> page = Webs.page_jm(req);
			page = mapper.page(ShopGoodsDisplay.class,page,"ShopGoodsDisplay.count" , "ShopGoodsDisplay.index" ,cri,"studentId",student_id);
			List<ShopGoodsDisplay> voList = wxGoodsService.getShopGoodsDisplay(page);
			
			mb.put("voList", voList);
			Code.ok(mb, "success");
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
	
}
