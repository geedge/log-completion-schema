package com.zdjizhi.tools.general;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.hadoop.HadoopUtils;
import com.zdjizhi.utils.IpLookupV2;
import com.zdjizhi.utils.StringUtil;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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
     * 从HDFS下载文件更新IpLookup
     *
     * @param knowledgeFileCache 广播流下发的元数据信息，包含更新内容
     * @return 更新后的IpLookup
     */
    public static IpLookupV2 updateIpLookup(Map<String, String> knowledgeFileCache) {
        IpLookupV2.Builder builder = new IpLookupV2.Builder(false);
//        try {
        InputStream ipv4BuiltInFile = getUpdateFile(ipv4BuiltInName, knowledgeFileCache.get(ipv4BuiltInName));
        if (ipv4BuiltInFile != null) {
            builder.loadDataFileV4(ipv4BuiltInFile);
        }

        InputStream ipv6BuiltInFileFile = getUpdateFile(ipv6BuiltInName, knowledgeFileCache.get(ipv6BuiltInName));
        if (ipv6BuiltInFileFile != null) {
            builder.loadDataFileV6(ipv6BuiltInFileFile);
        }

        InputStream ipv4UserDefinedFile = getUpdateFile(ipv4UserDefinedName, knowledgeFileCache.get(ipv4UserDefinedName));
        if (ipv4UserDefinedFile != null) {
            builder.loadDataFilePrivateV4(ipv4UserDefinedFile);
        }

        InputStream ipv6UserDefinedFile = getUpdateFile(ipv6UserDefinedName, knowledgeFileCache.get(ipv6UserDefinedName));
        if (ipv6UserDefinedFile != null) {
            builder.loadDataFilePrivateV6(ipv6UserDefinedFile);
        }

        InputStream asnV4File = getUpdateFile(asnV4Name, knowledgeFileCache.get(asnV4Name));
        if (asnV4File != null) {
            builder.loadAsnDataFileV4(asnV4File);
        }

        InputStream asnV6File = getUpdateFile(asnV6Name, knowledgeFileCache.get(asnV6Name));
        if (asnV6File != null) {
            builder.loadAsnDataFileV6(asnV6File);
        }
        return builder.build();
    }

    /**
     * 从HDFS下载文件初始化IpLookup
     *
     * @return 初始化的IpLookup
     */
    public static IpLookupV2 initIpLookup() {
        IpLookupV2.Builder builder = new IpLookupV2.Builder(false);
        InputStream ipv4BuiltInFile = getInitFile(ipv4BuiltInName);
        if (ipv4BuiltInFile != null) {
            builder.loadDataFileV4(ipv4BuiltInFile);
        }

        InputStream ipv6BuiltInFileFile = getInitFile(ipv6BuiltInName);
        if (ipv6BuiltInFileFile != null) {
            builder.loadDataFileV6(ipv6BuiltInFileFile);
        }

        InputStream ipv4UserDefinedFile = getInitFile(ipv4UserDefinedName);
        if (ipv4UserDefinedFile != null) {
            builder.loadDataFilePrivateV4(ipv4UserDefinedFile);
        }

        InputStream ipv6UserDefinedFile = getInitFile(ipv6UserDefinedName);
        if (ipv6UserDefinedFile != null) {
            builder.loadDataFilePrivateV6(ipv6UserDefinedFile);
        }

        InputStream asnV4File = getInitFile(asnV4Name);
        if (asnV4File != null) {
            builder.loadAsnDataFileV4(asnV4File);
        }

        InputStream asnV6File = getInitFile(asnV6Name);
        if (asnV6File != null) {
            builder.loadAsnDataFileV6(asnV6File);
        }
        return builder.build();
    }

    /**
     * 下载更新文件并校验完整性
     *
     * @param fileName 文件名
     * @param sha256   文件校验码
     * @return true or false
     */
    private static InputStream getUpdateFile(String fileName, String sha256) {
        InputStream inputStream = null;
        try {
            if (StringUtil.isNotBlank(sha256)) {
                byte[] downloadFileBytes = downloadFile(fileName);
                if (downloadFileBytes != null) {
                    String downloadFileSha256Hex = DigestUtil.sha256Hex(downloadFileBytes);
                    if (sha256.equals(downloadFileSha256Hex)) {
                        inputStream = new ByteArrayInputStream(downloadFileBytes);
                    } else {
                        logger.error("The downloaded file {} sha256 is inconsistent!", fileName);
                    }
                }
            } else {
                byte[] downloadFileBytes = downloadFile(fileName);
                if (downloadFileBytes != null) {
                    inputStream = new ByteArrayInputStream(downloadFileBytes);
                }
            }
        } catch (RuntimeException e) {
            logger.error("Failed to download the {} file for update cache, Exception message:{}", fileName, e.getMessage());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return inputStream;
    }

    /**
     * 下载文件（初始化）
     *
     * @param fileName 文件名
     * @return 文件io流
     */
    private static InputStream getInitFile(String fileName) {
        InputStream inputStream = null;
        try {
            byte[] fileBytes = downloadFile(fileName);
            if (fileBytes != null) {
                inputStream = new ByteArrayInputStream(fileBytes);
            }
        } catch (RuntimeException e) {
            logger.error("Failed to download the {} file for initialization cache, The exception message is:{}", fileName, e.getMessage());
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return inputStream;
    }

    /**
     * 根据不同文件系统获取定位库io流
     *
     * @param fileName 文件名
     * @return 文件io流
     */
    private static byte[] downloadFile(String fileName) {
        byte[] fileBytes = null;
        try {
            String filePath = FlowWriteConfig.KNOWLEDGEBASE_FILE_STORAGE_PATH + fileName;
            fileBytes = FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_STORAGE_TYPE) ?
                    HadoopUtils.downloadFileByBytes(filePath) : new FileReader(filePath).readBytes();
        } catch (RuntimeException e) {
            logger.error("Failed to download file {} from {}, The exception message is:{}", FlowWriteConfig.KNOWLEDGEBASE_FILE_STORAGE_TYPE, fileName, e.getMessage());
            e.printStackTrace();
        }
        return fileBytes;
    }

}
