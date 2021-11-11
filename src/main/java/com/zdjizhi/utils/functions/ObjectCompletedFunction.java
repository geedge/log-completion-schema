package com.zdjizhi.utils.functions;

import com.zdjizhi.utils.general.TransFormObject;
import org.apache.flink.api.common.functions.MapFunction;


/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.functions
 * @Description:
 * @date 2021/5/2715:01
 */
public class ObjectCompletedFunction implements MapFunction<String, String> {

    @Override
    @SuppressWarnings("unchecked")
    public String map(String logs) {
        return TransFormObject.dealCommonMessage(logs);
    }
}
