package com.zdjizhi.utils.kafka;

import com.zdjizhi.common.FlowWriteConfig;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Optional;
import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.kafka
 * @Description:
 * @date 2021/6/814:04
 */
public class KafkaProducer {

    private static Properties createProducerConfig() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", FlowWriteConfig.SINK_KAFKA_SERVERS);
        properties.put("acks", FlowWriteConfig.PRODUCER_ACK);
        properties.put("retries", FlowWriteConfig.RETRIES);
        properties.put("linger.ms", FlowWriteConfig.LINGER_MS);
        properties.put("request.timeout.ms", FlowWriteConfig.REQUEST_TIMEOUT_MS);
        properties.put("batch.size", FlowWriteConfig.BATCH_SIZE);
        properties.put("buffer.memory", FlowWriteConfig.BUFFER_MEMORY);
        properties.put("max.request.size", FlowWriteConfig.MAX_REQUEST_SIZE);
        properties.put("compression.type", FlowWriteConfig.PRODUCER_KAFKA_COMPRESSION_TYPE);

        CertUtils.chooseCert(FlowWriteConfig.SINK_KAFKA_SERVERS, properties);

        return properties;
    }


    public static FlinkKafkaProducer<String> getKafkaProducer() {
        FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<String>(
                FlowWriteConfig.SINK_KAFKA_TOPIC,
                new SimpleStringSchema(),
                createProducerConfig(),
                //sink与所有分区建立连接，轮询写入；
                Optional.empty());

        //允许producer记录失败日志而不是捕获和抛出它们
        kafkaProducer.setLogFailuresOnly(true);

        return kafkaProducer;
    }

}
