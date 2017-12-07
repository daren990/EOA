
package cn.oa.web.action.wxTI;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.model.ShopProduct;
import cn.oa.model.vo.WxShopProductVO;
import cn.oa.utils.MapBean;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;
import cn.oa.web.filter.ClientAutoLogin;
import cn.oa.web.view.FreemarkerView;

@Filters
@IocBean(name = "wx.product")
@At(value = "/wx/product")
public class WeiXinProductAction extends Action {
	
	private static final Log log = Logs.getLog(WeiXinProductAction.class);
	
	@GET
	@At
	@Filters({@By(type = ClientAutoLogin.class)})
	public View list(HttpServletRequest req, String org_id,String code,String openid) throws Exception {
		req.setAttribute("org_id", org_id);
		return new FreemarkerView("wxTI/wechat_productList");
	}
	
	@GET
	@POST
	@At
	@Ok("json")
	public Object getData(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		try {
			SimpleCriteria cri = Cnd.cri();
			cri.desc("id");
			
			Integer orgId = Https.getInt(req, "org_id", R.REQUIRED, R.I, "机构id");
			cri.where().and("corp_id","=",orgId).and("onSale","=",1).and("saleType","=","1");
			
			String seacherKey = Https.getStr(req, "seacherKey");
			if(seacherKey!=null && !seacherKey.equals("")){
				cri.where().and(new Static(" name like '%"+seacherKey+"%' "));
			}
			Page<ShopProduct> page = Webs.page_jm(req);
			page = mapper.page(ShopProduct.class, page, "Product.count", "Product.index",cri);
			List<WxShopProductVO> voList = wxClientService.getShopProductVo(page);
			mb.put("voList", voList);
			Code.ok(mb, "");
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
}
