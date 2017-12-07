package cn.oa.web.action.sys.cons;
import java.util.List;

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
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Status;
import cn.oa.model.Org;
import cn.oa.model.Zkem;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

/**
 * 配置打卡机
 * @qiumingxie 
 */
@IocBean(name = "sys.cons.oaapp")
@At(value = "/sys/cons/oaapp")
public class OaAppAction extends Action {

	public static Log log = Logs.getLog(OaAppAction.class);
	
	private void pageUtil(HttpServletRequest req ,Integer corpId) {
		MapBean mb = new MapBean();
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/cons/oaapp/add", token);
		CSRF.generate(req, "/sys/cons/oaapp/delete", token);
		Criteria cri = Cnd.cri();
		Page<Zkem> page = Webs.page(req);
		page = mapper.page(Zkem.class, page, "Zkem.count", "Zkem.index", cri);
		req.setAttribute("mb", mb);
		req.setAttribute("page", page);
	}
	
	@GET
	@At
	@Ok("ftl:sys/cons/oaapp_page")
	public void page(HttpServletRequest req) {
		pageUtil(req, null);
	}
	
	@GET
	@At
	@Ok("ftl:sys/cons/oaapp_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		MapBean mb = new MapBean();
		Integer zkemId = Https.getInt(req, "zkemId", R.I);
		Zkem zkem = null;
		if (zkemId!=null) {
			zkem = dao.fetch(Zkem.class,zkemId);
		}else{
			zkem = new Zkem();
		}
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		req.setAttribute("corps", corps);
		req.setAttribute("mb", mb);
		req.setAttribute("zkem", zkem);
	}
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		CSRF.generate(req);
		MapBean mb = new MapBean();
		Integer zkemId = null;
		try {
			zkemId = Https.getInt(req, "zkemId", R.I,"机器ID");
			String address = Https.getStr(req, "address", R.REQUIRED,R.CLEAN,"IP地址");
			Integer port = Https.getInt(req, "port",R.REQUIRED ,R.I,"端口");
			Integer number = Https.getInt(req, "number",R.REQUIRED ,R.I,"机器号");
			Integer[] arr = Converts.array(address, "\\.");
			if(arr == null){
				throw new Errors("IP地址格式不正确");
			}
			if(arr.length != 4){
				throw new Errors("IP地址格式不正确");
			}
			Zkem zkem = new Zkem();
			zkem.setAddress(address);
			zkem.setNumber(number);
			zkem.setPort(port);
			transSave(zkemId,zkem);
			
			Code.ok(mb, (zkemId == null ? "新建" : "编辑") + "打卡机成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("oaAppAction:add) error: ", e);
			Code.error(mb, (zkemId == null ? "新建" : "编辑") + "打卡机失败");
		}

		return mb;
	}
	
	@POST
	@At
	@Ok("json")
	public Object delete(HttpServletRequest req, HttpServletResponse res) {
		CSRF.generate(req);
		MapBean mb = new MapBean();
		try {
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");

			if (arr != null && arr.length > 0) {
				transDelete(arr);
			}else{
				throw new Errors("没有找到该打卡机");
			}
			Code.ok(mb, "删除打卡机成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("oaAppAction:add) error: ", e);
			Code.error(mb, "删除打卡机失败");
		}
		return mb;
	}
	private void transDelete(final Integer[] arr) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.clear(Zkem.class, Cnd.where("zkem_id", "in", arr));
			}
		});
	}
	
	private void transSave(final Integer zkemId, final Zkem zkem) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if(zkemId != null){
					zkem.setZkemId(zkemId);
					dao.update(zkem);
				}else{
					dao.insert(zkem);
				}
			}
		});
	}
}
