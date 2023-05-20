package com.zdjizhi.json;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.*;
import com.zdjizhi.json.pojo.KnowlegeBaseMeta;
import org.junit.Test;


/**
 * @author qidaijie
 * @Package com.zdjizhi.json
 * @Description:
 * @date 2022/3/2410:22
 */
public class JsonPathTest {
    private static final Log logger = LogFactory.get();

//    $.[?(@.version=='latest' && @.name in ['ip_v4_built_in','ip_v6_built_in','ip_v4_user_defined','ip_v6_user_defined','asn_v4','asn_v6'])].['name','sha256','format','path']

    @Test
    public void konwledgeBaseTest() {
        String meta = "";
        String KNOWLEDGE_EXPR = "[?(@.version=='latest' && (@.name in ('ip_v4_built_in','ip_v6_built_in','ip_v4_user_defined','ip_v6_user_defined','asn_v4','asn_v6')))]";
        JSONPath jsonPath = JSONPath.of(KNOWLEDGE_EXPR);
        String extract = jsonPath.extract(JSONReader.of(meta)).toString();

        JSONArray jsonArray = JSON.parseArray(extract);
        for (int i = 0; i < jsonArray.size(); i++) {
            KnowlegeBaseMeta knowlegeBaseMeta = JSONObject.parseObject(jsonArray.getString(i), KnowlegeBaseMeta.class);
            System.out.println(knowlegeBaseMeta.toString());
        }

    }
}
