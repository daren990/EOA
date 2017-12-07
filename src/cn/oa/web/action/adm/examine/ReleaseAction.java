package cn.oa.web.action.adm.examine;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
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
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Org;
import cn.oa.model.PerformModel;
import cn.oa.model.Release;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MailC;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.examine.release")
@At(value = "/adm/examine/release")
public class ReleaseAction extends Action {

	public static Log log = Logs.getLog(ReleaseAction.class);
	
	@GET
	@At
	@Ok("ftl:adm/examine/release_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/examine/release/able", token);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
//		Cnds.eq(cri, mb, "r.user_id", "=", Context.getUserId());
//		mb.put(props);
		cri.where().and("r.corp_id", "=", Context.getCorpId());
		cri.getOrderBy().desc("r.modify_time");
		
		Page<Release> page = Webs.page(req);
		page = mapper.page(Release.class, page, "Release.count", "Release.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:");
			Integer[] arr = Converts.array(checkedIds, ",");
			
			Criteria cri = Cnd.cri();
			cri.where().and("status", "=", Status.ENABLED);
			if (!Asserts.isEmpty(arr)) {
				cri.where().and("corpId", "in", arr);
			}
			
			List<Release> nodes = dao.query(Release.class, cri);
			mb.put("nodes", nodes);
			
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Release:nodes) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:adm/examine/release_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer releaseId = Https.getInt(req, "releaseId", R.REQUIRED, R.I);
		Release release = null;
		if (releaseId != null) {
			release = mapper.fetch(Release.class, "Release.query", Cnd.where("r.release_id", "=", releaseId));
		}
		if (release == null){
			release = new Release();
			String corpName =  dao.fetch(Org.class, Context.getCorpId()).getOrgName();
			release.setCorpName(corpName);
		}

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		req.setAttribute("release", release);
		req.setAttribute("corps", corps);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer releaseId = null;
		try {
			CSRF.validate(req);
			
			releaseId = Https.getInt(req, "releaseId", R.I);
			String releaseName = Https.getStr(req, "releaseName", R.CLEAN, R.REQUIRED, "发布名称");
			String startStr = Https.getStr(req, "start_yyyyMMdd", R.REQUIRED, R.D, "开始时间");
			String endStr = Https.getStr(req, "end_yyyyMMdd", R.REQUIRED, R.D, "结束时间");
			String releaseStartStr = Https.getStr(req, "releaseStart_yyyyMMdd", R.REQUIRED, R.D, "考核开始时间");
			String releaseEndStr = Https.getStr(req, "releaseEnd_yyyyMMdd", R.REQUIRED, R.D, "考核结束时间");
			Integer version = Https.getInt(req, "version", R.REQUIRED, R.I, R.IN, "in [0,1]", "版本");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			DateTime now = new DateTime();
			Release release = null;
			
			Criteria cri = Cnd.cri();
			cri.where()
					.and("corpId", "=", Context.getCorpId())
					.and("status", "=", Status.ENABLED);
			if (releaseId != null)
				cri.where().and("releaseId", "!=", releaseId);
			
			if (status.equals(Status.ENABLED)) {
				int count = dao.count(Release.class, cri);
				if (count > 1) throw new Errors("禁止启用多个绩效发布");
			}
			if (releaseId != null) {
				release = dao.fetch(Release.class, releaseId);
				Asserts.isNull(release, "发布绩效不存在");
			} else {
				release = new Release();
				release.setCreateTime(now.toDate());
			}
			
			
			
			release.setReleaseStartDate(Calendars.parse(releaseStartStr, Calendars.DATE).toDate());
			release.setReleaseEndDate(Calendars.parse(releaseEndStr, Calendars.DATE).toDate());
			release.setUserId(Context.getUserId());
			release.setCorpId(Context.getCorpId());
			release.setReleaseName(releaseName);
			release.setStartDate(Calendars.parse(startStr, Calendars.DATE).toDate());
			release.setEndDate(Calendars.parse(endStr, Calendars.DATE).toDate());
			release.setVersion(version);
			release.setStatus(status);
			release.setModifyTime(now.toDate());

			if (releaseId != null)
				dao.update(release);
			else{
				dao.insert(release);
			}
//			if(status == Status.ENABLED){
			if(false){
				//发邮件.//
				List<PerformModel> list = dao.query(PerformModel.class, Cnd.where("corpId", "=", Context.getCorpId()));
				StringBuffer Sb = new StringBuffer();
				for(PerformModel p:list){
					Sb.append(p.getModelUsers());
				}
				Integer[] userIds =Converts.array(Sb.toString(), ",");
				List<User> users = dao.query(User.class, Cnd.where("userId","in",userIds).and("status", "=", Status.ENABLED));
				List<String> maiList = new ArrayList<String>();
				for(User u:users){
					if(u.getEmail()==null||"".equals(u.getEmail()))continue;	
					if(!MailC.checkEmail(u.getEmail()))continue;
					maiList.add(u.getEmail());
				}
				String subject = dao.fetch(Org.class, Cnd.where("org_id", "=", Context.getCorpId())).getOrgName();
				String content = null;
				if(version == 0){
					subject += " 已发布绩效考核!";
					content = "请"+"各位员工"+"登录OA填写绩效计划!";
				}else{
					subject += " 已发布绩效评分!";
					content = "请"+"各位员工"+"登录OA进行评分!";
				}
				//String content = "请"+"各位员工"+"登录OA进行确认";
				MailStart mail = new MailStart();
				String mailR = mail.mail(maiList,subject,content);
				Code.ok(mb, (releaseId == null ? "新建" : "编辑") + "发布绩效成功"+" , "+mailR+", 发送人数:"+maiList.size());
			}else{
				Code.ok(mb, (releaseId == null ? "新建" : "编辑") + "发布绩效成功");
			}
			
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Release:add) error: ", e);
			Code.error(mb, (releaseId == null ? "新建" : "编辑") + "发布绩效失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			
			if (arr != null && arr.length == 1) {
				Release release = dao.fetch(Release.class, arr[0]);
				if (release != null) {
					Criteria cri = Cnd.cri();
					cri.where()
							.and("corpId", "=", release.getCorpId())
							.and("status", "=", Status.ENABLED)
							.and("releaseId", "!=", arr[0]);
					
					if (status.equals(Status.ENABLED)) {
						int count = dao.count(Release.class, cri);
						if (count > 1) throw new Errors("禁止启用三个及以上的绩效发布");
					}
					
					dao.update(Release.class, Chain.make("status", status), Cnd.where("releaseId", "in", arr));
				}
			}
			Code.ok(mb, "设置发布绩效状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Release:able) error: ", e);
			Code.error(mb, "设置发布绩效状态失败");
		}

		return mb;
	}
}
