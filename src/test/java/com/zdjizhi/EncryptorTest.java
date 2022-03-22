package com.zdjizhi;

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
        String pin = "galaxy2019";
        String encPin = encryptor.encrypt(pin);
        String user = "admin";
        String encUser = encryptor.encrypt(user);
        System.out.println(encPin);
        System.out.println(encUser);
        // 再进行解密：raw_password
        String rawPwd = encryptor.decrypt("6MleDyA3Z73HSaXiKsDJ2k7Ys8YWLhEJ");
        String rawUser = encryptor.decrypt("nsyGpHKGFA4KW0zro9MDdw==");

        System.out.println("The username is: "+rawPwd);
        System.out.println("The pin is: "+rawUser);
    }

}
