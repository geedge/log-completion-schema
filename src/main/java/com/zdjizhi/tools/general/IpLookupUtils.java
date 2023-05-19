package com.zdjizhi.tools.general;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.hadoop.HadoopUtils;
import com.zdjizhi.utils.IpLookupV2;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

/**
 * @author qidaijie
 * @version 2022/11/16 15:23
 */
public class IpLookupUtils {
    private static final Log logger = LogFactory.get();

    /**
     * 初始化定位库，根据文件系统类型判断从何地获取文件。
     */
    public static IpLookupV2 initIpLookup() {
        System.out.println("Time:" + System.currentTimeMillis() + " ETL-" + FlowWriteConfig.SINK_KAFKA_TOPIC + "开始初始化定位库信息！");
        IpLookupV2 ipLookupV2 = null;
        try {
            ipLookupV2 = new IpLookupV2.Builder(false)
                    .loadDataFileV4(getFileInputStream("ip_v4_built_in.mmdb"))
                    .loadDataFileV6(getFileInputStream("ip_v6_built_in.mmdb"))
                    .loadDataFilePrivateV4(getFileInputStream("ip_v4_user_defined.mmdb"))
                    .loadDataFilePrivateV6(getFileInputStream("ip_v6_user_defined.mmdb"))
                    .loadAsnDataFile(getFileInputStream("asn_v4.mmdb"))
                    .loadAsnDataFileV6(getFileInputStream("asn_v6.mmdb"))
                    .build();
            System.out.println("Time:" + System.currentTimeMillis() + " ETL-" + FlowWriteConfig.SINK_KAFKA_TOPIC + "定位库信息初始化完成！");
        } catch (RuntimeException e) {
            logger.error("IpLookupUtils init data error, please check! message is:" + e.getMessage());
            System.out.println("Time:" + System.currentTimeMillis() + " ETL-" + FlowWriteConfig.SINK_KAFKA_TOPIC + "定位库信息初始化失败！");
            e.printStackTrace();
        }
        return ipLookupV2;
    }

    /**
     * 更新定位库
     *
     * @param knowledgeFileCache 定位库缓存
     * @return 工具类
     */
    public static IpLookupV2 updateIpLookup(Map<String, byte[]> knowledgeFileCache) {
        IpLookupV2 ipLookupV2 = null;
        try {
            if (knowledgeFileCache.size()==6 && knowledgeIntegrityCheck(knowledgeFileCache)) {
                ipLookupV2 = new IpLookupV2.Builder(false)
                        .loadDataFileV4(new ByteArrayInputStream(knowledgeFileCache.get("ip_v4_built_in.mmdb")))
                        .loadDataFileV6(new ByteArrayInputStream(knowledgeFileCache.get("ip_v6_built_in.mmdb")))
                        .loadDataFilePrivateV4(new ByteArrayInputStream(knowledgeFileCache.get("ip_v4_user_defined.mmdb")))
                        .loadDataFilePrivateV6(new ByteArrayInputStream(knowledgeFileCache.get("ip_v6_user_defined.mmdb")))
                        .loadAsnDataFileV4(new ByteArrayInputStream(knowledgeFileCache.get("asn_v4.mmdb")))
                        .loadAsnDataFileV6(new ByteArrayInputStream(knowledgeFileCache.get("asn_v6.mmdb")))
                        .build();
            }
        } catch (RuntimeException e) {
            logger.error("IpLookupUtils update data error,message is:" + e.getMessage());
            e.printStackTrace();
        }
        return ipLookupV2;
    }

    /**
     * 校验广播流内的知识库完整性
     *
     * @param knowledgeFileCache 知识库文件流
     * @return true or false
     */
    private static Boolean knowledgeIntegrityCheck(Map<String, byte[]> knowledgeFileCache) {
        boolean integrity = true;
        for (String key : knowledgeFileCache.keySet()) {
            logger.error("当前更新的{}文件字节大小{}", key, knowledgeFileCache.get(key).length);
            if (knowledgeFileCache.get(key) == null) {
                integrity = false;
            }
        }
        return integrity;
    }

    /**
     * 根据不同文件系统获取定位库io流
     *
     * @param name 文件名
     * @return 文件io流
     */
    private static InputStream getFileInputStream(String name) {
        InputStream inputStream = null;
        try {
            byte[] fileBytes = FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_SYSTEM_TYPE) ?
                    HadoopUtils.downloadFileByBytes(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + name) :
                    new FileReader(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + name).readBytes();

            if (fileBytes != null) {
                inputStream = new ByteArrayInputStream(fileBytes);
            }
        } catch (RuntimeException e) {
            logger.error("IpLookupUtils download MMDB files error, message is:" + e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return inputStream;
    }


}
