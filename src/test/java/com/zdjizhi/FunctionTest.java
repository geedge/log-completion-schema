package com.zdjizhi;

import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.utils.IpLookupV2;
import com.zdjizhi.utils.general.CityHash;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Calendar;

/**
 * @author qidaijie
 * @Package com.zdjizhi
 * @Description:
 * @date 2021/11/611:38
 */
public class FunctionTest {

    private static IpLookupV2 ipLookup = new IpLookupV2.Builder(false)
            .loadDataFileV4(FlowWriteConfig.TOOLS_LIBRARY + "ip_v4_built_in.mmdb")
            .loadDataFileV6(FlowWriteConfig.TOOLS_LIBRARY + "ip_v6_built_in.mmdb")
            .loadDataFilePrivateV4(FlowWriteConfig.TOOLS_LIBRARY + "ip_v4_user_defined.mmdb")
            .loadDataFilePrivateV6(FlowWriteConfig.TOOLS_LIBRARY + "ip_v6_user_defined.mmdb")
            .loadAsnDataFile(FlowWriteConfig.TOOLS_LIBRARY + "asn_v4.mmdb")
            .loadAsnDataFileV6(FlowWriteConfig.TOOLS_LIBRARY + "asn_v6.mmdb")
            .build();

    @Test
    public void CityHashTest() {

        byte[] dataBytes = String.valueOf(613970406986188816L).getBytes();
        long hashValue = CityHash.CityHash64(dataBytes, 0, dataBytes.length);
        String decimalValue = Long.toUnsignedString(hashValue, 10);
        BigInteger result = new BigInteger(decimalValue);
        System.out.println(result);
    }

    @Test
    public void ipLookupTest() {
        String ip = "61.144.36.144";
        System.out.println(ipLookup.cityLookupDetail(ip));
        System.out.println(ipLookup.countryLookup(ip));
    }

    @Test
    public void timestampTest(){
        Calendar cal = Calendar.getInstance();
        Long utcTime=cal.getTimeInMillis();
        System.out.println(utcTime);
        System.out.println(System.currentTimeMillis());
    }
}
