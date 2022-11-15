package com.zdjizhi.tools.general;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.hdfs.HdfsFileUtils;
import com.zdjizhi.utils.IpLookupV2;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class IpLookupUtils {
    private static final Log logger = LogFactory.get();

    /**
     * 初始化定位库，根据文件系统类型判断从何地获取文件。
     */
    public static IpLookupV2 initIpLookup() {
        IpLookupV2 ipLookupV2 = null;
        try {
            ipLookupV2 = new IpLookupV2.Builder(false)
                    .loadDataFileV4(getFileInputStream("ip_v4_built_in.mmdb"))
                    .loadDataFileV6(getFileInputStream("ip_v4_user_defined.mmdb"))
                    .loadDataFilePrivateV4(getFileInputStream("ip_v6_built_in.mmdb"))
                    .loadDataFilePrivateV6(getFileInputStream("ip_v6_user_defined.mmdb"))
                    .loadAsnDataFile(getFileInputStream("asn_v4.mmdb"))
                    .loadAsnDataFileV6(getFileInputStream("asn_v6.mmdb"))
                    .build();
        } catch (RuntimeException e) {
            logger.error("IpLookupUtils init data error, please check! message is:" + e.getMessage());
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
            ipLookupV2 = new IpLookupV2.Builder(false)
                    .loadDataFileV4(new ByteArrayInputStream(knowledgeFileCache.get("ip_v4_built_in.mmdb")))
                    .loadDataFileV6(new ByteArrayInputStream(knowledgeFileCache.get("ip_v6_built_in.mmdb")))
                    .loadDataFilePrivateV4(new ByteArrayInputStream(knowledgeFileCache.get("ip_v4_user_defined.mmdb")))
                    .loadDataFilePrivateV6(new ByteArrayInputStream(knowledgeFileCache.get("ip_v6_user_defined.mmdb")))
                    .loadAsnDataFile(new ByteArrayInputStream(knowledgeFileCache.get("asn_v4.mmdb")))
                    .loadAsnDataFileV6(new ByteArrayInputStream(knowledgeFileCache.get("asn_v6.mmdb")))
                    .build();
        } catch (RuntimeException e) {
            logger.error("IpLookupUtils update data error,message is:" + e.getMessage());
            e.printStackTrace();
        }
        return ipLookupV2;
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
            if (FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_SYSTEM_TYPE)) {
                byte[] fileBytes = HdfsFileUtils.downloadFileByBytes(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + name);
                if (fileBytes != null) {
                    inputStream = new ByteArrayInputStream(fileBytes);
                }
            } else {
                byte[] fileBytes = new FileReader(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + name).readBytes();
                if (fileBytes != null) {
                    inputStream = new ByteArrayInputStream(fileBytes);
                }
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
