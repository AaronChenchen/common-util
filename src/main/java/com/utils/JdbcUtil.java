package com.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

//工具类
public class JdbcUtil {

	private static String driverClass;
	private static String url;
	private static String user;
	private static String password;
	public static Connection conn;
	public static Connection getConnection(String url) {
//		if (conn != null) {
//			return conn;
//		}
		try {
			ClassLoader cl = JdbcUtil.class.getClassLoader();
			InputStream in = cl.getResourceAsStream("dbcfg.properties");
			Properties props = new Properties();
			props.load(in);
			driverClass = props.getProperty("driverClass");
			url = props.getProperty(url);
			user = props.getProperty("user");
			password = props.getProperty("password");
			Class.forName(driverClass);
			conn = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return conn;
	}

	public static void release(ResultSet rs, Statement stmt, Connection conn) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			rs = null;
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			stmt = null;
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			conn = null;
		}
	}

}
