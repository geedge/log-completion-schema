package com.zdjizhi.json;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zdjizhi.json.pojo.UserList;
import com.zdjizhi.json.pojo.UserMap;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author qidaijie
 * @Package com.zdjizhi.json
 * @Description:
 * @date 2023/5/2014:02
 */
public class FastJsonTest {

    @Test
    public void pojoTest() {
        //all right
        String message = "{\"name\":\"zhangsan\",\"age\":50,\"idcard\":\"140303199999999999\",\"previousaddress\":{\"first\":\"北京\",\"second\":\"上海\"}}";
        //int error (yes)
//        String message = "{\"name\":\"zhangsan\",\"age\":\"123\",\"idCard\":\"140303199999999999\",\"previousaddress\":{\"first\":\"北京\",\"second\":\"上海\"}}";
        //string error (yes)
//        String message = "{\"name\":123,\"age\":123,\"idCard\":\"140303199999999999\",\"previousaddress\":{\"first\":\"北京\",\"second\":\"上海\"}}";
        //json error (no)
//        String message = "{\"name\":\"zhangsan\",\"age\":50,\"idCard\":\"140303199999999999\",\"previousaddress\":\"{\\\"first\\\":\\\"北京\\\",\\\"second\\\":\\\"上海\\\"}\"}";

        UserMap user = JSON.parseObject(message, UserMap.class);
        System.out.println(user.getName());
        System.out.println(user.getDevicetag());
        System.out.println(JSON.toJSONString(user));
    }

    @Test
    public void typeCheckTest() {
        //jsonobject
        String message = "{\"name\":\"zhangsan\",\"age\":50,\"idcard\":\"123456789\",\"devicetag\":\"{\\\"tags\\\":[{\\\"tag\\\":\\\"group\\\",\\\"value\\\":\\\"7400\\\"},{\\\"tag\\\":\\\"center\\\",\\\"value\\\":\\\"7400\\\"}]}\"}";
        JSONObject objectTest = JSONObject.parseObject(message);

        for (Map.Entry<String, Object> entry : objectTest.entrySet()) {
            System.out.println("key:" + entry.getKey() + "————value:" + entry.getValue() + "————class: " + entry.getValue().getClass());
        }
        System.out.println("\n输出原始日志：" + objectTest.toString());

        Object previousMap = objectTest.get("devicetag");
        if (previousMap.getClass() != Map.class) {
            JSONObject previousObject = JSONObject.parseObject(previousMap.toString());
            objectTest.put("devicetag", previousObject);
        }
        System.out.println("输出转换map类型后的日志：" + objectTest.toString());

        UserMap userMap = objectTest.toJavaObject(UserMap.class);
        System.out.println(JSON.toJSONString(userMap));

        System.out.println("\n-----------------------------------------------\n");

        //jsonarray
        message = "{\"name\":\"zhangsan\",\"age\":50,\"idcard\":\"123456789\",\"devicetag\":\"[{\\\"tag\\\":\\\"group\\\",\\\"value\\\":\\\"7400\\\"},{\\\"tag\\\":\\\"center\\\",\\\"value\\\":\\\"7400\\\"}]\"}";
        JSONObject arrayTest = JSONObject.parseObject(message);
        for (Map.Entry<String, Object> entry : arrayTest.entrySet()) {
            System.out.println("key:" + entry.getKey() + "————value:" + entry.getValue() + "————class: " + entry.getValue().getClass());
        }
        System.out.println("\n输出原始日志：" + arrayTest.toString());

        Object previousList = arrayTest.get("devicetag");
        if (previousList.getClass() != List.class) {
            JSONArray previousArray = JSONArray.parseArray(previousList.toString());
            arrayTest.put("devicetag", previousArray);
        }
        System.out.println("输出转换list类型后的日志：" + arrayTest.toString());

        UserList userList = arrayTest.toJavaObject(UserList.class);
        System.out.println(JSON.toJSONString(userList));

    }

    @Test
    public void typeTest() {
        String message = "{\"name\":\"zhangsan\",\"age\":12,\"object\":{\"name\":\"a\",\"age\":12},\"array\":[{\"one\":1},{\"two\":2}]}";
        JSONObject objectTest = JSONObject.parseObject(message);

        for (Map.Entry<String, Object> entry : objectTest.entrySet()) {
            String key = entry.getKey();
            Class<?> aClass = entry.getValue().getClass();

            System.out.println(key + "---------" + aClass.getSimpleName());
        }
        Object bbb = objectTest.get("bbb");
        if (bbb == null){
            System.out.println("null");
        }
    }

}
