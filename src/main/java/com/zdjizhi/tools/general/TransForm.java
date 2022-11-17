package com.zdjizhi.tools.general;

import com.zdjizhi.tools.json.JsonParseUtil;
import com.zdjizhi.utils.IpLookupV2;

import java.util.Map;


/**
 * 描述:转换或补全工具类
 *
 * @author qidaijie
 */
public class TransForm {

    public static void cleanLog(Map<String, Object> jsonMap, IpLookupV2 ipLookup) {
        for (String[] strings : JsonParseUtil.getJobList()) {
            //该日志字段的值
            Object logValue = JsonParseUtil.getValue(jsonMap, strings[0]);
            //结果值映射到的日志字段key
            String appendToKey = strings[1];
            //匹配操作函数的字段
            String function = strings[2];
            //额外的参数的值
            String param = strings[3];

            //结果值映射到的日志字段原始value
            Object appendToValue = JsonParseUtil.getValue(jsonMap, appendToKey);

            functionSet(function, jsonMap, appendToKey, appendToValue, logValue, param, ipLookup);
        }
    }


    /**
     * 根据schema描述对应字段进行操作的 函数集合
     *
     * @param function      匹配操作函数的字段
     * @param jsonMap       原始日志解析map
     * @param appendToKey   需要补全的字段的key
     * @param appendToValue 需要补全的字段的值
     * @param logValue      用到的参数的值
     * @param param         额外的参数的值
     */
    private static void functionSet(String function, Map<String, Object> jsonMap, String appendToKey, Object appendToValue, Object logValue, String param, IpLookupV2 ipLookup) {
        switch (function) {
            case "current_timestamp":
                if (!(appendToValue instanceof Long)) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunctions.getCurrentTime());
                }
                break;
            case "snowflake_id":
                JsonParseUtil.setValue(jsonMap, appendToKey, SnowflakeId.generateId());
                break;
            case "geo_ip_detail":
                if (logValue != null && appendToValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunctions.getGeoIpDetail(logValue.toString(), ipLookup));
                }
                break;
            case "geo_asn":
                if (logValue != null && appendToValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunctions.getGeoAsn(logValue.toString(), ipLookup));
                }
                break;
            case "geo_ip_country":
                if (logValue != null && appendToValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunctions.getGeoIpCountry(logValue.toString(), ipLookup));
                }
                break;
            case "flattenSpec":
                if (logValue != null && param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunctions.flattenSpec(logValue.toString(), param));
                }
                break;
            case "if":
                if (param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunctions.condition(jsonMap, param));
                }
                break;
            case "decode_of_base64":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunctions.decodeBase64(logValue.toString(), TransFunctions.isJsonValue(jsonMap, param)));
                }
                break;
            case "sub_domain":
                if (appendToValue == null && logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunctions.getTopDomain(logValue.toString()));
                }
                break;
            case "radius_match":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunctions.radiusMatch(jsonMap, logValue.toString()));
                }
                break;
            case "gtpc_match":
                if (logValue != null) {
                    TransFunctions.gtpcMatch(jsonMap, logValue.toString(), appendToKey, param);
                }
                break;
            case "set_value":
                if (param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, param);
                }
                break;
            case "get_value":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, logValue);
                }
                break;
            default:
        }
    }

}