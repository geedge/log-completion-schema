package com.zdjizhi.utils.functions;


import com.zdjizhi.utils.general.TransFormMap;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.Map;


/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.functions
 * @Description:
 * @date 2021/5/2715:01
 */
public class MapCompletedFunction implements MapFunction<Map<String, Object>, String> {

    @Override
    @SuppressWarnings("unchecked")
    public String map(Map<String, Object> logs) {
        return TransFormMap.dealCommonMessage(logs);
    }
}
