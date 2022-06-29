package cn.easyes.annotation;

import cn.easyes.common.constants.Analyzer;
import cn.easyes.common.enums.FieldStrategy;
import cn.easyes.common.enums.FieldType;
import cn.easyes.common.params.DefaultNestedClass;
import cn.easyes.common.params.JoinField;

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
     * 默认嵌套类
     *
     * @return 默认嵌套类
     */
    Class<?> nestedClass() default DefaultNestedClass.class;

    /**
     * 父名称
     *
     * @return 父名称
     */
    String parentName() default "";

    /**
     * 子名称
     *
     * @return 子名称
     */
    String childName() default "";

    /**
     * 父子类型关系字段类 如果使用自定义的类,需要在此处指明,否则采用默认(推荐)
     *
     * @return 默认子类
     */
    Class<?> joinFieldClass() default JoinField.class;
}
