package cn.oa.web.action.sys.conf;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.nutz.dao.Chain;
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
import java.util.Map.Entry;

import cn.oa.consts.Cache;
import cn.oa.consts.Status;
import cn.oa.model.ConfLeaveType;
import cn.oa.model.Org;
import cn.oa.model.ConfLeave;
import cn.oa.utils.Asserts;
import cn.oa.utils.Converts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Values;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.validate.Validator;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Servlets;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At("/sys/conf/confLeaveAction")
public class ConfLeaveAction extends Action{
	public static Log log = Logs.getLog(ConfLeaveAction.class);
	
	@GET
	@At
	@Ok("ftl:sys/conf/confLeave_page")	
	public void page (HttpServletRequest req){
		String token = CSRF.generate(req);
		CSRF.generate(req, "/sys/conf/confLeaveAction/page/able", token);
		MapBean mb = new MapBean();
		Criteria cri = Cnd.cri();
		Page<ConfLeave> page = Webs.page(req);
		page = mapper.page(ConfLeave.class, page, "ConfLeave.count", "ConfLeave.index", cri);
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		
		for (ConfLeave confLeave : page.getResult()) {
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getConfLeaveId() == null) continue;
				if (o.getConfLeaveId().equals(confLeave.getConfLeaveId())) {
					orgs.add(o);
				}
			}
			confLeave.setCorps(orgs);
		}
		
		req.setAttribute("mb", mb);
		req.setAttribute("page", page);
	}
	
	@GET
	@At
	@Ok("ftl:sys/conf/confLeave_add")
	public void add(HttpServletRequest req){
		CSRF.generate(req);
		Integer confLeaveId = Https.getInt(req, "confLeaveId", R.REQUIRED,R.I);
		ConfLeave conf = null;
		ConfLeaveType type = null;
		List<ConfLeaveType> leaveType = null;
		List<Org> corps = dao.query(Org.class, Cnd.where("status", "=", Status.ENABLED).and("type", "=", Org.CORP));
		if(confLeaveId !=null){
			conf = dao.fetch(ConfLeave.class,confLeaveId);
			Asserts.isNull(conf, "配置不存在");
			leaveType = mapper.query(ConfLeaveType.class, "ConfLeaveType.query", Cnd.where("t.conf_leave_id", "=", confLeaveId));
		}
		if(leaveType == null){
			leaveType = new ArrayList<ConfLeaveType>();
			type = new ConfLeaveType();
			leaveType.add(type);
		}
		
		if(conf == null)
			conf = new ConfLeave();
		
		if(confLeaveId !=null){
			List<Org> orgs = new ArrayList<Org>();
			for (Org o : corps) {
				if (o.getConfLeaveId() == null) continue;
				if (o.getConfLeaveId().equals(conf.getConfLeaveId())) {
					orgs.add(o);
				}
			}
			conf.setCorps(orgs);
		}
		req.setAttribute("leaveMap", dictService.map("leave"));
		req.setAttribute("corps", corps);
		req.setAttribute("conf", conf);
		req.setAttribute("leaveType", leaveType);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res){
		MapBean mb = new MapBean();
		Integer confLeaveId = null;
		try{
			CSRF.validate(req);
			confLeaveId = Https.getInt(req, "confLeaveId",R.I); 
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I); 
			String confLeaveName = Https.getStr(req, "confLeaveName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "配置名称");
			String corpIds = Https.getStr(req, "corpIds", R.CLEAN, R.REGEX, "regex:^[0-9,]+$:不是合法值", "公司绑定");
			Map<String, Object> leaveMap = Servlets.startsWith(req, "leaveType_");
			Map<String, Object> wayMap = Servlets.startsWith(req, "way_");
			Map<String, Object> dayAmountMap = Servlets.startsWith(req, "dayAmount_");
			Map<String, Object> multiplicationMap = Servlets.startsWith(req, "multiplication_");
			Integer[] arr = Converts.array(corpIds, ",");
			ConfLeave confLeave = null;
			for(Entry<String, Object> entry : leaveMap.entrySet()){
				String index = entry.getKey();
				Validator.validate(entry.getValue(), R.REQUIRED, R.I, "请假类别");
				Validator.validate(wayMap.get(index), R.REQUIRED, R.I, "带薪天数");
				Validator.validate(dayAmountMap.get(index), R.REQUIRED, R.I, "带薪天数");
				Validator.validate(multiplicationMap.get(index), R.REQUIRED, R.F,R.REGEX,"regex:(^[0]+(.[0-9]{0,2})?)|(^[1])$:值只能在0-1之间且最多只有两位小数", "扣除系数");
			}
			if(confLeaveId !=null){
				confLeave = dao.fetch(ConfLeave.class,confLeaveId);
				Asserts.isNull(confLeave, "配置不存在");
			}
			else{
				confLeave = new ConfLeave();
			}
			confLeave.setConfLeaveName(confLeaveName);
			confLeave.setStatus(status);
	
			tranSave(arr, confLeave, confLeaveId, corpIds, leaveMap, wayMap, dayAmountMap, multiplicationMap);
			Code.ok(mb, (confLeaveId == null ? "新建" : "编辑") + "请假配置成功");
		}catch (Errors e) {
			Code.error(mb, e.getMessage());
		}
		catch (Exception e) {
			log.error("(confLeave:add) error: ", e);
			Code.error(mb, (confLeaveId == null ? "新建" : "编辑") + "请假配置失败");
		}
		return mb;
		
	}
	
	@POST
	@At
	@Ok("json")
	public Object able(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		try {
			CSRF.validate(req);
			String checkedIds = Https.getStr(req, "checkedIds", R.CLEAN, R.REQUIRED, R.REGEX, "regex:^[0-9,]+$:不是合法值", "checkedIds");
			Integer[] arr = Converts.array(checkedIds, ",");
			Integer status = Https.getInt(req, "status", R.REQUIRED, R.I, R.IN, "in [0,1]", "状态");
			if (arr != null && arr.length > 0) {
				cache.removeAll(Cache.FQN_RESOURCES);
				dao.update(ConfLeave.class, Chain.make("status", status), Cnd.where("confLeaveId", "in", arr));
			}
			Code.ok(mb, "设置请假配置状态成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Day:able) error: ", e);
			Code.error(mb, "设置请假配置状态失败");
		}

		return mb;
	}
	
	private void tranSave(final Integer[] arr, final ConfLeave confLeave, final Integer confLeaveId, final String corpIds, final Map<String, Object> leaveMap,
						final Map<String, Object> wayMap, final Map<String, Object> dayAmountMap, final Map<String, Object> multiplicationMap){
		Trans.exec(new Atom(){

			@Override
			public void run() {
				Integer id = confLeaveId;
				
				//将org表中原本的指向某个请假配置的id设置为null，因为准备修改请假配置
				dao.update(Org.class, Chain.make("confLeaveId", null), Cnd.where("confLeaveId", "=", confLeaveId));
				
				if(confLeaveId != null){
					//更新conf_leave表中的记录中的名字
					dao.update(confLeave);
					//清空原来的请假配置的内容
					dao.clear(ConfLeaveType.class,Cnd.where("confLeaveId", "=",confLeaveId));
				}
				else{
					ConfLeave resultSet =  dao.insert(confLeave);
					id = resultSet.getConfLeaveId();
				}
				
				List<ConfLeaveType> leaveTypes = new ArrayList<ConfLeaveType>();
				for (Entry<String, Object> entry : leaveMap.entrySet()) {
					String index = entry.getKey();
					String Type =  (String) entry.getValue();
					Integer leaveType = Integer.parseInt(Type);
					Integer way = Values.getInt(wayMap.get(index));
					Integer dayAmount = Values.getInt(dayAmountMap.get(index));
					Double multiplication = Values.getDouble(multiplicationMap.get(index));
					leaveTypes.add(new ConfLeaveType(id,leaveType, way, dayAmount, multiplication));
				}
				//插入批量的ConfLeaveType对象
				dao.fastInsert(leaveTypes);
				
				if (!Asserts.isEmpty(arr))
					//更新公司中所指向的请假配置
					dao.update(Org.class, Chain.make("confLeaveId", id), Cnd.where("orgId", "in", arr));
				
			}});
					
	}
}	
