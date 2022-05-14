package com.xpc.easyes.core.anno;

import com.xpc.easyes.core.enums.Analyzer;
import com.xpc.easyes.core.enums.FieldStrategy;
import com.xpc.easyes.core.enums.FieldType;
import com.xpc.easyes.core.params.DefaultNestedClass;

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
public @interface TableField {
    /**
     * 自定义字段在es中的名称
     *
     * @return 字段在es中的名称
     */
    String value() default "";

    /**
     * 是否为数据库表字段 默认 true 存在，false 不存在
     *
     * @return 存在
     */
    boolean exist() default true;

    /**
     * 字段在es索引中的类型,建议根据业务场景指定,若不指定则由本框架自动推断
     *
     * @return 类型
     */
    FieldType fieldType() default FieldType.NONE;

    /**
     * 索引文档时用的分词器
     *
     * @return 分词器
     */
    Analyzer analyzer() default Analyzer.NONE;

    /**
     * 查询分词器
     *
     * @return 分词器
     */
    Analyzer searchAnalyzer() default Analyzer.NONE;

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
     * 默认嵌套类
     *
     * @return 默认嵌套类
     */
    Class<?> nestedClass() default DefaultNestedClass.class;
}
