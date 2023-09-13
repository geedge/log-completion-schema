package com.zdjizhi.operator.map;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSONObject;
import com.zdjizhi.tools.json.MetaUtil;
import com.zdjizhi.tools.transform.TransForm;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author qidaijie
 * @version 2021/5/27 15:01
 */
public class TypeMapCompleted extends ProcessFunction<String, String> {
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

            TransForm.transformLog(jsonObject);
            MetaUtil.typeTransform(jsonObject);
            out.collect(jsonObject.toJSONString());
        } catch (RuntimeException e) {
            logger.error("TransForm logs failed( The field type is verified ),The exception is :{}\n The error Message is:{}", e, message);
        }
    }

}
