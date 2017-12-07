package cn.oa.web.action.edu.course.conf;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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

import cn.oa.consts.Cache;
import cn.oa.consts.Path;
import cn.oa.model.CompanyCheckInfo;
import cn.oa.model.Org;
import cn.oa.model.ShopCompanyWechatConfig;
import cn.oa.utils.Asserts;
import cn.oa.utils.DesUtils;
import cn.oa.utils.MapBean;
import cn.oa.utils.QRCode.TwoDimensionCode;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.PathUtil;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/edu/course/conf/checkConfig")
public class CheckConfigAction extends Action{
	public static Log log = Logs.getLog(CheckConfigAction.class);
	
	@At
	@Ok("ftl:edu/course/conf/checkConfig")
	public void add(HttpServletRequest req) {

		Integer orgId = Context.getCorpId();
		
		CompanyCheckInfo checkInfo = new CompanyCheckInfo();
		
		CompanyCheckInfo existCheckInfo = coopCorpService.findByOrgId(orgId);

		if(existCheckInfo != null){
			checkInfo = existCheckInfo;
		}
		
		req.setAttribute("checkInfo", checkInfo);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer id = null;
		try {
			Integer orgId = Context.getCorpId();
			id = Https.getInt(req, "id");
			String secret = Https.getStr(req, "secret",R.REQUIRED);
			String ip = Https.getStr(req, "ip", R.REQUIRED);
			if(id == null){
				CompanyCheckInfo info = new CompanyCheckInfo();
				info.setIp(ip);
				info.setOrgId(orgId);
				info.setSecret(secret);
				dao.insert(info);
			}else{
				CompanyCheckInfo info = dao.fetch(CompanyCheckInfo.class, Cnd.where("id", "=", id));
				info.setIp(ip);
				info.setSecret(secret);
				dao.updateIgnoreNull(info);
			}
			Code.ok(mb, (id == null ? "新建" : "编辑") + "打卡机配置成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Cooperation:add) error: ", e);
			Code.error(mb, (id == null ? "新建" : "编辑") + "打卡机配置失败");
		}
		return mb;
	}



}
