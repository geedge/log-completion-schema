package com.zdjizhi.tools.general;

import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi.common
 * @Description:
 * @date 2023/7/2714:22
 */
public class ConfigurationsUtils {

    public static String getStringProperty(Properties properties, String key) {
        if (!properties.isEmpty() && properties.containsKey(key)) {
            return properties.getProperty(key).trim();
        }
        return "";
    }

    public static Integer getIntProperty(Properties properties, String key) {
        if (!properties.isEmpty() && properties.containsKey(key)) {
            return Integer.parseInt(properties.getProperty(key).trim());
        }
        return 0;
    }

    public static Long getLongProperty(Properties properties, String key) {
        if (!properties.isEmpty() && properties.containsKey(key)) {
            return Long.parseLong(properties.getProperty(key).trim());
        }
        return 0L;
    }

    public static Boolean getBooleanProperty(Properties properties, String key) {
        if (!properties.isEmpty() && properties.containsKey(key)) {
            return Boolean.parseBoolean(properties.getProperty(key).trim());
        }
        return false;
    }

}
