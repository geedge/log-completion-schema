package com.zdjizhi;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.DefaultProConfig;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 * NTC系统配置产生日志写入数据中心类
 *
 * @author Administrator
 * @create 2018-08-13 15:11
 */

public class KafkaLogSend {
    private static final Log logger = LogFactory.get();

    /**
     * kafka生产者，用于向kafka中发送消息
     */
    private static org.apache.kafka.clients.producer.Producer<String, String> kafkaProducer;

    /**
     * kafka生产者适配器（单例），用来代理kafka生产者发送消息
     */
    private static KafkaLogSend kafkaLogSend;

    private KafkaLogSend() {
        initKafkaProducer();
    }

    public static KafkaLogSend getInstance() {
        if (kafkaLogSend == null) {
            kafkaLogSend = new KafkaLogSend();
        }
        return kafkaLogSend;
    }


    public void sendMessage(String message) {
//        for (String value : list) {
            kafkaProducer.send(new ProducerRecord<>("test", message), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        logger.error("写入test出现异常", exception);
                    }
                }
            });
//        }
//        kafkaProducer.flush();
        logger.debug("Log sent to National Center successfully!!!!!");
    }

    /**
     * 根据kafka生产者配置信息初始化kafka消息生产者,只初始化一次
     */
    private void initKafkaProducer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "192.168.44.33:9093,192.168.44.34:9093,192.168.44.35:9093");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("acks", "1");
        properties.put("retries", DefaultProConfig.RETRIES);
        properties.put("linger.ms", DefaultProConfig.LINGER_MS);
        properties.put("request.timeout.ms", DefaultProConfig.REQUEST_TIMEOUT_MS);
        properties.put("batch.size", DefaultProConfig.BATCH_SIZE);
        properties.put("buffer.memory", DefaultProConfig.BUFFER_MEMORY);
        properties.put("max.request.size", DefaultProConfig.MAX_REQUEST_SIZE);

        properties.put("security.protocol", "SSL");
        properties.put("ssl.keystore.location", "D:\\K18-Phase2\\tsgSpace\\dat\\kafka\\client.keystore.jks");
        properties.put("ssl.keystore.password", "ceiec2019");
        properties.put("ssl.truststore.location", "D:\\K18-Phase2\\tsgSpace\\dat\\kafka\\client.truststore.jks");
        properties.put("ssl.truststore.password", "ceiec2019");
        properties.put("ssl.key.password", "ceiec2019");


        /*
         * kafka限流配置-20201117
         */
//        properties.put(ProducerConfig.CLIENT_ID_CONFIG, VoipRelationConfig.PRODUCER_CLIENT_ID);
//        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, VoipRelationConfig.PRODUCER_KAFKA_COMPRESSION_TYPE);


        kafkaProducer = new KafkaProducer<>(properties);
    }


}
