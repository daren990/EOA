package cn.oa.model;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

/**
 * 评论表
 * @author jiawei.lu
 *
 */
@Table("alb_commom")
public class Alb_commom {

	@Id
	@Column("id")
	private Integer id;
	
	@Column("userId")
	private Integer userId;
	
	@Column("username")
	private String username;
	
	@Column("albumId")
	private Integer albumId;
	
	@Column("content")
	private String content;
	
	@Column("toUserId")
	private Integer toUserId;
	
	@Column("toUserName")
	private String toUserName;
	
	@Column("imgUrl")
	private String imgUrl;
	
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
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
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Integer getAlbumId() {
		return albumId;
	}
	public void setAlbumId(Integer albumId) {
		this.albumId = albumId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Integer getToUserId() {
		return toUserId;
	}
	public void setToUserId(Integer toUserId) {
		this.toUserId = toUserId;
	}
	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	public Alb_commom(Integer userId, String username, Integer albumId,
			String content, Integer toUserId, String toUserName,String imgUrl) {
		super();
		this.userId = userId;
		this.username = username;
		this.albumId = albumId;
		this.content = content;
		this.toUserId = toUserId;
		this.toUserName = toUserName;
		this.imgUrl = imgUrl;
	}
	
	public Alb_commom(Integer userId, String username, Integer albumId,
			String content, String imgUrl) {
		super();
		this.userId = userId;
		this.username = username;
		this.albumId = albumId;
		this.content = content;
		this.imgUrl = imgUrl;
	}
	public Alb_commom() {
		super();
	}
	
	
}
