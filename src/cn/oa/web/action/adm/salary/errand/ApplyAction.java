package cn.oa.web.action.adm.salary.errand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.DomainName;
import cn.oa.consts.Status;
import cn.oa.consts.WxOpen;
import cn.oa.model.Errand;
import cn.oa.model.Role;
import cn.oa.model.ShiftCorp;
import cn.oa.model.User;
import cn.oa.model.WorkAttendance;
import cn.oa.model.WorkDay;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
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
import cn.oa.web.action.wx.WxSendService;
import cn.oa.web.action.wx.comm.WxRoleUtil;

/*
 * 出差申请
 */
@IocBean(name = "adm.salary.errand.apply")
@At(value = "/adm/salary/errand/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/salary/errand/apply/del", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("e.status", "=", Status.ENABLED)
				.and("e.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "e.approve", "approve", approve);
		Cnds.gte(cri, mb, "e.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "e.create_time", "endTime", endStr);
		cri.getOrderBy().desc("e.modify_time");

		Page<Errand> page = Webs.page(req);
		page = mapper.page(Errand.class, page, "Errand.count", "Errand.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/errand/apply_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/errand/apply_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer errandId = Https.getInt(req, "errandId", R.I);
		Errand errand = null;
		if (errandId != null)
			errand = dao.fetch(Errand.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("userId", "=", Context.getUserId())
					.and("errandId", "=", errandId));
		if (errand == null)
			errand = new Errand();
		User user = Context.getUser();
		User manager = dao.fetch(User.class, user.getManagerId());
		List<User> operators = new ArrayList<User>();
		if(null != manager){
			operators.add(manager);
		} else {
			operators = userService.operators(user.getCorpId(), user.getLevel());
		}
		req.setAttribute("operators", operators);
		req.setAttribute("errand", errand);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/errand/apply_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/errand/apply_add_wx")
	public void wxadd(HttpServletRequest req){
		addUtil(req);
	}

	public Object addPostUtil(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer errandId = null;
		try {
			CSRF.validate(req);
			errandId = Https.getInt(req, "errandId", R.I);
			String start_yyyyMMdd = Https.getStr(req, "start_yyyyMMdd", R.REQUIRED, R.D, "开始时间");
			String end_yyyyMMdd = Https.getStr(req, "end_yyyyMMdd", R.REQUIRED, R.D, "结束时间");
			String place = Https.getStr(req, "place", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "出差地点");
			String project = Https.getStr(req, "project", R.CLEAN, R.RANGE, "{1,60}", "所属项目");
			String equipment = Https.getStr(req, "equipment", R.CLEAN, R.RANGE, "{1,60}", "需要设备");
			String content = Https.getStr(req, "content", R.CLEAN, R.REQUIRED, R.RANGE, "{1,200}", "工作描述");
			String remark = Https.getStr(req, "remark", R.CLEAN, R.RANGE, "{1,60}", "备注");
			Integer operatorId = Https.getInt(req, "operatorId", R.REQUIRED, R.I, "上级审批");
			String actorName = Https.getStr(req, operatorId.toString(), "{1,60}");
			String titleName = Https.getStr(req, "titleName", "{1,60}");
			Map<Integer, WorkDay> dayMap = workRepository.dayMap();
			Map<Integer, String[]> weekMap = workRepository.weekMap();
			Map<String, Integer[]> monthMap = workRepository.monthMap(Context.getCorpId());

			WorkDay day = dayMap.get(Context.getDayId());
			Asserts.isNull(day, "日排班不能为空值");
			String[] weeks = weekMap.get(Context.getWeekId());
			Asserts.isEmpty(weeks, "周排班不能为空值");
			
			WorkAttendance att = dao.fetch(WorkAttendance.class, Context.getCorpId());
			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());

			DateTime start = Calendars.parse(start_yyyyMMdd + " " + day.getCheckIn(), Calendars.DATE_TIME);
			DateTime end = Calendars.parse(end_yyyyMMdd + " " + day.getCheckOut(), Calendars.DATE_TIME);
			
			if (start.isBefore(pos))
				throw new Errors("出差日期不能小于" + pos.toString("yyyy年MM月dd日"));

			Errand errand = null;
			DateTime now = new DateTime();
			DateTime orgStart = null;
			DateTime orgEnd = null;

			if (errandId != null) {
				errand = dao.fetch(Errand.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("errandId", "=", errandId));
				Asserts.isNull(errand, "申请不存在");
				Asserts.notEqOr(errand.getApprove(), Status.APPROVED, Status.OK, "禁止修改已审批的出差申请");
				orgStart = new DateTime(errand.getStartTime());
				orgEnd = new DateTime(errand.getEndTime());
			} else {
				errand = new Errand();
				errand.setStatus(Status.ENABLED);
				errand.setCreateTime(now.toDate());
			}

			StringBuilder buff = new StringBuilder();
			buff.append(Context.getTrueName())
				.append("于")
				.append(Calendars.str(now, Calendars.DATE))
				.append("申请出差");			
			errand.setUserId(Context.getUserId());
			errand.setSubject(buff.toString());
			errand.setStartTime(start.toDate());
			errand.setEndTime(end.toDate());
			errand.setPlace(place);
			errand.setProject(project);
			errand.setEquipment(equipment);
			errand.setContent(content);
			errand.setRemark(remark);
			errand.setOperatorId(operatorId);
			errand.setApprove(Status.PROOFING);
			errand.setModifyTime(now.toDate());
			errand.setOpinion(Status.value);
			/*参数 	必须 	说明
			touser 	否 	UserID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
			toparty 否 	PartyID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
			totag 	否 	TagID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
			agentid 是 	企业应用的id，整型。可在应用的设置页面查看
			title 	否 	标题
	    	description 否 	描述
			url 	否 	点击后跳转的链接。企业可根据url里面带的code参数校验员工的真实身份。具体参考“9 微信页面跳转员工身份查询”
			picurl 	否 	图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图640*320，小图80*80。如不填，在客户端不显示图片 */
			if(WxOpen.ERRAND.isOPEN()){	
			WxSendService wx=new WxSendService();
			String atitle=titleName+"申请出差";
			String description="出差描述:"+errand.getContent();
			List<Role> roles = roleRepository.find(operatorId);
			String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
			String agentid=WxRoleUtil.wxrole(roleNames);
			//actorName
			wx.sendarticle(actorName,"","",agentid,atitle,description,WxOpen.ERRAND.getURL(),"");	
			//end of weixin!
			}
			errandService.save(errandId, errand, orgStart, orgEnd, day, weeks, monthMap, Context.getUserId(), now);

			Code.ok(mb, (errandId == null ? "新建" : "编辑") + "出差申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (errandId == null ? "新建" : "编辑") + "出差申请失败");
		}

		return mb;
	}
	public Object newAddPostUtil(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer errandId = null;
		try {
			CSRF.validate(req);
			errandId = Https.getInt(req, "errandId", R.I);
			String start_yyyyMMdd = Https.getStr(req, "start_yyyyMMdd", R.REQUIRED, R.D, "开始时间");
			String end_yyyyMMdd = Https.getStr(req, "end_yyyyMMdd", R.REQUIRED, R.D, "结束时间");
			String place = Https.getStr(req, "place", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "出差地点");
			String project = Https.getStr(req, "project", R.CLEAN, R.RANGE, "{1,60}", "所属项目");
			String equipment = Https.getStr(req, "equipment", R.CLEAN, R.RANGE, "{1,60}", "需要设备");
			String content = Https.getStr(req, "content", R.CLEAN, R.REQUIRED, R.RANGE, "{1,200}", "工作描述");
			String remark = Https.getStr(req, "remark", R.CLEAN, R.RANGE, "{1,60}", "备注");
			Integer operatorId = Https.getInt(req, "operatorId", R.REQUIRED, R.I, "上级审批");
			String actorName = Https.getStr(req, operatorId.toString(), "{1,60}");
			String titleName = Https.getStr(req, "titleName", "{1,60}");
			
			WorkAttendance att = dao.fetch(WorkAttendance.class, Context.getCorpId());
			Asserts.isNull(att, "最近考勤周期未配置");

			String typeName = "出差（待审批）";			
			//遍历排班,获取出差开始时间和结束时间
			Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
			Map<String, String[]> dayMap2 = new ConcurrentHashMap<String, String[]>();
			String[] stime = errandService.shift(start_yyyyMMdd, end_yyyyMMdd, dayMap,  typeName,Context.getUserId());
			if(errandId != null){
				Errand oldErrand = dao.fetch(Errand.class, errandId);
				errandService.shift(Calendars.str(oldErrand.getStartTime(),Calendars.DATE), Calendars.str(oldErrand.getEndTime(),Calendars.DATE),  dayMap2,  typeName,Context.getUserId());				
			}
			Errand errand = null;
			DateTime now = new DateTime();

			if (errandId != null) {
				errand = dao.fetch(Errand.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("errandId", "=", errandId));
				Asserts.isNull(errand, "申请不存在");
				Asserts.notEqOr(errand.getApprove(), Status.APPROVED, Status.OK, "禁止修改已审批的出差申请");
			} else {
				errand = new Errand();
				errand.setStatus(Status.ENABLED);
				errand.setCreateTime(now.toDate());
			}

			StringBuilder buff = new StringBuilder();
			buff.append(Context.getTrueName())
				.append("于")
				.append(Calendars.str(now, Calendars.DATE))
				.append("申请出差");			
			errand.setUserId(Context.getUserId());
			errand.setSubject(buff.toString());
			if(stime[0]==null||stime[1]==null){throw new Errors("该时间段班次没有设置");}
			errand.setStartTime(Calendars.parse(stime[0], Calendars.DATE_TIME).toDate());
			errand.setEndTime(Calendars.parse(stime[1], Calendars.DATE_TIME).toDate());
			errand.setPlace(place);
			errand.setProject(project);
			errand.setEquipment(equipment);
			errand.setContent(content);
			errand.setRemark(remark);
			errand.setOperatorId(operatorId);
			errand.setApprove(Status.PROOFING);
			errand.setModifyTime(now.toDate());
			errand.setOpinion(Status.value);
			
			String atitle=titleName+"申请出差";
			String url = null;
			url = "http://"+DomainName.OA+"/"+WxOpen.ERRAND.getURL();

			transSave(errandId, errand, typeName, dayMap,dayMap2,atitle,url,actorName,operatorId);
			Code.ok(mb, (errandId == null ? "新建" : "编辑") + "出差申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (errandId == null ? "新建" : "编辑") + "出差申请失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		//获取公司使用的排班类型
		ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class,"ShiftCorp.query",Cnd.where("o.status","=",Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", Context.getCorpId()));
		//新排班
		if(shiftCorp!=null){
			return newAddPostUtil(req,res);
		}else{
			return addPostUtil(req,res);
		}
	}
	
	@POST
	@At
	@Ok("json")
	public Object del(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				List<Errand> errands = mapper.query(Errand.class, "Errand.query", Cnd
						.where("e.status", "=", Status.ENABLED)
						.and("e.user_id", "=", Context.getUserId())
						.and("e.approve", "=", Status.PROOFING)
						.and("e.errand_id", "in", arr));
				for (Errand errand : errands) {
					if(errand.getApprove()==Status.APPROVED){
						throw new Errors("禁止删除已审批的出差申请!");
					}
					errandService.delete(errand);
				}
			}
			Code.ok(mb, "删除出差申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除出差申请失败");
		}
		return mb;
	}
	
	private void transSave(final Integer errandId, final Errand errand,
			final String typeName,final Map<String, String[]> dayMap,final Map<String, String[]> dayMap2,final String atitle,final String url,final String actorName,final Integer operatorId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (errandId != null) {
					dao.update(errand);
					checkedRecordService.delete(errand.getUserId(), dayMap2, Context.getUserId());
				} else {
					dao.insert(errand).getErrandId();
				}
				checkedRecordService.update3(errand.getUserId(), Context.getUserId(), dayMap);						
				if(WxOpen.ERRAND.isOPEN()&&errandId==null&&wxUserService.errand()){
					WxSendService wx=new WxSendService();
					String description="出差描述:"+errand.getContent();
					List<Role> roles = roleRepository.find(operatorId);
					String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
					String agentid=WxRoleUtil.wxrole(roleNames);
					wx.sendarticle(actorName,"","",agentid,atitle,description,url,"");	
				}
			}
		});
	}
	
}
