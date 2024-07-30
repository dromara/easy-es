package org.dromara.easyes.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.dromara.easyes.annotation.rely.AnnotationConstants.DEFAULT_JOIN_FIELD_NAME;

/**
 * 父子类型
 * <p>
 * Copyright © 2024 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Join {
    /**
     * 父子类型急切全局序数 默认值为true, 表示在创建索引时，会为该字段创建全局序数，从而加快搜索速度
     *
     * @return 是否开启急切全局序数
     */
    boolean eagerGlobalOrdinals() default true;

    /**
     * join字段在es中的名字
     *
     * @return 索引中的join字段名称 默认为joinField
     */
    String joinField() default DEFAULT_JOIN_FIELD_NAME;

    /**
     * 根节点别名 不指定则默认使用加了当前注解的根类的名称小写作为根节点别名(推荐)
     *
     * @return 根节点别名
     */
    String rootAlias() default "";

    /**
     * 非根节点
     *
     * @return 非根节点列表
     */
    Node[] nodes() default {};
}
