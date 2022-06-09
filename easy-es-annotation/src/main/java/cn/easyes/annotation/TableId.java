package cn.easyes.annotation;

import cn.easyes.common.enums.IdType;

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
public @interface TableId {
    /**
     * 字段值
     *
     * @return es默认_id
     */
    String value() default "_id";

    /**
     * 主键ID
     *
     * @return 默认为未设置
     */
    IdType type() default IdType.NONE;
}
