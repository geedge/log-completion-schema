package com.zdjizhi.operator.map;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSONObject;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.general.IpLookupUtils;
import com.zdjizhi.tools.transform.TransForm;
import com.zdjizhi.tools.json.MetaUtil;
import com.zdjizhi.utils.IpLookupV2;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Map;


/**
 * @author qidaijie
 * @version 2021/5/27 15:01
 */
public class MapCompleted extends BroadcastProcessFunction<Tuple2<String, Long>, Map<String, String>, String> {
    private static final Log logger = LogFactory.get();
    private static IpLookupV2 ipLookup;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ipLookup = IpLookupUtils.initIpLookup();
    }

    @Override
    public void processElement(Tuple2<String, Long> value, ReadOnlyContext readOnlyContext, Collector<String> out) {
        String message = value.f0;
        try {
            JSONObject jsonObject = JSONObject.parseObject(message);
            Long timestamp = value.f1;
            if (timestamp != 0L) {
                jsonObject.put("common_ingestion_time", timestamp);
            }
            MetaUtil.dropJsonField(jsonObject);
            TransForm.transformLog(jsonObject, ipLookup);
            out.collect(jsonObject.toJSONString());
        } catch (RuntimeException e) {
            logger.error("TransForm log failed ( The field type is not verified ),The exception is :{}\n The error Message is:{}", e, message);
        }
    }

    @Override
    public void processBroadcastElement(Map<String, String> knowledgeUpdateCache, Context context, Collector<String> out) throws Exception {
        if (!knowledgeUpdateCache.isEmpty()) {
            ipLookup = IpLookupUtils.updateIpLookup(knowledgeUpdateCache);
        }
    }

}
