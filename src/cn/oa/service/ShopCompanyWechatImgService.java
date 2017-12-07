package cn.oa.service;

import java.util.ArrayList;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;

import cn.oa.model.ShopCompanyWechatImg;
import cn.oa.model.User;
import cn.oa.model.vo.ShopCompanyWechatImgVO;
import cn.oa.repository.Mapper;
import cn.oa.utils.DateUtil;

@IocBean(name="shopCompanyWechatImgService")
public class ShopCompanyWechatImgService {

	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	
	public List<ShopCompanyWechatImgVO> getShopCompanyWechatImgVO(Integer orgId){
		List<ShopCompanyWechatImgVO> list = new ArrayList<ShopCompanyWechatImgVO>();
		List<ShopCompanyWechatImg> nodes = dao.query(ShopCompanyWechatImg.class, Cnd.where("org_id", "=",orgId).and("state","!=","-1").asc("id"));
		for (ShopCompanyWechatImg node : nodes) {
			list.add(getShopCompanyWechatImgVO(node));
		}
		return list;
	}
	
	public ShopCompanyWechatImgVO getShopCompanyWechatImgVO(ShopCompanyWechatImg node){
		ShopCompanyWechatImgVO vo = new ShopCompanyWechatImgVO();
		vo.setId(node.getId());
		vo.setCreateTime(DateUtil.getDayTime(node.getCreateTime()));
		vo.setImgPath(node.getImgPath());
		vo.setOperatorId(node.getOperatorId());
		User u = dao.fetch(User.class, node.getOperatorId());
		if(u != null ){
			vo.setOperatorName(u.getUsername());
		}
		vo.setOrgId(node.getOrgId());
		vo.setState(node.getState());
		return vo;
	}
}
