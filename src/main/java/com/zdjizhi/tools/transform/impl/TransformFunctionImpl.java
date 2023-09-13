package com.zdjizhi.tools.transform.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.JSONObject;
import com.geedgenetworks.utils.FormatUtils;
import com.geedgenetworks.utils.StringUtil;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.general.IpLookupUtils;
import com.zdjizhi.tools.general.SnowflakeId;
import com.zdjizhi.tools.transform.TransformFunction;
import com.zdjizhi.tools.connections.hbase.HBaseUtils;
import com.zdjizhi.tools.json.JsonPathUtil;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;

/**
 * @author qidaijie
 * @Package com.zdjizhi.tools.transform.impl
 * @Description:
 * @date 2023/5/20 10:12
 */
public class TransformFunctionImpl implements TransformFunction {
    private static final Log logger = LogFactory.get();
    private static final int IF_PARAM_LENGTH = 3;
    /**
     * if函数连接分隔符
     */
    private static final String IF_CONDITION_SPLITTER = "=";

    private static final String SEPARATOR = "!=";

    /**
     * 标识字段为日志字段还是schema指定字段
     */
    private static final String IS_JSON_KEY_TAG = "$.";

    /**
     * 生成当前时间戳的操作
     */
    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    @Override
    public long getSnowflakeId() {
        return SnowflakeId.generateId();
    }

    /**
     * 根据clientIp获取location信息
     *
     * @param ip client IP
     * @return ip地址详细信息
     */
    @Override
    public String getGeoIpDetail(String ip) {
        String detail = "";
        try {
            detail = IpLookupUtils.getIpLookup().cityLookupDetail(ip);
        } catch (NullPointerException npe) {
            logger.error("The IP Location MMDB file is not loaded or IP is null! " + npe);
        } catch (RuntimeException e) {
            logger.error("Get clientIP location error! " + e.getMessage());
        }
        return detail;
    }

    /**
     * 根据ip获取country信息
     *
     * @param ip server IP
     * @return 国家
     */
    @Override
    public String getGeoIpCountry(String ip) {
        String country = "";
        try {
            country = IpLookupUtils.getIpLookup().countryLookup(ip);
        } catch (NullPointerException npe) {
            logger.error("The IP Location MMDB file is not loaded or IP is null! " + npe);
        } catch (RuntimeException e) {
            logger.error("Get ServerIP location error! " + e.getMessage());
        }
        return country;
    }

    /**
     * 根据ip获取asn信息
     *
     * @param ip client/server IP
     * @return ASN
     */
    @Override
    public String getGeoAsn(String ip) {
        String asn = "";
        try {
            asn = IpLookupUtils.getIpLookup().asnLookup(ip);
        } catch (NullPointerException npe) {
            logger.error("The ASN MMDB file is not loaded or IP is null! " + npe);
        } catch (RuntimeException e) {
            logger.error("Get IP ASN error! " + e.getMessage());
        }
        return asn;
    }

    /**
     * radius借助HBase补齐
     *
     * @param ip client IP
     * @return account
     */
    @Override
    public String radiusMatch(JSONObject jsonObject, String ip) {
        if (FlowWriteConfig.DEFAULT_RELATIONSHIP_MODULE.equals(FlowWriteConfig.DATA_RELATIONSHIP_MODEL)) {
            int vsysId = jsonObject.getIntValue("common_vsys_id", 1);
            return HBaseUtils.getAccount(ip + vsysId);
        } else {
            return HBaseUtils.getAccount(ip);
        }
    }


    /**
     * 借助HBase补齐GTP-C信息,解析tunnels信息，优先使用gtp_uplink_teid，其次使用gtp_downlink_teid
     * <p>
     * "common_tunnels":[{"tunnels_schema_type":"GTP","gtp_endpoint_a2b_teid":235261261,"gtp_endpoint_b2a_teid":665547833,"gtp_sgw_ip":"192.56.5.2","gtp_pgw_ip":"192.56.10.20","gtp_sgw_port":2152,"gtp_pgw_port":2152}]
     *
     * @param jsonObject  原始日志json
     * @param logValue    上行TEID
     * @param appendToKey 结果值映射到的日志字段key
     * @param param       用于解析jsonarray，直接定位到GTP信息所在的位置
     */
    @Override
    public void gtpcMatch(JSONObject jsonObject, String logValue, String appendToKey, String param) {
        try {
            String teid = null;
            String[] exprs = param.split(FlowWriteConfig.FORMAT_SPLITTER);
            for (String expr : exprs) {
                Object result = JsonPathUtil.analysis(logValue, expr);
                if (result != null) {
                    teid = result.toString();
                    break;
                }
            }

            if (teid != null) {
                if (FlowWriteConfig.DEFAULT_RELATIONSHIP_MODULE.equals(FlowWriteConfig.DATA_RELATIONSHIP_MODEL)) {
                    int vsysId = jsonObject.getIntValue("common_vsys_id", 1);
                    teid = teid + vsysId;
                }
                String[] appendToKeys = appendToKey.split(FlowWriteConfig.FORMAT_SPLITTER);
                HashMap<String, Object> userData = HBaseUtils.getGtpData(teid);
                if (userData != null) {
                    for (String key : appendToKeys) {
                        jsonObject.put(key, userData.get(key).toString());
                    }
                } else {
                    logger.warn("Description The user whose TEID is " + teid + " was not matched!");
                }
            }
        } catch (RuntimeException re) {
            logger.error("An exception occurred in teid type conversion or parsing of user information!" + re.getMessage());
            re.printStackTrace();
        }
    }

    /**
     * 解析顶级域名
     *
     * @param domain 初始域名
     * @return 顶级域名
     */
    @Override
    public String getTopDomain(String domain) {
        String topDomain = "";
        try {
            topDomain = FormatUtils.getTopPrivateDomain(domain);
        } catch (StringIndexOutOfBoundsException outException) {
            logger.error("Parse top-level domain exceptions, exception domain names:" + domain);
        }
        return topDomain;
    }

    /**
     * 根据编码解码base64
     *
     * @param jsonObject 原始日志json
     * @param message    base64
     * @param param      用于获取编码的参数
     * @return 解码字符串
     */
    @Override
    public String decodeBase64(JSONObject jsonObject, String message, String param) {
        String decodeResult = "";
        Object charset = isJsonValue(jsonObject, param);
        try {
            if (StringUtil.isNotBlank(message)) {
                byte[] base64decodedBytes = Base64.getDecoder().decode(message);
                if (charset == null) {
                    decodeResult = new String(base64decodedBytes);
                } else {
                    decodeResult = new String(base64decodedBytes, charset.toString());
                }
            }
        } catch (RuntimeException e) {
            logger.error("Resolve Base64 exception, exception information:" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            logger.error("The Character Encoding [" + charset.toString() + "] is not supported.exception information:" + e.getMessage());
        }
        return decodeResult;
    }

    /**
     * 根据表达式解析json
     *
     * @param message json
     * @param expr    解析表达式
     * @return 解析结果
     */
    @Override
    public Object flattenSpec(String message, String expr) {
        return JsonPathUtil.analysis(message, expr);
    }

    /**
     * IF函数实现，解析日志构建三目运算;包含判断是否为数字若为数字则转换为long类型返回结果。
     *
     * @param jsonObject 原始日志反序列化对象
     * @param ifParam    字段名/普通字符串
     * @return resultA or resultB or null
     */
    @Override
    public Object condition(JSONObject jsonObject, String ifParam) {
        Object result = null;
        try {
            String[] split = ifParam.split(FlowWriteConfig.FORMAT_SPLITTER);
            if (split.length == IF_PARAM_LENGTH) {
                String expression = split[0];
                Object resultA = isJsonValue(jsonObject, split[1]);
                Object resultB = isJsonValue(jsonObject, split[2]);
                if (expression.contains(SEPARATOR)) {
                    String[] regexp = expression.split(SEPARATOR);
                    Object direction = isJsonValue(jsonObject, regexp[0]);
                    if (direction instanceof Number) {
                        result = Integer.parseInt(direction.toString()) != Integer.parseInt(regexp[1]) ? resultA : resultB;
                    } else if (direction instanceof String) {
                        result = direction.equals(regexp[1]) ? resultA : resultB;
                    }
                } else {
                    String[] regexp = expression.split(IF_CONDITION_SPLITTER);
                    Object direction = isJsonValue(jsonObject, regexp[0]);
                    if (direction instanceof Number) {
                        result = Integer.parseInt(direction.toString()) == Integer.parseInt(regexp[1]) ? resultA : resultB;
                    } else if (direction instanceof String) {
                        result = direction.equals(regexp[1]) ? resultA : resultB;
                    }
                }
            }
        } catch (RuntimeException e) {
            logger.error("IF function execution exception, exception information:" + e.getMessage());
        }
        return result;
    }

    /**
     * 给json中的某个key赋值（指定值）
     *
     * @param jsonObject  原始日志json
     * @param appendToKey 要赋值的key
     * @param param       参数（指定值）
     */
    @Override
    public void setValue(JSONObject jsonObject, String appendToKey, String param) {
        jsonObject.put(appendToKey, param);
    }

    /**
     * 从json中获取A的值，赋值给B
     *
     * @param jsonObject  原始日志json
     * @param appendToKey 要赋值的key
     * @param logValue    获取的值
     */
    @Override
    public void getValue(JSONObject jsonObject, String appendToKey, Object logValue) {
        jsonObject.put(appendToKey, logValue);
    }

    /**
     * 判断是否为日志字段,是则返回对应value，否则返回原始字符串
     *
     * @param jsonObject 原始日志反序列化对象
     * @param param      字段名/普通字符串
     * @return JSON.Value or String
     */
    private static Object isJsonValue(JSONObject jsonObject, String param) {
        if (param.contains(IS_JSON_KEY_TAG)) {
            return jsonObject.get(param.substring(2));
        } else {
            return param;
        }
    }
}
