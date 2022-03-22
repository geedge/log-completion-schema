package com.zdjizhi;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author qidaijie
 * @Package com.zdjizhi
 * @Description:
 * @date 2021/12/310:42
 */
public class HBaseTest {

    @Test
    public void getColumn() {
        // 管理Hbase的配置信息
        Configuration configuration = HBaseConfiguration.create();
        // 设置zookeeper节点
        configuration.set("hbase.zookeeper.quorum", "192.168.44.11:2181");
        configuration.set("hbase.client.retries.number", "3");
        configuration.set("hbase.bulkload.retries.number", "3");
        configuration.set("zookeeper.recovery.retry", "3");
        try {
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table table = connection.getTable(TableName.valueOf("tsg_galaxy:relation_framedip_account"));
            Scan scan2 = new Scan();
            ResultScanner scanner = table.getScanner(scan2);
            for (Result result : scanner) {
                int acctStatusType;
                boolean hasType = result.containsColumn(Bytes.toBytes("radius"), Bytes.toBytes("acct_status_type"));
                if (hasType) {
                    acctStatusType = Bytes.toInt(result.getValue(Bytes.toBytes("radius"), Bytes.toBytes("acct_status_type")));
                } else {
                    acctStatusType = 3;
                }
                String framedIp = Bytes.toString(result.getValue(Bytes.toBytes("radius"), Bytes.toBytes("framed_ip")));
                String account = Bytes.toString(result.getValue(Bytes.toBytes("radius"), Bytes.toBytes("account")));
                System.out.println("status" + acctStatusType + "key:" + framedIp + "value:" + account);
//                System.out.println(Arrays.toString(result.getValue(Bytes.toBytes("radius"), Bytes.toBytes("acct_status_type"))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
