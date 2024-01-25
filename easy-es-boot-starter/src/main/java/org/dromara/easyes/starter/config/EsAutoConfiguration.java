package org.dromara.easyes.starter.config;

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
import org.dromara.easyes.common.utils.*;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.util.*;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;
import static org.dromara.easyes.common.utils.RestHighLevelClientUtils.DEFAULT_DS;

/**
 * es自动配置
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Configuration
@ConditionalOnClass(RestHighLevelClient.class)
@EnableConfigurationProperties(value = {DynamicEsProperties.class, EasyEsConfigProperties.class})
@ConditionalOnProperty(prefix = "easy-es", name = {"enable"}, havingValue = "true", matchIfMissing = true)
public class EsAutoConfiguration {
    @Autowired
    private EasyEsConfigProperties easyEsConfigProperties;
    @Autowired
    private DynamicEsProperties dynamicEsProperties;

    /**
     * 装配RestHighLevelClient
     *
     * @return RestHighLevelClient bean
     */
    @Bean
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient() {
        return restHighLevelClient(easyEsConfigProperties);
    }

    @Bean
    public RestHighLevelClientUtils restHighLevelClientUtils() {
        RestHighLevelClientUtils restHighLevelClientUtils = new RestHighLevelClientUtils();
        Map<String, EasyEsConfigProperties> datasourceMap = dynamicEsProperties.getDatasource();
        if (CollectionUtils.isEmpty(datasourceMap)) {
            // 设置默认数据源,兼容不使用多数据源配置场景的老用户使用习惯
            datasourceMap.put(DEFAULT_DS, easyEsConfigProperties);
        }
        for (String key : datasourceMap.keySet()) {
            EasyEsConfigProperties easyEsConfigProperties = datasourceMap.get(key);
            RestHighLevelClientUtils.registerRestHighLevelClient(key, restHighLevelClient(easyEsConfigProperties));
        }
        return restHighLevelClientUtils;
    }


    private RestHighLevelClient restHighLevelClient(EasyEsConfigProperties easyEsConfigProperties) {
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
