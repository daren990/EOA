package cn.oa.web.action.wx.comm;

import cn.oa.consts.Roles;
import cn.oa.utils.Asserts;

public class WxRoleUtil {

	public static String wxrole(String rolesName[]){
		if(Asserts.hasAny(Roles.GM.getName(),rolesName)){
			// 经理微助手
			return "1000006";
		} else if(Asserts.hasAny(Roles.SVI.getName(),rolesName)){
			// 员工微助手
			return "1000006";
		} else if(Asserts.hasAny(Roles.BOSS.getName(),rolesName)){
			// 总裁微助手
			return "1000006";
		} else {
			return "1000006";
		}
	}
}
