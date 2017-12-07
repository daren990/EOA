package cn.oa.web.action.adm.salary;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import cn.oa.model.Annual;
import cn.oa.model.Dict;
import cn.oa.model.Leave;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

/**
 * 年休假
 * @author SimonTang
 */
@IocBean(name = "adm.salary.annual")
@At(value = "/adm/salary/annual")
public class AnnualAction extends Action {

	public static Log log = Logs.getLog(AnnualAction.class);

	@GET
	@At
	@Ok("ftl:adm/salary/annual_page")
	public void page(HttpServletRequest req) {
		String startStr = Https.getStr(req, "startTime", R.D);
		String endStr = Https.getStr(req, "endTime", R.D);

		Integer typeId = null;
		Map<String, String> leaveMap = dictService.map(Dict.LEAVE);
		for (Entry<String, String> entry : leaveMap.entrySet()) {
			if (entry.getValue().equals("年休假")) {
				typeId = Values.getInt(entry.getKey(), null);
				break;
			}
		}
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where()
				.and("l.status", "=", Status.ENABLED)
				.and("l.user_id", "=", Context.getUserId());
		Cnds.eq(cri, mb, "l.type_id", "typeId", typeId);
		Cnds.gte(cri, mb, "l.start_time", "startTime", startStr);
		Cnds.lte(cri, mb, "l.end_time", "endTime", endStr);
		cri.getOrderBy().desc("l.modify_time");

		Page<Leave> page = Webs.page(req);
		page = mapper.page(Leave.class, page, "Leave.count", "Leave.index", cri);

		List<Annual> annuals = mapper.query(Annual.class, "Annual.query", Cnd
				.where("a.user_id", "=", Context.getUserId()));
		for (Annual annual : annuals) {
			int lastMinute = leaveService.lastAnnualMinute(Context.getUserId(), null, null, null, leaveMap);
			annual.setLastMinute(lastMinute);
		}

		req.setAttribute("page", page);
		req.setAttribute("mb", mb);

		req.setAttribute("leaveMap", leaveMap);
		req.setAttribute("annuals", annuals);
		/*req.setAttribute("works", works);*/
	}
}
