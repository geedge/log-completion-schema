package com.zdjizhi.tools.functions.filter;

import com.zdjizhi.utils.StringUtil;
import org.apache.flink.api.common.functions.FilterFunction;

/**
 * @author qidaijie
 * @version 2021/5/27 15:01
 */
public class FilterNull implements FilterFunction<String> {
    @Override
    public boolean filter(String message) {
        return StringUtil.isNotBlank(message);
    }
}
