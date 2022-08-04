package com.zdjizhi.utils.system;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.StringUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.Properties;


/**
 * @author Administrator
 */

public final class FlowWriteConfigurations {

    private static Properties propDefault = new Properties();
    private static Properties propService = new Properties();


    public static String getStringProperty(Integer type, String key) {
        if (type == 0) {
            return propService.getProperty(key).trim();
        } else if (type == 1) {
            return propDefault.getProperty(key).trim();
        } else {
            return null;
        }
    }

    public static Integer getIntProperty(Integer type, String key) {
        if (type == 0) {
            return Integer.parseInt(propService.getProperty(key).trim());
        } else if (type == 1) {
            return Integer.parseInt(propDefault.getProperty(key).trim());
        } else {
            return null;
        }
    }

    public static Long getLongProperty(Integer type, String key) {
        if (type == 0) {
            return Long.parseLong(propService.getProperty(key).trim());
        } else if (type == 1) {
            return Long.parseLong(propDefault.getProperty(key).trim());
        } else {
            return null;
        }
    }

    public static Boolean getBooleanProperty(Integer type, String key) {
        if (type == 0) {
            return StringUtil.equals(propService.getProperty(key).toLowerCase().trim().toUpperCase(Locale.ENGLISH), "true");
        } else if (type == 1) {
            return StringUtil.equals(propDefault.getProperty(key).toLowerCase().trim().toUpperCase(Locale.ENGLISH), "true");
        } else {
            return null;
        }
    }

    static {
        try {
            propService.load(FlowWriteConfigurations.class.getClassLoader().getResourceAsStream("service_flow_config.properties"));
            propDefault.load(FlowWriteConfigurations.class.getClassLoader().getResourceAsStream("default_config.properties"));
        } catch (IOException | RuntimeException e) {
            propDefault = null;
            propService = null;
        }
    }
}
