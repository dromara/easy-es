package com.xpc.easyes.autoconfig.config;

import com.xpc.easyes.autoconfig.constants.PropertyKeyConstants;
import com.xpc.easyes.core.cache.GlobalConfigCache;
import com.xpc.easyes.core.config.GlobalConfig;
import com.xpc.easyes.core.enums.FieldStrategy;
import com.xpc.easyes.core.enums.IdType;
import com.xpc.easyes.core.toolkit.ExceptionUtils;
import com.xpc.easyes.core.toolkit.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.*;

import static com.xpc.easyes.core.constants.BaseEsConstants.COLON;
import static com.xpc.easyes.core.constants.BaseEsConstants.DEFAULT_SCHEMA;

/**
 * es自动配置
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Configuration
@Order(Integer.MIN_VALUE)
@EnableConfigurationProperties(EsConfigProperties.class)
@ConditionalOnClass(RestHighLevelClient.class)
@ConditionalOnProperty(prefix = "easy-es", name = {"enable"}, havingValue = "true", matchIfMissing = true)
public class EsAutoConfiguration implements InitializingBean, EnvironmentAware, PropertyKeyConstants {
    @Autowired
    private EsConfigProperties esConfigProperties;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestHighLevelClient restHighLevelClient() {
        // 处理地址
        String address = environment.getProperty(ADDRESS);
        if (StringUtils.isEmpty(address)) {
            throw ExceptionUtils.eee("please config the es address");
        }
        if (!address.contains(COLON)) {
            throw ExceptionUtils.eee("the address must contains port and separate by ':'");
        }
        String schema = StringUtils.isEmpty(esConfigProperties.getSchema())
                ? DEFAULT_SCHEMA : esConfigProperties.getSchema();
        List<HttpHost> hostList = new ArrayList<>();
        Arrays.stream(esConfigProperties.getAddress().split(","))
                .forEach(item -> hostList.add(new HttpHost(item.split(":")[0],
                        Integer.parseInt(item.split(":")[1]), schema)));

        // 转换成 HttpHost 数组
        HttpHost[] httpHost = hostList.toArray(new HttpHost[]{});
        // 构建连接对象
        RestClientBuilder builder = RestClient.builder(httpHost);

        // 设置账号密码最大连接数之类的
        String username = esConfigProperties.getUsername();
        String password = esConfigProperties.getPassword();
        Integer maxConnTotal = esConfigProperties.getMaxConnTotal();
        Integer maxConnPerRoute = esConfigProperties.getMaxConnPerRoute();
        boolean needSetHttpClient = (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
                || (Objects.nonNull(maxConnTotal) || Objects.nonNull(maxConnPerRoute));
        if (needSetHttpClient) {
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                Optional.ofNullable(maxConnTotal).ifPresent(httpClientBuilder::setMaxConnTotal);
                Optional.ofNullable(maxConnPerRoute).ifPresent(httpClientBuilder::setMaxConnPerRoute);
                if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    // 设置账号密码
                    credentialsProvider.setCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(esConfigProperties.getUsername(), esConfigProperties.getPassword()));
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
                return httpClientBuilder;
            });
        }

        // 设置超时时间之类的
        Integer connectTimeOut = esConfigProperties.getConnectTimeout();
        Integer socketTimeOut = esConfigProperties.getSocketTimeout();
        Integer requestTimeOut = esConfigProperties.getRequestTimeout();
        Integer connectionRequestTimeOut = esConfigProperties.getConnectionRequestTimeout();
        boolean needSetRequestConfig = Objects.nonNull(connectTimeOut) || Objects.nonNull(requestTimeOut)
                || Objects.nonNull(connectionRequestTimeOut);
        if (needSetRequestConfig) {
            builder.setRequestConfigCallback(requestConfigBuilder -> {
                Optional.ofNullable(connectTimeOut).ifPresent(requestConfigBuilder::setConnectTimeout);
                Optional.ofNullable(socketTimeOut).ifPresent(requestConfigBuilder::setSocketTimeout);
                Optional.ofNullable(connectionRequestTimeOut)
                        .ifPresent(requestConfigBuilder::setConnectionRequestTimeout);
                return requestConfigBuilder;
            });
        }

        return new RestHighLevelClient(builder);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        GlobalConfig globalConfig = new GlobalConfig();
        Optional.ofNullable(environment.getProperty(PRINT_DSL))
                .ifPresent(p -> globalConfig.setPrintDsl(Boolean.parseBoolean(p)));
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        Optional.ofNullable(environment.getProperty(TABLE_PREFIX)).ifPresent(dbConfig::setTablePrefix);
        Optional.ofNullable(environment.getProperty(MAP_UNDERSCORE_TO_CAMEL_CASE))
                .ifPresent(p -> dbConfig.setMapUnderscoreToCamelCase(Boolean.parseBoolean(p)));
        Optional.ofNullable(environment.getProperty(ID_TYPE))
                .ifPresent(i -> dbConfig.setIdType(Enum.valueOf(IdType.class, i.toUpperCase(Locale.ROOT))));
        Optional.ofNullable(environment.getProperty(FIELD_STRATEGY))
                .ifPresent(f -> dbConfig.setFieldStrategy(Enum.valueOf(FieldStrategy.class, f.toUpperCase(Locale.ROOT))));
        globalConfig.setDbConfig(dbConfig);
        GlobalConfigCache.setGlobalConfig(globalConfig);
    }
}
