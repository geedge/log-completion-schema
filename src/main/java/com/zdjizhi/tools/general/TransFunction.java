package com.zdjizhi.tools.general;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.hbase.HBaseUtils;
import com.zdjizhi.utils.FormatUtils;
import com.zdjizhi.utils.IpLookupV2;
import com.zdjizhi.utils.StringUtil;
import com.zdjizhi.tools.json.JsonParseUtil;
import com.zdjizhi.tools.json.JsonPathUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qidaijie
 */
class TransFunction {
    private static final Log logger = LogFactory.get();

    /**
     * 生成当前时间戳的操作
     */
    static long getCurrentTime() {

        return System.currentTimeMillis() / 1000;
    }

    /**
     * 根据clientIp获取location信息
     *
     * @param ip client IP
     * @return ip地址详细信息
     */
    static String getGeoIpDetail(String ip, IpLookupV2 ipLookup) {
        String detail = "";
        try {
            detail = ipLookup.cityLookupDetail(ip);
        } catch (NullPointerException npe) {
            logger.error("The MMDB file is not loaded or IP is null! " + npe.getMessage());
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
    static String getGeoIpCountry(String ip, IpLookupV2 ipLookup) {
        String country = "";
        try {
            country = ipLookup.countryLookup(ip);
        } catch (NullPointerException npe) {
            logger.error("The MMDB file is not loaded or IP is null! " + npe.getMessage());
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
    static String getGeoAsn(String ip, IpLookupV2 ipLookup) {
        String asn = "";
        try {
            asn = ipLookup.asnLookup(ip);
        } catch (NullPointerException npe) {
            logger.error("The MMDB file is not loaded or IP is null! " + npe.getMessage());
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
    static String radiusMatch(Map<String, Object> jsonMap, String ip) {
        if (FlowWriteConfig.DEFAULT_RELATIONSHIP_MODULE.equals(FlowWriteConfig.DATA_RELATIONSHIP_MODEL)) {
            String vsysId = jsonMap.getOrDefault("common_vsys_id", "1").toString();
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
     * @param jsonMap     原始日志json
     * @param logValue    上行TEID
     * @param appendToKey 结果值映射到的日志字段key
     * @param param       用于解析jsonarray，直接定位到GTP信息所在的位置
     */
    static void gtpcMatch(Map<String, Object> jsonMap, String logValue, String appendToKey, String param) {
        try {
            String teid = null;
            String[] exprs = param.split(FlowWriteConfig.FORMAT_SPLITTER);
            for (String expr : exprs) {
                String value = JsonPathUtil.getTeidValue(logValue, expr);
                if (value != null) {
                    teid = value;
                    break;
                }
            }

            if (teid != null) {
                if (FlowWriteConfig.DEFAULT_RELATIONSHIP_MODULE.equals(FlowWriteConfig.DATA_RELATIONSHIP_MODEL)) {
                    String vsysId = jsonMap.getOrDefault("common_vsys_id", "1").toString();
                    teid = teid + vsysId;
                }
                String[] appendToKeys = appendToKey.split(FlowWriteConfig.FORMAT_SPLITTER);
                HashMap<String, Object> userData = HBaseUtils.getGtpData(teid);
                if (userData != null) {
                    for (String key : appendToKeys) {
                        JsonParseUtil.setValue(jsonMap, key, userData.get(key).toString());
                    }
                } else {
                    logger.warn("Description The user whose TEID is " + teid + " was not matched!");
                }
            }
        } catch (RuntimeException re) {
            logger.error("An exception occurred in teid type conversion or parsing of user information!" + re.getMessage());
        }
    }

    /**
     * 解析顶级域名
     *
     * @param domain 初始域名
     * @return 顶级域名
     */
    static String getTopDomain(String domain) {
        try {
            return FormatUtils.getTopPrivateDomain(domain);
        } catch (StringIndexOutOfBoundsException outException) {
            logger.error("Parse top-level domain exceptions, exception domain names:" + domain);
            return "";
        }
    }

    /**
     * 根据编码解码base64
     *
     * @param message base64
     * @param charset 编码
     * @return 解码字符串
     */
    static String decodeBase64(String message, Object charset) {
        String result = "";
        try {
            if (StringUtil.isNotBlank(message)) {
                byte[] base64decodedBytes = Base64.getDecoder().decode(message);
                if (charset == null) {
                    result = new String(base64decodedBytes);
                } else {
                    result = new String(base64decodedBytes, charset.toString());
                }
            }
        } catch (RuntimeException e) {
            logger.error("Resolve Base64 exception, exception information:" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            logger.error("The Character Encoding [" + charset.toString() + "] is not supported.exception information:" + e.getMessage());
        }
        return result;
    }

    /**
     * 根据表达式解析json
     *
     * @param message json
     * @param expr    解析表达式
     * @return 解析结果
     */
    static String flattenSpec(String message, String expr) {
        String flattenResult = "";
        try {
            if (StringUtil.isNotBlank(expr)) {
                ArrayList<String> read = JsonPath.parse(message).read(expr);
                if (read.size() >= 1) {
                    flattenResult = read.get(0);
                }
            }
        } catch (ClassCastException | InvalidPathException | ArrayIndexOutOfBoundsException e) {
            logger.error("The label resolution exception or [expr] analytic expression error" + e.getMessage());
        }
        return flattenResult;
    }

    /**
     * 判断是否为日志字段,是则返回对应value，否则返回原始字符串
     *
     * @param jsonMap 内存实体类
     * @param param   字段名/普通字符串
     * @return JSON.Value or String
     */
    static Object isJsonValue(Map<String, Object> jsonMap, String param) {
        if (param.contains(FlowWriteConfig.IS_JSON_KEY_TAG)) {
            return JsonParseUtil.getValue(jsonMap, param.substring(2));
        } else {
            return param;
        }
    }

    /**
     * IF函数实现，解析日志构建三目运算;包含判断是否为数字若为数字则转换为long类型返回结果。
     *
     * @param jsonMap 内存实体类
     * @param ifParam 字段名/普通字符串
     * @return resultA or resultB or null
     */
    static Object condition(Map<String, Object> jsonMap, String ifParam) {
        Object result = null;
        try {
            String[] split = ifParam.split(FlowWriteConfig.FORMAT_SPLITTER);
            if (split.length == FlowWriteConfig.IF_PARAM_LENGTH) {
                String[] norms = split[0].split(FlowWriteConfig.IF_CONDITION_SPLITTER);
                Object direction = isJsonValue(jsonMap, norms[0]);
                Object resultA = isJsonValue(jsonMap, split[1]);
                Object resultB = isJsonValue(jsonMap, split[2]);
                if (direction instanceof Number) {
                    result = (Integer.parseInt(direction.toString()) == Integer.parseInt(norms[1])) ? resultA : resultB;
                } else if (direction instanceof String) {
                    result = direction.equals(norms[1]) ? resultA : resultB;
                }
            }
        } catch (RuntimeException e) {
            logger.error("IF function execution exception, exception information:" + e.getMessage());
        }
        return result;
    }
}
