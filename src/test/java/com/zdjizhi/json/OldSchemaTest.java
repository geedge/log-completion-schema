package com.zdjizhi.json;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
 * Applicable to schemas < TSG22.08
 * @author qidaijie
 * @Package com.zdjizhi.nacos
 * @Description:
 * @date 2022/3/1714:57
 */
public class OldSchemaTest {

    private static Properties properties = new Properties();


    static {
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, "192.168.44.12:8848");
        properties.setProperty(PropertyKeyConst.NAMESPACE, "prod");
        properties.setProperty(PropertyKeyConst.USERNAME, "nacos");
        properties.setProperty(PropertyKeyConst.PASSWORD, "nacos");
    }
    

    @Test
    public void oldSchemaTest() {

        try {
            ConfigService configService = NacosFactory.createConfigService(properties);
            String dataId = "session_record.json";
            String group = "Galaxy";
            ArrayList<String[]> oldJobList = getOldJobList(configService.getConfig(dataId, group, 5000));

            for (String[] job : oldJobList) {
                System.out.println(Arrays.toString(job));
            }
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析schema，解析之后返回一个任务列表 (useList toList funcList paramlist)
     *
     * @param schema 日志schema
     * @return 任务列表
     */
    private static ArrayList<String[]> getOldJobList(String schema) {
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
