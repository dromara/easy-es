package com.xpc.easyes.core.enums;

/**
 * 字段策略枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public enum FieldStrategy {
    /**
     * 忽略判断
     */
    IGNORED,
    /**
     * 非NULL判断
     */
    NOT_NULL,
    /**
     * 非空判断
     */
    NOT_EMPTY,
    /**
     * 默认的,一般只用于注解里
     */
    DEFAULT;
}
