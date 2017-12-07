package cn.oa.web.action.adm.salary.errand;

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
import cn.oa.model.Errand;
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

@IocBean(name = "adm.salary.errand.approve")
@At(value = "/adm/salary/errand/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/salary/errand/approve/able", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("e.status", "=", Status.ENABLED)
				.and("e.operator_id", "=", Context.getUserId());
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
	@Ok("ftl:adm/salary/errand/approve_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	@GET
	@At
	@Ok("ftl:adm/salary/errand/approve_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer errandId = Https.getInt(req, "errandId", R.REQUIRED, R.I, "申请ID");
		Errand errand = mapper.fetch(Errand.class, "Errand.query", Cnd
				.where("e.status", "=", Status.ENABLED)
				.and("e.operator_id", "=", Context.getUserId())
				.and("e.Errand_id", "=", errandId));
		Asserts.isNull(errand, "申请不存在");
		req.setAttribute("errand", errand);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/errand/approve_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/errand/approve_add_wx")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}
	
/*	@GET
	@At
	@Ok("ftl:wx/errand")
	public void errand(HttpServletRequest req) {
		CSRF.generate(req);
		Integer errandId = Https.getInt(req, "errandId", R.REQUIRED, R.I, "申请ID");
		Errand errand = mapper.fetch(Errand.class, "Errand.query", Cnd
				.where("e.status", "=", Status.ENABLED)
				.and("e.operator_id", "=", Context.getUserId())
				.and("e.Errand_id", "=", errandId));
		Asserts.isNull(errand, "申请不存在");
		req.setAttribute("errand", errand);
	}*/
	public Object addPostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer errandId = Https.getInt(req, "errandId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			
			Errand errand = mapper.fetch(Errand.class, "Errand.query", Cnd
					.where("e.status", "=", Status.ENABLED)
					.and("e.operator_id", "=", Context.getUserId())
					.and("e.Errand_id", "=", errandId));
			Asserts.isNull(errand, "申请不存在");
			
			WorkAttendance att = dao.fetch(WorkAttendance.class, errand.getCorpId());
			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());

			DateTime start = new DateTime(errand.getStartTime());
			
			if (start.isBefore(pos))
				throw new Errors("考勤结束日期禁止审批");

			DateTime now = new DateTime();
			save(errand, approve, Context.getUserId(), now, opinion);

			Code.ok(mb, "出差申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "出差申请审批失败");
		}

		return mb;
	}
	public Object newAddPostUtil(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer errandId = Https.getInt(req, "errandId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			
			Errand errand = mapper.fetch(Errand.class, "Errand.query", Cnd
					.where("e.status", "=", Status.ENABLED)
					.and("e.operator_id", "=", Context.getUserId())
					.and("e.Errand_id", "=", errandId));
			Asserts.isNull(errand, "申请不存在");
			
			WorkAttendance att = dao.fetch(WorkAttendance.class, errand.getCorpId());
			Asserts.isNull(att, "最近考勤周期未配置");
			DateTime pos = new DateTime(att.getEndDate());

			DateTime start = new DateTime(errand.getStartTime());
			if(errand.getApprove().equals(Status.OK)||//
					errand.getApprove().equals(Status.APPROVED)||//
					errand.getApprove().equals(Status.UNAPPROVED)){
				throw new Errors("审批已通过，不能再修改");
			}
			if (start.isBefore(pos))
				throw new Errors("考勤结束日期禁止审批");
			Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
			String typeName = null;
			if(approve == Status.APPROVED){
				typeName = "出差 (已批准) ";
			}else{
				typeName = "出差（未批准）";
			}
			errandService.shift(Calendars.str(errand.getStartTime(),Calendars.DATE), Calendars.str(errand.getEndTime(),Calendars.DATE),  dayMap,  typeName,errand.getUserId());	
			
			newSave(errand, approve, Context.getUserId(), opinion,dayMap);

			Code.ok(mb, "出差申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "出差申请审批失败");
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
				
				List<Errand> errands = mapper.query(Errand.class, "Errand.query", Cnd
						.where("e.status", "=", Status.ENABLED)
						.and("e.operator_id", "=", Context.getUserId())
						.and("e.errand_id", "in", arr));
				for (Errand errand : errands) {
					WorkAttendance att = attMap.get(errand.getCorpId());
					Asserts.isNull(att, "最近考勤周期未配置");
					DateTime pos = new DateTime(att.getEndDate());
					
					DateTime start = new DateTime(errand.getStartTime());
					if (start.isBefore(pos)) continue;
					save(errand, approve, Context.getUserId(), now, Status.value);
				}
			}
			Code.ok(mb, "出差申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:able) error: ", e);
			Code.error(mb, "出差申请审批失败");
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
				
				List<Errand> errands = mapper.query(Errand.class, "Errand.query", Cnd
						.where("e.status", "=", Status.ENABLED)
						.and("e.operator_id", "=", Context.getUserId())
						.and("e.errand_id", "in", arr));
				for (Errand errand : errands) {
					WorkAttendance att = attMap.get(errand.getCorpId());
					Asserts.isNull(att, "最近考勤周期未配置");
					DateTime pos = new DateTime(att.getEndDate());
					
					DateTime start = new DateTime(errand.getStartTime());
					if (start.isBefore(pos)) continue;
					Map<String, String[]> dayMap = new ConcurrentHashMap<String, String[]>();
					String typeName = null;
					if(approve == Status.APPROVED){
						typeName = "出差 (已批准) ";
					}else{
						typeName = "出差（未批准）";
					}
					errandService.shift(Calendars.str(errand.getStartTime(),Calendars.DATE), Calendars.str(errand.getEndTime(),Calendars.DATE),  dayMap,  typeName,errand.getUserId());	
					
					newSave(errand, approve, Context.getUserId(), "-",dayMap);
				}
			}
			Code.ok(mb, "出差申请审批成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:able) error: ", e);
			Code.error(mb, "出差申请审批失败");
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

	private void save(Errand errand, Integer approve, Integer modifyId, DateTime now, String opinion) {
		if (errand.getApprove().equals(approve)&&errand.getOpinion().equals(opinion)) return;

		errand.setApprove(approve);
		errand.setModifyTime(now.toDate());

		Map<Integer, WorkDay> dayMap = workRepository.dayMap();
		Map<Integer, String[]> weekMap = workRepository.weekMap();
		Map<String, Integer[]> monthMap = workRepository.monthMap(errand.getCorpId());

		WorkDay day = dayMap.get(errand.getDayId());
		String[] weeks = weekMap.get(errand.getWeekId());
		
		errand.setApprove(approve);
		errand.setModifyTime(now.toDate());
		errand.setOpinion(opinion);

		errandService.save(errand.getErrandId(), errand, null, null, day, weeks, monthMap, modifyId, now);
	}
	
	private void newSave(Errand errand, Integer approve, Integer modifyId, String opinion,final Map<String, String[]> dayMap) {
		if (errand.getApprove().equals(approve)&&errand.getOpinion().equals(opinion)) return;
		errand.setApprove(approve);
		errand.setModifyTime(new Date());
		errand.setOpinion(opinion);
		dao.update(errand);
		checkedRecordService.update3(errand.getUserId(), modifyId, dayMap);
	}
}
