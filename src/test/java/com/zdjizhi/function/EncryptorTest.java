package com.zdjizhi.function;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;

/**
 * @author qidaijie
 * @Package com.zdjizhi
 * @Description:
 * @date 2022/3/1610:55
 */
public class EncryptorTest {


    @Test
    public void passwordTest(){
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        // 配置加密解密的密码/salt值
        encryptor.setPassword("galaxy");
        // 对"raw_password"进行加密：S5kR+Y7CI8k7MaecZpde25yK8NKUnd6p
        String kafkaUser = encryptor.encrypt("admin");
        String kafkaPin = encryptor.encrypt("galaxy2019");
        String nacosPin = encryptor.encrypt("nacos");
        String nacosUser = encryptor.encrypt("nacos");

        System.out.println("Kafka:\n"+"The username is: "+kafkaUser);
        System.out.println("The pin is: "+kafkaPin);
        System.out.println("Nacos:\n"+"The username is: "+nacosUser);
        System.out.println("The pin is: "+nacosPin);
        // 再进行解密：raw_password
        System.out.println("Kafka:\n"+"The username is: "+encryptor.decrypt(kafkaUser));
        System.out.println("The pin is: "+encryptor.decrypt(kafkaPin));

        System.out.println("Nacos:\n"+"The username is: "+encryptor.decrypt(nacosUser));
        System.out.println("The pin is: "+encryptor.decrypt(nacosPin));
    }

}
