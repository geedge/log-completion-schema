package com.zdjizhi.utils.functions;

import com.zdjizhi.utils.StringUtil;
import org.apache.flink.api.common.functions.FilterFunction;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.functions
 * @Description:
 * @date 2021/5/2715:01
 */
public class FilterNullFunction implements FilterFunction<String> {
    @Override
    public boolean filter(String message) {
        return StringUtil.isNotBlank(message);
    }
}
