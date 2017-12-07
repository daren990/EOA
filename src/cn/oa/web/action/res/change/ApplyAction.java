package cn.oa.web.action.res.change;

import java.util.List;

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
import cn.oa.model.Asset;
import cn.oa.model.Change;
import cn.oa.model.ChangeActor;
import cn.oa.model.Dict;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
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

@IocBean(name = "res.change.apply")
@At(value = "/res/change/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);

	@GET
	@At
	@Ok("ftl:res/change/apply_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/res/change/apply/del", token);
		CSRF.generate(req, "/res/change/approve/actors", token);

		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);

		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("c.status", "=", Status.ENABLED)
			.and("c.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "c.approved", "approve", approve);
		Cnds.gte(cri, mb, "c.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "c.create_time", "endTime", endStr);
		cri.getOrderBy().desc("c.modify_time");

		Page<Change> page = Webs.page(req);
		page = mapper.page(Change.class, page, "Change.count", "Change.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@GET
	@At
	@Ok("ftl:res/change/apply_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer changeId = Https.getInt(req, "changeId", R.I);
		Change change = null;
		if (changeId != null) {
			change = mapper.fetch(Change.class, "Change.query", Cnd
					.where("c.status", "=", Status.ENABLED)
					.and("c.user_id", "=", Context.getUserId())
					.and("c.change_id", "=", changeId));
			if (change != null) {
				// 上级审批
				ChangeActor actor = dao.fetch(ChangeActor.class, Cnd
						.where("changeId", "=", changeId)
						.and("variable", "=", Roles.MGR.getName())
						.limit(1)
						.asc("modifyTime"));
				if (actor != null) {
					change.setActorId(actor.getActorId());
				}
			}
		}
		if (change == null)
			change = new Change();

		List<User> operators = userService.operators(Context.getCorpId(), Context.getLevel());
		List<Asset> assets = mapper.query(Asset.class, "Asset.query1", Cnd.where("a.status","=",Status.ENABLED));
		
		req.setAttribute("operators", operators);
		req.setAttribute("change", change);
		req.setAttribute("assets", assets);
		req.setAttribute("assetMap", dictService.map(Dict.ASSET));
	}
	
	@POST
	@At
	@Ok("json")
	public Object nodes(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer typeId = Https.getInt(req, "typeId", R.I);
			List<Asset> nodes = mapper.query(Asset.class, "Asset.query1", Cnd.where("a.status","=",Status.ENABLED).and("typeId", "=", typeId));
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
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer changeId = null;
		try {
			CSRF.validate(req);
			changeId = Https.getInt(req, "changeId", R.I);
			
			Integer assetId = Https.getInt(req, "assetId", R.REQUIRED, R.I, "资产名称");
			String reason = Https.getStr(req, "reason", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "使用原因");
			Integer actorId = Https.getInt(req, "actorId", R.REQUIRED, R.I, "上级审批");
			
			Change change = null;
			DateTime now = new DateTime();
			
			if (changeId != null) {
				change = dao.fetch(Change.class, Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("changeId", "=", changeId));
				Asserts.isNull(change, "申请不存在");
				Asserts.notEq(change.getApproved(), Status.PROOFING, "禁止修改已审批的资产申请");
			} else {
				change = new Change();
				change.setStatus(Status.ENABLED);
				change.setCreateTime(now.toDate());
			}
			
			Asset asset = dao.fetch(Asset.class, assetId);
			
			StringBuilder buff = new StringBuilder();
			buff.append("资产申请_")
				.append(Context.getUsername())
				.append("登记使用")
				.append(asset.getAssetName());

			change.setUserId(Context.getUserId());
			change.setSubject(buff.toString());
			change.setAssetId(assetId);
			change.setReason(reason);
			change.setApproved(Status.PROOFING);
			change.setModifyTime(now.toDate());
			
			transSave(changeId, change, actorId);

			Code.ok(mb, (changeId == null ? "新建" : "编辑") + "资产申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (changeId == null ? "新建" : "编辑") + "资产申请失败");
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
				dao.update(Change.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("userId", "=", Context.getUserId())
						.and("approved", "=", Status.PROOFING)
						.and("changeId", "in", arr));
			}
			Code.ok(mb, "删除资产申请成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除资产申请失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object returned(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				dao.update(Change.class, Chain
						.make("returned", Status.ENABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("approved", "=", 3)
						.and("changeId", "in", arr));
			}
			Code.ok(mb, "归还资产成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:returned) error: ", e);
			Code.error(mb, "归还资产失败");
		}

		return mb;
	}
	
	private void transSave(final Integer changeId, final Change change, final Integer actorId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = changeId;
				if (changeId != null) {
					dao.clear(ChangeActor.class, Cnd.where("changeId", "=", changeId));
					dao.update(change);
				} else {
					id = dao.insert(change).getChangeId();
				}
				
				// 指定上级审批
				dao.insert(new ChangeActor(id, actorId, Value.I, Status.PROOFING, Roles.MGR.getName(), change.getModifyTime(), Status.value));
			}
		});
	}
}