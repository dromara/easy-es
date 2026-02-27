package org.dromara.easyes.annotation;


import org.dromara.easyes.annotation.rely.Analyzer;
import org.dromara.easyes.annotation.rely.DefaultNestedOrObjectClass;
import org.dromara.easyes.annotation.rely.FieldStrategy;
import org.dromara.easyes.annotation.rely.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段注解
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface IndexField {
    /**
     * 自定义字段在es中的名称
     *
     * @return 字段在es中的名称
     */
    String value() default "";

    /**
     * 是否为索引字段 默认 true 存在，false 不存在
     *
     * @return 存在
     */
    boolean exist() default true;

    /**
     * 用于将多个字段的值复制到一个新的字段中，以便在查询时可以简化查询语句，提高检索效率
     *
     * @return 复制当前字段至指定字段中
     */
    String[] copyTo() default {};

    /**
     * 字段在es索引中的类型,建议根据业务场景指定,若不指定则由本框架自动推断
     *
     * @return 类型
     */
    FieldType fieldType() default FieldType.NONE;

    /**
     * 设置text、keyword_text 可以进行聚合操作
     *
     * @return 是否设置可聚合
     */
    boolean fieldData() default false;

    /**
     * 索引文档时用的分词器
     *
     * @return 分词器
     */
    String analyzer() default Analyzer.NONE;

    /**
     * 查询分词器
     *
     * @return 分词器
     */
    String searchAnalyzer() default Analyzer.NONE;

    /**
     * 字段验证策略
     *
     * @return 默认策略
     */
    FieldStrategy strategy() default FieldStrategy.DEFAULT;

    /**
     * es索引中的日期格式
     *
     * @return 日期格式 例如yyyy-MM-dd HH:mm:ss
     */
    String dateFormat() default "";

    /**
     * 是否忽略大小写 默认 false 不忽略，为true时则大小写不敏感，都可查
     *
     * @return 是否忽略大小写
     */
    boolean ignoreCase() default false;

    /**
     * 长度超过ignore_above设置的字符串将不会被索引或存储 keyword_text默认值为256
     *
     * @return 索引字段最大长度
     */
    int ignoreAbove() default -1;

    /**
     * 向量的维度大小，不能超过2048 且非负 字段类型为dense_vector时必须指定此字段值,否则索引无法正确创建
     *
     * @return 向量的维度大小
     */
    int dims() default -1;

    /**
     * 用于指定浮点数字段的缩放因子 缩放因子用于将浮点数值映射到整数值以进行存储和索引,取值范围是 1 到 10000 针对BigDecimal类型字段,不指定时默认值为 100
     *
     * @return 缩放因子
     */
    int scalingFactor() default -1;

    /**
     * 默认嵌套类
     *
     * @return 默认嵌套类
     */
    Class<?> nestedOrObjectClass() default DefaultNestedOrObjectClass.class;

    /**
     * false时禁用倒排索引，无法搜索
     * @return 是否启用
     */
    boolean index() default true;

    /**
     * false时禁用列式存储，不可排序、聚合或脚本访问
     * @return 是否启用
     */
    boolean docValues() default true;
}
