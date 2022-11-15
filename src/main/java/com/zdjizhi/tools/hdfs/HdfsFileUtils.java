package com.zdjizhi.tools.hdfs;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author qidaijie
 * @Package com.zdjizhi.tools.hdfs
 * @Description:
 * @date 2022/11/217:57
 */
public class HdfsFileUtils {
    private static final Log logger = LogFactory.get();

    private static FileSystem fileSystem;

    static {
        if (FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_SYSTEM_TYPE)) {
            Configuration configuration = new Configuration();
            try {
                //指定用户
                System.setProperty("HADOOP_USER_NAME", "etl");
                //创建fileSystem,用于连接hdfs
                fileSystem = FileSystem.get(new URI(FlowWriteConfig.HDFS_URI), configuration);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载HDFS文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    public static byte[] downloadFileByBytes(String filePath) {
        try (FSDataInputStream open = fileSystem.open(new Path(filePath))) {
            byte[] bytes = new byte[open.available()];
            open.read(0, bytes, 0, open.available());
            return bytes;
        } catch (IOException e) {
            logger.error("An I/O exception when files are download from HDFS. Message is :" + e.getMessage());
        }
        return null;
    }

    /**
     * 更新文件到HDFS
     *
     * @param filePath 文件路径
     * @param bytes    文件
     */
    public static void uploadFileByBytes(String filePath, byte[] bytes) {
        try (FSDataOutputStream fsDataOutputStream = fileSystem.create(new Path(filePath), true)) {
            fsDataOutputStream.write(bytes);
        } catch (RuntimeException e) {
            logger.error("Uploading files to the HDFS is abnormal. Message is :" + e.getMessage());
        } catch (IOException e) {
            logger.error("An I/O exception when files are uploaded to HDFS. Message is :" + e.getMessage());
        }
    }
}

