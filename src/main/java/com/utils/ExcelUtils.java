package com.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.junit.Test;

public class ExcelUtils {

	/**
	 * 读取第clomns列的数据，返回这一列每一行的结果
	 * @param filePath
	 * @param ignoreRows
	 * @param columns
	 * @param sheetName
	 * @return
	 */
	public static Map<Integer,Object> getData(String filePath, int ignoreRows,int columns,String sheetName){
		File file = new File(filePath);
		Map<Integer,Object> result = new HashMap<Integer,Object>();
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			POIFSFileSystem fs = new POIFSFileSystem(in);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFCell cell = null;
			HSSFSheet st = wb.getSheet(sheetName);
			// 第一行为标题，不取
			for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
				HSSFRow row = st.getRow(rowIndex);
				if (row == null) {
					continue;
				}
				cell=row.getCell(columns);
				if (null!=cell) {
					result.put(rowIndex, cell);
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void setData(String filePath, int ignoreRows,int columns,String sheetName,Map<Integer,Object> data){
		File file = new File(filePath);
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			POIFSFileSystem fs = new POIFSFileSystem(in);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFCell cell = null;
			HSSFSheet st = wb.getSheet(sheetName);
//			HSSFCellStyle cellStyle=wb.createCellStyle();    
//			cellStyle.setWrapText(true);    
			// 第一行为标题，不取
			for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
				HSSFRow row = st.getRow(rowIndex);
				if (row == null) {
					continue;
				}
				cell=row.getCell(columns,MissingCellPolicy.CREATE_NULL_AS_BLANK);
//				cell.setCellStyle(cellStyle);
				cell.setCellValue(data.get(rowIndex).toString());
			}
			in.close();
			FileOutputStream fos = new FileOutputStream(file);
			wb.write(fos);
			fos.close();// 关闭文件输出流

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void excelTest(){
		String filePath = ExcelUtils2.class.getClassLoader().getResource("test.xls").getPath();
		Map<Integer,Object> result =ExcelUtils.getData(filePath, 1, 4, "Sheet1");
		Iterator ite = result.entrySet().iterator();
		while(ite.hasNext()){
			  Map.Entry entry = (Map.Entry) ite.next();  
			  System.out.println(entry.getKey());  
			  System.out.println(entry.getValue());  
			}  
		
//		ExcelUtils.setData(filePath, 1, 2, "Sheet1",result);

	}

	

}
