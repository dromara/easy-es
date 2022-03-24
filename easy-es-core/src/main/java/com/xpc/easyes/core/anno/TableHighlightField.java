package com.xpc.easyes.core.anno;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 高亮字段注解
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableHighlightField {
    /**
     * 高亮字段对应源数据库表字段
     *
     * @return 存在
     */
    String value();

}
