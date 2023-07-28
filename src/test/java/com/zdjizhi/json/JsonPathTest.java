package com.zdjizhi.json;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.*;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.zdjizhi.common.CommonConfig;
import com.zdjizhi.common.FlowWriteConfig;
import org.junit.Test;

import java.util.Properties;


/**
 * @author qidaijie
 * @Package com.zdjizhi.json
 * @Description:
 * @date 2022/3/2410:22
 */
public class JsonPathTest {

    private static Properties properties = new Properties();


    static {
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, "192.168.44.12:8848");
        properties.setProperty(PropertyKeyConst.USERNAME, "nacos");
        properties.setProperty(PropertyKeyConst.PASSWORD, "nacos");
    }

    @Test
    public void konwledgeBaseTest() {
        try {
            ConfigService configService = NacosFactory.createConfigService(properties);
            String schema = configService.getConfig("knowledge_base.json", "DEFAULT_GROUP", 5000);
//            String KNOWLEDGE_EXPR = "[?(@.version=='latest' && (@.name in ('ip_v4_built_in','ip_v6_built_in','ip_v4_user_defined','ip_v6_user_defined','asn_v4','asn_v6')))]";
//            String KNOWLEDGE_EXPR = "[?(@.version=='latest' && (@.type in ('ip_location','asn','ip')))]";
//            String KNOWLEDGE_EXPR = "[?(@.version=='latest' && (@.name in ('ip_v4_built_in','ip_v6_built_in','ip_v4_user_defined','ip_v6_user_defined','asn_v4','asn_v6')) && (@.type in ('ip_location','asn','ip')))]";
//            String types = "[?(@.type in ('ip_location','asn','ip_user_defined'))]";
//            String names = "[?(@.name in ('ip_v4_built_in','ip_v6_built_in','asn_v4','asn_v6','内置IP定位库'))]";
//            String expr = "[?(@.version=='latest')][?(@.type in ('ip_location','asn','ip_user_defined'))]";

//            String expr = "[?(@.version=='latest')][?(@.name == 'QQQ' || (@.type == 'ip_user_defined'))]";
            String expr = "[?(@.version=='latest')][@.type in ('ip_location','asn','ip')][?(@.name in ('QQQ'))]";

//            JSONPath jsonPath = JSONPath.of(combinationFilterList());
            JSONPath jsonPath = JSONPath.of(expr);

            String extract = jsonPath.extract(JSONReader.of(schema)).toString();


            JSONArray jsonArray = JSON.parseArray(extract);
            for (int i = 0; i < jsonArray.size(); i++) {
                System.out.println(jsonArray.getString(i));
//                KnowlegeBaseMeta knowlegeBaseMeta = JSONObject.parseObject(jsonArray.getString(i), KnowlegeBaseMeta.class);
//                System.out.println(knowlegeBaseMeta.toString());
            }

        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    public static String combinationFilterList() {
        String[] typeList = CommonConfig.KNOWLEDGEBASE_TYPE_LIST.split(",");
        String[] nameList = CommonConfig.KNOWLEDGEBASE_NAME_LIST.split(",");
        String expr = "[?(@.version=='latest')]";
        // ip_location > 'ip_location'

        if (typeList.length > 1) {
            StringBuilder typeBuilder = new StringBuilder();
            typeBuilder.append("[?(@.type in (");
            for (int i = 0; i < typeList.length; i++) {
                if (i == typeList.length - 1) {
                    typeBuilder.append("'").append(typeList[i]).append("'))]");
                } else {
                    typeBuilder.append("'").append(typeList[i]).append("',");
                }
            }
            expr = expr + typeBuilder.toString();
        }

        if (nameList.length > 1) {
            StringBuilder nameBuilder = new StringBuilder();
            nameBuilder.append("[?(@.name in (");
            for (int i = 0; i < nameList.length; i++) {
                if (i == nameList.length - 1) {
                    nameBuilder.append("'").append(nameList[i]).append("'))]");
                } else {
                    nameBuilder.append("'").append(nameList[i]).append("',");
                }
            }
            expr = expr + nameBuilder.toString();
        }

        return expr;
    }

}
