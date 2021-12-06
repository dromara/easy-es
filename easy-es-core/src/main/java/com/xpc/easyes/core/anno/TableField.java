package com.xpc.easyes.core.anno;

import com.xpc.easyes.core.enums.FieldStrategy;

import java.lang.annotation.*;

/**
 * 字段注解
 *
 * @ProjectName: easy-es
 * @Package: com.xpc.easyes.core.config
 * @Description: 处理实体对象字段
 * @Author: xpc
 * @Version: 1.0
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableField {
    /**
     * 是否为数据库表字段
     * 默认 true 存在，false 不存在
     *
     * @return
     */
    boolean exist() default true;

    /**
     * 字段验证策略
     * <p>默认追随全局配置</p>
     */
    FieldStrategy strategy() default FieldStrategy.DEFAULT;
}
