
package cn.oa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.Filters;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Path;
import cn.oa.consts.Status;
import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentClient;
import cn.oa.model.EduStudentCorp;
import cn.oa.model.EduStudentCourse;
import cn.oa.model.EduTeacher;
import cn.oa.model.Org;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopCompanyWechatConfig;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopMember;
import cn.oa.model.ShopOrder;
import cn.oa.model.ShopProduct;
import cn.oa.model.ShopWechatrelation;
import cn.oa.model.vo.EduCourseVO;
import cn.oa.model.vo.WxShopGoodsVO;
import cn.oa.model.vo.WxShopOrderVO;
import cn.oa.model.vo.WxShopProductVO;
import cn.oa.repository.Mapper;
import cn.oa.service.client.ClientService;
import cn.oa.utils.Asserts;
import cn.oa.utils.DateUtil;
import cn.oa.utils.DesUtils;
import cn.oa.utils.MapBean;
import cn.oa.utils.SignUtil;
import cn.oa.utils.UuidUtil;
import cn.oa.utils.QRCode.TwoDimensionCode;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Page;
import cn.oa.utils.web.PathUtil;
import cn.oa.utils.web.Webs;
import cn.oa.web.action.wx.comm.QiyeWeixinUtil;
import cn.oa.web.wx.Access_TokenSingleton;
import cn.oa.web.wx.Config;
import cn.oa.web.wx.InterfacePath;
import cn.oa.web.wx.JsApiTicketSingleton;
import cn.oa.web.wx.Merchant;


@Filters
@IocBean(name="wxClientService")
public class WxClientService{
	
	@Inject
	private Dao dao;
	@Inject
	private Mapper mapper;
	@Inject
	private ClientService clientService;
	
	/**
	 * 获取Merchantid,Merchantsecret 微信支付用
	 * @throws Exception 
	 */
	public Merchant initOrgMerchant(String org_id) throws Exception{
		Criteria cri = Cnd.cri();
		cri.where().and("org_id","=",org_id);
		ShopCompanyWechatConfig sc = dao.fetch(ShopCompanyWechatConfig.class, cri);
		if(sc == null){
			return null;
		}
		DesUtils des = new DesUtils(DesUtils.strDefaultKey);
		Merchant m = new Merchant(des.decrypt(sc.getMerchantid()),des.decrypt(sc.getMerchantsecret()));
		return m;
	}
	
	/**
	 * 获取appid,secret
	 * @throws Exception 
	 */
	public Config initOrgConfig(String org_id) throws Exception{
		Criteria cri = Cnd.cri();
		cri.where().and("org_id","=",org_id);
		ShopCompanyWechatConfig sc = dao.fetch(ShopCompanyWechatConfig.class, cri);
		if(sc == null){
			return null;
		}
		DesUtils des = new DesUtils(DesUtils.strDefaultKey);
		Config c = new Config(des.decrypt(sc.getAppid()),des.decrypt(sc.getSecret()));
		return c;
	}
	
	/**
	 * 获取appid,secret
	 * @throws Exception 
	 */
	public Config initOrgConfig(String org_id,StringBuilder QR_code) throws Exception{
		Criteria cri = Cnd.cri();
		cri.where().and("org_id","=",org_id);
		ShopCompanyWechatConfig sc = dao.fetch(ShopCompanyWechatConfig.class, cri);
		if(sc == null){
			return null;
		}
		QR_code.append(sc.getQrCode());
		DesUtils des = new DesUtils(DesUtils.strDefaultKey);
		Config c = new Config(des.decrypt(sc.getAppid()),des.decrypt(sc.getSecret()));
		return c;
	}
	
	/**
	 * 获取openid
	 * @return openid="reset"重新转发
	 */
	public String getOpenid(String code,Config config){
		if(config == null){
			return null;
		}
        StringBuilder sb = new StringBuilder(InterfacePath.NET_ACCESS_TOKEN_URL);
        sb.append("appid=").append(config.getAPPID());
        sb.append("&secret=").append(config.getSECRET());
        sb.append("&code=").append(code);
        sb.append("&grant_type=authorization_code");
        JSONObject jsonObject =  QiyeWeixinUtil.httpRequest(sb.toString(), "GET", null);
        String openid = null;
        //code已使用或者code过期无效
        if((jsonObject.containsKey("errcode") && (jsonObject.getString("errcode").equals("40163") || jsonObject.getString("errcode").equals("40029")))
        		&& (jsonObject.containsKey("errmsg") && (jsonObject.getString("errmsg").contains("code been used") || jsonObject.getString("errmsg").contains("invalid code")))){
        	openid = "reset";
        }else if(jsonObject.containsKey("openid")){
        	openid = jsonObject.getString("openid");
        }
        return openid != null ? openid : null;
    }
	
	/**
	 * 判断是否关联微信
	 */
	public boolean isClient(String org_id,String openid){
		if(openid == null){
			return false;
		}
		return getByOrgIdAndOpenId(org_id,openid) != null ? true : false;
	}
	
	public void wx_OrderAdd(final Integer org_id ,final String stu_Name,final String openid,final	Integer shopProduct_id,
			final Integer member_id,final Integer member_level,final MapBean mb){
		Trans.exec(new Atom() {
			@Override
			public void run() {
				ShopProduct sp = dao.fetch(ShopProduct.class,shopProduct_id);
				if(sp.getMax() - (sp.getSold() == null ? 0 : sp.getSold()) == 0){
					Code.error(mb, "商品已售空");
				}else{
					List<ShopGoods> sgList = dao.query(ShopGoods.class,
							Cnd.where(new Static("id in (SELECT shop_goods_id from shop_product where id = "+sp.getId()+" OR parent_id = "+sp.getId()+")")));
					boolean c = true;
					for (ShopGoods sg : sgList) {
						if(sg.getMax() == 0){
							c = false;
							Code.error(mb, "其中课程报名人数已满");
						}
					}
					if(c){
						boolean add = true;
						EduStudent es = new EduStudent();
						ShopOrder so = new ShopOrder();
						Criteria cri = Cnd.cri();
						cri.where().and("openID","=",openid).and("orgId", "=", org_id);
						ShopWechatrelation sw = dao.fetch(ShopWechatrelation.class, cri);
						String sql_str = "select * from edu_student es "
											+ "where es.id in (select esc.edu_student_id from edu_student_client esc where esc.shop_client_id = "+sw.getClientId()+") "
											+ "and es.name = '"+stu_Name+"';";
						Sql sql = Sqls.create(sql_str);
						sql.setCallback(Sqls.callback.entities());
						Entity<EduStudent> entity = dao.getEntity(EduStudent.class);
					    sql.setEntity(entity);
						dao.execute(sql);
						es = sql.getObject(EduStudent.class);
						if(es == null){
							es = new EduStudent();
							es.setStatus(0);
							es.setName(stu_Name);
							es.setNumber(System.currentTimeMillis() + "");
							dao.insert(es);
							EduStudentClient esc = new EduStudentClient();
							esc.setEduStudentId(es.getId());
							esc.setShopClientId(sw.getClientId());
							dao.insert(esc);
							EduStudentCorp esco = new EduStudentCorp();
							esco.setCorpId(org_id);
							esco.setEduStudentId(es.getId());
							dao.insert(esco);
						}else{
							so = dao.fetch(ShopOrder.class,
									Cnd.where("edu_student_id", "=", es.getId())
									.and("shop_client_id", "=", sw.getClientId())
									.and("shop_product_id","=",shopProduct_id)
									.and("crop_id","=",org_id)
									.and("order_status","=","1"));
							if(so!=null){
								Code.error(mb, "该小朋友已经报名此课程");
								add = false;
							}
						}
						if(add){
							so = new ShopOrder();
							so.setCreate_time(new Date());
							so.setCrop_id(org_id);
							so.setShop_product_id(sp.getId());
							so.setEdu_student_id(es.getId());
							so.setShopClientId(sw.getClientId());
							so.setOrderStatus(1);
							so.setIsSettlement(0);
							so.setPayStatus(0);
							float amount = sp.getPrice();
							if(member_id != null && member_id > 0){
								so.setIsMemberPay(1);
								so.setPayMemberLevel(member_level);
								if(member_level == 1){
									amount = (float) (amount * 0.7);
								}else if (member_level == 2){
									amount = (float) (amount * 0.8);
								}else if (member_level == 3){
									amount = (float) (amount * 0.9);
								}
							}else{
								so.setIsMemberPay(0);
							}
							so.setAmount(amount);
							dao.insert(so);
							if(sp.getSold() == null){
								sp.setSold(1);
							}else{
								sp.setSold(sp.getSold() + 1);
							}
							dao.update(sp);
							Code.setData(mb, "ShopOrderId", so.getId());
							
							for (ShopGoods sg : sgList) {
								sg.setMax(sg.getMax() - 1);
								if(sg.getSold() == null){
									sg.setSold(1);
								}else{
									sg.setSold(sg.getSold() + 1);
								}
								dao.update(sg);
								EduStudentCourse escou = dao.fetch(EduStudentCourse.class, 
										Cnd.where(new Static(" edu_student_id = " + es.getId() + " and edu_course_id = " + sg.getId())));
								if(escou == null){
									escou = new EduStudentCourse();
									escou.setEduCourseId(sg.getId());
									escou.setEduStudentId(es.getId());
									dao.insert(escou);
								}
							}
							Code.ok(mb, "报名成功");
						}
					}
				}
			}
		});
	}
	
	public void wx_InsertShopClient(final String clientName,final String clientPhone,final Integer corpId,final String openid){
		Trans.exec(new Atom() {
			@Override
			public void run() {
				ShopClient client = new ShopClient();
				client.setTelephone(clientPhone);
				client.setStatus(Status.DISABLED);
				ShopClient c;
				try {
					
					c = clientService.selectOne(client, null);
					if(c != null){
						client = c;
					}else{
						client.setTruename(clientName);
					}
					
					Org corp = new Org();
					corp.setOrgId(corpId);
					client.setCorps(pack(corp));
					clientService.buildRelation(client);
					
					ShopWechatrelation scr = dao.fetch(ShopWechatrelation.class, Cnd.where("clientId", "=", client.getId()).and("orgId", "=", corpId));
					if(scr == null){
						scr = new ShopWechatrelation();
					}
					scr.setClientId(client.getId());
					scr.setCreateTime(new Date());
					scr.setOpenId(openid);
					scr.setOrgId(corpId);
					if(scr.getId() == null){
						dao.insert(scr);
					}else{
						dao.update(scr);
					}
					
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	/**
	 * 获取微信关联
	 */
	public ShopWechatrelation getByOrgIdAndOpenId(String org_id,String openid){
		Criteria cri = Cnd.cri();
		cri.where().and("org_id","=",org_id).and("openID","=",openid);
		ShopWechatrelation sw = dao.fetch(ShopWechatrelation.class, cri);
		return sw;
	}
	
	
	/**
	 * 获取会员
	 */
	public ShopMember getByClientId(String client_id){
		Criteria cri = Cnd.cri();
		cri.where().and("client_id","=",client_id);
		return dao.fetch(ShopMember.class, cri);
	}
	
	/**
	 * 成为会员
	 */
	public void insertMember(Integer clientId,HttpServletRequest req,StringBuilder QR_code){
		ShopMember sm = new ShopMember();
		sm.setClientId(clientId);
		sm.setLevel(3);
		sm.setCreateTime(new Date());
		
		//图片名称
		String n = UuidUtil.get32UUID()+".png";
		//图片路径
		String c = Webs.root() + Path.QRCODE + "/" + n;
		//图片内容
		String ec = PathUtil.getBasePath(req)+"/wx/member/getMember?clientId="+clientId;
		TwoDimensionCode.encoderQRCode(ec, c, "png");
		sm.setQrCode(n);
		QR_code.append(n);
		try {
			dao.insert(sm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private <T> List<T> pack(T element){
		List<T> list = new ArrayList<T>();
		list.add(element);
		return list;
	}
	
	public List<WxShopOrderVO> getShopOrderVO(List<ShopOrder> plist){
		List<WxShopOrderVO> list = new ArrayList<WxShopOrderVO>();
		for (ShopOrder sp : plist) {
			list.add(getShopOrderVO(sp));
		}
		return list;
	}
	
	public List<WxShopOrderVO> getShopOrderVO(Page<ShopOrder> page){
		List<WxShopOrderVO> list = new ArrayList<WxShopOrderVO>();
		for (ShopOrder sp : page.getResult()) {
			list.add(getShopOrderVO(sp));
		}
		return list;
	}
	
	public List<WxShopOrderVO> getShopOrderVO(String orderid){
		String sql_str = "select * from shop_order where id = "+orderid;
		List<ShopOrder> soList = new ArrayList<ShopOrder>();
		Sql sql = Sqls.create(sql_str);
		sql.setCallback(Sqls.callback.entities());
		Entity<ShopOrder> entity = dao.getEntity(ShopOrder.class);
	    sql.setEntity(entity);
		dao.execute(sql);
		soList = sql.getList(ShopOrder.class);
		List<WxShopOrderVO> list = new ArrayList<WxShopOrderVO>();
		for (ShopOrder sp : soList) {
			list.add(getShopOrderVO(sp));
		}
		return list;
	}
	
	public List<WxShopOrderVO> getShopOrderVO(String openid,String org_id){
		String sql_str = "select * from shop_order where order_status = 1 and pay_status = 0 "
				+ "and shop_client_id in (select client_id from shop_wechatrelation where openID = '"+openid+"')and crop_id = "+org_id;
		List<ShopOrder> soList = new ArrayList<ShopOrder>();
		Sql sql = Sqls.create(sql_str);
		sql.setCallback(Sqls.callback.entities());
		Entity<ShopOrder> entity = dao.getEntity(ShopOrder.class);
	    sql.setEntity(entity);
		dao.execute(sql);
		soList = sql.getList(ShopOrder.class);
		List<WxShopOrderVO> list = new ArrayList<WxShopOrderVO>();
		for (ShopOrder sp : soList) {
			list.add(getShopOrderVO(sp));
		}
		return list;
	}
	
	public WxShopOrderVO getShopOrderVO(ShopOrder so){
		WxShopOrderVO vo = new WxShopOrderVO();
		vo.setId(so.getId());
		vo.setAmount(so.getAmount());
		vo.setCreate_time(DateUtil.getDayTime(so.getCreate_time()));
		vo.setEdu_student_id(so.getEdu_student_id());
		vo.setShop_client_id(so.getShopClientId());
		vo.setShop_product_id(so.getShop_product_id());
		vo.setIsUnsubscribe(so.getIsUnsubscribe());
		vo.setIsSettlement(so.getIsSettlement());
		vo.setOrderStatus(so.getOrderStatus());
		vo.setPayStatus(so.getPayStatus());
		EduStudent es = dao.fetch(EduStudent.class,so.getEdu_student_id());
		vo.setEdu_student_name(es.getName());
		ShopClient sc = dao.fetch(ShopClient.class,so.getShopClientId());
		vo.setShop_client_name(sc.getTruename());
		ShopProduct sp = dao.fetch(ShopProduct.class,so.getShop_product_id());
		vo.setShop_product_name(sp.getName());
		vo.setShop_product_price(sp.getPrice());
		return vo;
	}
	
	public EduCourseVO getECVO(String courseid){
		EduCourseVO ecvo = new EduCourseVO();
		ShopGoods sg = dao.fetch(ShopGoods.class,Integer.valueOf(courseid));
		ecvo.setCourse_id(sg.getId());
		ecvo.setCourse_name(sg.getName());
		ecvo.setTeacher_id(sg.getEduTeacherId());
		EduTeacher et = dao.fetch(EduTeacher.class, sg.getEduTeacherId());
		ecvo.setTeacher_name(et.getTruename());
		return ecvo;
	}
	
	public List<WxShopProductVO> getShopProductVo(Page<ShopProduct> page){
		if(page == null){
			return null;
		}
		List<WxShopProductVO> list = new ArrayList<WxShopProductVO>();
		for (ShopProduct sp : page.getResult()) {
			list.add(getShopProductVo(sp));
		}
		return list;
	}
	
	public WxShopProductVO getShopProductVo(ShopProduct sp){
		if(sp == null){
			return null;
		}
		WxShopProductVO vo = new WxShopProductVO();
		
		vo.setGoods_id(sp.getShopGoodsId());
		if(sp.getShopGoodsId() !=null && sp.getShopGoodsId() >-1){
			vo.setSgvo(getShopGoodsVO(dao.fetch(ShopGoods.class,sp.getShopGoodsId())));
		}
		vo.setId(sp.getId());
		vo.setName(sp.getName());
		vo.setPrice(sp.getPrice());
		if(sp.getId().equals(sp.getParentId())){
			vo.setOnSale(sp.getOnSale());
			vo.setStartTime(sp.getStartTime());
			vo.setEndTime(sp.getEndTime());
			vo.setSt(DateUtil.getDay(sp.getStartTime()));
			vo.setEt(DateUtil.getDay(sp.getEndTime()));
			vo.setMax(sp.getMax());
			vo.setSold(sp.getSold() != null ? sp.getSold() : 0);
			vo.setLocation(sp.getLocation());
			vo.setDetail(sp.getDetail());
		}
		
		List<ShopProduct> child = dao.query(ShopProduct.class, Cnd.where("parent_id","=",sp.getId()).and(new Static (" parent_id != id")));
		if(child !=null && child.size()>0){
			List<WxShopProductVO> childList = new ArrayList<WxShopProductVO>();
			for (ShopProduct sp_c : child) {
				childList.add(getShopProductVo(sp_c));
			}
			vo.setChildList(childList);
		}else{
			vo.setChildList(new ArrayList<WxShopProductVO>());
		}
		
		return vo;
	}
	
	public WxShopGoodsVO getShopGoodsVO(ShopGoods sg){
		if(sg == null){
			return null;
		}
		WxShopGoodsVO sgvo = new WxShopGoodsVO();
		sgvo.setId(sg.getId());
		sgvo.setName(sg.getName());
		sgvo.setPrice(sg.getPrice());
		sgvo.setMax(sg.getMax());
		sgvo.setSold(sg.getSold());
		EduTeacher et = dao.fetch(EduTeacher.class, sg.getEduTeacherId());
		sgvo.setTeacher_id(sg.getEduTeacherId());
		sgvo.setTeacher_name(et.getTruename());
		return sgvo;
	}
	
	public void setSignature(HttpServletRequest req,Config c){
		String access_token = Access_TokenSingleton.getInstance().getMap(c).get(c.getAPPID()).get("access_token");
		String ticket = JsApiTicketSingleton.getInstance().getMap(c, access_token).get(c.getAPPID()).get("jsapi_ticket");
		String wp = req.getRequestURL() + "?" + req.getQueryString();
		Map<String, String> ret = SignUtil.sign(ticket, wp);
		req.setAttribute("timestamp", ret.get("timestamp").toString());
		req.setAttribute("nonceStr", ret.get("nonceStr").toString());
		req.setAttribute("signature", ret.get("signature").toString());
	}
	
	//根据客户的id获取对应的ShopWechatrelation对象
	public List<ShopWechatrelation> getOpenIdAndOrgId(Integer clientId){
		Asserts.isNull(clientId, "请输入客户的id");
		List<ShopWechatrelation> shopWechatrelations = dao.query(ShopWechatrelation.class, Cnd.where("clientId", "=", clientId));
		if(shopWechatrelations == null || shopWechatrelations.size() == 0){
			return null;
		}
		return shopWechatrelations;
	}
}
