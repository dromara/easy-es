package com.xpc.easyes.autoconfig.constants;

/**
 * 属性key常量
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
    String PRINT_DSL = "easy-es.global-config.print-dsl";
    /**
     * es索引前缀
     */
    String TABLE_PREFIX = "easy-es.global-config.db-config.table-prefix";
    /**
     * 下划线转驼峰
     */
    String MAP_UNDERSCORE_TO_CAMEL_CASE = "easy-es.global-config.db-config.map-underscore-to-camel-case";
    /**
     * es id类型
     */
    String ID_TYPE = "easy-es.global-config.db-config.id-type";
    /**
     * es 字段策略
     */
    String FIELD_STRATEGY = "easy-es.global-config.db-config.field-strategy";
}
