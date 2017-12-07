package cn.oa.service;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.repository.Mapper;

@IocBean
public class OvertimeRestService {

	@Inject
	private Mapper mapper;
	
	public int getWorkMinute(Integer userId,String type){
		Long count =  mapper.count("OvertimeRest.minute", Cnd.where("apply_type", "=", type).and("userid", "=", userId));
		if(count !=null){
			return count.intValue();
		}else{
			return 0;
		}
	}
	
}
