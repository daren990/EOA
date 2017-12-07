package cn.oa.web.action.adm.expense.reimburse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Accountant;
import cn.oa.model.Dict;
import cn.oa.model.Org;
import cn.oa.model.Project;
import cn.oa.model.Reimburse;
import cn.oa.model.ReimburseActor;
import cn.oa.model.ReimburseItem;
import cn.oa.model.ReimburseThreshold;
import cn.oa.model.ReimburseType;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.service.UserService;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MailC;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.Values;
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

@IocBean(name = "adm.expense.reimburse.approve")
@At(value = "/adm/expense/reimburse/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);
	
	
	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/expense/reimburse/approve/actors", token);
		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		String number = Https.getStr(req, "number", R.CLEAN,"{1,100}");
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("r.status", "=", Status.ENABLED)
			.and("a.actor_id", "=", Context.getUserId());
		Cnds.gte(cri, mb, "r.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "r.create_time", "endTime", endStr);
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		Cnds.eq(cri, mb, "r.number", "number", number);
		cri.getOrderBy().desc("r.modify_time");
		
		Page<Reimburse> page = Webs.page(req);
		page = mapper.page(Reimburse.class, page, "ReimburseApprove.count", "ReimburseApprove.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/approve_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/approve_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer reimburseId = Https.getInt(req, "reimburseId", R.REQUIRED, R.I, "申请ID");
			List<ReimburseActor> actors = mapper.query(ReimburseActor.class, "ReimburseActor.query", Cnd
					.where("a.reimburse_id", "=", reimburseId)
					.asc("a.modify_time"));
			mb.put("actors", actors);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:actors) error: ", e);
		}

		return mb;
	}
	
	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer threshold = Value.I;
		List<ReimburseItem> reimburseItems = null;
		threshold = dao.fetch(ReimburseThreshold.class,1).getThresholdValue();
		threshold *= 100; 
		Integer reimburseId = Https.getInt(req, "reimburseId", R.REQUIRED, R.I, "申请ID");
		Reimburse reimburse = mapper.fetch(Reimburse.class, "ReimburseApprove.query", Cnd
				.where("a.actor_id", "=", Context.getUserId())
				.and("r.reimburse_id", "=", reimburseId));
		Asserts.isNull(reimburse, "申请不存在");
		//审批物品列表
		reimburseItems = dao.query(ReimburseItem.class, Cnd.where("reimburseId", "=", reimburseId));
		
		//审批的人员列表
		List<ReimburseActor> actors = mapper.query(ReimburseActor.class, "ReimburseActor.query", Cnd
				.where("a.reimburse_id", "=", reimburseId)
				.asc("a.modify_time"));
		ReimburseActor actor = null; // 当前审批人员
		ReimburseActor next = null; // 指向下一审批人员
		
		//下级审批人
		Integer bindId = null;
		for (ReimburseActor e : actors) {
			//获取到审批人信息
			if (e.getActorId().equals(Context.getUserId())) actor = e;
			//获取审批人有没有是助理,肯定有的-------------------------48-----------------------********************__________
			if (e.getVariable().equals(Roles.ASS.getName())) bindId = e.getActorId();
		}
		//判断下级审批人,此处除助理审批外全为0
		if (actor.getRefererId() != 0) {
			//获取下一个审批人
			for (ReimburseActor e : actors) {
				if (e.getActorId().equals(actor.getRefererId())) next = e;
			}
		}
		//查找预算项目
		Project project = null;
		if(reimburse.getProjectId() == 0){	
			project = new Project();
			project.setProjectName("无");			
		}else{
		project = dao.fetch(Project.class, reimburse.getProjectId());
		Asserts.isNull(project, "项目不存在");
		}
		Integer orgId =dao.fetch(User.class, reimburse.getUserId()).getCorpId();
		Integer accountant = dao.fetch(Org.class, orgId).getAccountantId();
		Asserts.isNull(accountant, "财务人员配置不能为空");
		Accountant userIds = dao.fetch(Accountant.class, accountant);
		Asserts.isNull(userIds, "财务人员配置不能为空");
		//财务人员的ID
		String [] str = Strings.splitIgnoreBlank(userIds.getUserIds(), ",");
		
		List<User> operators = null;
	//	Integer[] actorIds = Converts.array(ReimburseActor.class, Integer.class, actors, "actorId");
		//如果是助理审批，下一审批人是预算经理
		if (actor.getVariable().equals(Roles.ASS.getName())) {
			//如果没有预算项目,则交给总经理
			if(reimburse.getProjectId() == 0){
				operators = userService.operators(Roles.GM.getName());
				bindId = null;
			}else{
			operators = dao.query(User.class, Cnd.where("userId", "=", project.getOperatorId()));
			bindId = project.getOperatorId();
			}
		//如果是预算经理审批下一步是总经理
		} else if (actor.getVariable().equals(Roles.BGM.getName())) {
		
			
			String [] context = userService.findRoleNames(Context.getUserId());
			//判断预算经理是否也是总经理
			if(Asserts.hasAny(context, new String[] { Roles.GM.getName()})){
				//判断是否超过预算阀值,如果超过了,则交给总裁
				if(reimburse.getAmount() > threshold){
					operators = userService.operators(Roles.BOSS.getName());//"""""""""""""'''''''''''''''''''''''''''''''''''''''
					bindId = null;
				}
				else{
					operators = new ArrayList<User>();
					for(String userId : str){
						String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
						//判断财务人员是否包含会计
						if(Asserts.hasAny(Roles.ACC.getName(), roleName)){
							//operators = new ArrayList<User>();
							//查找会计user列表信息
							operators.add(dao.fetch(User.class, Integer.parseInt(userId)));//"""""""""""""'''''''''''''''''''''''''''''''''''''''
							//*********************************这里需要补充bindid
							bindId = null;
						}
					}
				}
					
			}
			//预算经理非总经理,则下级审批为总经理
			else{				
				operators = userService.operators(Context.getCorpId(), Roles.GM.getName());//"""""""""""""'''''''''''''''''''''''''''''''''''''''
				bindId = null;
			}
			
		
		//如果是总经理，判断有没超过阀值，超过则要通过总裁,否则根据公司配置财务
		} else if (actor.getVariable().equals(Roles.GM.getName())) {
			
			if(reimburse.getAmount() > threshold){
				operators = userService.operators(Roles.BOSS.getName());
				bindId = null;
			}
			else{
				operators = new ArrayList<User>();
				for(String userId : str){
					String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
					if(Asserts.hasAny(Roles.ACC.getName(), roleName)){
						//operators = new ArrayList<User>();
						operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
						bindId = null;
					}
				}
				/*List<User> list = userService.operators(Roles.BOSS.getName());
				for(User user : list){
					operators.add(user);
				}*/
			}
		}
		//如果是总裁，则根据公司配置财务
		if(actor.getVariable().equals(Roles.BOSS.getName())){
			operators = new ArrayList<User>();
			for(String userId : str){
				String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
				if(Asserts.hasAny(Roles.ACC.getName(), roleName)){
					//operators = new ArrayList<User>();
					operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
					bindId = null;
				}
			}
		}
		//如果是会计下一步是会计主管
		if(actor.getVariable().equals(Roles.ACC.getName())){
			operators = new ArrayList<User>();
			for(String userId : str){
				String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
				if(Asserts.hasAny(Roles.ASVI.getName(), roleName)){
					//operators = new ArrayList<User>();
					operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
					bindId = null;
				}
			}
		}
		//如果是会计主管下一步是出纳
		if(actor.getVariable().equals(Roles.ASVI.getName())){
			operators = new ArrayList<User>();
			for(String userId : str){
				String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
				if(Asserts.hasAny(Roles.CAS.getName(), roleName)){
					//operators = new ArrayList<User>();
					operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
					bindId = null;
				}
			}
		}
		
		//如果是出纳下一步是出纳主管
		if(actor.getVariable().equals(Roles.CAS.getName())){
			operators = new ArrayList<User>();
			for(String userId : str){
				String roleName[] = userService.findRoleNames(Integer.parseInt(userId));
				if(Asserts.hasAny(Roles.CSVI.getName(), roleName)){
					//operators = new ArrayList<User>();
					operators.add(dao.fetch(User.class, Integer.parseInt(userId)));
					bindId = null;
				}
			}
		}
		
		//如果是出纳主管
		if(actor.getVariable().equals(Roles.CSVI.getName())){
					operators = new ArrayList<User>();
					bindId = null;
				}
			
		
			
		List<ReimburseType> type = dao.query(ReimburseType.class, null);
		List<Map<String, String>> smallType = new ArrayList<Map<String, String>>();
		for(ReimburseType reimburseType : type){
				smallType.add(Converts.map(reimburseType.getSmallType()));
			}
		if(reimburseId != null){
		for(ReimburseItem item : reimburseItems){
			Integer big = item.getBigType();
			Integer small = item.getSmallType();
			String smalltype = dao.fetch(ReimburseType.class, big).getSmallType();
			Map<String,String> map = Converts.map(smalltype);
			item.setSmallName(map.get(small+""));
		}
	}
		
		req.setAttribute("projects", project);
		req.setAttribute("type", type);
		req.setAttribute("smallType",smallType);
		req.setAttribute("operators", operators);
		req.setAttribute("reimburse", reimburse);
		//当前审批人员list
		req.setAttribute("actors", actors);
		//当前审批人员
		req.setAttribute("actor", actor);
		//下一级审批人员
		req.setAttribute("next", next);
		//审批人员的refererId  //这里有误      与operators重复了,值已改为null
		req.setAttribute("bindId", bindId);
		//报销元素ID,单据
		req.setAttribute("reimburseItems", reimburseItems);
	//	req.setAttribute("expenseMap", dictService.map(Dict.EXPENSE));
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/approve_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/approve_add_wx")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer reimburseId = null;
		Integer threshold = Value.I;
		threshold = dao.fetch(ReimburseThreshold.class,1).getThresholdValue();
		threshold *= 100;
		try {
			CSRF.validate(req);
			
			reimburseId = Https.getInt(req, "reimburseId", R.REQUIRED, R.I, "申请ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1,2]", "审批状态");
			String opinion = Https.getStr(req, "opinion", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "opinion:审批意见");
			
			DateTime now = new DateTime();
			Reimburse reimburse = mapper.fetch(Reimburse.class, "ReimburseApprove.query", Cnd
					.where("a.actor_id", "=", Context.getUserId())
					.and("r.reimburse_id", "=", reimburseId));
			Asserts.isNull(reimburse, "申请不存在");
			
			/*ReimburseActor actor = dao.fetch(ReimburseActor.class, Cnd
					.where("reimburseId", "=", reimburseId)
					.and("actorId", "=", Context.getUserId()).orderBy().desc("actorId"));*/
			/*ReimburseActor actor = dao.fetch(ReimburseActor.class, Cnd
					.where("reimburseId", "=", reimburseId)
					.and("actorId", "=", Context.getUserId()));*/
			
			Criteria cris = Cnd
					.where("reimburseId", "=", reimburseId)
					.and("actorId", "=", Context.getUserId());
			cris.getOrderBy().desc("modify_time");
			ReimburseActor actor = dao.fetch(ReimburseActor.class, cris);

			Asserts.isNull(actor, "禁止审批报销申请");
			
			Integer required = R.CLEAN;
			
			if (!actor.getVariable().equals(Roles.CSVI.getName()) && approve.equals(Status.APPROVED)) {
				required = R.REQUIRED;
			}
			Integer refererId = Https.getInt(req, "refererId", R.CLEAN, required, R.I, "审批人员");
			
			refererId = Values.getInt(refererId);
			//是否有预算项目
			Project project = null;
			if(reimburse.getProjectId() == 0){			
			}else{
			project = dao.fetch(Project.class, reimburse.getProjectId());
			Asserts.isNull(project, "项目不存在");
			
			Criteria cri = Cnd
					.where("projectId", "=", project.getProjectId())
					.and("deduct", "=", Value.T);
			if (reimburseId != null)
				cri.where().and("reimburseId", "!=", reimburseId);
			Integer deduct = dao.func(Reimburse.class, "sum", "amount", cri);
			
			if (project.getMoney() - deduct - reimburse.getAmount() < 0)
				throw new Errors("禁止审批余额不足的项目");
			}//---
			if (actor.getRefererId() != 0) {
				ReimburseActor next = dao.fetch(ReimburseActor.class, Cnd
						.where("reimburseId", "=", reimburseId)
						.and("actorId", "=", actor.getRefererId()));
				if (next != null)
					Asserts.notEq(next.getApprove(), Status.PROOFING, "禁止修改已审批的报销申请");
			}
			
			String nextVariable = null;
			if (approve.equals(Status.APPROVED)) {
				if (actor.getVariable().equals(Roles.ASS.getName())) {
					if(reimburse.getProjectId() == 0)
						nextVariable = Roles.GM.getName();
					else
						nextVariable = Roles.BGM.getName();
				} else if (Asserts.hasAny(actor.getVariable(), new String[] { Roles.BGM.getName()})) {
					
					String [] context = userService.findRoleNames(Context.getUserId());
					
					if(Asserts.hasAny(context, new String[] { Roles.GM.getName()})){
						if(reimburse.getAmount() > threshold){
							nextVariable = Roles.BOSS.getName();
						}
						else
							nextVariable = Roles.ACC.getName();
					}
					else{
						nextVariable = Roles.GM.getName();
					}
					
				} else if (actor.getVariable().equals(Roles.GM.getName())) {
					if(reimburse.getAmount() > threshold){
						nextVariable = Roles.BOSS.getName();
					}
					else{
						nextVariable = Roles.ACC.getName();
					}
				}
				else if(actor.getVariable().equals(Roles.ACC.getName())){
					nextVariable = Roles.ASVI.getName();
				}
				else if(actor.getVariable().equals(Roles.ASVI.getName())){
					nextVariable = Roles.CAS.getName();
				}
				else if(actor.getVariable().equals(Roles.BOSS.getName())){
					nextVariable = Roles.ACC.getName();
				}
				else 
					nextVariable = Roles.CSVI.getName();
				
			}
			
			transApprove(reimburseId, approve, Context.getUserId(), actor.getVariable(), refererId, nextVariable, now, opinion);
			Boolean self = refererId.equals(Value.I) ? true : false;
			List<String> maiList = new ArrayList<String>();
			String mailR = null;
			if(self){
				User u = dao.fetch(User.class,reimburse.getUserId());
				if(u!=null){
					boolean next = true;
					if(!(u.getEmail()==null||"".equals(u.getEmail()))){
					if(MailC.checkEmail(u.getEmail())){
						maiList.add(u.getEmail());
						}else{
							mailR = "申请人邮箱格式不对!";
							next = false;
						}
					}else{
						mailR = "申请人没有填写邮箱!";
						next = false;
					}
					if(next){
					String subject = "你的报销申请已审批完成";

					String content = "请登录OA进行核对";
					MailStart mail = new MailStart();
					mailR = mail.mail(maiList,subject,content);
					}
				}
				
			}
			Code.ok(mb, "申请审批成功"+(mailR==null?"":" "+mailR));
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "申请审批失败");
		}

		return mb;
	}
	
	private void transApprove(final Integer reimburseId, final Integer approve, final Integer actorId, final String variable,
			final Integer refererId, final String nextVariable, final DateTime now, final String opinion) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				dao.update(ReimburseActor.class, Chain
						.make("approve", approve)
						.add("refererId", refererId)
						.add("modifyTime", now.toDate())
						.add("opinion", opinion), Cnd
						.where("reimburseId", "=", reimburseId)
						.and("actorId", "=", actorId));
				dao.clear(ReimburseActor.class, Cnd.where("reimburseId", "=", reimburseId).and("approve", "=", Status.PROOFING));
				if (approve.equals(Status.APPROVED)) {
					if (!refererId.equals(Value.I))
						dao.insert(new ReimburseActor(reimburseId, refererId, Value.I, Status.PROOFING, nextVariable, now.plusSeconds(1).toDate(),"-"));
					self = refererId.equals(Value.I) ? Status.OK : Status.APPROVED;
				}
				
				Chain chain = Chain
						.make("approved", self)
						.add("modifyTime", now.toDate());
				
				// 是否扣除金额
				if (Asserts.hasAny(variable,
						new String[] { Roles.CSVI.getName()})) {
					chain.add("deduct", approve.equals(Status.APPROVED) ? Value.T : Value.F);
				}
				
				dao.update(Reimburse.class, chain, Cnd
						.where("reimburseId", "=", reimburseId));
			}
		});
	}
	
}