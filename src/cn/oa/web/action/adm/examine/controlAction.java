package cn.oa.web.action.adm.examine;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.consts.Value;
import cn.oa.model.Perform;
import cn.oa.model.PerformActor;
import cn.oa.model.PerformModel;
import cn.oa.model.Release;
import cn.oa.model.Target;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Cnds;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.examine.control")
@At(value = "adm/examine/control")
public class controlAction extends Action{
	public static Log log = Logs.getLog(controlAction.class);
	
	@GET
	@At
	@Ok("ftl:adm/examine/control_page")
	public void page(HttpServletRequest req){
		String token = CSRF.generate(req);
		CSRF.generate(req, "/adm/examine/control/actors", token);
		CSRF.generate(req, "/adm/examine/perform/apply/nodes", token);
		MapBean mb = new MapBean();
		List<Release>  release = mapper.query(Release.class, "Release.query", Cnd.where("r.corp_id", "=", Context.getCorpId()));
	
		Integer releaseId = Https.getInt(req, "releaseId", R.CLEAN, R.I);

		Criteria cri2 = Cnd.cri();
		cri2.where().and("r.corp_id", "=", Context.getCorpId())
					.and("p.user_id", "=", Context.getUserId())
					.and("p.status", "=", 1)
					;
		Cnds.eq(cri2, mb, "p.release_id", "releaseId", releaseId);
		
		Criteria cri = Cnd.cri();
		cri.where().and("r.corp_id", "=", Context.getCorpId()).and("p.status", "=", 1);
		Cnds.eq(cri, mb, "p.release_id", "releaseId", releaseId);
		
		Page<Perform> page = Webs.page(req);
		
		if(Asserts.hasAny(Roles.PFM.getName(), Context.getRoles())){
		page = mapper.page(Perform.class, page, "Perform.count", "Perform.index", cri);
		}else
			page = mapper.page(Perform.class, page, "Perform.count", "Perform.index", cri2);
		
		
		calculate(page.getResult());
		
		if(release == null)
			release = new ArrayList<Release>();
		req.setAttribute("release", release);
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
		
	}
	
	@POST
	@At
	@Ok("json")
	public Object actors(HttpServletRequest req) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			Integer performId = Https.getInt(req, "performId", R.REQUIRED, R.I, "绩效ID");
			List<PerformActor> actors = mapper.query(PerformActor.class, "PerformActor.query", Cnd
					.where("a.perform_id", "=", performId)
					.and("a.approve", "!=", Status.OK)
					.asc("a.modify_time"));
			mb.put("actors", actors);
			Code.ok(mb, "");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Approve:actors) error: ", e);
		}

		return mb;
	}
	
	private void calculate(List<Perform> result){
		
		
		List<Target> targets = null;
		for(Perform perform : result){
			Double score = 0.0;
			Integer modelId = perform.getModelId();
			PerformModel model = dao.fetch(PerformModel.class, modelId);
			Double firstStep = Values.getDouble(model.getFirstStep())/100; 
			Double secondStep = Values.getDouble(model.getSecondStep())/100; 
			targets = dao.query(Target.class, Cnd.where("performId", "=", perform.getPerformId()));
			
			for(Target target : targets){
				
				Double firstScore = Values.getDouble(target.getScore());
				Double secondScore = Values.getDouble(target.getManscore());
				Double weight = Values.getDouble(target.getWeight())/100;
				score +=(firstStep*weight*firstScore + secondStep*weight*secondScore);
			}
			perform.setScore(score);
		}
	}
	
}
