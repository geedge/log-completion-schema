package com.zdjizhi.json;

import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.zdjizhi.tools.json.JsonPathUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author qidaijie
 * @Package com.zdjizhi.json
 * @Description:
 * @date 2022/3/2410:22
 */
public class JsonPathTest {
    private static final Log logger = LogFactory.get();

    @Test
    public void arrayFilterTest() {
        String json = "[{\"tunnels_schema_type\":\"GTP\",\"gtp_uplink_teid\":16777219,\"gtp_downlink_teid\":16777219,\"gtp_sgw_ip\":\"120.36.3.97\",\"gtp_pgw_ip\":\"43.224.53.100\",\"gtp_sgw_port\":2152,\"gtp_pgw_port\":51454},{\"tunnels_schema_type\":\"ETHERNET\",\"source_mac\":\"80:69:33:ea:a5:57\",\"destination_mac\":\"14:09:dc:df:a3:40\"}]";
        DocumentContext parse = JsonPath.parse(json);
        List<Object> fields = parse.read("$.[?(@.tunnels_schema_type=='GTP')]");
        if (fields.size() > 0) {
            JSONObject gtpJson = new JSONObject(fields.get(0), false, true);
            System.out.println(gtpJson.getStr("gtp_uplink_teid"));
            System.out.println(gtpJson.getStr("gtp_downlink_teid"));
        } else {
            System.out.println("no has");
        }


        ArrayList<Object> read = JsonPath.parse(json).read("$.[?(@.tunnels_schema_type=='GTP')].gtp_uplink_teid");
        String s = read.get(0).toString();
        System.out.println("string:" + s);
    }


    @Test
    public void getTeidTest() {
        String message = "[{\"tunnels_schema_type\":\"GTP\",\"gtp_uplink_teid\":999997894,\"gtp_downlink_teid\":665547895,\"gtp_sgw_ip\":\"192.56.5.2\",\"gtp_pgw_ip\":\"192.56.10.20\",\"gtp_sgw_port\":2152,\"gtp_pgw_port\":2152},{\"tunnels_schema_type\":\"MULTIPATH_ETHERNET\",\"c2s_source_mac\":\"00:0c:29:9b:2f:a4\",\"c2s_destination_mac\":\"00:0c:29:15:b4:f4\",\"s2c_source_mac\":\"00:0c:29:15:b4:f4\",\"s2c_destination_mac\":\"00:0c:29:9b:2f:a4\"}]";
        System.out.println(JsonPathUtil.getTeidValue(message, "$.[?(@.tunnels_schema_type=='GTP')].gtp_uplink_teid"));
        System.out.println(JsonPathUtil.getTeidValue(message, "$.[?(@.tunnels_schema_type=='GTP')].gtp_downlink_teid"));
    }


    @Test
    public void getFileMetaTest() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, "192.168.44.67:8848");
        properties.setProperty(PropertyKeyConst.NAMESPACE, "public");
        properties.setProperty(PropertyKeyConst.USERNAME, "nacos");
        properties.setProperty(PropertyKeyConst.PASSWORD, "nacos");
        ConfigService configService = null;
        try {
            configService = NacosFactory.createConfigService(properties);
            String message = configService.getConfig("knowledge_base.json", "DEFAULT_GROUP", 5000);
            ArrayList<Object> read = JsonPath.parse(message).read("$.[?(@.version=='latest' && @.name in ['ip_v4_built_in','ip_v6_built_in','ip_v4_user_defined','ip_v6_user_defined','asn_v4','asn_v6'])].['name','sha256','format','path']");
            if (read.size() >= 1) {
                for (Object metadata : read) {
                    System.out.println(metadata.toString());
                    JSONObject knowledgeJson = new JSONObject(metadata, false, true);
                    System.out.println("fileName:" + knowledgeJson.getStr("name") + "." + knowledgeJson.getStr("format") + " ---- filePath:" + knowledgeJson.getStr("path") + "\n");
                }
            }
        } catch (NacosException e) {
            e.printStackTrace();
        }

    }
}
