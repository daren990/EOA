package cn.oa.web.action.wxTI;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
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
import cn.oa.model.CompanyCheckInfo;
import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentDisplay;
import cn.oa.model.EduStudentWechatrelation;
import cn.oa.model.ShopClient;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.filter.StudentAutoLogin;
import cn.oa.web.filter.WechatCallback;
import cn.oa.web.view.FreemarkerView;
import cn.oa.web.wx.Config;
import net.sf.json.JSONObject;

@Filters
@IocBean(name = "wx.student")
@At(value = "/wx/student")
public class WeiXinStudentAction extends Action{

	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View page(HttpServletRequest req, String org_id,String code,String openid,String flag) throws Exception {
		try
		{
			req.setAttribute("flag", flag);
			req.setAttribute("org_id", org_id);
		}
		catch(Exception e)
		{
			String redirect = "/wx/index/indexPage?&org_id="+org_id;
			return new ServerRedirectView(redirect);
		}

		return new FreemarkerView("wxTI/wechat_studentList");
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
			cri.asc("s.id");
			ShopClient sc = (ShopClient) req.getSession().getAttribute(Sessions.wxShopClient);

			String seacherKey = Https.getStr(req, "seacherKey");
			
			cri.where().and(new Static(" sc.shop_client_id = " + sc.getId() + " "));

			if(seacherKey!=null && !seacherKey.equals("")){
				if(seacherKey.equals("男"))
				{
					cri.where().and(new Static(" (s.name like '%"+seacherKey+"%' or s.sex = 1) "));
				}
				else if(seacherKey.equals("女"))
				{
					cri.where().and(new Static(" (s.name like '%"+seacherKey+"%' or s.sex = 0) "));
				}
				else if(seacherKey.equals("未登记"))
				{
					cri.where().and(new Static(" (s.telephone is null or s.sex is null) "));
				}
				else
				{
					cri.where().and(new Static(" (s.name like '%"+seacherKey+"%' or s.telephone like '%" + seacherKey + "%') "));
				}
			}

			Page<EduStudent> page = Webs.page_jm(req);
			page = mapper.page(EduStudent.class, page, "StudentClient.count", "StudentClient.index",cri);
			List<EduStudentDisplay> voList = wxStudentService.getEduStudentDisplay(page);
						
			mb.put("voList", voList);
			Code.ok(mb, "success");
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
  
	@GET
	@At
	public View join_view(HttpServletRequest req, String org_id) throws Exception {
		Asserts.isNull(org_id, "请传入公司的id");
		req.setAttribute("org_id", org_id);
		return new FreemarkerView("wxTI/wechat_join_student");
	}
	
	@GET
	@POST
	@At
	@Filters({@By(type = WechatCallback.class)})
	public View join(HttpServletRequest req, Integer org_id, String code) throws Exception {
		MapBean mb = new MapBean();
		String parameter_json = req.getParameter("parameter_json");
		JSONObject jsonObject = WechatCallback.getJsonObject(parameter_json);
		String clientPhone =  WechatCallback.getValue(jsonObject, "clientPhone");
		String studentName =  WechatCallback.getValue(jsonObject, "studentName");
		//根据clientPhone和studentName定位到某个学生
		EduStudent student = null;
		if(clientPhone != null && studentName != null){
			ShopClient client = new ShopClient();
			client.setTelephone(clientPhone);
			client = clientService.selectOne(client, null);
			if(client == null){
				mb.put("msg", "该家长的电话没有注册,请重新输入");
				req.setAttribute("mb", mb);
				req.setAttribute("org_id", org_id);
				req.setAttribute("clientPhone", clientPhone);
				req.setAttribute("studentName", studentName);
				return new FreemarkerView("wxTI/wechat_join_student");
			}
			
			List<EduStudent> existStudents = studentService.selectAll(client, -1);
			
			if(existStudents != null){
				student = findByName(existStudents, studentName);
				if(student == null){
					mb.put("msg", "学生的姓名不正确，请重新输入");
					req.setAttribute("mb", mb);
					req.setAttribute("org_id", org_id);
					req.setAttribute("clientPhone", clientPhone);
					req.setAttribute("studentName", studentName);
					return new FreemarkerView("wxTI/wechat_join_student");
				}
			}else{
				mb.put("msg", "该家长没有注册任何学生");
				req.setAttribute("mb", mb);
				req.setAttribute("org_id", org_id);
				req.setAttribute("clientPhone", clientPhone);
				req.setAttribute("studentName", studentName);
				return new FreemarkerView("wxTI/wechat_join_student");
			}

		}else{
			mb.put("msg", "您的输入有误，请重新输入");
			req.setAttribute("mb", mb);
			req.setAttribute("org_id", org_id);
			req.setAttribute("clientPhone", clientPhone);
			req.setAttribute("studentName", studentName);
			return new FreemarkerView("wxTI/wechat_join_student");
		}
		
		//以下是获取openId的操作
		MapBean studentMb = studentService.getEduStudent(org_id+"", code);
		Integer type = (Integer)studentMb.get("type");
		if(type.equals(-1) || type.equals(1)){
			mb.put("msg", "无法获取到openId，请重新输入");
			req.setAttribute("mb", mb);
			req.setAttribute("org_id", org_id);
			req.setAttribute("clientPhone", clientPhone);
			req.setAttribute("studentName", studentName);
			return new FreemarkerView("wxTI/wechat_join_student");
		}else if(type.equals(2)){
			//该流程说明，需要将openId绑定到数据库中的某个学生
			//先查询edu_student_wechatrelation是否已经有这个学生的微信信息,存在则更新，否则插入
			EduStudentWechatrelation stuWechatRelation = studentService.getWechatConfig(org_id+"", student.getId());
			if(stuWechatRelation != null){
				stuWechatRelation.setOpenId((String)studentMb.get("openid"));
			}else{
				stuWechatRelation = new EduStudentWechatrelation();
				stuWechatRelation.setCreateTime(new DateTime().toDate());
				stuWechatRelation.setOpenId((String)studentMb.get("openid"));
				stuWechatRelation.setOrgId(org_id);
				stuWechatRelation.setStudentId(student.getId());
			}
			studentService.insertOrUpdate(stuWechatRelation);
			//更新缓存
			MapBean wxEduStudentMap = (MapBean)req.getSession().getAttribute(Sessions.wxEduStudentMap);
			if(wxEduStudentMap != null){
				wxEduStudentMap.put(org_id, studentMb.get("openid"));
			}
			//调用JS-SDK
//			Config c = wxClientService.initOrgConfig(org_id+"");
//			wxClientService.setSignature(req,c);
//			req.setAttribute("appId", c.getAPPID());
			return new FreemarkerView("wxTI/wechat_join_student_success");
			
		}else if(type.equals(3)){
			//该流程说明，该微信已经跟数据库中的某个学生进行了关联，此时将该微信绑定到新的学生上
			EduStudent existStudent = (EduStudent)studentMb.get("eduStudent");
			//先查询edu_student_wechatrelation是否已经有这个学生的微信信息,存在则不允许操作（暂定）
			EduStudentWechatrelation stuWechatRelation = studentService.getWechatConfig(org_id+"", student.getId());
			if(stuWechatRelation != null){
				mb.put("msg", "您指定的学生已经绑定了某个微信号" );
				req.setAttribute("mb", mb);
				req.setAttribute("org_id", org_id);
				req.setAttribute("clientPhone", clientPhone);
				req.setAttribute("studentName", studentName);
				return new FreemarkerView("wxTI/wechat_join_student");
			}else{
//				//更新studentService.getEduStudent(org_id+"", code);查到的eduStudentWechatrelation
//				EduStudentWechatrelation eduStudentWechatrelation = (EduStudentWechatrelation)(studentMb.get("eduStudentWechatrelation"));
//				eduStudentWechatrelation.setStudentId(student.getId());
//				studentService.insertOrUpdate(eduStudentWechatrelation);
//				//由于本微信号已经换了学生去绑定，因此要清空缓存
//				req.getSession().removeAttribute(Sessions.wxEduStudent);
//				req.getSession().removeAttribute(Sessions.wxEduStudentMap);
				mb.put("msg", "本微信号已经绑定学生:" + existStudent.getName());
				req.setAttribute("mb", mb);
				req.setAttribute("org_id", org_id);
				req.setAttribute("clientPhone", clientPhone);
				req.setAttribute("studentName", studentName);
				return new FreemarkerView("wxTI/wechat_join_student");
			}
		}
		req.setAttribute("org_id", org_id);
		req.setAttribute("mb", mb);
		return null;
	}
	
	private EduStudent findByName(List<EduStudent> existStudents, String name){
		for(EduStudent student : existStudents){
			if(student.getName().equals(name)){
				return student;
			}
		}
		return null;
	}
	

	@At
	@POST
	@Ok("json")
	public Object check(HttpServletRequest req, HttpServletResponse response, String open_id, Integer org_id, String secret){
		if(open_id == null || org_id == null || secret == null || "".equals(open_id) || "".equals(secret)){
			MapBean mb = new MapBean();
			mb.put("code", 2);
			return mb;
		}
		
		//校验收到的外网上的secret是不是跟本地上的secret一样，防止异地打卡
		Properties properties = new Properties();  
		try { 
			InputStream is = this.getClass().getResourceAsStream("/check.properties"); 
			properties.load(is); 
			String existSecret = properties.getProperty("check.secret");
			System.out.println(existSecret);
			if(!secret.equals(existSecret)){
				MapBean mb = new MapBean();
				mb.put("code", 2);
				return mb;
			}
		} catch (IOException e) {  
			MapBean mb = new MapBean();
			mb.put("code", 2);
			return mb;
		}
		
		//解决跨域请求的问题
		response.setHeader("Access-Control-Allow-Origin", "*");
		MapBean mb = null;
		try {
			mb = new MapBean();
			EduStudentWechatrelation stuWe = studentService.getByOrgIdAndOpenId(org_id+"", open_id);
			if(stuWe == null){
				mb.put("code", 2);
				return mb;
			}
			EduStudent student = new EduStudent();
			student.setId(stuWe.getStudentId());
			studentService.sign(student);
			mb.put("code", 1);
			return mb;
		} catch (Exception e) {
			mb.put("code", 2);
			return mb;
		}
	}
	
	
	@At
	@Filters({@By(type = StudentAutoLogin.class)})
	public View checkView(HttpServletRequest req, Integer org_id){
		
		try {
			Config c = wxClientService.initOrgConfig(org_id+"");
			//调用JS-SDK，由于某些方法不需要权限，所以注释掉这句代码
//			wxClientService.setSignature(req,c);
			String openId = Webs.getWxEduStudentOpenId(req, org_id+"");
			CompanyCheckInfo info = coopCorpService.findByOrgId(org_id);
			if(info == null || info.getIp() == null || info.getSecret() == null){
				req.setAttribute("error", "还没有配置打卡机的IP地址或标识码");
			}else{
				req.setAttribute("IP", info.getIp());
				req.setAttribute("secret", info.getSecret());
			}
			req.setAttribute("appId", c.getAPPID());
			req.setAttribute("org_id", org_id);
			req.setAttribute("openid", openId);

			return new FreemarkerView("wxTI/wechat_student_signView");
		} catch (Exception e) {
			return null;
		}
	}
	
}