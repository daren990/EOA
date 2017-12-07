package cn.oa.consts;

public enum RoleMap {

	ADM	("admin", "超级管理员"),
	ASS	("assistant", "助理"),
	PFM	("perform.manager", "绩效经理"),
	BGM	("budget.manager", "预算经理"),
	MGR	("manager", "上级"),
	ACC	("accounting", "会计"),
	CAS	("cashier", "出纳"),
	WRN ("warn.operator", "故障处理人"),
	Me	("self", "本人")	;
	
	private final String name;
	private final String desc;

	private RoleMap(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public static final String getDesc(String name) {
		String desc = "";
		for (RoleMap role : RoleMap.values()) {
			if (role.getName().equals(name)) {
				desc = role.getDesc();
				break;
			}
		}
		return desc;
	}
}
