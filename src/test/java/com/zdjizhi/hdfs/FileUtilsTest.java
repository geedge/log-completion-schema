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
 * @Package com.zdjizhi.tools.hdfs
 * @Description:
 * @date 2022/11/217:57
 */
public class FileUtilsTest {
    private static final Log logger = LogFactory.get();

    private static FileSystem fileSystem;

    static {
        Configuration configuration = new Configuration();
        try {
            //创建fileSystem,用于连接hdfs
            fileSystem = FileSystem.get(new URI(FlowWriteConfig.HDFS_URI),configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void mkdir() throws Exception{
        fileSystem.mkdirs(new Path("/IPlocation/ETL-SESSION-RECORD-COMPLETED"));
    }

    @Test
    public void create() throws Exception{
        FSDataOutputStream outputStream = fileSystem.create(new Path("/qitest/test/test.txt"));
        outputStream.write("Hello World".getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Test
    public void cat() throws Exception{
        FSDataInputStream inputStream = fileSystem.open(new Path("/qitest/test/test.txt"));
        IOUtils.copyBytes(inputStream, System.out, 1024);
        inputStream.close();
    }

    @Test
    public void rename() throws Exception{
        fileSystem.rename(new Path("/qitest"), new Path("/IPlocation"));
    }

    @Test
    public void delete() throws Exception{
        fileSystem.delete(new Path("/qitest/test2"),true);//是否递归删除
    }
}

