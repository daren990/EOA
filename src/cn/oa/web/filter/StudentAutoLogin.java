package cn.oa.web.filter;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.joda.time.DateTime;
import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.oa.consts.Sessions;
import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentWechatrelation;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopWechatrelation;
import cn.oa.service.WxClientService;
import cn.oa.service.client.ClientService;
import cn.oa.service.student.StudentService;
import cn.oa.utils.Asserts;
import cn.oa.utils.DesUtils;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.PathUtil;
import cn.oa.web.view.FreemarkerView;
import cn.oa.web.wx.Config;
import cn.oa.web.wx.InterfacePath;

public class StudentAutoLogin implements ActionFilter {
	public static Log log = Logs.getLog(StudentAutoLogin.class);

	@Override
	public View match(ActionContext ctx) {
		Ioc ioc = ctx.getIoc();
		StudentService studentService = ioc.get(StudentService.class, "studentService");
		
		HttpServletRequest req = ctx.getRequest();
		String org_id = req.getParameter("org_id");
		Asserts.isNull(org_id, "请传入公司的id");
		String code = req.getParameter("code");
		
		//从session中获取学生个人信息
		EduStudent student = (EduStudent) req.getSession().getAttribute(Sessions.wxEduStudent);
		MapBean eduStudentMap = (MapBean) req.getSession().getAttribute(Sessions.wxEduStudentMap);
		
		try {
			if(code == null || code.equals("")){
				//进入本流程，说明方法并非是微信回调的
				if(student !=null || eduStudentMap != null){
					if(eduStudentMap.containsKey(org_id)){
						return null;
					}
				}
				//让微信回调本方法，以此实现自动登录
				return weixinCallBack(ioc, req);
			}else{
				
				if(student !=null && eduStudentMap != null){
					//跟下方的!eduStudentMap.containsKey(org_id)配合，保证（student !=null && eduStudentMap != null）的时候的所有情况的处理
					if(eduStudentMap.containsKey(org_id)){
						return null;
					}
				}
				
				MapBean studentMb = studentService.getEduStudent(org_id+"", code);
				
				Integer type = (Integer)studentMb.get("type");
				if(type.equals(-1) || type.equals(1)){
					//让微信回调本方法，以此实现自动登录
					return weixinCallBack(ioc, req);
				
				}else if(type.equals(2)){
					//该流程说明，可能需要将新的openId绑定到数据库中的某个学生
					String newOpenId = (String)studentMb.get("openid");
					if(student != null && eduStudentMap != null){
						if(!eduStudentMap.containsKey(org_id)){
							//当学生的手机（微信）换了，此时需要将新的手机（微信）自动绑定到该学生中，覆盖之前的
							EduStudentWechatrelation stuWechatRelation = studentService.getWechatConfig(org_id+"", student.getId());
							if(stuWechatRelation != null){
								if(!stuWechatRelation.getOpenId().equals(newOpenId)){
									stuWechatRelation.setOpenId(newOpenId);
									studentService.insertOrUpdate(stuWechatRelation);
								}
							}else{
								stuWechatRelation = new EduStudentWechatrelation();
								stuWechatRelation.setCreateTime(new DateTime().toDate());
								stuWechatRelation.setOpenId((String)studentMb.get("openid"));
								stuWechatRelation.setOrgId(Integer.parseInt(org_id));
								stuWechatRelation.setStudentId(student.getId());
								studentService.insertOrUpdate(stuWechatRelation);
							}
							
							//更新缓存
							eduStudentMap.put(org_id, newOpenId);
						}
						return null;
					}else{
						MapBean mb = new MapBean();
						mb.put("msg", "请先登记学生信息");
						//转发到登记页面，让客户填入学生的信息后才进一步操作
						req.setAttribute("org_id", org_id);
						req.setAttribute("mb", mb);
						return new FreemarkerView("wxTI/wechat_join_student");
					}
					
				}else if(type.equals(3)){
					if(student == null || eduStudentMap == null){
						//将数据库中的学生信息缓存起来
						student = (EduStudent)studentMb.get("eduStudent");
						req.getSession().setAttribute(Sessions.wxEduStudent, student);
//						List<EduStudentWechatrelation> eduStudentWechatrelations  = studentService.selectWechatrelation(student);
//						MapBean wxEduStudentMap = toMapBean(eduStudentWechatrelations);
						
						MapBean mb = new MapBean();
						mb.put(org_id , (String)studentMb.get("openid"));
						req.getSession().setAttribute(Sessions.wxEduStudentMap, mb);
						return null;
					}else{
						//进入该流程，说明是带着缓存访问新的公众号，但这个公众号之前已经绑定了这个openId
						EduStudent dbstudent = (EduStudent)studentMb.get("eduStudent");
						if(!dbstudent.getId().equals(student.getId())){
							//覆盖之前缓存的学生的对象,即相当于换账号登录
							req.getSession().setAttribute(Sessions.wxEduStudent, dbstudent);
							MapBean mb = new MapBean();
							mb.put(org_id , (String)studentMb.get("openid"));
							req.getSession().setAttribute(Sessions.wxEduStudentMap, mb);
						}else{
							eduStudentMap.put(org_id, (String)studentMb.get("openid"));
						}
						return null;
					}
				}
	
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return null;
	}
	
	private MapBean toMapBean(List<EduStudentWechatrelation> eduStudentWechatrelations){
		MapBean mapBean = new MapBean();
		if(eduStudentWechatrelations == null){
			return mapBean; 
		}
		for(EduStudentWechatrelation eduStudentWechatrelation : eduStudentWechatrelations){
			mapBean.put(eduStudentWechatrelation.getOrgId(), eduStudentWechatrelation.getOpenId());
		}
		return mapBean;
	}
	
	private View weixinCallBack(Ioc ioc, HttpServletRequest req){

		WxClientService wxClientService = ioc.get(WxClientService.class, "wxClientService");
		String org_id = req.getParameter("org_id");
		Asserts.isNull(org_id, "请传入公司的id");
		
		try {
			String redirectUrl = null;
			if("POST".equals(req.getMethod())){
				Map<String, String[]> paremeterMap = req.getParameterMap();
				String parameterJson = getParameterJson(paremeterMap);
				String secrectJson = encrypt(parameterJson);
				Config c = wxClientService.initOrgConfig(org_id);
//				System.out.println(secrectJson);
				redirectUrl = PathUtil.getRequestURL(req)+"?parameter_json="+secrectJson+"%26org_id="+org_id;
				String url = String.format(InterfacePath.AUTH_URL, c.getAPPID(), redirectUrl,"snsapi_base");
//				System.out.println(redirectUrl);
//				System.out.println(url);
				return new ServerRedirectView(url);
			}else if("GET".equals(req.getMethod())){
				String code = req.getParameter("code");
				if(code != null && !"".equals(code)){
					return null;
				}else{
					Config c = wxClientService.initOrgConfig(org_id);
					String queryString = req.getQueryString();
					queryString = queryString.replace("&", "%26");
					redirectUrl = PathUtil.getRequestURL(req)+"?"+queryString;
					String url = String.format(InterfacePath.AUTH_URL, c.getAPPID(), redirectUrl,"snsapi_base");
//					System.out.println(redirectUrl);
//					System.out.println(url);
					return new ServerRedirectView(url);
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return null;
	}
	
	private String getParameterJson(Map<String, String[]> paremeterMap){
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(paremeterMap);
		System.out.println(json);
		return json;
	}
	
	private String encrypt(String str) throws Exception{
		DesUtils des = new DesUtils(WechatCallback.KEY);//自定义密钥   
		return des.encrypt(str);
	}
}
