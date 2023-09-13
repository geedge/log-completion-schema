package com.zdjizhi.tools.transform;

import com.alibaba.fastjson2.JSONObject;
import com.zdjizhi.tools.json.MetaUtil;
import com.zdjizhi.tools.transform.impl.TransformFunctionImpl;


/**
 * 描述:转换或补全工具类
 *
 * @author qidaijie
 */
public class TransForm {
    private static TransformFunctionImpl transformFunction = new TransformFunctionImpl();

    public static void transformLog(JSONObject jsonObject) {
        for (String[] strings : MetaUtil.getJobList()) {
            //该日志字段的值
            Object logValue = jsonObject.get(strings[0]);
            //结果值映射到的日志字段key
            String appendToKey = strings[1];
            //匹配操作函数的字段
            String function = strings[2];
            //额外的参数的值
            String param = strings[3];

            //结果值映射到的日志字段原始value
            Object appendToValue = jsonObject.get(appendToKey);

            functionSet(function, jsonObject, appendToKey, appendToValue, logValue, param);
        }
    }


    /**
     * 根据schema描述对应字段进行操作的 函数集合
     *
     * @param function      匹配操作函数的字段
     * @param jsonObject    原始日志解析map
     * @param appendToKey   需要补全的字段的key
     * @param appendToValue 需要补全的字段的值
     * @param logValue      用到的参数的值
     * @param param         额外的参数的值
     */
    private static void functionSet(String function, JSONObject jsonObject, String appendToKey, Object appendToValue, Object logValue, String param) {

        switch (function) {
            case "current_timestamp":
                if (!(appendToValue instanceof Long)) {
                    jsonObject.put(appendToKey, transformFunction.getCurrentTime());
                }
                break;
            case "snowflake_id":
                jsonObject.put(appendToKey, transformFunction.getSnowflakeId());
                break;
            case "geo_ip_detail":
                if (logValue != null && appendToValue == null) {
                    jsonObject.put(appendToKey, transformFunction.getGeoIpDetail(logValue.toString()));
                }
                break;
            case "geo_asn":
                if (logValue != null && appendToValue == null) {
                    jsonObject.put(appendToKey, transformFunction.getGeoAsn(logValue.toString()));
                }
                break;
            case "geo_ip_country":
                if (logValue != null && appendToValue == null) {
                    jsonObject.put(appendToKey, transformFunction.getGeoIpCountry(logValue.toString()));
                }
                break;
            case "flattenSpec":
                if (logValue != null && param != null) {
                    jsonObject.put(appendToKey, transformFunction.flattenSpec(logValue.toString(), param));
                }
                break;
            case "if":
                if (param != null) {
                    jsonObject.put(appendToKey, transformFunction.condition(jsonObject, param));
                }
                break;
            case "decode_of_base64":
                if (logValue != null) {
                    jsonObject.put( appendToKey, transformFunction.decodeBase64(jsonObject, logValue.toString(), param));
                }
                break;
            case "sub_domain":
                if (appendToValue == null && logValue != null) {
                    jsonObject.put( appendToKey, transformFunction.getTopDomain(logValue.toString()));
                }
                break;
            case "radius_match":
                if (logValue != null) {
                    jsonObject.put( appendToKey, transformFunction.radiusMatch(jsonObject, logValue.toString()));
                }
                break;
            case "gtpc_match":
                if (logValue != null) {
                    transformFunction.gtpcMatch(jsonObject, logValue.toString(), appendToKey, param);
                }
                break;
            case "set_value":
                if (param != null) {
                    transformFunction.setValue(jsonObject, appendToKey, param);
                }
                break;
            case "get_value":
                if (logValue != null) {
                    transformFunction.getValue(jsonObject, appendToKey, logValue);
                }
                break;
            default:
        }
    }

}