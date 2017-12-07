package cn.oa.web.action.addressBook;


import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;

import cn.oa.consts.Status;
import cn.oa.model.ReportModel;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
@IocBean(name = "addressBook.viewShare")
@At(value = "addressBook/viewShare")
public class ViewShare extends Action{
	
	public static Log log = Logs.getLog(ViewShare.class);
	public static final int PAGE_SIZE = 100000;
	
	@GET
	@At
	@Ok("ftl:addressBook/viewShare")
	public void wxpage(HttpServletRequest req){
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where().and("share", "=", Status.DISABLED);
		cri.getOrderBy().asc("r.create_time");
		req.setAttribute("pageSize", PAGE_SIZE);
		Page<ReportModel> page = Webs.page(req);
		page = mapper.page(ReportModel.class, page, "Report.count", "Report.query", cri);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
		
	}
}
