package com.zdjizhi.json;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.StringUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * Applicable to schemas >= TSG22.08
 *
 * @author qidaijie
 * @Package com.zdjizhi.nacos
 * @Description:
 * @date 2022/3/1714:57
 */
public class NewSchemaTest {

    private static Properties properties = new Properties();


    static {
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, "192.168.44.12:8848");
        properties.setProperty(PropertyKeyConst.NAMESPACE, "test");
        properties.setProperty(PropertyKeyConst.USERNAME, "nacos");
        properties.setProperty(PropertyKeyConst.PASSWORD, "nacos");
    }


    @Test
    public void newSchemaTest() {
        try {

            ConfigService configService = NacosFactory.createConfigService(properties);
            String dataId = "session_record.json";
            String group = "Galaxy";
            ArrayList<String[]> newJobList = getNewJobList(configService.getConfig(dataId, group, 5000));

            for (String[] job : newJobList) {
                System.out.println(Arrays.toString(job));
            }
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据http链接获取schema，解析之后返回一个任务列表 (useList toList funcList paramlist)
     *
     * @return 任务列表
     */
    private static ArrayList<String[]> getNewJobList(String schema) {
        ArrayList<String[]> list = new ArrayList<>();

        JSONObject schemaJson = new JSONObject(schema, false, true);
        JSONArray fields = schemaJson.getJSONArray("fields");
        for (Object field : fields) {
            JSONObject fieldJson = new JSONObject(field, false, true);
            boolean hasDoc = fieldJson.containsKey("doc");
            if (hasDoc) {
                JSONObject docJson = fieldJson.getJSONObject("doc");
                boolean hasFormat = docJson.containsKey("format");
                if (hasFormat) {
                    String name = fieldJson.getStr("name");
                    JSONArray formatList = docJson.getJSONArray("format");
                    for (Object format : formatList) {
                        JSONObject formatJson = new JSONObject(format, false, true);
                        String function = formatJson.getStr("function");
                        String appendTo;
                        String params = null;

                        if (formatJson.containsKey("appendTo")) {
                            appendTo = formatJson.getStr("appendTo");
                        } else {
                            appendTo = name;
                        }

                        if (formatJson.containsKey("param")) {
                            params = formatJson.getStr("param");
                        }

                        list.add(new String[]{name, appendTo, function, params});

                    }
                }
            }
        }

        return list;
    }

}
