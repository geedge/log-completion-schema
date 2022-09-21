package com.zdjizhi.tools.hbase;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Map;

/**
 * @author qidaijie
 * @Package com.zdjizhi.utils.hbase
 * @Description:
 * @date 2022/7/1510:12
 */
class RadiusRelation {
    private static final Log logger = LogFactory.get();

    /**
     * 获取全量的Radius数据
     */
    static void getAllRadiusRelation(Connection connection, Map<String, String> radiusMap) {
        long begin = System.currentTimeMillis();
        ResultScanner scanner = null;
        try {
            Table table = connection.getTable(TableName.valueOf(FlowWriteConfig.HBASE_RADIUS_TABLE_NAME));
            Scan scan = new Scan();
            if (FlowWriteConfig.HBASE_RADIUS_SCAN_MAX_ROWS > 0) {
                scan.setLimit(FlowWriteConfig.HBASE_RADIUS_SCAN_MAX_ROWS);
            }
            scanner = table.getScanner(scan);
            for (Result result : scanner) {
                int acctStatusType = RadiusRelation.getAcctStatusType(result);
                String framedIp = HBaseUtils.getString(result, FlowWriteConfig.RADIUS_FAMILY_NAME, "framed_ip").trim();
                String account = HBaseUtils.getString(result, FlowWriteConfig.RADIUS_FAMILY_NAME, "account").trim();
                if (acctStatusType == 1) {
                    if (FlowWriteConfig.DEFAULT_RELATIONSHIP_MODULE.equals(FlowWriteConfig.DATA_RELATIONSHIP_MODEL)) {
                        String vsysId = HBaseUtils.getVsysId(result).trim();
                        radiusMap.put(framedIp + vsysId, account);
                    } else {
                        radiusMap.put(framedIp, account);
                    }
                }
            }
            System.out.println(radiusMap.toString());

            logger.warn("The obtain the number of RADIUS relationships : " + radiusMap.size());
            logger.warn("The time spent to obtain radius relationships : " + (System.currentTimeMillis() - begin) + "ms");
        } catch (IOException | RuntimeException e) {
            logger.error("The relationship between framedIP and account obtained from HBase is abnormal! message is :" + e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * 增量更新Radius关系
     *
     * @param connection HBase连接
     * @param radiusMap  radius关系缓存
     * @param startTime  开始时间
     * @param endTime    结束时间
     */
    static void upgradeRadiusRelation(Connection connection, Map<String, String> radiusMap, Long startTime, Long endTime) {
        Long begin = System.currentTimeMillis();
        Table table = null;
        ResultScanner scanner = null;
        Scan scan = new Scan();
        try {
            table = connection.getTable(TableName.valueOf(FlowWriteConfig.HBASE_RADIUS_TABLE_NAME));
            scan.setTimeRange(startTime, endTime);
            if (FlowWriteConfig.HBASE_RADIUS_SCAN_MAX_ROWS > 0) {
                scan.setLimit(FlowWriteConfig.HBASE_RADIUS_SCAN_MAX_ROWS);
            }
            scanner = table.getScanner(scan);
            for (Result result : scanner) {
                int acctStatusType = RadiusRelation.getAcctStatusType(result);
                String framedIp = HBaseUtils.getString(result, FlowWriteConfig.RADIUS_FAMILY_NAME, "framed_ip").trim();
                String account = HBaseUtils.getString(result, FlowWriteConfig.RADIUS_FAMILY_NAME, "account").trim();
                if (acctStatusType == 1) {
                    if (FlowWriteConfig.DEFAULT_RELATIONSHIP_MODULE.equals(FlowWriteConfig.DATA_RELATIONSHIP_MODEL)) {
                        String vsysId = HBaseUtils.getVsysId(result).trim();
                        radiusMap.put(framedIp + vsysId, account);
                    } else {
                        radiusMap.put(framedIp, account);
                    }
                } else if (acctStatusType == 2) {
                    if (FlowWriteConfig.DEFAULT_RELATIONSHIP_MODULE.equals(FlowWriteConfig.DATA_RELATIONSHIP_MODEL)) {
                        String vsysId = HBaseUtils.getVsysId(result).trim();
                        radiusMap.remove(framedIp+vsysId);
                    } else {
                        radiusMap.remove(framedIp);
                    }
                }
            }
            Long end = System.currentTimeMillis();
            logger.warn("The current number of Radius relationships is: " + radiusMap.keySet().size());
            logger.warn("The time used to update the Radius relationship is: " + (end - begin) + "ms");
        } catch (IOException | RuntimeException e) {
            logger.error("Radius relationship update exception, the content is:" + e);
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
    private static int getAcctStatusType(Result result) {
        boolean hasType = result.containsColumn(Bytes.toBytes(FlowWriteConfig.RADIUS_FAMILY_NAME), Bytes.toBytes("acct_status_type"));
        if (hasType) {
            return Bytes.toInt(result.getValue(Bytes.toBytes(FlowWriteConfig.RADIUS_FAMILY_NAME), Bytes.toBytes("acct_status_type")));
        } else {
            return 1;
        }
    }
}
