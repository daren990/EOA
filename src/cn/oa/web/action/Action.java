package cn.oa.web.action;

import org.nutz.dao.Dao;
import org.nutz.ioc.loader.annotation.Inject;

import cn.oa.app.cache.Ehcache;
import cn.oa.app.workflow.Snaker;
import cn.oa.repository.Mapper;
import cn.oa.repository.ResourceRepository;
import cn.oa.repository.RoleRepository;
import cn.oa.repository.WorkRepository;
import cn.oa.service.AddressBookService;
import cn.oa.service.AnnouncementService;
import cn.oa.service.CheckedRecordService;
import cn.oa.service.DictService;
import cn.oa.service.ErrandService;
import cn.oa.service.LeaveService;
import cn.oa.service.OrderService;
//import cn.oa.service.OrderService;
import cn.oa.service.OutworkService;
import cn.oa.service.OvertimeService;
import cn.oa.service.ProductSettlementService;
import cn.oa.service.ResultService;
import cn.oa.service.ShopCompanyWechatImgService;
import cn.oa.service.ToolsService;
import cn.oa.service.UserService;
import cn.oa.service.WorkService;
import cn.oa.service.WxClientService;
import cn.oa.service.WxGoodsService;
import cn.oa.service.WxStudentService;
//import cn.oa.service.WxClientService;
import cn.oa.service.WxUserService;
import cn.oa.service.client.ClientService;
import cn.oa.service.course.CourseService;
import cn.oa.service.org.coop.CoopCorpService;
//import cn.oa.service.org.coop.CoopCorpService;
import cn.oa.service.schedule.TeachingSchedule;
import cn.oa.service.student.StudentService;
import cn.oa.service.teacher.TeacherService;
import cn.oa.utils.encoder.Md5;
import cn.oa.web.Views;

public class Action {
 
	@Inject
	protected Dao dao;
	@Inject
	protected Mapper mapper;
	
	@Inject
	protected ResourceRepository resourceRepository;
	@Inject
	protected RoleRepository roleRepository;
	@Inject
	protected WorkRepository workRepository;
	
	@Inject
	protected CheckedRecordService checkedRecordService;
	@Inject
	protected DictService dictService;
	@Inject
	protected ErrandService errandService;
	@Inject
	protected LeaveService leaveService;
	@Inject
	protected OutworkService outworkService;
	@Inject
	protected OvertimeService overtimeService;
	@Inject
	protected ResultService resultService;
	@Inject
	protected UserService userService;
	@Inject
	protected WorkService workService;
	@Inject
	protected StudentService studentService;
	@Inject
	protected ClientService clientService;
	@Inject
	protected TeacherService teacherService;
	@Inject
	protected CourseService courseService;
	@Inject
	protected TeachingSchedule teachingSchedule;
	@Inject
	protected CoopCorpService coopCorpService;
	@Inject
	protected WxUserService wxUserService;
	
	@Inject
	protected AnnouncementService announcementService;
	
	@Inject
	protected AddressBookService addressBookService;
	
	@Inject
	protected WxClientService wxClientService;
		
	@Inject
	protected OrderService orderService;
	@Inject
	protected ProductSettlementService productSettlementService;
	@Inject
	protected WxStudentService wxStudentService;
	@Inject
	protected WxGoodsService wxGoodsService;
	@Inject
	protected ShopCompanyWechatImgService shopCompanyWechatImgService;
	
	@Inject
	protected ToolsService toolsService;
	
	@Inject
	protected Ehcache cache;
	@Inject
	protected Md5 md5;
	@Inject
	protected Snaker snaker;
	@Inject
	protected Views views;
}
