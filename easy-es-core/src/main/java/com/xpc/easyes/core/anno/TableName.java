package com.xpc.easyes.core.anno;

import java.lang.annotation.*;

/**
 * 索引注解
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface TableName {
    /**
     * 实体对应的表名
     *
     * @return 默认为空
     */
    String value() default "";
}
