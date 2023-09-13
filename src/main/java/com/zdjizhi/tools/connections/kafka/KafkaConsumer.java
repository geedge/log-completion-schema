package com.zdjizhi.tools.connections.kafka;

import com.zdjizhi.common.FlowWriteConfig;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;


import java.util.Properties;

/**
 * @author qidaijie
 * @version 2021/6/813:54
 */
public class KafkaConsumer {
    private static Properties createConsumerConfig() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", FlowWriteConfig.SOURCE_KAFKA_SERVERS);
        properties.put("group.id", FlowWriteConfig.GROUP_ID);
        properties.put("session.timeout.ms", FlowWriteConfig.SESSION_TIMEOUT_MS);
        properties.put("max.poll.records", FlowWriteConfig.MAX_POLL_RECORDS);
        properties.put("max.partition.fetch.bytes", FlowWriteConfig.MAX_PARTITION_FETCH_BYTES);
        properties.put("partition.discovery.interval.ms", "10000");
        CertUtils.chooseCert(FlowWriteConfig.SOURCE_KAFKA_SERVERS, properties);

        return properties;
    }

    /**
     * 官方序列化kafka数据
     *
     * @return kafka logs
     */
    public static FlinkKafkaConsumer<String> flinkConsumer() {
        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(FlowWriteConfig.SOURCE_KAFKA_TOPIC,
                new SimpleStringSchema(), createConsumerConfig());

        //随着checkpoint提交，将offset提交到kafka
        kafkaConsumer.setCommitOffsetsOnCheckpoints(true);

        //从消费组当前的offset开始消费
        kafkaConsumer.setStartFromGroupOffsets();

        return kafkaConsumer;
    }
}
