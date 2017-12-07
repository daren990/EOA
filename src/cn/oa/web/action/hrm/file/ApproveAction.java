package cn.oa.web.action.hrm.file;

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
import cn.oa.model.Archive;
import cn.oa.model.File;
import cn.oa.model.FileActor;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
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

@IocBean(name = "hrm.file.approve")
@At(value = "/hrm/file/approve")
public class ApproveAction extends Action {

	public static Log log = Logs.getLog(ApproveAction.class);
	
	@GET
	@At
	@Ok("ftl:hrm/file/approve_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/hrm/file/apply/nodes", token);
		CSRF.generate(req, "/hrm/file/approve/actors", token);
		
		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);
		Integer approve = Https.getInt(req, "approve", R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("f.status", "=", Status.ENABLED)
			.and("a.actor_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "a.approve", "approve", approve);
		Cnds.gte(cri, mb, "f.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "f.create_time", "endTime", endStr);
		cri.getOrderBy().desc("f.modify_time");
		
		Page<File> page = Webs.page(req);
		page = mapper.page(File.class, page, "FileApprove.count", "FileApprove.index", cri);

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
			Integer fileId = Https.getInt(req, "fileId", R.REQUIRED, R.I, "档案文件ID");
			List<FileActor> actors = mapper.query(FileActor.class, "FileActor.query", Cnd
					.where("a.file_id", "=", fileId)
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
	@Ok("ftl:hrm/file/approve_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer fileId = Https.getInt(req, "fileId", R.REQUIRED, R.I, "档案文件ID");
		
		File file = mapper.fetch(File.class, "FileApprove.query", Cnd
				.where("a.actor_id", "=", Context.getUserId())
				.and("f.file_id", "=", fileId));
		Asserts.isNull(file, "档案文件不存在");
		
		List<FileActor> actors = mapper.query(FileActor.class, "FileActor.query", Cnd.where("a.file_id", "=", fileId).asc("a.modify_time"));
		FileActor actor = null; // 当前审批人员
		
		Integer bindId = null;
		for (FileActor e : actors) {
			if (e.getActorId().equals(Context.getUserId())) actor = e; // last
			if (e.getVariable().equals(Roles.Me.getName())) bindId = e.getActorId();
		}
		Asserts.isNull(actor, "当前审批人员不存在");
		
		req.setAttribute("file", file);
		req.setAttribute("actors", actors);
		req.setAttribute("actor", actor);
		req.setAttribute("bindId", bindId);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer fileId = null;
		try {
			CSRF.validate(req);
			
			fileId = Https.getInt(req, "fileId", R.REQUIRED, R.I, "档案文件ID");
			Integer approve = Https.getInt(req, "approve", R.REQUIRED, R.I, R.IN, "in [1,-1]", "处理状态");
			
			DateTime now = new DateTime();
			File file = mapper.fetch(File.class, "FileApprove.query", Cnd
					.where("a.actor_id", "=", Context.getUserId())
					.and("f.file_id", "=", fileId));
			Asserts.isNull(file, "档案文件不存在");

			Asserts.notHasAny(file.getApproved(), new Integer[] { Status.PROOFING, Status.UNAPPROVED }, "禁止编辑已同意的档案文件");
			
			FileActor actor = dao.fetch(FileActor.class, Cnd.where("fileId", "=", fileId).and("actorId", "=", Context.getUserId()));
			Asserts.isNull(actor, "禁止审批档案文件");
			
			transApprove(fileId, approve, Context.getUserId(), now);
			
			Code.ok(mb, "档案文件处理成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:add) error: ", e);
			Code.error(mb, "档案文件处理失败");
		}

		return mb;
	}

	private void transApprove(final Integer fileId, final Integer approve, final Integer actorId, final DateTime now) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer self = Status.UNAPPROVED;
				dao.update(FileActor.class, Chain
						.make("approve", approve)
						.add("modifyTime", now.toDate()), Cnd
						.where("fileId", "=", fileId)
						.and("actorId", "=", actorId));
				dao.clear(FileActor.class, Cnd.where("fileId", "=", fileId).and("approve", "=", Status.PROOFING));

				if (approve.equals(Status.APPROVED)) {
					self = Status.OK;
				}
				
				dao.update(File.class, Chain
						.make("approved", self)
						.add("modifyTime", now.toDate()), Cnd
						.where("fileId", "=", fileId));
				
				if (approve.equals(Status.APPROVED)) {
					File file = dao.fetch(File.class, fileId);
					Archive archive = dao.fetch(Archive.class, actorId);
					boolean exist = true;
					if (archive == null) {
						archive = new Archive();
						exist = false;
					}
					
					copy(file, archive);

					if (exist)
						dao.update(archive);
					else
						dao.insert(archive);
					
					dao.update(User.class, Chain
							.make("trueName", file.getTrueName()), Cnd
							.where("userId", "=", actorId));
				}
			}
		});
	}
	
	private void copy(File file, Archive archive) {
		archive.setUserId(file.getUserId());
		archive.setGender(file.getGender());
		archive.setBirthday(file.getBirthday());
		archive.setPlace(file.getPhone());
		archive.setMarry(file.getMarry());
		archive.setDegree(file.getDegree());
		archive.setMajor(file.getMajor());
		archive.setSchool(file.getSchool());
		archive.setIdcard(file.getIdcard());
		archive.setPhone(file.getPhone());
		archive.setEntryDate(file.getEntryDate());
		archive.setFullDate(file.getFullDate());
	}
}