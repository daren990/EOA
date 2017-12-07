package test;

import org.junit.Test;
import org.nutz.log.Log;
import org.nutz.log.Logs;

public class LogTest {

	public static Log log = Logs.getLog(LogTest.class);

	@Test
	public void test() {
		double a = 4550;
		int b = 21;
		double c = a/b;
		double d = c*b;
		System.out.println(c + "=" + d);
		log.info("log test");
		log.infof("%s:%s", "key", "value");
	}
}
