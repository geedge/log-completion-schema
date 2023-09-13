package com.zdjizhi.tools.general;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.*;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.geedgenetworks.utils.IpLookupV2;
import com.geedgenetworks.utils.StringUtil;
import com.google.common.base.Joiner;
import com.zdjizhi.common.CommonConfig;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.common.pojo.KnowlegeBaseMeta;
import com.zdjizhi.tools.connections.http.HttpClientService;
import com.zdjizhi.tools.connections.nacos.NacosConnection;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.concurrent.Executor;

/**
 * @author qidaijie
 * @version 2022/11/16 15:23
 */
public class IpLookupUtils {
    private static final Log logger = LogFactory.get();
    private static final String ipv4BuiltInName = "ip_v4_built_in.mmdb";
    private static final String ipv6BuiltInName = "ip_v6_built_in.mmdb";
    private static final String ipv4UserDefinedName = "ip_v4_user_defined.mmdb";
    private static final String ipv6UserDefinedName = "ip_v6_user_defined.mmdb";
    private static final String asnV4Name = "asn_v4.mmdb";
    private static final String asnV6Name = "asn_v6.mmdb";

    /**
     * ip定位库
     */
    private static IpLookupV2 ipLookup;

    /**
     * 定位库默认分隔符
     */
    private static final String LOCATION_SEPARATOR = ".";

    /**
     * 最大重试次数
     */
    private static final int TRY_TIMES = 5;

    /**
     * http connections
     */
    private static final HttpClientService httpClientService;

    /**
     * 定位库元数据缓存
     */
    private static final HashMap<String, KnowlegeBaseMeta> knowledgeMetaCache = new HashMap<>(16);

    static {
        JSONPath jsonPath = JSONPath.of(getFilterParameter());
        httpClientService = new HttpClientService();

        NacosConnection nacosConnection = new NacosConnection();
        ConfigService schemaService = nacosConnection.getPublicService();
        try {
            String configInfo = schemaService.getConfigAndSignListener(FlowWriteConfig.NACOS_KNOWLEDGEBASE_DATA_ID, FlowWriteConfig.NACOS_PUBLIC_GROUP, FlowWriteConfig.NACOS_CONNECTION_TIMEOUT, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    if (StringUtil.isNotBlank(configInfo)) {
                        updateIpLookup(jsonPath, configInfo);
                    }
                }
            });

            if (StringUtil.isNotBlank(configInfo)) {
                updateIpLookup(jsonPath, configInfo);
            }
        } catch (NacosException e) {
            logger.error("Get Schema config from Nacos error,The exception message is :" + e.getMessage());
        }
    }

    private static void updateIpLookup(JSONPath jsonPath, String configInfo) {
        String extract = jsonPath.extract(JSONReader.of(configInfo)).toString();
        if (StringUtil.isNotBlank(extract)) {
            JSONArray jsonArray = JSON.parseArray(extract);
            if (jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    KnowlegeBaseMeta knowlegeBaseMeta = JSONObject.parseObject(jsonArray.getString(i), KnowlegeBaseMeta.class);
                    String fileName = Joiner.on(LOCATION_SEPARATOR).useForNull("").join(knowlegeBaseMeta.getName(), knowlegeBaseMeta.getFormat());
                    knowledgeMetaCache.put(fileName, knowlegeBaseMeta);
                }
                reloadIpLookup();
            }
        }
    }

    /**
     * 从HDFS下载文件更新IpLookup
     *
     * @return 更新后的IpLookup
     */
    public static void reloadIpLookup() {
        int retryNum = 0;
        IpLookupV2.Builder builder = new IpLookupV2.Builder(false);
        for (String fileName : knowledgeMetaCache.keySet()) {
            KnowlegeBaseMeta knowlegeBaseMeta = knowledgeMetaCache.get(fileName);
            String metaSha256 = knowlegeBaseMeta.getSha256();
            do {
                byte[] httpGetByte = httpClientService.httpGetByte(knowlegeBaseMeta.getPath(), FlowWriteConfig.HTTP_SOCKET_TIMEOUT);
                if (httpGetByte.length > 0) {
                    String downloadFileSha256 = DigestUtil.sha256Hex(httpGetByte);
                    if (metaSha256.equals(downloadFileSha256)) {
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(httpGetByte);
                        switch (fileName) {
                            case ipv4BuiltInName:
                                builder.loadDataFileV4(inputStream);
                                break;
                            case ipv6BuiltInName:
                                builder.loadDataFileV6(inputStream);
                                break;
                            case ipv4UserDefinedName:
                                builder.loadDataFilePrivateV4(inputStream);
                                break;
                            case ipv6UserDefinedName:
                                builder.loadDataFilePrivateV6(inputStream);
                                break;
                            case asnV4Name:
                                builder.loadAsnDataFileV4(inputStream);
                                break;
                            case asnV6Name:
                                builder.loadAsnDataFileV6(inputStream);
                                break;
                            default:
                        }
                        retryNum = TRY_TIMES;
                    } else {
                        logger.error("通过HOS下载{}的sha256为:{} ,Nacos内记录为:{} ,sha256不相等 开始第{}次重试下载文件", fileName, downloadFileSha256, metaSha256, retryNum);
                        retryNum++;
                    }
                } else {
                    logger.error("通过HOS下载{}的流为空 ,开始第{}次重试下载文件", fileName, retryNum);
                    retryNum++;
                }
            } while (retryNum < TRY_TIMES);
        }
        ipLookup = builder.build();
    }

    /**
     * 根据配置组合生成知识库元数据过滤参数
     *
     * @return 过滤参数
     */
    private static String getFilterParameter() {
        String[] typeList = CommonConfig.KNOWLEDGEBASE_TYPE_LIST.split(",");
        String[] nameList = CommonConfig.KNOWLEDGEBASE_NAME_LIST.split(",");
        String expr = "[?(@.version=='latest')]";

        if (typeList.length > 1) {
            StringBuilder typeBuilder = new StringBuilder();
            typeBuilder.append("[?(@.type in (");
            for (int i = 0; i < typeList.length; i++) {
                if (i == typeList.length - 1) {
                    typeBuilder.append("'").append(typeList[i]).append("'))]");
                } else {
                    typeBuilder.append("'").append(typeList[i]).append("',");
                }
            }
            expr = expr + typeBuilder;
        }

        if (nameList.length > 1) {
            StringBuilder nameBuilder = new StringBuilder();
            nameBuilder.append("[?(@.name in (");
            for (int i = 0; i < nameList.length; i++) {
                if (i == nameList.length - 1) {
                    nameBuilder.append("'").append(nameList[i]).append("'))]");
                } else {
                    nameBuilder.append("'").append(nameList[i]).append("',");
                }
            }
            expr = expr + nameBuilder;
        }

        return expr;
    }

    public static IpLookupV2 getIpLookup() {
        return ipLookup;
    }
}
