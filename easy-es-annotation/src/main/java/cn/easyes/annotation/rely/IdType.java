package cn.easyes.annotation.rely;

/**
 * 主键类型枚举
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public enum IdType {
    /**
     * 该类型为未设置主键类型,由es自动生成
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
