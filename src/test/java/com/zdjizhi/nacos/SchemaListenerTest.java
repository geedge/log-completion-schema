package com.zdjizhi.nacos;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author qidaijie
 * @Package com.zdjizhi.nacos
 * @Description:
 * @date 2022/3/1714:57
 */
public class SchemaListenerTest {

    private static Properties properties = new Properties();
    private static ArrayList<String[]> jobList;


    static {
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, "192.168.44.12:8848");
        properties.setProperty(PropertyKeyConst.NAMESPACE, "test");
        properties.setProperty(PropertyKeyConst.USERNAME, "nacos");
        properties.setProperty(PropertyKeyConst.PASSWORD, "nacos");

        try {
            ConfigService configService = NacosFactory.createConfigService(properties);
            String dataId = "session_record.json";
            String group = "Galaxy";
            jobList = getJobListFromHttp(configService.getConfig(dataId, group, 5000));
            configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configMsg) {
                    jobList = getJobListFromHttp(configMsg);
                }
            });
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void dealCommonMessage() {
        //keep running,change nacos config,print new config
        for (int i = 0; i < 1; i++) {
            try {
                for (String[] job : jobList) {
                    System.out.println(Arrays.toString(job));
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据http链接获取schema，解析之后返回一个任务列表 (useList toList funcList paramlist)
     *
     * @return 任务列表
     */
    private static ArrayList<String[]> getJobListFromHttp(String schema) {
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
                        String appendTo = null;
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
