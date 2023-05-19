package com.zdjizhi.tools.functions.broadcast;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.base.Joiner;
import com.jayway.jsonpath.JsonPath;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.hadoop.HadoopUtils;
import com.zdjizhi.tools.http.HttpClientService;
import com.zdjizhi.utils.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author qidaijie
 * @version 2022/11/3 10:37
 */
public class KnowledgeBaseSource extends RichSourceFunction<Map<String, byte[]>> {
    private static final Log logger = LogFactory.get();
    //最大重试次数
    private static final int TRY_TIMES = 3;
    //JSONpath 表达式
    private static final String KNOWLEDGE_EXPR = "$.[?(@.version=='latest' && @.name in ['ip_v4_built_in','ip_v6_built_in','ip_v4_user_defined','ip_v6_user_defined','asn_v4','asn_v6'])].['name','sha256','format','path']";
    //定位库元数据缓存
    private static HashMap<String, String> knowledgeMetaCache;
    //定位库缓存
    private static HashMap<String, byte[]> knowledgeFileCache;
    //http connections
    private static HttpClientService httpClientService;
    //nacos Service
    private ConfigService configService;
    //http header
    private static Header header;
    //运行状态cancel时置为false
    private boolean isRunning = true;
    //是否下发，默认不发送
    private boolean isSending = false;


    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        //初始化http连接
        httpClientService = new HttpClientService();
        //初始化元数据缓存
        knowledgeMetaCache = new HashMap<>(16);
        //初始化定位库缓存
        knowledgeFileCache = new HashMap<>(16);
        //token
        header = new BasicHeader("token", FlowWriteConfig.HOS_TOKEN);
        //连接nacos的配置
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, FlowWriteConfig.NACOS_SERVER);
        properties.setProperty(PropertyKeyConst.NAMESPACE, FlowWriteConfig.NACOS_KNOWLEDGEBASE_NAMESPACE);
        properties.setProperty(PropertyKeyConst.USERNAME, FlowWriteConfig.NACOS_USERNAME);
        properties.setProperty(PropertyKeyConst.PASSWORD, FlowWriteConfig.NACOS_PIN);
        configService = NacosFactory.createConfigService(properties);
    }

    @Override
    public void run(SourceContext<Map<String, byte[]>> ctx) {
        try {
            String dataId = FlowWriteConfig.NACOS_KNOWLEDGEBASE_DATA_ID;
            String group = FlowWriteConfig.NACOS_KNOWLEDGEBASE_GROUP;
            String configMsg = configService.getConfig(dataId, group, 5000);
            if (StringUtil.isNotBlank(configMsg)) {
                ArrayList<Object> metaList = JsonPath.parse(configMsg).read(KNOWLEDGE_EXPR);
                loadKnowledge(metaList);
                if (isSending) {
                    ctx.collect(knowledgeFileCache);
                }
            }

            configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configMsg) {
                    logger.error("知识库监听调用，返回信息:" + configMsg);
                    if (StringUtil.isNotBlank(configMsg)) {
                        ArrayList<Object> metaList = JsonPath.parse(configMsg).read(KNOWLEDGE_EXPR);
                        if (metaList.size() >= 1) {
                            for (Object metadata : metaList) {
                                JSONObject knowledgeJson = new JSONObject(metadata, false, true);
                                String fileName = Joiner.on(FlowWriteConfig.LOCATION_SEPARATOR).useForNull("").join(knowledgeJson.getStr("name"),
                                        knowledgeJson.getStr("format"));
                                String sha256 = knowledgeJson.getStr("sha256");
                                String filePath = knowledgeJson.getStr("path");
                                if (!sha256.equals(knowledgeMetaCache.get(fileName))) {
                                    knowledgeMetaCache.put(fileName, sha256);
                                    updateKnowledge(fileName, filePath, sha256);
                                }
                            }
                            if (isSending) {
                                ctx.collect(knowledgeFileCache);
                            }
                        }
                    }
                }
            });

            while (isRunning) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (NacosException e) {
            logger.error("Get Schema config from Nacos error,The exception message is :" + e.getMessage());
        }
    }

    /**
     * 初始化时，获取全部的
     *
     * @param metaList 元数据
     */
    private void loadKnowledge(ArrayList<Object> metaList) {
        if (metaList.size() >= 1) {
            try {
                for (Object metadata : metaList) {
                    JSONObject knowledgeJson = new JSONObject(metadata, false, true);
                    String fileName = Joiner.on(FlowWriteConfig.LOCATION_SEPARATOR).useForNull("").join(knowledgeJson.getStr("name"),
                            knowledgeJson.getStr("format"));
                    String sha256 = knowledgeJson.getStr("sha256");
                    String filePath = knowledgeJson.getStr("path");
                    byte[] localFile = getLocalFile(fileName);
                    String localFileSha256Hex = DigestUtil.sha256Hex(localFile);
                    if (sha256.equals(localFileSha256Hex)) {
                        logger.info("本地文件{}的sha256为:{} ,Nacos内记录为:{} ,sha256相等", fileName, localFileSha256Hex, sha256);
                        knowledgeMetaCache.put(fileName, sha256);
                        knowledgeFileCache.put(fileName, localFile);
                    } else {
                        logger.info("本地文件{}的sha256为:{} ,Nacos内记录为:{} ,sha256不相等，更新本地文件及缓存", fileName, localFileSha256Hex, sha256);
                        updateKnowledge(fileName, filePath, sha256);
                    }
                }
            } catch (RuntimeException e) {
                logger.error("初始化知识库缓存出现异常,异常信息为:", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新知识库缓存及本地文件
     *
     * @param fileName 文件名
     * @param filePath 文件下载地址
     */
    private void updateKnowledge(String fileName, String filePath, String sha256) {
        InputStream inputStream = null;
        int retryNum = 0;
        try {
            while (retryNum < TRY_TIMES) {
                inputStream = httpClientService.httpGetInputStream(filePath, FlowWriteConfig.HTTP_SOCKET_TIMEOUT, header);
                if (inputStream != null) {
                    byte[] downloadBytes = IOUtils.toByteArray(inputStream);
                    String downloadFileSha256Hex = DigestUtil.sha256Hex(downloadBytes);
                    if (sha256.equals(downloadFileSha256Hex)) {
                        logger.info("通过HOS下载{}的sha256为:{} ,Nacos内记录为:{} ,sha256相等", fileName, downloadFileSha256Hex, sha256);
                        knowledgeMetaCache.put(fileName, sha256);
                        knowledgeFileCache.put(fileName, downloadBytes);
                        updateLocalFile(fileName);
                        retryNum = TRY_TIMES;
                        isSending = true;
                    } else {
                        logger.error("通过HOS下载{}的sha256为:{} ,Nacos内记录为:{} ,sha256不相等 开始第{}次重试下载文件", fileName, downloadFileSha256Hex, sha256, retryNum);
                        retryNum++;
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 判断使用的文件系统类型对应更新本地文件
     *
     * @param fileName 文件名
     */
    private void updateLocalFile(String fileName) {
        FileOutputStream outputStream = null;
        try {
            if (FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_SYSTEM_TYPE)) {
                HadoopUtils.uploadFileByBytes(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + fileName, knowledgeFileCache.get(fileName));
            } else {
                FileUtil.mkdir(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY);
                File file = new File(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY.concat(File.separator).concat(fileName));
                outputStream = new FileOutputStream(file);
                IoUtil.copy(new ByteArrayInputStream(knowledgeFileCache.get(fileName)), outputStream);
            }
        } catch (IOException ioe) {
            logger.error("更新本地文件{}时发生IO异常,异常信息为:", fileName, ioe.getMessage());
            ioe.printStackTrace();
        } catch (RuntimeException e) {
            logger.error("更新本地文件{}时发生异常,异常信息为:", fileName, e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

    }

    /**
     * 根据不同文件系统获取定位库io流
     *
     * @param name 文件名
     * @return 文件io流
     */
    private static byte[] getLocalFile(String name) {
        byte[] fileBytes = null;
        try {
            fileBytes = FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_SYSTEM_TYPE) ?
                    HadoopUtils.downloadFileByBytes(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + name) :
                    new FileReader(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + name).readBytes();

        } catch (RuntimeException e) {
            logger.error("IpLookupUtils download MMDB files error, message is:" + e.getMessage());
            e.printStackTrace();
        }
        return fileBytes;
    }


    @Override
    public void cancel() {
        this.isRunning = false;
    }
}
