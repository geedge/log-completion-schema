package com.zdjizhi.utils.general;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.FormatUtils;
import com.zdjizhi.utils.IpLookupV2;
import com.zdjizhi.utils.StringUtil;
import com.zdjizhi.utils.app.AppUtils;
import com.zdjizhi.utils.hbase.HBaseUtils;
import com.zdjizhi.utils.json.JsonParseUtil;
import com.zdjizhi.utils.json.TypeUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qidaijie
 */
class TransFunction {
    private static final Log logger = LogFactory.get();

    /**
     * 校验数字正则
     */
    private static final Pattern PATTERN = Pattern.compile("[0-9]*");

    /**
     * IP定位库工具类
     */
    private static IpLookupV2 ipLookup = new IpLookupV2.Builder(false)
            .loadDataFileV4(FlowWriteConfig.TOOLS_LIBRARY + "ip_v4_built_in.mmdb")
            .loadDataFileV6(FlowWriteConfig.TOOLS_LIBRARY + "ip_v6_built_in.mmdb")
            .loadDataFilePrivateV4(FlowWriteConfig.TOOLS_LIBRARY + "ip_v4_user_defined.mmdb")
            .loadDataFilePrivateV6(FlowWriteConfig.TOOLS_LIBRARY + "ip_v6_user_defined.mmdb")
            .loadAsnDataFile(FlowWriteConfig.TOOLS_LIBRARY + "asn_v4.mmdb")
            .loadAsnDataFileV6(FlowWriteConfig.TOOLS_LIBRARY + "asn_v6.mmdb")
            .build();

    /**
     * 生成当前时间戳的操作
     */
    static long getCurrentTime() {

        return System.currentTimeMillis() / 1000;
    }

    /**
     * CityHash64算法
     * 版本规划暂不实现-TSG22.01
     *
     * @param data 原始数据
     * @return 散列结果
     */
    static BigInteger getDecimalHash(long data) {
        byte[] dataBytes = String.valueOf(data).getBytes();
        long hashValue = CityHash.CityHash64(dataBytes, 0, dataBytes.length);
        String decimalValue = Long.toUnsignedString(hashValue, 10);
        BigInteger result = new BigInteger(decimalValue);
        return result;
    }

    /**
     * 根据clientIp获取location信息
     *
     * @param ip client IP
     * @return ip地址详细信息
     */
    static String getGeoIpDetail(String ip) {
        return ipLookup.cityLookupDetail(ip);
    }

    /**
     * 根据ip获取asn信息
     *
     * @param ip client/server IP
     * @return ASN
     */
    static String getGeoAsn(String ip) {
        return ipLookup.asnLookup(ip);
    }

    /**
     * 根据ip获取country信息
     *
     * @param ip server IP
     * @return 国家
     */
    static String getGeoIpCountry(String ip) {

        return ipLookup.countryLookup(ip);
    }


    /**
     * radius借助HBase补齐
     *
     * @param ip client IP
     * @return account
     */
    static String radiusMatch(String ip) {
        return HBaseUtils.getAccount(ip.trim());
    }

    /**
     * appId与缓存中对应关系补全appName
     *
     * @param appIds app id 列表
     * @return appName
     */
    static String appMatch(String appIds) {
        try {
            String appId = StrUtil.split(appIds, FlowWriteConfig.FORMAT_SPLITTER, true, true).get(0);
            return AppUtils.getAppName(Integer.parseInt(appId));
        } catch (NumberFormatException | ClassCastException exception) {
            logger.error("APP ID列表分割转换异常，异常APP ID列表:" + appIds);
            return "";
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
            logger.error("解析顶级域名异常,异常域名:" + domain);
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
                if (charset == null) {
                    result = Base64.decodeStr(message, FlowWriteConfig.MAIL_DEFAULT_CHARSET);
                } else {
                    result = Base64.decodeStr(message, charset.toString());
                }
            }
        } catch (RuntimeException rune) {
            logger.error("解析 Base64 异常,异常信息:" + rune);
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
            logger.error("设备标签解析异常，[ " + expr + " ]解析表达式错误" + e);
        }
        return flattenResult;
    }


    /**
     * 判断是否为日志字段,是则返回对应value，否则返回原始字符串
     *
     * @param object 内存实体类
     * @param param  字段名/普通字符串
     * @return JSON.Value or String
     */
    static Object isJsonValue(Object object, String param) {
        if (param.contains(FlowWriteConfig.IS_JSON_KEY_TAG)) {
            return JsonParseUtil.getValue(object, param.substring(2));
        } else {
            return param;
        }
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
     * @param object  内存实体类
     * @param ifParam 字段名/普通字符串
     * @return resultA or resultB or null
     */
    static Object condition(Object object, String ifParam) {
        Object result = null;
        try {
            String[] split = ifParam.split(FlowWriteConfig.FORMAT_SPLITTER);
            if (split.length == FlowWriteConfig.IF_PARAM_LENGTH) {
                String[] norms = split[0].split(FlowWriteConfig.IF_CONDITION_SPLITTER);
                Object direction = isJsonValue(object, norms[0]);
                Object resultA = isJsonValue(object, split[1]);
                Object resultB = isJsonValue(object, split[2]);
                if (direction instanceof Number) {
                    result = TypeUtils.castToIfFunction((Integer.parseInt(direction.toString()) == Integer.parseInt(norms[1])) ? resultA : resultB);
                } else if (direction instanceof String) {
                    result = TypeUtils.castToIfFunction(direction.equals(norms[1]) ? resultA : resultB);
                }
            }
        } catch (RuntimeException e) {
            logger.error("IF 函数执行异常,异常信息:" + e);
        }
        return result;
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
            logger.error("IF 函数执行异常,异常信息:" + e);
        }
        return result;
    }


    /**
     * 设置固定值函数 若为数字则转为long返回
     *
     * @param param 默认值
     * @return 返回数字或字符串
     */
    static Object setValue(String param) {
        try {
            Matcher isNum = PATTERN.matcher(param);
            if (isNum.matches()) {
                return Long.parseLong(param);
            } else {
                return param;
            }
        } catch (RuntimeException e) {
            logger.error("SetValue 函数异常，异常信息：" + e);
        }
        return null;
    }
}
