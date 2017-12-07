package cn.oa.app.workflow;

import java.sql.Connection;
import java.sql.SQLException;

import org.nutz.trans.Trans;
import org.snaker.engine.access.jdbc.JdbcAccess;

public class NutAccess extends JdbcAccess {
	
	protected Connection getConnection() throws SQLException {
		return Trans.getConnectionAuto(dataSource);
	}
}
