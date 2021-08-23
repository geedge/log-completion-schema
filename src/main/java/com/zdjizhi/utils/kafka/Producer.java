package com.zdjizhi.utils.kafka;

import com.zdjizhi.common.DefaultProConfig;
import com.zdjizhi.common.FlowWriteConfig;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.kafka
 * @Description:
 * @date 2021/6/814:04
 */
public class Producer {

    private static Properties createProducerConfig() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", FlowWriteConfig.OUTPUT_KAFKA_SERVERS);
//        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("acks", FlowWriteConfig.PRODUCER_ACK);
        properties.put("retries", DefaultProConfig.RETRIES);
        properties.put("linger.ms", DefaultProConfig.LINGER_MS);
        properties.put("request.timeout.ms", DefaultProConfig.REQUEST_TIMEOUT_MS);
        properties.put("batch.size", DefaultProConfig.BATCH_SIZE);
        properties.put("buffer.memory", DefaultProConfig.BUFFER_MEMORY);
        properties.put("max.request.size", DefaultProConfig.MAX_REQUEST_SIZE);

        /**
         * kafka限流配置-20201117
         */
//        properties.put(ProducerConfig.CLIENT_ID_CONFIG, FlowWriteConfig.PRODUCER_CLIENT_ID);
//        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, FlowWriteConfig.PRODUCER_KAFKA_COMPRESSION_TYPE);
        return properties;
    }


    public static FlinkKafkaProducer<String> getKafkaProducer() {
        FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<String>(
                FlowWriteConfig.OUTPUT_KAFKA_TOPIC,
                new SimpleStringSchema(),
                createProducerConfig());

        kafkaProducer.setLogFailuresOnly(false);
//        kafkaProducer.setWriteTimestampToKafka(true);

        return kafkaProducer;
    }
}
