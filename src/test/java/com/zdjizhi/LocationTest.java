package com.zdjizhi;

import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.IpLookup;
import org.junit.Test;

/**
 * @author qidaijie
 * @Package com.zdjizhi
 * @Description:
 * @date 2021/8/1811:34
 */
public class LocationTest {
    private static IpLookup ipLookup = new IpLookup.Builder(false)
            .loadDataFileV4("D:\\K18-Phase2\\tsgSpace\\dat\\tsg\\ip_v4.mmdb")
            .loadDataFileV6("D:\\K18-Phase2\\tsgSpace\\dat\\tsg\\ip_v6.mmdb")
            .loadDataFilePrivateV4("D:\\K18-Phase2\\tsgSpace\\dat\\tsg\\ip_private_v4.mmdb")
            .loadDataFilePrivateV6("D:\\K18-Phase2\\tsgSpace\\dat\\tsg\\ip_private_v6.mmdb")
            .build();

    @Test
    public void IpLocationTest() {
        System.out.println(ipLookup.cityLookupDetail("24.241.112.0"));
        System.out.println(ipLookup.cityLookupDetail("1.1.1.1"));
        System.out.println(ipLookup.cityLookupDetail("192.168.50.58"));
        System.out.println(ipLookup.cityLookupDetail("2600:1700:9010::"));
    }
}
