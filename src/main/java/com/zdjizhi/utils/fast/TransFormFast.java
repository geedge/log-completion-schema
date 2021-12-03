package com.zdjizhi.utils.fast;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.JsonMapper;
import com.zdjizhi.utils.StringUtil;
import com.zdjizhi.utils.general.SnowflakeId;
import com.zdjizhi.utils.json.JsonParseUtil;
import com.zdjizhi.utils.json.JsonTypeUtils;

import java.util.ArrayList;
import java.util.Map;


/**
 * 描述:转换或补全工具类
 *
 * @author qidaijie
 */
public class TransFormFast {
    private static final Log logger = LogFactory.get();

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
    @SuppressWarnings("unchecked")
    public static String dealCommonMessage(String message) {
        try {
            if (StringUtil.isNotBlank(message)) {
                JSONObject jsonMap = JSONObject.parseObject(message);
                JsonParseUtil.dropJsonField(jsonMap);
                for (String[] strings : jobList) {
                    //用到的参数的值
                    Object logValue = JsonParseUtil.getValue(jsonMap, strings[0]);
                    //需要补全的字段的key
                    String appendToKeyName = strings[1];
                    //需要补全的字段的值
                    Object appendToKeyValue = JsonParseUtil.getValue(jsonMap, appendToKeyName);
                    //匹配操作函数的字段
                    String function = strings[2];
                    //额外的参数的值
                    String param = strings[3];
                    functionSet(function, jsonMap, appendToKeyName, appendToKeyValue, logValue, param);
                }

                return FastJsonTypeUtils.typeTransform(jsonMap);

            } else {
                return "";
            }
        } catch (RuntimeException e) {
            logger.error("Logs TransForm Exception! Error message is:" + e);
            e.printStackTrace();
            return "";
        }
    }


    /**
     * 根据schema描述对应字段进行操作的 函数集合
     *
     * @param function         匹配操作函数的字段
     * @param jsonMap          原始日志解析map
     * @param appendToKeyName  需要补全的字段的key
     * @param appendToKeyValue 需要补全的字段的值
     * @param logValue         用到的参数的值
     * @param param            额外的参数的值
     */
    private static void functionSet(String function, JSONObject jsonMap, String appendToKeyName, Object appendToKeyValue, Object logValue, String param) {
        switch (function) {
            case "current_timestamp":
                if (!(appendToKeyValue instanceof Long)) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.getCurrentTime());
                }
                break;
            case "snowflake_id":
                JsonParseUtil.setValue(jsonMap, appendToKeyName, SnowflakeId.generateId());
                break;
            case "geo_ip_detail":
                if (logValue != null && appendToKeyValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.getGeoIpDetail(logValue.toString()));
                }
                break;
            case "geo_asn":
                if (logValue != null && appendToKeyValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.getGeoAsn(logValue.toString()));
                }
                break;
            case "geo_ip_country":
                if (logValue != null && appendToKeyValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.getGeoIpCountry(logValue.toString()));
                }
                break;
            case "set_value":
                if (param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.setValue(param));
                }
                break;
            case "get_value":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, logValue);
                }
                break;
            case "if":
                if (param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.condition(jsonMap, param));
                }
                break;
            case "sub_domain":
                if (appendToKeyValue == null && logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.getTopDomain(logValue.toString()));
                }
                break;
            case "radius_match":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.radiusMatch(logValue.toString()));
                }
                break;
            case "app_match":
                if (logValue != null && appendToKeyValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.appMatch(logValue.toString()));
                }
                break;
            case "decode_of_base64":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.decodeBase64(logValue.toString(), TransFunctionFast.isJsonValue(jsonMap, param)));
                }
                break;
            case "flattenSpec":
                if (logValue != null && param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunctionFast.flattenSpec(logValue.toString(), param));
                }
                break;
            default:
        }
    }

}