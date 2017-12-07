package cn.oa.utils.helper;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.snaker.engine.entity.Task;
import org.snaker.engine.model.CustomModel;
import org.snaker.engine.model.NodeModel;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.SubProcessModel;
import org.snaker.engine.model.TaskModel;
import org.snaker.engine.model.TransitionModel;

public class Jsons {
	
	public static String getActive(List<Task> tasks) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{'activeRects':{'rects':[");
		for (Task task : tasks) {
			buffer.append("{'paths':[],'name':'");
			buffer.append(task.getDisplayName());
			buffer.append("'},");
		}
		buffer.deleteCharAt(buffer.length() - 1);
		buffer.append("]}}");
		buffer.append("");
		buffer.append("");
		return buffer.toString();
	}

	public static String getModel(ProcessModel model) {
		StringBuffer buffer = new StringBuffer();
		List<TransitionModel> tms = new ArrayList<TransitionModel>();
		for (NodeModel node : model.getNodes()) {
			for (TransitionModel tm : node.getOutputs()) {
				tms.add(tm);
			}
		}
		buffer.append("{");
		buffer.append(getNode(model.getNodes()));
		buffer.append(getPath(tms));
		buffer.append("props:{props:{name:{name:'name',value:'");
		buffer.append(convert(model.getName()));
		buffer.append("'},displayName:{name:'displayName',value:'");
		buffer.append(convert(model.getDisplayName()));
		buffer.append("'},expireTime:{name:'expireTime',value:'");
		buffer.append(convert(model.getExpireTime()));
		buffer.append("'},instanceUrl:{name:'instanceUrl',value:'");
		buffer.append(convert(model.getInstanceUrl()));
		buffer.append("'},instanceNoClass:{name:'instanceNoClass',value:'");
		buffer.append(convert(model.getInstanceNoClass()));
		buffer.append("'}}}}");
		return buffer.toString();
	}

	public static String getNode(List<NodeModel> nodes) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("states: {");
		for (NodeModel node : nodes) {
			buffer.append(node.getName());
			buffer.append(getBase(node));
			buffer.append(getLayout(node));
			buffer.append(getProperty(node));
			buffer.append(",");
		}
		buffer.deleteCharAt(buffer.length() - 1);
		buffer.append("},");
		return buffer.toString();
	}

	public static String getPath(List<TransitionModel> tms) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("paths:{");
		for (TransitionModel tm : tms) {
			buffer.append(tm.getName());
			buffer.append(":{from:'");
			buffer.append(tm.getSource().getName());
			buffer.append("',to:'");
			buffer.append(tm.getTarget().getName());
			buffer.append("', dots:[");
			if (StringUtils.isNotEmpty(tm.getG())) {
				String[] bendpoints = tm.getG().split(";");
				for (String bendpoint : bendpoints) {
					buffer.append("{");
					String[] xy = bendpoint.split(",");
					buffer.append("x:").append(getNumber(xy[0]));
					buffer.append(",y:").append(xy[1]);
					buffer.append("},");
				}
				buffer.deleteCharAt(buffer.length() - 1);
			}
			buffer.append("],text:{text:'");
			buffer.append(tm.getDisplayName());
			buffer.append("'},textPos:{");
			if (StringUtils.isNotEmpty(tm.getOffset())) {
				String[] values = tm.getOffset().split(",");
				buffer.append("x:").append(values[0]).append(",");
				buffer.append("y:").append(values[1]).append("");
			}
			buffer.append("}, props:{name:{value:'" + tm.getName() + "'}}}");
			buffer.append(",");
		}
		buffer.deleteCharAt(buffer.length() - 1);
		buffer.append("},");
		return buffer.toString();
	}

	private static String getBase(NodeModel node) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(":{type:'");
		String nodeClass = node.getClass().getSimpleName().toLowerCase();
		buffer.append(nodeClass.substring(0, nodeClass.length() - 5));
		buffer.append("',text:{text:'");
		buffer.append(node.getDisplayName());
		buffer.append("'},");
		return buffer.toString();
	}

	private static String getProperty(NodeModel node) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("props:{");
		try {
			PropertyDescriptor[] beanProperties = PropertyUtils.getPropertyDescriptors(node);
			for (PropertyDescriptor propertyDescriptor : beanProperties) {
				if (propertyDescriptor.getReadMethod() == null || propertyDescriptor.getWriteMethod() == null)
					continue;
				String name = propertyDescriptor.getName();
				String value = "";
				if (propertyDescriptor.getPropertyType() == String.class) {
					value = (String) BeanUtils.getProperty(node, name);
				} else {
					continue;
				}
				if (value == null || value.equals(""))
					continue;
				buffer.append(name);
				buffer.append(":{value:'");
				buffer.append(convert(value));
				buffer.append("'},");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		buffer.deleteCharAt(buffer.length() - 1);
		buffer.append("}}");
		return buffer.toString();
	}

	private static String getLayout(NodeModel node) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("attr:{");
		String[] values = node.getLayout().split(",");
		buffer.append("x:").append(getNumber(values[0])).append(",");
		buffer.append("y:").append(values[1]).append(",");
		if ("-1".equals(values[2])) {
			if (node instanceof TaskModel || node instanceof CustomModel || node instanceof SubProcessModel) {
				values[2] = "100";
			} else {
				values[2] = "50";
			}
		}
		if ("-1".equals(values[3])) {
			values[3] = "50";
		}
		buffer.append("width:").append(values[2]).append(",");
		buffer.append("height:").append(values[3]);
		buffer.append("},");
		return buffer.toString();
	}

	private static String convert(String value) {
		if (StringUtils.isEmpty(value)) return "";
		if (value.indexOf("'") != -1) { value = value.replaceAll("'", "#1"); }
		if (value.indexOf("\"") != -1) { value = value.replaceAll("\"", "#2"); }
		if (value.indexOf("\r\n") != -1) { value = value.replaceAll("\r\n", "#3"); }
		if (value.indexOf("\n") != -1) { value = value.replaceAll("\n", "#4"); }
		if (value.indexOf(">") != -1) { value = value.replaceAll(">", "#5"); }
		if (value.indexOf("<") != -1) { value = value.replaceAll("<", "#6"); }
		return value;
	}

	public static String convertXml(String value) {
		if (StringUtils.isEmpty(value)) return "";
		if (value.indexOf("#1") != -1) { value = value.replaceAll("#1", "'"); }
		if (value.indexOf("#2") != -1) { value = value.replaceAll("#2", "\""); }
		if (value.indexOf("#5") != -1) { value = value.replaceAll("#5", "&gt;"); }
		if (value.indexOf("#6") != -1) { value = value.replaceAll("#6", "&lt;"); }
		return value;
	}

	private static int getNumber(String value) {
		if (value == null) return 0;
		try {
			return Integer.parseInt(value) + 180;
		} catch (Exception e) {
			return 0;
		}
	}
}
