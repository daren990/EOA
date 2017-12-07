var ioc = {
	dataSource : {
		type : "org.apache.commons.dbcp.BasicDataSource",
		events : { depose : "close" },
		fields : {
			driverClassName : "com.mysql.jdbc.Driver",

			url : "jdbc:mysql://192.168.0.75/oa_producing?useUnicode=true&characterEncoding=utf-8",
			username : "shengyu",
			password : "Shengyu@123",
			
			initialSize : 5,

			maxActive : 50,
			maxIdle : 10,
			defaultAutoCommit : false,
			timeBetweenEvictionRunsMillis : 3600000,
			minEvictableIdleTimeMillis : 3600000
		}
	},
	dao : {
		type : "org.nutz.dao.impl.NutDao",
		args : [ { refer : "dataSource" } ],
		fields : { sqlManager : { refer : "sql" } }
	},
	sql : {
		type : "org.nutz.dao.impl.FileSqlManager",
		args : [ "cn/oa/model/mapper" ]
	},
	mapper : {
		type : "cn.oa.repository.Mapper",
		fields : { dao : { refer : "dao" } }
	},
	cache : {
		type : "cn.oa.app.cache.Ehcache",
		events : { create : "setup" }
	},
	snaker : {
		type : "cn.oa.app.workflow.Snaker",
		args : [ { refer : "dataSource" } ]
	},
	md5 : {
		type : "cn.oa.utils.encoder.Md5"
	},
	views : {
		type : "cn.oa.web.Views"
	},
}
