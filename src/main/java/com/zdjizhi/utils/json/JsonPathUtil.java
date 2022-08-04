package com.zdjizhi.utils.json;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.zdjizhi.utils.StringUtil;

import java.util.ArrayList;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.json
 * @Description:
 * @date 2022/7/1817:19
 */
public class JsonPathUtil {
    private static final Log logger = LogFactory.get();


    /**
     * 通过 josnPath 解析，返回String类型数据
     *
     * @param message json数据
     * @param expr    解析表达式
     * @return 返回值
     */
    public static String getStringValue(String message, String expr) {
        String result = null;
        try {
            if (StringUtil.isNotBlank(message) && StringUtil.isNotBlank(expr)) {
                ArrayList<String> read = JsonPath.parse(message).read(expr);
                if (read.size() >= 1) {
                    result = read.get(0);
                }
            }
        } catch (RuntimeException e) {
            logger.error("JSONPath parsing json returns String data exception" + e);
        }

        return result;
    }


    /**
     * 通过 josnPath 解析，返回Long类型数据
     *
     * @param message json数据
     * @param expr    解析表达式
     * @return 返回值
     */
    public static Integer getIntegerValue(String message, String expr) {
        Integer result = null;
        try {
            if (StringUtil.isNotBlank(message) && StringUtil.isNotBlank(expr)) {
                ArrayList<Integer> read = JsonPath.parse(message).read(expr);
                if (read.size() >= 1) {
                    result = read.get(0);
                }
            }
        } catch (RuntimeException e) {
            logger.error("JSONPath parsing json returns Long data exception" + e);
        }

        return result;
    }

    /**
     * 通过 josnPath 解析，返回Long类型数据
     *
     * @param message json数据
     * @param expr    解析表达式
     * @return 返回值
     */
    public static Long getLongValue(String message, String expr) {
        Long result = null;
        try {
            if (StringUtil.isNotBlank(message) && StringUtil.isNotBlank(expr)) {
                System.out.println(message);
                ArrayList<Long> read = JsonPath.parse(message).read(expr);
                if (read.size() >= 1) {
                    result = read.get(0);
                }
            }
        } catch (RuntimeException e) {
            logger.error("JSONPath parsing json returns Long data exception" + e);
        }

        return result;
    }
}
