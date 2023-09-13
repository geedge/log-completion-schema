package com.zdjizhi.tools.connections.hadoop;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.geedgenetworks.utils.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * @author qidaijie
 * @version 2022/11/2 17:57
 */
public class HadoopUtils {
    private static final Log logger = LogFactory.get();

    private static HadoopUtils hadoopUtils;

    private static FileSystem fileSystem;

    private static void getInstance() {
        hadoopUtils = new HadoopUtils();
    }

    /**
     * 构造函数
     */
    private HadoopUtils() {
        //获取连接
        getConnection();
    }

    private static void getConnection() {
        Configuration configuration = new Configuration();
        try {
            //指定用户
            System.setProperty("HADOOP_USER_NAME", "etl");
            //配置hdfs相关信息
            configuration.set("fs.defaultFS", "hdfs://ns1");
            configuration.set("hadoop.proxyuser.root.hosts", "*");
            configuration.set("hadoop.proxyuser.root.groups", "*");
            configuration.set("dfs.nameservices", "ns1");
            configuration.set("dfs.ha.namenodes.ns1", "nn1,nn2");
            String[] servers = StringUtil.split(FlowWriteConfig.HDFS_SERVERS, FlowWriteConfig.FORMAT_SPLITTER);
            configuration.set("dfs.namenode.rpc-address.ns1.nn1", servers[0]);
            configuration.set("dfs.namenode.rpc-address.ns1.nn2", servers[1]);
            configuration.set("dfs.client.failover.proxy.provider.ns1", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
            //创建fileSystem,用于连接hdfs
            fileSystem = FileSystem.get(configuration);
        } catch (IOException | RuntimeException e) {
            logger.error("Failed to create HDFS connection. message is: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    /**
//     * 创建hdfs连接
//     */
//    static {
//        if (FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_STORAGE_TYPE)) {
//            Configuration configuration = new Configuration();
//            try {
//                //指定用户
//                System.setProperty("HADOOP_USER_NAME", "etl");
//                //配置hdfs相关信息
//                configuration.set("fs.defaultFS", "hdfs://ns1");
//                configuration.set("hadoop.proxyuser.root.hosts", "*");
//                configuration.set("hadoop.proxyuser.root.groups", "*");
//                configuration.set("dfs.nameservices", "ns1");
//                configuration.set("dfs.ha.namenodes.ns1", "nn1,nn2");
//                String[] servers = StringUtil.split(FlowWriteConfig.HDFS_SERVERS, FlowWriteConfig.FORMAT_SPLITTER);
//                configuration.set("dfs.namenode.rpc-address.ns1.nn1", servers[0]);
//                configuration.set("dfs.namenode.rpc-address.ns1.nn2", servers[1]);
//                configuration.set("dfs.client.failover.proxy.provider.ns1", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
//                //创建fileSystem,用于连接hdfs
//                fileSystem = FileSystem.get(configuration);
//            } catch (IOException | RuntimeException e) {
//                logger.error("Failed to create HDFS connection. message is: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * 下载HDFS文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    public static byte[] downloadFileByBytes(String filePath) {
        if (hadoopUtils == null) {
            getInstance();
        }

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
        if (hadoopUtils == null) {
            getInstance();
        }
        try (FSDataOutputStream fsDataOutputStream = fileSystem.create(new Path(filePath), true)) {
            fsDataOutputStream.write(bytes);
//            fsDataOutputStream.flush();
        } catch (RuntimeException e) {
            logger.error("Uploading files to the HDFS is abnormal. Message is :" + e.getMessage());
        } catch (IOException e) {
            logger.error("An I/O exception when files are uploaded to HDFS. Message is :" + e.getMessage());
        }
    }
}

