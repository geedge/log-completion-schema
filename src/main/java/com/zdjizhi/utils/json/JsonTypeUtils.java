package com.zdjizhi.utils.json;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.JsonMapper;
import com.zdjizhi.utils.exception.FlowWriteException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qidaijie
 * @Package PACKAGE_NAME
 * @Description:
 * @date 2021/7/1217:34
 */
public class JsonTypeUtils {
    private static final Log logger = LogFactory.get();
    /**
     * 在内存中加载反射类用的map
     */
    private static HashMap<String, Class> map = JsonParseUtil.getMapFromHttp(FlowWriteConfig.SCHEMA_HTTP);

    /**
     * 类型转换
     *
     * @param jsonMap 原始日志map
     */
    public static Map<String, Object> typeTransform(Map<String, Object> jsonMap) throws RuntimeException {
        JsonParseUtil.dropJsonField(jsonMap);
        HashMap<String, Object> tmpMap = new HashMap<>(192);
        for (String key : jsonMap.keySet()) {
            if (map.containsKey(key)) {
                String simpleName = map.get(key).getSimpleName();
                switch (simpleName) {
                    case "String":
                        tmpMap.put(key, checkString(jsonMap.get(key)));
                        break;
                    case "Integer":
                        tmpMap.put(key, getIntValue(jsonMap.get(key)));
                        break;
                    case "long":
                        tmpMap.put(key, checkLongValue(jsonMap.get(key)));
                        break;
                    case "List":
                        tmpMap.put(key, checkArray(jsonMap.get(key)));
                        break;
                    case "Map":
                        tmpMap.put(key, checkObject(jsonMap.get(key)));
                        break;
                    case "double":
                        tmpMap.put(key, checkDouble(jsonMap.get(key)));
                        break;
                    default:
                        tmpMap.put(key, checkString(jsonMap.get(key)));
                }
            }
        }
        return tmpMap;
    }

    /**
     * String 类型检验转换方法
     *
     * @param value json value
     * @return String value
     */
    private static String checkString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map){
            return JsonMapper.toJsonString(value);
        }

        if (value instanceof List){
            return JsonMapper.toJsonString(value);
        }

        return  value.toString();
    }

    /**
     * array 类型检验转换方法
     *
     * @param value json value
     * @return List value
     */
    private static Map checkObject(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map) {
            return (Map) value;
        }

        throw new FlowWriteException("can not cast to map, value : " + value);
    }

    /**
     * array 类型检验转换方法
     *
     * @param value json value
     * @return List value
     */
    private static List checkArray(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof List) {
            return (List) value;
        }

        throw new FlowWriteException("can not cast to List, value : " + value);
    }

    private static Long checkLong(Object value) {
        if (value == null) {
            return null;
        }

        return TypeUtils.castToLong(value);
    }

    /**
     * long 类型检验转换方法,若为空返回基础值
     *
     * @param value json value
     * @return Long value
     */
    private static long checkLongValue(Object value) {
        Long longVal = TypeUtils.castToLong(value);
        if (longVal == null) {
            return 0L;
        }

//        return longVal.longValue();
        return longVal;
    }

    /**
     * Double 类型校验转换方法
     *
     * @param value json value
     * @return Double value
     */
    private static Double checkDouble(Object value) {
        if (value == null) {
            return null;
        }

        return TypeUtils.castToDouble(value);
    }


    private static Integer checkInt(Object value) {
        if (value == null) {
            return null;
        }

        return TypeUtils.castToInt(value);
    }


    /**
     * int 类型检验转换方法,若为空返回基础值
     *
     * @param value json value
     * @return int value
     */
    private static int getIntValue(Object value) {

        Integer intVal = TypeUtils.castToInt(value);
        if (intVal == null) {
            return 0;
        }

//        return intVal.intValue();
        return intVal;
    }

}
