package cn.oa.web.action.hrm.file;

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
import cn.oa.model.Archive;
import cn.oa.model.File;
import cn.oa.model.FileActor;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Calendars;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.lang.Uploads;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "hrm.file.apply")
@At(value = "/hrm/file/apply")
public class ApplyAction extends Action {

	public static Log log = Logs.getLog(ApplyAction.class);
	
	@GET
	@At
	@Ok("ftl:hrm/file/apply_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/hrm/file/apply/node", token);
		CSRF.generate(req, "/hrm/file/apply/del", token);
		CSRF.generate(req, "/hrm/file/approve/actors", token);
	//	CSRF.generate(req, "/hrm/file/apply/addPhoto", token);
		
		String startStr = Https.getStr(req, "startTime", R.CLEAN, R.D);
		String endStr = Https.getStr(req, "endTime", R.CLEAN, R.D);
		Integer approve = Https.getInt(req, "approve", R.CLEAN, R.I);
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
			.and("f.status", "=", Status.ENABLED)
			.and("f.modify_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "f.approved", "approve", approve);
		Cnds.gte(cri, mb, "f.create_time", "startTime", startStr);
		Cnds.lte(cri, mb, "f.create_time", "endTime", endStr);
		cri.getOrderBy().desc("f.modify_time");
		
		Page<File> page = Webs.page(req);
		page = mapper.page(File.class, page, "File.count", "File.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}

	@POST
	@At
	@Ok("json")
	public Object node(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I, "用户ID");
			
			Archive archive = dao.fetch(Archive.class, Cnd.where("userId", "=", userId));
			if (archive == null)
				archive = new Archive();

			mb.put("archive", archive);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:node) error: ", e);
		}

		return mb;
	}
	
	@GET
	@At
	@Ok("ftl:hrm/file/apply_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer fileId = Https.getInt(req, "fileId", R.I);
		Integer userId = Https.getInt(req, "userId", R.I);
		
		File file = null;
		if (fileId != null) {
			file = mapper.fetch(File.class, "File.query", Cnd
					.where("f.modify_id", "=", Context.getUserId())
					.and("f.file_id", "=", fileId));
			if (file != null) {
				// 档案审批
				FileActor actor = dao.fetch(FileActor.class, Cnd
						.where("fileId", "=", fileId)
						.and("variable", "=", Roles.Me.getName())
						.limit(1)
						.asc("modifyTime"));
				if (actor != null) {
					file.setActorId(actor.getActorId());
				}
			}
		}
		if (file == null) {
			file = new File();
			file.setUserId(userId);
		}
		
		List<User> users = dao.query(User.class, Cnd.where("status", "=", Status.ENABLED));

		req.setAttribute("file", file);
		req.setAttribute("users", users);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer fileId = null;
		try {
			CSRF.validate(req);
			
			fileId = Https.getInt(req, "fileId", R.I);

			Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I, "用户ID");
			String trueName = Https.getStr(req, "trueName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "姓名");
			Integer gender = Https.getInt(req, "gender", R.REQUIRED, R.I, R.IN, "in [0,1]", "性别");
			String birthday = Https.getStr(req, "birthday", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "出生年月");
			String place = Https.getStr(req, "place", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "籍贯");
			String nation = Https.getStr(req, "nation", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "民族");
			String health = Https.getStr(req, "health", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "健康状况");
			Integer marry = Https.getInt(req, "marry", R.REQUIRED, R.I, R.IN, "in [0,1]", "婚否");
			String degree = Https.getStr(req, "degree", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "最高学历");
			String major = Https.getStr(req, "major", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "所学专业");
			String school = Https.getStr(req, "school", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "毕业院校");
			String idcard = Https.getStr(req, "idcard", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "身份证");
			String phone = Https.getStr(req, "phone", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "联系电话");
			String entry_yyyyMMdd = Https.getStr(req, "entry_yyyyMMdd", R.REQUIRED, R.D, "入职日期");
			String full_yyyyMMdd = Https.getStr(req, "full_yyyyMMdd", R.REQUIRED, R.D, "转正日期");
			
			Integer actorId = Https.getInt(req, "userId", R.REQUIRED, R.I, "审批人员");
			
			DateTime entryDate = Calendars.parse(entry_yyyyMMdd, Calendars.DATE);
			DateTime fullDate = Calendars.parse(full_yyyyMMdd, Calendars.DATE);
			
			if (entryDate.isAfter(fullDate))
				throw new Errors("入职日期不能大于转正日期");
			
			DateTime now = new DateTime();
			File file = null;
			if (fileId != null) {
				file = dao.fetch(File.class, Cnd
						.where("modifyId", "=", Context.getUserId())
						.and("fileId", "=", fileId));
				Asserts.isNull(file, "档案文件不存在");
				Asserts.notEq(file.getApproved(), Status.PROOFING, "禁止编辑已处理的档案文件");
			} else {
				file = new File();
				file.setCreateTime(now.toDate());
			}

			User user = dao.fetch(User.class, userId);
			
			StringBuilder buff = new StringBuilder();
			buff.append("人事档案_")
				.append(Context.getUsername())
				.append("申请编辑")
				.append(user.getTrueName())
				.append("档案文件");
			
			file.setUserId(userId);
			file.setSubject(buff.toString());
			file.setModifyId(Context.getUserId());
			file.setTrueName(trueName);
			file.setGender(gender);
		//	file.setBirthday(birthday);
			file.setPlace(place);
			file.setNation(nation);
			file.setHealth(health);
			file.setMarry(marry);
			file.setDegree(degree);
			file.setMajor(major);
			file.setSchool(school);
			file.setIdcard(idcard);
			file.setPhone(phone);
			file.setEntryDate(entryDate.toDate());
			file.setFullDate(fullDate.toDate());
			file.setApproved(Status.PROOFING);
			file.setStatus(Status.ENABLED);
			file.setModifyTime(now.toDate());
			
			transSave(fileId, file, actorId);
			
			Code.ok(mb, (fileId == null ? "新建" : "编辑") + "档案文件成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:add) error: ", e);
			Code.error(mb, (fileId == null ? "新建" : "编辑") + "档案文件失败");
		}

		return mb;
	}
/*	@At
	@Ok("ftl:hrm/file/addPhoto")
	public void addpic(){
		
	}
	 @At  
	 @Ok("ftl:hrm/file/addPhoto")  
	 @AdaptBy(type = UploadAdaptor.class, args = { "${app.root}/res/tmp" })  
	 public void addPhoto(@Param("..")NutMap map,HttpServletRequest req){
		 //	CSRF.validate(req);
		 	DateTime now = new DateTime();
		 	Uploads.required(map.get("photoFile"));
			Uploads.max(map.get("photoFile"), 1);
			Uploads.suff(map.get("photoFile"), "gif,jpeg,jpg");
			String str= Uploads.path(map.get("photoFile"), now,null);
			System.out.println(str);
			req.setAttribute("str", str);
	 }*/
	 
	
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
				dao.update(File.class, Chain
						.make("status", Status.DISABLED), Cnd
						.where("status", "=", Status.ENABLED)
						.and("modifyId", "=", Context.getUserId())
						.and("approved", "=", Status.PROOFING)
						.and("fileId", "in", arr));
			}
			Code.ok(mb, "删除档案文件成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除档案文件失败");
		}

		return mb;
	}
	
	private void transSave(final Integer fileId, final File file, final Integer actorId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				Integer id = fileId;
				if (fileId != null) {
					dao.clear(FileActor.class, Cnd.where("fileId", "=", fileId));
					dao.update(file);
				} else {
					id = dao.insert(file).getFileId();
				}
				
				// 指定档案人员审批
				dao.insert(new FileActor(id, actorId, Value.I, Status.PROOFING, Roles.Me.getName(), file.getModifyTime()));
			}
		});
	}
}