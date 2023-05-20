package com.zdjizhi.tools.json;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zdjizhi.tools.exception.FlowWriteException;

/**
 * @author qidaijie
 * @Package com.zdjizhi.tools.json
 * @Description:
 * @date 2023/5/2014:58
 */
class DataTypeCheck {

    /**
     * 根据Schema指定的字段类型，对原始日志类型不一致的数据进行转换。
     *
     * @param jsonObject 原始日志json
     * @param fieldName  需要转换类型的字段名
     * @param fieldValue 需要转换类型的值
     * @param fieldType  schema指定的类型
     * @throws RuntimeException 抛出转换类型异常
     */
    static void typeConverter(JSONObject jsonObject, String fieldName, Object fieldValue, String fieldType) throws RuntimeException {
        switch (fieldType) {
            case "String":
                jsonObject.put(fieldName, fieldValue.toString());
                break;
            case "Integer":
                jsonObject.put(fieldName, castToInt(fieldValue));
                break;
            case "long":
                jsonObject.put(fieldName, castToLong(fieldValue));
                break;
            case "List":
                jsonObject.put(fieldName, JSONArray.parseArray(fieldValue.toString()));
                break;
            case "Map":
                jsonObject.put(fieldName, JSONObject.parseObject(fieldValue.toString()));
                break;
            case "double":
                jsonObject.put(fieldName, castToDouble(fieldValue));
                break;
            default:

        }
    }

    /**
     * Integer 类型判断方法
     *
     * @param value json value
     * @return Integer value or null
     */
    private static Integer castToInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            String strVal = value.toString();
            return Integer.parseInt(strVal);
        }

        throw new FlowWriteException("can not cast to int, value : " + value);
    }

    /**
     * Long类型判断方法
     *
     * @param value json value
     * @return (Long)value or null
     */
    private static Long castToLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            return Long.parseLong(strVal);
        }

        throw new FlowWriteException("can not cast to long, value : " + value);
    }

    /**
     * Double类型判断方法
     *
     * @param value json value
     * @return double value or null
     */
    private static Double castToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            return Double.parseDouble(strVal);
        }

        throw new FlowWriteException("can not cast to double, value : " + value);
    }
}
