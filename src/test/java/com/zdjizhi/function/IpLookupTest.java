package com.zdjizhi.function;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.geedgenetworks.utils.GalaxyDataBaseReader;
import com.geedgenetworks.utils.IpLookupV2;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.connections.http.HttpClientService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.net.InetAddress;
import java.util.Map;

/**
 * @author qidaijie
 * @Package com.zdjizhi
 * @Description:
 * @date 2021/11/611:38
 */
public class IpLookupTest {
    private static final Log logger = LogFactory.get();
    private static final String DAT_FILEPATH = "D:\\workerspace\\dat\\";


    @Test
    public void ipLookupDatabaseTest() {

        IpLookupV2.Builder ipLookupBuilder = new IpLookupV2.Builder(false);

        ipLookupBuilder.loadDataFileV4(DAT_FILEPATH + "ip_v4_built_in.mmdb");
//        ipLookupBuilder.loadDataFileV6(DAT_FILEPATH + "ip_v6_built_in.mmdb");
//        ipLookupBuilder.loadDataFilePrivateV4(DAT_FILEPATH + "ip_v4_user_defined.mmdb");
//        ipLookupBuilder.loadDataFilePrivateV6(DAT_FILEPATH + "ip_v6_user_defined.mmdb");
//        ipLookupBuilder.loadAsnDataFile(DAT_FILEPATH + "asn_v4.mmdb");
//        ipLookupBuilder.loadAsnDataFileV6(DAT_FILEPATH + "asn_v6.mmdb");
        IpLookupV2 ipLookup = ipLookupBuilder.build();


//        String ip = "23.200.74.224";
        String ip = "121.14.89.209";

        try {
            System.out.println(ipLookup.cityLookup(ip));
            System.out.println(ipLookup.cityLookupDetail(ip));
            System.out.println(ipLookup.cityLatLngLookup(ip));
            System.out.println(ipLookup.provinceLookup(ip));
            System.out.println(ipLookup.administrativeAreaLookupDetail(ip));
            System.out.println(ipLookup.countryLookup(ip));
            System.out.println(ipLookup.locationLookupDetail(ip));
            System.out.println(ipLookup.administrativeAreaLookupDetail(ip));
            System.out.println(ipLookup.infoLookupToJSONString(ip));

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
            String url = "http://192.168.44.12:9098/hos/knowledge_base_hos_bucket/d2ab3313-1941-4847-84fa-5dbbd8a9007f-aXBfdjRfYnVpbHRfaW4=.mmdb";
            HttpClientService httpClientService = new HttpClientService();
            inputStream = httpClientService.httpGetInputStream(url, 3000);
            Reader reader = new Reader(inputStream, new CHMCache());
            InetAddress ipAddress = InetAddress.getByName("121.14.89.209");
            Map map = reader.get(ipAddress, Map.class);
            System.out.println(map.toString());
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
            String asnUrl = "http://192.168.44.12:9098/hos/knowledge_base_hos_bucket/1b96764c-59dd-4d6b-8edb-623705f708a5-YXNuX3Y0.mmdb";
            String ipv4Url = "http://192.168.44.12:9098/hos/knowledge_base_hos_bucket/24cb6a74-f048-4672-988f-112858427a3b-aXBfdjRfYnVpbHRfaW4=.mmdb";
            asnInputStream = httpClientService.httpGetInputStream(asnUrl, 3000);
            ipv4InputStream = httpClientService.httpGetInputStream(ipv4Url, 3000);

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


    @Test
    public void inputLookupTest() {
//        String ip = "121.14.89.209";
//
        byte[] localFile = new FileReader(DAT_FILEPATH + "built_in_ip_location.mmdb").readBytes();
        String localFileSha256 = DigestUtil.sha256Hex(localFile);
        System.out.println("本地文件SHA256："+localFileSha256);
//
//        IpLookupV2 ipLookup = new IpLookupV2.Builder(false)
//                .loadDataFileV4(DAT_FILEPATH + "built_in_ip_location.mmdb").build();
//
//        System.out.println(ipLookup.infoLookup(ip));

        try {
            HttpClientService httpClientService = new HttpClientService();
            InputStream inputStream = httpClientService.httpGetInputStream("http://192.168.44.55:9098/hos/knowledge_base_hos_bucket/9b1ce6b4-024d-4343-80d5-6e6dc0ad0863-aXA0.mmdb", FlowWriteConfig.HTTP_SOCKET_TIMEOUT);
            byte[] bytes = IOUtils.toByteArray(inputStream);
            String downloadFileSha256 = DigestUtil.sha256Hex(bytes);
            InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            System.out.println("HOS下载文件2 SHA256：" + downloadFileSha256);
            System.out.println("HOS下载文件2 size：" + bytes.length);

//            IpLookupV2 ipLookup2 = new IpLookupV2.Builder(false)
//                    .loadDataFileV4(byteArrayInputStream).build();
            new GalaxyDataBaseReader.Builder(byteArrayInputStream).withCache(new CHMCache()).build();
            InputStream inputStream1 = httpClientService.httpGetInputStream("http://192.168.44.55:9098/hos/knowledge_base_hos_bucket/9b1ce6b4-024d-4343-80d5-6e6dc0ad0863-aXA0.mmdb", FlowWriteConfig.HTTP_SOCKET_TIMEOUT);
            new GalaxyDataBaseReader.Builder(inputStream1).withCache(new CHMCache()).build();
//            System.out.println(ipLookup2.infoLookup(ip));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
