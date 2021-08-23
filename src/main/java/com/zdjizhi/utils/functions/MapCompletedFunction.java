package com.zdjizhi.utils.functions;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.utils.general.TransFormTypeMap;
import org.apache.flink.api.common.functions.MapFunction;


/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.functions
 * @Description:
 * @date 2021/5/2715:01
 */
public class MapCompletedFunction implements MapFunction<String, String> {
    private static final Log logger = LogFactory.get();

    @Override
    @SuppressWarnings("unchecked")
    public String map(String logs) {
        try {
            return TransFormTypeMap.dealCommonMessage(logs);
        } catch (RuntimeException e) {
            logger.error("解析补全日志信息过程异常,异常信息:" + e + "\n" + logs);
            return "";
        }
    }
}
