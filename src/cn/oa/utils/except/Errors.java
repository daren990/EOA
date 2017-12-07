package cn.oa.utils.except;

public class Errors extends RuntimeException {

	private static final long serialVersionUID = 8817312172666605547L;

	public Errors() {
		super();
	}

	public Errors(String message) {
		super(message);
	}

	public Errors(Throwable cause) {
		super(cause);
	}

	public Errors(String message, Throwable cause) {
		super(message, cause);
	}
}
