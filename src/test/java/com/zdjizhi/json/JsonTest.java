package com.zdjizhi.json;

import com.zdjizhi.utils.JsonMapper;
import com.zdjizhi.utils.json.JsonParseUtil;
import org.junit.Test;

import java.util.Map;

/**
 * @author qidaijie
 * @Package com.zdjizhi.json
 * @Description:
 * @date 2022/5/515:08
 */
public class JsonTest {

    @Test
    public void JacksonTest() {
        String value = "{\"common_log_id\":null}";
        Map<String, Object> json = (Map<String, Object>) JsonMapper.fromJsonString(value, Map.class);
        System.out.println(json.get("common_log_id"));
    }
}