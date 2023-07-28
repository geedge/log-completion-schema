package com.zdjizhi.tools.connections.http;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.zdjizhi.common.FlowWriteConfig;
import com.zdjizhi.tools.exception.FlowWriteException;
import com.zdjizhi.utils.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class HttpClientService {

    private static final Log log = LogFactory.get();

    /**
     * 在调用SSL之前需要重写验证方法，取消检测SSL
     * 创建ConnectionManager，添加Connection配置信息
     *
     * @return HttpClient 支持https
     */
    private PoolingHttpClientConnectionManager getSslClientManager() {
        try {
            // 在调用SSL之前需要重写验证方法，取消检测SSL
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String str) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String str) {
                }
            };
            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[]{trustManager}, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", socketFactory).build();
            // 创建ConnectionManager，添加Connection配置信息
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // 设置最大连接数
            connManager.setMaxTotal(FlowWriteConfig.HTTP_POOL_MAX_CONNECTION);
            // 设置每个连接的路由数
            connManager.setDefaultMaxPerRoute(FlowWriteConfig.HTTP_POOL_MAX_PER_ROUTE);
            return connManager;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new FlowWriteException(e.getMessage());
        }
    }

    /**
     * 获取Http客户端连接对象
     *
     * @param socketTimeOut 响应超时时间
     * @return Http客户端连接对象
     */
    private CloseableHttpClient getHttpClient(int socketTimeOut) {
        // 创建Http请求配置参数
        RequestConfig requestConfig = RequestConfig.custom()
                // 获取连接超时时间
                .setConnectionRequestTimeout(FlowWriteConfig.HTTP_POOL_REQUEST_TIMEOUT)
                // 请求超时时间
                .setConnectTimeout(FlowWriteConfig.HTTP_POOL_CONNECT_TIMEOUT)
                // 响应超时时间
                .setSocketTimeout(socketTimeOut)
                .build();

        /**
         * 测出超时重试机制为了防止超时不生效而设置
         *  如果直接放回false,不重试
         *  这里会根据情况进行判断是否重试
         */
        HttpRequestRetryHandler retry = (exception, executionCount, context) -> {
            if (executionCount >= 3) {// 如果已经重试了3次，就放弃
                return false;
            }
            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                return false;
            }
            if (exception instanceof SocketTimeoutException) {
                if (exception.getMessage().contains("Read timed out")) {
                    return false;
                }
            }
            if (exception instanceof UnknownHostException) {// 目标服务器不可达
                return false;
            }
            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                return false;
            }
            if (exception instanceof SSLException) {// ssl握手异常
                return false;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return true;
            }


            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        };


        ConnectionKeepAliveStrategy myStrategy = (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 60 * 1000;//如果没有约定，则默认定义时长为60s
        };

        // 创建httpClient
        return HttpClients.custom()
                // 把请求相关的超时信息设置到连接客户端
                .setDefaultRequestConfig(requestConfig)
                // 把请求重试设置到连接客户端
                .setRetryHandler(retry)
                .setKeepAliveStrategy(myStrategy)
                // 配置连接池管理对象
                .setConnectionManager(getSslClientManager())
                .build();
    }

    public InputStream httpGetInputStream(String url, int socketTimeout, Header... headers) {
        InputStream result = null;
        // 获取客户端连接对象
        CloseableHttpClient httpClient = getHttpClient(socketTimeout);
        // 创建GET请求对象
        HttpGet httpGet = new HttpGet(url);
        if (StringUtil.isNotEmpty(headers)) {
            for (Header h : headers) {
                httpGet.addHeader(h);
            }
        }
        CloseableHttpResponse response = null;

        try {
            // 执行请求
            response = httpClient.execute(httpGet);
            // 获取响应实体
            result = IOUtils.toBufferedInputStream(response.getEntity().getContent());
            // 获取响应信息
            EntityUtils.consume(response.getEntity());
        } catch (ClientProtocolException e) {
            log.error("current file: {},Protocol error:{}", url, e.getMessage());

        } catch (ParseException e) {
            log.error("current file: {}, Parser error:{}", url, e.getMessage());

        } catch (IOException e) {
            log.error("current file: {},IO error:{}", url, e.getMessage());

        } finally {
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    log.error("Release Connection error:{}", e.getMessage());

                }
            }
            return result;
        }
    }
}
