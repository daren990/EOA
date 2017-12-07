package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;



/**
 * 头像
 * @author jiawei.lu
 *
 */
@Table("alb_headImg")
public class Alb_headImg {

	@Id
	@Column("id")
	private Integer id;
	@Column("userId")
	private Integer userId;
	@Column("userName")
	private String userName;
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
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
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
	public Alb_headImg(Integer userId, String userName,
			String imgUrl, String imgName) {
		super();
		this.userId = userId;
		this.userName = userName;
		this.imgUrl = imgUrl;
		this.imgName = imgName;
	}
	public Alb_headImg() {
		super();
	}
	
	
	
}
