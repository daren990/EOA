package cn.oa.web.action.adm.salary.outwork;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import cn.oa.consts.ShiftC;
import cn.oa.consts.Status;
import cn.oa.consts.WxOpen;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Outwork;
import cn.oa.model.Role;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.ShiftCorp;
import cn.oa.model.User;
import cn.oa.model.WorkAttendance;
import cn.oa.model.WorkDay;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.helper.Works;
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

/**
 * 外勤申请
 * @author SimonTang
 */
@IocBean(name = "adm.salary.outwork.apply")
@At(value = "/adm/salary/outwork/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/salary/outwork/apply/del", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("w.status", "=", Status.ENABLED)
				.and("w.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "w.approve", "approve", approve);
		Cnds.gte(cri, mb, "w.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "w.create_time", "endTime", endStr);
		cri.getOrderBy().desc("w.modify_time");

		Page<Outwork> page = Webs.page(req);
		page = mapper.page(Outwork.class, page, "Outwork.count", "Outwork.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	@GET
	@At
	@Ok("ftl:adm/salary/outwork/apply_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/outwork/apply_page_wx_2")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer outworkId = Https.getInt(req, "outworkId", R.I);
		Outwork outwork = null;
		if (outworkId != null)
			outwork = dao.fetch(Outwork.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("userId", "=", Context.getUserId())
					.and("outworkId", "=", outworkId));
		if (outwork == null)
			outwork = new Outwork();
		
		User user = Context.getUser();
		User manager = dao.fetch(User.class, user.getManagerId());
		//获取用户的上级，目的是让用户选择审批的人
		List<User> operators = new ArrayList<User>();
		if(null != manager){
			operators.add(manager);
		} else {
			operators = userService.operators(user.getCorpId(), user.getLevel());
		}
//		List<User> operators = userService.operators(Context.getCorpId(), Context.getLevel());
		String name = req.getParameter("name");
		String flag = "";
		if(StringUtils.isNotBlank(name)){
			String date = req.getParameter("date");
			Shift shift = mapper.fetch(Shift.class, "Shiftinner.query",Cnd.where("s.user_id", "=", Context.getUser().getUserId()).and("s.shift_date", "=", date));
			ShiftClass shiftClass = dao.fetch(ShiftClass.class, shift.getClasses());
			if(ShiftC.ENABLED == shiftClass.getSecond()){ // 两头班
				if(name.equals("checkedIn")){
					outwork.setStartTime(Calendars.parse(date+" "+shiftClass.getFirstMorning(), Calendars.DATE_TIME).toDate());
					outwork.setEndTime(Calendars.parse(date+" "+shiftClass.getFirstNight(), Calendars.DATE_TIME).toDate());
					outwork.setType(0);
					flag = "in";
				}else{
					outwork.setStartTime(Calendars.parse(date+" "+shiftClass.getSecondMorning(), Calendars.DATE_TIME).toDate());
					outwork.setEndTime(Calendars.parse(date+" "+shiftClass.getSecondNight(), Calendars.DATE_TIME).toDate());
					outwork.setType(1);
					flag = "out";
				}
			} else { // 一头班
				if(name.equals("checkedIn")){
					outwork.setType(0);
					flag = "in";
				}else{
					outwork.setType(1);
					flag = "out";
				}
				outwork.setStartTime(Calendars.parse(date+" "+shiftClass.getFirstMorning(), Calendars.DATE_TIME).toDate());
				outwork.setEndTime(Calendars.parse(date+" "+shiftClass.getFirstNight(), Calendars.DATE_TIME).toDate());
			}
		}
		req.setAttribute("operators", operators);
		req.setAttribute("outwork", outwork);
		req.setAttribute("flag", flag);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/outwork/apply_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/outwork/apply_add_wx")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}

	public Object addPostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer outworkId = null;
		try {
			CSRF.validate(req);
			outworkId = Https.getInt(req, "outworkId", R.I);
			String work_yyyyMMdd = Https.getStr(req, "work_yyyyMMdd", R.REQUIRED, R.D, "外勤日期");
			Integer type = Https.getInt(req, "type", R.REQUIRED, R.I, R.IN, "in [0,1]", "外勤时间");
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "外勤原因");
			Integer operatorId = Https.getInt(req, "operatorId", R.CLEAN, R.REQUIRED, R.I, "上级审批");
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

			DateTime workDate = Calendars.parse(work_yyyyMMdd, Calendars.DATE);
			DateTime moment = new DateTime();
			int worksDayBetween;
			if (workDate.isBefore(pos))
				throw new Errors("外勤日期不能小于" + pos.toString("yyyy年MM月dd日"));
			
			if(moment.isBefore(workDate)){
				
				//提前7天包含休息日，延迟两天是工作日
				worksDayBetween = Works.dayBetween(Calendars.str(moment, Calendars.DATE), work_yyyyMMdd);
				if(worksDayBetween >7)
					throw new Errors("申请外勤最多能提前7天");
			}
				
			else{
				
				worksDayBetween = Works.workDay(work_yyyyMMdd, Calendars.str(moment, Calendars.DATE), monthMap, weeks);
				if(worksDayBetween >3){
					throw new Errors("申请外勤最多能延迟2个工作日");
				}
			}
			
			Outwork outwork = null;
			DateTime now = new DateTime();
			DateTime orgStart = null;
			DateTime orgEnd = null;

			if (outworkId != null) {
				outwork = dao.fetch(Outwork.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("outworkId", "=", outworkId));
				Asserts.isNull(outwork, "申请不存在");
				Asserts.notEqOr(outwork.getApprove(), Status.APPROVED, Status.APPROVEDOK, "禁止修改已审批的外勤申请");
				orgStart = new DateTime(outwork.getStartTime());
				orgEnd = new DateTime(outwork.getEndTime());
			} else {
				outwork = new Outwork();
				outwork.setStatus(Status.ENABLED);
				outwork.setCreateTime(now.toDate());
			}

			String startStr = work_yyyyMMdd + " " + (type.equals(Outwork.AM) ? day.getCheckIn() : day.getRestOut());
			String endStr = work_yyyyMMdd + " " + (type.equals(Outwork.AM) ? day.getRestIn() : day.getCheckOut());
			
			StringBuilder buff = new StringBuilder();
			buff.append(Context.getTrueName())
				.append("于")
				.append(work_yyyyMMdd)
				.append(type.equals(0) ? "上午" : "下午")
				.append("外勤");
			
			outwork.setUserId(Context.getUserId());
			outwork.setSubject(buff.toString());
			outwork.setStartTime(Calendars.parse(startStr, Calendars.DATE_TIME).toDate());
			outwork.setEndTime(Calendars.parse(endStr, Calendars.DATE_TIME).toDate());
			outwork.setType(type);
			outwork.setReason(reason);
			outwork.setOperatorId(operatorId);
			outwork.setApprove(Status.PROOFING);
			outwork.setModifyTime(now.toDate());
			outwork.setOpinion(Status.value);
			/*参数 	必须 	说明
			touser 	否 	UserID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
			toparty 否 	PartyID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
			totag 	否 	TagID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数
			agentid 是 	企业应用的id，整型。可在应用的设置页面查看
			title 	否 	标题
	    	description 否 	描述
			url 	否 	点击后跳转的链接。企业可根据url里面带的code参数校验员工的真实身份。具体参考“9 微信页面跳转员工身份查询”
			picurl 	否 	图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图640*320，小图80*80。如不填，在客户端不显示图片 */
			if(WxOpen.OUTWORK.isOPEN()){	
			WxSendService wx=new WxSendService();
			String atitle=titleName+"申请外勤";
			String description="外勤原因:"+outwork.getReason();
			List<Role> roles = roleRepository.find(operatorId);
			String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
			String agentid=WxRoleUtil.wxrole(roleNames);
			//actorName
			wx.sendarticle(actorName,"","",agentid,atitle,description,WxOpen.OUTWORK.getURL(),"");	
			//end of weixin!
			}
			outworkService.save(outworkId, outwork, orgStart, orgEnd, day, weeks, monthMap, Context.getUserId(), now);

			Code.ok(mb, (outworkId == null ? "新建" : "编辑") + "外勤申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (outworkId == null ? "新建" : "编辑") + "外勤申请失败");
		}

		return mb;
	}
	
	public Object newAddPostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer outworkId = null;
		try {
			CSRF.validate(req);
			outworkId = Https.getInt(req, "outworkId", R.I);
			String work_yyyyMMdd = Https.getStr(req, "work_yyyyMMdd", R.REQUIRED, R.D, "外勤日期");
			Integer type = Https.getInt(req, "type", R.REQUIRED, R.I, R.IN, "in [0,1]", "外勤时间");
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "外勤原因");
			Integer operatorId = Https.getInt(req, "operatorId", R.CLEAN, R.REQUIRED, R.I, "上级审批");
			String actorName = Https.getStr(req, operatorId.toString(), "{1,60}");
			String titleName = Https.getStr(req, "titleName", "{1,60}");
			
			WorkAttendance att = dao.fetch(WorkAttendance.class, Context.getCorpId());
			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());

			DateTime workDate = Calendars.parse(work_yyyyMMdd, Calendars.DATE);
			DateTime moment = new DateTime();
			int worksDayBetween;
			if (workDate.isBefore(pos))
				throw new Errors("外勤日期不能小于" + pos.toString("yyyy年MM月dd日"));
			
			if(moment.isBefore(workDate)){
				
				//提前7天包含休息日，延迟两天是工作日
				worksDayBetween = Works.dayBetween(Calendars.str(moment, Calendars.DATE), work_yyyyMMdd);
				if(worksDayBetween >7)
					throw new Errors("申请外勤最多能提前7天");
			}
			Outwork outwork = null;
			String typeName = "外勤（待审批）";
			//遍历排班,获取出差开始时间和结束时间
			Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
			Map<String, String[]> dayMap2 = new ConcurrentHashMap<String, String[]>();
			if (outworkId != null) {
				outwork = dao.fetch(Outwork.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("outworkId", "=", outworkId));
				Asserts.isNull(outwork, "申请不存在");
				Asserts.notEqOr(outwork.getApprove(), Status.APPROVED, Status.APPROVEDOK, "禁止修改已审批的外勤申请");
			
				outworkService.shift(Calendars.str(outwork.getStartTime(),Calendars.DATE), dayMap2,  typeName,Context.getUserId(),type);
			} else {
				outwork = new Outwork();
				outwork.setStatus(Status.ENABLED);
				outwork.setCreateTime(new Date());
			}
			//在外勤当天没有排班的情况下，会抛出异常，方法的作用是装载dayMap和外勤的开始时间和结束时间
			String[] stime = outworkService.shift(work_yyyyMMdd, dayMap,  typeName,Context.getUserId(),type);
			StringBuilder buff = new StringBuilder();
			buff.append(Context.getTrueName())
				.append("于")
				.append(work_yyyyMMdd)
				.append(type.equals(0) ? "上午" : "下午")
				.append("外勤");
			
			outwork.setUserId(Context.getUserId());
			outwork.setSubject(buff.toString());
			outwork.setStartTime(Calendars.parse(stime[0], Calendars.DATE_TIME).toDate());
			outwork.setEndTime(Calendars.parse(stime[1], Calendars.DATE_TIME).toDate());
			outwork.setType(type);
			outwork.setReason(reason);
			outwork.setOperatorId(operatorId);
			outwork.setApprove(Status.PROOFING);
			outwork.setModifyTime(new Date());
			outwork.setOpinion(Status.value);
			
			String url = null;
			url = "http://"+DomainName.OA+"/"+WxOpen.OUTWORK.getURL();
	
			transSave(outworkId, outwork, typeName, dayMap, dayMap2, actorName, operatorId, titleName, url);
			Code.ok(mb, (outworkId == null ? "新建" : "编辑") + "外勤申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (outworkId == null ? "新建" : "编辑") + "外勤申请失败");
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
				List<Outwork> outworks = mapper.query(Outwork.class, "Outwork.query", Cnd
						.where("w.status", "=", Status.ENABLED)
						.and("w.user_id", "=", Context.getUserId())
						.and("w.outwork_id", "in", arr));
				for (Outwork outwork : outworks) {
					if(outwork.getApprove() != Status.PROOFING){
						throw new Errors("禁止删除已审批的外勤申请!");
					}
					outworkService.delete(outwork);
				}
			}
			Code.ok(mb, "删除外勤申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除外勤申请失败");
		}
		return mb;
	}
	
	private void transSave(final Integer outWorkId, final Outwork outwork, final String typeName,
			final Map<String, String[]> dayMap,final Map<String, String[]> dayMap2,final String actorName,final Integer operatorId, final String titleName, final String url) {

		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (outWorkId != null) {
					dao.update(outwork);
					checkedRecordService.delete(outwork.getUserId(), dayMap2, Context.getUserId());
				} else {
					dao.insert(outwork);
				}
				//向考勤日志表插入或更新一条的记录，其中的内容参考dayMap
				checkedRecordService.update3(outwork.getUserId(), Context.getUserId(), dayMap);
			
				String description="外勤原因:"+outwork.getReason();
				String atitle = titleName + "申请外勤";
				
				//外勤微信通知
				if(WxOpen.OUTWORK.isOPEN()&&outWorkId == null&&wxUserService.outWork()){	
					WxSendService wx=new WxSendService();
					List<Role> roles = roleRepository.find(operatorId);
					String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
					String agentid=WxRoleUtil.wxrole(roleNames);
					wx.sendarticle(actorName,"","",agentid,atitle,description,url,"");	
				}
				
				//外勤邮件通知
				if (operatorId != 0) {
					User user = dao.fetch(User.class, Cnd.where("user_id", "=", operatorId));
					List<String> maiList = new ArrayList<String>();
					if (StringUtils.isNotBlank(user.getEmail())) {
						maiList.add(user.getEmail());
					}
					MailStart mail = new MailStart();
					mail.mail(maiList, atitle, description);
				}
			}
		});
	}
	
	
}
