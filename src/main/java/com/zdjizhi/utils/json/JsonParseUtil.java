package com.zdjizhi.utils.json;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.StringUtil;

import java.util.*;
import java.util.concurrent.Executor;


/**
 * 使用FastJson解析json的工具类
 *
 * @author qidaijie
 */
public class JsonParseUtil {
    private static final Log logger = LogFactory.get();
    private static Properties propNacos = new Properties();

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

    /**
     * 获取任务列表
     * list的每个元素是一个四元字符串数组 (有format标识的字段，补全的字段，用到的功能函数，用到的参数)，例如：
     * (mail_subject mail_subject decode_of_base64 mail_subject_charset)
     */
    private static ArrayList<String[]> jobList;

    static {
        propNacos.setProperty(PropertyKeyConst.SERVER_ADDR, FlowWriteConfig.NACOS_SERVER);
        propNacos.setProperty(PropertyKeyConst.NAMESPACE, FlowWriteConfig.NACOS_SCHEMA_NAMESPACE);
        propNacos.setProperty(PropertyKeyConst.USERNAME, FlowWriteConfig.NACOS_USERNAME);
        propNacos.setProperty(PropertyKeyConst.PASSWORD, FlowWriteConfig.NACOS_PIN);
        try {
            ConfigService configService = NacosFactory.createConfigService(propNacos);
            String dataId = FlowWriteConfig.NACOS_DATA_ID;
            String group = FlowWriteConfig.NACOS_GROUP;
            String schema = configService.getConfig(dataId, group, 5000);
            if (StringUtil.isNotBlank(schema)) {
                jsonFieldsMap = getFieldsFromSchema(schema);
                jobList = getJobListFromHttp(schema);
            }
            configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configMsg) {
                    if (StringUtil.isNotBlank(configMsg)) {
                        clearCache();
                        jsonFieldsMap = getFieldsFromSchema(configMsg);
                        jobList = getJobListFromHttp(configMsg);
                    }
                }
            });
        } catch (NacosException e) {
            logger.error("Get Schema config from Nacos error,The exception message is :" + e.getMessage());
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
     * 类型转换
     *
     * @param jsonMap 原始日志map
     */
    public static Map<String, Object> typeTransform(Map<String, Object> jsonMap) throws RuntimeException {
        JsonParseUtil.dropJsonField(jsonMap);
        JsonParseUtil.setFieldDefault(jsonMap);
        HashMap<String, Object> tmpMap = new HashMap<>(256);
        for (String key : jsonMap.keySet()) {
            if (jsonFieldsMap.containsKey(key)) {
                String simpleName = jsonFieldsMap.get(key).getSimpleName();
                switch (simpleName) {
                    case "String":
                        tmpMap.put(key, JsonTypeUtil.checkString(jsonMap.get(key)));
                        break;
                    case "Integer":
                        tmpMap.put(key, JsonTypeUtil.getIntValue(jsonMap.get(key)));
                        break;
                    case "long":
                        tmpMap.put(key, JsonTypeUtil.checkLongValue(jsonMap.get(key)));
                        break;
                    case "List":
                        tmpMap.put(key, JsonTypeUtil.checkArray(jsonMap.get(key)));
                        break;
                    case "Map":
                        tmpMap.put(key, JsonTypeUtil.checkObject(jsonMap.get(key)));
                        break;
                    case "double":
                        tmpMap.put(key, JsonTypeUtil.checkDouble(jsonMap.get(key)));
                        break;
                    default:
                        tmpMap.put(key, JsonTypeUtil.checkString(jsonMap.get(key)));
                }
            }
        }
        return tmpMap;
    }

    public static ArrayList<String[]> getJobList() {
        return jobList;
    }

    /**
     * 删除schema内指定的无效字段（jackson）
     *
     * @param jsonMap 原始日志
     */
    public static void dropJsonField(Map<String, Object> jsonMap) {
        for (String field : dropList) {
            jsonMap.remove(field);
        }
    }

    /**
     * 根据schema内指定的默认值，给数据赋值。
     *
     * @param jsonMap 原始日志
     */
    private static void setFieldDefault(Map<String, Object> jsonMap) {
        for (String key : defaultFieldsMap.keySet()) {
            jsonMap.put(key, defaultFieldsMap.get(key));
        }
    }


    /**
     * 通过schema来获取所需的字段及字段类型。
     *
     * @return 用于反射生成schema类型的对象的一个map集合
     */
    private static HashMap<String, Class> getFieldsFromSchema(String schema) {
        HashMap<String, Class> map = new HashMap<>(256);

        //获取fields，并转化为数组，数组的每个元素都是一个name doc type
        JSONObject schemaJson = new JSONObject(schema, false, true);
        JSONArray fields = schemaJson.getJSONArray("fields");

        for (Object field : fields) {
            String filedStr = field.toString();
            if (checkKeepField(filedStr)) {
                JSONObject fieldJson = new JSONObject(filedStr, false, true);
                String name = fieldJson.getStr("name");
                String type = fieldJson.getStr("type");
                if (fieldJson.containsKey("default")) {
                    System.out.println(fieldJson.toString());
                    System.out.println(fieldJson.get("default"));
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
        JSONObject fieldJson = new JSONObject(message, false, true);
        boolean hasDoc = fieldJson.containsKey("doc");
        if (hasDoc) {
            JSONObject doc = new JSONObject(fieldJson.getStr("doc"), false, true);
            if (doc.containsKey("visibility")) {
                String visibility = doc.getStr("visibility");
                if (FlowWriteConfig.VISIBILITY.equals(visibility)) {
                    isKeepField = false;
                }
            }
        }
        return isKeepField;
    }

    /**
     * 解析schema，解析之后返回一个任务列表 (useList toList funcList paramlist)
     *
     * @param schema 日志schema
     * @return 任务列表
     */
    private static ArrayList<String[]> getJobListFromHttp(String schema) {
        ArrayList<String[]> list = new ArrayList<>();

        JSONObject schemaJson = new JSONObject(schema, false, true);
        JSONArray fields = schemaJson.getJSONArray("fields");
        for (Object field : fields) {
            JSONObject fieldJson = new JSONObject(field, false, true);
            boolean hasDoc = fieldJson.containsKey("doc");
            if (hasDoc) {
                JSONObject docJson = fieldJson.getJSONObject("doc");
                boolean hasFormat = docJson.containsKey("format");
                if (hasFormat) {
                    String name = fieldJson.getStr("name");
                    JSONArray formatList = docJson.getJSONArray("format");
                    for (Object format : formatList) {
                        JSONObject formatJson = new JSONObject(format, false, true);
                        String function = formatJson.getStr("function");
                        String appendTo;
                        String params = null;

                        if (formatJson.containsKey("appendTo")) {
                            appendTo = formatJson.getStr("appendTo");
                        } else {
                            appendTo = name;
                        }

                        if (formatJson.containsKey("param")) {
                            params = formatJson.getStr("param");
                        }

                        list.add(new String[]{name, appendTo, function, params});

                    }
                }
            }
        }

        return list;
    }

    /**
     * 在配置变动时，清空缓存重新获取
     */
    private static void clearCache() {
        jobList.clear();
        jsonFieldsMap.clear();
        dropList.clear();
        defaultFieldsMap.clear();
    }

}