package org.dromara.easyes.annotation;

import org.dromara.easyes.annotation.rely.IdType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主键注解
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IndexId {

    /**
     * 自定义字段在es中的名称
     *
     * @return 字段在es中的名称
     */
    String value() default "";

    /**
     * 主键ID
     *
     * @return 默认为未设置
     */
    IdType type() default IdType.NONE;

    /**
     * 是否将主键写入到source中
     * @return 默认写入
     */
    boolean writeToSource() default true;
}
