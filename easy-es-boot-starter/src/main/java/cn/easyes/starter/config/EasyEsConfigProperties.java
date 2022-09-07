package cn.easyes.starter.config;

import cn.easyes.core.config.GlobalConfig;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * easy-es基础配置项
 * <p>
 * Copyright © 2022 xpc1024 All Rights Reserved
 **/
@Data
@Configuration
@ConfigurationProperties(value = "easy-es")
@ConditionalOnProperty(prefix = "easy-es", name = {"enable"}, havingValue = "true", matchIfMissing = true)
public class EasyEsConfigProperties {
    /**
     * 是否开启easy-es 默认开启
     */
    private boolean enable = true;
    /**
     * 是否开启easy-es LOGO BANNER的打印
     */
    private boolean banner = true;
    /**
     * es client address es客户端地址
     */
    private String address = "127.0.0.1:9200";
    /**
     * schema 模式
     */
    private String schema = "http";
    /**
     * username of es 用户名,可缺省
     */
    private String username;
    /**
     * password of es 密码,可缺省
     */
    private String password;
    /**
     * maxConnectTotal 最大连接数
     */
    private Integer maxConnTotal;
    /**
     * maxConnectPerRoute 最大连接路由数
     */
    private Integer maxConnPerRoute;
    /**
     * connectTimeout timeUnit:millis 连接超时时间 单位毫秒
     */
    private Integer connectTimeout;
    /**
     * socketTimeout timeUnit:millis  通讯超时时间 单位毫秒
     */
    private Integer socketTimeout;
    /**
     * requestTimeout timeUnit:millis  请求超时时间 单位毫秒
     */
    private Integer requestTimeout;
    /***
     * 保持心跳时间 timeUnit:millis  单位毫秒
     */
    private Integer keepAliveMillis;
    /**
     * connectionRequestTimeout timeUnit:millis 连接请求超时时间 单位毫秒
     */
    private Integer connectionRequestTimeout;
    /**
     * global config 全局配置
     */
    @NestedConfigurationProperty
    private GlobalConfig globalConfig = new GlobalConfig();
}
