package com.zdjizhi.common;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.tools.connections.nacos.NacosConnection;
import com.zdjizhi.tools.general.ConfigurationsUtils;
import com.zdjizhi.utils.StringUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author qidaijie
 * @Package com.zdjizhi.common
 * @Description:
 * @date 2023/7/2811:00
 */
public class CommonConfig {
    private static final Log logger = LogFactory.get();

    private static Properties configProperties;

    static {
        NacosConnection nacosConnection = new NacosConnection();
        ConfigService configService = nacosConnection.getPublicService();
        try {
            String dataId = "olap_common_config.properties";
            String group = FlowWriteConfig.NACOS_PUBLIC_GROUP;
            String config = configService.getConfigAndSignListener(dataId, group, FlowWriteConfig.NACOS_CONNECTION_TIMEOUT, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    if (StringUtil.isNotBlank(configInfo)) {
                        logger.info("Reload the common config when it changes.");
                        updateConfigProperties(configInfo);
                    }
                }
            });

            if (StringUtil.isNotBlank(config)) {
                updateConfigProperties(config);
            }

        } catch (NacosException e) {
            logger.error("Get common config from Nacos error,The exception message is :{}" + e);
        }
    }


    private static void updateConfigProperties(String config) {
        configProperties = new Properties();
        try {
            configProperties.load(new StringReader(config));
        } catch (IOException e) {
            logger.error("IOException:{}", e);
        }
    }

    public static String KNOWLEDGEBASE_TYPE_LIST = ConfigurationsUtils.getStringProperty(configProperties, "etl.knowledgebase.type.list");
    public static String KNOWLEDGEBASE_NAME_LIST = ConfigurationsUtils.getStringProperty(configProperties, "etl.knowledgebase.name.list");
    public static Boolean SCHEMA_UPDATE_ENABLED = ConfigurationsUtils.getBooleanProperty(configProperties, "etl.schema.update.enabled");

}
