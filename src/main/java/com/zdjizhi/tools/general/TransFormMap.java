package com.zdjizhi.tools.general;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.utils.JsonMapper;
import com.zdjizhi.tools.json.JsonParseUtil;

import java.util.Map;


/**
 * 描述:转换或补全工具类
 *
 * @author qidaijie
 */
public class TransFormMap {
    private static final Log logger = LogFactory.get();

    /**
     * 解析日志，并补全
     *
     * @param jsonMap kafka Topic消费原始日志并解析
     * @return 补全后的日志
     */
    @SuppressWarnings("unchecked")
    public static String dealCommonMessage(Map<String, Object> jsonMap) {
        try {
            JsonParseUtil.dropJsonField(jsonMap);
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

                functionSet(function, jsonMap, appendToKey, appendToValue, logValue, param);
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
     * @param function      匹配操作函数的字段
     * @param jsonMap       原始日志解析map
     * @param appendToKey   需要补全的字段的key
     * @param appendToValue 需要补全的字段的值
     * @param logValue      用到的参数的值
     * @param param         额外的参数的值
     */
    private static void functionSet(String function, Map<String, Object> jsonMap, String appendToKey, Object appendToValue, Object logValue, String param) {
        switch (function) {
            case "current_timestamp":
                if (!(appendToValue instanceof Long)) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunction.getCurrentTime());
                }
                break;
            case "snowflake_id":
                JsonParseUtil.setValue(jsonMap, appendToKey, SnowflakeId.generateId());
                break;
            case "geo_ip_detail":
                if (logValue != null && appendToValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunction.getGeoIpDetail(logValue.toString()));
                }
                break;
            case "geo_asn":
                if (logValue != null && appendToValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunction.getGeoAsn(logValue.toString()));
                }
                break;
            case "geo_ip_country":
                if (logValue != null && appendToValue == null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunction.getGeoIpCountry(logValue.toString()));
                }
                break;
            case "flattenSpec":
                if (logValue != null && param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunction.flattenSpec(logValue.toString(), param));
                }
                break;
            case "if":
                if (param != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunction.condition(jsonMap, param));
                }
                break;
            case "decode_of_base64":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunction.decodeBase64(logValue.toString(), TransFunction.isJsonValue(jsonMap, param)));
                }
                break;
            case "sub_domain":
                if (appendToValue == null && logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunction.getTopDomain(logValue.toString()));
                }
                break;
            case "radius_match":
                if (logValue != null) {
                    JsonParseUtil.setValue(jsonMap, appendToKey, TransFunction.radiusMatch(jsonMap, logValue.toString()));
                }
                break;
            case "gtpc_match":
                if (logValue != null) {
                    TransFunction.gtpcMatch(jsonMap, logValue.toString(), appendToKey, param);
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