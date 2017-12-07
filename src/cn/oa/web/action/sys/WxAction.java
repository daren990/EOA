package cn.oa.web.action.sys;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Status;
import cn.oa.model.Wxnotice;

import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/sys/wx")
public class WxAction extends Action {

	public static Log log = Logs.getLog(WxAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/wx_page")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/wx/able", token);
		Wxnotice wxnotice = null;
		wxnotice = dao.fetch(Wxnotice.class,"leave");
		if(wxnotice==null){
			wxnotice = new Wxnotice("leave","请假",Status.DISABLED);			
			dao.insert(wxnotice);
			wxnotice = null;
		}
		
		wxnotice = dao.fetch(Wxnotice.class,"errand");
		if(wxnotice==null){
			wxnotice = new Wxnotice("errand","出差",Status.DISABLED);			
			dao.insert(wxnotice);
			wxnotice = null;
		}
		
		wxnotice = dao.fetch(Wxnotice.class,"outwork");
		if(wxnotice==null){
			wxnotice = new Wxnotice("outwork","外勤",Status.DISABLED);			
			dao.insert(wxnotice);
			wxnotice = null;
		}
		
		wxnotice = dao.fetch(Wxnotice.class,"overtime");
		if(wxnotice==null){
			wxnotice = new Wxnotice("overtime","加班",Status.DISABLED);			
			dao.insert(wxnotice);
			wxnotice = null;
		}
		
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();		
		Page<Wxnotice> page = Webs.page(req);
		page = mapper.page(Wxnotice.class, page, "Wxnotice.count", "Wxnotice.index", cri);

		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, "checkedIds");
			String[] arr = checkedIds.split(",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			Wxnotice wxnotice = dao.fetch(Wxnotice.class,arr[0]);
			if(wxnotice == null)throw new Errors("该选项不存在");
			wxnotice.setStatus(status);
			dao.update(wxnotice);

			Code.ok(mb, "设置状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Wxaction:able) error: ", e);
			Code.error(mb, "设置状态失败");
		}

		return mb;
	}
}
