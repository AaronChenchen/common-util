package com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import com.sun.corba.se.spi.orb.StringPair;

public class DatabaseOperationsUtils{
	static Logger logger = Logger.getLogger(DatabaseOperationsUtils.class);
	private static String databaseURLnameInproperty="url_db";
	public static IDatabaseConnection getConnection() {
		java.sql.Connection con = JdbcUtil.getConnection(databaseURLnameInproperty);
		try {
			IDatabaseConnection connection = new DatabaseConnection(con);// DbUnit将jdbcConnection对象封装成自身的通用数据库接口对象IDatabaseConnection
			DatabaseConfig config = connection.getConfig();
			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
			return connection;
		} catch (DatabaseUnitException e) {
			e.printStackTrace();
		}
		return null;

	}
	
	/**
	 * 插入数据库
	 * @param sourceFolder
	 * @return
	 */
    public static boolean importToTable(File sourceFolder){
        try{
            IDataSet dataSet = new CsvDataSet(sourceFolder);
            //准备读入数据
            DatabaseOperation.CLEAN_INSERT.execute(getConnection(), dataSet);
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
      }

	/**
	 * 从数据库查询某些字段得到data写到响应的文件里，不是整个表
	 * @param tablenames	表名+查询的具体sql
	 * @param filePath	文件路径
	 * @param fileName	文件名
	 * @throws Exception
	 */
	public static void getTableDataBySql(List<StringPair> tablenames, String filePath, String fileName) throws Exception {
		if (tablenames.size() == 0) {
			logger.info("请传入要备份的数据库表名");
			return;
		}

		QueryDataSet backupDataSet = new QueryDataSet(getConnection());
		for (StringPair tablename : tablenames) {
			backupDataSet.addTable(tablename.getFirst(), tablename.getSecond());// 将数据库中要备份的表放入数据集中
		}
		File dir = new File(filePath);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Exception("目录不存在，创建目录失败");
			}
		}
		FileOutputStream fileOutputStream = null;
		// File file = File.createTempFile("database_backup", ".xls", dir);//
		// 创建一个文件用于保存备份数据
		File file = new File(filePath+File.separator+fileName);
		file.createNewFile();
		try {
			fileOutputStream = new FileOutputStream(file);
			XlsDataSet.write(backupDataSet, fileOutputStream);// 将备份集写入new出的文件中
//			CsvDataSetWriter.write(backupDataSet, file);
			logger.info("数据成功备份到----->" + file);
		} catch (DataSetException e) {
			logger.info("数据成功备份失败");
			e.printStackTrace();
		} finally {
			fileOutputStream.close();
		}

	}

    /**
     * Write the specified dataset to the specified Excel document.
     */
	/**
	 * 从数据库查询某些字段得到data写到响应的文件里，不是整个表
	 * @param tablenames	表名+查询的具体sql
	 * @param filePath	文件路径
	 * @param fileName	文件名
	 * @throws Exception
	 */
	public static void getTableDataToCSVBySql(List<StringPair> tablenames, String filePath) throws Exception {
		if (tablenames.size() == 0) {
			logger.info("请传入要备份的数据库表名");
			return;
		}

		QueryDataSet backupDataSet = new QueryDataSet(getConnection());
		for (StringPair tablename : tablenames) {
			backupDataSet.addTable(tablename.getFirst(), tablename.getSecond());// 将数据库中要备份的表放入数据集中
		}
		File dir = new File(filePath);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Exception("目录不存在，创建目录失败");
			}
		}
		FileOutputStream fileOutputStream = null;
		// 创建一个文件用于保存备份数据
		File file = new File(filePath);
		file.createNewFile();
		try {
			CsvDataSetWriter.write(backupDataSet, file);
			logger.info("数据成功备份到----->" + file);
		} catch (DataSetException e) {
			logger.info("数据成功备份失败");
			e.printStackTrace();
		} 
	}

	/**
	 * 从数据库查询data写到响应的文件里，整个表的数据
	 * @param tablenames	表名
	 * @param filePath	文件路径
	 * @param fileName	文件名
	 * @throws Exception
	 */
	public static void getTableData(List<String> tablenames, String filePath, String fileName) throws Exception {
		if (tablenames.size() == 0) {
			logger.info("请传入要备份的数据库表名");
			return;
		}

		QueryDataSet backupDataSet = new QueryDataSet(getConnection());
		for (String tablename : tablenames) {
			backupDataSet.addTable(tablename);// 将数据库中要备份的表放入数据集中
		}
		File dir = new File(filePath);
		if (!dir.exists()) {
			if (!dir.mkdirs()) { // mkdir()当且仅当已创建目录时，返回 true；否则返回 false
				throw new Exception("目录不存在，创建目录失败");
			}
		}
		FileOutputStream fileOutputStream = null;
		// File file = File.createTempFile("database_backup", ".xls", dir);//
		// 创建一个临时文件用于保存备份数据
		File file = new File(filePath + fileName);
		file.createNewFile();
		try {
			fileOutputStream = new FileOutputStream(file);
			XlsDataSet.write(backupDataSet, fileOutputStream);// 将备份集写入new出的文件中
			logger.info("数据成功备份到----->" + file);
		} catch (DataSetException e) {
			logger.info("数据成功备份失败");
			e.printStackTrace();
		} finally {
			fileOutputStream.close();
		}

	}

	public void deleteTables(List<String> tablenames) throws Exception {
    		java.sql.Connection conn = JdbcUtil.getConnection(databaseURLnameInproperty);
	            //2.建立连接  
	            conn=JdbcUtil.getConnection(databaseURLnameInproperty);  
	            //3.创建语句  
	            Statement st=conn.createStatement();  
	            //4.执行语句  
	            for(String tablename:tablenames){
		            String sql="delete from "+tablename;  
		            st.executeUpdate(sql); 
	            }
	            st.close();
	            conn.close();

	}

	/**
	 * 从文件写入数据库
	 * @param filePrepare
	 * @throws Exception
	 */
	public static void cleanAllAndInsertToDatabase(String filePrepare) throws Exception {
		if ("".equals(filePrepare)) {
			return;
		}
		File file = new File(filePrepare);
		// XlsDataSet是装载测试数据的
		// 将准备的数据读到数据集中
		IDataSet dataset = new XlsDataSet(new FileInputStream(file));
		// DatabaseOperation.DELETE_ALL.execute(getConnection(), dataset);
		// DatabaseOperation.INSERT.execute(getConnection(), dataset);
		// 删除表中所有数据再插入数据集中的数据该方法可以循环处理多张table
		DatabaseOperation.CLEAN_INSERT.execute(getConnection(), dataset);
	}
	
	
	//
	public static boolean checkHasData(String sql){
		java.sql.Connection conn = JdbcUtil.getConnection(databaseURLnameInproperty);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			// 执行查询，用ResultSet类的对象，返回查询的结果
			// sql="select * from credit_report_customer_info where apply_no = 123";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	
	@Test
	public void test() throws Exception {
		List<StringPair> tablenames = new ArrayList<StringPair>();
		tablenames.add(new StringPair("tablename",
				"select colum1,colum2,colum3 from tablename"));
		tablenames.add(new StringPair("tablename2",
				"select colum1,colum2,colum3 from tablename2"));
		DatabaseOperationsUtils databaseOperations = new DatabaseOperationsUtils();
		databaseOperations.getTableDataBySql(tablenames, "E:/TestCases/goldenFiles/", "test17.xls");
	}
	
	

}
