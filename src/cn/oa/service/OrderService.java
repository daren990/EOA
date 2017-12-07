
package cn.oa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.cri.Static;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.model.EduStudent;
import cn.oa.model.EduStudentClient;
import cn.oa.model.EduStudentCorp;
import cn.oa.model.EduStudentCourse;
import cn.oa.model.ShopClient;
import cn.oa.model.ShopClientCorp;
import cn.oa.model.ShopGoods;
import cn.oa.model.ShopMember;
import cn.oa.model.ShopOrder;
import cn.oa.model.ShopOrderHandleLog;
import cn.oa.model.ShopOrderUnsubscribe;
import cn.oa.model.ShopProduct;
import cn.oa.model.ShopProductSettlement;
import cn.oa.model.User;
import cn.oa.model.vo.ShopOrderHandleLogVO;
import cn.oa.model.vo.ShopOrderUnsubscribeVO;
import cn.oa.model.vo.ShopProductSettlementLinePageVO;
import cn.oa.model.vo.ShopProductSettlementVO;
import cn.oa.model.vo.WxShopOrderVO;
import cn.oa.repository.Mapper;
import cn.oa.service.student.StudentService;
import cn.oa.utils.DateUtil;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Page;
import cn.oa.web.Context;

@IocBean(name="orderService")
public class OrderService {

	@Inject
	private Dao dao;
	
	@Inject
	private Mapper mapper;
	
	@Inject
	private StudentService studentService;
	
	@Inject
	protected WxClientService wxClientService;
	
	//操作日志类型
	public final static Integer delete = 0;
	
	public List<ShopOrderUnsubscribeVO> getUnsubsribeVOByShopProductID(Integer id){
		List<ShopOrderUnsubscribeVO> list = new ArrayList<ShopOrderUnsubscribeVO>();
		List<ShopOrderUnsubscribe> soulist = dao.query(ShopOrderUnsubscribe.class, Cnd.where(new Static(" shop_order_id in (select id from shop_order where shop_product_id = "+id+")")));
		for (ShopOrderUnsubscribe sou : soulist) {
			list.add(getShopOrderUnsubscribeVO(sou));
		}
		return list;
	}
	
	public List<ShopOrderUnsubscribeVO> getUnsubsribeVOByOrderids(String orderids){
		List<ShopOrderUnsubscribeVO> list = new ArrayList<ShopOrderUnsubscribeVO>();
		List<ShopOrderUnsubscribe> soulist = dao.query(ShopOrderUnsubscribe.class, Cnd.where(new Static(" shop_order_id in ("+orderids+")")));
		for (ShopOrderUnsubscribe sou : soulist) {
			list.add(getShopOrderUnsubscribeVO(sou));
		}
		return list;
	}
	
	/**
	 * 
	 * @param id shop_product >> id
	 * @return true:全部课程结束		false:有课程未结束
	 */
	public boolean isFinishCourse(Integer id){
		List<ShopGoods> sgList = dao.query(ShopGoods.class,
				Cnd.where(new Static (" id in (select shop_goods_id from shop_product where (id = "+id+" and parent_id = id) or (parent_id = "+id+" and parent_id != id))"))
				.and("status","!=","2"));
		if(sgList != null && sgList.size() > 0){
			return false;
		}else if(sgList == null){
			return true;
		}
		return true;
	}
	
	public void logHandle(Integer handleType,String ramark,Integer shop_order_id,Integer crop_id){
		ShopOrderHandleLog sohl = new ShopOrderHandleLog();
		sohl.setCreateTime(new Date());
		sohl.setCrop_id(crop_id);
		sohl.setHandleType(handleType);
		sohl.setOperatorId(Context.getUserId());
		sohl.setRemark(ramark);
		sohl.setShop_order_id(shop_order_id);
		dao.insert(sohl);
	}
	
	public ShopProductSettlementLinePageVO getShopProductSettlementLinePageVO(Page<ShopProductSettlement> page){
		ShopProductSettlementLinePageVO vo = new ShopProductSettlementLinePageVO();
		String ids ="";
		String productNames ="";
		String amounts ="";
		String unsubscribes ="";
		String expenditures ="";
		for (ShopProductSettlement sps : page.getResult()) {
			ShopProduct sp = dao.fetch(ShopProduct.class, sps.getShop_product_id());
			productNames += sp.getName() + ",";
			ids += sp.getName() + "-spid-" + sps.getId()+",";
			amounts += sps.getAmount()+",";
			unsubscribes += sps.getUnsubscribe()+",";
			expenditures += sps.getExpenditure()+",";
		}
		vo.setIds(ids.split(","));
		vo.setAmounts(amounts.split(","));
		vo.setExpenditures(expenditures.split(","));
		vo.setProductNames(productNames.split(","));
		vo.setUnsubscribes(unsubscribes.split(","));
		return vo;
	}
	
	public List<ShopProductSettlementVO> getShopProductSettlementVO(Page<ShopProductSettlement> page){
		List<ShopProductSettlementVO> list = new ArrayList<ShopProductSettlementVO>();
		for (ShopProductSettlement sps : page.getResult()) {
			list.add(getShopProductSettlementVO(sps));
		}
		return list;
	}
	
	public ShopProductSettlementVO getShopProductSettlementVO(ShopProductSettlement sps){
		ShopProductSettlementVO vo = new ShopProductSettlementVO();
		vo.setOrderids(sps.getOrderids());
		vo.setAmount(sps.getAmount());
		vo.setCreateTime(DateUtil.getDayTime(sps.getCreateTime()));
		vo.setCrop_id(sps.getCrop_id());
		vo.setUnsubscribe(sps.getUnsubscribe());
		vo.setExpenditure(sps.getExpenditure());
		vo.setId(sps.getId());
		ShopProduct sp = dao.fetch(ShopProduct.class, sps.getShop_product_id());
		vo.setShop_product_id(sps.getShop_product_id());
		vo.setShop_product_name(sp.getName());
		User u = dao.fetch(User.class, sps.getOperatorId());
		vo.setOperatorId(sps.getOperatorId());
		vo.setOperatorName(u.getUsername());
		vo.setTimes(sps.getTimes());
		vo.setType(sps.getType());
		return vo;
	}
	
	public List<ShopOrderHandleLogVO> getShopOrderHandleLogVO(Page<ShopOrderHandleLog> page){
		List<ShopOrderHandleLogVO> list = new ArrayList<ShopOrderHandleLogVO>();
		for (ShopOrderHandleLog sohl : page.getResult()) {
			list.add(getShopOrderHandleLogVO(sohl));
		}
		return list;
	}
	
	public ShopOrderHandleLogVO getShopOrderHandleLogVO(ShopOrderHandleLog sohl){
		ShopOrderHandleLogVO vo = new ShopOrderHandleLogVO();
		vo.setCreateTime(DateUtil.getDayTime(sohl.getCreateTime()));
		vo.setCrop_id(sohl.getCrop_id());
		vo.setId(sohl.getId());
		vo.setRemark(sohl.getRemark());
		vo.setHandleType(sohl.getHandleType());
		ShopOrder so = dao.fetch(ShopOrder.class,sohl.getShop_order_id());
		vo.setShop_order_id(sohl.getShop_order_id());
		WxShopOrderVO sovo = wxClientService.getShopOrderVO(so);
		vo.setTitle(sovo.getShop_client_name()+"于"+sovo.getCreate_time()+"购买的"+sovo.getShop_product_name());
		User u = dao.fetch(User.class, sohl.getOperatorId());
		vo.setOperatorId(sohl.getOperatorId());
		vo.setOperatorName(u.getUsername());
		return vo;
	}
	
	public List<ShopOrderUnsubscribeVO> getShopOrderUnsubscribeVO(Page<ShopOrderUnsubscribe> page){
		List<ShopOrderUnsubscribeVO> list = new ArrayList<ShopOrderUnsubscribeVO>();
		for (ShopOrderUnsubscribe sp : page.getResult()) {
			list.add(getShopOrderUnsubscribeVO(sp));
		}
		return list;
	}
	
	public ShopOrderUnsubscribeVO getShopOrderUnsubscribeVO(ShopOrderUnsubscribe sou){
		ShopOrderUnsubscribeVO vo = new ShopOrderUnsubscribeVO();
		vo.setAmount(sou.getAmount());
		vo.setCreateTime(DateUtil.getDayTime(sou.getCreateTime()));
		vo.setCrop_id(sou.getCrop_id());
		vo.setId(sou.getId());
		vo.setRemark(sou.getRemark());
		vo.setUnsubscribeType(sou.getUnsubscribeType());
		vo.setIsAll(sou.getIsAll());
		ShopOrder so = dao.fetch(ShopOrder.class,sou.getShop_order_id());
		vo.setShop_order_id(sou.getShop_order_id());
		WxShopOrderVO sovo = wxClientService.getShopOrderVO(so);
		vo.setTitle(sovo.getShop_client_name()+"于"+sovo.getCreate_time()+"购买的"+sovo.getShop_product_name());
		User u = dao.fetch(User.class, sou.getOperatorId());
		vo.setOperatorId(sou.getOperatorId());
		vo.setOperatorName(u.getUsername());
		return vo;
	}
	
	public void OrderDelete(final Integer orderId){
		Trans.exec(new Atom() {
			@Override
			public void run() {
				ShopOrder so = dao.fetch(ShopOrder.class, orderId);
				so.setOrderStatus(0);
				dao.update(so);
				ShopProduct sp = dao.fetch(ShopProduct.class,so.getShop_product_id());
				sp.setSold(sp.getSold()-1);
				dao.update(sp);
				List<ShopGoods> sgList = dao.query(ShopGoods.class,
						Cnd.where(new Static("id in (SELECT shop_goods_id from shop_product where id = "+sp.getId()+" OR parent_id = "+sp.getId()+")")));
				EduStudent es = new EduStudent();
				es.setId(so.getEdu_student_id());
				studentService.deleteRelantionWithCourses(es, sgList);
				for (ShopGoods sg : sgList) {
					sg.setMax(sg.getMax() + 1);
					sg.setSold(sg.getSold() - 1);
					dao.update(sg);
				}
				logHandle(delete,"web端按钮操作作废订单",so.getId(),so.getCrop_id());
			}
		});
	}
	
	public void OrderAdd(final Integer org_id ,final String stu_Name,final Integer shopProduct_id,final String client_Name,
			final String client_Phone,final Integer isOne,final Integer isBookPrice,final MapBean mb){
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
						ShopClient sc = dao.fetch(ShopClient.class,Cnd.where("telephone","=",client_Phone));
						if(sc == null || sc.getId() == null){
							sc = new ShopClient();
							sc.setTruename(client_Name);
							sc.setTelephone(client_Phone);
							dao.insert(sc);
							ShopClientCorp scc = new ShopClientCorp();
							scc.setCorpId(org_id);
							scc.setShopClientId(sc.getId());
							dao.insert(scc);
						}
						
						String sql_str = "select * from edu_student es "
								+ "where es.id in (select esc.edu_student_id from edu_student_client esc where esc.shop_client_id = "+sc.getId()+") "
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
							esc.setShopClientId(sc.getId());
							dao.insert(esc);
							EduStudentCorp esco = new EduStudentCorp();
							esco.setCorpId(org_id);
							esco.setEduStudentId(es.getId());
							dao.insert(esco);
						}else{
							so = dao.fetch(ShopOrder.class,
									Cnd.where("edu_student_id", "=", es.getId())
									.and("shop_client_id", "=", sc.getId())
									.and("shop_product_id","=",shopProduct_id)
									.and("crop_id","=",org_id));
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
							so.setShopClientId(sc.getId());
							so.setOrderStatus(1);
							so.setIsSettlement(0);
							so.setPayStatus(0);
							float amount = sp.getPrice();
							ShopMember sm = dao.fetch(ShopMember.class,Cnd.where("client_id", "=", sc.getId()));
							if(sm!=null && sm.getId()!=null){
								Integer member_level = sm.getLevel();
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
	
	public void unsubscribeOrder(final float amount, final Integer operatorId, final String remark, final Integer shop_order_id, final Integer unsubscribe,final Integer isAll){
		Trans.exec(new Atom() {
			@Override
			public void run() {
				ShopOrder so = dao.fetch(ShopOrder.class,shop_order_id);
				so.setIsUnsubscribe(1);
				dao.update(so);
				ShopOrderUnsubscribe sou = new ShopOrderUnsubscribe();
				sou.setCrop_id(so.getCrop_id());
				sou.setAmount(amount);
				sou.setCreateTime(new Date());
				sou.setOperatorId(operatorId);
				sou.setRemark(remark);
				sou.setShop_order_id(shop_order_id);
				sou.setUnsubscribeType(unsubscribe);
				sou.setIsAll(isAll);
				dao.insert(sou);
				if(isAll == 1){
					ShopProduct sp = dao.fetch(ShopProduct.class,so.getShop_product_id());
					sp.setSold(sp.getSold() - 1);
					dao.update(sp);
					List<ShopGoods> sgList = dao.query(ShopGoods.class,
							Cnd.where(new Static("id in (SELECT shop_goods_id from shop_product where id = "+sp.getId()+" OR parent_id = "+sp.getId()+")")));
					EduStudent es = new EduStudent();
					es.setId(so.getEdu_student_id());
					studentService.deleteRelantionWithCourses(es, sgList);
					for (ShopGoods sg : sgList) {
						sg.setMax(sg.getMax() + 1);
						sg.setSold(sg.getSold() - 1);
						dao.update(sg);
					}
				}
			}
		});
	}
	
}
