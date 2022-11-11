package com.zdjizhi.tools.general;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.hdfs.HdfsFileUtils;
import com.zdjizhi.utils.IPUtil;
import com.zdjizhi.utils.StringUtil;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

public class IpLookup {
    private static final Log logger = LogFactory.get();
    private static final String LOCATION_SEPARATOR = ".";
    private static final String PRIVATE_IP = "Private IP";
    private static final String UNKNOWN = "";

    private static final String[] FILES = {"ip_v4_built_in.mmdb",
            "ip_v4_user_defined.mmdb",
            "ip_v6_built_in.mmdb",
            "ip_v6_user_defined.mmdb",
            "asn_v4.mmdb",
            "asn_v6.mmdb"};

    private Reader ipLocationPublicReaderV4 = null;
    private Reader ipLocationPublicReaderV6 = null;
    private Reader ipLocationPrivateReaderV4 = null;
    private Reader ipLocationPrivateReaderV6 = null;
    private Reader asnV4Reader = null;
    private Reader asnV6Reader = null;

    /**
     * 初始化定位库，根据文件系统类型判断从何地获取文件。
     */
    public void loadIpLookup() {
        try {
            if (FlowWriteConfig.FILE_SYSTEM_TYPE.equals(FlowWriteConfig.KNOWLEDGEBASE_FILE_SYSTEM_TYPE)) {
                for (String name : FILES) {
                    byte[] fileBytes = HdfsFileUtils.downloadFileByBytes(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + name);
                    if (fileBytes != null) {
                        updateIpLookup(name, fileBytes);
                    }
                }
            } else {
                for (String name : FILES) {
                    byte[] fileBytes = new FileReader(FlowWriteConfig.KNOWLEDGEBASE_LIBRARY + name).readBytes();
                    if (fileBytes != null) {
                        updateIpLookup(name, fileBytes);
                    }
                }
            }

        } catch (RuntimeException e) {
            logger.error("IpLookup init data error, please check! message is:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 更新定位库
     *
     * @param name 文件名
     * @param data 数据
     */
    public void updateIpLookup(String name, byte[] data) {
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(data);
            switch (name) {
                case "ip_v4_built_in.mmdb":
                    this.ipLocationPublicReaderV4 = new Reader(inputStream, new CHMCache());
                    break;
                case "ip_v4_user_defined.mmdb":
                    this.ipLocationPrivateReaderV4 = new Reader(inputStream, new CHMCache());
                    break;
                case "ip_v6_built_in.mmdb":
                    this.ipLocationPublicReaderV6 = new Reader(inputStream, new CHMCache());
                    break;
                case "ip_v6_user_defined.mmdb":
                    this.ipLocationPrivateReaderV6 = new Reader(inputStream, new CHMCache());
                    break;
                case "asn_v4.mmdb":
                    this.asnV4Reader = new Reader(inputStream, new CHMCache());
                    break;
                case "asn_v6.mmdb":
                    this.asnV6Reader = new Reader(inputStream, new CHMCache());
                    break;
            }
        } catch (IOException ioe) {
            logger.error("The IpLookup database data conversion error, please check!");
        } catch (RuntimeException e) {
            logger.error("IpLookup update data error, please check!");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 获取ASN信息
     *
     * @param ip ip地址
     * @return asn信息
     */
    String asnLookup(String ip) {
        JsonNode location = getAsn(ip);
        if (IPUtil.internalIp(ip) || StringUtil.isEmpty(location)) {
            return UNKNOWN;
        } else {
            return StringUtil.setDefaultIfEmpty(location.get("ASN"), UNKNOWN).toString().replace("\"", "");
        }
    }

    /**
     * 获取省市县定位信息
     *
     * @param ip ip地址
     * @return 返回省市县信息
     */
    String cityLookupDetail(String ip) {
        if (IPUtil.internalIp(ip)) {
            return PRIVATE_IP;
        }
        JsonNode location = getLocation(ip);
        if (location == null) {
            return UNKNOWN;
        } else {
            return Joiner.on(LOCATION_SEPARATOR).useForNull("").join(location.get("COUNTRY"),
                    location.get("SUPER_ADMINISTRATIVE_AREA"), location.get("ADMINISTRATIVE_AREA")).replace("\"", "");
        }
    }

    /**
     * 获取国家定位信息
     *
     * @param ip ip地址
     * @return 返回国家信息
     */
    String countryLookup(String ip) {
        if (IPUtil.internalIp(ip)) {
            return PRIVATE_IP;
        }
        JsonNode location = getLocation(ip);
        if (location == null) {
            return UNKNOWN;
        } else {
            return StringUtil.setDefaultIfEmpty(location.get("COUNTRY"), UNKNOWN).toString().replace("\"", "");
        }
    }

    /**
     * 从地理位置定位库获取相关信息
     *
     * @param ip ip地址
     * @return ip对应地理位置信息
     */
    private JsonNode getLocation(String ip) {
        JsonNode jsonNode = null;
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);

            if (IPUtil.isIPAddress(ip)) {

                if (IPUtil.isIPv4Address(ip)) {
                    jsonNode = ipLocationPrivateReaderV4.get(ipAddress);
                    if (StringUtil.isEmpty(jsonNode)) {
                        jsonNode = ipLocationPublicReaderV4.get(ipAddress);
                    }
                } else if (IPUtil.isIPv6Address(ip)) {
                    jsonNode = ipLocationPrivateReaderV6.get(ipAddress);
                    if (StringUtil.isEmpty(jsonNode)) {
                        jsonNode = ipLocationPublicReaderV6.get(ipAddress);
                    }
                }
            }
        } catch (RuntimeException e) {
            logger.error("ip address :" + ip + ", parser error " + e);
        } catch (IOException e) {
            logger.error("IP address " + ip + "of a host could not be determined, parser error " + e);
        }
        return jsonNode;
    }

    /**
     * 从ASN定位库获取相关信息
     *
     * @param ip ip地址
     * @return ip对应ASN信息
     */
    private JsonNode getAsn(String ip) {
        JsonNode jsonNode = null;
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);

            if (IPUtil.isIPAddress(ip)) {

                if (IPUtil.isIPv4Address(ip)) {
                    jsonNode = asnV4Reader.get(ipAddress);
                } else if (IPUtil.isIPv6Address(ip)) {
                    jsonNode = asnV6Reader.get(ipAddress);
                }
            }
        } catch (RuntimeException e) {
            logger.error("IP address :" + ip + ", parser error " + e);
        } catch (IOException e) {
            logger.error("IP address " + ip + "of a host could not be determined, parser error " + e);
        }
        return jsonNode;
    }


}
