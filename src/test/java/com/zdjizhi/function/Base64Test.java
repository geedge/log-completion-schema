package com.zdjizhi.function;

import cn.hutool.core.codec.Base64;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * @author qidaijie
 * @Package com.zdjizhi.function
 * @Description:
 * @date 2022/11/39:36
 */
public class Base64Test {
    private static final Log logger = LogFactory.get();

    /**
     * 根据编码解码base64(hutool)

     * @return 解码字符串
     */
    @Test
    public void decodeBase64Hutool() {
        try {
            System.out.println(Base64.decodeStr("bWFpbF90ZXN0X2VuZ2xpc2gudHh0"));
            System.out.println(Base64.decodeStr("aGVsbG8="));
        } catch (RuntimeException e) {
            logger.error("Resolve Base64 exception, exception information:" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 根据编码解码base64

     * @return 解码字符串
     */
    @Test
    public void encodeBase64() {
        try {
            System.out.println(java.util.Base64.getUrlEncoder().encodeToString("runoob?java8".getBytes("ISO-8859-1")));
            System.out.println(java.util.Base64.getUrlEncoder().encodeToString("runoob?java8".getBytes("utf-8")));

        } catch (RuntimeException e) {
            logger.error("Resolve Base64 exception, exception information:" + e.getMessage());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据编码解码base64

     * @return 解码字符串
     */
    @Test
    public void decodeBase64() {
        try {
            byte[] base64decodedBytes = java.util.Base64.getDecoder().decode("bWFpbF90ZXN0X2VuZ2xpc2gudHh0");

            System.out.println("原始字符串: " + new String(base64decodedBytes, "utf-8"));
            System.out.println("原始字符串: " + new String(base64decodedBytes));

        } catch (RuntimeException e) {
            logger.error("Resolve Base64 exception, exception information:" + e.getMessage());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
