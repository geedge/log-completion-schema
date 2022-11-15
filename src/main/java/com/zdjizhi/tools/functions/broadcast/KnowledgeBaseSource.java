package com.zdjizhi.tools.functions.broadcast;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
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
import com.zdjizhi.common.CustomFile;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.hdfs.HdfsFileUtils;
import com.zdjizhi.tools.http.HttpClientUtils;
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
 * @Package com.zdjizhi.tools.functions
 * @Description:
 * @date 2022/11/3 10:37
 */
public class KnowledgeBaseSource extends RichSourceFunction<Map<String, byte[]>> {
    private static final Log logger = LogFactory.get();
    //JSONpath 表达式
    private static final String EXPR = "$.[?(@.version=='latest' && @.name in ['ip_v4_built_in','ip_v6_built_in','ip_v4_user_defined','ip_v6_user_defined','asn_v4','asn_v6'])].['name','sha256','format','path']";
    //定位库元数据缓存
    private static HashMap<String, String> knowledgeMetaCache;
    //定位库缓存
    private static HashMap<String, byte[]> knowledgeFileCache;
    //nacos Service
    private ConfigService configService;
    private boolean isRunning = true;


    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        //初始化元数据缓存
        knowledgeMetaCache = new HashMap<>(16);
        //初始化定位库缓存
        knowledgeFileCache = new HashMap<>(16);
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
                ArrayList<Object> metaList = JsonPath.parse(configMsg).read(EXPR);
                loadKnowledge(metaList);
            }

            configService.addListener(dataId, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configMsg) {
                    if (StringUtil.isNotBlank(configMsg)) {
                        ArrayList<Object> metaList = JsonPath.parse(configMsg).read(EXPR);
                        if (metaList.size() >= 1) {
                            for (Object metadata : metaList) {
                                JSONObject knowledgeJson = new JSONObject(metadata, false, true);
                                String fileName = Joiner.on(FlowWriteConfig.LOCATION_SEPARATOR).useForNull("").join(knowledgeJson.getStr("name"),
                                        knowledgeJson.getStr("format"));
                                String sha256 = knowledgeJson.getStr("sha256");
                                String filePath = knowledgeJson.getStr("path");
                                if (!sha256.equals(knowledgeMetaCache.get(fileName))) {
                                    knowledgeMetaCache.put(fileName, sha256);
                                    updateKnowledge(fileName, filePath);
                                }

                            }
                            ctx.collect(knowledgeFileCache);
                        }
                    }
                }
            });

            while (isRunning) {
                try {
                    Thread.sleep(10000);
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
     * @param metaList
     */
    private void loadKnowledge(ArrayList<Object> metaList) {
        InputStream inputStream = null;
        try {
            if (metaList.size() >= 1) {
                for (Object metadata : metaList) {
                    JSONObject knowledgeJson = new JSONObject(metadata, false, true);
                    String fileName = Joiner.on(FlowWriteConfig.LOCATION_SEPARATOR).useForNull("").join(knowledgeJson.getStr("name"),
                            knowledgeJson.getStr("format"));
                    String sha256 = knowledgeJson.getStr("sha256");
                    String filePath = knowledgeJson.getStr("path");
                    Header header = new BasicHeader("token", FlowWriteConfig.HOS_TOKEN);
                    HttpClientUtils httpClientUtils = new HttpClientUtils();
                    inputStream = httpClientUtils.httpGetInputStream(filePath, 3000, header);
                    knowledgeMetaCache.put(fileName, sha256);
                    knowledgeFileCache.put(fileName, IOUtils.toByteArray(inputStream));
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 根据知识库返回的地址下载文件，并判断使用的文件系统类型对应更新本地文件
     *
     * @param fileName 文件名
     * @param filePath 文件下载地址
     */
    private void updateKnowledge(String fileName, String filePath) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            Header header = new BasicHeader("token", FlowWriteConfig.HOS_TOKEN);
            HttpClientUtils httpClientUtils = new HttpClientUtils();
            inputStream = httpClientUtils.httpGetInputStream(filePath, 3000, header);
            if (FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_SYSTEM_TYPE)) {
                byte[] bytes = IOUtils.toByteArray(inputStream);
                HdfsFileUtils.uploadFileByBytes(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + fileName, bytes);
                knowledgeFileCache.put(fileName, bytes);
            } else {
                FileUtil.mkdir(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY);
                File file = new File(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY.concat(File.separator).concat(fileName));
                outputStream = new FileOutputStream(file);
                byte[] bytes = IOUtils.toByteArray(inputStream);
                knowledgeFileCache.put(fileName, bytes);
                IoUtil.copy(new ByteArrayInputStream(knowledgeFileCache.get(fileName)), outputStream);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    @Override
    public void cancel() {
        this.isRunning = false;
    }
}
