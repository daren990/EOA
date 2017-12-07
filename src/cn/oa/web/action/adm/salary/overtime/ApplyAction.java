package cn.oa.web.action.adm.salary.overtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.Inject;
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
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.consts.WxOpen;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Dict;
import cn.oa.model.Overtime;
import cn.oa.model.OvertimeActor;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.service.OvertimeService;
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

/**
 * 加班申请
 * @author SimonTang
 */
@IocBean(name = "adm.salary.overtime.apply")
@At(value = "/adm/salary/overtime/apply")
public class ApplyAction extends Action {
	
	@Inject
	private OvertimeService overtimeService;

	public static Log log = Logs.getLog(ApplyAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/salary/overtime/apply/del", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("ot.status", "=", Status.ENABLED)
				.and("ot.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "ot.approve", "approve", approve);
		Cnds.gte(cri, mb, "ot.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "ot.create_time", "endTime", endStr);
		cri.getOrderBy().desc("ot.modify_time");

		Page<Overtime> page = Webs.page(req);
		page = mapper.page(Overtime.class, page, "OvertimeApply.count", "OvertimeApply.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/overtime/apply_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/overtime/apply_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer overtimeId = Https.getInt(req, "overtimeId", R.I);
		Overtime overtime = null;
		
		if (overtimeId != null)
			overtime = dao.fetch(Overtime.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("userId", "=", Context.getUserId())
					.and("overtimeId", "=", overtimeId));
		if (overtime == null)
			overtime = new Overtime();
		
		User user = Context.getUser();
		User manager = dao.fetch(User.class, user.getManagerId());
		List<User> operators = new ArrayList<User>();
		if(null != manager){
			operators.add(manager);
		} else {
			operators = userService.operators(user.getCorpId(), user.getLevel());
		}
		req.setAttribute("operators", operators);
		req.setAttribute("overtime", overtime);
		req.setAttribute("overtimeMap", dictService.map(Dict.OVERTIME));
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/overtime/apply_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/salary/overtime/apply_add_wx")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}

	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer overtimeId = null;
		try {
			CSRF.validate(req);
			overtimeId = Https.getInt(req, "overtimeId", R.I);
			String workDate = Https.getStr(req, "work_yyyyMMdd", R.REQUIRED, R.D, "加班日期");
			String start_HH = Https.getStr(req, "start_HH", R.REQUIRED, R.HH, "开始时间");
			String start_mm = Https.getStr(req, "start_mm", R.REQUIRED, R.mm, "开始时间");
			String end_HH = Https.getStr(req, "end_HH", R.REQUIRED, R.HH, "结束时间");
			String end_mm = Https.getStr(req, "end_mm", R.REQUIRED, R.mm, "结束时间");
			Integer typeId = Https.getInt(req, "typeId", R.CLEAN, R.REQUIRED, R.I, "加班类型");
			String project = Https.getStr(req, "project", R.CLEAN, R.RANGE, "{1,60}", "所属项目");
			String content = Https.getStr(req, "content", R.CLEAN, R.REQUIRED, R.RANGE, "{1,200}", "工作描述");
			String remark = Https.getStr(req, "remark", R.CLEAN, R.RANGE, "{1,60}", "备注");
			Integer operatorId = Https.getInt(req, "operatorId", R.CLEAN, R.REQUIRED, R.I, "上级审批");
			String actorName = Https.getStr(req, operatorId.toString(), "{1,60}");
			String titleName = Https.getStr(req, "titleName", "{1,60}");
			
			String startStr = workDate + " " + start_HH + ":" + start_mm;
			String endStr = workDate + " " + end_HH + ":" + end_mm;

			DateTime start = Calendars.parse(startStr, Calendars.DATE_TIME);
			DateTime end = Calendars.parse(endStr, Calendars.DATE_TIME);
			
			Map<String, String> overtimeMap = dictService.map(Dict.OVERTIME);
			String overTimeTypeName = overtimeMap.get(typeId.toString());
			
			Map<String, String[]> remarkMap = new HashMap<String, String[]>();
			
			boolean isLegal = overtimeService.isLegalApply(start, end, remarkMap, overTimeTypeName);
			if(!isLegal){
				throw new Errors("申请的时段不允许加班");
			}
			
			Integer workMinute = Minutes.minutesBetween(start, end).getMinutes();

			Overtime overtime = null;

			if (overtimeId != null) {
				overtime = dao.fetch(Overtime.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("overtimeId", "=", overtimeId));
				Asserts.isNull(overtime, "申请不存在");
				Asserts.notEqOr(overtime.getApprove(),Status.APPROVED, Status.APPROVEDOK, "禁止修改已审批的加班申请");
			} else {
				overtime = new Overtime();
				overtime.setStatus(Status.ENABLED);
				overtime.setCreateTime(new Date());
			}

			StringBuilder buff = new StringBuilder();
				buff.append(Context.getTrueName())
				.append("于")
				.append(start.toString(Calendars.DATE))
				.append("申请加班");
				
			overtime.setUserId(Context.getUserId());
			overtime.setSubject(buff.toString());
			overtime.setStartTime(start.toDate());
			overtime.setEndTime(end.toDate());
			overtime.setTypeId(typeId);
			overtime.setWorkMinute(workMinute);
			overtime.setProject(project);
			overtime.setContent(content);
			overtime.setRemark(remark);
			overtime.setOperatorId(operatorId);
			overtime.setApprove(Status.PROOFING);
			overtime.setModifyTime(new Date());
			overtime.setOpinion(Status.value);
			
			String [] str=userService.findRoleNames(operatorId);
			
			String variable=null;
		
			if(Asserts.hasAny(Roles.BOSS.getName(),str)){
				variable=Roles.BOSS.getName();
			}
			else if(Asserts.hasAny(Roles.GM.getName(),str)){
				variable=Roles.GM.getName();
			}
			else if(Asserts.hasAny(Roles.MAN.getName(),str)){
				variable=Roles.MAN.getName();
			}
			else if(Asserts.hasAny(Roles.SVI.getName(),str)){
				variable=Roles.SVI.getName();
			}
			else if(Asserts.hasAny(Roles.EMP.getName(),str)){
				variable=Roles.EMP.getName();
			}
			else {
				variable=Roles.EMP.getName();
			}
			
			String atitle=titleName+"申请加班";
			
			String url = null;
			url = "http://"+DomainName.OA+"/"+WxOpen.OVERTIME.getURL();

			transSave(overtimeId, overtime, operatorId,atitle,url,actorName, variable, remarkMap);
			
			Code.ok(mb, (overtimeId == null ? "新建" : "编辑") + "加班申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (overtimeId == null ? "新建" : "编辑") + "加班申请失败");
		}

		return mb;
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
				List<Overtime> overtimes = mapper.query(Overtime.class, "Overtime.query", Cnd
						.where("ot.status", "=", Status.ENABLED)
						.and("ot.user_id", "=", Context.getUserId())
						.and("ot.approve", "=", Status.PROOFING)
						.and("ot.overtime_id", "in", arr));
				for (Overtime overtime : overtimes) {
					overtimeService.delete(overtime);
				}
			}
			Code.ok(mb, "删除加班申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除加班申请失败");
		}
		return mb;
	}
	
	private void transSave(final Integer overtimeId, final Overtime overtime, final Integer actorId,final String atitle, final String url, final String actorName, final String variable, final Map<String, String[]> remarkMap) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = overtimeId;
				if (overtimeId != null) {
					dao.clear(OvertimeActor.class, Cnd.where("overtime_Id", "=", overtimeId));
					dao.update(overtime);
				} else {
					id = dao.insert(overtime).getOvertimeId();
				}
				
				//如果申请人属于职能部门,135行政人事部,136市场部,121和德集团
				/*boolean toBoss = false;
				User user = new User();
				user = dao.fetch(User.class,Context.getUserId());
				List<Role> resigRole = null;
				//获取当前申请人角色名称
				Criteria cris = Cnd.cri();
				cris.where()
					.and("u.status", "=", Status.ENABLED)
					.and("u.user_id", "=", overtime.getUserId());
				resigRole =  mapper.query(Role.class,"Role.query", cris);
				if(((135==user.getOrgId())||(136==user.getOrgId()))&&(121==user.getCorpId())){				
					for(Role roleName : resigRole){
						String[] supervisor = roleName.getRoleName().split("\\.");
						if(supervisor.length==2){
						//如果当人申请人属于职能部门主管
						if("supervisor".equals(supervisor[1])){
							toBoss = true;
						}
						}
						}
				}else{
					for(Role roleName : resigRole){
						//如果当人申请人属于公司经理
						if(Roles.GM.getName().equals(roleName.getRoleName())){
							toBoss = true;
						}
					}
				}
				
				// 指定上级审批
				if(toBoss==true)
					dao.insert(new OvertimeActor(id, actorId, Value.I, Status.PROOFING, Roles.BOSS.getName(), overtime.getModifyTime(),"-"));
				else 
					dao.insert(new OvertimeActor(id, actorId, Value.I, Status.PROOFING, Roles.MGR.getName(), overtime.getModifyTime(),"-"));
				*/
				
				// 指定审批
				dao.insert(new OvertimeActor(id, actorId, Value.I, Status.PROOFING, variable, overtime.getModifyTime(), overtime.getOpinion()));
				
				checkedRecordService.update3(overtime.getUserId(), Context.getUserId(), remarkMap);
				
				String description="加班原因:"+overtime.getContent();
				if(WxOpen.OVERTIME.isOPEN()&&wxUserService.overtime()&&overtimeId == null){	
					WxSendService wx=new WxSendService();
					List<Role> roles = roleRepository.find(actorId);
					String[] roleNames = Converts.array(Role.class, String.class, roles, "roleName");
					String agentid=WxRoleUtil.wxrole(roleNames);
					wx.sendarticle(actorName,"","",agentid,atitle,description,url,"");	
				}
				
				if (actorId != 0) {
					User u = dao.fetch(User.class, Cnd.where("user_id", "=", actorId));
					List<String> maiList = new ArrayList<String>();
					if (StringUtils.isNotBlank(u.getEmail())) {
						maiList.add(u.getEmail());
					}
					MailStart mail = new MailStart();
					mail.mail(maiList, atitle, description);
				}
			}
		});
	}
}
