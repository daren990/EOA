package cn.oa.web.action.edu.course.conf;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.trans.Atom;
import org.nutz.trans.Trans;

import cn.oa.consts.Cache;
import cn.oa.consts.Path;
import cn.oa.model.ShopCompanyWechatConfig;
import cn.oa.utils.Asserts;
import cn.oa.utils.DesUtils;
import cn.oa.utils.MapBean;
import cn.oa.utils.QRCode.TwoDimensionCode;
import cn.oa.utils.except.Errors;
import cn.oa.utils.web.CSRF;
import cn.oa.utils.web.Code;
import cn.oa.utils.web.Https;
import cn.oa.utils.web.PathUtil;
import cn.oa.utils.web.Webs;
import cn.oa.web.Context;
import cn.oa.web.action.Action;
import cn.oa.web.action.wx.comm.QiyeWeixinUtil;
import cn.oa.web.wx.Access_TokenSingleton;
import cn.oa.web.wx.Config;


@IocBean
@At(value = "/edu/course/conf/wechatConfig")
public class WechatConfigAction extends Action
{
	
	public static Log log = Logs.getLog(WechatConfigAction.class);
	
	//设置行业信息
	public static String Industry = "https://api.weixin.qq.com/cgi-bin/template/api_set_industry?access_token=ACCESS_TOKEN";
	
	@At
	@Ok("ftl:edu/course/conf/wechatConfig")
	public void add(HttpServletRequest req) {
		CSRF.generate(req);
		Integer orgId = Context.getCorpId();
		
		ShopCompanyWechatConfig wechat = new ShopCompanyWechatConfig();

		List<ShopCompanyWechatConfig> chatList = dao.query(ShopCompanyWechatConfig.class, Cnd
				.where("org_id", "=",orgId));
		
		if(chatList.size() != 0)
		{
			wechat = chatList.get(0);
			//对加密类进行实例化，参数为默认值，不进行任何修改。
			DesUtils des;
			try 
			{
				des = new DesUtils();
				//加密后赋值
				wechat.setAppid(des.decrypt(wechat.getAppid()));
				wechat.setSecret(des.decrypt(wechat.getSecret()));
				wechat.setMerchantid(des.decrypt(wechat.getMerchantid()));
				wechat.setMerchantsecret(des.decrypt(wechat.getMerchantsecret()));
			}
			catch (Exception e1) 
			{
				e1.printStackTrace();
			}
		}
		
		req.setAttribute("wechat", wechat);
	}
	
	@POST
	@At
	@Ok("json")
	public Object add(HttpServletRequest req, HttpServletResponse res) {
		MapBean mb = new MapBean();
		Integer id = null;
		try {
			CSRF.validate(req);
			
			id = Https.getInt(req, "id");
			String appid = Https.getStr(req, "appid");
			String secret = Https.getStr(req, "secret");
			String merchantid = Https.getStr(req, "merchantid");
			String merchantsecret = Https.getStr(req, "merchantsecret");
						
			//初始化公众号的行业信息
			int result = initTemplet(appid, secret);
			
			DesUtils des = new DesUtils();
			//解密后赋值，防止用户提交已加密的数据，再次加密。
			String encodeAppId = des.encrypt(appid);
			String encodeSecret = des.encrypt(secret);
			String encodeMerchantid = des.encrypt(merchantid);
			String encodeMerchantsecret = des.encrypt(merchantsecret);
			String qrname = UUID.randomUUID().toString() + ".png";
			
			//图片路径
			String c = Webs.root() + Path.QRCODE + "/" + qrname;
			//图片内容
			String ec = PathUtil.getBasePath(req)+"/wx/member/join_view?org_id=" + Context.getOrgId();

			//生成二维码
			TwoDimensionCode.encoderQRCode(ec, c, "png");
			
			ShopCompanyWechatConfig wechat = new ShopCompanyWechatConfig();
			if (id != null) {
				wechat = dao.fetch(ShopCompanyWechatConfig.class, id);
				Asserts.isNull(wechat, "机构不存在");
				wechat.setAppid(encodeAppId);
				wechat.setSecret(encodeSecret);
				wechat.setMerchantid(encodeMerchantid);
				wechat.setMerchantsecret(encodeMerchantsecret);
			} else {
				wechat.setQrCode(qrname);
				wechat.setOrgId(Context.getOrgId());
				wechat.setAppid(encodeAppId);
				wechat.setSecret(encodeSecret);	
				wechat.setMerchantid(encodeMerchantid);
				wechat.setMerchantsecret(encodeMerchantsecret);
				wechat.setCreateTime(new Date());
			}

			cache.removeAll(Cache.FQN_RESOURCES);
			transSave(id, wechat);

			Code.ok(mb, (id == null ? "新建" : "编辑") + "微信配置成功");
		} catch (Errors e) {
			Code.error(mb, e.getMessage());
		} catch (Exception e) {
			log.error("(Cooperation:add) error: ", e);
			Code.error(mb, (id == null ? "新建" : "编辑") + "微信配置失败");
		}

		return mb;
	}

	private void transSave(final Integer id, final ShopCompanyWechatConfig wechat) {
		Trans.exec(new Atom() {
			@Override
			public void run() {
				if (id != null) {
					dao.update(wechat);
					dao.clearLinks(wechat, "wechat");
				} else {
					dao.insert(wechat);
				}
				dao.insertRelation(wechat, "wechat");
			}
		});
	}
	
	private int initTemplet(String appid, String secret){
		Asserts.isNull(appid, "appId为空");
		Asserts.isNull(secret, "secret为空");
		String url = WechatConfigAction.Industry;
		//行业信息设置为院校和培训
		String outstr = "{\"industry_id1\":\"16\",\"industry_id2\":\"17\"}";
		Config c= new Config(appid, secret);
		String access_token = Access_TokenSingleton.getInstance().getMap(c).get(c.getAPPID()).get("access_token");
		if(access_token == null){
			return 1;
		}
		int result = QiyeWeixinUtil.PostMessage(access_token, "POST", url, outstr);
		return result;
	}

	
}
