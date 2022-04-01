package com.zdjizhi.json;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.StringUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author qidaijie
 * @Package com.zdjizhi.json
 * @Description:
 * @date 2022/3/2410:22
 */
public class JsonPathTest {
    private static final Log logger = LogFactory.get();

    private static Properties propNacos = new Properties();

    /**
     * 获取需要删除字段的列表
     */
    private static ArrayList<String> dropList = new ArrayList<>();

    /**
     * 在内存中加载反射类用的map
     */
    private static HashMap<String, Class> map;

    /**
     * 获取任务列表
     * list的每个元素是一个四元字符串数组 (有format标识的字段，补全的字段，用到的功能函数，用到的参数)，例如：
     * (mail_subject mail_subject decode_of_base64 mail_subject_charset)
     */
    private static ArrayList<String[]> jobList;

    private static String schema;

    static {
        propNacos.setProperty(PropertyKeyConst.SERVER_ADDR, FlowWriteConfig.NACOS_SERVER);
        propNacos.setProperty(PropertyKeyConst.NAMESPACE, FlowWriteConfig.NACOS_SCHEMA_NAMESPACE);
        propNacos.setProperty(PropertyKeyConst.USERNAME, FlowWriteConfig.NACOS_USERNAME);
        propNacos.setProperty(PropertyKeyConst.PASSWORD, FlowWriteConfig.NACOS_PIN);
        try {
            ConfigService configService = NacosFactory.createConfigService(propNacos);
            String dataId = FlowWriteConfig.NACOS_DATA_ID;
            String group = FlowWriteConfig.NACOS_GROUP;
            String config = configService.getConfig(dataId, group, 5000);
            if (StringUtil.isNotBlank(config)) {
                schema = config;
            }
        } catch (NacosException e) {
            logger.error("Get Schema config from Nacos error,The exception message is :" + e.getMessage());
        }
    }

    @Test
    public void parseSchemaGetFields() {
        DocumentContext parse = JsonPath.parse(schema);
        List<Object> fields = parse.read("$.fields[*]");
        for (Object field : fields) {
            String name = JsonPath.read(field, "$.name").toString();
            String type = JsonPath.read(field, "$.type").toString();
        }
    }
}
