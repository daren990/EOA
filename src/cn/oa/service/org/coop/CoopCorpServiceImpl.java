package cn.oa.service.org.coop;

import java.util.List;

import junit.framework.Assert;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.consts.Status;
import cn.oa.model.CompanyCheckInfo;
import cn.oa.model.Org;
import cn.oa.utils.Asserts;

@IocBean(name="coopCorpService")
public class CoopCorpServiceImpl implements CoopCorpService {
	
	@Inject
	private Dao dao;

	@Override
	public List<Org> findCoopCorp(Org center) {
		Asserts.isNull(center, "请传入center");
		SimpleCriteria cri = Cnd.cri();
		cri.where().and("parentId", "=", center.getOrgId());
		cri.where().and("type", "=", "3");
		cri.where().or("orgId", "=", center.getOrgId());
		return dao.query(Org.class, cri);
	}

	@Override
	public Org findCenter() {
		Org org = dao.fetch(Org.class, Cnd.where("type", "=", 3));
		return dao.fetch(Org.class, Cnd.where("orgId", "=", org.getParentId()));
	}

	@Override
	public List<Org> findCenterAndCoopCorp(Org coopOrg) {
		if(coopOrg != null && coopOrg.getOrgId() != null){
			return dao.query(Org.class, Cnd.where("type", "in", new Integer[]{2,3}).and("status", "=", Status.ENABLED).and("orgId", "=", coopOrg.getOrgId()));
		}else{
			return dao.query(Org.class, Cnd.where("type", "in", new Integer[]{2,3}).and("status", "=", Status.ENABLED));
		}
	}

	@Override
	public CompanyCheckInfo findByOrgId(Integer orgId) {
		Asserts.isNull(orgId, "请输入公司的id");
		return dao.fetch(CompanyCheckInfo.class, Cnd.where("orgId", "=", orgId));
	}

}
