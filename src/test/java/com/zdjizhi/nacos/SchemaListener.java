package com.zdjizhi.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.StringUtil;
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
public class SchemaListener {

    private static Properties properties = new Properties();
    private static ArrayList<String[]> jobList;


    static {
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, "192.168.40.43:8848");
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
        while (true) {
            try {
                System.out.println(Arrays.toString(jobList.get(0)));
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

        //获取fields，并转化为数组，数组的每个元素都是一个name doc type
        JSONObject schemaJson = JSON.parseObject(schema);
        JSONArray fields = (JSONArray) schemaJson.get("fields");

        for (Object field : fields) {

            if (JSON.parseObject(field.toString()).containsKey("doc")) {
                Object doc = JSON.parseObject(field.toString()).get("doc");

                if (JSON.parseObject(doc.toString()).containsKey("format")) {
                    String name = JSON.parseObject(field.toString()).get("name").toString();
                    Object format = JSON.parseObject(doc.toString()).get("format");
                    JSONObject formatObject = JSON.parseObject(format.toString());

                    String functions = formatObject.get("functions").toString();
                    String appendTo = null;
                    String params = null;

                    if (formatObject.containsKey("appendTo")) {
                        appendTo = formatObject.get("appendTo").toString();
                    }

                    if (formatObject.containsKey("param")) {
                        params = formatObject.get("param").toString();
                    }


                    if (StringUtil.isNotBlank(appendTo) && StringUtil.isBlank(params)) {
                        String[] functionArray = functions.split(FlowWriteConfig.FORMAT_SPLITTER);
                        String[] appendToArray = appendTo.split(FlowWriteConfig.FORMAT_SPLITTER);

                        for (int i = 0; i < functionArray.length; i++) {
                            list.add(new String[]{name, appendToArray[i], functionArray[i], null});
                        }

                    } else if (StringUtil.isNotBlank(appendTo) && StringUtil.isNotBlank(params)) {
                        String[] functionArray = functions.split(FlowWriteConfig.FORMAT_SPLITTER);
                        String[] appendToArray = appendTo.split(FlowWriteConfig.FORMAT_SPLITTER);
                        String[] paramArray = params.split(FlowWriteConfig.FORMAT_SPLITTER);

                        for (int i = 0; i < functionArray.length; i++) {
                            list.add(new String[]{name, appendToArray[i], functionArray[i], paramArray[i]});

                        }
                    } else {
                        list.add(new String[]{name, name, functions, params});
                    }

                }
            }

        }
        return list;
    }

}
