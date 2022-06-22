package com.zdjizhi.utils.hbase;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * HBase 工具类
 *
 * @author qidaijie
 */

public class HBaseUtils {
    private static final Log logger = LogFactory.get();
    private static Map<String, String> subIdMap = new ConcurrentHashMap<>(16);
    private static Connection connection;
    private static Long time;

    private static HBaseUtils hBaseUtils;

    private static void getInstance() {
        hBaseUtils = new HBaseUtils();
    }


    /**
     * 构造函数-新
     */
    private HBaseUtils() {
        //获取连接
        getConnection();
        //拉取所有
        getAll();
        //定时更新
        updateCache();

    }

    private static void getConnection() {
        try {
            // 管理Hbase的配置信息
            Configuration configuration = HBaseConfiguration.create();
            // 设置zookeeper节点
            configuration.set("hbase.zookeeper.quorum", FlowWriteConfig.HBASE_ZOOKEEPER_SERVERS);
            configuration.set("hbase.client.retries.number", "3");
            configuration.set("hbase.bulkload.retries.number", "3");
            configuration.set("zookeeper.recovery.retry", "3");
            connection = ConnectionFactory.createConnection(configuration);
            time = System.currentTimeMillis();
            logger.warn("HBaseUtils get HBase connection,now to getAll().");
        } catch (IOException ioe) {
            logger.error("HBaseUtils getHbaseConn() IOException===>{" + ioe + "}<===");
        } catch (RuntimeException e) {
            logger.error("HBaseUtils getHbaseConn() Exception===>{" + e + "}<===");
        }
    }

    /**
     * 更新变量
     */
    private static void change() {
        if (hBaseUtils == null) {
            getInstance();
        }
        long nowTime = System.currentTimeMillis();
        timestampsFilter(time - 1000, nowTime + 500);
    }


    /**
     * 获取变更内容
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    private static void timestampsFilter(Long startTime, Long endTime) {
        Long begin = System.currentTimeMillis();
        Table table = null;
        ResultScanner scanner = null;
        Scan scan2 = new Scan();
        try {
            table = connection.getTable(TableName.valueOf(FlowWriteConfig.HBASE_TABLE_NAME));
            scan2.setTimeRange(startTime, endTime);
            scanner = table.getScanner(scan2);
            for (Result result : scanner) {
                int acctStatusType = getAcctStatusType(result);
                String framedIp = Bytes.toString(result.getValue(Bytes.toBytes("radius"), Bytes.toBytes("framed_ip"))).trim();
                String account = Bytes.toString(result.getValue(Bytes.toBytes("radius"), Bytes.toBytes("account"))).trim();
                if (acctStatusType == 1) {
                    if (subIdMap.containsKey(framedIp)) {
                        boolean same = account.equals(subIdMap.get(framedIp));
                        if (!same) {
                            subIdMap.put(framedIp, account);
                        }
                    } else {
                        subIdMap.put(framedIp, account);
                    }
                } else if (acctStatusType == 2) {
                    subIdMap.remove(framedIp);
                }
            }
            Long end = System.currentTimeMillis();
            logger.warn("HBaseUtils Now subIdMap.keySet().size() is: " + subIdMap.keySet().size());
            logger.warn("HBaseUtils Update cache timeConsuming is: " + (end - begin) + ",BeginTime: " + startTime + ",EndTime: " + endTime);
            time = endTime;
        } catch (IOException ioe) {
            logger.error("HBaseUtils timestampsFilter is IOException===>{" + ioe + "}<===");
        } catch (RuntimeException e) {
            logger.error("HBaseUtils timestampsFilter is Exception===>{" + e + "}<===");
        } finally {
            if (scanner != null) {
                scanner.close();
            }
            if (table != null) {
                try {
                    table.close();
                } catch (IOException e) {
                    logger.error("HBase Table Close ERROR! Exception message is:" + e);
                }
            }
        }
    }

    /**
     * 获取所有的 key value
     */
    private static void getAll() {
        long begin = System.currentTimeMillis();
        try {
            Table table = connection.getTable(TableName.valueOf(FlowWriteConfig.HBASE_TABLE_NAME));
            Scan scan2 = new Scan();
            ResultScanner scanner = table.getScanner(scan2);
            for (Result result : scanner) {
                int acctStatusType = getAcctStatusType(result);
                String framedIp = Bytes.toString(result.getValue(Bytes.toBytes("radius"), Bytes.toBytes("framed_ip")));
                String account = Bytes.toString(result.getValue(Bytes.toBytes("radius"), Bytes.toBytes("account")));
                if (acctStatusType == 1) {
                    subIdMap.put(framedIp, account);
                }
            }
            logger.warn("HBaseUtils Get fullAmount List size->subIdMap.size(): " + subIdMap.size());
            logger.warn("HBaseUtils Get fullAmount List size->subIdMap.size() timeConsuming is: " + (System.currentTimeMillis() - begin));
            scanner.close();
        } catch (IOException ioe) {
            logger.error("HBaseUtils getAll() is IOException===>{" + ioe + "}<===");
        } catch (RuntimeException e) {
            logger.error("HBaseUtils getAll() is Exception===>{" + e + "}<===");
        }
    }

    /**
     * 验证定时器,每隔一段时间验证一次-验证获取新的Cookie
     */
    private void updateCache() {
//        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
//                new BasicThreadFactory.Builder().namingPattern("hbase-change-pool-%d").daemon(true).build());
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    if (FlowWriteConfig.HBASE_TICK_TUPLE_FREQ_SECS != 0) {
                        change();
                    }
                } catch (RuntimeException e) {
                    logger.error("HBaseUtils update hbaseCache is error===>{" + e + "}<===");
                }
            }
        }, 1, FlowWriteConfig.HBASE_TICK_TUPLE_FREQ_SECS, TimeUnit.SECONDS);
    }


    /**
     * 获取 account
     *
     * @param clientIp client_ip
     * @return account
     */
    public static String getAccount(String clientIp) {
        if (FlowWriteConfig.HBASE_TICK_TUPLE_FREQ_SECS != 0) {
            if (hBaseUtils == null) {
                getInstance();
            }
            return subIdMap.get(clientIp);
        }
        return "";
    }

    /**
     * 获取当前用户上下线状态信息
     *
     * @param result HBase内获取的数据
     * @return 状态 1-上线 2-下线
     */
    private static int getAcctStatusType(Result result) {
        boolean hasType = result.containsColumn(Bytes.toBytes("radius"), Bytes.toBytes("acct_status_type"));
        if (hasType) {
            return Bytes.toInt(result.getValue(Bytes.toBytes("radius"), Bytes.toBytes("acct_status_type")));
        } else {
            return 1;
        }
    }

}
