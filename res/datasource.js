var ioc = {
	dataSource : {
		type : "org.apache.commons.dbcp.BasicDataSource",
		fields : {
			driverClassName : "com.mysql.jdbc.Driver",
			url : "jdbc:mysql://192.168.0.5/oa?useUnicode=true&characterEncoding=utf-8",
//			username : "root",
//			password : "root",
			username : "root",
			password : "123456",
			initialSize : 5,
			maxActive : 50,
			maxIdle : 10,
			defaultAutoCommit : false,
			timeBetweenEvictionRunsMillis : 3600000,
			minEvictableIdleTimeMillis : 3600000
		}
	},
	snaker : {
		type : "cn.oa.app.sql.Snaker",
		args : [ { refer : "dataSource" } ]
	}
}