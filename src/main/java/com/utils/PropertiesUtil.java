package com.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

public class PropertiesUtil {
	private static final Properties pro = new Properties();
	static {
		try {
			ClassLoader cl = JdbcUtil.class.getClassLoader();
			InputStream in = cl.getResourceAsStream("services.properties");
			pro.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProp(String key) {
		return (String) pro.getProperty(key);
	}

	@Test
	public static void PUTest() {
		System.out.println("ccc====" + PropertiesUtil.getProp("service"));

	}
}
