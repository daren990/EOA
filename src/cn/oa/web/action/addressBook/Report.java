package cn.oa.web.action.addressBook;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import cn.oa.consts.Cache;
import cn.oa.model.AnnualRole;
import cn.oa.model.ReportModel;
import cn.oa.model.User;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
@IocBean(name = "addressBook.report")
@At(value = "addressBook/report")
public class Report extends Action{
	
	public static Log log = Logs.getLog(Report.class);
	public static final int PAGE_SIZE = 100000;
	
	@GET
	@At
	@Ok("ftl:addressBook/report")
	public void wxpage(HttpServletRequest req){
		CSRF.generate(req);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Cnds.eq(cri, mb, "r.user_id", "userId", Context.getUserId());
		cri.getOrderBy().asc("r.create_time");
		req.setAttribute("pageSize", PAGE_SIZE);
		Page<ReportModel> page = Webs.page(req);
		page = mapper.page(ReportModel.class, page, "Report.count", "Report.query", cri);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			Integer reportId = Https.getInt(req, "reportId", R.I);
			Integer share = Https.getInt(req, "share", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			dao.update(ReportModel.class, Chain.make("share", share), Cnd.where("reportId", "=", reportId));
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
		}

		return mb;
	}
}
