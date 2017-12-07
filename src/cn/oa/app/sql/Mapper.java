package cn.oa.app.sql;

import java.util.List;
import java.util.Map;

import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.utils.web.Page;

@IocBean
public class Mapper {

	@Inject
	private Dao dao;

	public <T> Page<T> page(Class<T> clazz, Page<T> page, String count, String index, Condition cnd) {
		Sql sql = null;

		if (page.isAutoCount()) {
			sql = Sqls.queryEntity(dao.sqls().get(count));
			if (cnd != null) {
				sql.setCondition(cnd);
			}
			sql.setCallback(Sqls.callback.longValue());
			dao.execute(sql);
			page.setTotalItems(sql.getObject(Long.class));
		}

		sql = Sqls.queryEntity(dao.sqls().get(index));
		sql.setEntity(dao.getEntity(clazz));
		if (cnd != null) {
			sql.setCondition(cnd);
		}
		sql.params()
			.set("first", page.getOffset())
			.set("size", page.getPageSize());
		dao.execute(sql);
		page.setResult(sql.getList(clazz));

		return page;
	}

	public <T> List<T> query(Class<T> clazz, String query, Condition cnd) {
		return query(clazz, query, cnd, null, null);
	}
	
	public <T> List<T> query(Class<T> clazz, String query, Condition cnd, Map<String, String> params, Map<String, String> vars) {
		Sql sql = null;

		sql = Sqls.queryEntity(dao.sqls().get(query));
		sql.setEntity(dao.getEntity(clazz));
		if (cnd != null) {
			sql.setCondition(cnd);
		}
		if (params != null) {
			sql.params().putAll(params);
		}
		if (vars != null) {
			sql.vars().putAll(vars);
		}
		dao.execute(sql);
		return sql.getList(clazz);
	}
	
	public <T> T fetch(Class<T> clazz, String query, Condition cnd) {
		return fetch(clazz, query, cnd, null, null);
	}
	
	public <T> T fetch(Class<T> clazz, String query, Condition cnd, Map<String, String> params, Map<String, String> vars) {
		Sql sql = null;

		sql = Sqls.queryEntity(dao.sqls().get(query));
		sql.setEntity(dao.getEntity(clazz));
		if (cnd != null) {
			sql.setCondition(cnd);
		}
		if (params != null) {
			sql.params().putAll(params);
		}
		if (vars != null) {
			sql.vars().putAll(vars);
		}
		dao.execute(sql);
		return sql.getObject(clazz);
	}

	public Long count(String query, Condition cnd) {
		Sql sql = Sqls.queryEntity(dao.sqls().get(query));
		if (cnd != null) {
			sql.setCondition(cnd);
		}
		sql.setCallback(Sqls.callback.longValue());
		dao.execute(sql);
		return sql.getObject(Long.class);
	}
}
