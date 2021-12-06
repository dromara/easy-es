package com.xpc.easyes.autoconfig.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 基础配置模型
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 用于存放es连接所需基础信息
 * @Author: xpc
 * @Version: 1.0
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
}
