package com.zdjizhi.utils.hbase;

import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.hbase
 * @Description:
 * @date 2022/7/1510:12
 */
class GtpCRelation {
    private static final Log logger = LogFactory.get();

    /**
     * 获取全量的Radius数据
     */
    static void getAllGtpCRelation(Connection connection, Map<Long, String> gtpcMap) {
        long begin = System.currentTimeMillis();
        ResultScanner scanner = null;
        try {
            Table table = connection.getTable(TableName.valueOf(FlowWriteConfig.HBASE_GTPC_TABLE_NAME));
            Scan scan = new Scan();
            if (FlowWriteConfig.HBASE_GTPC_SCAN_MAX_ROWS > 0) {
                scan.setLimit(FlowWriteConfig.HBASE_GTPC_SCAN_MAX_ROWS);
            }
            scanner = table.getScanner(scan);
            for (Result result : scanner) {
                int acctStatusType = GtpCRelation.getMsgType(result);
                if (acctStatusType == 1) {
                    Long teid = Bytes.toLong(result.getValue(Bytes.toBytes("gtp"), Bytes.toBytes("teid")));
                    String phoneNumber = Bytes.toString(result.getValue(Bytes.toBytes("gtp"), Bytes.toBytes("phone_number"))).trim();
                    String imsi = Bytes.toString(result.getValue(Bytes.toBytes("gtp"), Bytes.toBytes("imsi"))).trim();
                    String imei = Bytes.toString(result.getValue(Bytes.toBytes("gtp"), Bytes.toBytes("imei"))).trim();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.set("common_phone_number", phoneNumber);
                    jsonObject.set("common_imsi", imsi);
                    jsonObject.set("common_imei", imei);
                    gtpcMap.put(teid, jsonObject.toJSONString(0));
                }
            }
            logger.warn("The obtain the number of GTP-C relationships : " + gtpcMap.size());
            logger.warn("The time spent to obtain GTP-C relationships : " + (System.currentTimeMillis() - begin) + "ms");
        } catch (IOException | RuntimeException e) {
            logger.error("The relationship between USER and TEID obtained from HBase is abnormal! message is :" + e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }


    /**
     * 增量更新GTP-C关系
     *
     * @param connection HBase连接
     * @param gtpcMap    gtp-c关系缓存
     * @param startTime  开始时间
     * @param endTime    结束时间
     */
    static void upgradeGtpCRelation(Connection connection, Map<Long, String> gtpcMap, Long startTime, Long endTime) {
        Long begin = System.currentTimeMillis();
        Table table = null;
        ResultScanner scanner = null;
        Scan scan = new Scan();
        try {
            table = connection.getTable(TableName.valueOf(FlowWriteConfig.HBASE_GTPC_TABLE_NAME));
            scan.setTimeRange(startTime, endTime);
            if (FlowWriteConfig.HBASE_GTPC_SCAN_MAX_ROWS > 0) {
                scan.setLimit(FlowWriteConfig.HBASE_GTPC_SCAN_MAX_ROWS);
            }
            scanner = table.getScanner(scan);
            for (Result result : scanner) {
                int acctStatusType = GtpCRelation.getMsgType(result);
                Long teid = Bytes.toLong(result.getValue(Bytes.toBytes("gtp"), Bytes.toBytes("teid")));
                if (acctStatusType == 1) {
                    String phoneNumber = Bytes.toString(result.getValue(Bytes.toBytes("gtp"), Bytes.toBytes("phone_number"))).trim();
                    String imsi = Bytes.toString(result.getValue(Bytes.toBytes("gtp"), Bytes.toBytes("imsi"))).trim();
                    String imei = Bytes.toString(result.getValue(Bytes.toBytes("gtp"), Bytes.toBytes("imei"))).trim();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.set("common_phone_number", phoneNumber);
                    jsonObject.set("common_imsi", imsi);
                    jsonObject.set("common_imei", imei);
                    gtpcMap.put(teid, jsonObject.toJSONString(0));
                } else if (acctStatusType == 2) {
                    gtpcMap.remove(teid);
                }
            }
            Long end = System.currentTimeMillis();
            logger.warn("The current number of GTPC relationships is: " + gtpcMap.keySet().size());
            logger.warn("The time used to update the GTPC relationship is: " + (end - begin) + "ms");
        } catch (IOException | RuntimeException e) {
            logger.error("GTPC relationship update exception, the content is:" + e);
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
     * 获取当前用户上下线状态信息
     *
     * @param result HBase内获取的数据
     * @return 状态 1-上线 2-下线
     */
    private static int getMsgType(Result result) {
        boolean hasType = result.containsColumn(Bytes.toBytes("gtp"), Bytes.toBytes("msg_type"));
        if (hasType) {
            return Bytes.toInt(result.getValue(Bytes.toBytes("gtp"), Bytes.toBytes("msg_type")));
        } else {
            return 1;
        }
    }
}
