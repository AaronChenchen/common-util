package com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class ExcelUtilsNew {

	/**
	 * 
	 * @param filePath
	 * @param ignoreRows
	 * @param columns    读取的列，数组
	 * @param sheetName
	 * @return 结果返回的是两层的map，第一层的key是列的维度，第二层是当前这列的行的维度
	 */
	public static Map<Integer, Map<Integer, Object>> getData(String filePath, int ignoreRows, int[] columns,
			String sheetName) {
		Map<Integer, Map<Integer, Object>> mapsss = new HashMap<Integer, Map<Integer, Object>>();
		File file = new File(filePath);
		Workbook wb = null;

		try {
			InputStream in = new FileInputStream(file);
			boolean isExcel2003 = file.getName().toLowerCase().endsWith("xls") ? true : false;
			if (isExcel2003) {
				wb = new HSSFWorkbook(in);
			} else {
				wb = new XSSFWorkbook(in);
			}
			Cell cell = null;
			Sheet st = wb.getSheet(sheetName);
			for (int column = 0; column < columns.length; column++) {
				Map<Integer, Object> result = new HashMap<Integer, Object>();
				// 第一行为标题，不取
				for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
					Row row = st.getRow(rowIndex);
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
	 * @param columns    要写入的列，与list相对应
	 * @param sheetName
	 * @param list       写入列的结果，list里是Map类型，Map是行的维度
	 */
	public static void setData(String filePath, int ignoreRows, int[] columns, String sheetName,
			ArrayList<Map<Integer, Object>> list) {
		File file = new File(filePath);
		Workbook wb = null;
		try {
			FileInputStream in = new FileInputStream(file);
			boolean isExcel2003 = file.getName().toLowerCase().endsWith("xls") ? true : false;
			if (isExcel2003) {
				wb = new HSSFWorkbook(in);
			} else {
				wb = new XSSFWorkbook(in);
			}
			Cell cell = null;
			Sheet st = wb.getSheet(sheetName);
			CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setWrapText(true);
			// 第一行为标题，不取
			for (int column = 0; column < columns.length; column++) {
				Map<Integer, Object> map = new HashMap<Integer, Object>();
				map = list.get(column);
				for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++) {
					Row row = st.getRow(rowIndex);
					if (row == null) {
						st.createRow(rowIndex);
					}
					cell = row.getCell(columns[column], MissingCellPolicy.CREATE_NULL_AS_BLANK);
					cell.setCellStyle(cellStyle);
					System.out.println(rowIndex + "==========================" + columns[column]);
					if (map.get(rowIndex) != null) {
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

		String filePath = ExcelUtilsNew.class.getClassLoader().getResource("test.xlsx").getPath();
		System.out.println("filepath=======" + filePath);
		Map<Integer, Map<Integer, Object>> map = ExcelUtilsNew.getData(filePath, 1, new int[] { 0, 1, 2 }, "Sheet1");
		Iterator ite = map.entrySet().iterator();
		System.out.println("map.entrySet().size()===" + map.entrySet().size());

		while (ite.hasNext()) {
			Map.Entry entry = (Map.Entry) ite.next();
			System.out.println();
			System.out.println("1111111111----------" + entry.getKey());
			System.out.println(entry.getValue());
		}

		ArrayList<Map<Integer, Object>> list = new ArrayList<Map<Integer, Object>>();

		for (int key : map.keySet()) {
			list.add(map.get(key));
		}

		ExcelUtilsNew.setData(filePath, 1, new int[] { 4, 5 }, "Sheet1", list);

	}

}
