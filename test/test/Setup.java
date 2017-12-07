package test;

import org.junit.Before;
import org.nutz.dao.Dao;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.annotation.AnnotationIocLoader;
import org.nutz.ioc.loader.combo.ComboIocLoader;
import org.nutz.ioc.loader.json.JsonLoader;

import cn.oa.app.cache.Ehcache;
import cn.oa.app.workflow.Snaker;
import cn.oa.repository.Mapper;
import cn.oa.service.ResultService;

public class Setup {

	protected Ioc ioc;
	protected Dao dao;
	protected Mapper mapper;
	protected Ehcache cache;
	protected Snaker snaker;
	protected ResultService resultService;

	@Before
	public void before() throws ClassNotFoundException {
		ioc = new NutIoc(new ComboIocLoader(new JsonLoader("ioc.js"), new AnnotationIocLoader("cn.oa.repository", "cn.oa.service")));
		dao = ioc.get(Dao.class);
		mapper = ioc.get(Mapper.class);
		cache = ioc.get(Ehcache.class, "cache");
		snaker = ioc.get(Snaker.class);
		resultService = ioc.get(ResultService.class);
	}
}
