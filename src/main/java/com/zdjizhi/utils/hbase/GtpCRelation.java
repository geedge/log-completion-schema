package com.zdjizhi.utils.hbase;

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
     * 获取全量的GTpc数据
     */
    static void getAllGtpCRelation(Connection connection, Map<Long, HashMap<String, Object>> gtpcMap) {
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
                    Long upLinkTeid = HBaseUtils.getLong(result, FlowWriteConfig.GTPC_FAMILY_NAME, "uplink_teid");
                    Long downLinkTeid = HBaseUtils.getLong(result, FlowWriteConfig.GTPC_FAMILY_NAME, "downlink_teid");
                    String phoneNumber = HBaseUtils.getString(result, FlowWriteConfig.GTPC_FAMILY_NAME, "phone_number").trim();
                    String imsi = HBaseUtils.getString(result, FlowWriteConfig.GTPC_FAMILY_NAME, "imsi").trim();
                    String imei = HBaseUtils.getString(result, FlowWriteConfig.GTPC_FAMILY_NAME, "imei").trim();
                    Long lastUpdateTime = HBaseUtils.getLong(result, FlowWriteConfig.GTPC_FAMILY_NAME, "last_update_time");

                    HashMap<String, Object> buildUserData = buildUserData(phoneNumber, imsi, imei, lastUpdateTime);

                    updateCache(gtpcMap, upLinkTeid, buildUserData, lastUpdateTime);
                    updateCache(gtpcMap, downLinkTeid, buildUserData, lastUpdateTime);
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
    static void upgradeGtpCRelation(Connection connection, Map<Long, HashMap<String, Object>> gtpcMap, Long startTime, Long endTime) {
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
                Long upLinkTeid = HBaseUtils.getLong(result, FlowWriteConfig.GTPC_FAMILY_NAME, "uplink_teid");
                Long downLinkTeid = HBaseUtils.getLong(result, FlowWriteConfig.GTPC_FAMILY_NAME, "downlink_teid");
                if (acctStatusType == 1) {
                    String phoneNumber = HBaseUtils.getString(result, FlowWriteConfig.GTPC_FAMILY_NAME, "phone_number").trim();
                    String imsi = HBaseUtils.getString(result, FlowWriteConfig.GTPC_FAMILY_NAME, "imsi").trim();
                    String imei = HBaseUtils.getString(result, FlowWriteConfig.GTPC_FAMILY_NAME, "imei").trim();
                    Long lastUpdateTime = HBaseUtils.getLong(result, FlowWriteConfig.GTPC_FAMILY_NAME, "last_update_time");

                    HashMap<String, Object> buildUserData = buildUserData(phoneNumber, imsi, imei, lastUpdateTime);

                    updateCache(gtpcMap, upLinkTeid, buildUserData, lastUpdateTime);
                    updateCache(gtpcMap, downLinkTeid, buildUserData, lastUpdateTime);
                } else if (acctStatusType == 2) {
                    removeCache(gtpcMap, upLinkTeid);
                    removeCache(gtpcMap, downLinkTeid);
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
     * @return onff_type 状态 1-上线 2-下线
     */
    private static int getMsgType(Result result) {
        boolean hasType = result.containsColumn(Bytes.toBytes(FlowWriteConfig.GTPC_FAMILY_NAME), Bytes.toBytes("msg_type"));
        if (hasType) {
            return Bytes.toInt(result.getValue(Bytes.toBytes(FlowWriteConfig.GTPC_FAMILY_NAME), Bytes.toBytes("msg_type")));
        } else {
            return 0;
        }
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
     * 判断缓存与新获取的数据时间戳大小，若大于缓存内记录的时间戳；则更新缓存
     *
     * @param gtpcMap        缓存集合
     * @param teid           上下行teid
     * @param userData       获取HBase内的用户信息
     * @param lastUpdateTime 该用户信息最后更新时间
     */
    private static void updateCache(Map<Long, HashMap<String, Object>> gtpcMap, Long teid, HashMap<String, Object> userData, Long lastUpdateTime) {
        if (teid != 0L) {
            if (gtpcMap.containsKey(teid)) {
                Long oldUpdateTime = Long.parseLong(gtpcMap.get(teid).get("last_update_time").toString());
                if (lastUpdateTime > oldUpdateTime) {
                    gtpcMap.put(teid, userData);
                }
            } else {
                gtpcMap.put(teid, userData);
            }
        }
    }

    /**
     * 将过期用户从缓存中删除
     *
     * @param gtpcMap 缓存集合
     * @param teid    上下行teid
     */
    private static void removeCache(Map<Long, HashMap<String, Object>> gtpcMap, Long teid) {
        if (teid != 0L) {
            gtpcMap.remove(teid);
        }
    }
}
