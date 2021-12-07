package com.xpc.easyes.core.anno;

import com.xpc.easyes.core.enums.FieldStrategy;

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
@Target(ElementType.FIELD)
public @interface TableField {
    /**
     * 是否为数据库表字段 默认 true 存在，false 不存在
     *
     * @return 存在
     */
    boolean exist() default true;

    /**
     * 字段验证策略
     *
     * @return 默认策略
     */
    FieldStrategy strategy() default FieldStrategy.DEFAULT;
}
