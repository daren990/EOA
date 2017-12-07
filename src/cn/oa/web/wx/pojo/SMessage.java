package cn.oa.web.wx.pojo;

/**  
* 发送消息类  
*/  
public class SMessage {  
    //发送接口  
    public static String POST_URL = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=ACCESS_TOKEN";        
    /**
     * news消息
     * @param touser UserID列表（消息接收者，多个接收者用‘|’分隔）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送————"touser": "UserID1|UserID2|UserID3"
     * @param toparty PartyID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数————"toparty": " PartyID1 | PartyID2 "
     * @param totag TagID列表，多个接受者用‘|’分隔。当touser为@all时忽略本参数————"totag": " TagID1 | TagID2 "
     * @param msgtype 消息类型，此时固定为：news
     * @param agentid 企业应用的id，整型。可在应用的设置页面查看
     * @param articlesList 图文集合
     */  
    public static String SNewsMsg(String touser,String toparty,String totag,String agentid , String articlesList){
    	if(touser.equals("\"@all\"")){
    		String postData = "{\"touser\":%s,\"msgtype\":\"news\",\"agentid\":%s,\"news\":{\"articles\":%s}}";  
    		return String.format(postData,touser,agentid,articlesList);
    	}
        String postData = "{\"touser\":%s,\"toparty\":%s,\"totag\":%s,\"msgtype\":\"news\",\"agentid\":%s,\"news\":{\"articles\":%s}}";  
        return String.format(postData,touser,toparty,totag,agentid,articlesList);  
    }  
      
}
