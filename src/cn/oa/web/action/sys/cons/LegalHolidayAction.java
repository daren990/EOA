package cn.oa.web.action.sys.cons;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
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
import cn.oa.model.ShiftHoliday;
import cn.oa.model.User;
import cn.oa.utils.Calendars;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "sys.cons.LegalHoliday")
@At(value = "/sys/cons/LegalHoliday")
public class LegalHolidayAction  extends Action {
	public static Log log = Logs.getLog(LegalHolidayAction.class);
	@GET
	@At
	@Ok("ftl:sys/cons/legalHoliday")
	public void scheduling(HttpServletRequest req){
		Integer yearInt = Https.getInt(req, "dataValue",R.I, "法定日期");
		String year = null;
		if(yearInt == null){
			year = new DateTime().toString("yyyy");}
		else {
			year = String.valueOf(yearInt);
		}
		String yearStart = year+"-01-01";
		String yearEnd = year+"-12-31";
		Criteria cri = Cnd.cri();
		cri.where()
			.and("holiday", ">=", yearStart)
			.and("holiday", "<=", yearEnd);
		cri.getOrderBy().asc("holiday");
		List<String> strings = new ArrayList<String>();
		List<ShiftHoliday> shiftHolidays = dao.query(ShiftHoliday.class, cri);
		for(ShiftHoliday s:shiftHolidays){
			s.setMonth(Calendars.str(s.getHoliday(), "MM"));
			s.setDay(Calendars.str(s.getHoliday(), "dd"));
			strings.add(s.getMonth()+"-"+s.getDay()+"!"+s.getRemarks()+",");
		}
		req.setAttribute("shiftHolidays", shiftHolidays);
		req.setAttribute("strings", strings);
		req.setAttribute("year", year);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String strDate = Https.getStr(req, "dataValue");
			Integer year = Https.getInt(req, "year",R.REQUIRED, R.I,"年份");
			String yearStart = String.valueOf(year)+"-01-01";
			String yearEnd = String.valueOf(year)+"-12-31";
			//Set<ShiftHoliday> shiftHolidays = new HashSet <ShiftHoliday> ();
			String[] oneDay=strDate.split(",");
			Date day = null;
			Map<Date, ShiftHoliday> dayMap = new ConcurrentHashMap<Date, ShiftHoliday>();
			
			//防止重复值,用dayMap覆盖原来的值
			for(int i=0;i<oneDay.length;i++){
				ShiftHoliday shiftHoliday = new ShiftHoliday();
				String[] dayRemark = oneDay[i].split("!");
				if(dayRemark.length!=2)continue;
				shiftHoliday.setRemarks(dayRemark[1]);
				day =  Calendars.parse(year+"-"+dayRemark[0], Calendars.DATE).toDate();				
				shiftHoliday.setHoliday(day);
				/*if(dayMap.containsKey(day)){
					
				}else{
					dayMap.put(day, shiftHoliday);
					shiftHolidays.add(shiftHoliday);
				}*/
				dayMap.put(day, shiftHoliday);
				//shiftHolidays.add(shiftHoliday);
			}
			transSave(dayMap,yearStart,yearEnd);
			Code.ok(mb, "编辑法定节假日成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(LegalHoliday:add) error: ", e);
			Code.error(mb, "编辑法定节假日失败");
		}

		return mb;
		
	}
	private void transSave(final Map<Date, ShiftHoliday> dayMap,final String yearStart,final String yearEnd) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.clear(ShiftHoliday.class,Cnd.where("holiday", ">=", yearStart).and("holiday","<=",yearEnd));
				for (Entry<Date, ShiftHoliday> entry : dayMap.entrySet()) {
					 dao.insert(entry.getValue());
				}
			}

		});
		}
}
