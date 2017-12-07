package test;

import org.junit.Test;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.json.JsonLoader;

import cn.oa.app.cache.Ehcache;

public class CacheTest {

	@Test
	public void test() {
		try {
			Ioc ioc = new NutIoc(new JsonLoader("ioc.js"));
			Ehcache cache = ioc.get(Ehcache.class, "cache");
			cache.put("fqn", "key", "value");
			System.out.println(cache.get("fqn", "key"));
		} catch (Exception e) {
		}
	}
}
