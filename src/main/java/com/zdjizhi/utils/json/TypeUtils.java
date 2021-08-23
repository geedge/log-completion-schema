package com.zdjizhi.utils.json;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.StringUtil;
import com.zdjizhi.utils.exception.FlowWriteException;

/**
 * @author qidaijie
 * @Package PACKAGE_NAME
 * @Description:
 * @date 2021/7/1218:20
 */
public class TypeUtils {
    private static final Log logger = LogFactory.get();

    /**
     * Integer 类型判断方法
     *
     * @param value json value
     * @return Integer value or null
     */
    public static Object castToIfFunction(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return value.toString();
        }

        if (value instanceof Integer) {
            return ((Number) value).intValue();
        }

        if (value instanceof Long) {
            return ((Number) value).longValue();
        }

//        if (value instanceof Map) {
//            return (Map) value;
//        }
//
//        if (value instanceof List) {
//            return Collections.singletonList(value.toString());
//        }

        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }

        throw new FlowWriteException("can not cast to int, value : " + value);
    }

    /**
     * Integer 类型判断方法
     *
     * @param value json value
     * @return Integer value or null
     */
    static Integer castToInt(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (StringUtil.isBlank(strVal)) {
                return null;
            }

            //将 10,20 类数据转换为10
            if (strVal.contains(FlowWriteConfig.FORMAT_SPLITTER)) {
                strVal = strVal.split(FlowWriteConfig.FORMAT_SPLITTER)[0];
            }

            try {
                return Integer.parseInt(strVal);
            } catch (NumberFormatException ex) {
                logger.error("String change Integer Error,The error Str is:" + strVal);
            }
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }

        throw new FlowWriteException("can not cast to int, value : " + value);
    }

    /**
     * Double类型判断方法
     *
     * @param value json value
     * @return double value or null
     */
    static Double castToDouble(Object value) {

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;

            if (StringUtil.isBlank(strVal)) {
                return null;
            }

            //将 10,20 类数据转换为10
            if (strVal.contains(FlowWriteConfig.FORMAT_SPLITTER)) {
                strVal = strVal.split(FlowWriteConfig.FORMAT_SPLITTER)[0];
            }

            try {
                return Double.parseDouble(strVal);
            } catch (NumberFormatException ex) {
                logger.error("String change Double Error,The error Str is:" + strVal);
            }
        }

        throw new FlowWriteException("can not cast to double, value : " + value);
    }

    /**
     * Long类型判断方法
     *
     * @param value json value
     * @return (Long)value or null
     */
    static Long castToLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;

            if (StringUtil.isBlank(strVal)) {
                return null;
            }

            //将 10,20 类数据转换为10
            if (strVal.contains(FlowWriteConfig.FORMAT_SPLITTER)) {
                strVal = strVal.split(FlowWriteConfig.FORMAT_SPLITTER)[0];
            }

            try {
                return Long.parseLong(strVal);
            } catch (NumberFormatException ex) {
                logger.error("String change Long Error,The error Str is:" + strVal);
            }
        }

        throw new FlowWriteException("can not cast to long, value : " + value);
    }

}
