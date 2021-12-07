package com.xpc.easyes.core.enums;

/**
 * 主键类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public enum IdType {
    /**
     * es自动生成
     */
    AUTO(0),
    /**
     * 该类型为未设置主键类型
     */
    NONE(1),
    /**
     * 全局唯一ID (UUID)
     */
    UUID(2);

    /**
     * 类型
     */
    private final int key;

    IdType(int key) {
        this.key = key;
    }
}
