package cn.oa.web.action.hrm.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

import cn.oa.consts.Status;
import cn.oa.model.AttendanceResult;
import cn.oa.model.AttendanceThreshold;
import cn.oa.model.AttendanceThresholdItem;
import cn.oa.model.ConfLeave;
import cn.oa.model.ConfLeaveType;
import cn.oa.model.Measure;
import cn.oa.model.Org;
import cn.oa.model.SalaryRule;
import cn.oa.model.SalaryRuleItem;
import cn.oa.model.ShiftCorp;
import cn.oa.model.User;
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

/**
 * 考勤汇总
 * @author SimonTang
 */
@IocBean(name = "hrm.result.attendance")
@At(value = "/hrm/result/attendance")
public class AttendanceAction extends Action {
	
	public static Log log = Logs.getLog(AttendanceAction.class);

	@GET
	@At
	@Ok("ftl:hrm/result/attendance_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/user/nodes", token);
		CSRF.generate(req, "/adm/examine/release/nodes", token);
		CSRF.generate(req, "/hrm/result/attendance/version", token);
		
		String year = Https.getStr(req, "year", R.yyyy);
		String month = Https.getStr(req, "month", R.MM);
		
		Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I);
		Integer releaseId = Https.getInt(req, "releaseId", R.I);
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);
		
		DateTime now = new DateTime();
		MapBean mb = new MapBean();
		
		String resultMonth = null;
		if (Strings.isNotBlank(year) && Strings.isNotBlank(month)) resultMonth = year + month;
		
		Criteria cri = Cnd.cri();
		Cnds.eq(cri, mb, "u.corp_id", "corpId", corpId);
		Cnds.eq(cri, mb, "a.user_id", "userId", userId);
		Cnds.like(cri, mb,"a.result_month","resultMonth", resultMonth);
		cri.getOrderBy().desc("a.result_month");
		
		Page<AttendanceResult> page = Webs.page(req);
		page = mapper.page(AttendanceResult.class, page, "AttendanceResult.count", "AttendanceResult.index", cri);

		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));

		Webs.put(mb, "releaseId", releaseId);
		Webs.put(mb, "year", year);
		Webs.put(mb, "month", month);
		mb.put("years", now.getYear());
		mb.put("months", now.plusMonths(-1).toString("MM"));
		
		req.setAttribute("page", page);
		req.setAttribute("corps", corps);
		req.setAttribute("mb", mb);
	}
	
	/**
	 * 汇总定版
	 * @param req
	 * @param res
	 * @return
	 */
	@POST
	@At
	@Ok("json")
	public Object version(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String year = Https.getStr(req, "year", R.REQUIRED, R.yyyy, "年份");
			String month = Https.getStr(req, "month", R.REQUIRED, R.MM, "月份");
			Integer corpId = Https.getInt(req, "corpId", R.REQUIRED, R.I, "所属公司");
			Integer releaseId = Https.getInt(req, "releaseId", R.I, "绩效考核");
			Integer userId = Https.getInt(req, "userId", R.I, "用户姓名");
			
			//如果userId为空，那么就查询公司下的所有人
			List<User> users = null;
			if (userId != null) {
				User user = mapper.fetch(User.class, "User.query", Cnd
						.where("u.user_id", "=", userId)
						.and("u.status", "=", Status.ENABLED));
				Asserts.isNull(user, "用户不存在");
				users = new ArrayList<User>();
				users.add(user);
			} else {
				users =	mapper.query(User.class, "User.query", Cnd
						.where("u.corp_id", "=", corpId)
						.and("u.status", "=", Status.ENABLED));
			}

			Asserts.isEmpty(users, "用户集合不能为空值");
			
			//将User对象中的id提取出来
			Integer[] userIds = Converts.array(User.class, Integer.class, users, "userId");
			
			Criteria cri = Cnd.cri();
			//将最后两个参数分别作为mb的key和val存入，将中间的两个参数作为条件设置到cri中
			Cnds.like(cri, mb,  "a.result_month","resultMonth", year + month);
			cri.where().and("a.user_id", "in", userIds);
			//查询考勤结果表，返回空值则抛出异常，因此出工资条的前提操作是进行考勤总汇
			List<AttendanceResult> atts = mapper.query(AttendanceResult.class, "AttendanceResult.query", cri);
			Asserts.isEmpty(atts, "考勤汇总不能为空值");
			
			Map<Integer, User> userMap = new ConcurrentHashMap<Integer, User>();
			for (User user : users){
				userMap.put(user.getUserId(), user);
			}
			
			DateTime now = new DateTime();

			Map<Float, Float> measureMap = new LinkedHashMap<Float, Float>();
			if(null != releaseId){
				List<Measure> measures = dao.query(Measure.class, Cnd.where("orgId", "=", corpId).desc("score"));
				Asserts.isEmpty(measures, "未配置相应的绩效评分标准！");
				for (Measure measure : measures) {
					measureMap.put(measure.getScore(), measure.getCoefficient());
				}
			}
			
			Org corp = dao.fetch(Org.class, Cnd.where("orgId", "=", corpId));
			Asserts.isNull(corp, "所选公司不存在");
			
			Integer confleaveId = corp.getConfLeaveId();
			Asserts.isNull(confleaveId, "公司请假类型薪酬配置不能为空");
			ConfLeave leave = dao.fetch(ConfLeave.class, Cnd.where("confLeaveId", "=", confleaveId).and("Status", "=", Status.ENABLED));
			Asserts.isNull(leave, "公司请假类型薪酬配置不能为空");
			
			List<ConfLeaveType> leaveType =  dao.query(ConfLeaveType.class, Cnd.where("confLeaveId", "=", confleaveId));
			
			List<AttendanceThreshold> thresholds = dao.query(AttendanceThreshold.class, Cnd.where("status", "=", Status.ENABLED));
			Asserts.isEmpty(thresholds, "考勤阈值集合不能为空");
			
			Map<Integer, AttendanceThreshold> thresholdMap = new ConcurrentHashMap<Integer, AttendanceThreshold>();
			//从代码可知，一家公司在conf_attendance_threshold中只能有一项配置
			for(AttendanceThreshold threshold : thresholds){
				Integer arr[] = Converts.array(threshold.getOrgIds(), ",");
				for(Integer a : arr){
					thresholdMap.put(a, threshold);
				}
			}
			
			AttendanceThreshold threshold = thresholdMap.get(corpId);
			Asserts.isNull(threshold, "公司考勤阈值不能为空");
			List<AttendanceThresholdItem> thresholdItem= dao.query(AttendanceThresholdItem.class, Cnd.where("thresholdId", "=", threshold.getThresholdId()).and("status", "=", Status.ENABLED));
			Asserts.isNull(thresholdItem, "公司考勤阈值不能为空");
			
			List<SalaryRule> salaryRules = dao.query(SalaryRule.class, Cnd.where("status", "=", Status.ENABLED));
			Asserts.isEmpty(salaryRules, "工资规则配置集合不能为空");
			Map<Integer, SalaryRule> salaryRuleMap = new ConcurrentHashMap<Integer, SalaryRule>();
			for(SalaryRule salaryRule : salaryRules){
				Integer arr[] = Converts.array(salaryRule.getOrgIds(), ",");
				for(Integer a : arr){
					salaryRuleMap.put(a, salaryRule);
				}
			}
			SalaryRule salaryRule = salaryRuleMap.get(corpId);
			Asserts.isNull(salaryRule, "工资规则配置集合不能为空");

			List<SalaryRuleItem> salaryRuleItems= dao.query(SalaryRuleItem.class, Cnd.where("salaryRuleId", "=", salaryRule.getId()).and("status", "=", Status.ENABLED));
			Asserts.isNull(salaryRuleItems, "工资规则配置集合不能为空");
			
			
			//获取公司使用的排班类型
			ShiftCorp shiftCorp = mapper.fetch(ShiftCorp.class,"ShiftCorp.query",Cnd.where("o.status","=",Status.ENABLED).and("o.parent_id", "=", 0).and("c.corp_id", "=", corpId));
			//新排班
			if(shiftCorp!=null){
				for (AttendanceResult att : atts) {
					User user = userMap.get(att.getUserId());
					newTransSave(user, releaseId, measureMap, thresholdItem, salaryRuleItems, leaveType, att, Context.getUserId(), now);
				}
			}else{//旧排班
				for (AttendanceResult att : atts) {
					User user = userMap.get(att.getUserId());
					transSave(user, releaseId, measureMap, thresholdItem,leaveType, att, Context.getUserId(), now);
				}
			}
			
			Code.ok(mb, "考勤汇总定版成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Attendance:version) error: ", e);
			Code.error(mb, "考勤汇总定版失败");
		}
		return mb;
	}
	
	private void transSave(final User user, final Integer releaseId, final Map<Float, Float> measureMap, final List<AttendanceThresholdItem> thresholdItem,final List<ConfLeaveType> leaveType, final AttendanceResult att,
			final Integer modifyId, final DateTime now) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				String startStr = new DateTime(att.getStartDate()).toString(Calendars.DATE_TIME);
				String endStr = new DateTime(att.getEndDate()).toString(Calendars.DATE_TIME);
				att.setVersion(Status.ENABLED);
				dao.update(att);
				resultService.wage(user, releaseId, measureMap, thresholdItem,leaveType, att.getWorkDay(), startStr, endStr, modifyId, now,att);
				checkedRecordService.version(user.getUserId(), startStr, endStr, modifyId, now);
			}
		});
	}
	
	private void newTransSave(final User user, final Integer releaseId, final Map<Float, Float> measureMap, final List<AttendanceThresholdItem> thresholdItem,
			final List<SalaryRuleItem> salaryRuleItems, final List<ConfLeaveType> leaveType, final AttendanceResult att, final Integer modifyId, final DateTime now) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				String startStr = new DateTime(att.getStartDate()).toString(Calendars.DATE_TIME);
				String endStr = new DateTime(att.getEndDate()).toString(Calendars.DATE_TIME);
				att.setVersion(Status.ENABLED);
				dao.update(att);
				resultService.newWage(user, releaseId, measureMap, thresholdItem, salaryRuleItems, leaveType, startStr, endStr, modifyId, now, att);
				checkedRecordService.version(att.getUserId(), startStr, endStr, modifyId, now);
				
			}
		});
	}
}
