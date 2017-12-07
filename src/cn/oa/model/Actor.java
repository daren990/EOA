package cn.oa.model;

public class Actor {

	private String operator;
	private String content;
	private String modifyTime;

	public Actor(String operator, String content, String modifyTime) {
		this.operator = operator;
		this.content = content;
		this.modifyTime = modifyTime;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}
}
