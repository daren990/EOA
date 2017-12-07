package cn.oa.web.action.sys.cons;

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

import cn.oa.consts.Status;
import cn.oa.model.Org;
import cn.oa.model.Shift;
import cn.oa.model.ShiftClass;
import cn.oa.utils.Calendars;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

/**
 * 班次设置
 * @author SimonTang
 */
@IocBean
@At("/sys/cons/classes")
public class ShiftClassesAction extends Action {
	
	public static Log log = Logs.getLog(ShiftClassesAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/cons/day_classes")
	public void page(HttpServletRequest req) {
		//获取公司名称
		Org org = dao.fetch(Org.class,Context.getCorpId());
		
		String[] roles = Context.getRoles();
		for(String role : roles){
			// 管理员、排班管理员角色可以设置所有公司的班次
			if("admin".equals(role) || "shift.manager".equals(role)){
				List<ShiftClass> shiftClasses = dao.query(ShiftClass.class, null);
				req.setAttribute("corp", org.getOrgName());
				req.setAttribute("shiftClasses", shiftClasses);
				return ;
			}
		}
		Criteria cri = Cnd.cri();
		cri.where().and("corp_id", "=", Context.getCorpId());
		cri.getOrderBy().desc("modify_time");
		List<ShiftClass> shiftClasses = dao.query(ShiftClass.class, cri);
		
		req.setAttribute("corp", org.getOrgName());
		req.setAttribute("shiftClasses", shiftClasses);
	}
	
	@POST
	@At
	@Ok("json")
	public Object select(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			/*CSRF.validate(req);*/
			String[] classId = req.getParameterValues("classId");

			transSave(classId);
			/*dao.delete(ShiftClass.class,classId);*/
			Code.ok(mb, "选定班次成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "选定班次失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		/*ShiftClass shiftClass = null;*/
		Integer classId = null;
		try {
			/*CSRF.validate(req);*/
			classId = Https.getInt(req, "classId", R.I, "班次ID");
			String className = Https.getStr(req, "className", R.REQUIRED, "班次名称");
			String color = Https.getStr(req, "color", R.REQUIRED, "颜色配置"); 
			//1=日班,0=夜班
			Integer night = Https.getInt(req, "night", R.REQUIRED, R.I, R.IN, "in [0,1]", "日班与夜班配置");
			/*Integer second = Https.getInt(req, "second");
			if(second!=null)*/
			Integer second = 0;
			/*float sumTime = Https.getFloat(req, "sumTime", R.REQUIRED, R.F, "总时长");*/
			float sumTime = 0;
			String firstM = Https.getStr(req, "firstM", R.CLEAN, R.T, R.REQUIRED, "一头班早班时间");
			String firstN = Https.getStr(req, "firstN", R.CLEAN, R.T, R.REQUIRED, "一头班晚班时间");
			DateTime fm = Calendars.parse("2015-2-6 "+firstM, Calendars.DATE_TIME);
			DateTime fn = Calendars.parse("2015-2-6 "+firstN, Calendars.DATE_TIME);
			String secondM = null;
			String secondN = null;
			DateTime now = new DateTime();	
			float sumTimesFloat = 0;
			String t = null;
			if(night == 1){
				second = Https.getInt(req, "second", R.I, "二头班");
				if(second == null)second=0;
				if(fm.isAfter(fn))
					throw new Errors("班次时间段1(上班时间不能大于下班时间)");
				sumTimesFloat = fn.toDate().getTime()-fm.toDate().getTime();
			//如果为二头班
				if(second == 1){
					secondM = Https.getStr(req, "secondM", R.CLEAN, R.T, R.REQUIRED, "二头班早班时间");
					secondN = Https.getStr(req, "secondN", R.CLEAN, R.T, R.REQUIRED, "二头班晚班时间");
					DateTime sm = Calendars.parse("2015-2-6 "+secondM, Calendars.DATE_TIME);
					DateTime sn = Calendars.parse("2015-2-6 "+secondN, Calendars.DATE_TIME);
					if(sm.isBefore(fn))
						throw new Errors("班次时间段2开始时间不能小于时间段1结束时间");
					if(sm.isAfter(sn))
						throw new Errors("班次时间段2(上班时间不能大于下班时间)");
					sumTimesFloat += sn.toDate().getTime()-sm.toDate().getTime();
				}
				/*sumTime = sumTimesFloat/(60*60*1000);*/
			}else{
				sumTimesFloat = fn.plusDays(1).toDate().getTime()-fm.toDate().getTime();
				/*sumTime = sumTimesFloat/(60*60*1000)+24;*/
			}
			sumTime = sumTimesFloat/(60*60*1000);
			if(classId==null){
				ShiftClass shiftClass = new ShiftClass();
				shiftClass.setColor(color);
				shiftClass.setSecond(second);
				shiftClass.setClassName(className);
				shiftClass.setCorpId(Context.getCorpId());
			/*	shiftClass.setClassType(classType);*/
				shiftClass.setSumTime(sumTime);
				shiftClass.setFirstMorning(firstM);
				shiftClass.setFirstNight(firstN);
				shiftClass.setSecondMorning(secondM);
				shiftClass.setSecondNight(secondN);
				shiftClass.setCreateTime(now.toDate());
				shiftClass.setModifyId(Context.getUserId());
				shiftClass.setModifyTime(now.toDate());
				shiftClass.setStatus(0);
				shiftClass.setNight(night);
				dao.insert(shiftClass);
			}else{
				List<Shift> shifts = dao.query(Shift.class, Cnd.where("classes", "=", classId));
				if(shifts.size() > 0){
					ShiftClass sClass = dao.fetch(ShiftClass.class, classId);
					if(sClass == null)throw new Errors("该班次不存在");
					//已用于排班的班次只允许修改颜色和名称
					t = "该班次已用于排班,只能修改名称和颜色";
					if(!color.equals(sClass.getColor())||!className.equals(sClass.getClassName())){
						dao.update(ShiftClass.class, Chain
								.make("class_name", className)							
								.add("color", color)								
								.add("modify_time", now.toString("yyyy-MM-dd hh:mm:ss"))
								.add("modify_id", Context.getUserId()), Cnd
								.where("class_id", "=", classId));
						//t = "该班次已用于排班,只能修改名称和颜色";
					}else{
						throw new Errors(t);}
				}else{
				dao.update(ShiftClass.class, Chain
					.make("class_name", className)
					.add("is_second", second)
					/*.add("class_type", classType)*/
					.add("sum_time", sumTime)
					.add("color", color)
					.add("first_m", firstM)
					.add("first_n", firstN)
					.add("second_m", secondM)
					.add("second_n", secondN)
					.add("modify_time", now.toString("yyyy-MM-dd hh:mm:ss"))
					.add("modify_id", Context.getUserId()), Cnd
					.where("class_id", "=", classId));
				}
			}
			Code.ok(mb, (classId==null?"添加":"编辑")+"班次成功"+(t==null?"":","+t));
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, (classId==null?"添加":"编辑")+"班次失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object del(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			/*CSRF.validate(req);*/
			Integer classId = Https.getInt(req, "classId", R.REQUIRED, R.I, "班次ID");
			//dao.delete(ShiftClass.class,classId);
			List<Shift> shifts = dao.query(Shift.class, Cnd.where("classes", "=", classId));
			if(shifts.size() > 0){
				throw new Errors("该班次已用于排班,无法删除");
			}
			transDelete(classId);
			Code.ok(mb, "删除班次成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Apply:del) error: ", e);
			Code.error(mb, "删除班次失败");
		}

		return mb;
	}
	
	private void transSave(final String[] classId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.update(ShiftClass.class, Chain.make("status", Status.DISABLED), Cnd.where("corp_id", "=", Context.getCorpId()));
				if(classId!=null){
					for(int i=0;i<classId.length;i++){
						dao.update(ShiftClass.class, Chain.make("status", Status.ENABLED), Cnd.where("class_id", "=", classId[i]));
					}
				}
			}
		});
	}
	
	private void transDelete(final Integer classId) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.delete(ShiftClass.class,classId);
				dao.clear(Shift.class,Cnd.where("classes", "=", classId));
			}
		});
	}
}
