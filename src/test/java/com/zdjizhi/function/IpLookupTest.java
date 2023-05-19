package com.zdjizhi.function;

import cn.hutool.core.io.IoUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.http.HttpClientService;
import com.zdjizhi.utils.IpLookupV2;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import java.io.*;
import java.net.InetAddress;

/**
 * @author qidaijie
 * @Package com.zdjizhi
 * @Description:
 * @date 2021/11/611:38
 */
public class IpLookupTest {
    private static final Log logger = LogFactory.get();

    @Test
    public void ipLookupDatabaseTest() {
        IpLookupV2 ipLookup = new IpLookupV2.Builder(false)
                .loadDataFileV4(FlowWriteConfig.TOOLS_LIBRARY + "ip_v4_built_in.mmdb")
                .loadDataFileV6(FlowWriteConfig.TOOLS_LIBRARY + "ip_v6_built_in.mmdb")
                .loadDataFilePrivateV4(FlowWriteConfig.TOOLS_LIBRARY + "ip_v4_user_defined.mmdb")
                .loadDataFilePrivateV6(FlowWriteConfig.TOOLS_LIBRARY + "ip_v6_user_defined.mmdb")
                .loadAsnDataFile(FlowWriteConfig.TOOLS_LIBRARY + "asn_v4.mmdb")
                .loadAsnDataFileV6(FlowWriteConfig.TOOLS_LIBRARY + "asn_v6.mmdb")
                .build();

        String ip = "23.200.74.224";
//        String ip = "1.13.7.45";
        try {
            System.out.println(ipLookup.cityLookup(ip));
            System.out.println(ipLookup.cityLookupDetail(ip));
            System.out.println(ipLookup.cityLatLngLookup(ip));
            System.out.println(ipLookup.provinceLookup(ip));
            System.out.println(ipLookup.administrativeAreaLookupDetail(ip));
            System.out.println(ipLookup.countryLookup(ip));
            System.out.println(ipLookup.locationLookupDetail(ip));
            System.out.println(ipLookup.administrativeAreaLookupDetail(ip));
            System.out.println(ipLookup.infoLookupToJson(ip));

            //ASN
            System.out.println(ipLookup.asnLookup(ip));
            System.out.println(ipLookup.asnLookupInfo(ip));
            System.out.println(ipLookup.asnLookupDetail(ip));
            System.out.println(ipLookup.asnLookupOrganization(ip));

        } catch (NullPointerException npe) {
            logger.error("The MMDB file is not loaded or IP is null! " + npe.getMessage());
        } catch (RuntimeException e) {
            logger.error("Get clientIP location error! " + e.getMessage());
        }
    }

    @Test
    public void ipLookupForBytesTest() {
        InputStream inputStream = null;
        try {
            Header header = new BasicHeader("token", FlowWriteConfig.HOS_TOKEN);
            String url = "http://192.168.44.12:9098/hos/knowledge_base_hos_bucket/a69c41bc-c673-46da-a810-f29aa18a3a16-aXBfdjRfYnVpbHRfaW4=.mmdb";
            HttpClientService httpClientService = new HttpClientService();
            inputStream = httpClientService.httpGetInputStream(url, 3000, header);
            Reader reader = new Reader(inputStream, new CHMCache());
            InetAddress ipAddress = InetAddress.getByName("114.64.231.114");
            JsonNode jsonNode = reader.get(ipAddress);

            System.out.println(jsonNode.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IoUtil.close(inputStream);
        }
    }


    @Test
    public void ipLookupInputStreamTest() {
        InputStream asnInputStream = null;
        InputStream ipv4InputStream = null;
        String ip = "114.64.231.114";
        try {
            HttpClientService httpClientService = new HttpClientService();
            Header header = new BasicHeader("token", FlowWriteConfig.HOS_TOKEN);
            String asnUrl = "http://192.168.44.12:9098/hos/knowledge_base_hos_bucket/1b96764c-59dd-4d6b-8edb-623705f708a5-YXNuX3Y0.mmdb";
            String ipv4Url = "http://192.168.44.12:9098/hos/knowledge_base_hos_bucket/24cb6a74-f048-4672-988f-112858427a3b-aXBfdjRfYnVpbHRfaW4=.mmdb";
            asnInputStream = httpClientService.httpGetInputStream(asnUrl, 3000, header);
            ipv4InputStream = httpClientService.httpGetInputStream(ipv4Url, 3000, header);

            IpLookupV2 ipLookup = new IpLookupV2.Builder(false)
                    .loadDataFileV4(ipv4InputStream)
                    .loadAsnDataFileV4(asnInputStream)
                    .build();

            System.out.println(ipLookup.cityLookup(ip));
            System.out.println(ipLookup.cityLookupDetail(ip));
            System.out.println(ipLookup.cityLatLngLookup(ip));
            System.out.println(ipLookup.provinceLookup(ip));
            System.out.println(ipLookup.administrativeAreaLookupDetail(ip));
            System.out.println(ipLookup.countryLookup(ip));
            System.out.println(ipLookup.locationLookupDetail(ip));
            System.out.println(ipLookup.asnLookup(ip));
            System.out.println(ipLookup.administrativeAreaLookupDetail(ip));

        } catch (NullPointerException npe) {
            logger.error("The MMDB file is not loaded or IP is null! " + npe.getMessage());
        } catch (RuntimeException e) {
            logger.error("Get clientIP location error! " + e.getMessage());
        } finally {
            IoUtil.close(asnInputStream);
            IoUtil.close(ipv4InputStream);
        }
    }

}
