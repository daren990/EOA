package cn.oa.web.action.sys.cons;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Cnd;
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
import cn.oa.model.Org;
import cn.oa.model.ShiftCorp;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.web.action.Action;

/**
 * 启用新排班
 * @author 
 */
@IocBean(name = "sys.cons.corp")
@At(value = "/sys/cons/corp")
public class ShiftCorpAction extends Action {

	public static Log log = Logs.getLog(ShiftCorpAction.class);
	@GET
	@At
	@Ok("ftl:sys/cons/shift_corp")
	public void page(HttpServletRequest req) {
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/cons/corp/add", token);
		List<Org> orgs = dao.query(Org.class, Cnd.where("parent_id", "=", 0).and("status","=",Status.ENABLED));
		List<ShiftCorp> shiftCorps = mapper.query(ShiftCorp.class,"ShiftCorp.query",Cnd.where("o.status","=",Status.ENABLED).and("o.parent_id", "=", 0));
		req.setAttribute("orgs", orgs);
		req.setAttribute("shiftCorps", shiftCorps);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			String[] orgs = req.getParameterValues("orgId");
			transSave(orgs);
			Code.ok(mb, "提交公司的排班成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(ShiftAction:add) error: ", e);
			Code.error(mb, "提交公司的排班失败");
		}

		return mb;
	}
	private void transSave(final String[] orgs) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				ShiftCorp shiftCorp = new ShiftCorp();
				dao.clear(ShiftCorp.class);
				if(orgs!=null){
				for(int i=0;i<orgs.length;i++){
					shiftCorp.setCorpId(Integer.parseInt(orgs[i]));
					dao.insert(shiftCorp);
				}
				}
			}
		});
	}

}
