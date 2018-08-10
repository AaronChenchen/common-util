package com.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Properties;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDBUtil{
	public static MongoDatabase mdb;

	public static MongoDatabase getMongoDatabase() {
		if (mdb != null) {
			return mdb;
		}
		try {
			// 用户名 数据库 密码
			String host=PropertiesUtil.getProp("host");
			int port=Integer.valueOf(PropertiesUtil.getProp("port"));
			String username=PropertiesUtil.getProp("username");
			String password=PropertiesUtil.getProp("password");
			String mongo_dbname=PropertiesUtil.getProp("mongo_dbname");
			
//			host=10.10.232.206
//			port=60060
//			username=admin
//			password=Yqlv4dQlLaBtCZ03NEbh
//			mongo_dbname=admin
			MongoCredential credential = MongoCredential.createCredential(username, mongo_dbname, password.toCharArray());
			// IP port
			ServerAddress addr = new ServerAddress(host, port);
			MongoClient client = new MongoClient(addr, Arrays.asList(credential));
			// 得到数据库
			mdb = client.getDatabase("database");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mdb;
	}
}
