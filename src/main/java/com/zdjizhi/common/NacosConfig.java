package com.zdjizhi.common;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.utils.StringUtil;
import com.zdjizhi.utils.system.FlowWriteConfigurations;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi.common
 * @Description:
 * @date 2022/3/189:36
 */
public class NacosConfig {
    private static final Log logger = LogFactory.get();
    private static Properties propCommon = new Properties();
    private static Properties propNacos = new Properties();

    private static NacosConfig nacosConfig;

    private static void getInstance() {
        nacosConfig = new NacosConfig();
    }

    /**
     * 构造函数-新
     */
    private NacosConfig() {
        //获取连接
        getConnection();
    }

    /**
     * 初始化Nacos配置列表
     */
    private static void getConnection() {
        try {
            propNacos.setProperty(PropertyKeyConst.SERVER_ADDR, FlowWriteConfig.NACOS_SERVER);
            propNacos.setProperty(PropertyKeyConst.NAMESPACE, FlowWriteConfig.NACOS_COMMON_NAMESPACE);
            propNacos.setProperty(PropertyKeyConst.USERNAME, FlowWriteConfig.NACOS_USERNAME);
            propNacos.setProperty(PropertyKeyConst.PASSWORD, FlowWriteConfig.NACOS_PIN);
            ConfigService configService = NacosFactory.createConfigService(propNacos);
            String commonConfig = configService.getConfig("topology_common_config.properties", FlowWriteConfig.NACOS_GROUP, 5000);
            if (StringUtil.isNotBlank(commonConfig)) {
                propCommon.load(new StringReader(commonConfig));
            }
        } catch (NacosException | IOException e) {
            logger.error("Get topology run configuration error,The exception message is ：" + e.getMessage());
        }
    }

    /**
     * 获取String类型配置
     *
     * @param key config key
     * @return value
     */
    public static String getStringProperty(String key) {

        if (nacosConfig == null) {
            getInstance();
        }
        return propCommon.getProperty(key);

    }

    /**
     * 获取Integer类型配置
     *
     * @param key config key
     * @return value
     */
    public static Integer getIntegerProperty(String key) {
        if (nacosConfig == null) {
            getInstance();
        }

        return Integer.parseInt(propCommon.getProperty(key));

    }


    /**
     * 获取Long类型配置
     *
     * @param key config key
     * @return value
     */
    public static Long getLongProperty(String key) {
        if (nacosConfig == null) {
            getInstance();
        }

        return Long.parseLong(propCommon.getProperty(key));

    }

}
