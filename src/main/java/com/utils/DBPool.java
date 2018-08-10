package com.utils;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBPool {
 
 private static DBPool dbPool;
 private ComboPooledDataSource dataSource;
 
 static {
  dbPool = new DBPool();
 }
 
 
 public DBPool() {
  try {
   dataSource = new ComboPooledDataSource();
   dataSource.setUser("root");
   dataSource.setPassword("123456");
   dataSource
     .setJdbcUrl("jdbc:mysql://localhost:3306/jwdb");
   dataSource.setDriverClass("com.mysql.jdbc.Driver");
   dataSource.setInitialPoolSize(2);
   dataSource.setMinPoolSize(1);
   dataSource.setMaxPoolSize(10);
   dataSource.setMaxStatements(50);
   dataSource.setMaxIdleTime(60);
  } catch (PropertyVetoException e) {
   throw new RuntimeException(e);
  }
 }
 
 
 public final static DBPool getInstance() {
  return dbPool;
 }
 
 public final Connection getConnection() {
  try {
   return dataSource.getConnection();
  } catch (SQLException e) {
   throw new RuntimeException("无法从数据源获取连接 ", e);
  }
 }
 
 
 public static void main(String[] args) throws SQLException {  
 
//直接获取数据的测试，跟jdbc类似。
/*  Connection con = null;
  String sql="select agent_id from agent";
  PreparedStatement ps;
  //List list=new ArrayList();
  try {
   con = DBPool.getInstance().getConnection();
   ps=con.prepareStatement(sql);
   ResultSet rs=ps.executeQuery();
   while(rs.next()){
    System.out.println(rs.getString("agent_id"));
   }
  } catch (Exception e) {
  } finally {
   if (con != null)
    con.close();
  }
  */
  
  
  System.out.println("使用连接池................................");
  for (int i = 0; i < 20; i++) {
   long beginTime = System.currentTimeMillis();
   Connection conn = DBPool.getInstance().getConnection();
   try {
    PreparedStatement pstmt = conn
      .prepareStatement("SELECT * FROM netbar");
    ResultSet rs = pstmt.executeQuery();
    while (rs.next()) {
    }
   } catch (SQLException e) {
    e.printStackTrace();
   } finally {
    try {
     conn.close();
    } catch (SQLException e) {
     e.printStackTrace();
    }
   }
   long endTime = System.currentTimeMillis();
   System.out.println("第" + (i + 1) + "次执行花费时间为:"
     + (endTime - beginTime));
  }
 
 

  System.out.println("不使用连接池................................");
  for (int i = 0; i < 20; i++) {
   long beginTime = System.currentTimeMillis();
   try {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
   } catch (Exception e1) {
    e1.printStackTrace();
   }
   String url = "jdbc:mysql://localhost:3306/jwdb";
   String user = "root";
   String password = "123456";
   Connection conn = DriverManager.getConnection(url, user, password);
   try {
    PreparedStatement pstmt = conn
      .prepareStatement("SELECT * FROM netbar");
    ResultSet rs = pstmt.executeQuery();
    while (rs.next()) {
     // do nothing...
    }
   } catch (SQLException e) {
    e.printStackTrace();
   } finally {
    try {
     conn.close();
    } catch (SQLException e) {
     e.printStackTrace();
    }
   }
   long endTime = System.currentTimeMillis();
   System.out.println("第" + (i + 1) + "次执行花费时间为:"
     + (endTime - beginTime));
  }
 }
 }