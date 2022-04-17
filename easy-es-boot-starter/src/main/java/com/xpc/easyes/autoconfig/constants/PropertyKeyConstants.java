package com.xpc.easyes.autoconfig.constants;

/**
 * 属性key常量
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public interface PropertyKeyConstants {
    /**
     * 框架banner是否展示
     */
    String PRINT_DSL = "easy-es.global-config.print-dsl";
    /**
     * 自动托管索引模式
     */
    String PROCESS_INDEX_MODE = "easy-es.global-config.process_index_mode";
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
