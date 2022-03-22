package com.zdjizhi.utils.json;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.JsonMapper;
import com.zdjizhi.utils.exception.FlowWriteException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import static com.zdjizhi.utils.json.JsonParseUtil.getJobListFromHttp;
import static com.zdjizhi.utils.json.JsonParseUtil.getMapFromHttp;

/**
 * @author qidaijie
 * @Package PACKAGE_NAME
 * @Description:
 * @date 2021/7/1217:34
 */
public class JsonTypeUtils {
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
