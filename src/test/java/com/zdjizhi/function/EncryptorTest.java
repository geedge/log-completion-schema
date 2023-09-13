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
        System.out.println("------------------------------------------------------");

        System.out.println("The vknRT6U4I739rLIha9CvojM+4uFyXZLEYpO2HZayLnRak1HPW0K2yZ3vnQBA2foo decryption result is: "+encryptor.decrypt("vknRT6U4I739rLIha9CvojM+4uFyXZLEYpO2HZayLnRak1HPW0K2yZ3vnQBA2foo"));
        System.out.println("The SU05WiYTLLrF+cVZ410gJdrfEgxgnYG9RGsI+3dZZq54XFLOXEzOvRuMAvLlBf4k decryption result is: "+encryptor.decrypt("SU05WiYTLLrF+cVZ410gJdrfEgxgnYG9RGsI+3dZZq54XFLOXEzOvRuMAvLlBf4k"));
        System.out.println("The qUA355VopKSx6kwwwXZwqWWEYSu76Slz decryption result is: "+encryptor.decrypt("qUA355VopKSx6kwwwXZwqWWEYSu76Slz"));
        System.out.println("The sJ9iiTeE/7moI2hKn8asMg== decryption result is: "+encryptor.decrypt("sJ9iiTeE/7moI2hKn8asMg=="));
        System.out.println("The 63aTpwv2vH0vPikW+3Jjig== decryption result is: "+encryptor.decrypt("63aTpwv2vH0vPikW+3Jjig=="));
        System.out.println("The Ei1P4R1e5KTdJR+ZVnBmug== decryption result is: "+encryptor.decrypt("Ei1P4R1e5KTdJR+ZVnBmug=="));
        System.out.println("The iW8ekP1SZC6v/7cfJKAqXXrjApJox+cH decryption result is: "+encryptor.decrypt("iW8ekP1SZC6v/7cfJKAqXXrjApJox+cH"));
        System.out.println("The TV7Jm4dQCE/LJznp4iTm4ICkBscquv9G decryption result is: "+encryptor.decrypt("TV7Jm4dQCE/LJznp4iTm4ICkBscquv9G"));
        System.out.println("The LDEb2OekU7iZWiFw6pUYBSozVKP27r1y decryption result is: "+encryptor.decrypt("LDEb2OekU7iZWiFw6pUYBSozVKP27r1y"));

        StandardPBEStringEncryptor encryptorCM = new StandardPBEStringEncryptor();
        encryptorCM.setPassword("bifang-api");
        System.out.println("The Zp65tFN3CsAXwpNfgfjZ0PrquSrokM1c decryption result is: "+encryptorCM.decrypt("Zp65tFN3CsAXwpNfgfjZ0PrquSrokM1c"));
    }

}
