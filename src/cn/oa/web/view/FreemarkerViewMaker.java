package cn.oa.web.view;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.View;
import org.nutz.mvc.ViewMaker;

import cn.oa.utils.Strings;

public class FreemarkerViewMaker implements ViewMaker {

	@Override
	public View make(Ioc ioc, String type, String value) {
		if (Strings.equals("ftl", type)) {
			return new FreemarkerView(value);
		}

		return null;
	}
}
