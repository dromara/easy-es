package com.xpc.easyes.core.anno;

import java.lang.annotation.*;

/**
 * 索引注解
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 此处并非像Mybatis-Plus那样为数据库表名,为了屏蔽Es和MySQL差异,此处沿用它的命名,但实际指索引
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface TableName {
    /**
     * 实体对应的表名
     */
    String value() default "";
}
