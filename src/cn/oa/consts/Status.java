package cn.oa.consts;

public interface Status {

	public static final int DISABLED = 0; 		// 禁用
	public static final int ENABLED = 1;		// 启用

	public static final int PROOFING = 0;		// 待审批
	public static final int APPROVED = 1;		// 已批准
	public static final int UNAPPROVED = -1;	// 未批准
	public static final int CHECKING = 2;		// 核对
	
	public static final int APPROVEDOK = 1;		// 流程完成已批准
	public static final int UNAPPROVEDOK = -1;	// 未批准
	
	public static final int OK = 99;			// 流程完成
	
	public static final int L = 0; //迟到/早退
	public static final int F = 1; //未打卡
	public static final int A = 2; //旷工
	public static final int ACCUMULATE = 3; //迟到/早退、旷工、未打卡累积
	
	public static final int Y = 0; //元
	public static final int D = 1; //天
	public static final int C = 0; //总额
	public static final int E = 1; //每次
	public static final int S = 0; //基本工资
	public static final int ALL = 1; //全额工资
	
	public static final int MOMENT = 1;		// 当前
	public static final int INTERIM = 2;		// 调薪过渡工资
	
	public static final String value = "-";
	
	public static final int UNUSED = 0;     		//闲置
	
	public static final int USER = 1;				//在使用
	
	public static final int SCRAP = -1;    			//报废
	
	public static final String PERSONADM = "personadm";   //行政部
	public static final String PERSONOUT = "personout";  //移出部门
	public static final String PERSONSIGN = "personsign";		//签收部门
	
	public static final String TYPE_OVERTIME = "type_overtime";//加班
	
	public static final String TYPE_REST = "rest";//补休
	
	public static final Integer LEAVE_TYPE_REST = 7;
	
}
