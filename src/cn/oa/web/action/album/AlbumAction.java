package cn.oa.web.action.album;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.nutz.dao.Cnd;
import org.nutz.dao.sql.Criteria;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.model.Alb_album;
import cn.oa.model.Alb_commom;
import cn.oa.model.Alb_headImg;
import cn.oa.model.Alb_img;
import cn.oa.model.User;
import cn.oa.utils.MapBean;
import cn.oa.utils.web.Code;
import cn.oa.web.Context;
import cn.oa.web.action.Action;

@IocBean(name = "album.album")
@At(value = "/album/album")
public class AlbumAction extends Action{

	@GET
	@At
	@Ok("ftl:album/album")
	public void album_page(HttpServletRequest req){
		Criteria cri = Cnd.cri();
		cri.where().and("userId","=",Context.getUserId());
		List<Alb_album> listAlbum = dao.query(Alb_album.class, cri);
		req.setAttribute("listAlbum", listAlbum);
	}
	
	@GET
	@At
	@Ok("ftl:album/album_list")
	public void album_list(HttpServletRequest req){
		String albumId = req.getParameter("id");
		Criteria cri = Cnd.cri();
		cri.where().and("album_id","=",albumId)
		.and("userId","=",Context.getUserId());
		List<Alb_img> listImg = dao.query(Alb_img.class, cri);
		Alb_album alb_album = dao.fetch(Alb_album.class, Integer.parseInt(albumId));
		req.setAttribute("title", alb_album.getAlbumTitle());
		req.setAttribute("listImg", listImg);
		req.setAttribute("albumId", albumId);
	}
	
	@GET
	@At
	@Ok("ftl:album/downAlbum")
	public void downAlbum(HttpServletRequest req){
		String albumId = req.getParameter("id");
		req.getSession().setAttribute("albumId", albumId);
	}
	
	/**
	 * 图片上传
	 * @param req
	 */
	@POST
	@At
	public void albumUpload(HttpServletRequest req){
		DiskFileItemFactory disk = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(disk);
		Integer userId = Context.getUserId();
		String albumId = (String) req.getSession().getAttribute("albumId");
		String name = Context.getUsername();
		String path = req.getSession().getServletContext().getRealPath(File.separator+"jw_js_css_img"+File.separator+"img"+File.separator+"upload"+File.separator+""+name);
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		String albumImgUrl = null;
		Alb_album alb_album = null;
		Alb_img alb_img = null;
		upload.setSizeMax(10*1024*1024);
		upload.setHeaderEncoding("utf-8");
		if(ServletFileUpload.isMultipartContent(req)){
			try {
				List<FileItem> list = upload.parseRequest(req);
				for(FileItem item:list){
					if(!item.isFormField()){
						String uuid = UUID.randomUUID().toString();
						int album_id = Integer.parseInt(albumId);
						String imgName = item.getName();
						String imgUrl = File.separator+"jw_js_css_img"+File.separator+"img"+File.separator+"upload"+File.separator+""+name+""+File.separator+""+uuid+imgName;
						String root = path;
						alb_img = new Alb_img(album_id, userId, imgUrl, imgName);
						dao.insert(alb_img);
						alb_album = dao.fetch(Alb_album.class,album_id);
						albumImgUrl = imgUrl;
						item.write(new File(root, uuid+imgName));
					}
				}
				Criteria cri = Cnd.cri();
				cri.where().and("album_id","=",Integer.parseInt(albumId));
				List<Alb_img> albLen = dao.query(Alb_img.class, cri);
				alb_album.setCount(albLen.size());
				alb_album.setImgUrl(albumImgUrl);
				dao.update(alb_album);
			} catch (Exception e) {
			}
		}
		
	}
	
	/**
	 * 创建相册
	 */
	@POST
	@At
	@Ok("json")
	public Object createAlbum(HttpServletRequest req){
		MapBean mb = new MapBean();
		try {
			String albumTitle = req.getParameter("albumTitle");
			String description = req.getParameter("description");
			Integer userId = Context.getUserId();
			String username = Context.getTrueName();
			String name = Context.getUsername();
			Date createTime = new Date();
			Alb_album alb_album = new Alb_album(userId, albumTitle, 0, File.separator+"jw_js_css_img"+File.separator+"img"+File.separator+"noneAblum.jpg", createTime,username,File.separator+"jw_js_css_img"+File.separator+"img"+File.separator+"upload/"+name+""+File.separator+""+name+"Head.jpg",description);
			dao.insert(alb_album);
			mb.put("id", alb_album.getId());
			Code.ok(mb, "");
		} catch (Exception e) {
			Code.error(mb, "");
		}
		return mb;
	}
	
	/**
	 * 删除照片
	 */
	@POST
	@At
	@Ok("json")
	public Object deleteImg(HttpServletRequest req){
		MapBean mb = new MapBean();
		int count = 0;
		try {
			String ids = req.getParameter("ids");
			String[] id = ids.split(",");
			for(int i=0;i<id.length;i++){
				
				Alb_img alb_img = dao.fetch(Alb_img.class, Integer.parseInt(id[i]));
				
				//删除文件
				String imgUrl = req.getSession().getServletContext().getRealPath(alb_img.getImgUrl());
				File imgFile = new File(imgUrl);
				imgFile.delete();
				
				//删除数据库
				int albId = alb_img.getAlbum_id();
				Alb_album alb_album = dao.fetch(Alb_album.class, albId);
				count = alb_album.getCount() - 1;
				alb_album.setCount(count);
				dao.update(alb_album);
				dao.delete(Alb_img.class, Integer.parseInt(id[i]));
			}
			Code.ok(mb, "");
		} catch (Exception e) {
			Code.error(mb, "");
		}
		return mb;
	}
	
	/**
	 * 删除相册以及相册包含的照片
	 */
	@POST
	@At
	@Ok("json")
	public Object deleteAlbum(HttpServletRequest req){
		MapBean mb = new MapBean();
		try {
			String albumId = req.getParameter("albumId");
			Integer id = Integer.parseInt(albumId);
			Alb_album alb_album = dao.fetch(Alb_album.class, id);//得到相册
			
			Criteria cri = Cnd.cri();
			cri.where().and("album_id","=",id);
			List<Alb_img> listImg = dao.query(Alb_img.class, cri);//得到相册里面的图片
			transDelete(alb_album,listImg,req);
			Code.ok(mb, "");
		} catch (Exception e) {
			Code.error(mb, "");
		}
		return mb;
	}
	
	/**
	 * 删除相册和相册里面的照片的事务
	 * @param alb_album
	 * @param listImg
	 */
	private void transDelete(final Alb_album alb_album,final List<Alb_img> listImg,final HttpServletRequest req) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				dao.delete(alb_album);
				for(int i=0;i<listImg.size();i++){
					Alb_img alb_img = dao.fetch(Alb_img.class,listImg.get(i).getId());
					dao.delete(alb_img);
					String path = req.getSession().getServletContext().getRealPath(alb_img.getImgUrl());
					File imgFile = new File(path);
					imgFile.delete();
				}
			}
		});
	}
	
	/**
	 * 分享
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:album/album_share")
	public void share(HttpServletRequest req){
		Integer userId = Context.getUserId();
		Criteria albListCri = Cnd.cri();
		Criteria imgListCri = Cnd.cri();
		Criteria commomListCri = Cnd.cri();
		Criteria imgUrlCri = Cnd.cri();
		albListCri.getOrderBy().desc("id");
		imgListCri.getOrderBy().desc("id");
		commomListCri.getOrderBy().desc("id");
		imgUrlCri.where().and("userId", "=", userId);
		List<Alb_album> albList = dao.query(Alb_album.class,albListCri);
		List<Alb_img> imgList = dao.query(Alb_img.class, imgListCri);
		List<Alb_commom> commomList = dao.query(Alb_commom.class, commomListCri);
		List<Alb_headImg> headImgList = dao.query(Alb_headImg.class, imgUrlCri);
		for(Alb_album album:albList){
			for(Alb_img img:imgList){
				if(img.getAlbum_id() == album.getId()){
					album.getImgList().add(img);
				}
			}
			for(Alb_commom commom:commomList){
				if(commom.getAlbumId() == album.getId()){
					album.getCommomList().add(commom);
				}
			}
		}
		for(Alb_headImg imgUrl:headImgList){
			req.setAttribute("imgUrl", imgUrl.getImgUrl());
		}
		req.setAttribute("albList", albList);
		req.setAttribute("trueName", Context.getTrueName());
		req.setAttribute("trueId", Context.getUserId());
	}
	
	
	/**
	 * 评论
	 * @param req
	 * @return
	 */
	@POST
	@At
	@Ok("json")
	public Object commom(HttpServletRequest req){
		MapBean mb = new MapBean();
		try {
			String content = req.getParameter("content");//回复内容
			String album = req.getParameter("albumId");//相册id
			Integer albumId = Integer.parseInt(album);//转换相册id为int
			String toUser = req.getParameter("toUserId");//向谁评论的id
			String imgUrl = req.getParameter("imgUrl");//得到地址
			
			Integer userId = Context.getUserId();//评论者id
			String username = Context.getTrueName();//评论者姓名
			if(toUser == "" || toUser == null){
				Alb_commom alb_commom = new Alb_commom(userId, username, albumId, content,imgUrl);
				dao.insert(alb_commom);
			}else if(toUser != ""){
				Integer toUserId = Integer.parseInt(toUser);//转化向谁评论id为int类型
				User user = dao.fetch(User.class, toUserId);
				String toUserName = user.getTrueName();
				Alb_commom alb_commom = new Alb_commom(userId, username, albumId, content, toUserId, toUserName,imgUrl);
				dao.insert(alb_commom);
			}
			Code.ok(mb, "");
		} catch (Exception e) {
			Code.error(mb, "");
		}
		return mb;
	}
	
	/**
	 * 对一张照片的评论
	 */
	@POST
	@At
	@Ok("json")
	public Object oneImgCommom(HttpServletRequest req){
		MapBean mb = new MapBean();
		try {
			String imgUrl = req.getParameter("imgUrl");
			String content = req.getParameter("content");
			Integer albumId = null;//相册ID
			Integer userId = Context.getUserId();//自身id
			String username = Context.getTrueName();//自身姓名
			Criteria cri = Cnd.cri();
			cri.where().and("imgUrl", "=", imgUrl);
			List<Alb_img> albImgList = dao.query(Alb_img.class, cri);
			for(Alb_img albImg:albImgList){
				albumId = albImg.getAlbum_id();
			}
			Alb_commom alb_commom = new Alb_commom(userId, username, albumId, content, imgUrl);
			dao.insert(alb_commom);
			Code.ok(mb, "");
			return mb;
		} catch (Exception e) {
			Code.error(mb, "");
			return mb;
		}
	}
	
	/**
	 * 跳到头像上传页面
	 * @param req
	 */
	@GET
	@At
	@Ok("ftl:album/album_headPreview")
	public void album_headPreview(HttpServletRequest req){
	}
	
	/**
	 * 头像裁剪
	 * @param req
	 * @throws IOException 
	 * @throws ServletException 
	 */
	@POST
	@At
	@Ok("ftl:album/album_share")
	public void cutHeadImg(HttpServletRequest req,HttpServletResponse res) throws IOException, ServletException{
		String name = Context.getUsername();
		OperateImage operateImage = new OperateImage();
		String srcpath = req.getSession().getServletContext().getRealPath("/jw_js_css_img/img/upload/"+name+"/"+name+"Head.jpg");
		String subpath = req.getSession().getServletContext().getRealPath("/jw_js_css_img/img/upload/"+name+"/"+name+"Head.jpg");
		String x = req.getParameter("x");
		String y = req.getParameter("y");
		String width = req.getParameter("width");
		String height = req.getParameter("height");
		operateImage.cut(srcpath, subpath, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(width), Integer.parseInt(height));
		
		Integer userId = Context.getUserId();
		Criteria albListCri = Cnd.cri();
		Criteria imgListCri = Cnd.cri();
		Criteria commomListCri = Cnd.cri();
		Criteria imgUrlCri = Cnd.cri();
		albListCri.getOrderBy().desc("id");
		imgListCri.getOrderBy().desc("id");
		commomListCri.getOrderBy().desc("id");
		imgUrlCri.where().and("userId", "=", userId);
		List<Alb_album> albList = dao.query(Alb_album.class,albListCri);
		List<Alb_img> imgList = dao.query(Alb_img.class, imgListCri);
		List<Alb_commom> commomList = dao.query(Alb_commom.class, commomListCri);
		List<Alb_headImg> headImgList = dao.query(Alb_headImg.class, imgUrlCri);
		for(Alb_album album:albList){
			for(Alb_img img:imgList){
				if(img.getAlbum_id() == album.getId()){
					album.getImgList().add(img);
				}
			}
			for(Alb_commom commom:commomList){
				if(commom.getAlbumId() == album.getId()){
					album.getCommomList().add(commom);
				}
			}
		}
		for(Alb_headImg imgUrl:headImgList){
			req.setAttribute("imgUrl", imgUrl.getImgUrl());
		}
		req.setAttribute("albList", albList);
		req.setAttribute("trueName", Context.getTrueName());
		req.setAttribute("trueId", Context.getUserId());
		String path = "http://"+req.getLocalAddr()+":"+req.getLocalPort()+"/album/album/share";
		res.sendRedirect(path);
	}
	
	
	/**
	 * 上传需要裁剪的头像
	 * @param req
	 * @throws IOException
	 */
	@POST
	@At
	@Ok("ftl:album/album_headImg")
	public void uploadHeadImg(HttpServletRequest req) throws IOException{
		DiskFileItemFactory disk = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(disk);
		upload.setSizeMax(100*1024*1024);
		upload.setHeaderEncoding("utf-8");
		if(ServletFileUpload.isMultipartContent(req)){
			try {
				List<FileItem> list = upload.parseRequest(req);
				for(FileItem item:list){
					if(!item.isFormField()){
						String name = Context.getUsername();
						String path = req.getSession().getServletContext().getRealPath(File.separator+"jw_js_css_img"+File.separator+"img"+File.separator+"upload"+File.separator+""+name);
						String imgName = item.getName();
						File file = new File(path);
						if(!file.exists()){
							file.mkdir();
						}
						item.write(new File(path, name+"Head.jpg"));
						
						Criteria headImgCri = Cnd.cri();
						headImgCri.where().and("userId", "=", Context.getUserId());
						List<Alb_headImg> headImg = dao.query(Alb_headImg.class, headImgCri);
						if(headImg.size() == 0){
							Alb_headImg albHeadImg = new Alb_headImg(Context.getUserId(), name, File.separator+"jw_js_css_img"+File.separator+"img"+File.separator+"upload"+File.separator+""+name+""+File.separator+""+name+"Head.jpg", imgName);
							dao.insert(albHeadImg);
						}
					}
				}
			} catch (Exception e) {
			}
		}
		
	}
	
	@POST
	@At
	@Ok("ftl:album/album_headImg")
	public void jumpHeadImg(HttpServletRequest req) throws IOException{
		Criteria headImgCri = Cnd.cri();
		headImgCri.where().and("userId", "=", Context.getUserId());
		List<Alb_headImg> headImgList = dao.query(Alb_headImg.class, headImgCri);
		for(Alb_headImg headImg:headImgList){
			req.setAttribute("path", headImg.getImgUrl());
		}
	}
	
	@POST
	@At
	@Ok("ftl:album/album_headImg")
	public void myShare(HttpServletRequest req) throws IOException{
		Criteria headImgCri = Cnd.cri();
		headImgCri.where().and("userId", "=", Context.getUserId());
		List<Alb_headImg> headImgList = dao.query(Alb_headImg.class, headImgCri);
		for(Alb_headImg headImg:headImgList){
			req.setAttribute("path", headImg.getImgUrl());
		}
	}
	
	/**
	 * 事务控制
	 * @param alb_album
	 */
	/*private void transSave(final Alb_album alb_album,final Alb_img alb_img) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
			}
		});
	}*/
	
}
