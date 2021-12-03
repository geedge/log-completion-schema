package com.zdjizhi.utils.functions;

import com.zdjizhi.utils.fast.TransFormFast;
import com.zdjizhi.utils.general.TransFormTypeMap;
import org.apache.flink.api.common.functions.MapFunction;


/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.functions
 * @Description:
 * @date 2021/5/2715:01
 */
public class TypeMapCompletedFunction implements MapFunction<String, String> {

    @Override
    @SuppressWarnings("unchecked")
    public String map(String logs) {

        return TransFormTypeMap.dealCommonMessage(logs);
//        return TransFormFast.dealCommonMessage(logs);
    }
}
