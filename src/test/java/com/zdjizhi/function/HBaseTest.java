package com.zdjizhi.function;

import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.hbase.HBaseUtils;
import com.zdjizhi.utils.StringUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;
import scala.Int;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qidaijie
 * @Package com.zdjizhi
 * @Description:
 * @date 2021/12/310:42
 */
public class HBaseTest {
    private static final Log logger = LogFactory.get();
    private static Map<String, String> radiusMap = new ConcurrentHashMap<>(16);

    private static Map<String,HashMap<String, Object>> gtpcMap = new ConcurrentHashMap<>(16);

    @Test
    public void getColumn() {
        // 管理Hbase的配置信息
        Configuration configuration = HBaseConfiguration.create();
        // 设置zookeeper节点
        configuration.set("hbase.zookeeper.quorum", "192.168.44.111:2181");
        configuration.set("hbase.client.retries.number", "1");
        configuration.set("hbase.client.pause", "50");
        configuration.set("hbase.rpc.timeout", "3000");
        configuration.set("zookeeper.recovery.retry", "1");
        configuration.set("zookeeper.recovery.retry.intervalmill", "200");
        try {
            System.out.println(System.currentTimeMillis());
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println(System.currentTimeMillis());
        }
    }


    @Test
    public void getGtpcData() {
        // 管理Hbase的配置信息
        Configuration configuration = HBaseConfiguration.create();
        // 设置zookeeper节点
        configuration.set("hbase.zookeeper.quorum", "192.168.44.11:2181");
        configuration.set("hbase.client.retries.number", "1");
        configuration.set("hbase.client.pause", "50");
        configuration.set("hbase.rpc.timeout", "3000");
        configuration.set("zookeeper.recovery.retry", "1");
        configuration.set("zookeeper.recovery.retry.intervalmill", "200");
        long begin = System.currentTimeMillis();
        ResultScanner scanner = null;
        try {
            Connection connection = ConnectionFactory.createConnection(configuration);
            Table table = connection.getTable(TableName.valueOf(FlowWriteConfig.HBASE_GTPC_TABLE_NAME));
            Scan scan2 = new Scan();
            scanner = table.getScanner(scan2);
            for (Result result : scanner) {
                String upLinkTeid = getTeid(result, "uplink_teid");
                String downLinkTeid = getTeid(result, "downlink_teid");
                String phoneNumber = getString(result, FlowWriteConfig.GTPC_FAMILY_NAME, "phone_number").trim();
                String imsi = getString(result, FlowWriteConfig.GTPC_FAMILY_NAME, "imsi").trim();
                String imei = getString(result, FlowWriteConfig.GTPC_FAMILY_NAME, "imei").trim();
                Long lastUpdateTime = getLong(result, FlowWriteConfig.GTPC_FAMILY_NAME, "last_update_time");

                HashMap<String, Object> buildUserData = buildUserData(phoneNumber, imsi, imei, lastUpdateTime);

                if (FlowWriteConfig.DEFAULT_RELATIONSHIP_MODULE.equals(FlowWriteConfig.DATA_RELATIONSHIP_MODEL)) {
                    String vsysId = getVsysId(result).trim();
                    updateCache(gtpcMap, upLinkTeid+vsysId, buildUserData, lastUpdateTime);
                    updateCache(gtpcMap, downLinkTeid+vsysId, buildUserData, lastUpdateTime);
                } else {
                    updateCache(gtpcMap, upLinkTeid, buildUserData, lastUpdateTime);
                    updateCache(gtpcMap, downLinkTeid, buildUserData, lastUpdateTime);
                }
            }
            logger.warn("The obtain the number of GTP-C relationships : " + gtpcMap.size());
            logger.warn("The time spent to obtain GTP-C relationships : " + (System.currentTimeMillis() - begin));
        } catch (IOException | RuntimeException e) {
            logger.error("The relationship between USER and TEID obtained from HBase is abnormal! message is :" + e);
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        for (String key : gtpcMap.keySet()){
            System.out.println(key +"---"+gtpcMap.get(key));
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
    private static String getString(Result result, String familyName, String columnName) {
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
    private static Long getLong(Result result, String familyName, String columnName) {
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
    private static String getTeid(Result result, String columnName) {
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
     * 构建用户信息
     *
     * @param phoneNumber 手机号
     * @param imsi        用户标识
     * @param imei        设备标识
     * @return 用户信息
     */
    private static HashMap<String, Object> buildUserData(String phoneNumber, String imsi, String imei, Long lastUpdateTime) {
        HashMap<String, Object> tmpMap = new HashMap<>(4);
        tmpMap.put("common_phone_number", phoneNumber);
        tmpMap.put("common_imsi", imsi);
        tmpMap.put("common_imei", imei);
        tmpMap.put("last_update_time", lastUpdateTime);
        return tmpMap;
    }


    /**
     * 获取HBase内String类型的值
     *
     * @param result 结果集
     * @return 结果数据
     */
    static String getVsysId(Result result) {
        byte[] familyBytes = Bytes.toBytes(FlowWriteConfig.COMMON_FAMILY_NAME);
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
     * 判断缓存与新获取的数据时间戳大小，若大于缓存内记录的时间戳；则更新缓存
     *
     * @param gtpcMap        缓存集合
     * @param key            上下行teid
     * @param userData       获取HBase内的用户信息
     * @param lastUpdateTime 该用户信息最后更新时间
     */
    private static void updateCache(Map<String, HashMap<String, Object>> gtpcMap, String key, HashMap<String, Object> userData, Long lastUpdateTime) {
        if (StringUtil.isNotBlank(key)){
            if (gtpcMap.containsKey(key)) {
                Long oldUpdateTime = Long.parseLong(gtpcMap.get(key).get("last_update_time").toString());
                if (lastUpdateTime > oldUpdateTime) {
                    gtpcMap.put(key, userData);
                }
            } else {
                gtpcMap.put(key, userData);
            }
        }
    }


}
