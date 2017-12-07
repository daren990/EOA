package cn.oa.web.action.hrm.regular;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.upload.UploadAdaptor;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Regular;
import cn.oa.model.RegularActor;
import cn.oa.model.Resign;
import cn.oa.model.ResignActor;
import cn.oa.model.Role;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.lang.Uploads;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "hrm.regular.apply")
@At(value = "/hrm/regular/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	@GET
	@At
	@Ok("ftl:hrm/regular/apply_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/hrm/Regular/apply/del", token);
		CSRF.generate(req, "/hrm/Regular/approve/actors", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("r.status", "=", Status.ENABLED)
			.and("r.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "r.approved", "approve", approve);
		Cnds.gte(cri, mb, "r.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "r.create_time", "endTime", endStr);
		cri.getOrderBy().desc("r.modify_time");

		Page<Regular> page = Webs.page(req);
		page = mapper.page(Regular.class, page, "Regular.count", "Regular.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer resignId = Https.getInt(req, "resignId", R.REQUIRED, R.I, "申请ID");
			List<RegularActor> actors = mapper.query(RegularActor.class, "RegularActor.query", Cnd
					.where("a.resign_id", "=", resignId)
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
	
	@GET
	@At
	@Ok("ftl:hrm/regular/apply_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer resignId = Https.getInt(req, "resignId", R.I);
		Regular resign = null;
		if (resignId != null) {
			resign = mapper.fetch(Regular.class, "Resign.query", Cnd
					.where("r.status", "=", Status.ENABLED)
					.and("r.user_id", "=", Context.getUserId())
					.and("r.resign_id", "=", resignId));
			if (resign != null) {
				// 上级审批
				ResignActor actor = dao.fetch(ResignActor.class, Cnd
						.where("resignId", "=", resignId)
						.and("variable", "=", Roles.MGR.getName())
						.limit(1)
						.asc("modifyTime"));
				if (actor != null) {
					resign.setActorId(actor.getActorId());
				}
			}
		}
		if (resign == null)
			resign = new Regular();
		boolean toWho = false;
		boolean toGm =false;
		//获取当前申请人角色名称
		boolean toBoss = false;
		Criteria cri = Cnd.cri();
		cri.where()
			.and("u.status", "=", Status.ENABLED)
			.and("u.user_id", "=", Context.getUserId());
		List<Role> page =  mapper.query(Role.class,"Role.query", cri);
		//如果申请角色为总经理
		for(Role roleName : page){
			if(Roles.GM.getName().equals(roleName.getRoleName())){
				toGm = true;
			}
		}
		//如果申请人属于职能部门,135行政人事部,136市场部,121和德集团
		User user = new User();
		user = dao.fetch(User.class,Context.getUserId());
		List<Role> resigRole = null;
		if(((135==user.getOrgId())||(136==user.getOrgId()))&&(121==user.getCorpId())){
			//获取当前申请人角色名称
			Criteria cris = Cnd.cri();
			cris.where()
				.and("u.status", "=", Status.ENABLED)
				.and("u.user_id", "=", Context.getUserId());
			resigRole =  mapper.query(Role.class,"Role.query", cris);
			toBoss = true;
		}
		/*boolean toWho = false;*/
		List<User> operators = null;
		if(toBoss){
			for(Role roleName : resigRole){
				String[] supervisor = roleName.getRoleName().split("\\.");
				if(supervisor.length==2){
				if("supervisor".equals(supervisor[1])){
					toWho = true;
				}
				}
			}
			if(toWho)
				operators = userService.operators(null, Roles.PSVI.getName());
			else
				operators = userService.operators(Context.getCorpId(), Context.getLevel());

		}else{
			if(toGm)
				operators = userService.operators(null, Roles.PSVI.getName());
			else
				operators = userService.operators(Context.getCorpId(), Context.getLevel());
		}
		if(operators.size() == 0){
			User manager = dao.fetch(User.class, Cnd
					.where("status", "=", Status.ENABLED)
					.and("userId", "=", Context.getUser().getManagerId()));
			operators.add(manager);
		}
		req.setAttribute("operators", operators);
		req.setAttribute("resign", resign);
	}

	@POST
	@At
	@Ok("json")
	@AdaptBy(type = UploadAdaptor.class, args = { "${app.root}/res/tmp" })
	public Object add(HttpServletRequest req, HttpServletResponse res, @Param("..") NutMap map) {
		MapBean mb = new MapBean();
		Integer resignId = null;
		try {
			String token = Validator.getStr(map.get("token"), R.CLEAN, R.REQUIRED, R.RANGE, "{1,32}");
			CSRF.validate(req, token);
			
			resignId = Validator.getInt(map.get("resignId"), R.I, "转正ID");
			Integer actorId = Validator.getInt(map.get("actorId"), R.REQUIRED, R.I, "上级审批");

			Uploads.required(map.get("file"));
			Uploads.max(map.get("file"), 10);
			
			Regular resign = null;
			DateTime now = new DateTime();
			
			if (resignId != null) {
				resign = dao.fetch(Regular.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("resignId", "=", resignId));
				Asserts.isNull(resign, "申请不存在");
				Asserts.notEq(resign.getApproved(), Status.PROOFING, "禁止修改已审批的转正申请");
			} else {
				resign = new Regular();
				resign.setStatus(Status.ENABLED);
				resign.setCreateTime(now.toDate());
			}
			String resignDate = Validator.getStr(map.get("resignDate"), R.REQUIRED, "转正日期");
			StringBuilder buff = new StringBuilder();
			buff.append("转正申请_")
				.append(Context.getUsername())
				.append("申请转正");
			
			resign.setUserId(Context.getUserId());
			resign.setSubject(buff.toString());
			resign.setFilePath(Uploads.path(map.get("file"), now, resign.getFilePath()));
			resign.setApproved(Status.PROOFING);
			resign.setModifyTime(now.toDate());
			//转正日期
			resign.setResignDate(resignDate);
			transSave(resignId, resign, actorId);

			Code.ok(mb, (resignId == null ? "新建" : "编辑") + "转正申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (resignId == null ? "新建" : "编辑") + "转正申请失败");
		} finally {
			
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
				dao.update(Regular.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("approved", "=", Status.PROOFING)
						.and("resignId", "in", arr));
			}
			Code.ok(mb, "删除转正申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除转正申请失败");
		}

		return mb;
	}
	
	private void transSave(final Integer resignId, final Regular resign, final Integer actorId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = resignId;
				if (resignId != null) {
					dao.clear(RegularActor.class, Cnd.where("resignId", "=", resignId));
					dao.update(resign);
				} else {
					id = dao.insert(resign).getResignId();
				}
				//如果申请人属于职能部门,135行政人事部,136市场部,121和德集团
				boolean toBoss = false;
				User user = new User();
				user = dao.fetch(User.class,Context.getUserId());
				List<Role> resigRole = null;
				if(((135==user.getOrgId())||(136==user.getOrgId()))&&(121==user.getCorpId())){
					//获取当前申请人角色名称
					Criteria cris = Cnd.cri();
					cris.where()
						.and("u.status", "=", Status.ENABLED)
						.and("u.user_id", "=", resign.getUserId());
					resigRole =  mapper.query(Role.class,"Role.query", cris);
					toBoss = true;
				}
				boolean toWho = false;
				if(toBoss){
					for(Role roleName : resigRole){
						String[] supervisor = roleName.getRoleName().split("\\.");
						if(supervisor.length==2){
						if("supervisor".equals(supervisor[1])){
							toWho = true;
						}
						}
						}
				}
				// 指定上级审批
				if(toWho==true)
					dao.insert(new RegularActor(id, actorId, Value.I, Status.PROOFING, Roles.PSVI.getName(), resign.getModifyTime(),"-"));
				else 
					dao.insert(new RegularActor(id, actorId, Value.I, Status.PROOFING, Roles.MGR.getName(), resign.getModifyTime(),"-"));
				
			}
		});
	}
}