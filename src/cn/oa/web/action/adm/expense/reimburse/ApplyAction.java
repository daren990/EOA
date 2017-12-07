package cn.oa.web.action.adm.expense.reimburse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.parser.Entity;

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
import cn.oa.model.Asset;
import cn.oa.model.Dict;
import cn.oa.model.Project;
import cn.oa.model.Reimburse;
import cn.oa.model.ReimburseActor;
import cn.oa.model.ReimburseItem;
import cn.oa.model.ReimburseType;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.RMB;
import cn.oa.utils.ReimburseNumber;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Servlets;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.expense.reimburse.apply")
@At(value = "/adm/expense/reimburse/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	public void pageUtil(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/expense/reimburse/apply/del", token);
		CSRF.generate(req, "/adm/expense/reimburse/approve/actors", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		String number = Https.getStr(req, "number", R.CLEAN,"{1,100}");

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("r.status", "=", Status.ENABLED)
			.and("r.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "r.approved", "approve", approve);
		Cnds.eq(cri, mb, "r.number", "number", number);
		Cnds.gte(cri, mb, "r.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "r.create_time", "endTime", endStr);
		cri.getOrderBy().desc("r.modify_time");
		
		Page<Reimburse> page = Webs.page(req);
		page = mapper.page(Reimburse.class, page, "Reimburse.count", "Reimburse.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/apply_page")
	public void page(HttpServletRequest req) {
		pageUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/apply_page_wx")
	public void wxpage(HttpServletRequest req) {
		pageUtil(req);
	}

	public void addUtil(HttpServletRequest req) {
		CSRF.generate(req);
		Integer reimburseId = Https.getInt(req, "reimburseId", R.I);
		Reimburse reimburse = null;
		List<ReimburseItem> reimburseItems = null;
		
		if (reimburseId != null) {
			reimburse = mapper.fetch(Reimburse.class, "Reimburse.query", Cnd
					.where("r.status", "=", Status.ENABLED)
					.and("r.user_id", "=", Context.getUserId())
					.and("r.reimburse_id", "=", reimburseId));
			if (reimburse != null) {
				// 助理审批
				ReimburseActor actor = dao.fetch(ReimburseActor.class, Cnd
						.where("reimburseId", "=", reimburseId)
						.and("variable", "=", Roles.ASS.getName())
						.limit(1)
						.asc("modifyTime"));
				
				if (actor != null) {
					reimburse.setActorId(actor.getActorId());
				}
				reimburseItems = dao.query(ReimburseItem.class, Cnd.where("reimburseId", "=", reimburseId));
			}
		}
		if(Asserts.isEmpty(reimburseItems)){
			reimburseItems = new ArrayList<ReimburseItem>();
			reimburseItems.add(new ReimburseItem());
		}
		if (reimburse == null)
			reimburse = new Reimburse();

		List<Project> projects = dao.query(Project.class, Cnd.where("status", "=", Status.ENABLED).and("approved", "=", Status.OK));
		List<User> operators = userService.operators(Context.getCorpId(), Roles.ASS.getName());
		List<ReimburseType> type = dao.query(ReimburseType.class, null);
	//	List<String> bigType = new ArrayList<String>();
		List<Map<String, String>> smallType = new ArrayList<Map<String, String>>();
		for(ReimburseType reimburseType : type){
		//	bigType.add(reimburseType.getBigType());
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
		
		
		req.setAttribute("type", type);
		req.setAttribute("reimburseItems", reimburseItems);
		req.setAttribute("operators", operators);
		req.setAttribute("reimburse", reimburse);
		req.setAttribute("projects", projects);
	//	req.setAttribute("bigType",bigType);
		req.setAttribute("smallType",smallType);
	//	req.setAttribute("expenseMap", dictService.map(Dict.EXPENSE));
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/apply_add")
	public void add(HttpServletRequest req) {
		addUtil(req);
	}
	
	@GET
	@At
	@Ok("ftl:adm/expense/reimburse/apply_add_wx")
	public void wxadd(HttpServletRequest req) {
		addUtil(req);
	}

	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer reimburseId = null;
		Float moneyTest = 0f;
		Integer billTest = 0;
		try {
			CSRF.validate(req);
			reimburseId = Https.getInt(req, "reimburseId", R.I);
		//	Integer typeId = Https.getInt(req, "typeId", R.REQUIRED, R.I, "报销类型");
			Integer projectId = Https.getInt(req, "projectId", R.REQUIRED, R.I, "预算项目");
			Float amount = Https.getFloat(req, "amount", R.REQUIRED, R.F, "报销总金额");
			Integer sumbill = Https.getInt(req, "sumbill", R.REQUIRED, R.I, "单据总数");
			Integer actorId = Https.getInt(req, "actorId", R.REQUIRED, R.I, "助理审批");
			String remark = Https.getStr(req, "remark", R.REQUIRED, R.RANGE,"{1,120}", "备注");
			String theme = Https.getStr(req, "theme", R.REQUIRED, R.RANGE,"{1,120}", "报销主题");
			
			Reimburse reimburse = null;
			DateTime now = new DateTime();
			Project project =null;
			if(projectId == 0){
				
			}else{
			project = dao.fetch(Project.class, projectId);
			Asserts.isNull(project, "项目不存在");			
			DateTime start = new DateTime(project.getStartDate());
			DateTime end = new DateTime(project.getEndDate());

			if (start.isAfter(now) || end.isBefore(now))
				throw new Errors("禁止申请不在预算时间范围内的项目");

			Criteria cri = Cnd
					.where("projectId", "=", projectId)
					.and("deduct", "=", Value.T);
			if (reimburseId != null)
				cri.where().and("reimburseId", "!=", reimburseId);
			Integer deduct = dao.func(Reimburse.class, "sum", "amount", cri);
			
			if (project.getMoney() - deduct - RMB.on(amount) < 0)
				throw new Errors("禁止申请余额不足的项目");
			}
			if (reimburseId != null) {
				reimburse = dao.fetch(Reimburse.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("reimburseId", "=", reimburseId));
				
				Asserts.isNull(reimburse, "申请不存在");
				Asserts.notEq(reimburse.getApproved(), Status.PROOFING, "禁止修改已审批的报销申请");
			} else {
				reimburse = new Reimburse();
				reimburse.setStatus(Status.ENABLED);
				reimburse.setCreateTime(now.toDate());
			}
			
			
			Map<String, Object> bigTypeMap = Servlets.startsWith(req, "bigType_");
			Map<String, Object> smallTypeMap = Servlets.startsWith(req, "smallType_");
			Map<String, Object> moneyMap = Servlets.startsWith(req, "money_");
			Map<String, Object> billMap = Servlets.startsWith(req, "bill_");
			Map<String, Object> purposeMap = Servlets.startsWith(req, "purpose_");
			
			for(Entry<String, Object> entry : bigTypeMap.entrySet()){
				String index = entry.getKey();
				Validator.validate(entry.getValue(), R.REQUIRED, R.I, "报销类型");
				Validator.validate(smallTypeMap.get(index), R.REQUIRED, R.I, "报销小类");
				Validator.validate(moneyMap.get(index), R.REQUIRED, R.F, "报销金额");
				Validator.validate(billMap.get(index), R.REQUIRED, R.I, "票据张数");
				Validator.validate(purposeMap.get(index), R.REQUIRED, R.CLEAN, "{1,120}", "用途");
				
				String Intmoney =  Values.getStr((String)(moneyMap.get(index)),"");
				Float money =  Float.parseFloat(Intmoney);
				Integer bill =Values.getInt( billMap.get(index));
				moneyTest = moneyTest + money;
				billTest = billTest + bill;
			}
			Asserts.notEq(billTest, sumbill, "单据总数不正确");
			Asserts.notEq(moneyTest, amount, "金额总数不正确");
				
		//	Map<String, String> expenseMap = dictService.map(Dict.EXPENSE);
			
			StringBuilder buff = new StringBuilder();
			buff.append(Context.getTrueName())
				.append("申请报销")
				.append(amount)
				.append("元");
			reimburse.setUserId(Context.getUserId());
			reimburse.setSubject(buff.toString());
			reimburse.setProjectId(projectId);
			reimburse.setAmount(RMB.toMinute(amount,null));
			reimburse.setDeduct(Value.F);
			reimburse.setApproved(Status.PROOFING);
			reimburse.setModifyTime(now.toDate());
			reimburse.setRemark(remark);
			reimburse.setSumBill(sumbill);
			reimburse.setTheme(theme);
			
			transSave(reimburseId, reimburse, actorId, bigTypeMap, smallTypeMap, moneyMap, billMap, purposeMap);

			Code.ok(mb, (reimburseId == null ? "新建" : "编辑") + "报销申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (reimburseId == null ? "新建" : "编辑") + "报销申请失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer reimburseId = Https.getInt(req, "reimburseId", R.I);
			ReimburseType reimburseType = dao.fetch(ReimburseType.class,reimburseId);
			Map<String, String> nodes = new HashMap<String, String>();
			nodes = Converts.map(reimburseType.getSmallType());
			mb.put("nodes", nodes);
			
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Asset:asset) error: ", e);
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
				dao.update(Reimburse.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("approved", "=", Status.PROOFING)
						.and("reimburseId", "in", arr));
			}
			Code.ok(mb, "删除报销申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除报销申请失败");
		}

		return mb;
	}
	
	private void transSave(final Integer reimburseId, final Reimburse reimburse, final Integer actorId,
							final Map<String, Object> bigTypeMap,
							final Map<String, Object> smallTypeMap,
							final Map<String, Object> moneyMap,
							final Map<String, Object> billMap,
							final Map<String, Object> purposeMap) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = reimburseId;
				if (reimburseId != null) {
					dao.clear(ReimburseActor.class, Cnd.where("reimburseId", "=", reimburseId));
					reimburse.setNumber(ReimburseNumber.create(id));
					dao.update(reimburse);
				} else {
					Reimburse resultSet = dao.insert(reimburse);
					resultSet.setNumber(ReimburseNumber.create(resultSet.getReimburseId()));
					dao.update(resultSet);
					id = resultSet.getReimburseId();
				}
				
				dao.clear(ReimburseItem.class, Cnd.where("reimburseId", "=", reimburseId));
				List<ReimburseItem> items = new ArrayList<ReimburseItem>();
				
				for(Entry<String, Object> entry : purposeMap.entrySet()){
					String index = entry.getKey();
					String purpose = (String) entry.getValue(); 
					String Intmoney =  Values.getStr((String)(moneyMap.get(index)),"");
					Integer smallType =Values.getInt(smallTypeMap.get(index));
					Integer bigType = Values.getInt(bigTypeMap.get(index));
					Float money =  Float.parseFloat(Intmoney);
					Integer bill =Values.getInt( billMap.get(index));
					
					items.add(new ReimburseItem(id, bigType, RMB.toMinute(money,null), bill, purpose, smallType));
				}
				dao.fastInsert(items);
				
				// 指定助理审批
				dao.insert(new ReimburseActor(id, actorId, Value.I, Status.PROOFING, Roles.ASS.getName(), reimburse.getModifyTime(), "-"));
			}
		});
	}
}
