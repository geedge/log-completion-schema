package com.zdjizhi.tools.transform;

import com.alibaba.fastjson2.JSONObject;
import com.geedgenetworks.utils.IpLookupV2;


/**
 * @author qidaijie
 * @Package com.zdjizhi.tools.general
 * @Description:
 * @date 2023/5/2010:11
 */
public interface TransformFunction {

    long getCurrentTime();

    long getSnowflakeId();

    String getGeoIpDetail(String ip);

    String getGeoIpCountry(String ip);

    String getGeoAsn(String ip);

    String radiusMatch(JSONObject jsonObject, String ip);

    void gtpcMatch(JSONObject jsonObject, String logValue, String appendToKey, String param);

    String getTopDomain(String domain);

    String decodeBase64(JSONObject jsonObject,String message, String param);

    Object flattenSpec(String message, String expr);

    Object condition(JSONObject jsonObject, String ifParam);

    void setValue(JSONObject jsonObject, String appendToKey, String param);

    void getValue(JSONObject jsonObject, String appendToKey,  Object logValue);
}
