package com.xpc.easyes.autoconfig.config;

import com.xpc.easyes.core.config.GlobalConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
public class EasyEsConfigProperties {
    /**
     * 是否开启easy-es 默认开启
     */
    private boolean enable = true;
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
     * connectTimeout 连接超时时间
     */
    private Integer connectTimeout;
    /**
     * socketTimeout 通讯超时时间
     */
    private Integer socketTimeout;
    /**
     * requestTimeout 请求超时时间
     */
    private Integer requestTimeout;
    /**
     * connectionRequestTimeout 连接请求超时时间
     */
    private Integer connectionRequestTimeout;
    /**
     * global config 全局配置
     */
    @NestedConfigurationProperty
    private GlobalConfig globalConfig = new GlobalConfig();
}
