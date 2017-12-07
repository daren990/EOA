package cn.oa.web.action.adm.salary.recording;

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
import org.nutz.dao.TableName;
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

import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.consts.WxOpen;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.CheckedRecord;
import cn.oa.model.LeaveActor;
import cn.oa.model.Outwork;
import cn.oa.model.Recording;
import cn.oa.model.Role;
import cn.oa.model.ShiftCorp;
import cn.oa.model.User;
import cn.oa.model.WorkAttendance;
import cn.oa.model.WorkDay;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
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

/**
 * 补录审批
 * @author SimonTang
 */
@IocBean(name = "adm.salary.recording.approve")
@At(value = "/adm/salary/recording/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);

	public void pageUtil(HttpServletRequest req) {
//		String token = CSRF.generate(req);
//		CSRF.generate(req, "/adm/salary/outwork/approve/able", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("r.status", "=", Status.ENABLED)
				.and("r.operator_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "r.approve", "approve", approve);
		Cnds.gte(cri, mb, "r.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "r.create_time", "endTime", endStr);
		cri.getOrderBy().desc("r.modify_time");

		Page<Recording> page = Webs.page(req);
		page = mapper.page(Recording.class, page, "Recording.count", "Recording.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/recording/approve_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/recording/approve_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	
	public void addUtil(HttpServletRequest req) {
//		CSRF.generate(req);
		Integer recordingId = Https.getInt(req, "recordingId", R.REQUIRED, R.I, "申请ID");

		Recording recording = mapper.fetch(Recording.class, "Recording.query", Cnd
				.where("r.status", "=", Status.ENABLED)
				.and("r.operator_id", "=", Context.getUserId())
				.and("r.recording_id", "=", recordingId));
		Asserts.isNull(recording, "申请不存在");
		if (recording == null)
			recording = new Recording();
		req.setAttribute("recording", recording);
	}
	
	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {

			Integer recordingId = Https.getInt(req, "recordingId", R.REQUIRED, R.I, "申请ID");
			List<Recording> recordings = mapper.query(Recording.class, "Recording.query", Cnd.where("r.recording_id", "=", recordingId).asc("r.modify_time"));
			mb.put("actors", recordings);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:actors) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/recording/approve_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/recording/approve_add_wx_2")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}


	
	public Object newAddPostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
//			CSRF.validate(req);
			Integer recordingId = Https.getInt(req, "recordingId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");

			Recording recording = mapper.fetch(Recording.class, "Recording.query", Cnd
					.where("r.status", "=", Status.ENABLED)
					.and("r.operator_id", "=", Context.getUserId())
					.and("r.recording_id", "=", recordingId));
			Asserts.isNull(recording, "申请不存在");

			if(recording.getApprove().equals(Status.OK)||//
					recording.getApprove().equals(Status.APPROVED)||//
					recording.getApprove().equals(Status.UNAPPROVED)){
				throw new Errors("审批已通过，不能再修改");
			}
			
			recording.setApprove(approve);
			newSave(recording);
			
			Code.ok(mb, "补录申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "补录申请审批失败");
		}

		return mb;
	}
	
	public Object newAblePostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
//			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");

			if (arr != null && arr.length > 0) {
				List<WorkAttendance> atts = dao.query(WorkAttendance.class, null);
				Map<Integer, WorkAttendance> attMap = new ConcurrentHashMap<Integer, WorkAttendance>();
				for (WorkAttendance att : atts)
					attMap.put(att.getOrgId(), att);

				List<Recording> recordings = mapper.query(Recording.class, "Recording.query", Cnd
						.where("r.status", "=", Status.ENABLED)
						.and("r.operator_id", "=", Context.getUserId())
						.and("r.recording_id", "in", arr));
				for (Recording recording : recordings) {
//					WorkAttendance att = attMap.get(outwork.getCorpId());
//					Asserts.isNull(att, "最近考勤周期未配置");
//					DateTime pos = new DateTime(att.getEndDate());
//					
//					DateTime start = new DateTime(outwork.getStartTime());
//					if (start.isBefore(pos)) continue;
					
					if(recording.getApprove().equals(Status.OK)||//
							recording.getApprove().equals(Status.APPROVED)||//
							recording.getApprove().equals(Status.UNAPPROVED)){
						continue;
					}
					
					recording.setApprove(approve);
					
					newSave(recording);

				}
			}
			Code.ok(mb, "外勤申请审批成功");
			
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:able) error: ", e);
			Code.error(mb, "外勤申请审批失败");
		}

		return mb;
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
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		return newAblePostUtil(req,res);
	
	}


//	private void newSave(Outwork outwork, Integer approve, Integer modifyId, String opinion,final Map<String, String[]> dayMap) {
//		if (outwork.getApprove().equals(approve)&&outwork.getOpinion().equals(opinion)) return;
//		outwork.setApprove(approve);
//		outwork.setModifyTime(new Date());
//		outwork.setOpinion(opinion);
//		dao.update(outwork);
//		checkedRecordService.update3(outwork.getUserId(), modifyId, dayMap);
//	}
	
	private void newSave(final Recording recording) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if(recording.getApprove().equals(Status.APPROVED)){
					String month = Calendars.parse(recording.getRecordingTime(), Calendars.DATE).toString("yyyyMM");
					Map<String, String> vars = new ConcurrentHashMap<String, String>();
					vars.put("month", month);

					CheckedRecord attendance = mapper.fetch(CheckedRecord.class, "CheckedRecord.query", Cnd.where("c.user_id", "=", recording.getUserId()).and("c.work_date", "=", recording.getRecordingTime()), null, vars);
					Asserts.isNull(attendance, "考勤记录不存在");
					
					if (attendance.getVersion().equals(Status.ENABLED))
						throw new Errors("禁止修改已定版的考勤记录");

					if (recording.getRecordingAM().equals(0)) {
						attendance.setRemarkIn("已补录");
					}else{
						attendance.setRemarkOut("已补录");
					}
					update(month, attendance);
				}
				dao.update(recording);
			}
		});
		
	}
	
	private void update(String month, final CheckedRecord attendance) {
		TableName.run(month, new Runnable() {
			@Override
			public void run() {
				dao.update(attendance);
			}
		});
	}
}
