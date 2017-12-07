package cn.oa.web.action.hrm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.model.Archive;
import cn.oa.utils.Calendars;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.action.Action;

/**
 * 人事档案
 * @author SimonTang
 */
@IocBean()
@At(value = "/hrm/archive")
public class ArchiveAction extends Action {
	
	public static Log log = Logs.getLog(ArchiveAction.class);

	@GET
	@At
	@Ok("ftl:hrm/archive_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);
		Archive archive = mapper.fetch(Archive.class, "Archive.query", Cnd.where("u.user_id", "=", userId));
		req.setAttribute("archive", archive);
	}

	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer userId = null;
		try {
			CSRF.validate(req);
			
			userId = Https.getInt(req, "userId", R.REQUIRED, R.I, "用户ID");
			Integer amount = Https.getInt(req, "amount", R.I, "签约次数");
			Integer gender = Https.getInt(req, "gender", R.REQUIRED, R.I, R.IN, "in [0,1]", "性别");
			String idcard = Https.getStr(req, "idcard", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "身份证");
			Integer onPosition = Https.getInt(req, "onPosition", R.REQUIRED, R.I, R.IN, "in [0,1,3]", "是否在职");
			String place = Https.getStr(req, "place", R.CLEAN, R.RANGE, "{1,20}", "籍贯");
			Integer marry = Https.getInt(req, "marry", R.REQUIRED, R.I, R.IN, "in [0,1]", "婚否");
			String degree = Https.getStr(req, "degree", R.CLEAN,R.RANGE, "{1,20}", "最高学历");
			String major = Https.getStr(req, "major", R.CLEAN,R.RANGE, "{1,20}", "所学专业");
			String school = Https.getStr(req, "school", R.CLEAN, R.RANGE, "{1,20}", "毕业院校");
			String phone = Https.getStr(req, "phone", R.CLEAN,R.RANGE, "{1,20}", "联系电话");
			String address = Https.getStr(req, "address", R.CLEAN,R.RANGE, "{1,200}", "联系地址");
			String QQ = Https.getStr(req, "QQ", R.CLEAN,R.RANGE, "{1,200}", "QQ");
			String exigencyPhone = Https.getStr(req, "exigencyPhone", R.CLEAN,R.RANGE, "{1,200}", "紧急联系人电话");
			String exigencyName = Https.getStr(req, "exigencyName", R.CLEAN,R.RANGE, "{1,20}", "紧急联系人地址");
			String email = Https.getStr(req, "email", R.E, R.RANGE, "{1,64}", "邮箱");
			String position = Https.getStr(req, "position", R.CLEAN,R.RANGE, "{1,20}", "职务");
			String entry_yyyyMMdd = Https.getStr(req, "entry_yyyyMMdd", R.REQUIRED, R.D, "入职日期");
			String full_yyyyMMdd = Https.getStr(req, "full_yyyyMMdd", R.D, "转正日期");
			String contract_start = Https.getStr(req, "contractStart_yyyyMMdd", R.D, "合同开始日期");
			String contract_end = Https.getStr(req, "contractEnd_yyyyMMdd", R.D, "合同结束日期");
			String birthday_yyyyMMdd = Https.getStr(req, "birthday_yyyyMMdd", R.D, "出生年月");
			DateTime entryDate = Calendars.parse(entry_yyyyMMdd, Calendars.DATE);
			DateTime fullDate = Calendars.parse(full_yyyyMMdd, Calendars.DATE);
			DateTime contractStart = Calendars.parse(contract_start, Calendars.DATE);
			DateTime contractEnd = Calendars.parse(contract_end, Calendars.DATE);
			DateTime birthday = Calendars.parse(birthday_yyyyMMdd, Calendars.DATE);
			Integer hukou = Https.getInt(req, "hukou", R.REQUIRED, R.I, R.IN, "in [0,1,2,3]", "户口性质");
			Integer socialSecurity = Https.getInt(req, "socialSecurity", R.REQUIRED, R.I, R.IN, "in [0,1,2,3]", "是否曾经购买社保");

			if (entryDate.isAfter(fullDate))
				throw new Errors("入职日期不能大于转正日期");
			
			boolean exist = true;
			Archive archive = dao.fetch(Archive.class, userId);
			
			if (archive == null) {
				exist = false;
				archive = new Archive();
			}
			
			archive.setQQ(QQ);
			archive.setPosition(position);
			archive.setAddress(address);
			archive.setExigencyPhone(exigencyPhone);
			archive.setExigencyName(exigencyName);
			archive.setEmail(email);
			if(contractStart != null && contractEnd != null){
				archive.setContractStart(contractStart.toDate());
				archive.setContractEnd(contractEnd.toDate());
			}
			archive.setAmount(amount);
			archive.setOnPosition(onPosition);
			archive.setUserId(userId);
			archive.setGender(gender);
			archive.setBirthday(birthday.toDate());
			archive.setPlace(place);
			archive.setMarry(marry);
			archive.setDegree(degree);
			archive.setMajor(major);
			archive.setSchool(school);
			archive.setIdcard(idcard);
			archive.setPhone(phone);
			//在职状态
			archive.setOnPosition(onPosition);
			archive.setEntryDate(entryDate.toDate());
			//户口性质
			archive.setHukou(hukou);
			archive.setSocialSecurity(socialSecurity);
			if(fullDate != null){
				archive.setFullDate(fullDate.toDate());
			}
//			if(onPosition==3)
//				Code.error(mb, "不能修改离职状态");
//			else{
				/*if (Strings.isNotBlank(quit_yyyyMMdd)) 
					archive.setQuitDate(Calendars.parse(quit_yyyyMMdd, Calendars.DATE).toDate());*/
				if (Strings.isNotBlank(full_yyyyMMdd)) archive.setFullDate(Calendars.parse(full_yyyyMMdd, Calendars.DATE).toDate());
				if (Strings.isNotBlank(contract_end)) archive.setContractEnd(Calendars.parse(contract_end, Calendars.DATE).toDate());
				if (Strings.isNotBlank(contract_start)) archive.setContractStart(Calendars.parse(contract_start, Calendars.DATE).toDate());
				if (exist)
					dao.update(archive);
				else
					dao.insert(archive);
		
				Code.ok(mb, "编辑档案成功");
//			}
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Archive:add) error: ", e);
			Code.error(mb, "编辑档案失败");
		}
		return mb;
	}
}
