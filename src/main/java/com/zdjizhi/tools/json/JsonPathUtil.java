package com.zdjizhi.tools.json;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qidaijie
 * @Package com.zdjizhi.tools.json
 * @Description:
 * @date 2023/5/1917:51
 */
public class JsonPathUtil {
    private static final Log logger = LogFactory.get();

    private static Map<String, JSONPath> jsonPathMap = new ConcurrentHashMap<>(16);

    /**
     * 根据表达式使用JsonPath解析数据
     *
     * @param message json
     * @param expr    解析表达式
     * @return 解析结果
     */
    public static Object analysis(String message, String expr) {
        Object flattenResult = "";
        try {
            JSONReader parser = JSONReader.of(message);
            if (jsonPathMap.containsKey(expr)) {
                flattenResult = jsonPathMap.get(expr).extract(parser);
            } else {
                JSONPath jsonPath = JSONPath.of(expr);
                jsonPathMap.put(expr, jsonPath);
                flattenResult = jsonPath.extract(parser);
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            logger.error("The label resolution exception or [expr] analytic expression error" + e.getMessage());
        }
        return flattenResult;
    }
}
