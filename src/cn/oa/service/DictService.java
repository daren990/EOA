package cn.oa.service;

import java.util.Map;

import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.model.Dict;
import cn.oa.utils.Converts;

@IocBean
public class DictService {

	@Inject
	private Dao dao;

	public Map<String, String> map(String name) {
		Dict dict = dao.fetch(Dict.class, name);
		if (dict == null) return null;
		return Converts.map(dict.getDictValue());
	}
}
