package cn.oa.utils.lang;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.dom4j.DocumentException;



public class ParseXml {

	@SuppressWarnings("unchecked")
	public static Map<String, String> parseXml(HttpServletRequest req) throws IOException, DocumentException  {
		
		Map<String, String> map = new HashMap<String, String>();
		/*InputStream inputStream = req.getInputStream();
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputStream);
		Element root = document.getRootElement();
		
		List<Element> elementList = root.elements();

		
		for (Element e : elementList)
			map.put(e.getName(), e.getText());

		
		inputStream.close();
		inputStream = null;*/
		
	
		

		return map;
	}
	public static String input2String(InputStream is) throws IOException{
		 StringBuffer out = new StringBuffer(); 
	        byte[] b = new byte[4096]; 
	        for(int n; (n = is.read(b)) != -1;)   { 
	           out.append(new   String(b,   0,   n)); 
	        } 
	        return out.toString();
	}
}
