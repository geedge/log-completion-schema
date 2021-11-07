package com.zdjizhi.utils.json;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.StringUtil;
import com.zdjizhi.utils.http.HttpClientUtil;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;

import java.util.*;

/**
 * 使用FastJson解析json的工具类
 *
 * @author qidaijie
 */
public class JsonParseUtil {

    private static final Log logger = LogFactory.get();

    private static ArrayList<String> dropList = new ArrayList<>();

    /**
     * 模式匹配，给定一个类型字符串返回一个类类型
     *
     * @param type 类型
     * @return 类类型
     */

    private static Class getClassName(String type) {
        Class clazz;

        switch (type) {
            case "int":
                clazz = Integer.class;
                break;
            case "string":
                clazz = String.class;
                break;
            case "long":
                clazz = long.class;
                break;
            case "array":
                clazz = List.class;
                break;
            case "double":
                clazz = double.class;
                break;
            case "float":
                clazz = float.class;
                break;
            case "char":
                clazz = char.class;
                break;
            case "byte":
                clazz = byte.class;
                break;
            case "boolean":
                clazz = boolean.class;
                break;
            case "short":
                clazz = short.class;
                break;
            default:
                clazz = String.class;
        }
        return clazz;
    }

    /**
     * 获取属性值的方法
     *
     * @param obj      对象
     * @param property key
     * @return 属性的值
     */
    public static Object getValue(Object obj, String property) {
        try {
            BeanMap beanMap = BeanMap.create(obj);
            return beanMap.get(property);
        } catch (RuntimeException e) {
            logger.error("获取json-value异常，异常key：" + property + "异常信息为：" + e);
            return null;
        }
    }

    /**
     * 获取属性值的方法
     *
     * @param jsonMap  原始日志
     * @param property key
     * @return 属性的值
     */
    public static Object getValue(Map<String, Object> jsonMap, String property) {
        try {
            return jsonMap.getOrDefault(property, null);
        } catch (RuntimeException e) {
            logger.error("获取json-value异常，异常key：" + property + "异常信息为：" + e);
            return null;
        }
    }

    /**
     * 更新属性值的方法
     *
     * @param jsonMap  原始日志json map
     * @param property 更新的key
     * @param value    更新的值
     */
    public static void setValue(Map<String, Object> jsonMap, String property, Object value) {
        try {
            jsonMap.put(property, value);
        } catch (RuntimeException e) {
            logger.error("赋予实体类错误类型数据", e);
        }
    }

    /**
     * 更新属性值的方法
     *
     * @param obj      对象
     * @param property 更新的key
     * @param value    更新的值
     */
    public static void setValue(Object obj, String property, Object value) {
        try {
            BeanMap beanMap = BeanMap.create(obj);
            beanMap.put(property, value);
        } catch (ClassCastException e) {
            logger.error("赋予实体类错误类型数据", e);
        }
    }

    /**
     * 根据反射生成对象的方法
     *
     * @param properties 反射类用的map
     * @return 生成的Object类型的对象
     */
    public static Object generateObject(Map properties) {
        BeanGenerator generator = new BeanGenerator();
        Set keySet = properties.keySet();
        for (Object aKeySet : keySet) {
            String key = (String) aKeySet;
            generator.addProperty(key, (Class) properties.get(key));
        }
        return generator.create();
    }

    /**
     * 通过获取String类型的网关schema链接来获取map，用于生成一个Object类型的对象
     *
     * @param http 网关schema地址
     * @return 用于反射生成schema类型的对象的一个map集合
     */
    public static HashMap<String, Class> getMapFromHttp(String http) {
        HashMap<String, Class> map = new HashMap<>(16);

        String schema = HttpClientUtil.requestByGetMethod(http);
        Object data = JSON.parseObject(schema).get("data");

        //获取fields，并转化为数组，数组的每个元素都是一个name doc type
        JSONObject schemaJson = JSON.parseObject(data.toString());
        JSONArray fields = (JSONArray) schemaJson.get("fields");

        for (Object field : fields) {
            String filedStr = field.toString();
            if (checkKeepField(filedStr)) {
                String name = JsonPath.read(filedStr, "$.name").toString();
                String type = JsonPath.read(filedStr, "$.type").toString();
                if (type.contains("{")) {
                    type = JsonPath.read(filedStr, "$.type.type").toString();
                }
                //组合用来生成实体类的map
                map.put(name, getClassName(type));
            } else {
                dropList.add(filedStr);
            }
        }
        return map;
    }

    /**
     * 判断字段是否需要保留
     *
     * @param message 单个field-json
     * @return true or false
     */
    private static boolean checkKeepField(String message) {
        boolean isKeepField = true;
        boolean isHiveDoc = JSON.parseObject(message).containsKey("doc");
        if (isHiveDoc) {
            boolean isHiveVi = JsonPath.read(message, "$.doc").toString().contains("visibility");
            if (isHiveVi) {
                String visibility = JsonPath.read(message, "$.doc.visibility").toString();
                if (FlowWriteConfig.VISIBILITY.equals(visibility)) {
                    isKeepField = false;
                }
            }
        }
        return isKeepField;
    }

    public static void dropJsonField(Map<String, Object> jsonMap) {
        for (String field : dropList) {
            jsonMap.remove(field);
        }
    }

    /**
     * 根据http链接获取schema，解析之后返回一个任务列表 (useList toList funcList paramlist)
     *
     * @param http 网关url
     * @return 任务列表
     */
    public static ArrayList<String[]> getJobListFromHttp(String http) {
        ArrayList<String[]> list = new ArrayList<>();

        String schema = HttpClientUtil.requestByGetMethod(http);
        //解析data
        Object data = JSON.parseObject(schema).get("data");

        //获取fields，并转化为数组，数组的每个元素都是一个name doc type
        JSONObject schemaJson = JSON.parseObject(data.toString());
        JSONArray fields = (JSONArray) schemaJson.get("fields");

        for (Object field : fields) {

            if (JSON.parseObject(field.toString()).containsKey("doc")) {
                Object doc = JSON.parseObject(field.toString()).get("doc");

                if (JSON.parseObject(doc.toString()).containsKey("format")) {
                    String name = JSON.parseObject(field.toString()).get("name").toString();
                    Object format = JSON.parseObject(doc.toString()).get("format");
                    JSONObject formatObject = JSON.parseObject(format.toString());

                    String functions = formatObject.get("functions").toString();
                    String appendTo = null;
                    String params = null;

                    if (formatObject.containsKey("appendTo")) {
                        appendTo = formatObject.get("appendTo").toString();
                    }

                    if (formatObject.containsKey("param")) {
                        params = formatObject.get("param").toString();
                    }


                    if (StringUtil.isNotBlank(appendTo) && StringUtil.isBlank(params)) {
                        String[] functionArray = functions.split(FlowWriteConfig.FORMAT_SPLITTER);
                        String[] appendToArray = appendTo.split(FlowWriteConfig.FORMAT_SPLITTER);

                        for (int i = 0; i < functionArray.length; i++) {
                            list.add(new String[]{name, appendToArray[i], functionArray[i], null});
                        }

                    } else if (StringUtil.isNotBlank(appendTo) && StringUtil.isNotBlank(params)) {
                        String[] functionArray = functions.split(FlowWriteConfig.FORMAT_SPLITTER);
                        String[] appendToArray = appendTo.split(FlowWriteConfig.FORMAT_SPLITTER);
                        String[] paramArray = params.split(FlowWriteConfig.FORMAT_SPLITTER);

                        for (int i = 0; i < functionArray.length; i++) {
                            list.add(new String[]{name, appendToArray[i], functionArray[i], paramArray[i]});

                        }
                    } else {
                        list.add(new String[]{name, name, functions, params});
                    }

                }
            }

        }
        return list;
    }

}