package com.zdjizhi.utils.functions;

import com.zdjizhi.utils.general.TransFormTypeMap;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.Map;


/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.functions
 * @Description:
 * @date 2021/5/2715:01
 */
public class TypeMapCompletedFunction implements MapFunction<Map<String, Object>, String> {

    @Override
    @SuppressWarnings("unchecked")
    public String map(Map<String, Object> logs) {

        return TransFormTypeMap.dealCommonMessage(logs);
    }
}
