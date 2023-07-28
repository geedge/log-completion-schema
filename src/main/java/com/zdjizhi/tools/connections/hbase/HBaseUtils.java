package com.zdjizhi.tools.connections.hbase;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.HashMap;
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
    private static Map<String, String> radiusMap = new ConcurrentHashMap<>(16);
    private static Map<String, HashMap<String, Object>> gtpcMap = new ConcurrentHashMap<>(16);
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
        RadiusRelation.getAllRadiusRelation(connection, radiusMap);
        GtpCRelation.getAllGtpCRelation(connection, gtpcMap);
        //定时更新
        updateCache();

    }

    private static void getConnection() {
        try {
            Configuration configuration = HBaseConfiguration.create();
            configuration.set("hbase.zookeeper.quorum", FlowWriteConfig.ZOOKEEPER_SERVERS);
            configuration.set("hbase.client.retries.number", "1");
            configuration.set("hbase.client.pause", "100");
            configuration.set("hbase.rpc.timeout", FlowWriteConfig.HBASE_RPC_TIMEOUT);
            configuration.set("zookeeper.recovery.retry", "1");
            configuration.set("zookeeper.recovery.retry.intervalmill", "200");
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
     * 获取HBase内String类型的值
     *
     * @param result     结果集
     * @param familyName 列族名称
     * @param columnName 列名称
     * @return 结果数据
     */
    static String getString(Result result, String familyName, String columnName) {
        byte[] familyBytes = Bytes.toBytes(familyName);
        byte[] columnBytes = Bytes.toBytes(columnName);
        boolean contains = result.containsColumn(familyBytes, columnBytes);
        if (contains) {
            String data = Bytes.toString(result.getValue(familyBytes, columnBytes)).trim();
            if (StringUtil.isNotBlank(data)) {
                return data;
            }
        }

        return "";
    }

    /**
     * 获取HBase内String类型的值
     *
     * @param result     结果集
     * @param columnName 列名称
     * @return 结果数据
     */
    static Long getLong(Result result, String familyName, String columnName) {
        byte[] familyBytes = Bytes.toBytes(familyName);
        byte[] columnBytes = Bytes.toBytes(columnName);
        boolean contains = result.containsColumn(familyBytes, columnBytes);
        if (contains) {
            return Bytes.toLong(result.getValue(familyBytes, columnBytes));
        }
        return 0L;
    }

    /**
     * 获取HBase内String类型的值
     *
     * @param result     结果集
     * @param columnName 列名称
     * @return 结果数据
     */
    static String getTeid(Result result, String columnName) {
        byte[] familyBytes = Bytes.toBytes(FlowWriteConfig.GTPC_FAMILY_NAME);
        byte[] columnBytes = Bytes.toBytes(columnName);
        boolean contains = result.containsColumn(familyBytes, columnBytes);
        if (contains) {
            String data = String.valueOf(Bytes.toLong(result.getValue(familyBytes, columnBytes))).trim();
            if (StringUtil.isNotBlank(data)) {
                return data;
            }
        }
        return "0";
    }


    /**
     * 获取HBase内String类型的值
     *
     * @param result 结果集
     * @return 结果数据
     */
    static String getVsysId(Result result) {
        byte[] familyBytes = Bytes.toBytes("common");
        byte[] columnBytes = Bytes.toBytes("vsys_id");
        boolean contains = result.containsColumn(familyBytes, columnBytes);
        if (contains) {
            String data = String.valueOf(Bytes.toInt(result.getValue(familyBytes, columnBytes))).trim();
            if (StringUtil.isNotBlank(data)) {
                return data;
            }
        }
        return "1";
    }

    /**
     * 更新变量
     */
    private static void change() {
        if (hBaseUtils == null) {
            getInstance();
        }
        long nowTime = System.currentTimeMillis();
        RadiusRelation.upgradeRadiusRelation(connection, radiusMap, time - 1000, nowTime + 500);
        GtpCRelation.upgradeGtpCRelation(connection, gtpcMap, time - 1000, nowTime + 500);
        time = nowTime;
    }

    /**
     * 验证定时器,每隔一段时间验证一次-验证获取新的Cookie
     */
    private void updateCache() {
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
     * 获取Radius account
     *
     * @param clientIp client_ip
     * @return account
     */
    public static String getAccount(String clientIp) {
        if (FlowWriteConfig.HBASE_TICK_TUPLE_FREQ_SECS != 0) {
            if (hBaseUtils == null) {
                getInstance();
            }
            return radiusMap.get(clientIp);
        }
        return "";
    }


    /**
     * 获取GTPC用户信息
     *
     * @param teid TEID
     * @return account
     */
    public static HashMap<String, Object> getGtpData(String teid) {
        if (FlowWriteConfig.HBASE_TICK_TUPLE_FREQ_SECS != 0) {
            if (hBaseUtils == null) {
                getInstance();
            }
            return gtpcMap.get(teid);
        }
        return null;
    }
}
