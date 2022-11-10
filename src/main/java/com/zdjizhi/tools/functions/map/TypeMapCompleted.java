package com.zdjizhi.tools.functions.map;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.CustomFile;
import com.zdjizhi.tools.general.IpLookup;
import com.zdjizhi.tools.general.TransForm;
import com.zdjizhi.tools.json.JsonParseUtil;
import com.zdjizhi.utils.JsonMapper;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.Map;


/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.functions
 * @Description:
 * @date 2021/5/2715:01
 */
public class TypeMapCompleted extends BroadcastProcessFunction<Map<String, Object>, List<CustomFile>, String> {
    private static final Log logger = LogFactory.get();

    private static IpLookup ipLookup;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ipLookup = new IpLookup();
        ipLookup.loadIpLookup();
    }

    @Override
    public void processElement(Map<String, Object> value, ReadOnlyContext readOnlyContext, Collector<String> out) throws Exception {
        try {
            Map<String, Object> jsonMap = JsonParseUtil.typeTransform(value);
            for (String[] strings : JsonParseUtil.getJobList()) {
                //该日志字段的值
                Object logValue = JsonParseUtil.getValue(jsonMap, strings[0]);
                //结果值映射到的日志字段key
                String appendToKey = strings[1];
                //匹配操作函数的字段
                String function = strings[2];
                //额外的参数的值
                String param = strings[3];

                //结果值映射到的日志字段原始value
                Object appendToValue = JsonParseUtil.getValue(jsonMap, appendToKey);

                TransForm.functionSet(function, jsonMap, appendToKey, appendToValue, logValue, param, ipLookup);
            }

            out.collect(JsonMapper.toJsonString(jsonMap));

        } catch (RuntimeException e) {
            logger.error("TransForm logs failed( The field type is verified ),The exception is :" + e);
            e.printStackTrace();
        }
    }

    @Override
    public void processBroadcastElement(List<CustomFile> customFiles, Context context, Collector<String> out) throws Exception {
        if (!customFiles.isEmpty()) {
            for (CustomFile customFile : customFiles) {
                logger.info("定位库名称:" + customFile.getFileName() + ",更新文件字节长度:" + customFile.getContent().length);
                ipLookup.updateIpLookup(customFile.getFileName(), customFile.getContent());
            }
        }
    }

}
