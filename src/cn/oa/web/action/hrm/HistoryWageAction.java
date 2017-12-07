package cn.oa.web.action.hrm;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;

import cn.oa.model.HistoryWage;
import cn.oa.utils.MapBean;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean(name = "hrm.historyWage")
@At(value = "/hrm/historyWage")
public class HistoryWageAction  extends Action{
	
	public static Log log = Logs.getLog(HistoryWageAction.class);

	@GET
	@At
	@Ok("ftl:hrm/historyWage_page")
	public void page(HttpServletRequest req) {
		Integer userId = Https.getInt(req, "userId", R.REQUIRED, R.I);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where().and("w.user_id", "=", userId);
		cri.getOrderBy().desc("u.modify_time");

		Page<HistoryWage> page = Webs.page(req);
		page = mapper.page(HistoryWage.class, page, "HistoryWage.count", "HistoryWage.index", cri);

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
}
