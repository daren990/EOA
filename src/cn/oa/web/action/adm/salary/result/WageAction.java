package cn.oa.web.action.adm.salary.result;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;

import cn.oa.consts.Suffix;
import cn.oa.model.WageResult;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.encoder.Encodes;
import cn.oa.utils.helper.Wages;
import cn.oa.utils.lang.Data;
import cn.oa.utils.lang.Excels;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.salary.result.wage")
@At(value = "/adm/salary/result/wage")
public class WageAction extends Action {

	public static Log log = Logs.getLog(WageAction.class);

	@GET
	@At
	@Ok("ftl:adm/salary/result/wage_page")
	public void page(HttpServletRequest req) {
		DateTime now = new DateTime();
		MapBean mb = new MapBean();
		
		String year = Values.getStr(Https.getStr(req, "year", R.yyyy), String.valueOf(now.getYear()));
		String month = Values.getStr(Https.getStr(req, "month", R.MM), now.plusMonths(-1).toString("MM"));
		
		Criteria cri = Cnd.cri();
		cri.where()
			.and("w.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "w.result_month", "resultMonth", year + month);
		cri.getOrderBy().desc("w.result_month");
		
		Page<WageResult> page = Webs.page(req);
		page = mapper.page(WageResult.class, page, "WageResult.count", "WageResult.index", cri);

		mb.put("year", year);
		mb.put("month", month);
		mb.put("years", now.getYear());
		mb.put("months", now.plusMonths(-1).toString("MM"));
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@GET
	@At
	public void download(HttpServletRequest req, HttpServletResponse res) {
		try {
			DateTime now = new DateTime();
			MapBean mb = new MapBean();
			
			String year = Values.getStr(Https.getStr(req, "year", R.yyyy), String.valueOf(now.getYear()));
			String month = Values.getStr(Https.getStr(req, "month", R.MM), now.plusMonths(-1).toString("MM"));
			
			Criteria cri = Cnd.cri();
			cri.where()
				.and("w.user_id", "=", Context.getUserId());
			Cnds.eq(cri, mb, "w.result_month", "resultMonth", year + month);
			cri.getOrderBy().desc("w.result_month");
			
			List<WageResult> results = mapper.query(WageResult.class, "WageResult.query", cri);
			
			ServletOutputStream output = Webs.output(req, res, Encodes.urlEncode("工资条." + Suffix.XLSX));
			Excels excels = new Excels(Suffix.XLSX);
			List<List<Data>> rowList = new ArrayList<List<Data>>();
			rowList.add(Wages.subjects());
			for (WageResult e : results) {
				rowList.add(Wages.cells(views, e));
			}
			
			excels.write(output, rowList, "工资条");
		} catch (Exception e) {
			log.error("(Wage:download) error: ", e);
		}
	}
}
