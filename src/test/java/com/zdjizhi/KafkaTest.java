package com.zdjizhi;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi
 * @Description:
 * @date 2021/8/217:39
 */
public class KafkaTest {
    private static final Log logger = LogFactory.get();

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "192.168.44.12:9091");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("acks", "1");
//        properties.put("retries", DefaultProConfig.RETRIES);
//        properties.put("linger.ms", DefaultProConfig.LINGER_MS);
//        properties.put("request.timeout.ms", DefaultProConfig.REQUEST_TIMEOUT_MS);
//        properties.put("batch.size", DefaultProConfig.BATCH_SIZE);
//        properties.put("buffer.memory", DefaultProConfig.BUFFER_MEMORY);
//        properties.put("max.request.size", DefaultProConfig.MAX_REQUEST_SIZE);

        properties.put("security.protocol", "SSL");
//        properties.put("ssl.keystore.location", "D:\\K18-Phase2\\tsgSpace\\dat\\kafka\\client.keystore.jks");
        properties.put("ssl.keystore.location", "D:\\K18-Phase2\\tsgSpace\\dat\\tsg\\keystore.jks");
        properties.put("ssl.keystore.password", "galaxy2019");
//        properties.put("ssl.truststore.location", "D:\\K18-Phase2\\tsgSpace\\dat\\kafka\\client.truststore.jks");
        properties.put("ssl.truststore.location", "D:\\K18-Phase2\\tsgSpace\\dat\\tsg\\truststore.jks");
        properties.put("ssl.truststore.password", "galaxy2019");
        properties.put("ssl.key.password", "galaxy2019");
        properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

        Producer<String, String> producer = new KafkaProducer<String, String>(properties);

        producer.send(new ProducerRecord<>("test", "hello!"), new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    logger.error("写入test出现异常", exception);
                }
            }
        });

        producer.close();
    }
}
