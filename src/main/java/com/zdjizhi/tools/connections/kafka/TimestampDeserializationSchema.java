package com.zdjizhi.tools.connections.kafka;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSONObject;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author qidaijie
 * @version 2022/3/89:42
 */
public class TimestampDeserializationSchema implements KafkaDeserializationSchema {
    private static final Log logger = LogFactory.get();
    /**
     * 默认的字符串解析编码
     */
    private static final String ENCODING = "UTF8";

    @Override
    public TypeInformation getProducedType() {
        return TypeInformation.of(new TypeHint<Tuple2<String, Long>>() {
        });
    }

    @Override
    public boolean isEndOfStream(Object nextElement) {
        return false;
    }

    @Override
    public Tuple2<String, Long> deserialize(ConsumerRecord record) throws Exception {
        if (record != null) {
            try {
                long timestamp = record.timestamp() / 1000;
                String value = new String((byte[]) record.value(), ENCODING);
                return new Tuple2<>(value, timestamp);
            } catch (RuntimeException e) {
                logger.error("KafkaConsumer Deserialize failed,The exception is : " + e.getMessage());
                e.printStackTrace();
            }
        }
        return new Tuple2<>(null, null);
    }
}
