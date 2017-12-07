package cn.oa;

import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Encoding;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.IocBy;
import org.nutz.mvc.annotation.Modules;
import org.nutz.mvc.annotation.SetupBy;
import org.nutz.mvc.annotation.Views;
import org.nutz.mvc.ioc.provider.ComboIocProvider;

import cn.oa.web.AppSetup;
import cn.oa.web.filter.AuthorityFilter;
import cn.oa.web.filter.CtxFilter;
import cn.oa.web.filter.LoggerFilter;
import cn.oa.web.filter.SessionFilter;
import cn.oa.web.view.FreemarkerViewMaker;

@IocBy(type = ComboIocProvider.class, args = {
	"*org.nutz.ioc.loader.json.JsonLoader",
	"ioc.js",
	"*org.nutz.ioc.loader.annotation.AnnotationIocLoader",
	"cn.oa.repository",
	"cn.oa.service",
	"cn.oa.web.action"})
@Modules(scanPackage = true)
@SetupBy(AppSetup.class)
@Filters({
	@By(type = SessionFilter.class),
	@By(type = AuthorityFilter.class),
	@By(type = LoggerFilter.class),
	@By(type = CtxFilter.class) })
@Encoding(input = "utf-8", output = "utf-8")
@Views({ FreemarkerViewMaker.class })
@Fail("ftl:404")
public class MainModule {
      
}
