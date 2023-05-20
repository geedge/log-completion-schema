package com.zdjizhi.tools.kafka;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

/**
 * @author qidaijie
 * @version 2022/3/89:42
 */
public class TimestampDeserializationSchema implements KafkaDeserializationSchema {
    private static final Log logger = LogFactory.get();

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
    @SuppressWarnings("unchecked")
    public Tuple2<String, Long> deserialize(ConsumerRecord record) throws Exception {
        if (record != null) {
            try {
                long timestamp = record.timestamp() / 1000;
                String value = new String((byte[]) record.value(), FlowWriteConfig.ENCODING);
                return new Tuple2<>(value, timestamp);
            } catch (RuntimeException e) {
                logger.error("KafkaConsumer Deserialize failed,The exception is : " + e.getMessage());
                e.printStackTrace();
            }
        }
        return new Tuple2<>(null, null);
    }
}
