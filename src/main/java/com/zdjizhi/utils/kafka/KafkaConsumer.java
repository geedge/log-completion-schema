package com.zdjizhi.utils.kafka;

import com.zdjizhi.common.FlowWriteConfig;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;


import java.util.Map;
import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.kafka
 * @Description:
 * @date 2021/6/813:54
 */
public class KafkaConsumer {
    private static Properties createConsumerConfig() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", FlowWriteConfig.SOURCE_KAFKA_SERVERS);
        properties.put("group.id", FlowWriteConfig.GROUP_ID);
        properties.put("session.timeout.ms", FlowWriteConfig.SESSION_TIMEOUT_MS);
        properties.put("max.poll.records", FlowWriteConfig.MAX_POLL_RECORDS);
        properties.put("max.partition.fetch.bytes", FlowWriteConfig.MAX_PARTITION_FETCH_BYTES);
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty(FlinkKafkaConsumerBase.KEY_PARTITION_DISCOVERY_INTERVAL_MILLIS, "10");
        CertUtils.chooseCert(FlowWriteConfig.SOURCE_KAFKA_SERVERS, properties);

        return properties;
    }

    /**
     * 用户序列化kafka数据，增加 kafka Timestamp内容。
     *
     * @return kafka logs -> map
     */
    public static FlinkKafkaConsumer<Map<String, Object>> myDeserializationConsumer() {
        FlinkKafkaConsumer<Map<String, Object>> kafkaConsumer = new FlinkKafkaConsumer<>(FlowWriteConfig.SOURCE_KAFKA_TOPIC,
                new TimestampDeserializationSchema(), createConsumerConfig());

        kafkaConsumer.setCommitOffsetsOnCheckpoints(false);
        kafkaConsumer.setStartFromGroupOffsets();

        return kafkaConsumer;
    }

    /**
     * 官方序列化kafka数据
     *
     * @return kafka logs
     */
    public static FlinkKafkaConsumer<String> flinkConsumer() {
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(FlowWriteConfig.SOURCE_KAFKA_TOPIC,
                new SimpleStringSchema(), createConsumerConfig());

        kafkaConsumer.setCommitOffsetsOnCheckpoints(false);
        kafkaConsumer.setStartFromGroupOffsets();

        return kafkaConsumer;
    }
}
