package com.zdjizhi.tools.connections.nacos;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.common.FlowWriteConfig;

import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi.tools.connections.nacos
 * @Description:
 * @date 2023/7/2714:49
 */
public class NacosConnection {
    private static final Log logger = LogFactory.get();

    private ConfigService configService;


    public ConfigService getSchemaService() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, FlowWriteConfig.NACOS_SERVER);
        properties.setProperty(PropertyKeyConst.NAMESPACE, FlowWriteConfig.NACOS_SCHEMA_NAMESPACE);
        properties.setProperty(PropertyKeyConst.USERNAME, FlowWriteConfig.NACOS_USERNAME);
        properties.setProperty(PropertyKeyConst.PASSWORD, FlowWriteConfig.NACOS_PIN);
        try {
            configService = NacosFactory.createConfigService(properties);
        } catch (NacosException e) {
            logger.error("NacosException:{}", e);
        }
        return configService;
    }


    public ConfigService getPublicService() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, FlowWriteConfig.NACOS_SERVER);
        properties.setProperty(PropertyKeyConst.NAMESPACE, FlowWriteConfig.NACOS_PUBLIC_NAMESPACE);
        properties.setProperty(PropertyKeyConst.USERNAME, FlowWriteConfig.NACOS_USERNAME);
        properties.setProperty(PropertyKeyConst.PASSWORD, FlowWriteConfig.NACOS_PIN);
        try {
            configService = NacosFactory.createConfigService(properties);
        } catch (NacosException e) {
            logger.error("NacosException:{}", e);
        }
        return configService;
    }
}
