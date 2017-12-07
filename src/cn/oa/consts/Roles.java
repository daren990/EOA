package cn.oa.consts;

public enum Roles {

	ADM	("admin", "超级管理员"),
	BOSS("boss","总裁"),
	GM ("g.manager","总经理"),
	MAN("section.manager","部门经理"),
	MMOR("market.manager", "市场经理"),
	MADR("strative.manager", "行政人事经理"),
	MPSR("person.manager", "人事经理"),
	SVI("supervisor","部门主管"),
	SSVI("str.supervisor","行政主管"),
	MSVI("mar.supervisor","市场主管"),
	PSVI("per.supervisor","人事主管"),
	PSR	("personnel.operator", "人事专员"),
	MOR	("market.operator", "市场专员"),
	ADR ("administrative", "行政处理人"),
	FIM("finance.manager","财务经理"),
	CSVI("cashier.supervisor","出纳主管"),
	ASVI("account.supervisor","会计主管"),
	ACC	("accounting", "会计"),
	CAS	("cashier", "出纳"),
	ASM	("asset.manager", "资产管理员"),
	ASS	("assistant", "助理"),
	BGM	("budget.manager", "预算经理"),
	MGR	("manager", "上级"),
	FAP("firstApprove","第一审批人"),
	SAP("secondApprove","第二审批人"),
	PFM	("perform.manager", "绩效管理员"),
	WRR ("warn.operator", "故障处理人"),
	Me	("self", "本人")	,
	EMP("employee","普通员工"),
	FIS("finance.supervisor","财务主管"),
	SS("shift.manager","排班管理员");
	
	private final String name;
	private final String desc;

	private Roles(String name, String desc) {
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
		for (Roles role : Roles.values()) {
			if (role.getName().equals(name)) {
				desc = role.getDesc();
				break;
			}
		}
		return desc;
	}
}
