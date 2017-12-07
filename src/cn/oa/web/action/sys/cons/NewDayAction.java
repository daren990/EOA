package cn.oa.web.action.sys.cons;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.model.Meet;
import cn.oa.model.Org;
import cn.oa.model.WorkDay;
import cn.oa.utils.Asserts;
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
import cn.oa.web.action.Action;

@Filters
@IocBean(name = "sys.cons.day")
@At(value = "/sys/cons/day")
public class NewDayAction extends Action {

	public static Log log = Logs.getLog(NewDayAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/cons/day_page")
	public void page(HttpServletRequest req) {
		Criteria cri = Cnd.cri();
		cri.where().and("m.status", "=", Status.ENABLED);
		Page<Meet> page = Webs.page(req);
		page = mapper.page(Meet.class, page, "Meet.count", "Meet.index", cri);
	}	
	@GET
	@At
	@Ok("ftl:sys/cons/day_classes")
	public void classes(HttpServletRequest req) {

	}	
}
