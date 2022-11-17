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
public class TypeMapCompleted extends BroadcastProcessFunction<Map<String, Object>, Map<String, byte[]>, String> {
    private static final Log logger = LogFactory.get();

    private static IpLookupV2 ipLookup;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ipLookup = IpLookupUtils.initIpLookup();
    }

    @Override
    public void processElement(Map<String, Object> value, ReadOnlyContext readOnlyContext, Collector<String> out) throws Exception {
        try {
            Map<String, Object> jsonMap = JsonParseUtil.typeTransform(value);
            TransForm.cleanLog(jsonMap, ipLookup);
            String result = JsonMapper.toJsonString(jsonMap);
            if (result != null) {
                out.collect(result);
            }
        } catch (RuntimeException e) {
            logger.error("TransForm logs failed( The field type is verified ),The exception is :" + e);
            e.printStackTrace();
        }
    }

    @Override
    public void processBroadcastElement(Map<String, byte[]> knowledgeFileCache, Context context, Collector<String> out) throws Exception {
        if (!knowledgeFileCache.isEmpty()) {
            ipLookup = IpLookupUtils.updateIpLookup(knowledgeFileCache);
        }
    }

}
