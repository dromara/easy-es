package com.xpc.easyes.core.common;

import com.xpc.easyes.core.anno.TableField;
import com.xpc.easyes.core.config.GlobalConfig;
import com.xpc.easyes.core.enums.FieldStrategy;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.List;

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
     * 字段名
     */
    private String column;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 表映射结果集
     */
    private String resultMap;
    /**
     * 表主键ID 属性名
     */
    private String keyProperty;
    /**
     * 表主键ID 字段名
     */
    private String keyColumn;
    /**
     * 字段策略 默认，自判断 null
     */
    private final FieldStrategy fieldStrategy;
    /**
     * 表字段信息列表
     */
    private List<EntityFieldInfo> fieldList;


    /**
     * 标记该字段属于哪个类
     */
    private Class<?> clazz;
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


    /**
     * 存在 TableField 注解时, 使用的构造函数
     *
     * @param dbConfig   索引配置
     * @param field      字段
     * @param column     字段名
     * @param tableField 字段注解
     */
    public EntityFieldInfo(GlobalConfig.DbConfig dbConfig, Field field,
                           String column, TableField tableField) {
        this.clazz = field.getDeclaringClass();
        this.column = column;
        /*
         * 优先使用单个字段注解，否则使用全局配置
         */
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
        this.clazz = field.getDeclaringClass();
        this.column = field.getName();
    }
}
