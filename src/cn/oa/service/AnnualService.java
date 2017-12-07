package cn.oa.service;

import java.util.Map;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.model.AnnualRole;
import cn.oa.utils.Converts;

@IocBean
public class AnnualService {
	
	@Inject
	private Dao dao;

	public Map<String, String> map(int annualId) {
		
		AnnualRole annualRole = dao.fetch(AnnualRole.class, annualId);
		if (annualRole == null) return null;
			return Converts.map(annualRole.getAnnualValue());
		
	}
}
