package org.dromara.easyes.common.utils;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.dromara.easyes.common.enums.SchemaEnum;
import org.dromara.easyes.common.property.EasyEsProperties;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import javax.net.ssl.SSLContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;

/**
 * @author lyy
 */
public class RestHighLevelClientUtils {

    public static final String DEFAULT_DS = "DEFAULT_DS";

    private final static Map<String, RestHighLevelClient> restHighLevelClientMap = new ConcurrentHashMap<>();

    public RestHighLevelClientUtils() {
    }

    public static RestHighLevelClient getRestHighLevelClient(String restHighLevelClientId) {
        if (DEFAULT_DS.equals(restHighLevelClientId)) {
            return restHighLevelClientMap.values()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> ExceptionUtils.eee("Could not found RestHighLevelClient,restHighLevelClientId:%s", restHighLevelClientId));
        }
        RestHighLevelClient restHighLevelClient = restHighLevelClientMap.get(restHighLevelClientId);
        if (restHighLevelClient == null) {
            LogUtils.formatError("restHighLevelClientId: %s can not find any data source, please check your config", restHighLevelClientId);
            throw ExceptionUtils.eee("Cloud not found RestHighLevelClient,restHighLevelClientId:%s", restHighLevelClientId);
        }
        return restHighLevelClient;
    }

    public RestHighLevelClient getClient(String restHighLevelClientId) {
        return RestHighLevelClientUtils.getRestHighLevelClient(restHighLevelClientId);
    }

    public static void registerRestHighLevelClient(String restHighLevelClientId, RestHighLevelClient restHighLevelClient) {
        RestHighLevelClientUtils.restHighLevelClientMap.putIfAbsent(restHighLevelClientId, restHighLevelClient);
    }

    public static RestHighLevelClient restHighLevelClient(EasyEsProperties easyEsConfigProperties) {
        // 处理地址
        String address = easyEsConfigProperties.getAddress();
        if (StringUtils.isEmpty(address)) {
            throw ExceptionUtils.eee("please config the es address");
        }
        if (!address.contains(COLON)) {
            throw ExceptionUtils.eee("the address must contains port and separate by ':'");
        }
        String schema = StringUtils.isEmpty(easyEsConfigProperties.getSchema())
                ? DEFAULT_SCHEMA : easyEsConfigProperties.getSchema();
        List<HttpHost> hostList = new ArrayList<>();
        Arrays.stream(easyEsConfigProperties.getAddress().split(COMMA))
                .forEach(item -> hostList.add(new HttpHost(item.split(COLON)[0],
                        Integer.parseInt(item.split(COLON)[1]), schema)));

        // 转换成 HttpHost 数组
        HttpHost[] httpHost = hostList.toArray(new HttpHost[]{});

        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            // 设置心跳时间,最大连接数,最大连接路由
            Optional.ofNullable(easyEsConfigProperties.getKeepAliveMillis()).ifPresent(p -> httpClientBuilder.setKeepAliveStrategy((response, context) -> p));
            Optional.ofNullable(easyEsConfigProperties.getMaxConnTotal()).ifPresent(httpClientBuilder::setMaxConnTotal);
            Optional.ofNullable(easyEsConfigProperties.getMaxConnPerRoute()).ifPresent(httpClientBuilder::setMaxConnPerRoute);

            // 设置账号密码
            String username = easyEsConfigProperties.getUsername();
            String password = easyEsConfigProperties.getPassword();
            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(username, password));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }

            // https ssl ignore
            if (SchemaEnum.https.name().equals(schema)) {
                try {
                    // 信任所有
                    SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();
                    SSLIOSessionStrategy sessionStrategy = new SSLIOSessionStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
                    httpClientBuilder.disableAuthCaching();
                    httpClientBuilder.setSSLStrategy(sessionStrategy);
                } catch (Exception e) {
                    LogUtils.error("restHighLevelClient build SSLContext exception: %s", e.getMessage());
                    e.printStackTrace();
                    throw ExceptionUtils.eee(e);
                }
            }
            return httpClientBuilder;
        });

        // 设置超时时间之类的
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            Optional.ofNullable(easyEsConfigProperties.getConnectTimeout()).ifPresent(requestConfigBuilder::setConnectTimeout);
            Optional.ofNullable(easyEsConfigProperties.getSocketTimeout()).ifPresent(requestConfigBuilder::setSocketTimeout);
            Optional.ofNullable(easyEsConfigProperties.getConnectionRequestTimeout())
                    .ifPresent(requestConfigBuilder::setConnectionRequestTimeout);
            return requestConfigBuilder;
        });
        return new RestHighLevelClient(builder);
    }
}