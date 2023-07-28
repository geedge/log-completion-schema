package com.zdjizhi.operator.broadcast;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.alibaba.fastjson2.*;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.base.Joiner;
import com.zdjizhi.common.CommonConfig;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.common.pojo.KnowlegeBaseMeta;
import com.zdjizhi.tools.connections.hadoop.HadoopUtils;
import com.zdjizhi.tools.connections.http.HttpClientService;
import com.zdjizhi.tools.connections.nacos.NacosConnection;
import com.zdjizhi.utils.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author qidaijie
 * @version 2022/11/3 10:37
 */
public class KnowledgeBaseSource extends RichSourceFunction<Map<String, String>> {
    private static final Log logger = LogFactory.get();
    /**
     * 定位库默认分隔符
     */
    private static final String LOCATION_SEPARATOR = ".";
    //最大重试次数
    private static final int TRY_TIMES = 5;
    //定位库元数据缓存
    private static HashMap<String, String> knowledgeMetaCache;
    //定位库更新数据缓存
    private static HashMap<String, String> knowledgeUpdateCache;
    //http connections
    private static HttpClientService httpClientService;
    //nacos Service
    private ConfigService nacosConfigService;
    //jsonpath
    private JSONPath jsonPath;
    //运行状态cancel时置为false
    private boolean isRunning = true;


    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        jsonPath = JSONPath.of(getFilterParameter());
        //初始化http连接
        httpClientService = new HttpClientService();
        //初始化元数据缓存
        knowledgeMetaCache = new HashMap<>(16);
        //初始化定位库更新数据缓存
        knowledgeUpdateCache = new HashMap<>(16);
        //连接nacos的配置
        createNacosConnection();
        //初始化
        initKnowledge();
    }

    @Override
    public void run(SourceContext<Map<String, String>> ctx) {
        try {
            sendUpdateCache(ctx);

            nacosConfigService.addListener(FlowWriteConfig.NACOS_KNOWLEDGEBASE_DATA_ID, FlowWriteConfig.NACOS_PUBLIC_GROUP, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configMsg) {
                    if (StringUtil.isNotBlank(configMsg)) {
                        String extract = jsonPath.extract(JSONReader.of(configMsg)).toString();
                        if (StringUtil.isNotBlank(extract)) {
                            JSONArray jsonArray = JSON.parseArray(extract);
                            if (jsonArray.size() > 0) {
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    KnowlegeBaseMeta knowlegeBaseMeta = JSONObject.parseObject(jsonArray.getString(i), KnowlegeBaseMeta.class);
                                    String fileName = Joiner.on(LOCATION_SEPARATOR).useForNull("").join(knowlegeBaseMeta.getName(), knowlegeBaseMeta.getFormat());
                                    String sha256 = knowlegeBaseMeta.getSha256();
                                    String filePath = knowlegeBaseMeta.getPath();
                                    if (!sha256.equals(knowledgeMetaCache.get(fileName))) {
                                        updateKnowledge(fileName, filePath, sha256);
                                    }
                                }
                                sendUpdateCache(ctx);
                            }
                        }
                    }
                }
            });
        } catch (NacosException e) {
            logger.error("Get Schema config from Nacos error,The exception message is :" + e.getMessage());
        }

        while (isRunning) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void cancel() {
        this.isRunning = false;
    }

    private void createNacosConnection() {
        NacosConnection nacosConnection = new NacosConnection();
        nacosConfigService = nacosConnection.getPublicService();
    }

    private void sendUpdateCache(SourceContext<Map<String, String>> ctx) {
        if (!knowledgeUpdateCache.isEmpty()) {
            ctx.collect(knowledgeUpdateCache);
            knowledgeUpdateCache.clear();
        }
    }

    /**
     * 初始化
     */
    private void initKnowledge() {
        String configMsg = "";
        try {
            configMsg = nacosConfigService.getConfig(FlowWriteConfig.NACOS_KNOWLEDGEBASE_DATA_ID,
                    FlowWriteConfig.NACOS_PUBLIC_GROUP,
                    FlowWriteConfig.NACOS_CONNECTION_TIMEOUT);
        } catch (NacosException e) {
            logger.error("从Nacos获取知识库元数据配置文件异常，异常信息为:{}", e.getMessage());
        }

        if (StringUtil.isNotBlank(configMsg)) {
            String extract = jsonPath.extract(JSONReader.of(configMsg)).toString();
            if (StringUtil.isNotBlank(extract)) {
                JSONArray jsonArray = JSON.parseArray(extract);
                if (jsonArray.size() > 0) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        KnowlegeBaseMeta knowlegeBaseMeta = JSONObject.parseObject(jsonArray.getString(i), KnowlegeBaseMeta.class);
                        String fileName = Joiner.on(LOCATION_SEPARATOR).useForNull("").join(knowlegeBaseMeta.getName(), knowlegeBaseMeta.getFormat());
                        String sha256 = knowlegeBaseMeta.getSha256();
                        String filePath = knowlegeBaseMeta.getPath();
                        byte[] localFile = getLocalFile(fileName);
                        String localFileSha256 = DigestUtil.sha256Hex(localFile);
                        if (sha256.equals(localFileSha256)) {
                            knowledgeMetaCache.put(fileName, sha256);
                        } else {
                            logger.info("{}的sha256为：{} ,Nacos记录为：{},sha256不相等开始更新本地文件", fileName, localFileSha256, sha256);
                            updateKnowledge(fileName, filePath, sha256);
                        }
                    }
                }
            }
        } else {
            logger.error("从Nacos获取知识库元数据配置为空，广播流初始化异常。");
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
                inputStream = httpClientService.httpGetInputStream(filePath, FlowWriteConfig.HTTP_SOCKET_TIMEOUT);
                if (inputStream != null) {
                    byte[] downloadBytes = IOUtils.toByteArray(inputStream);
                    String downloadFileSha256 = DigestUtil.sha256Hex(downloadBytes);
                    if (sha256.equals(downloadFileSha256) && downloadBytes.length > 0) {
                        boolean updateStatus = updateLocalFile(fileName, downloadBytes);
                        if (updateStatus) {
                            knowledgeMetaCache.put(fileName, sha256);
                            knowledgeUpdateCache.put(fileName, sha256);
                            retryNum = TRY_TIMES;
                            logger.info("{}文件更新成功！", fileName);
                        } else {
                            logger.info("{}文件更新本地失败，等待重试！", fileName);
                            retryNum++;
                        }
                    } else {
                        logger.error("通过HOS下载{}的sha256为:{} ,Nacos内记录为:{} ,sha256不相等 开始第{}次重试下载文件", fileName, downloadFileSha256, sha256, retryNum);
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
    private boolean updateLocalFile(String fileName, byte[] downloadBytes) {
        FileOutputStream outputStream = null;
        boolean updateStatus = false;
        try {
            String filePath = FlowWriteConfig.KNOWLEDGEBASE_FILE_STORAGE_PATH + fileName;
            if (FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_STORAGE_TYPE)) {
                HadoopUtils.uploadFileByBytes(filePath, downloadBytes);
                updateStatus = true;
            } else {
                File file = new File(filePath);
                outputStream = new FileOutputStream(file);
                IoUtil.copy(new ByteArrayInputStream(downloadBytes), outputStream);
                updateStatus = true;
            }
        } catch (IOException ioe) {
            logger.error("更新本地文件{}时发生IO异常,异常信息为:{}", fileName, ioe.getMessage());
            ioe.printStackTrace();
        } catch (RuntimeException e) {
            logger.error("更新本地文件{}时发生异常,异常信息为:{}", fileName, e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return updateStatus;
    }

    /**
     * 根据不同文件系统获取定位库io流
     *
     * @param fileName 文件名
     * @return 文件io流
     */
    private byte[] getLocalFile(String fileName) {
        byte[] fileBytes = null;
        try {
            String filePath = FlowWriteConfig.KNOWLEDGEBASE_FILE_STORAGE_PATH + fileName;
            fileBytes = FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_STORAGE_TYPE) ?
                    HadoopUtils.downloadFileByBytes(filePath) : new FileReader(filePath).readBytes();
        } catch (RuntimeException e) {
            logger.error("下载本地文件失败，异常信息为:{}", e.getMessage());
            e.printStackTrace();
        }
        return fileBytes;
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
            expr = expr + typeBuilder.toString();
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
            expr = expr + nameBuilder.toString();
        }

        return expr;
    }

}
