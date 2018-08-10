package com.utils;

import java.awt.List;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.junit.Test;

public class ExcelUtils2 {

	/**
	 * 
	 * @param filePath
	 * @param ignoreRows
	 * @param columns 读取的列，数组
	 * @param sheetName
	 * @return 结果返回的是两层的map，第一层的key是列的维度，第二层是当前这列的行的维度
	 */
	public static Map<Integer, Map<Integer, Object>> getData(String filePath, int ignoreRows, int[] columns,
			String sheetName) {
		Map<Integer, Map<Integer, Object>> mapsss = new HashMap<Integer, Map<Integer, Object>>();
		File file = new File(filePath);
		
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			POIFSFileSystem fs = new POIFSFileSystem(in);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFCell cell = null;
			HSSFSheet st = wb.getSheet(sheetName);
			for (int column = 0; column < columns.length; column++) {
				Map<Integer, Object> result = new HashMap<Integer, Object>();
				// 第一行为标题，不取
				for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
					HSSFRow row = st.getRow(rowIndex);
					if (row == null) {
						continue;
					}
					cell = row.getCell(columns[column]);
					if (null != cell) { 
						result.put(rowIndex, cell);
					}
				}
				mapsss.put(columns[column], result);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapsss;
	}

	/**
	 * 
	 * @param filePath
	 * @param ignoreRows
	 * @param columns 要写入的列，与list相对应
	 * @param sheetName
	 * @param list 写入列的结果，list里是Map类型，Map是行的维度
	 */
	public static void setData(String filePath, int ignoreRows, int[] columns, String sheetName,
			ArrayList<Map<Integer, Object>> list) {
		File file = new File(filePath);
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			POIFSFileSystem fs = new POIFSFileSystem(in);
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFCell cell = null;
			HSSFSheet st = wb.getSheet(sheetName);
			 HSSFCellStyle cellStyle=wb.createCellStyle();
			 cellStyle.setWrapText(true);
			// 第一行为标题，不取
			for (int column = 0; column < columns.length; column++) {
				Map<Integer, Object> map= new HashMap<Integer, Object>();
				map= list.get(column);
				for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
					HSSFRow row = st.getRow(rowIndex);
					if (row == null) {
						st.createRow(rowIndex);
					}
					cell = row.getCell(columns[column], MissingCellPolicy.CREATE_NULL_AS_BLANK);
					cell.setCellStyle(cellStyle);
					System.out.println(rowIndex+"=========================="+columns[column]);
					if(map.get(rowIndex)!=null){
						cell.setCellValue(map.get(rowIndex).toString());
					}
				}
			}
			in.close();
			FileOutputStream fos = new FileOutputStream(file);
			System.out.println(file.getPath());
			wb.write(fos);
			fos.close();// 关闭文件输出流

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void excelTest() {
		
		String filePath = ExcelUtils2.class.getClassLoader().getResource("test.xlsx").getPath();
		System.out.println("filepath======="+filePath);
		Map<Integer, Map<Integer, Object>> map = ExcelUtils2.getData(filePath, 1, new int[] { 0, 1, 2 }, "Sheet1");
		Iterator ite = map.entrySet().iterator();
		System.out.println("map.entrySet().size()===" + map.entrySet().size());

		while (ite.hasNext()) {
			Map.Entry entry = (Map.Entry) ite.next();
			System.out.println();
			System.out.println("1111111111----------" + entry.getKey());
			System.out.println(entry.getValue());
		}

		ArrayList<Map<Integer, Object>> list=new ArrayList<Map<Integer, Object>>();
		
		for (int key : map.keySet()) {
			list.add(map.get(key));
		}

		ExcelUtils2.setData(filePath, 1, new int[]{4,5}, "Sheet1",list);

	}

}
