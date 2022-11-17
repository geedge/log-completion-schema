package com.zdjizhi.tools.json;

import com.zdjizhi.tools.exception.FlowWriteException;
import com.zdjizhi.utils.JsonMapper;

import java.util.List;
import java.util.Map;


/**
 * @author qidaijie
 * @version 2021/7/1217:34
 */
public class JsonTypeUtil {
    /**
     * String 类型检验转换方法
     *
     * @param value json value
     * @return String value
     */
    static String checkString(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map) {
            return JsonMapper.toJsonString(value);
        }

        if (value instanceof List) {
            return JsonMapper.toJsonString(value);
        }

        return value.toString();
    }

    /**
     * array 类型检验转换方法
     *
     * @param value json value
     * @return List value
     */
    static Map checkObject(Object value) {
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
    static List checkArray(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof List) {
            return (List) value;
        }

        throw new FlowWriteException("can not cast to List, value : " + value);
    }

    /**
     * long 类型检验转换方法,若为空返回基础值
     *
     * @param value json value
     * @return Long value
     */
    static long checkLongValue(Object value) {
        Long longVal = TypeUtils.castToLong(value);

        if (longVal == null) {
            return 0L;
        }

        return longVal;
    }

    /**
     * Double 类型校验转换方法
     *
     * @param value json value
     * @return Double value
     */
    static Double checkDouble(Object value) {
        if (value == null) {
            return null;
        }

        return TypeUtils.castToDouble(value);
    }


    /**
     * int 类型检验转换方法,若为空返回基础值
     *
     * @param value json value
     * @return int value
     */
    static int getIntValue(Object value) {

        Integer intVal = TypeUtils.castToInt(value);
        if (intVal == null) {
            return 0;
        }
        return intVal;
    }

}
