package com.zdjizhi.utils.general;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.JsonMapper;
import com.zdjizhi.utils.StringUtil;
import com.zdjizhi.utils.json.JsonParseUtil;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 描述:转换或补全工具类
 *
 * @author qidaijie
 */
public class TransFormObject {
    private static final Log logger = LogFactory.get();

    /**
     * 在内存中加载反射类用的map
     */
    private static HashMap<String, Class> map = JsonParseUtil.getMapFromHttp(FlowWriteConfig.SCHEMA_HTTP);

    /**
     * 反射成一个类
     */
    private static Object mapObject = JsonParseUtil.generateObject(map);

    /**
     * 获取任务列表
     * list的每个元素是一个四元字符串数组 (有format标识的字段，补全的字段，用到的功能函数，用到的参数)，例如：
     * (mail_subject mail_subject decode_of_base64 mail_subject_charset)
     */
    private static ArrayList<String[]> jobList = JsonParseUtil.getJobListFromHttp(FlowWriteConfig.SCHEMA_HTTP);

    /**
     * 解析日志，并补全
     *
     * @param message kafka Topic原始日志
     * @return 补全后的日志
     */
    public static String dealCommonMessage(String message) {
        try {
            if (StringUtil.isNotBlank(message)) {
                Object object = JsonMapper.fromJsonString(message, mapObject.getClass());
                for (String[] strings : jobList) {
                    //用到的参数的值
                    Object name = JsonParseUtil.getValue(object, strings[0]);
                    //需要补全的字段的key
                    String appendToKeyName = strings[1];
                    //需要补全的字段的值
                    Object appendTo = JsonParseUtil.getValue(object, appendToKeyName);
                    //匹配操作函数的字段
                    String function = strings[2];
                    //额外的参数的值
                    String param = strings[3];
                    functionSet(function, object, appendToKeyName, appendTo, name, param);
                }
                return JsonMapper.toJsonString(object);
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            logger.error("解析补全日志信息过程异常,异常信息:" + e + "\n" + message);
            return null;
        }
    }


    /**
     * 根据schema描述对应字段进行操作的 函数集合
     *
     * @param function        匹配操作函数的字段
     * @param object          动态POJO Object
     * @param appendToKeyName 需要补全的字段的key
     * @param appendTo        需要补全的字段的值
     * @param name            用到的参数的值
     * @param param           额外的参数的值
     */
    private static void functionSet(String function, Object object, String appendToKeyName, Object appendTo, Object name, String param) {
        switch (function) {
            case "current_timestamp":
                if (!(appendTo instanceof Long)) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.getCurrentTime());
                }
                break;
            case "snowflake_id":
                JsonParseUtil.setValue(object, appendToKeyName, SnowflakeId.generateId());
                break;
            case "geo_ip_detail":
                if (name != null && appendTo == null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.getGeoIpDetail(name.toString()));
                }
                break;
            case "geo_asn":
                if (name != null && appendTo == null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.getGeoAsn(name.toString()));
                }
                break;
            case "geo_ip_country":
                if (name != null && appendTo == null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.getGeoIpCountry(name.toString()));
                }
                break;
            case "set_value":
                if (name != null && param != null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.setValue(param));
                }
                break;
            case "get_value":
                if (name != null) {
                    JsonParseUtil.setValue(object, appendToKeyName, name);
                }
                break;
            case "if":
                if (param != null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.condition(object, param));
                }
                break;
            case "sub_domain":
                if (appendTo == null && name != null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.getTopDomain(name.toString()));
                }
                break;
            case "radius_match":
                if (name != null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.radiusMatch(name.toString()));
                }
                break;
            case "decode_of_base64":
                if (name != null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.decodeBase64(name.toString(), TransFunction.isJsonValue(object, param)));
                }
                break;
            case "flattenSpec":
                if (name != null && param != null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.flattenSpec(name.toString(), param));
                }
                break;
            case "app_match":
                if (name != null && appendTo == null) {
                    JsonParseUtil.setValue(object, appendToKeyName, TransFunction.appMatch(name.toString()));
                }
                break;
            default:
        }
    }

}