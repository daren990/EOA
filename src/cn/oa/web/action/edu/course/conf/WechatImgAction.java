package cn.oa.web.action.edu.course.conf;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;

import cn.oa.model.ShopCompanyWechatImg;
import cn.oa.model.vo.ShopCompanyWechatImgVO;
import cn.oa.utils.MapBean;
import cn.oa.utils.UuidUtil;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.web.Context;
import cn.oa.web.action.Action;


@IocBean
@At(value = "/edu/course/conf/wechatImg")
public class WechatImgAction extends Action
{
	
	public static Log log = Logs.getLog(WechatImgAction.class);
	
	@GET
	@POST
	@At
	@Ok("ftl:edu/course/conf/wechatImg_page")
	public void page(HttpServletRequest req) {
		Integer orgId = Context.getCorpId();
		List<ShopCompanyWechatImgVO> voList = shopCompanyWechatImgService.getShopCompanyWechatImgVO(orgId);
		req.setAttribute("voList", voList);
	}
	
	/**
	 * 跳到图片上传页面
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:edu/course/conf/wechatImg_add")
	public void add(HttpServletRequest req){
	}
	
	/**
	 * 图片上传
	 * @param req
	 */
	@POST
	@At
	public void wechatImgUpload(HttpServletRequest req){
		DiskFileItemFactory disk = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(disk);
		Integer userId = Context.getUserId();
		Integer orgId = Context.getCorpId();
		String path = req.getSession().getServletContext().getRealPath(File.separator+"jw_js_css_img"+File.separator+"img"+File.separator+"wechatImg");
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		ShopCompanyWechatImg wx_img = null;
		upload.setSizeMax(10*1024*1024);
		upload.setHeaderEncoding("utf-8");
		if(ServletFileUpload.isMultipartContent(req)){
			try {
				List<FileItem> list = upload.parseRequest(req);
				for(FileItem item:list){
					if(!item.isFormField()){
						String uuid = UuidUtil.get32UUID();
						String imgName = item.getName();
						String root = path;
						wx_img = new ShopCompanyWechatImg();
						wx_img.setCreateTime(new Date());
						wx_img.setImgPath(uuid+imgName);
						wx_img.setOperatorId(userId);
						wx_img.setOrgId(orgId);
						wx_img.setState(1);
						dao.insert(wx_img);
						item.write(new File(root, uuid+imgName));
					}
				}
			} catch (Exception e) {
			}
		}
	}
	
	@POST
	@At
	@Ok("json")
	public Object updateImg(HttpServletRequest req) throws Exception {
		MapBean mb = new MapBean();
		try {
			Integer id = Https.getInt(req, "id");
			Integer state = Https.getInt(req, "state");
			ShopCompanyWechatImg img = dao.fetch(ShopCompanyWechatImg.class,id);
			img.setState(state);
			dao.update(img);
			Code.ok(mb, "操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			Code.error(mb, "服务器错误");
		}
		return mb;
	}
}
