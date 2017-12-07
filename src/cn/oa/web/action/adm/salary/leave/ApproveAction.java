package cn.oa.web.action.adm.salary.leave;

import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.entity.Record;
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

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.consts.WxOpen;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Dict;
import cn.oa.model.Leave;
import cn.oa.model.LeaveActor;
import cn.oa.model.Outwork;
import cn.oa.model.OvertimeRest;
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
import cn.oa.utils.Values;
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
 * 请假审批
 * 
 * @author SimonTang
 */
@IocBean(name = "adm.salary.leave.approve")
@At(value = "/adm/salary/leave/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/salary/leave/approve/actors", token);
		CSRF.generate(req, "/adm/salary/leave/approve/able", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where().and("l.status", "=", Status.ENABLED).and("a.actor_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "l.approve", "approve", approve);
		Cnds.gte(cri, mb, "l.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "l.create_time", "endTime", endStr);
		cri.getOrderBy().desc("l.modify_time");

		Page<Leave> page = Webs.page(req);
		page = mapper.page(Leave.class, page, "LeaveApprove.count", "LeaveApprove.index", cri);
		//遍历请假信息结果集，计算请假天数
		for (Leave l:page.getResult()){
			//获取所有请假用户ID
			int userId = l.getUserId();
			//转换请假开始日期时间为yyyy-MM-dd
			String mTime = Calendars.str(l.getStartTime(),"yyyy-MM-dd");
			//按照用户id与请假开始时间查询出排班
			Shift shift = mapper.fetch(Shift.class, "Shiftinner.query",Cnd.where("s.user_id", "=", userId).and("s.shift_date", "=", mTime));
			//为了数据统一，不进行计算而是再次查询排班表获得当前用户排班
//			Record re = dao.fetch("conf_shift_class", Cnd.where("class_id", "=", shift.getClasses())); 
			//得到上班时间，转换为分钟
//			Float minutes = Float.valueOf(re.getString("sum_time"))*60;
			//请假时间占比上班时间，取小数点后一位
//			String days = new DecimalFormat("0.0").format((float)l.getLeaveMinute()/minutes);
			//详情看Leave类的days属性getter和setter
//	     	l.setDays(days);
		}
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@GET
	@At
	@Ok("ftl:adm/salary/leave/approve_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}

//	@GET
//	@At
//	@Ok("ftl:adm/salary/leave/approve_page_wx")
//	public void wxpage(HttpServletRequest req) {
//		pageUtil(req);
//	}

	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer leaveId = Https.getInt(req, "leaveId", R.REQUIRED, R.I, "申请ID");

		Leave leave = mapper.fetch(Leave.class, "LeaveApprove.query",
				Cnd.where("l.status", "=", Status.ENABLED).and("a.actor_id", "=", Context.getUserId()).and("l.leave_id", "=", leaveId));
		Asserts.isNull(leave, "申请不存在");

		List<LeaveActor> actors = mapper.query(LeaveActor.class, "LeaveActor.query", Cnd.where("a.leave_id", "=", leaveId).asc("a.modify_time"));
		LeaveActor actor = null; // 当前审批人员
		LeaveActor next = null; // 指向下一审批人员

		Integer bindId = null;
		for (LeaveActor e : actors) {
			// 当前审批人
			if (e.getActorId().equals(Context.getUserId()))
				actor = e;
			// 第一个审批人
			if (e.getVariable().equals(Roles.SVI.getName()))
				bindId = e.getActorId();
		}
		// 下一个审批人
		if (actor.getRefererId() != 0) {
			for (LeaveActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId()))
					next = e;
			}
		}
		// 审批人
		List<User> operators = new ArrayList<User>();
		User user = Context.getUser();
		Boolean show = false;
		if (leave.getUserId() != null) {

			String[] svi = userService.findRoleNames(Context.getUserId());
			if (Asserts.hasAny(Roles.SVI.getName(), svi)) {
				show = true;
				User operators1 = dao.fetch(User.class, Context.getUser().getManagerId());
				if (operators1 != null) {
					operators.add(operators1);
					req.setAttribute("operators", operators);
					bindId = operators1.getUserId();
				} else {
					operators = userService.operators(user.getCorpId(), user.getLevel());
					req.setAttribute("operators", operators);
				}
			} else {

				req.setAttribute("operators", operators);
			}
		} else
			Asserts.isNull(leave, "不能审批该申请");
		req.setAttribute("leave", leave);
		req.setAttribute("leaveMap", dictService.map(Dict.LEAVE));
		req.setAttribute("actors", actors);
		req.setAttribute("actor", actor);
		req.setAttribute("next", next);
		req.setAttribute("bindId", bindId);
		req.setAttribute("show", show);	
	}

	@GET
	@At
	@Ok("ftl:adm/salary/leave/approve_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}

	@GET
	@At
	@Ok("ftl:adm/salary/leave/approve_add_wx")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}

	private Object newAddUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		WxSendService wx = new WxSendService();
		try {
			CSRF.validate(req);
			Integer leaveId = Https.getInt(req, "leaveId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			String titleName = Https.getStr(req, "titleName", "{1,60}");
			Leave leave = mapper.fetch(Leave.class, "LeaveApprove.query",
					Cnd.where("l.status", "=", Status.ENABLED).and("a.actor_id", "=", Context.getUserId()).and("l.leave_id", "=", leaveId));
	//		Asserts.isNull(leave, "申请不存在");

			LeaveActor actor = dao.fetch(LeaveActor.class, Cnd.where("leaveId", "=", leaveId).and("actorId", "=", Context.getUserId()));
	//		Asserts.isNull(actor, "禁止审批请假申请");

			WorkAttendance att = dao.fetch(WorkAttendance.class, leave.getCorpId());
//			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());

			DateTime start = new DateTime(leave.getStartTime());

//			if (start.isBefore(pos))
//				throw new Errors("考勤结束日期禁止审批");

			Integer required = R.CLEAN;
			if ((actor.getVariable().equals(Roles.SVI.getName()) && approve.equals(Status.APPROVED))) {
				required = R.REQUIRED;
			}
			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			if (refererId == null && (leave.getApprove().equals(Status.OK)//
					|| leave.getApprove().equals(Status.UNAPPROVED) || //
					leave.getApprove().equals(Status.APPROVED))) {
				throw new Errors("审批已通过，不能再修改");
			}
			
			
			String actorName = null;
			if (refererId != null)
				actorName = Https.getStr(req, refererId.toString(), "{1,60}");
			refererId = Values.getInt(refererId);

			String nextVariable = null;
			String roleNames[] = userService.findRoleNames(Context.getUserId());
			if (approve.equals(Status.APPROVED)) {
				if (Asserts.hasAny(Roles.SVI.getName(), roleNames)) {
					nextVariable = Roles.MAN.getName();
					approve = Status.APPROVED;
				} else
					approve = Status.APPROVED;
			}

			else
				approve = Status.UNAPPROVEDOK;

			DateTime now = new DateTime();

			Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
			DateTime startDate = Calendars.parse(leave.getStartTime(), Calendars.DATE);
			DateTime endDate = Calendars.parse(leave.getEndTime(), Calendars.DATE);
			// 请假类型
			Map<String, String> leaveMap = dictService.map(Dict.LEAVE);
			String typeName = leaveMap.get(String.valueOf(leave.getTypeId()));
			
			String start_yyyyMMdd = startDate.toString("yyyy-MM-dd");
			String end_yyyyMMdd = endDate.toString("yyyy-MM-dd");
			List<Shift> shifts = dao.query(Shift.class, Cnd.where("shift_date", ">=", start_yyyyMMdd).and("shift_date", "<=", end_yyyyMMdd).and("user_id", "=", leave.getUserId()));
			if (shifts.size() == 0) {
//				throw new Errors("排班在请假后已被修改,无法审批");
			} else {
				for (Shift s : shifts) {
					if (s.getClasses() == null) {
						continue;
					}
					ShiftClass shiftClass = dao.fetch(ShiftClass.class, s.getClasses());
					if (shiftClass == null) {
						continue;
					}
					String restIn = null;
					String restOut = null;
					String checkIn = shiftClass.getFirstMorning();
					String checkOut = null;
					// boolean once = false;
					// 二头班
					if (shiftClass.getSecond() == 1) {
						restIn = shiftClass.getFirstNight();
						restOut = shiftClass.getSecondMorning();
						checkOut = shiftClass.getSecondNight();
					} else {
						checkOut = shiftClass.getFirstNight();
						// 获取两个时间的中间值,2015-2-6 为任意时间
						DateTime fm = Calendars.parse("2015-2-6 " + checkIn, Calendars.DATE_TIME);
						DateTime fn = Calendars.parse("2015-2-6 " + checkOut, Calendars.DATE_TIME);
						long beginDate = (fn.toDate().getTime() + fm.toDate().getTime()) / 2;
						SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
						String middle = sdf.format(new Date(beginDate));
						restIn = restOut = middle;
						// once = true;
					}

					DateTime restInTime = Calendars.parse(start_yyyyMMdd + " " + restIn, Calendars.DATE_TIME);
					DateTime restOutTime = Calendars.parse(end_yyyyMMdd + " " + restOut, Calendars.DATE_TIME);
					int days = Days.daysBetween(Calendars.parse(start_yyyyMMdd, Calendars.DATE), Calendars.parse(end_yyyyMMdd, Calendars.DATE)).getDays();
					if (days == 0) {
						if (shiftClass.getSecond() != 1) {
							dayMap.put(start_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
						} else {
							if (startDate.isBefore(restInTime) && endDate.isBefore(restOutTime)) {
								dayMap.put(start_yyyyMMdd, new String[] { "1", null, checkIn, checkOut, restIn, restOut });
							} else if (startDate.isBefore(restInTime) && endDate.isAfter(restOutTime)) {
								dayMap.put(start_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
							} else if (startDate.isAfter(restInTime) && endDate.isAfter(restOutTime)) {
								dayMap.put(start_yyyyMMdd, new String[] { null, "1", checkIn, checkOut, restIn, restOut });
							} else {
								throw new Errors("审批无法完成,必须重新申请请假!");
							}
						}
					} else if (days > 0) {
						if (start_yyyyMMdd.equals(Calendars.str(s.getShiftDate(), Calendars.DATE))) {
							if (shiftClass.getSecond() != 1) {
								dayMap.put(start_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
							} else {
								if (startDate.isBefore(restInTime)) {
									dayMap.put(start_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
								} else if (startDate.isAfter(restInTime)) {
									dayMap.put(start_yyyyMMdd, new String[] { null, "1", checkIn, checkOut, restIn, restOut });
								} else {
									throw new Errors("审批无法完成,必须重新申请请假!");
								}
							}
						} else if (end_yyyyMMdd.equals(Calendars.str(s.getShiftDate(), Calendars.DATE))) {
							if (shiftClass.getSecond() != 1) {
								dayMap.put(end_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
							} else {
								if (endDate.isBefore(restOutTime)) {
									dayMap.put(end_yyyyMMdd, new String[] { "1", null, checkIn, checkOut, restIn, restOut });
								} else if (endDate.isAfter(restOutTime)) {
									dayMap.put(end_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
								} else {
									throw new Errors("审批无法完成,必须重新申请请假!");
								}
							}
						} else {
							dayMap.put(Calendars.str(s.getShiftDate(), Calendars.DATE), new String[] { "1", "1" });
						}
					}
				}
			}

			/*
			 * 参数 必须 说明 touser 否
			 * UserID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送 toparty
			 * 否 PartyID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数 totag 否
			 * TagID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数 agentid 是
			 * 企业应用的id，整型。可在应用的设置页面查看 title 否 标题 description 否 描述 url 否
			 * 点击后跳转的链接。企业可根据url里面带的code参数校验员工的真实身份。具体参考“9 微信页面跳转员工身份查询” picurl
			 * 否 图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图640*320，小图80*80。如不填，在客户端不显示图片
			 */
			if (!(actorName == null) && WxOpen.LEAVE_APPROVE.isOPEN()) {
				String atitle = titleName + "申请请假";
				String description = "请假原因:" + leave.getReason();
				List<Role> roles = roleRepository.find(refererId);
				String[] roleNames1 = Converts.array(Role.class, String.class, roles, "roleName");
				String agentid = WxRoleUtil.wxrole(roleNames1);
				wx.sendarticle(actorName, "", "", agentid, atitle, description, WxOpen.LEAVE_APPROVE.getURL(), "");
				// end of weixin!
				
			}
			if (refererId != 0) {
				User user = dao.fetch(User.class, Cnd.where("user_id", "=", refererId));
				List<String> maiList = new ArrayList<String>();
				if (StringUtils.isNotBlank(user.getEmail())) {
					maiList.add(user.getEmail());
				}
				MailStart mail = new MailStart();
				mail.mail(maiList, leave.getSubject(), leave.getReason());
			}
			transApprove2(leaveId, leave, null, null, typeName, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, Context.getUserId(), opinion,
					dayMap);

			Code.ok(mb, "请假申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "请假申请审批失败");
		}

		return mb;
	}

	private Object addUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		WxSendService wx = new WxSendService();
		try {
			CSRF.validate(req);
			Integer leaveId = Https.getInt(req, "leaveId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			// String actorName = Https.getStr(req, "actorName", "{1,60}");
			String titleName = Https.getStr(req, "titleName", "{1,60}");
			Leave leave = mapper.fetch(Leave.class, "LeaveApprove.query",
					Cnd.where("l.status", "=", Status.ENABLED).and("a.actor_id", "=", Context.getUserId()).and("l.leave_id", "=", leaveId));
			Asserts.isNull(leave, "申请不存在");

			LeaveActor actor = dao.fetch(LeaveActor.class, Cnd.where("leaveId", "=", leaveId).and("actorId", "=", Context.getUserId()));
//			Asserts.isNull(actor, "禁止审批请假申请");

			WorkAttendance att = dao.fetch(WorkAttendance.class, leave.getCorpId());
//			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());

			DateTime start = new DateTime(leave.getStartTime());

//			if (start.isBefore(pos))
//				throw new Errors("考勤结束日期禁止审批");

			Integer required = R.CLEAN;
			if ((actor.getVariable().equals(Roles.SVI.getName()) && approve.equals(Status.APPROVED))) {
				required = R.REQUIRED;
			}

			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			String actorName = null;
			if (refererId != null)
				actorName = Https.getStr(req, refererId.toString(), "{1,60}");
			refererId = Values.getInt(refererId);

			String nextVariable = null;
			String roleNames[] = userService.findRoleNames(Context.getUserId());
			if (approve.equals(Status.APPROVED)) {
				if (Asserts.hasAny(Roles.SVI.getName(), roleNames)) {
					nextVariable = Roles.MAN.getName();
					approve = Status.APPROVED;
				} else
					approve = Status.APPROVED;
			}

			else
				approve = Status.UNAPPROVEDOK;

			DateTime now = new DateTime();
			// save(leave, approve, Context.getUserId(), now);
			// save(leave, approve, Context.getUserId(), now ,refererId,
			// nextVariable);

			Map<Integer, WorkDay> dayMap = workRepository.dayMap();
			Map<Integer, String[]> weekMap = workRepository.weekMap();
			Map<String, Integer[]> monthMap = workRepository.monthMap(leave.getCorpId());

			WorkDay day = dayMap.get(leave.getDayId());
			String[] weeks = weekMap.get(leave.getWeekId());

			Map<String, String> leaveMap = dictService.map(Dict.LEAVE);
			String typeName = leaveMap.get(String.valueOf(leave.getTypeId()));
			/*
			 * 参数 必须 说明 touser 否
			 * UserID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送 toparty
			 * 否 PartyID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数 totag 否
			 * TagID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数 agentid 是
			 * 企业应用的id，整型。可在应用的设置页面查看 title 否 标题 description 否 描述 url 否
			 * 点击后跳转的链接。企业可根据url里面带的code参数校验员工的真实身份。具体参考“9 微信页面跳转员工身份查询” picurl
			 * 否 图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图640*320，小图80*80。如不填，在客户端不显示图片
			 */
			if (!(actorName == null) && WxOpen.LEAVE_APPROVE.isOPEN()) {
				String atitle = titleName + "申请请假";
				String description = "请假原因:" + leave.getReason();
				List<Role> roles = roleRepository.find(refererId);
				String[] roleNames1 = Converts.array(Role.class, String.class, roles, "roleName");
				String agentid = WxRoleUtil.wxrole(roleNames1);
				wx.sendarticle(actorName, "", "", agentid, atitle, description, WxOpen.LEAVE_APPROVE.getURL(), "");
				// end of weixin!
			}
			transApprove(leaveId, leave, null, null, typeName, day, weeks, monthMap, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now,
					Context.getUserId(), opinion);

			Code.ok(mb, "请假申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "请假申请审批失败");
		}

		return mb;
	}

	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		// 获取公司使用的排班类型
		ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class, "ShiftCorp.query",
				Cnd.where("o.status", "=", Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", Context.getCorpId()));
		// 新排班
		if (shiftCorp != null) {
			return newAddUtil(req, res);
		} else {
			return addUtil(req, res);
		}
	}

	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer leaveId = Https.getInt(req, "leaveId", R.REQUIRED, R.I, "申请ID");
			List<LeaveActor> actors = mapper.query(LeaveActor.class, "LeaveActor.query", Cnd.where("a.leave_id", "=", leaveId).asc("a.modify_time"));
			mb.put("actors", actors);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:actors) error: ", e);
		}

		return mb;
	}

	private void transApprove(final Integer leaveId, final Leave leave, final DateTime orgStart, final DateTime orgEnd, final String typeName, final WorkDay day,
			final String[] weeks, final Map<String, Integer[]> monthMap, final Integer approve, final Integer actorId, final String variable, final Integer refererId,
			final String nextVariable, final DateTime now, final Integer modifyId, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.PROOFING;
				dao.update(LeaveActor.class, Chain.make("approve", approve).add("refererId", refererId).add("modifyTime", now.toDate()).add("opinion", opinion),
						Cnd.where("leaveId", "=", leaveId).and("actorId", "=", actorId));
				dao.clear(LeaveActor.class, Cnd.where("leaveId", "=", leaveId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						dao.insert(new LeaveActor(leaveId, refererId, Value.F, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Value.I, Status.value));
					self = refererId.equals(Value.I) ? Status.OK : Status.PROOFING;
				}

				else if (approve.equals(Status.UNAPPROVED) && refererId.equals(Value.I)) {
					self = Status.UNAPPROVED;
				}

				dao.update(Leave.class, Chain.make("approve", self).add("modifyTime", now.toDate()), Cnd.where("leaveId", "=", leaveId));

				leaveService.save(leave, orgStart, orgEnd, typeName, day, weeks, monthMap, modifyId, now, self);
			}

		});

	}

	private void transApprove2(final Integer leaveId, final Leave leave, final DateTime orgStart, final DateTime orgEnd, final String typeName, final Integer approve,
			final Integer actorId, final String variable, final Integer refererId, final String nextVariable, final DateTime now, final Integer modifyId, final String opinion,
			final Map<String, String[]> remarkMap) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				//只有当self变成Status.UNAPPROVED或Status.APPROVED，审批才有结果，注意变化的条件
				Integer self = Status.PROOFING;
				//根据id更新请假日志表中的approve的值，以免被接下来的clear删除
				dao.update(LeaveActor.class, Chain.make("approve", approve).add("refererId", refererId).add("modifyTime", now.toDate()).add("opinion", opinion),
						Cnd.where("leaveId", "=", leaveId).and("actorId", "=", actorId));
				dao.clear(LeaveActor.class, Cnd.where("leaveId", "=", leaveId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						//插入下一级审批人员在LeaveActor中生成的记录，此时下一级的审批人员才可以收到通知
						dao.insert(new LeaveActor(leaveId, refererId, Value.F, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(), Value.I, Status.value));
					self = refererId.equals(Value.I) ? Status.OK : Status.PROOFING;
				}
				
				//从条件可以知道，只有当最上级的领导不批准，才会真正的不批准
				else if (approve.equals(Status.UNAPPROVED) && refererId.equals(Value.I)) {
					self = Status.UNAPPROVED;
					//根据请假的开始时间和持续长度查询OvertimeRest表中匹配的记录，目的是销掉原本申请成功的加班补休
					List<OvertimeRest> ovs = dao.query(OvertimeRest.class, Cnd.where("userid", "=", leave.getUserId())//
							.and("start_time", "=", leave.getStartTime())//
							.and("work_minute", "=", leave.getLeaveMinute()));
					if (ovs != null && ovs.size() > 0) {
						dao.delete(ovs.get(0));
					}
				}

				//补休的请假申请成功，因此插入OvertimeRest中的一条记录
				if (approve.equals(Status.APPROVED) && refererId.equals(Value.I)) {
					if (leave.getTypeId() == Status.LEAVE_TYPE_REST) {
						OvertimeRest or = new OvertimeRest();
						or.setCreteDateTime(new Date());
						or.setType(Status.TYPE_REST);
						or.setUserid(leave.getUserId());
						or.setWorkMinute(leave.getLeaveMinute());
						or.setStartTime(leave.getStartTime());
						dao.insert(or);
					}
				}
				//假设存在再上一级的审批，即!refererId.equals(Value.I),那么此时的self为Status.PROOFING，导致恢复成Status.PROOFING
				dao.update(Leave.class, Chain.make("approve", self).add("modifyTime", now.toDate()), Cnd.where("leaveId", "=", leaveId));
				leaveService.oparateSave(leave, typeName, modifyId, now, self, remarkMap);
			}
		});

	}
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		//获取公司使用的排班类型
		ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class,"ShiftCorp.query",Cnd.where("o.status","=",Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", Context.getCorpId()));

		//新排班
		if(shiftCorp!=null){
			return newAblePostUtil(req,res);
		}else{
			return null;
		}
	}
	
	public Object newAblePostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			String opinion = "-----";
			
			System.out.println(checkedIds);
			
			if (arr != null && arr.length > 0) {
				List<WorkAttendance> atts = dao.query(WorkAttendance.class, null);
				Map<Integer, WorkAttendance> attMap = new ConcurrentHashMap<Integer, WorkAttendance>();
				for (WorkAttendance att : atts)
					attMap.put(att.getOrgId(), att);

				List<Leave> leaves = mapper.query(Leave.class, "LeaveApprove.query", Cnd
						.where("l.status", "=", Status.ENABLED)
						.and("a.actor_id", "=", Context.getUserId())
						.and("l.leave_id", "in", arr));
				Asserts.isEmpty(leaves, "申请不存在");

				
				for(Leave leave : leaves){
					LeaveActor actor = dao.fetch(LeaveActor.class, Cnd.where("leaveId", "=", leave.getLeaveId()).and("actorId", "=", Context.getUserId()));
					if(actor == null){
						continue;
					}
					System.out.println(leave.getCorpId());
					WorkAttendance att = attMap.get(leave.getCorpId());
	//				Asserts.isNull(att, "最近考勤周期未配置");
					
					
					DateTime pos = new DateTime(att.getEndDate());

					DateTime start = new DateTime(leave.getStartTime());
	
	//				if (start.isBefore(pos))
	//					throw new Errors("考勤结束日期禁止审批");

					Integer required = R.CLEAN;
					if ((actor.getVariable().equals(Roles.SVI.getName()) && approve.equals(Status.APPROVED))) {
						required = R.REQUIRED;
					}
					Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
					if (refererId == null && (leave.getApprove().equals(Status.OK)//
							|| leave.getApprove().equals(Status.UNAPPROVED) || //
							leave.getApprove().equals(Status.APPROVED))) {
						continue;
					}
					
					
					String actorName = null;
					if (refererId != null)
						actorName = Https.getStr(req, refererId.toString(), "{1,60}");
					//假如refererId为null，该操作会导致refererId变为0
					refererId = Values.getInt(refererId);

					String nextVariable = null;
					String roleNames[] = userService.findRoleNames(Context.getUserId());
					if (approve.equals(Status.APPROVED)) {
						if (Asserts.hasAny(Roles.SVI.getName(), roleNames)) {
							nextVariable = Roles.MAN.getName();
							approve = Status.APPROVED;
						} else
							approve = Status.APPROVED;
					}else
						approve = Status.UNAPPROVEDOK;
					
					//如果存在下一级审批，暂时不允许批量操作
					if(nextVariable != null && refererId.equals(0)){
						continue;
					}
					
					DateTime now = new DateTime();

					Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
					DateTime startDate = Calendars.parse(leave.getStartTime(), Calendars.DATE);
					DateTime endDate = Calendars.parse(leave.getEndTime(), Calendars.DATE);
					// 请假类型
					Map<String, String> leaveMap = dictService.map(Dict.LEAVE);
					String typeName = leaveMap.get(String.valueOf(leave.getTypeId()));
					
					String start_yyyyMMdd = startDate.toString("yyyy-MM-dd");
					String end_yyyyMMdd = endDate.toString("yyyy-MM-dd");
					List<Shift> shifts = dao.query(Shift.class, Cnd.where("shift_date", ">=", start_yyyyMMdd).and("shift_date", "<=", end_yyyyMMdd).and("user_id", "=", leave.getUserId()));
					if (shifts.size() == 0) {
						throw new Errors("排班在请假后已被修改,无法审批");
					} else {
						for (Shift s : shifts) {
							if (s.getClasses() == null) {
								continue;
							}
							ShiftClass shiftClass = dao.fetch(ShiftClass.class, s.getClasses());
							if (shiftClass == null) {
								continue;
							}
							String restIn = null;
							String restOut = null;
							String checkIn = shiftClass.getFirstMorning();
							String checkOut = null;
							// boolean once = false;
							// 二头班
							if (shiftClass.getSecond() == 1) {
								restIn = shiftClass.getFirstNight();
								restOut = shiftClass.getSecondMorning();
								checkOut = shiftClass.getSecondNight();
							} else {
								checkOut = shiftClass.getFirstNight();
								// 获取两个时间的中间值,2015-2-6 为任意时间
								DateTime fm = Calendars.parse("2015-2-6 " + checkIn, Calendars.DATE_TIME);
								DateTime fn = Calendars.parse("2015-2-6 " + checkOut, Calendars.DATE_TIME);
								long beginDate = (fn.toDate().getTime() + fm.toDate().getTime()) / 2;
								SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
								String middle = sdf.format(new Date(beginDate));
								restIn = restOut = middle;
								// once = true;
							}

							DateTime restInTime = Calendars.parse(start_yyyyMMdd + " " + restIn, Calendars.DATE_TIME);
							DateTime restOutTime = Calendars.parse(end_yyyyMMdd + " " + restOut, Calendars.DATE_TIME);
							int days = Days.daysBetween(Calendars.parse(start_yyyyMMdd, Calendars.DATE), Calendars.parse(end_yyyyMMdd, Calendars.DATE)).getDays();
							if (days == 0) {
								if (shiftClass.getSecond() != 1) {
									dayMap.put(start_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
								} else {
									if (startDate.isBefore(restInTime) && endDate.isBefore(restOutTime)) {
										dayMap.put(start_yyyyMMdd, new String[] { "1", null, checkIn, checkOut, restIn, restOut });
									} else if (startDate.isBefore(restInTime) && endDate.isAfter(restOutTime)) {
										dayMap.put(start_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
									} else if (startDate.isAfter(restInTime) && endDate.isAfter(restOutTime)) {
										dayMap.put(start_yyyyMMdd, new String[] { null, "1", checkIn, checkOut, restIn, restOut });
									} else {
										throw new Errors("审批无法完成,必须重新申请请假!");
									}
								}
							} else if (days > 0) {
								if (start_yyyyMMdd.equals(Calendars.str(s.getShiftDate(), Calendars.DATE))) {
									if (shiftClass.getSecond() != 1) {
										dayMap.put(start_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
									} else {
										if (startDate.isBefore(restInTime)) {
											dayMap.put(start_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
										} else if (startDate.isAfter(restInTime)) {
											dayMap.put(start_yyyyMMdd, new String[] { null, "1", checkIn, checkOut, restIn, restOut });
										} else {
											throw new Errors("审批无法完成,必须重新申请请假!");
										}
									}
								} else if (end_yyyyMMdd.equals(Calendars.str(s.getShiftDate(), Calendars.DATE))) {
									if (shiftClass.getSecond() != 1) {
										dayMap.put(end_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
									} else {
										if (endDate.isBefore(restOutTime)) {
											dayMap.put(end_yyyyMMdd, new String[] { "1", null, checkIn, checkOut, restIn, restOut });
										} else if (endDate.isAfter(restOutTime)) {
											dayMap.put(end_yyyyMMdd, new String[] { "1", "1", checkIn, checkOut, restIn, restOut });
										} else {
											throw new Errors("审批无法完成,必须重新申请请假!");
										}
									}
								} else {
									dayMap.put(Calendars.str(s.getShiftDate(), Calendars.DATE), new String[] { "1", "1" });
								}
							}
						}
					}

					transApprove2(leave.getLeaveId(), leave, null, null, typeName, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, Context.getUserId(), opinion,
							dayMap);
					
				}

			}
			
			Code.ok(mb, "请假申请审批成功");
			
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:able) error: ", e);
			Code.error(mb, "请假申请审批失败");
		}

		return mb;
	}
}
