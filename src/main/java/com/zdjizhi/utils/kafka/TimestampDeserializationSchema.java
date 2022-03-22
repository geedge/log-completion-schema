package com.zdjizhi.utils.kafka;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.JsonMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.kafka
 * @Description:
 * @date 2022/3/89:42
 */
public class TimestampDeserializationSchema implements KafkaDeserializationSchema {
    private static final Log logger = LogFactory.get();

    @Override
    public TypeInformation getProducedType() {
        return TypeInformation.of(Map.class);
    }

    @Override
    public boolean isEndOfStream(Object nextElement) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> deserialize(ConsumerRecord record) throws Exception {
        if (record != null) {
            try {
                long timestamp = record.timestamp() / 1000;
                String value = new String((byte[]) record.value(), FlowWriteConfig.ENCODING);
                Map<String, Object> json = (Map<String, Object>) JsonMapper.fromJsonString(value, Map.class);
                json.put("common_ingestion_time", timestamp);
                return json;
            } catch (RuntimeException e) {
                logger.error("KafkaConsumer Deserialize failed,The exception is : " + e.getMessage());
            }
        }
        return null;
    }
}
