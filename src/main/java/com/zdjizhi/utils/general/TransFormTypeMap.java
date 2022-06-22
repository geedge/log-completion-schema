package com.zdjizhi.utils.general;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.utils.JsonMapper;
import com.zdjizhi.utils.json.JsonParseUtil;

import java.util.Map;


/**
 * 描述:转换或补全工具类
 *
 * @author qidaijie
 */
public class TransFormTypeMap {
    private static final Log logger = LogFactory.get();

    /**
     * 解析日志，并补全
     *
     * @param message kafka Topic原始日志
     * @return 补全后的日志
     */
    @SuppressWarnings("unchecked")
    public static String dealCommonMessage(Map<String, Object> message) {
        try {
            Map<String, Object> jsonMap = JsonParseUtil.typeTransform(message);
            for (String[] strings : JsonParseUtil.getJobList()) {
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
            return JsonMapper.toJsonString(jsonMap);
        } catch (RuntimeException e) {
            logger.error("TransForm logs failed,The exception is :" + e);
            return null;
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
    private static void functionSet(String function, Map<String, Object> jsonMap, String appendToKeyName, Object appendToKeyValue, Object logValue, String param) {
        switch (function) {
            case "current_timestamp":
                if (!(appendToKeyValue instanceof Long)) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.getCurrentTime());
                }
                break;
            case "snowflake_id":
                JsonParseUtil.setValue(jsonMap, appendToKeyName, SnowflakeId.generateId());
                //版本规划暂不实现TSG-22.01
//                JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.getDecimalHash(SnowflakeId.generateId()));
                break;
            case "geo_ip_detail":
                if (logValue != null && appendToKeyValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.getGeoIpDetail(logValue.toString()));
                }
                break;
            case "geo_asn":
                if (logValue != null && appendToKeyValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.getGeoAsn(logValue.toString()));
                }
                break;
            case "geo_ip_country":
                if (logValue != null && appendToKeyValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.getGeoIpCountry(logValue.toString()));
                }
                break;
            case "set_value":
                if (param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, param);
                }
                break;
            case "get_value":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, logValue);
                }
                break;
            case "if":
                if (param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.condition(jsonMap, param));
                }
                break;
            case "sub_domain":
                if (appendToKeyValue == null && logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.getTopDomain(logValue.toString()));
                }
                break;
            case "radius_match":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.radiusMatch(logValue.toString()));
                }
                break;
            case "decode_of_base64":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.decodeBase64(logValue.toString(), TransFunction.isJsonValue(jsonMap, param)));
                }
                break;
            case "flattenSpec":
                if (logValue != null && param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.flattenSpec(logValue.toString(), param));
                }
                break;
            case "app_match":
                if (logValue != null && appendToKeyValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKeyName, TransFunction.appMatch(logValue.toString()));
                }
                break;
            default:
        }
    }

}