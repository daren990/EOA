package cn.oa.web.action.adm.salary.outwork;

import java.util.Date;
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

import cn.oa.consts.Status;
import cn.oa.model.Outwork;
import cn.oa.model.ShiftCorp;
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

/**
 * 外勤审批
 * @author SimonTang
 */
@IocBean(name = "adm.salary.outwork.approve")
@At(value = "/adm/salary/outwork/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/salary/outwork/approve/able", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("w.status", "=", Status.ENABLED)
				.and("w.operator_id", "=", Context.getUserId());
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
	@Ok("ftl:adm/salary/outwork/approve_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/outwork/approve_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	
	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer outworkId = Https.getInt(req, "outworkId", R.REQUIRED, R.I, "申请ID");

		Outwork outwork = mapper.fetch(Outwork.class, "Outwork.query", Cnd
				.where("w.status", "=", Status.ENABLED)
				.and("w.operator_id", "=", Context.getUserId())
				.and("w.outwork_id", "=", outworkId));
		Asserts.isNull(outwork, "申请不存在");
		if (outwork == null)
			outwork = new Outwork();
		req.setAttribute("outwork", outwork);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/outwork/approve_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/outwork/approve_add_wx_2")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}

	public Object addPostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer outworkId = Https.getInt(req, "outworkId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");

			Outwork outwork = mapper.fetch(Outwork.class, "Outwork.query", Cnd
					.where("w.status", "=", Status.ENABLED)
					.and("w.operator_id", "=", Context.getUserId())
					.and("w.outwork_id", "=", outworkId));
			Asserts.isNull(outwork, "申请不存在");
			
			WorkAttendance att = dao.fetch(WorkAttendance.class, outwork.getCorpId());
			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());
			
			DateTime start = new DateTime(outwork.getStartTime());

			if (start.isBefore(pos))
				throw new Errors("考勤结束日期禁止审批");

			DateTime now = new DateTime();
			save(outwork, approve, Context.getUserId(), now, opinion);

			Code.ok(mb, "外勤申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "外勤申请审批失败");
		}

		return mb;
	}
	
	public Object newAddPostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer outworkId = Https.getInt(req, "outworkId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");

			Outwork outwork = mapper.fetch(Outwork.class, "Outwork.query", Cnd
					.where("w.status", "=", Status.ENABLED)
					.and("w.operator_id", "=", Context.getUserId())
					.and("w.outwork_id", "=", outworkId));
			Asserts.isNull(outwork, "申请不存在");
			
			WorkAttendance att = dao.fetch(WorkAttendance.class, outwork.getCorpId());
			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());
			if(outwork.getApprove().equals(Status.OK)||//
					outwork.getApprove().equals(Status.APPROVED)||//
					outwork.getApprove().equals(Status.UNAPPROVED)){
				throw new Errors("审批已通过，不能再修改");
			}
			DateTime start = new DateTime(outwork.getStartTime());

			if (start.isBefore(pos))
				throw new Errors("考勤结束日期禁止审批");

			Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
			String typeName = null;
			if(approve == Status.APPROVED){
				typeName = "外勤 (已批准) ";
			}else{
				typeName = "外勤（未批准）";
			}
			outworkService.shift(Calendars.str(outwork.getStartTime(),Calendars.DATE), dayMap, typeName,outwork.getUserId(),outwork.getType());
			newSave(outwork, approve, Context.getUserId(), "-", dayMap);
			Code.ok(mb, "外勤申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "外勤申请审批失败");
		}

		return mb;
	}
	
	public Object newAblePostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");

			if (arr != null && arr.length > 0) {
				List<WorkAttendance> atts = dao.query(WorkAttendance.class, null);
				Map<Integer, WorkAttendance> attMap = new ConcurrentHashMap<Integer, WorkAttendance>();
				for (WorkAttendance att : atts)
					attMap.put(att.getOrgId(), att);

				List<Outwork> outworks = mapper.query(Outwork.class, "Outwork.query", Cnd
						.where("w.status", "=", Status.ENABLED)
						.and("w.operator_id", "=", Context.getUserId())
						.and("w.outwork_id", "in", arr));
				for (Outwork outwork : outworks) {
					WorkAttendance att = attMap.get(outwork.getCorpId());
					Asserts.isNull(att, "最近考勤周期未配置");
					DateTime pos = new DateTime(att.getEndDate());
					
					DateTime start = new DateTime(outwork.getStartTime());
					if (start.isBefore(pos)) continue;
					Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
					String typeName = null;
					if(approve == Status.APPROVED){
						typeName = "外勤 (已批准) ";
					}else{
						typeName = "外勤（未批准）";
					}
					outworkService.shift(Calendars.str(outwork.getStartTime(),Calendars.DATE), dayMap,  typeName,outwork.getUserId(),outwork.getType());
					newSave(outwork, approve, Context.getUserId(), "-", dayMap);

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
		//获取公司使用的排班类型
		ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class,"ShiftCorp.query",Cnd.where("o.status","=",Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", Context.getCorpId()));
		//新排班
		if(shiftCorp!=null){
			return newAddPostUtil(req,res);
		}else{
			return addPostUtil(req,res);
		}
	}

	public Object ablePostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");

			if (arr != null && arr.length > 0) {
				DateTime now = new DateTime();
				
				List<WorkAttendance> atts = dao.query(WorkAttendance.class, null);
				Map<Integer, WorkAttendance> attMap = new ConcurrentHashMap<Integer, WorkAttendance>();
				for (WorkAttendance att : atts)
					attMap.put(att.getOrgId(), att);

				List<Outwork> outworks = mapper.query(Outwork.class, "Outwork.query", Cnd
						.where("w.status", "=", Status.ENABLED)
						.and("w.operator_id", "=", Context.getUserId())
						.and("w.outwork_id", "in", arr));
				for (Outwork outwork : outworks) {
					WorkAttendance att = attMap.get(outwork.getCorpId());
					Asserts.isNull(att, "最近考勤周期未配置");
					DateTime pos = new DateTime(att.getEndDate());
					
					DateTime start = new DateTime(outwork.getStartTime());
					if (start.isBefore(pos)) continue;
					save(outwork, approve, Context.getUserId(), now, "-");
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
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		//获取公司使用的排班类型
		ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class,"ShiftCorp.query",Cnd.where("o.status","=",Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", Context.getCorpId()));

		//新排班
		if(shiftCorp!=null){
			return newAblePostUtil(req,res);
		}else{
			return ablePostUtil(req,res);
		}
	}

	private void save(Outwork outwork, Integer approve, Integer modifyId, DateTime now, String opinion) {
		if (outwork.getApprove().equals(approve)&&outwork.getOpinion().equals(opinion)) return;

		outwork.setApprove(approve);
		outwork.setModifyTime(now.toDate());

		Map<Integer, WorkDay> dayMap = workRepository.dayMap();
		Map<Integer, String[]> weekMap = workRepository.weekMap();
		Map<String, Integer[]> monthMap = workRepository.monthMap(outwork.getCorpId());

		WorkDay day = dayMap.get(outwork.getDayId());
		String[] weeks = weekMap.get(outwork.getWeekId());

		outwork.setApprove(approve);
		outwork.setModifyTime(now.toDate());
		outwork.setOpinion(opinion);
		
		outworkService.save(outwork.getOutworkId(), outwork, null, null, day, weeks, monthMap, modifyId, now);
	}
	private void newSave(Outwork outwork, Integer approve, Integer modifyId, String opinion,final Map<String, String[]> dayMap) {
		if (outwork.getApprove().equals(approve)&&outwork.getOpinion().equals(opinion)) return;
		outwork.setApprove(approve);
		outwork.setModifyTime(new Date());
		outwork.setOpinion(opinion);
		dao.update(outwork);
		checkedRecordService.update3(outwork.getUserId(), modifyId, dayMap);
	}
}
