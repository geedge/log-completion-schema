package com.zdjizhi.hdfs;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author qidaijie
 * @Package com.zdjizhi.tools.connections.hadoop
 * @Description:
 * @date 2022/11/217:57
 */
public class FileUtilsTest {
    private static final Log logger = LogFactory.get();

    private static FileSystem fileSystem;

    static {
        Configuration configuration = new Configuration();
        try {
            configuration.set("fs.defaultFS","hdfs://ns1");
            configuration.set("hadoop.proxyuser.root.hosts","*");
            configuration.set("hadoop.proxyuser.root.groups","*");
            configuration.set("ha.zookeeper.quorum","192.168.44.83:2181,192.168.44.84:2181,192.168.44.85:2181");
            configuration.set("dfs.nameservices","ns1");
            configuration.set("dfs.ha.namenodes.ns1","nn1,nn2");
            configuration.set("dfs.namenode.rpc-address.ns1.nn1","192.168.44.85:9000");
            configuration.set("dfs.namenode.rpc-address.ns1.nn2","192.168.44.86:9000");
            configuration.set("dfs.client.failover.proxy.provider.ns1","org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
            //创建fileSystem,用于连接hdfs
            fileSystem = FileSystem.get(configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void mkdir() throws Exception{
        fileSystem.mkdirs(new Path("/knowledgebase/test"));
    }

    @Test
    public void create() throws Exception{
        FSDataOutputStream outputStream = fileSystem.create(new Path("/knowledgebase/test/test.txt"));
        outputStream.write("Hello World".getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Test
    public void cat() throws Exception{
        FSDataInputStream inputStream = fileSystem.open(new Path("/knowledgebase/test/test.txt"));
        IOUtils.copyBytes(inputStream, System.out, 1024);
        inputStream.close();
    }

    @Test
    public void rename() throws Exception{
        fileSystem.rename(new Path("/knowledgebase/test/test.txt"), new Path("/knowledgebase/test/test1.txt"));
    }

    @Test
    public void delete() throws Exception{
        fileSystem.delete(new Path("/knowledgebase/test"),true);//是否递归删除
    }
}

