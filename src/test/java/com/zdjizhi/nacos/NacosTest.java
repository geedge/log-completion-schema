package com.zdjizhi.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.system.FlowWriteConfigurations;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;


/**
 * @author qidaijie
 * @Package com.zdjizhi
 * @Description:
 * @date 2022/3/1016:58
 */
public class NacosTest {
    private static Properties properties = new Properties();

    @Test
    public void getProperties() {
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, "192.168.44.12:8848");
        properties.setProperty(PropertyKeyConst.NAMESPACE, "flink");
        properties.setProperty(PropertyKeyConst.USERNAME, "nacos");
        properties.setProperty(PropertyKeyConst.PASSWORD, "nacos");
    }


    @Test
    public void GetConfigurationTest() {
        try {
            getProperties();
            ConfigService configService = NacosFactory.createConfigService(properties);
            String content = configService.getConfig("topology_common_config.properties", "Galaxy", 5000);
            Properties nacosConfigMap = new Properties();
            nacosConfigMap.load(new StringReader(content));
            System.out.println(nacosConfigMap.getProperty("source.kafka.servers"));
            System.out.println(nacosConfigMap.getProperty("schema.http"));
        } catch (NacosException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void ListenerConfigurationTest() {
        getProperties();
        ConfigService configService = null;
        try {
            configService = NacosFactory.createConfigService(properties);
            String content = configService.getConfig("ETL-SESSION-RECORD-COMPLETED", "etl", 5000);

            Properties nacosConfigMap = new Properties();
            nacosConfigMap.load(new StringReader(content));
            System.out.println(nacosConfigMap.getProperty("source.kafka.servers"));

            configService.addListener("ETL-SESSION-RECORD-COMPLETED", "etl", new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configMsg) {
                    try {
                        Properties nacosConfigMap = new Properties();
                        nacosConfigMap.load(new StringReader(configMsg));
                        System.out.println(nacosConfigMap.getProperty("source.kafka.servers"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (NacosException | IOException e) {
            e.printStackTrace();
        }
    }
}
