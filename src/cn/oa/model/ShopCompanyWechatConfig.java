package cn.oa.model;

import java.util.Date;

import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("shop_company_wechatconfig")
public class ShopCompanyWechatConfig {

	@Override
	public String toString() {
		return "ShopCompanyWechatConfig [id=" + id + ", orgId=" + orgId + ", appid=" + appid + ", secret=" + secret
				+ ", merchantid=" + merchantid + ", merchantsecret=" + merchantsecret + ", qrCode=" + qrCode
				+ ", createTime=" + createTime + "]";
	}

	@Id
	@Column("id")
	private Integer id;

	@Column("org_id")
	private Integer orgId;

	@Column("appid")
	private String appid;

	@Column("secret")
	private String secret;

	@Column("merchantid")
	private String merchantid;

	@Column("merchantsecret")
	private String merchantsecret;

	@Column("qr_code")
	private String qrCode;

	@Column("create_time")
	private Date createTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getMerchantid() {
		return merchantid;
	}

	public void setMerchantid(String merchantid) {
		this.merchantid = merchantid;
	}

	public String getMerchantsecret() {
		return merchantsecret;
	}

	public void setMerchantsecret(String merchantsecret) {
		this.merchantsecret = merchantsecret;
	}

}
