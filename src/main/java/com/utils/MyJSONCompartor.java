package com.utils;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class MyJSONCompartor {
    @SuppressWarnings("unchecked")
    public static void compare(String src, String target) {
        JSONObject so = JSON.parseObject(src);
        JSONObject to = JSON.parseObject(target);
        for (String key : so.keySet()) {
            Object value = so.get(key);
            Object v2 = to.get(key);
            if (value instanceof Map) {
                compareMap((Map<String, Object>) value, (Map<String, Object>) v2);
            } else if (value instanceof List) {
                compareList((List<Map<String, Object>>) value, (List<Map<String, Object>>) v2);
            } else {
                if (!value.equals(v2)) {
                    System.out.println("key " + key + "值不一样，期望" + value + "，实际" + v2);
                }
            }
        }
    }

    private static void compareList(List<Map<String, Object>> src, List<Map<String, Object>> target) {
        for (int i = 0; i < src.size(); i++) {
            compareMap(src.get(i), target.get(i));
        }
    }

    private static void compareMap(Map<String, Object> src, Map<String, Object> target) {
        for (String key : src.keySet()) {
            Object value = src.get(key);
            Object v2 = target.get(key);
            if (!value.equals(v2)) {
                System.out.println("key " + key + "值不一样，期望" + value + "，实际" + v2);
            }
        }
    }

    // public static void main(String[] args) {
    // Student s1 = new Student("s1", "男");
    // Student s2 = new Student("s2", "男");
    // Clazz c1 = new Clazz("1", "三年一班");
    // List<Student> students = new ArrayList<>();
    // students.add(s1);
    // students.add(s2);
    // c1.setStudents(students);
    //
    // Student s3 = new Student("s1", "男");
    // Student s4 = new Student("s2", "男");
    // Clazz c2 = new Clazz("1", "三年一班");
    // List<Student> students1 = new ArrayList<>();
    // students1.add(s3);
    // students1.add(s4);
    // c2.setStudents(students1);
    //
    // compare(JSON.toJSONString(c1), JSON.toJSONString(c2));
    // }
    
    
    public static void compareJSON(String src, String target) {
//    	System.out.println("预期结果==="+src);
//    	System.out.println("实际结果==="+target);
        JSONObject so = JSON.parseObject(src);
        JSONObject to = JSON.parseObject(target);
        for (String key : so.keySet()) {
            Object value = so.get(key);
            Object v2 = to.get(key);
            if (value instanceof Map) {
            	compareJSON(JSON.toJSONString(value), JSON.toJSONString(v2));
            } else if (value instanceof List) {
            	List list = (List<Map<String, Object>>) value;
            	List list2 = (List<Map<String, Object>>) v2;
                for (int i = 0; i < list.size(); i++) {
                	if (list.get(i) instanceof List || list.get(i) instanceof Map) {
                		compareJSON(JSON.toJSONString(list.get(i)), JSON.toJSONString(list2.get(i)));
					}else{//如果是这种 LIst，值不是Map也不是List,就可以直接比较值 "phoneNumList": ["10086"，"10084"]
		                if (!list.get(i).equals(list2.get(i))) {
		                    System.err.println("key " + key + "值不一样，期望" + list.get(i) + "，实际" + list2.get(i));
		                }
                	}
                }
            } else {
            	if (null==value) {
                    System.err.println("key " + key + "值是null！！！！！！！！！！！");
            	}else if (!value.equals(v2)) {
                    System.err.println("key " + key + "值不一样，期望" + value + "，实际" + v2);
                }
            }
        }
    }

    
    public static void main(String[] args) {
    	String expected= "{\"phoneNumList\": [\"10086\",\"10087\"],\"fullName\": \"10086\"}";
    	String actul= "{\"phoneNumList\": [\"10086\",\"10088\"],\"fullName\": \"10086\"}";
		compareJSON(expected, actul);

	}
}
