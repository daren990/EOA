package cn.oa.web.action.hrm;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Mail;
import cn.oa.mail.pojo.MailStart;
import cn.oa.model.Archive;
import cn.oa.model.User;
import cn.oa.utils.MailC;
import cn.oa.utils.MapBean;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Code;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean()
@At(value = "/hrm/profile")
public class ProfileAction extends Action {

	public static Log log = Logs.getLog(ProfileAction.class);
	
	@GET
	@At
	@Ok("ftl:hrm/profile_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		User user = dao.fetch(User.class, Context.getUserId());
		Archive archive = dao.fetch(Archive.class, Context.getUserId());
		if (archive == null)
			archive = new Archive();
		req.setAttribute("user", user);
		req.setAttribute("archive", archive);
	}

	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String email = Https.getStr(req, "email", R.REQUIRED, R.E, R.RANGE, "{1,64}", "邮箱");
			String phone = Https.getStr(req, "phone", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "联系电话");
		//	Integer marry = Https.getInt(req, "marry", R.REQUIRED, R.I, R.IN, "in [0,1]", "婚否");
			
			boolean exist = true;
			User user = dao.fetch(User.class, Context.getUserId());
			Archive archive = dao.fetch(Archive.class, Context.getUserId());

			if (archive == null) {
				exist = false;
				archive = new Archive();
			}
			
			user.setEmail(email);
			archive.setUserId(Context.getUserId());
			archive.setPhone(phone);
		//	archive.setMarry(marry);
			//发送邮件验证邮箱
			List<String> maiList = new ArrayList<String>();
			String mailR = null;
			//User u = dao.fetch(User.class,reimburse.getUserId());
			if(MailC.checkEmail(email)){
				maiList.add(email);
			}else{
				throw new Errors("邮箱不是合法的邮箱格式!");
			}
			String subject = "OA邮箱信息核对";
			String content = "你的个人信息填写完成,请到OA上进行核对";
			MailStart mail = new MailStart();
			mailR = mail.mail(maiList,subject,content);	
			if(Mail.ERROR.equals(mailR)){
				throw new Errors("邮箱验证失败!");
			}
			transSave(exist, user, archive);

			Code.ok(mb, "编辑个人资料成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Profile:add) error: ", e);
			Code.error(mb, "编辑个人资料失败");
		}

		return mb;
	}

	private void transSave(final boolean exist, final User user, final Archive archive) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.update(user);
				if (exist)
					dao.update(archive);
				else
					dao.insert(archive);
			}
		});
	}
}
