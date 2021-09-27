package com.zdjizhi.utils.kafka;

import com.zdjizhi.common.FlowWriteConfig;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.kafka
 * @Description:
 * @date 2021/9/610:37
 */
class CertUtils {
    static void chooseCert(String type, Properties properties) {
        switch (type) {
            case "SSL":
                properties.put("security.protocol", "SSL");
                properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
                properties.put("ssl.keystore.location", FlowWriteConfig.TOOLS_LIBRARY + "keystore.jks");
                properties.put("ssl.keystore.password", FlowWriteConfig.KAFKA_PIN);
                properties.put("ssl.truststore.location", FlowWriteConfig.TOOLS_LIBRARY + "truststore.jks");
                properties.put("ssl.truststore.password", FlowWriteConfig.KAFKA_PIN);
                properties.put("ssl.key.password", FlowWriteConfig.KAFKA_PIN);
                break;
            case "SASL":
                properties.put("security.protocol", "SASL_PLAINTEXT");
                properties.put("sasl.mechanism", "PLAIN");
                properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username="
                        + FlowWriteConfig.KAFKA_USER + " password=" + FlowWriteConfig.KAFKA_PIN + ";");
                break;
            default:
        }

    }
}
