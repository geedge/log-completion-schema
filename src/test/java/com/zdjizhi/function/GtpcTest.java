package com.zdjizhi.function;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.connections.hbase.HBaseUtils;
import com.zdjizhi.tools.json.JsonPathUtil;
import org.junit.Test;

import java.util.HashMap;

public class GtpcTest {
    private static final Log logger = LogFactory.get();

    @Test
    public void gtpcMatch() {
        String param = "$.[?(@.tunnels_schema_type=='GTP')].gtp_endpoint_a2b_teid,$.[?(@.tunnels_schema_type=='GTP')].gtp_endpoint_b2a_teid";
        String logValue = "[{\"tunnels_schema_type\":\"GTP\",\"gtp_endpoint_a2b_teid\":4129335432,\"gtp_endpoint_b2a_teid\":4129335434,\"gtp_sgw_ip\":\"120.36.3.97\",\"gtp_pgw_ip\":\"43.224.53.100\",\"gtp_sgw_port\":2152,\"gtp_pgw_port\":51454},{\"tunnels_schema_type\":\"ETHERNET\",\"source_mac\":\"80:69:33:ea:a5:57\",\"destination_mac\":\"14:09:dc:df:a3:40\"}]";
        String appendToKey = "common_imsi,common_imei,common_phone_number";

        try {
            String teid = null;
            String[] exprs = param.split(FlowWriteConfig.FORMAT_SPLITTER);
            for (String expr : exprs) {
                String value = JsonPathUtil.analysis(logValue, expr).toString();
                if (value != null) {
                    teid = value;
                    break;
                }
            }
            System.out.println(teid);
            if (teid != null) {
                String[] appendToKeys = appendToKey.split(FlowWriteConfig.FORMAT_SPLITTER);
                HashMap<String, Object> userData = HBaseUtils.getGtpData(teid);
                if (userData != null) {
                    for (String key : appendToKeys) {
                        System.out.println(userData.get(key).toString());
                    }
                } else {
                    logger.warn("Description The user whose TEID is " + teid + " was not matched!");
                }
            }
        } catch (RuntimeException re) {
            logger.error("An exception occurred in teid type conversion or parsing of user information!" + re);
        }
    }
}
