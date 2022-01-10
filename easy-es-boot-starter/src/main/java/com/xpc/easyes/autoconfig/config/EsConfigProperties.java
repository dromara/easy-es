package com.xpc.easyes.autoconfig.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 基础配置模型
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
@ConfigurationProperties(prefix = "easy-es")
public class EsConfigProperties {
    /**
     * es 连接地址
     */
    private String address;
    /**
     * 模式
     */
    private String schema;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码 可缺省
     */
    private String password;
    /**
     * 最大连接数
     */
    private Integer maxConnTotal;
    /**
     * 最大连接路有数
     */
    private Integer maxConnPerRoute;
    /**
     * 连接超时时间
     */
    private Integer connectTimeout;
    /**
     * Socket 连接超时时间
     */
    private Integer socketTimeout;
    /**
     * 请求超时时间
     */
    private Integer requestTimeout;
    /**
     * 连接请求超时时间
     */
    private Integer connectionRequestTimeout;
}
