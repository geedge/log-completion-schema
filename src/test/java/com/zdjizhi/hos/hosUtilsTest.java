package com.zdjizhi.hos;

import cn.hutool.core.io.IoUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.zdjizhi.common.CustomFile;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.http.HttpClientUtils;
import com.zdjizhi.utils.StringUtil;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

/**
 * @author qidaijie
 * @Package com.zdjizhi.hos
 * @Description:
 * @date 2022/11/713:55
 */
public class hosUtilsTest {
    @Test
    public void downloadToLocalTest() {
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            HttpClientUtils httpClientUtils = new HttpClientUtils();
            Header header = new BasicHeader("token", FlowWriteConfig.HOS_TOKEN);
            String url = "http://192.168.44.12:9098/hos/knowledge_base_hos_bucket/a69c41bc-c673-46da-a810-f29aa18a3a16-aXBfdjRfYnVpbHRfaW4=.mmdb";
            inputStream = httpClientUtils.httpGetInputStream(url, 3000, header);
            File file = new File(FlowWriteConfig.TOOLS_LIBRARY.concat(File.separator).concat("ip_v4_built_in.mmdb"));
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            outputStream = new FileOutputStream(file);
            IoUtil.copy(inputStream, outputStream);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        } finally {
            IoUtil.close(inputStream);
            IoUtil.close(outputStream);
        }
    }


    @Test
    public void locationTest() {
        try {
            CustomFile customFile = new CustomFile();
            HttpClientUtils httpClientUtils = new HttpClientUtils();
            Header header = new BasicHeader("token", FlowWriteConfig.HOS_TOKEN);
            String url = "http://192.168.44.12:9098/hos/knowledge_base_hos_bucket/a69c41bc-c673-46da-a810-f29aa18a3a16-aXBfdjRfYnVpbHRfaW4=.mmdb";
            InputStream inputStream = httpClientUtils.httpGetInputStream(url, 3000, header);
            customFile.setFileName("ip_v4_built_in.mmdb");
            Reader reader = new Reader(inputStream, new CHMCache());
            InetAddress ipAddress = InetAddress.getByName("114.64.231.114");
            JsonNode jsonNode = reader.get(ipAddress);
            if (jsonNode != null) {
                System.out.println(StringUtil.setDefaultIfEmpty(jsonNode.toString(), "unkonw").toString());
                System.out.println(Joiner.on(".").useForNull("").join(jsonNode.get("COUNTRY"),
                        jsonNode.get("SUPER_ADMINISTRATIVE_AREA"), jsonNode.get("ADMINISTRATIVE_AREA")));
                System.out.println(Joiner.on(".").useForNull("").join(jsonNode.get("COUNTRY"),
                        jsonNode.get("SUPER_ADMINISTRATIVE_AREA"), jsonNode.get("ADMINISTRATIVE_AREA")).replace("\"", ""));
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void asnTest() {
        try {
            CustomFile customFile = new CustomFile();
            HttpClientUtils httpClientUtils = new HttpClientUtils();
            Header header = new BasicHeader("token", FlowWriteConfig.HOS_TOKEN);
            String url = "http://192.168.44.12:9098/hos/knowledge_base_hos_bucket/1b96764c-59dd-4d6b-8edb-623705f708a5-YXNuX3Y0.mmdb";
            InputStream inputStream = httpClientUtils.httpGetInputStream(url, 3000, header);
            customFile.setFileName("asn_v4.mmdb");
            Reader reader = new Reader(inputStream, new CHMCache());
            InetAddress ipAddress = InetAddress.getByName("114.64.231.114");
            JsonNode jsonNode = reader.get(ipAddress);
            if (jsonNode != null) {
                System.out.println(StringUtil.setDefaultIfEmpty(jsonNode.get("ASN"), "unkonw").toString());
                System.out.println(StringUtil.setDefaultIfEmpty(jsonNode.get("ASN"), "unkonw").toString().replace("\"", ""));
                System.out.println(StringUtil.setDefaultIfEmpty(jsonNode.toString(), "unkonw").toString());
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

}
