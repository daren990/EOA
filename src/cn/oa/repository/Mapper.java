package cn.oa.repository;

import java.util.List;
import java.util.Map;

import org.nutz.dao.Condition;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;

import cn.oa.utils.web.Page;

@IocBean
public class Mapper {

	@Inject
	private Dao dao;
	private static final Log log = Logs.getLog(Mapper.class);
	
	//发出两条查询条件相同的sql语句，实现分页查询
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
		List<T> list = sql.getList(clazz);
		page.setResult(list);

		return page;
	}

	//Author :  AshHo  其实就是传多一个键值对，name为SQL中声明的参数，args为具体值
	public <T> Page<T> page(Class<T> clazz, Page<T> page, String count, String index, Condition cnd,String name,String args) {
		Sql sql = null;

		if (page.isAutoCount()) {
			sql = Sqls.queryEntity(dao.sqls().get(count));
			if (cnd != null) {
				sql.setCondition(cnd);
			}
			sql.params().set(name, args);
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
		    .set(name, args)
			.set("first", page.getOffset())
			.set("size", page.getPageSize());
		dao.execute(sql);
		List<T> list = sql.getList(clazz);
		page.setResult(list);
		System.out.println("\n\n\n\n" + sql + "\n\n\n\n");
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
