package cn.oa.web.action.adm.examine;

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

import cn.oa.consts.Roles;
import cn.oa.consts.Status;
import cn.oa.model.Org;
import cn.oa.model.PerformModel;
import cn.oa.model.User;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "adm.examine.model")
@At(value = "/adm/examine/model")
public class ModelAction extends Action{
	public static Log log = Logs.getLog(ModelAction.class);
	
	@GET
	@At
	@Ok("ftl:adm/examine/model_page")
	public void page(HttpServletRequest req){
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		cri.where().and("m.corp_id", "=", Context.getCorpId());
		Page<PerformModel> page = Webs.page(req);
		page = mapper.page(PerformModel.class, page, "PerformModel.count", "PerformModel.index", cri);
		
		for(PerformModel model: page.getResult()){
			Integer userIds[] = Converts.arrayTonumber(model.getModelUsers(), ",");
			List<User> list = mapper.query(User.class, "User.operator", Cnd.where("u.user_id", "in", userIds));
			model.setUsers(list);
		}
		
		req.setAttribute("page", page);
		req.setAttribute("mb", mb);
	}
	@GET
	@At
	@Ok("ftl:adm/examine/model_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer modelId = Https.getInt(req, "modelId", R.REQUIRED, R.I);
		PerformModel model = null;
		Criteria criteria = Cnd.cri();
		if(modelId != null){
			model = mapper.fetch(PerformModel.class, "PerformModel.query", Cnd.where("m.model_id", "=", modelId));
		}
		if(model == null){
			model = new PerformModel();
			String orgName =  dao.fetch(Org.class, Context.getCorpId()).getOrgName();
			model.setOrgName(orgName);
		}
		
		if(modelId != null){
			Integer userIds[] = Converts.arrayTonumber(model.getModelUsers(), ",");
			criteria.where().and("u.user_id", "in", userIds);
			List<User> list = mapper.query(User.class, "User.operator", criteria);
			model.setUsers(list);
		}
		
		List<User> users = dao.query(User.class, Cnd.where("corpId", "=", Context.getCorpId()).and("status", "=", Status.ENABLED));
		String[] roles = new String[]{Roles.SVI.getName(), Roles.MAN.getName(), Roles.GM.getName()};
		users.addAll(userService.operators(roles));
		req.setAttribute("users", users);
		req.setAttribute("model", model);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer modelId = null;
		try {
			CSRF.validate(req);
			
			modelId = Https.getInt(req, "modelId", R.I);
			String modelName = Https.getStr(req, "modelName", R.CLEAN, R.REQUIRED, "模板名称");
			String modelUsers = Https.getStr(req, "modelUsers", R.REQUIRED,R.CLEAN,"角色集合");
			Integer firstStep = Https.getInt(req, "firstStep", R.REQUIRED, R.I, "第一审核人评分比重");
			Integer firstReferer = Https.getInt(req, "firstReferer", R.REQUIRED, R.I, "第一审核人");
			Integer secondReferer = Https.getInt(req, "secondReferer", R.I, "第二审核人");
		//	Integer thirdReferer = Https.getInt(req, "thirdReferer", R.I, "第三审核人");
			Integer secondStep = Https.getInt(req, "secondStep", R.I, "第二审核人评分比重");
		//	Integer thirdStep = Https.getInt(req, "thirdStep", R.I, "第三审核人评分比重");
			Integer[] arr = Converts.array(modelUsers, ",");
			PerformModel model = null;
			if(modelId != null){
				model = dao.fetch(PerformModel.class,modelId);
				Asserts.isNull(model, "配置不存在");
				List<PerformModel> list = dao.query(PerformModel.class, Cnd.where("corpId", "=", Context.getCorpId()).and("modelId", "!=", modelId));
				for(PerformModel performmodel : list){
					Integer[] userIds =Converts.array(performmodel.getModelUsers(), ","); 
					Asserts.hasAny(userIds, arr, "同一用户不能启用多个模板");
				}
			}else{
				model = new PerformModel();
				List<PerformModel> list = dao.query(PerformModel.class, Cnd.where("corpId", "=", Context.getCorpId()));
				for(PerformModel performmodel : list){
					Integer[] userIds =Converts.array(performmodel.getModelUsers(), ","); 
					Asserts.hasAny(userIds, arr, "同一用户不能启用多个模板");
				}
			}
			Integer weightSum = 0;
			weightSum = Values.getInt(firstStep)+ Values.getInt(secondStep);
			Asserts.notEq(weightSum, 100, "比率合计只能等于100");
			
			
			
			model.setCorpId(Context.getCorpId());
			model.setModelName(modelName);
			model.setModelUsers(modelUsers);
			model.setFirstReferer(firstReferer);
			model.setFirstSetp(firstStep);
			model.setSecondStep(secondStep);
			model.setSecondReferer(secondReferer);
		//	model.setThirdReferer(thirdReferer);
		//	model.setThirdStep(thirdStep);
			
			
			
			transSave(modelId,model,arr);
			Code.ok(mb, (modelId == null ? "新建" : "编辑") + "绩效模板成功");
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}catch (Exception e) {
			log.error("(AnnualRole:add) error: ", e);
			Code.error(mb, (modelId == null ? "新建" : "编辑") + "绩效模板失败");
		}
		return mb;
	}
	
	private void transSave(final Integer modelId, final PerformModel model, final Integer arr[]){
		Trans.exec(new Atom(){

			@Override
			public void run() {
				if(modelId != null)
					dao.update(model);
				else
					dao.insert(model);
			}
			
		});
	}
}
