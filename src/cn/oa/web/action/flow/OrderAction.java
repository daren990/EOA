package cn.oa.web.action.flow;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;

import cn.oa.utils.SnakerHelper;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;

@IocBean
@At(value = "/flow/order")
public class OrderAction {

	@GET
	@At
	@Ok("ftl:flow/order_page")
	public void page(HttpServletRequest req) {
		Page<HistoryOrder> page = Webs.page(req);

		org.snaker.engine.access.Page<HistoryOrder> orderPage = new org.snaker.engine.access.Page<HistoryOrder>(page.getPageNo());
		orderPage.setPageNo(page.getPageNo());
		orderPage.setPageSize(page.getPageSize());
		
		SnakerHelper.getEngine().query().getHistoryOrders(orderPage,  new QueryFilter());
		
		page.setResult(orderPage.getResult());
		page.setTotalItems(orderPage.getTotalCount());
		
		req.setAttribute("page", page);
	}
}
