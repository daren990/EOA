package cn.oa.web.action.wx;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.nutz.dao.Cnd;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cn.oa.model.Archive;
import cn.oa.repository.Mapper;


public class HrCoreService {
	public static String processRequest(String sMsg) throws Exception
	{
		// TODO: 解析出明文xml标签的内容进行处理
		// For example:
		String sendText = null;
		String text = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		StringReader sr = new StringReader(sMsg);
		InputSource is = new InputSource(sr);
		Document document = db.parse(is);

		Element root = document.getDocumentElement();
		// 得到根元素的所有子节点  
		NodeList ToUserName = root.getElementsByTagName("ToUserName");
		NodeList FromUserName = root.getElementsByTagName("FromUserName");
		NodeList CreateTime = root.getElementsByTagName("CreateTime");
		NodeList MsgType = root.getElementsByTagName("MsgType");
		NodeList AgentID = root.getElementsByTagName("AgentID");
		Map<String, String> map = new HashMap<String, String>();
		map.put("ToUserName", ToUserName.item(0).getTextContent());
		map.put("FromUserName", FromUserName.item(0).getTextContent());
		map.put("CreateTime", CreateTime.item(0).getTextContent());
		map.put("MsgType", MsgType.item(0).getTextContent());
		map.put("AgentID", AgentID.item(0).getTextContent());
		//判断接收方式为event
		if(map.get("MsgType").equals("event")){
			NodeList Event = root.getElementsByTagName("Event");
			map.put("Event", Event.item(0).getTextContent());
			//判断事件类型为click
			if(map.get("Event").equals("click")){
				NodeList EventKey = root.getElementsByTagName("EventKey");
				map.put("EventKey", EventKey.item(0).getTextContent());
				//判断事件KEY值，与自定义菜单接口中KEY值对应
				if(map.get("EventKey").equals("1")){
					text = "你好!\n";
					text += "1.输入(#员工ID)\n";
					text += "  ($工号)\n";
					text += "  (%姓名)\n";
					text += "  可以查询员工信息\n";
					text += "  如:#bingbingfan\n";
					text += "2.输入(!部门)\n";
					text += "  可以查询部门成员\n";
					text += "  如:!研发部\n";
					sendText = HrCoreService.STextMsg(map, text);	
					Mapper mapper = new Mapper();
					Archive archive = mapper.fetch(Archive.class, "Archive.query", Cnd.where("u.true_name", "=", "谢秋明"));
					String ar = archive.getJobNumber();
					System.out.println(ar+"  "+archive.getUserId());
				}
				if(map.get("EventKey").equals("2")){
					/*sendText = HrCoreService.STextMsg(map, "测试发送消息");*/					
				}
				if(map.get("EventKey").equals("3")){
					/*sendText = HrCoreService.STextMsg(map, "测试发送消息");	*/				
				}
			}
		}
		if(map.get("MsgType").equals("text")){
			/*NodeList Content = root.getElementsByTagName("Content");
			map.put("Content", Content.item(0).getTextContent());
			sendText = HrCoreService.STextMsg(map, "此功能未实现,稍过两天再试!");*/
		}
		System.out.println(sendText);
		return sendText;
	}
	//发送文本消息
	public static String STextMsg(Map<String, String> map,String content){
		String PostData = "<xml><ToUserName><![CDATA[%s]]></ToUserName><FromUserName><![CDATA[%s]]></FromUserName><CreateTime>%s</CreateTime><MsgType><![CDATA[%s]]></MsgType><Content><![CDATA[%s]]></Content></xml>";
		
		return String.format(PostData, map.get("FromUserName"),map.get("ToUserName"),map.get("CreateTime"),"text",content); 		
	}
}
