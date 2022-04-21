package com.xpc.easyes.core.common;

import com.alibaba.fastjson.serializer.NameFilter;
import com.xpc.easyes.core.anno.TableField;
import com.xpc.easyes.core.config.GlobalConfig;
import com.xpc.easyes.core.enums.Analyzer;
import com.xpc.easyes.core.enums.FieldStrategy;
import com.xpc.easyes.core.enums.FieldType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;

/**
 * es实体字段信息
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Data
public class EntityFieldInfo {
    /**
     * 忽略的字段
     */
    private String ignoreColumn;
    /**
     * 实体类字段名
     */
    private String column;
    /**
     * 字段类型,如Integer
     */
    private String columnType;
    /**
     * es中的字段名
     */
    private String mappingColumn;
    /**
     * 自动在es中的存储类型
     */
    private FieldType fieldType;
    /**
     * 分词器
     */
    private Analyzer analyzer;
    /**
     * 查询分词器
     */
    private Analyzer searchAnalyzer;
    /**
     * 用户配置的日期格式 例如yyyy-MM-dd HH:mm:ss
     */
    private String dateFormat;
    /**
     * 字段策略 默认，自判断 null
     */
    private final FieldStrategy fieldStrategy;

    /**
     * 缓存包含主键及字段的 sql select
     */
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private String allSqlSelect;
    /**
     * 缓存主键字段的 sql select
     */
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private String sqlSelect;
    private NameFilter nameFilter;

    /**
     * 存在 TableField 注解时, 使用的构造函数
     *
     * @param dbConfig   索引配置
     * @param field      字段
     * @param tableField 字段注解
     */
    public EntityFieldInfo(GlobalConfig.DbConfig dbConfig, Field field, TableField tableField) {
        this.column = field.getName();

        // 优先使用单个字段注解，否则使用全局配置
        if (tableField.strategy() == FieldStrategy.DEFAULT) {
            this.fieldStrategy = dbConfig.getFieldStrategy();
        } else {
            this.fieldStrategy = tableField.strategy();
        }
    }

    /**
     * 不存在 TableField 注解时, 使用的构造函数
     *
     * @param dbConfig 索引配置
     * @param field    字段
     */
    public EntityFieldInfo(GlobalConfig.DbConfig dbConfig, Field field) {
        this.fieldStrategy = dbConfig.getFieldStrategy();
        this.column = field.getName();
    }

}
