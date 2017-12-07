package cn.oa.web.action.sys.conf;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Status;
import cn.oa.model.Notice;
import cn.oa.model.Noticerecord;
import cn.oa.model.Org;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At("/sys/conf/announcement")
public class AnnouncementAction extends Action{
	public static Log log = Logs.getLog(AnnouncementAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/announcement_page")
	public void page(HttpServletRequest req){
		
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/announcement/able", token);
		CSRF.generate(req, "/sys/conf/announcement/add", token);
		
		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);
		Integer type = Https.getInt(req, "type", R.I);
		Integer approve = Https.getInt(req, "approve", R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where().and("corp", "=", Context.getCorpId());
		//Cnds.eq(cri, mb, "a.approve", "approve", approve);
		Date now = new Date();
		if(approve!=null){
			if(approve == 0){
				cri.where().and("a.start_time", ">=", now);
			}else if(approve == -1){
				cri.where().and("a.end_time", "<=", now);
			}else if(approve == 1){
				cri.where().and("a.end_time", ">=", now).and("a.start_time", "<=", now);
			}
			mb.put("approve",approve);
		}
		Cnds.gte(cri, mb, "a.creat_time", "startTime", startStr);
		Cnds.lte(cri, mb, "a.creat_time", "endTime", endStr);
		Cnds.eq(cri, mb, "a.type", "type", type);
		cri.getOrderBy().desc("a.creat_time");
		Page<Notice> page = Webs.page(req);
		page = mapper.page(Notice.class, page, "Notice.count","Notice.index",cri);
		
		for(Notice notice:page.getResult()){
			notice.setUnread(dao.count(Noticerecord.class, Cnd.where("notice_id", "=", notice.getNoticeId()).and("is_receive", "=", 0)));
			notice.setRead(dao.count(Noticerecord.class, Cnd.where("notice_id", "=", notice.getNoticeId()).and("is_receive", "=", 1)));
			if(notice.getStartTime().after(now)){
				notice.setStatus(0);//未生效
			}else if(notice.getEndTime().before(now)){
				notice.setStatus(-1);//已失效
			}else{
				notice.setStatus(1);//正在生效
			}
		}
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:sys/conf/announcement_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer announcementId = Https.getInt(req, "announcementId", R.REQUIRED,R.I);
		//Criteria criteria = Cnd.cri();
		Notice announcement = new Notice();
		if(announcementId!=null)
			announcement = dao.fetch(Notice.class,announcementId);
		//List<User> users = userService.operators(new String[]{Roles.CSVI.getName(), Roles.ASVI.getName(), Roles.CAS.getName(), Roles.ACC.getName()});
		//Org corp = dao.fetch(Org.class,announcement.getCorp());
		List<Org> orgs = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("parent_id", "=", 0));
		List<Role> roles = dao.query(Role.class, Cnd.where("status", "=", Status.ENABLED));
		Integer[] arrCorp = Converts.array(announcement.getCorpId(), ",");
		Integer[] arrRole = Converts.array(announcement.getRoleId(), ",");
		Integer[] arrUser = Converts.array(announcement.getUserId(), ",");
		
		List<Org> oList = new ArrayList<Org>();
		List<Role> rList = new ArrayList<Role>();
		List<User> uList = new ArrayList<User>();	
		Criteria cri = Cnd.cri();
		if(arrCorp!=null){
			oList = dao.query(Org.class, Cnd.where("org_id", "in", arrCorp));
			cri.where().and("u.corp_id", "in", arrCorp);
		}
		if(arrRole!=null){
			rList = dao.query(Role.class, Cnd.where("role_id", "in", arrRole));
			cri.where().and("r.role_id", "in", arrRole);
		}
		if(arrUser!=null)uList = dao.query(User.class, Cnd.where("user_id", "in", arrUser));
	
		cri.where().and("u.status", "=", Status.ENABLED);
		List<User> users = mapper.query(User.class, "User.operator", cri);
		
		String oString=""; 
		String rString="";
		String uString="";
		for(Org org:oList){
			oString += org.getOrgName()+",";
		}
		for(Role role:rList){
			rString += role.getRoleDesc()+",";
		}
		for(User user:uList){
			uString += user.getTrueName()+",";
		}
		req.setAttribute("oList", oString);		
		req.setAttribute("rList", rString);		
		req.setAttribute("uList", uString);	
		
		req.setAttribute("annuncement", announcement);
		req.setAttribute("orgs", orgs);		
		req.setAttribute("roles", roles);	
		req.setAttribute("users", users);	
		//req.setAttribute("corp", corp);		
	}
	
	@POST
	@At 
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer noticeId = null;
		try {
			CSRF.validate(req);
			noticeId = Https.getInt(req, "noticeId", R.I);
			String title = Https.getStr(req, "title", R.REQUIRED, "公告标题");
			if(title.length()>100)throw new Errors("该公告字数不能超过100");	
			String corpIds = Https.getStr(req, "modelOrgs", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			String roleIds = Https.getStr(req, "modelRoles", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "角色绑定");
			String userIds = Https.getStr(req, "modelUsers", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "员工绑定");
			/*Integer[] arrCorp = Converts.array(corpIds, ",");
			Integer[] arrRole = Converts.array(roleIds, ",");
			Integer[] arrUser = Converts.array(userIds, ",");*/
			
			String start_yyyyMMdd = Https.getStr(req, "start_yyyyMMdd", R.REQUIRED, R.D, "生效时间");
			String end_yyyyMMdd = Https.getStr(req, "end_yyyyMMdd", R.REQUIRED, R.D, "失效时间");
			Integer type = Https.getInt(req, "type", R.REQUIRED, R.I, R.IN, "in [0,1]", "发布类型");	
			String text = Https.getStr(req, "text", R.REQUIRED, "公告内容");	
			Integer versions = Https.getInt(req, "versions", R.I, "公告版本");
				if(noticeId!=null){Https.getInt(req, "versions", R.REQUIRED, "公告版本");}
			announcementService.noticeSave(noticeId, title, corpIds, roleIds, userIds, start_yyyyMMdd, end_yyyyMMdd, type, text, versions);		
			Code.ok(mb, (noticeId == null ? "新建" : "编辑") + "公告成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(AnnualRole:add) error: ", e);
			Code.error(mb, (noticeId == null ? "新建" : "编辑") + "公告失败");
		}

		return mb;
	}	
	@POST
	@At
	@Ok("json")
	public Object change(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String corpIds = Https.getStr(req, "modelOrgs", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			String roleIds = Https.getStr(req, "modelRoles", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "角色绑定");
			Integer[] arrCorp = Converts.array(corpIds, ",");
			Integer[] arrRole = Converts.array(roleIds, ",");
			Criteria cri = Cnd.cri();
			if(arrCorp!=null){
				cri.where().and("u.corp_id", "in", arrCorp);
			}
			if(arrRole!=null){
				cri.where().and("r.role_id", "in", arrRole);
			}
			cri.where().and("u.status","=",Status.ENABLED);
			List<User> users = mapper.query(User.class, "User.role", cri);
			mb.put("users",users);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Accountant:able) error: ", e);
			Code.error(mb, "查找用户");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			Integer noticeId = Https.getInt(req, "noticeId", R.I,R.REQUIRED);
			dao.update(Noticerecord.class, Chain.make("is_receive", 1).add("receive_time", new Date()), Cnd.where("noticeId", "=", noticeId).and("receive_id", "=", Context.getUserId()));
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Accountant:able) error: ", e);
			Code.error(mb, "");
		}

		return mb;
	}
}
