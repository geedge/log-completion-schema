package com.zdjizhi.tools.functions.map;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.tools.general.IpLookupUtils;
import com.zdjizhi.tools.general.TransForm;
import com.zdjizhi.tools.json.JsonParseUtil;
import com.zdjizhi.utils.IpLookupV2;
import com.zdjizhi.utils.JsonMapper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Map;


/**
 * @author qidaijie
 * @version 2021/5/27 15:01
 */
public class MapCompleted extends BroadcastProcessFunction<Map<String, Object>, Map<String, byte[]>, String> {
    private static final Log logger = LogFactory.get();

    private static IpLookupV2 ipLookup;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ipLookup = IpLookupUtils.initIpLookup();

    }

    @Override
    public void processElement(Map<String, Object> value, ReadOnlyContext readOnlyContext, Collector<String> out) {
        try {
            JsonParseUtil.dropJsonField(value);
            TransForm.cleanLog(value, ipLookup);
            String result = JsonMapper.toJsonString(value);
            if (result != null) {
                out.collect(result);
            }
        } catch (RuntimeException e) {
            logger.error("TransForm log failed ( The field type is not verified ),The exception is :" + e);
            e.printStackTrace();
        }
    }

    @Override
    public void processBroadcastElement(Map<String, byte[]> customFiles, Context context, Collector<String> out) throws Exception {
        if (!customFiles.isEmpty()) {
            ipLookup = IpLookupUtils.updateIpLookup(customFiles);
        }
    }

}
