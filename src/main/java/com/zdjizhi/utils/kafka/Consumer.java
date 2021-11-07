package com.zdjizhi.utils.kafka;

import com.sun.tools.javac.comp.Flow;
import com.zdjizhi.common.FlowWriteConfig;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;


import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.kafka
 * @Description:
 * @date 2021/6/813:54
 */
public class Consumer {
    private static Properties createConsumerConfig() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", FlowWriteConfig.SOURCE_KAFKA_SERVERS);
        properties.put("group.id", FlowWriteConfig.GROUP_ID);
        properties.put("session.timeout.ms", FlowWriteConfig.SESSION_TIMEOUT_MS);
        properties.put("max.poll.records", FlowWriteConfig.MAX_POLL_RECORDS);
        properties.put("max.partition.fetch.bytes", FlowWriteConfig.MAX_PARTITION_FETCH_BYTES);
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        CertUtils.chooseCert(FlowWriteConfig.KAFKA_SOURCE_PROTOCOL,properties);

        return properties;
    }

    public static FlinkKafkaConsumer<String> getKafkaConsumer() {
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(FlowWriteConfig.SOURCE_KAFKA_TOPIC,
                new SimpleStringSchema(), createConsumerConfig());

        kafkaConsumer.setCommitOffsetsOnCheckpoints(false);
        kafkaConsumer.setStartFromGroupOffsets();

        return kafkaConsumer;
    }
}
