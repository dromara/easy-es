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
    AUTO,
    /**
     * 该类型为未设置主键类型
     */
    NONE,
    /**
     * 全局唯一ID (UUID)
     */
    UUID,
    /**
     * 用户自定义,由用户传入
     */
    CUSTOMIZE;

}
