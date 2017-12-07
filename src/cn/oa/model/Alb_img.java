package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 相册对应的图片表
 * @author jiawei.lu
 *
 */

@Table("alb_img")
public class Alb_img {

	@Id
	@Column("id")
	private Integer id;
	
	@Column("album_id")
	private Integer album_id;
	
	@Column("userId")
	private Integer userId;
	
	@Column("imgUrl")
	private String imgUrl;
	
	@Column("imgName")
	private String imgName;
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getAlbum_id() {
		return album_id;
	}
	public void setAlbum_id(Integer album_id) {
		this.album_id = album_id;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getImgName() {
		return imgName;
	}
	public void setImgName(String imgName) {
		this.imgName = imgName;
	}
	
	
	public Alb_img() {
		super();
	}
	public Alb_img(Integer album_id, Integer userId, String imgUrl,
			String imgName) {
		super();
		this.album_id = album_id;
		this.userId = userId;
		this.imgUrl = imgUrl;
		this.imgName = imgName;
	}
	
	
}
