package cn.oa.web.wx.pojo;

public class CorpSendBase {
	/// UserID�б?��Ϣ�����ߣ�����������á�|���ָ��������������ָ��Ϊ@all�������ע����ҵӦ�õ�ȫ����Ա����
	private static String touser;
	/// PartyID�б?����������á�|���ָ�����touserΪ@allʱ���Ա�����
	private static String toparty;
	/// TagID�б?����������á�|���ָ�����touserΪ@allʱ���Ա�����
	private static String totag;
	/// ��Ϣ���� 
	private static String msgtype;
	/// ��ҵӦ�õ�id�����͡�����Ӧ�õ�����ҳ��鿴  
	private static String agentid;
	 /// ��ʾ�Ƿ��Ǳ�����Ϣ��0��ʾ��1��ʾ�ǣ�Ĭ��0   
	private static String safe;
	public static String getTouser() {
		return touser;
	}
	public static void setTouser(String touser) {
		CorpSendBase.touser = touser;
	}
	public static String getToparty() {
		return toparty;
	}
	public static void setToparty(String toparty) {
		CorpSendBase.toparty = toparty;
	}
	public static String getTotag() {
		return totag;
	}
	public static void setTotag(String totag) {
		CorpSendBase.totag = totag;
	}
	public static String getMsgtype() {
		return msgtype;
	}
	public static void setMsgtype(String msgtype) {
		CorpSendBase.msgtype = msgtype;
	}
	public static String getAgentid() {
		return agentid;
	}
	public static void setAgentid(String agentid) {
		CorpSendBase.agentid = agentid;
	}
	public static String getSafe() {
		return safe;
	}
	public static void setSafe(String safe) {
		CorpSendBase.safe = safe;
	}
	
}
