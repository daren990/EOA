package cn.oa.web.action.adm.salary.recording;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
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
import cn.oa.consts.Roles;
import cn.oa.consts.ShiftC;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.consts.WxOpen;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Dict;
import cn.oa.model.Leave;
import cn.oa.model.LeaveActor;
import cn.oa.model.LeaveType;
import cn.oa.model.Org;
import cn.oa.model.Recording;
import cn.oa.model.Role;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.model.ShiftCorp;
import cn.oa.model.User;
import cn.oa.model.WechatStaffmatching;
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
 * 补录申请
 * @author SimonTang
 */
@IocBean(name = "adm.salary.recording.apply")
@At(value = "/adm/salary/recording/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	public void pageUtil(HttpServletRequest req) {

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		System.out.println(startStr);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		System.out.println(approve);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("r.status", "=", Status.ENABLED)
				.and("r.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "r.approve", "approve", approve);
		Cnds.eq(cri, mb, "r.recording_time", "recording_time", startStr);
		cri.getOrderBy().desc("r.modify_time");

		Page<Recording> page = Webs.page(req);
		page = mapper.page(Recording.class, page, "Recording.count", "Recording.index", cri);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/recording/apply_page_wx_2")
	public void wxpage(HttpServletRequest req){
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/recording/apply_page")
	public void page(HttpServletRequest req){
		pageUtil(req);
	}

	
	public void addUtil(HttpServletRequest req) {

		Integer recordingId = Https.getInt(req, "recordingId", R.I);
//		String mTime = null;
//		String nTime = null;
		Recording recording = new Recording();
		if (recordingId != null)
			recording = dao.fetch(Recording.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("userId", "=", Context.getUserId())
					.and("recordingId", "=", recordingId));
//		if (recording!= null) {
//			mTime = Calendars.str(leave.getStartTime(),"HH:mm");
//			nTime = Calendars.str(leave.getEndTime(),"HH:mm");
//			// 审批
//			LeaveActor actor = dao.fetch(LeaveActor.class, Cnd
//					.where("leaveId", "=", leaveId)
//					.and("step", "=", 1)
//					.limit(1)
//					.asc("modifyTime"));
//			if (actor != null) {
//				//审批人id
//				leave.setActorId(actor.getActorId());
//			}
//		} else {
//			leave = new Leave();
//		}
		
		List<User> operators = null;
		
		User manager = dao.fetch(User.class, Context.getUser().getManagerId());
		if(manager != null){
			operators = new ArrayList<User>();
			operators.add(manager);
		} else{
			//返回等级大于当前用户的所有上级
			operators = userService.operators(Context.getCorpId(), Context.getLevel());
		}

//		req.setAttribute("mTime", mTime);
//		req.setAttribute("nTime", nTime);
		req.setAttribute("operators", operators);
		req.setAttribute("recording", recording);
//		req.setAttribute("leaveMap", dictService.map(Dict.LEAVE));
	}
	@GET
	@At
	@Ok("ftl:adm/salary/recording/apply_add")
	public void add(HttpServletRequest req){		
		addUtil(req);
	}
	@GET
	@At
	@Ok("ftl:adm/salary/recording/apply_add_wx_2")
	public void wxadd(HttpServletRequest req){
		addUtil(req);
	}
	private Object newAddPostUtil(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer recordingId = null;
		Recording recording = null;
		DateTime now = new DateTime();
		try {
			recordingId = Https.getInt(req, "recordingId", R.I);
			Integer type = Https.getInt(req, "type", R.REQUIRED, R.I, "补录时间段");
			String work_yyyyMMdd = Https.getStr(req, "work_yyyyMMdd",  R.D, "补录日期");
			String reason = Https.getStr(req, "reason", "补录原因");
			Integer operatorId = Https.getInt(req, "operatorId", R.REQUIRED, R.I, "审批人ID");

			if (recordingId != null) {
				recording = dao.fetch(Recording.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("recordingId", "=", recordingId));
				Asserts.isNull(recording, "申请不存在");
				Asserts.notEqOr(recording.getApprove(), Status.APPROVED, Status.OK, "禁止修改已审批的请假申请");

			} else {
				recording = new Recording();
				recording.setStatus(Status.ENABLED);
				recording.setCreateTime(now.toDate());
				recording.setApprove(Status.PROOFING);
			}
			
			recording.setUserId(Context.getUserId());
			recording.setModifyTime(now.toDate());
			recording.setSubject(Context.getTrueName()+"于"+Calendars.str(now,Calendars.DATE)+"申请补录");
			recording.setRecordingTime(Calendars.parse(work_yyyyMMdd, Calendars.DATE).toDate());
			recording.setReason(reason);
			recording.setRecordingAM(type);
			recording.setOperatorId(operatorId);
			

			//得到上级的所拥有的所有角色
//			String [] str=userService.findRoleNames(operatorId);
//			
//			String variable=null;
//		
//			if(Asserts.hasAny(Roles.BOSS.getName(),str)){
//				variable=Roles.BOSS.getName();
//			}
//			else if(Asserts.hasAny(Roles.GM.getName(),str)){
//				variable=Roles.GM.getName();
//			}
//			else if(Asserts.hasAny(Roles.MAN.getName(),str)){
//				variable=Roles.MAN.getName();
//			}
//			else if(Asserts.hasAny(Roles.SVI.getName(),str)){
//				variable=Roles.SVI.getName();
//			}
//			else if(Asserts.hasAny(Roles.EMP.getName(),str)){
//				variable=Roles.EMP.getName();
//			}
//			else {
//				variable=Roles.EMP.getName();
//			}
			
//			String atitle=titleName+"申请请假";
			
			String url = null;
			url = "http://"+DomainName.OA+"/"+WxOpen.LEAVE_APPLY.getURL();

			tranSave(recording);
//			transSave(leaveId, leave, actorId,variable,start, end, Context.getUserId(), typeName, dayMap,dayMapOld,atitle,url,actorName);	

			
			Code.ok(mb, (recordingId == null ? "新建" : "编辑") + "请假申请成功");
					
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (recordingId == null ? "新建" : "编辑") + "请假申请失败");
		}

		return mb;
	}
	
	
	private void tranSave(Recording recording){
		if(recording.getRecordingId() != null){
			dao.update(recording);
		}else{
			dao.insert(recording);
		}
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		return newAddPostUtil(req,res);
	}

	
	@POST
	@At
	@Ok("json")
	public Object del(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {

			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			if (arr != null && arr.length > 0) {
				List<Recording> recordings = dao.query(Recording.class, Cnd.where("recordingId", "in", arr).and("approve", "=", Status.PROOFING).and("user_id", "=", Context.getUserId()).and("status", "=", Status.ENABLED));
/*				List<Leave> leaves = mapper.query(Leave.class, "Leave.query", Cnd
						.where("l.status", "=", Status.ENABLED)
						.and("l.user_id", "=", Context.getUserId())
						.and("l.approve", "=", Status.PROOFING)
						.and("l.leave_id", "in", arr));*/
				if(recordings.size() == 0){
					throw new Errors("必须选定待审批的申请");
				}
				for (Recording recording : recordings) {
					recording.setStatus(0);
					dao.update(recording);
				}
			}
			Code.ok(mb, "删除请假申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除请假申请失败");
		}
		return mb;
	}
	

	private void transSave(final Integer leaveId, final Leave leave, final Integer actorId, final String variable,
			final DateTime orgStart, final DateTime orgEnd,
			final Integer modifyId, final String typeName,final Map<String, String[]> dayMap,final Map<String, String[]> dayMapOld,final String atitle,final String url,final String actorName) {
		//获取用户注册在企业号中的名字，用于发送微信
		final WechatStaffmatching matching = dao.fetch(WechatStaffmatching.class, Cnd.where("userId", "=", actorId));
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = leaveId;
				if (leaveId != null) {
					dao.clear(LeaveActor.class, Cnd.where("leaveId", "=", leaveId));
					checkedRecordService.updateClear(leave.getUserId(), new DateTime(leave.getStartTime()).toString(Calendars.DATE_TIME), new DateTime(leave.getEndTime()).toString(Calendars.DATE_TIME), null, dayMapOld, modifyId);						
					dao.update(leave);
				} else {
					id = dao.insert(leave).getLeaveId();
				}

				// 指定审批
				dao.insert(new LeaveActor(id, actorId, Value.F, leave.getApprove(), variable, leave.getModifyTime(), Value.T, Status.value));
				checkedRecordService.update3(leave.getUserId(), modifyId, dayMap);		
				
				String description="请假原因:"+leave.getReason();
				if(WxOpen.LEAVE_APPLY.isOPEN()&&leaveId == null&&wxUserService.leave() && matching != null && matching.getWechatName() != null && !matching.getWechatName().equals("")){
					List<Role> roles = roleRepository.find(actorId);
					String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
					String agentid=WxRoleUtil.wxrole(roleNames);
					WxSendService wx=new WxSendService();
					
					wx.sendarticle(matching.getWechatName(),"","",agentid,atitle,description,url,"");	
				}
				User user = dao.fetch(User.class, Cnd.where("user_id", "=", actorId));
				List<String> maiList = new ArrayList<String>();
				if(StringUtils.isNotBlank(user.getEmail())){
					maiList.add(user.getEmail());
				}
				MailStart mail = new MailStart();
				mail.mail(maiList,atitle,description);
			}
		});
	}

	
}
