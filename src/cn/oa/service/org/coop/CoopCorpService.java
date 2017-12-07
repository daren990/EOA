package cn.oa.service.org.coop;

import java.util.List;

import cn.oa.model.CompanyCheckInfo;
import cn.oa.model.Org;

public interface CoopCorpService {
	/**
	 * 如果org为null，找到所有合作机构，否则根据id查找
	 * @return
	 */
	public List<Org> findCenterAndCoopCorp(Org org);
	
	/**
	 * 查询某个公司的打卡项目的ip地址，还有通讯密码
	 * @param org
	 * @return
	 */
	public CompanyCheckInfo findByOrgId(Integer orgId);
	
	
//------------------------------------------------
	
	/**
	 * 找到所有合作机构，传入管理中心的id,如果传入的是不是管理中心，那么只会返回自己
	 * @return
	 */
	public List<Org> findCoopCorp(Org center);
	
	/**
	 * 找到培训中心
	 * @return
	 */
	public Org findCenter();
}
