package cn.oa.web.action.sys.conf;

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

import cn.oa.model.Dict;
import cn.oa.utils.Asserts;
import cn.oa.utils.MapBean;
import cn.oa.utils.Strings;
import cn.oa.utils.except.Errors;
import cn.oa.utils.validate.R;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.Action;

@IocBean
@At(value = "/sys/conf/dict")
public class DictAction extends Action {

	public static Log log = Logs.getLog(DictAction.class);

	@GET
	@At
	@Ok("ftl:sys/conf/dict_page")
	public void page(HttpServletRequest req) {
		Page<Dict> page = Webs.page(req);
		page = mapper.page(Dict.class, page, "Dict.count", "Dict.index", null);

		req.setAttribute("page", page);
	}

	@GET
	@At
	@Ok("ftl:sys/conf/dict_add")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		String dictName = Https.getStr(req, "dictName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}");
		Dict dict = null;
		if (Strings.isNotBlank(dictName)) {
			dict = dao.fetch(Dict.class, dictName);
		}
		if (dict == null)
			dict = new Dict();

		req.setAttribute("dict", dict);
	}

	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		String orgDictName = null;
		try {
			CSRF.validate(req);
			orgDictName = Https.getStr(req, "orgDictName", R.CLEAN, R.RANGE, "{1,20}", "字典名称");
			String dictName = Https.getStr(req, "dictName", R.CLEAN, R.REQUIRED, R.RANGE, "{1,20}", "字典名称");
			String dictDesc = Https.getStr(req, "dictDesc", R.CLEAN, R.REQUIRED, R.RANGE, "{1,60}", "字典描述");
			String dictValue = Https.getStr(req, "dictValue", R.REQUIRED, "字典集合");

			Asserts.notUnique(dictName, orgDictName, dao.count(Dict.class, Cnd
					.where("dictName", "=", dictName)), "字典名称已存在");

			Dict dict = null;
			if (Strings.isNotBlank(orgDictName)) {
				dict = dao.fetch(Dict.class, dictName);
				Asserts.isNull(dict, "字典不存在");
			} else {
				dict = new Dict();
			}

			dict.setDictName(dictName);
			dict.setDictDesc(dictDesc);
			dict.setDictValue(dictValue);

			if (Strings.isNotBlank(orgDictName))
				dao.update(dict);
			else
				dao.insert(dict);

			Code.ok(mb, (Strings.isBlank(orgDictName) ? "新建" : "编辑") + "字典成功");
		} catch (Errors e) {
			log.error("(Dict:add) error: ", e);
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Dict:add) error: ", e);
			Code.error(mb, (Strings.isBlank(orgDictName) ? "新建" : "编辑") + "字典失败");
		}
		
		return mb;
	}
}
