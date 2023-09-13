package com.zdjizhi.function;

import com.google.common.net.InternetDomainName;
import com.geedgenetworks.utils.FormatUtils;
import org.junit.Test;

/**
 * @author qidaijie
 * @Package com.zdjizhi.function
 * @Description:
 * @date 2023/7/2413:55
 */
public class TopDomainTest {


    @Test
    public void getTopDomainTest() {
        String host = "heartsofsteel-.tumblr.com";
        System.out.println(FormatUtils.getTopPrivateDomain(host));
        host = "heartsofsteel.tumblr.com";
        System.out.println(FormatUtils.getTopPrivateDomain(host));


//        String sni = "www.googleapis.com";
        String sni = "juicebox-.tumblr.com";
        String domain = FormatUtils.getDomain(sni);
        System.out.println(domain);
        System.out.println(FormatUtils.getTopPrivateDomain(sni));

        System.out.println(InternetDomainName.isValid(sni));
        System.out.println(InternetDomainName.isValid(domain));

        InternetDomainName internetDomainName = InternetDomainName.from(domain);
        if (internetDomainName.isUnderPublicSuffix()) {
            System.out.println(internetDomainName.topPrivateDomain().toString());
        }
    }

}
