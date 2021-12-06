package com.xpc.easyes.autoconfig.constants;

/**
 * 属性key常量
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 存放基础配置中的key
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface PropertyKeyConstants {
    /**
     * es 地址
     */
    String ADDRESS = "easy-es.address";
    /**
     * 属性
     */
    String SCHEMA = "easy-es.schema";
    /**
     * es用户名
     */
    String USERNAME = "easy-es.username";
    /**
     * es密码
     */
    String PASSWORD = "easy-es.password";
    /**
     * 框架banner是否展示
     */
    String BANNER = "easy-es.global-config.db-config.banner";
    /**
     * es索引前缀
     */
    String TABLE_PREFIX = "easy-es.global-config.db-config.table-prefix";
    /**
     * es id类型
     */
    String ID_TYPE = "easy-es.global-config.db-config.id-type";
    /**
     * es 字段策略
     */
    String FIELD_STRATEGY = "easy-es.global-config.db-config.field-strategy";
}
