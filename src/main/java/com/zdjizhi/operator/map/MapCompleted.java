package com.zdjizhi.operator.map;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSONObject;
import com.geedgenetworks.utils.IpLookupV2;
import com.zdjizhi.tools.general.IpLookupUtils;
import com.zdjizhi.tools.transform.TransForm;
import com.zdjizhi.tools.json.MetaUtil;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Map;


/**
 * @author qidaijie
 * @version 2021/5/27 15:01
 */
public class MapCompleted extends ProcessFunction<String, String> {
    private static final Log logger = LogFactory.get();

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
    }


    @Override
    public void processElement(String message, ProcessFunction<String, String>.Context ctx, Collector<String> out) throws Exception {
        try {
            JSONObject jsonObject = JSONObject.parseObject(message);
            jsonObject.put("common_ingestion_time", ctx.timestamp() / 1000);

            MetaUtil.dropJsonField(jsonObject);
            TransForm.transformLog(jsonObject);
            out.collect(jsonObject.toJSONString());
        } catch (RuntimeException e) {
            logger.error("TransForm log failed ( The field type is not verified ),The exception is :{}\n The error Message is:{}", e, message);
        }
    }
}
