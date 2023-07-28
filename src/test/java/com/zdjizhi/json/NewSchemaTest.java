package com.zdjizhi.json;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.common.FlowWriteConfig;
import org.junit.Test;

import java.util.*;

/**
 * Applicable to schemas >= TSG22.08
 *
 * @author qidaijie
 * @Package com.zdjizhi.nacos
 * @Description:
 * @date 2022/3/1714:57
 */
public class NewSchemaTest {

    private static Properties properties = new Properties();

    /**
     * 获取需要删除字段的列表
     */
    private static ArrayList<String> dropList = new ArrayList<>();

    /**
     * 获取schema指定的有效字段及类型
     */
    private static HashMap<String, Class> jsonFieldsMap;

    /**
     * 获取包含默认值的字段
     */
    private static HashMap<String, Object> defaultFieldsMap = new HashMap<>(16);




    static {
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, "192.168.44.12:8848");
        properties.setProperty(PropertyKeyConst.NAMESPACE, "test");
        properties.setProperty(PropertyKeyConst.USERNAME, "nacos");
        properties.setProperty(PropertyKeyConst.PASSWORD, "nacos");
    }


    @Test
    public void newSchemaTest() {
        try {

            ConfigService configService = NacosFactory.createConfigService(properties);
            String dataId = "session_record.json";
            String group = "Galaxy";
            String schema = configService.getConfig(dataId, group, 5000);

            ArrayList<String[]> newJobList = getNewJobList(schema);
            for (String[] job : newJobList) {
                System.out.println(Arrays.toString(job));
            }

            HashMap<String, Class> fieldsFromSchema = getFieldsFromSchema(schema);
            for (String key : fieldsFromSchema.keySet()) {
                System.out.println("fileName:" + key + " Class:" + fieldsFromSchema.get(key));

            }
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }


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
     * 通过schema来获取所需的字段及字段类型。
     *
     * @return 用于反射生成schema类型的对象的一个map集合
     */
    private static HashMap<String, Class> getFieldsFromSchema(String schema) {
        HashMap<String, Class> map = new HashMap<>(256);

        //获取fields，并转化为数组，数组的每个元素都是一个name doc type
        com.alibaba.fastjson2.JSONObject schemaJson = com.alibaba.fastjson2.JSONObject.parseObject(schema);
        com.alibaba.fastjson2.JSONArray fields = schemaJson.getJSONArray("fields");

        for (Object field : fields) {
            String filedStr = field.toString();
            if (checkKeepField(filedStr)) {
                com.alibaba.fastjson2.JSONObject fieldJson = com.alibaba.fastjson2.JSONObject.parseObject(filedStr);
                String name = fieldJson.getString("name");
                String type = fieldJson.getString("type");
                if (type.contains("{")) {
                    com.alibaba.fastjson2.JSONObject types = com.alibaba.fastjson2.JSONObject.parseObject(type);
                    type = types.getString("type");
                }

                if (fieldJson.containsKey("default")) {
                    defaultFieldsMap.put(name, fieldJson.get("default"));
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
        com.alibaba.fastjson2.JSONObject fieldJson = com.alibaba.fastjson2.JSONObject.parseObject(message);
        boolean hasDoc = fieldJson.containsKey("doc");
        if (hasDoc) {
            com.alibaba.fastjson2.JSONObject doc = com.alibaba.fastjson2.JSONObject.parseObject(fieldJson.getString("doc"));
            if (doc.containsKey("visibility")) {
                String visibility = doc.getString("visibility");
                if ("disabled".equals(visibility)) {
                    isKeepField = false;
                }
            }
        }
        return isKeepField;
    }


    /**
     * 根据http链接获取schema，解析之后返回一个任务列表 (useList toList funcList paramlist)
     *
     * @return 任务列表
     */
    private static ArrayList<String[]> getNewJobList(String schema) {
        ArrayList<String[]> list = new ArrayList<>();

        JSONObject schemaJson = JSONObject.parseObject(schema);
        JSONArray fields = schemaJson.getJSONArray("fields");
        for (Object field : fields) {
            JSONObject fieldJson = JSONObject.parseObject(field.toString());
            boolean hasDoc = fieldJson.containsKey("doc");
            if (hasDoc) {
                JSONObject docJson = JSONObject.parseObject(fieldJson.getString("doc"));
                boolean hasFormat = docJson.containsKey("format");
                if (hasFormat) {
                    String name = fieldJson.getString("name");
                    JSONArray formatList = docJson.getJSONArray("format");
                    for (Object format : formatList) {
                        JSONObject formatJson = JSONObject.parseObject(format.toString());
                        String function = formatJson.getString("function");
                        String appendTo;
                        String params = null;

                        if (formatJson.containsKey("appendTo")) {
                            appendTo = formatJson.getString("appendTo");
                        } else {
                            appendTo = name;
                        }
                        if (formatJson.containsKey("param")) {
                            params = formatJson.getString("param");
                        }
                        list.add(new String[]{name, appendTo, function, params});
                    }
                }
            }
        }
        return list;
    }

}
