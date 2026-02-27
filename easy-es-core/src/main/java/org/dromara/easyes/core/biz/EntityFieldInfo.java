package org.dromara.easyes.core.biz;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.rely.FieldStrategy;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.common.property.GlobalConfig;

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
     * 缩放因子
     */
    private Integer scalingFactor;
    /**
     * 字段在es中的存储类型
     */
    private FieldType fieldType;
    /**
     * 设置text、keyword_text 可以进行聚合操作
     */
    private boolean fieldData;
    /**
     * 字段是否忽略大小写，默认不忽略 为true时则忽略大小写
     */
    private boolean ignoreCase;
    /**
     * 字段最大索引长度
     */
    private Integer ignoreAbove;
    /**
     * 分词器
     */
    private String analyzer;
    /**
     * 查询分词器
     */
    private String searchAnalyzer;
//    /**
//     * 用户配置的日期格式 例如yyyy-MM-dd HH:mm:ss
//     */
//    private String dateFormat;
    /**
     * 向量的维度大小，不能超过2048
     */
    private Integer dims;
    /**
     * 字段策略 默认，自判断 null
     */
    private final FieldStrategy fieldStrategy;
    /**
     * 父名称
     */
    private String parentName;
    /**
     * 子名称
     */
    private String childName;

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
     * 复制字段
     */
    private List<String> copyToList;

    /**
     * 内部字段列表
     */
    private List<InnerFieldInfo> innerFieldInfoList;

    private Boolean index;

    private Boolean docValues;

    /**
     * 存在 TableField 注解时, 使用的构造函数
     *
     * @param dbConfig   索引配置
     * @param field      字段
     * @param tableField 字段注解
     */
    public EntityFieldInfo(GlobalConfig.DbConfig dbConfig, Field field, IndexField tableField) {
        this.column = field.getName();
        // 优先使用单个字段注解，否则使用全局配置
        if (tableField.strategy() == FieldStrategy.DEFAULT) {
            this.fieldStrategy = dbConfig.getFieldStrategy();
        } else {
            this.fieldStrategy = tableField.strategy();
        }
        this.index = tableField.index();
        this.docValues = tableField.docValues();
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
        this.index = Boolean.TRUE;
        this.docValues = Boolean.TRUE;
    }

    /**
     * 内部字段
     */
    @Data
    public static class InnerFieldInfo {
        /**
         * 内部字段名
         */
        private String column;
        /**
         * 内部字段类型
         */
        private FieldType fieldType;
        /**
         * 内部字段分词器
         */
        private String analyzer;
        /**
         * 内部字段查询分词器
         */
        private String searchAnalyzer;
        /**
         * 字段最大索引长度
         */
        private Integer ignoreAbove;
    }

}
