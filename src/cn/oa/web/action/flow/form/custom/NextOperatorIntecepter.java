package cn.oa.web.action.flow.form.custom;

import org.snaker.engine.SnakerInterceptor;
import org.snaker.engine.core.Execution;

import cn.oa.web.Context;

public class NextOperatorIntecepter implements SnakerInterceptor {

	@Override
	public void intercept(Execution execution) {
		execution.getArgs().put("task1.operator", Context.getUserId());
	}

}
