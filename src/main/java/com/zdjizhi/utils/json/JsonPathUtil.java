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
     * 通过 josnPath 解析，返回TEID数据
     *
     * @param message json数据
     * @param expr    解析表达式
     * @return 返回值
     */
    public static Long getTeidValue(String message, String expr) {
        Long result = null;
        try {
            if (StringUtil.isNotBlank(message) && StringUtil.isNotBlank(expr)) {
                ArrayList<Object> read = JsonPath.parse(message).read(expr);
                if (read.size() >= 1) {
                    result = Long.parseLong(read.get(0).toString());
                }
            }
        } catch (RuntimeException e) {
            logger.error("JSONPath parsing json returns Long data exception: " + e);
        }

        return result;
    }
}
