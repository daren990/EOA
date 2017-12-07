package cn.oa.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;


/**
 * 相册表
 * @author jiawei.lu
 *
 */
@Table("alb_album")
public class Alb_album {
	
	@Id
	@Column("id")
	private Integer id;
	
	@Column("userId")
	private Integer userId;
	
	@Column("albumTitle")
	private String albumTitle;
	
	@Column("count")
	private int count;
	
	@Column("imgUrl")
	private String imgUrl;
	
	@Column("createTime")
	private Date createTime;
	
	@Column("username")
	private String username;
	
	@Column("headImg")
	private String headImg;
	
	@Column("description")
	private String description;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	private List<Alb_img> imgList = new ArrayList<Alb_img>();
	
	private List<Alb_commom> commomList = new ArrayList<Alb_commom>();
	
	public List<Alb_commom> getCommomList() {
		return commomList;
	}
	public void setCommomList(List<Alb_commom> commomList) {
		this.commomList = commomList;
	}
	public List<Alb_img> getImgList() {
		return imgList;
	}
	public void setImgList(List<Alb_img> imgList) {
		this.imgList = imgList;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getHeadImg() {
		return headImg;
	}
	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getAlbumTitle() {
		return albumTitle;
	}
	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
	public Alb_album() {
		super();
	}
	public Alb_album(Integer userId, String albumTitle, int count,
			String imgUrl, Date createTime,String username,String headImg,String description) {
		super();
		this.userId = userId;
		this.albumTitle = albumTitle;
		this.count = count;
		this.imgUrl = imgUrl;
		this.createTime = createTime;
		this.username = username;
		this.headImg = headImg;
		this.description = description;
	}
	
	
}
